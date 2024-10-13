/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sourceforge.lame;


/*
PSYCHO ACOUSTICS


This routine computes the psycho acoustics, delayed by one granule.

Input: buffer of PCM data (1024 samples).

This window should be centered over the 576 sample granule window.
The routine will compute the psycho acoustics for
this granule, but return the psycho acoustics computed
for the *previous* granule.  This is because the block
type of the previous granule can only be determined
after we have computed the psycho acoustics for the following
granule.

Output:  maskings and energies for each scalefactor band.
block type, PE, and some correlation measures.
The PE is used by CBR modes to determine if extra bits
from the bit reservoir should be used.  The correlation
measures are used to determine mid/side or regular stereo.
*/
/*
Notation:

barks:  a non-linear frequency scale.  Mapping from frequency to
        barks is given by freq2bark()

scalefactor bands: The spectrum (frequencies) are broken into
                   SBMAX "scalefactor bands".  Thes bands
                   are determined by the MPEG ISO spec.  In
                   the noise shaping/quantization code, we allocate
                   bits among the partition bands to achieve the
                   best possible quality

partition bands:   The spectrum is also broken into about
                   64 "partition bands".  Each partition
                   band is about .34 barks wide.  There are about 2-5
                   partition bands for each scalefactor band.

LAME computes all psycho acoustic information for each partition
band.  Then at the end of the computations, this information
is mapped to scalefactor bands.  The energy in each scalefactor
band is taken as the sum of the energy in all partition bands
which overlap the scalefactor band.  The maskings can be computed
in the same way (and thus represent the average masking in that band)
or by taking the minmum value multiplied by the number of
partition bands used (which represents a minimum masking in that band).
*/
/*
The general outline is as follows:

1. compute the energy in each partition band
2. compute the tonality in each partition band
3. compute the strength of each partion band "masker"
4. compute the masking (via the spreading function applied to each masker)
5. Modifications for mid/side masking.

Each partition band is considiered a "masker".  The strength
of the i'th masker in band j is given by:

    s3(bark(i)-bark(j))*strength(i)

The strength of the masker is a function of the energy and tonality.
The more tonal, the less masking.  LAME uses a simple linear formula
(controlled by NMT and TMN) which says the strength is given by the
energy divided by a linear function of the tonality.
*/
/*
s3() is the "spreading function".  It is given by a formula
determined via listening tests.

The total masking in the j'th partition band is the sum over
all maskings i.  It is thus given by the convolution of
the strength with s3(), the "spreading function."

masking(j) = sum_over_i  s3(i-j)*strength(i)  = s3 o strength

where "o" = convolution operator.  s3 is given by a formula determined
via listening tests.  It is normalized so that s3 o 1 = 1.

Note: instead of a simple convolution, LAME also has the
option of using "additive masking"

The most critical part is step 2, computing the tonality of each
partition band.  LAME has two tonality estimators.  The first
is based on the ISO spec, and measures how predictiable the
signal is over time.  The more predictable, the more tonal.
The second measure is based on looking at the spectrum of
a single granule.  The more peaky the spectrum, the more
tonal.  By most indications, the latter approach is better.

Finally, in step 5, the maskings for the mid and side
channel are possibly increased.  Under certain circumstances,
noise in the mid & side channels is assumed to also
be masked by strong maskers in the L or R channels.


Other data computed by the psy-model:

ms_ratio        side-channel / mid-channel masking ratio (for previous granule)
ms_ratio_next   side-channel / mid-channel masking ratio for this granule

percep_entropy[2]     L and R values (prev granule) of PE - A measure of how
                      much pre-echo is in the previous granule
percep_entropy_MS[2]  mid and side channel values (prev granule) of percep_entropy
energy[4]             L,R,M,S energy in each channel, prev granule
blocktype_d[2]        block type to use for previous granule
*/

// psymodel.c

class PsyModel {
	private static final int rpelev = 2;
	private static final int rpelev2 = 16;
	//private static final int rpelev_s = 2;
	//private static final int rpelev2_s = 16;

	/* size of each partition band, in barks: */
	private static final float DELBARK = .34f;

	/* tuned for output level (sensitive to energy scale) */
	private static final float VO_SCALE = (1.f / (14752 * 14752) / (Encoder.BLKSIZE / 2));

	private static final float temporalmask_sustain_sec = 0.01f;

	private static final float NS_PREECHO_ATT0 = 0.8f;
	private static final float NS_PREECHO_ATT1 = 0.6f;
	private static final float NS_PREECHO_ATT2 = 0.3f;

	private static final float NS_MSFIX = 3.5f;
	private static final float NSATTACKTHRE = 4.4f;
	private static final float NSATTACKTHRE_S = 25;

	private static final int NSFIRLEN = 21;

	// #define  LN_TO_LOG10  0.2302585093
	private static final double LN_TO_LOG10 = Math.log( 10. ) / 10.;

/*
   L3psycho_anal.  Compute psycho acoustics.

   Data returned to the calling program must be delayed by one
   granule.

   This is done in two places.
   If we do not need to know the blocktype, the copying
   can be done here at the top of the program: we copy the data for
   the last granule (computed during the last call) before it is
   overwritten with the new data.  It looks like this:

   0. static psymodel_data
   1. calling_program_data = psymodel_data
   2. compute psymodel_data

   For data which needs to know the blocktype, the copying must be
   done at the end of this loop, and the old values must be saved:

   0. static psymodel_data_old
   1. compute psymodel_data
   2. compute possible block type of this granule
   3. compute final block type of previous granule based on #2.
   4. calling_program_data = psymodel_data_old
   5. psymodel_data_old = psymodel_data
*/

/* psycho_loudness_approx
   jd - 2001 mar 12
in:  energy   - BLKSIZE/2 elements of frequency magnitudes ^ 2
     gfp      - uses out_samplerate, ATHtype (also needed for ATHformula)
returns: loudness^2 approximation, a positive value roughly tuned for a value
         of 1.0 for signals near clipping.
notes:   When calibrated, feeding this function binary white noise at sample
         values +32767 or -32768 should return values that approach 3.
         ATHformula is used to approximate an equal loudness curve.
future:  Data indicates that the shape of the equal loudness curve varies
         with intensity.  This function might be improved by using an equal
         loudness curve shaped for typical playback levels (instead of the
         ATH, that is shaped for the threshold).  A flexible realization might
         simply bend the existing ATH curve to achieve the desired shape.
         However, the potential gain may not be enough to justify an effort.
*/
	private static final float psycho_loudness_approx(final float[] energy, final float[] eql_w) {
		float loudness_power = 0.0f;
		/* apply weights to power in freq. bands */
		for(int i = 0; i < Encoder.BLKSIZE / 2; ++i ) {
			loudness_power += energy[i] * eql_w[i];
		}
		loudness_power *= VO_SCALE;

		return loudness_power;
	}

/* mask_add optimization */
/* init the limit values used to avoid computing log in mask_add when it is not necessary */

/* For example, with i = 10*log10(m2/m1)/10*16         (= log10(m2/m1)*16)
 *
 * abs(i)>8 is equivalent (as i is an integer) to
 * abs(i)>=9
 * i>=9 || i<=-9
 * equivalent to (as i is the biggest integer smaller than log10(m2/m1)*16
 * or the smallest integer bigger than log10(m2/m1)*16 depending on the sign of log10(m2/m1)*16)
 * log10(m2/m1)>=9/16 || log10(m2/m1)<=-9/16
 * exp10 is strictly increasing thus this is equivalent to
 * m2/m1 >= 10^(9/16) || m2/m1<=10^(-9/16) which are comparisons to constants
 */

	private static final int I1LIMIT = 8;       /* as in if(i>8)  */
	private static final int I2LIMIT = 23;      /* as in if(i>24) . changed 23 */
	//private static final int MLIMIT  = 15;      /* as in if(m<15) */

	/** pow(10, (I1LIMIT + 1) / 16.0); */
	private static final float ma_max_i1 = 3.6517412725483771f;
	/** pow(10, (I2LIMIT + 1) / 16.0); */
	private static final float ma_max_i2 = 31.622776601683793f;
	/** pow(10, (MLIMIT) / 10.0); */
	private static final float ma_max_m  = 31.622776601683793f;

	/*This is the masking table:
	   According to tonality, values are going from 0dB (TMN)
	   to 9.3dB (NMT).
	   After additive masking computation, 8dB are added, so
	   final values are going from 8dB to 17.3dB
	 */
	private static final float tab[] = {
		1.0f /*pow(10, -0) */ ,
		0.79433f /*pow(10, -0.1) */ ,
		0.63096f /*pow(10, -0.2) */ ,
		0.63096f /*pow(10, -0.2) */ ,
		0.63096f /*pow(10, -0.2) */ ,
		0.63096f /*pow(10, -0.2) */ ,
		0.63096f /*pow(10, -0.2) */ ,
		0.25119f /*pow(10, -0.6) */ ,
		0.11749f             /*pow(10, -0.93) */
	};

	private static final int tab_mask_add_delta[] = { 2, 2, 2, 1, 1, 1, 0, 0, -1 };
	// #define STATIC_ASSERT_EQUAL_DIMENSION(A,B) enum{static_assert_##A=1/((dimension_of(A) == dimension_of(B))?1:0)}

	/*private static final int mask_add_delta(final int i) {// java: extracted inplace
		// STATIC_ASSERT_EQUAL_DIMENSION(tab_mask_add_delta,tab);
		// assert(i < (int)dimension_of(tab));
		return tab_mask_add_delta[i];
	}*/

	private static final void init_mask_add_max_values() {
/* #ifndef NDEBUG
		final float _ma_max_i1 = (float)Math.pow( 10, (I1LIMIT + 1) / 16.0 );
		final float _ma_max_i2 = (float)Math.pow(10, (I2LIMIT + 1) / 16.0);
		final float _ma_max_m = (float)Math.pow(10, (MLIMIT) / 10.0);
		assert( Math.abs(ma_max_i1 - _ma_max_i1) <= FLT_EPSILON );
		assert( Math.abs(ma_max_i2 - _ma_max_i2) <= FLT_EPSILON );
		assert( Math.abs(ma_max_m  - _ma_max_m ) <= FLT_EPSILON );
#endif */
	}

	private static final float table2[] = {
		1.33352f * 1.33352f, 1.35879f * 1.35879f, 1.38454f * 1.38454f, 1.39497f * 1.39497f,
		1.40548f * 1.40548f, 1.3537f * 1.3537f, 1.30382f * 1.30382f, 1.22321f * 1.22321f,
		1.14758f * 1.14758f,
		1f
	};

	/** addition of simultaneous masking   Naoki Shibata 2000/7 */
	private static final float vbrpsy_mask_add(float m1, float m2, final int b, final int delta) {
		if( m1 < 0 ) {
			m1 = 0;
		}
		if( m2 < 0 ) {
			m2 = 0;
		}
		if( m1 <= 0 ) {
			return m2;
		}
		if( m2 <= 0 ) {
			return m1;
		}
		float ratio;
		if( m2 > m1 ) {
			ratio = m2 / m1;
		} else {
			ratio = m1 / m2;
		}
		if( ( b >= 0 ? b : -b ) <= delta ) {       /* approximately, 1 bark = 3 partitions */
			/* originally 'if(i > 8)' */
			if( ratio >= ma_max_i1 ) {
				return m1 + m2;
			} else {
				final int i = (int) ((float)Math.log10( (double)ratio ) * (16.0f));// (Util.FAST_LOG10_X( ratio, 16.0f ));
				return (m1 + m2) * table2[i];
			}
		}
		if( ratio < ma_max_i2 ) {
			return m1 + m2;
		}
		if( m1 < m2 ) {
			m1 = m2;
		}
		return m1;
	}

	/** short block threshold calculation (part 2)

    partition band bo_s[sfb] is at the transition from scalefactor
    band sfb to the next one sfb+1; enn and thmm have to be split
    between them
	 */
	private static final void convert_partition2scalefac(final PsyConst_CB2SB gd, final float[] eb, final float[] thr,
														 final float enn_out[], final float thm_out[])
	{
		final int n = gd.n_sb;
		float enn = 0.0f, thmm = 0.0f;
		int sb = 0;
		for( int b = 0; sb < n; ++b, ++sb ) {
			final int bo_sb = gd.bo[sb];
			final int npart = gd.npart;
			final int b_lim = bo_sb < npart ? bo_sb : npart;
			while( b < b_lim ) {
				enn += eb[b];
				thmm += thr[b];
				b++;
			}
			if( b >= npart ) {
				enn_out[sb] = enn;
				thm_out[sb] = thmm;
				++sb;
				break;
			}
			{
				/* at transition sfb . sfb+1 */
				final float w_curr = gd.bo_weight[sb];
				final float w_next = 1.0f - w_curr;
				enn += w_curr * eb[b];
				thmm += w_curr * thr[b];
				enn_out[sb] = enn;
				thm_out[sb] = thmm;
				enn = w_next * eb[b];
				thmm = w_next * thr[b];
			}
		}
		/* zero initialize the rest */
		for( ; sb < n; ++sb ) {
			enn_out[sb] = 0;
			thm_out[sb] = 0;
		}
	}

	private static final void convert_partition2scalefac_s(final LAME_InternalFlags gfc, final float[] eb, final float[] thr, final int chn,
														   final int sblock)
	{
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gds = gfc.cd_psy.s;
		final float enn[] = new float[Encoder.SBMAX_s];
		final float thm[] = new float[Encoder.SBMAX_s];
		convert_partition2scalefac( gds, eb, thr, enn, thm );
		final float[][] en_chn_s = psv.en[chn].s;// java
		final float[][] thm_chn_s = psv.thm[chn].s;// java
		for(int sb = 0; sb < Encoder.SBMAX_s; ++sb ) {
			en_chn_s[sb][sblock] = enn[sb];
			thm_chn_s[sb][sblock] = thm[sb];
		}
	}

	/** longblock threshold calculation (part 2) */
	private static final void convert_partition2scalefac_l(final LAME_InternalFlags gfc, final float[] eb, final float[] thr, final int chn)
	{
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gdl = gfc.cd_psy.l;
		final float[] enn = psv.en[chn].l;//[0];
		final float[] thm = psv.thm[chn].l;//[0];
		convert_partition2scalefac( gdl, eb, thr, enn, thm );
	}

	private static final void convert_partition2scalefac_l_to_s(final LAME_InternalFlags gfc, final float[] eb, final float[] thr, final int chn)
	{
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gds = gfc.cd_psy.l_to_s;
		final float enn[] = new float[Encoder.SBMAX_s];
		final float thm[] = new float[Encoder.SBMAX_s];
		convert_partition2scalefac( gds, eb, thr, enn, thm );
		final float[][] en_chn_s = psv.en[chn].s;// java
		final float[][] thm_chn_s = psv.thm[chn].s;// java
		for(int sb = 0; sb < Encoder.SBMAX_s; ++sb ) {
			final float scale = 1.f / 64.f;
			final float tmp_enn = enn[sb];
			final float tmp_thm = thm[sb] * scale;
			final float[] en_chn_s_sb = en_chn_s[sb];// java
			final float[] thm_chn_s_sb = thm_chn_s[sb];// java
			en_chn_s_sb[0] = tmp_enn;
			en_chn_s_sb[1] = tmp_enn;
			en_chn_s_sb[2] = tmp_enn;
			thm_chn_s_sb[0] = tmp_thm;
			thm_chn_s_sb[1] = tmp_thm;
			thm_chn_s_sb[2] = tmp_thm;
		}
	}

	private static final float NS_INTERP(final float x, final float y, final float r) {
		/* was pow((x),(r))*pow((y),1-(r)) */
		if( r >= 1.0f ) {
			return x;
		}       /* 99.7% of the time */
		if( r <= 0.0f ) {
			return y;
		}
		if( y > 0.0f ) {
			return (float)Math.pow( x / y, r ) * y;
		} /* rest of the time */
		return 0.0f;        /* never happens */
	}

	private static final float regcoef_s[] = {
			11.8f,           /* these values are tuned only for 44.1kHz... */
			13.6f,
			17.2f,
			32f,
			46.5f,
			51.3f,
			57.5f,
			67.1f,
			71.5f,
			84.6f,
			97.6f,
			130f,
			/*      255.8 */
		};

	private static final float pecalc_s(final III_PsyRatio mr, final float masking_lower) {
		float pe_s = 1236.28f / 4;
		final float[][] thm_s = mr.thm.s;// java
		final float[][] en_s = mr.en.s;// java
		for(int sb = 0; sb < Encoder.SBMAX_s - 1; sb++ ) {
			final float[] thm_s_sb = thm_s[sb];// java
			final float[] en_s_sb = en_s[sb];// java
			for( int sblock = 0; sblock < 3; sblock++ ) {
				final float thm = thm_s_sb[sblock];
				if( thm > 0.0f ) {
					final float x = thm * masking_lower;
					final float en = en_s_sb[sblock];
					if( en > x ) {
						if( en > x * 1e10f ) {
							pe_s += regcoef_s[sb] * (10.0f * Util.LOG10);
						} else {
							pe_s += regcoef_s[sb] * (float)Math.log10( (double)(en / x) );// Util.FAST_LOG10( en / x );
						}
					}
				}
			}
		}

		return pe_s;
	}

	private static final float regcoef_l[] = {
			6.8f,            /* these values are tuned only for 44.1kHz... */
			5.8f,
			5.8f,
			6.4f,
			6.5f,
			9.9f,
			12.1f,
			14.4f,
			15f,
			18.9f,
			21.6f,
			26.9f,
			34.2f,
			40.2f,
			46.8f,
			56.5f,
			60.7f,
			73.9f,
			85.7f,
			93.4f,
			126.1f,
			/*      241.3 */
		};

	private static final float pecalc_l(final III_PsyRatio mr, final float masking_lower) {
		final float[] thm_l = mr.thm.l;// java
		final float[] en_l = mr.en.l;// java
		float pe_l = 1124.23f / 4;
		for(int sb = 0; sb < Encoder.SBMAX_l - 1; sb++ ) {
			final float thm = thm_l[sb];
			if( thm > 0.0f ) {
				final float x = thm * masking_lower;
				final float en = en_l[sb];
				if( en > x ) {
					if( en > x * 1e10f ) {
						pe_l += regcoef_l[sb] * (10.0f * Util.LOG10);
					} else {
						pe_l += regcoef_l[sb] * (float)Math.log10( (double)(en / x) );// Util.FAST_LOG10( en / x );
					}
				}
			}
		}
		return pe_l;
	}

	private static final void calc_energy(final PsyConst_CB2SB l, final float[] fftenergy, final float[] eb, final float[] max, final float[] avg)
	{
		final int[] n = l.numlines;
		final float[] rn = l.rnumlines;
		for( int b = 0, j = 0, be = l.npart; b < be; ++b ) {
			float ebb = 0, m = 0;
			for( int i = 0, ie = n[b]; i < ie; ++i, ++j ) {
				final float el = fftenergy[j];
				ebb += el;
				if( m < el ) {
					m = el;
				}
			}
			eb[b] = ebb;
			max[b] = m;
			avg[b] = ebb * rn[b];
		}
	}

	private static final void calc_mask_index_l(final LAME_InternalFlags gfc, final float[] max, final float[] avg, final byte[] mask_idx)
	{
		final PsyConst_CB2SB gdl = gfc.cd_psy.l;
		final int last_tab_entry = tab.length - 1;
		int b = 0;
		float a = avg[b] + avg[b + 1];
		if( a > 0.0f ) {
			float m = max[b];
			if( m < max[b + 1] ) {
				m = max[b + 1];
			}
			a = 20.0f * (m * 2.0f - a)
			/ (a * (gdl.numlines[b] + gdl.numlines[b + 1] - 1));
			int k = (int) a;
			if( k > last_tab_entry ) {
				k = last_tab_entry;
			}
			mask_idx[b] = (byte)k;
		} else {
			mask_idx[b] = 0;
		}

		final int npart1 = gdl.npart - 1;// java
		for( b = 1; b < npart1; b++ ) {
			a = avg[b - 1] + avg[b] + avg[b + 1];
			if( a > 0.0f ) {
				float m = max[b - 1];
				if( m < max[b] ) {
					m = max[b];
				}
				if( m < max[b + 1]) {
					m = max[b + 1];
				}
				a = 20.0f * (m * 3.0f - a) / (a * (gdl.numlines[b - 1] + gdl.numlines[b] + gdl.numlines[b + 1] - 1));
				int k = (int) a;
				if( k > last_tab_entry ) {
					k = last_tab_entry;
				}
				mask_idx[b] = (byte)k;
			} else {
				mask_idx[b] = 0;
			}
		}

		a = avg[b - 1] + avg[b];
		if( a > 0.0f ) {
			float m = max[b - 1];
			if( m < max[b]) {
				m = max[b];
			}
			a = 20.0f * (m * 2.0f - a)
			/ (a * (gdl.numlines[b - 1] + gdl.numlines[b] - 1));
			int k = (int) a;
			if( k > last_tab_entry) {
				k = last_tab_entry;
			}
			mask_idx[b] = (byte)k;
			return;
		}// else {
			mask_idx[b] = 0;
		//}
	}

	private static final void vbrpsy_compute_fft_l(final LAME_InternalFlags gfc,
		final float buffer[/*2*/][], final int boffset,// java
		final int chn,
		final int gr_out, final float fftenergy[/*HBLKSIZE*/],
		final float wsamp_l[/*BLKSIZE*/][], final int woffset)
	{
		final PsyStateVar psv = gfc.sv_psy;

		final float[] wsamp_l_0 = wsamp_l[woffset];// java
		if( chn < 2 ) {
			FFT.fft_long( gfc, wsamp_l_0, chn, buffer, boffset );
		} else if( chn == 2 ) {
			final float sqrt2_half = Util.SQRT2 * 0.5f;
			/* FFT data for mid and side channel is derived from L & R */
			final float[] wsamp_l_1 = wsamp_l[woffset + 1];
			for(int j = Encoder.BLKSIZE - 1; j >= 0; --j ) {
				final float l = wsamp_l_0[j];
				final float r = wsamp_l_1[j];
				wsamp_l_0[j] = (l + r) * sqrt2_half;
				wsamp_l_1[j] = (l - r) * sqrt2_half;
			}
		}

		/*********************************************************************
		*  compute energies
		*********************************************************************/
		fftenergy[0] = wsamp_l_0[0];
		fftenergy[0] *= fftenergy[0];

		for(int j = Encoder.BLKSIZE / 2 - 1; j >= 0; --j ) {
			final int jm = Encoder.BLKSIZE / 2 - j;// java
			final float re = wsamp_l_0[jm];
			final float im = wsamp_l_0[Encoder.BLKSIZE / 2 + j];
			fftenergy[jm] = (re * re + im * im) * 0.5f;
		}
		/* total energy */
		{
			float totalenergy = 0.0f;
			for(int j = 11; j < Encoder.HBLKSIZE; j++ ) {
				totalenergy += fftenergy[j];
			}

			psv.tot_ener[chn] = totalenergy;
		}
	}

	private static final void vbrpsy_compute_fft_s(final LAME_InternalFlags gfc,
		final float buffer[/*2*/][], final int boffset,// java
		final int chn,
		final int sblock, final float fftenergy_s[/*HBLKSIZE_s*/][],
		final float wsamp_s[/*3*/][/*BLKSIZE_s*/][], int woffset)
	{
		if( sblock == 0 && chn < 2 ) {
			FFT.fft_short( gfc, wsamp_s[woffset], chn, buffer, boffset );
		}
		final float[] w0 = wsamp_s[woffset][sblock];// java
		if( chn == 2 ) {
			final float[] w1 = wsamp_s[++woffset][sblock];// java
			final float sqrt2_half = Util.SQRT2 * 0.5f;
			/* FFT data for mid and side channel is derived from L & R */
			for(int j = Encoder.BLKSIZE_s - 1; j >= 0; --j ) {
				final float l = w0[j];
				final float r = w1[j];
				w0[j] = (l + r) * sqrt2_half;
				w1[j] = (l - r) * sqrt2_half;
			}
		}

		/*********************************************************************
		*  compute energies
		*********************************************************************/
		final float[] f = fftenergy_s[sblock];// java
		f[0] = w0[0];
		f[0] *= f[0];
		for(int j = Encoder.BLKSIZE_s / 2 - 1; j >= 0; --j ) {
			final int jm = Encoder.BLKSIZE_s / 2 - j;// java
			final float re = w0[jm];
			final float im = w0[Encoder.BLKSIZE_s / 2 + j];
			f[jm] = (re * re + im * im) * 0.5f;
		}
	}

	/*********************************************************************
	* compute loudness approximation (used for ATH auto-level adjustment)
	*********************************************************************/
	private static final void vbrpsy_compute_loudness_approximation_l(final LAME_InternalFlags gfc, final int gr_out, final int chn,
																	  final float fftenergy[/*HBLKSIZE*/])
	{
		final PsyStateVar psv = gfc.sv_psy;
		if( chn < 2 ) {      /*no loudness for mid/side ch */
			gfc.ov_psy.loudness_sq[gr_out][chn] = psv.loudness_sq_save[chn];
			psv.loudness_sq_save[chn] = psycho_loudness_approx( fftenergy, gfc.ATH.eql_w );
		}
	}

	private static final float fircoef[] = {
		-8.65163e-18f * 2, -0.00851586f * 2, -6.74764e-18f * 2, 0.0209036f * 2,
		-3.36639e-17f * 2, -0.0438162f * 2, -1.54175e-17f * 2, 0.0931738f * 2,
		-5.52212e-17f * 2, -0.313819f * 2
	};

	/**********************************************************************
	*  Apply HPF of fs/4 to the input signal.
	*  This is used for attack detection / handling.
	**********************************************************************/
	private static final void vbrpsy_attack_detection(final LAME_InternalFlags gfc,
													  final float buffer[/*2*/][], int boffset,
													  final int gr_out,
													  final III_PsyRatio masking_ratio[/*2*/][/*2*/], final III_PsyRatio masking_MS_ratio[/*2*/][/*2*/],
													  final float energy[/*4*/], final float sub_short_factor[/*4*/][/*3*/], final int ns_attacks[/*4*/][/*4*/],
													  final boolean uselongblock[/*2*/])
	{
		final float ns_hpfsmpl[][] = new float[2][576];// java: already zeroed
		final SessionConfig cfg = gfc.cfg;
		final PsyStateVar psv = gfc.sv_psy;
		final int n_chn_out = cfg.channels_out;
		/* chn=2 and 3 = Mid and Side channels */
		final int n_chn_psy = (cfg.mode == LAME.JOINT_STEREO) ? 4 : n_chn_out;

		/* Don't copy the input buffer into a temporary buffer */
		/* unroll the loop 2 times */
		boffset += 576 - 350 - NSFIRLEN + 192;
		final III_PsyRatio[] masking_ratio_gr_out = masking_ratio[gr_out];// java
		for( int chn = 0; chn < n_chn_out; chn++ ) {

			/* apply high pass filter of fs/4 */
			final float[] firbuf = buffer[chn];//&buffer[chn][576 - 350 - NSFIRLEN + 192];
			for( int i = 0, bi = boffset; i < 576; i++, bi++ ) {
				float sum1 = firbuf[bi + 10];
				float sum2 = 0.0f;
				for( int j = 0, bij = bi + NSFIRLEN; j < ((NSFIRLEN - 1) / 2) - 1; j++, bij-- ) {
					sum1 += fircoef[j] * (firbuf[bi + j] + firbuf[bij]);
					j++; bij--;
					sum2 += fircoef[j] * (firbuf[bi + j] + firbuf[bij]);
				}
				ns_hpfsmpl[chn][i] = sum1 + sum2;
			}
			masking_ratio_gr_out[chn].en.copyFrom( psv.en[chn] );
			masking_ratio_gr_out[chn].thm.copyFrom( psv.thm[chn] );
			if( n_chn_psy > 2 ) {
				/* MS maskings  */
				/*percep_MS_entropy         [chn-2]     = gfc . pe  [chn];  */
				masking_MS_ratio[gr_out][chn].en.copyFrom( psv.en[chn + 2] );
				masking_MS_ratio[gr_out][chn].thm.copyFrom( psv.thm[chn + 2] );
			}
		}
		final float attack_intensity[] = new float[12];
		final float en_subshort[] = new float[12];
		final float en_short[] = new float[4];// = { 0, 0, 0, 0 };
		for( int chn = 0; chn < n_chn_psy; chn++ ) {
			en_short[0] = 0; en_short[1] = 0; en_short[2] = 0; en_short[3] = 0;
			final float[] pf = ns_hpfsmpl[chn & 1];
			int pfi = 0;// java
			boolean ns_uselongblock = true;

			if( chn == 2 ) {
				for( int i = 0, j = 576; j > 0; ++i, --j ) {
					final float l = ns_hpfsmpl[0][i];
					final float r = ns_hpfsmpl[1][i];
					ns_hpfsmpl[0][i] = l + r;
					ns_hpfsmpl[1][i] = l - r;
				}
			}
			/***************************************************************
			* determine the block type (window type)
			***************************************************************/
			final float[] last_en_subshort_chn = psv.last_en_subshort[chn];// java
			/* calculate energies of each sub-shortblocks */
			for( int i = 0; i < 3; i++ ) {
				en_subshort[i] = last_en_subshort_chn[i + 6];
				attack_intensity[i] = en_subshort[i] / last_en_subshort_chn[i + 4];
				en_short[0] += en_subshort[i];
			}

			for( int i = 0; i < 9; i++ ) {
				float p = 1.f;
				for( final int pfe = pfi + 576 / 9; pfi < pfe; pfi++ ) {
					float abspf = pf[pfi];
					if( abspf < 0 ) {
						abspf = -abspf;
					}
					if( p < abspf ) {
						p = abspf;
					}
				}
				last_en_subshort_chn[i] = en_subshort[i + 3] = p;
				en_short[1 + i / 3] += p;
				if( p > en_subshort[i + 3 - 2] ) {
					p = p / en_subshort[i + 3 - 2];
				} else if( en_subshort[i + 3 - 2] > p * 10.0f ) {
					p = en_subshort[i + 3 - 2] / (p * 10.0f);
				} else {
					p = 0.0f;
				}
				attack_intensity[i + 3] = p;
			}

			/* pulse like signal detection for fatboy.wav and so on */
			for( int i = 0; i < 3; ++i ) {
				final float enn =
						en_subshort[i * 3 + 3] + en_subshort[i * 3 + 4] + en_subshort[i * 3 + 5];
				float factor = 1.f;
				if( en_subshort[i * 3 + 5] * 6 < enn ) {
					factor *= 0.5f;
					if( en_subshort[i * 3 + 4] * 6 < enn ) {
						factor *= 0.5f;
					}
				}
				sub_short_factor[chn][i] = factor;
			}

			/* compare energies between sub-shortblocks */
			final int[] ns_attacks_chn = ns_attacks[chn];// java
			{
				final float x = gfc.cd_psy.attack_threshold[chn];
				for( int i = 0; i < 12; i++ ) {
					final int i3 = i / 3;
					if( ns_attacks_chn[i3] == 0 ) {
						if( attack_intensity[i] > x ) {
							ns_attacks_chn[i3] = (i % 3) + 1;
						}
					}
				}
			}
			/* should have energy change between short blocks, in order to avoid periodic signals */
			/* Good samples to show the effect are Trumpet test songs */
			/* GB: tuned (1) to avoid too many short blocks for test sample TRUMPET */
			/* RH: tuned (2) to let enough short blocks through for test sample FSOL and SNAPS */
			for( int i = 1; i < 4; i++ ) {
				final float u = en_short[i - 1];
				final float v = en_short[i];
				final float m = (u >= v ? u : v);
				if( m < 40000 ) { /* (2) */
					if( u < 1.7f * v && v < 1.7f * u ) { /* (1) */
						if( i == 1 && ns_attacks_chn[0] <= ns_attacks_chn[i] ) {
							ns_attacks_chn[0] = 0;
						}
						ns_attacks_chn[i] = 0;
					}
				}
			}

			final int last_attacks_chn = psv.last_attacks[chn];// java
			if( ns_attacks_chn[0] <= last_attacks_chn ) {
				ns_attacks_chn[0] = 0;
			}

			if( last_attacks_chn == 3 ||
					(ns_attacks_chn[0] + ns_attacks_chn[1] + ns_attacks_chn[2] + ns_attacks_chn[3] != 0) ) {
				ns_uselongblock = false;

				if( ns_attacks_chn[1] != 0 && ns_attacks_chn[0] != 0 ) {
					ns_attacks_chn[1] = 0;
				}
				if( ns_attacks_chn[2] != 0 && ns_attacks_chn[1] != 0 ) {
					ns_attacks_chn[2] = 0;
				}
				if( ns_attacks_chn[3] != 0 && ns_attacks_chn[2] != 0 ) {
					ns_attacks_chn[3] = 0;
				}
			}

			if( chn < 2 ) {
				uselongblock[chn] = ns_uselongblock;
			} else {
				if( ! ns_uselongblock ) {
					uselongblock[0] = uselongblock[1] = false;
				}
			}

			/* there is a one granule delay.  Copy maskings computed last call
			 * into masking_ratio to return to calling program.
			 */
			energy[chn] = psv.tot_ener[chn];
		}
	}

	private static final void vbrpsy_skip_masking_s(final LAME_InternalFlags gfc, final int chn, final int sblock) {
		if( sblock == 0 ) {
			final float[] nbs2 = gfc.sv_psy.nb_s2[chn];//[0];
			final float[] nbs1 = gfc.sv_psy.nb_s1[chn];//[0];
			final int n = gfc.cd_psy.s.npart;
			for( int b = 0; b < n; b++ ) {
				nbs2[b] = nbs1[b];
			}
		}
	}

	private static final void vbrpsy_calc_mask_index_s(final LAME_InternalFlags gfc, final float[] max,
													   final float[] avg, final byte[] mask_idx)
	{
		final PsyConst_CB2SB gds = gfc.cd_psy.s;
		final int last_tab_entry = tab.length - 1;
		int b = 0;
		float a = avg[b] + avg[b + 1];
		if( a > 0.0f ) {
			float m = max[b];
			if( m < max[b + 1]) {
				m = max[b + 1];
			}
			a = 20.0f * (m * 2.0f - a)
			/ (a * (gds.numlines[b] + gds.numlines[b + 1] - 1));
			int k = (int) a;
			if( k > last_tab_entry ) {
				k = last_tab_entry;
			}
			mask_idx[b] = (byte)k;
		} else {
			mask_idx[b] = 0;
		}

		final int[] numlines = gds.numlines;// java
		final int npart1 = gds.npart - 1;// java
		for( b = 1; b < npart1; b++ ) {
			a = avg[b - 1] + avg[b] + avg[b + 1];
			if( a > 0.0 ) {
				float m = max[b - 1];
				if( m < max[b]) {
					m = max[b];
				}
				if( m < max[b + 1] ) {
					m = max[b + 1];
				}
				a = 20.0f * (m * 3.0f - a) / (a * (numlines[b - 1] + numlines[b] + numlines[b + 1] - 1));
				int k = (int) a;
				if( k > last_tab_entry ) {
					k = last_tab_entry;
				}
				mask_idx[b] = (byte)k;
			} else {
				mask_idx[b] = 0;
			}
		}

		a = avg[b - 1] + avg[b];
		if( a > 0.0f ) {
			float m = max[b - 1];
			if( m < max[b] ) {
				m = max[b];
			}
			a = 20.0f * (m * 2.0f - a) / (a * (numlines[b - 1] + numlines[b] - 1));
			int k = (int) a;
			if( k > last_tab_entry ) {
				k = last_tab_entry;
			}
			mask_idx[b] = (byte)k;
			return;
		}// else {
			mask_idx[b] = 0;
		//}
	}

	private static final void vbrpsy_compute_masking_s(final LAME_InternalFlags gfc, final float fftenergy_s[/*HBLKSIZE_s*/][],
													   final float[] eb, final float[] thr, final int chn, final int sblock)
	{
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gds = gfc.cd_psy.s;
		final float max[] = new float[Encoder.CBANDS];// java: already zeroed
		final float avg[] = new float[Encoder.CBANDS];// java: already zeroed
		final byte mask_idx_s[] = new byte[Encoder.CBANDS];// tab.length max

		final int npart = gds.npart;// java
		final float[] fftenergy_s_sblock = fftenergy_s[sblock];// java
		for( int b = 0, j = 0; b < npart; ++b ) {
			float ebb = 0, m = 0;
			final int n = gds.numlines[b];
			for( int i = 0; i < n; ++i, ++j ) {
				final float el = fftenergy_s_sblock[j];
				ebb += el;
				if( m < el ) {
					m = el;
				}
			}
			eb[b] = ebb;
			max[b] = m;
			avg[b] = ebb * gds.rnumlines[b];
		}
		vbrpsy_calc_mask_index_s( gfc, max, avg, mask_idx_s );

		final float[] nb_s1_chn = psv.nb_s1[chn];// java
		final float[] nb_s2_chn = psv.nb_s2[chn];// java
		final float[] s3 = gds.s3;// java
		int b = 0;
		for( int j = 0; b < npart; b++ ) {
			final int[] s3ind_b = gds.s3ind[b];// java
			int kk = s3ind_b[0];
			final int last = s3ind_b[1];
			final int delta = tab_mask_add_delta[ mask_idx_s[b] ];// mask_add_delta( mask_idx_s[b] );
			final float masking_lower = gds.masking_lower[b] * gfc.sv_qnt.masking_lower;

			int dd = mask_idx_s[kk];
			int dd_n = 1;
			float ecb = s3[j] * eb[kk] * tab[ mask_idx_s[kk] ];
			++j; ++kk;
			while( kk <= last ) {
				dd += mask_idx_s[kk];
				dd_n += 1;
				final float x = s3[j] * eb[kk] * tab[ mask_idx_s[kk] ];
				ecb = vbrpsy_mask_add( ecb, x, kk - b, delta );
				++j; ++kk;
			}
			dd = (1 + 2 * dd) / (2 * dd_n);
			final float avg_mask = tab[dd] * 0.5f;
			ecb *= avg_mask;
/* #if 0       // we can do PRE ECHO control now here, or do it later
			if( psv.blocktype_old[chn & 0x01] == SHORT_TYPE ) {
				// limit calculated threshold by even older granule
				final float t1 = rpelev_s * psv.nb_s1[chn][b];
				final float t2 = rpelev2_s * psv.nb_s2[chn][b];
				final float tm = (t2 > 0) ? Min(ecb, t2) : ecb;
				thr[b] = (t1 > 0) ? NS_INTERP(Min(tm, t1), ecb, 0.6) : ecb;
			} else {
				// limit calculated threshold by older granule
				final float t1 = rpelev_s * psv.nb_s1[chn][b];
				thr[b] = (t1 > 0) ? NS_INTERP(Min(ecb, t1), ecb, 0.6) : ecb;
			} */
// #else // we do it later
			thr[b] = ecb;
// #endif
			nb_s2_chn[b] = nb_s1_chn[b];
			nb_s1_chn[b] = ecb;
			{
				/*  if THR exceeds EB, the quantization routines will take the difference
				 *  from other bands. in case of strong tonal samples (tonaltest.wav)
				 *  this leads to heavy distortions. that's why we limit THR here.
				 */
				float x = max[b];
				x *= gds.minval[b];
				x *= avg_mask;
				if( thr[b] > x ) {
					thr[b] = x;
				}
			}
			if( masking_lower > 1 ) {
				thr[b] *= masking_lower;
			}
			if( thr[b] > eb[b] ) {
				thr[b] = eb[b];
			}
			if( masking_lower < 1 ) {
				thr[b] *= masking_lower;
			}
		}
		for(; b < Encoder.CBANDS; ++b ) {
			eb[b] = 0;
			thr[b] = 0;
		}
	}

	private static final void vbrpsy_compute_masking_l(final LAME_InternalFlags gfc, final float fftenergy[/*HBLKSIZE*/],
													   final float eb_l[/*CBANDS*/], final float thr[/*CBANDS*/], final int chn)
	{
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gdl = gfc.cd_psy.l;
		final float max[] = new float[Encoder.CBANDS];
		final float avg[] = new float[Encoder.CBANDS];
		final byte mask_idx_l[] = new byte[Encoder.CBANDS + 2];

		/*********************************************************************
		*    Calculate the energy and the tonality of each partition.
		*********************************************************************/
		calc_energy( gdl, fftenergy, eb_l, max, avg );
		calc_mask_index_l( gfc, max, avg, mask_idx_l );

		/*********************************************************************
		*      convolve the partitioned energy and unpredictability
		*      with the spreading function, s3_l[b][k]
		********************************************************************/
		final float[] nb_l1_chn = psv.nb_l1[chn];// java
		final float[] nb_l2_chn = psv.nb_l2[chn];// java
		final float[] s3 = gdl.s3;// java
		final int blocktype_old = psv.blocktype_old[chn & 0x01];// java
		int k = 0;
		int b = 0;
		for( ; b < gdl.npart; b++ ) {
			final float masking_lower = gdl.masking_lower[b] * gfc.sv_qnt.masking_lower;
			/* convolve the partitioned energy with the spreading function */
			final int[] s3ind_b = gdl.s3ind[b];// java
			int kk = s3ind_b[0];
			final int last = s3ind_b[1];
			final int delta = tab_mask_add_delta[  mask_idx_l[b] ];// mask_add_delta( mask_idx_l[b] );
			int dd = 0, dd_n = 0;

			dd = mask_idx_l[kk];
			dd_n += 1;
			float ecb = s3[k] * eb_l[kk] * tab[ mask_idx_l[kk] ];
			++k; ++kk;
			while( kk <= last ) {
				dd += mask_idx_l[kk];
				dd_n += 1;
				final float x = s3[k] * eb_l[kk] * tab[ mask_idx_l[kk] ];
				final float t = vbrpsy_mask_add( ecb, x, kk - b, delta );
/* #if 0
				ecb += eb_l[kk];
				if( ecb > t ) {
					ecb = t;
				}
#else */
				ecb = t;
// #endif
				++k; ++kk;
			}
			dd = (1 + (dd << 1)) / (dd_n << 1);
			final float avg_mask = tab[dd] * 0.5f;
			ecb *= avg_mask;

			/****   long block pre-echo control   ****/
			/* dont use long block pre-echo control if previous granule was
			 * a short block.  This is to avoid the situation:
			 * frame0:  quiet (very low masking)
			 * frame1:  surge  (triggers short blocks)
			 * frame2:  regular frame.  looks like pre-echo when compared to
			 *          frame0, but all pre-echo was in frame1.
			 */
			/* chn=0,1   L and R channels
			   chn=2,3   S and M channels.
			 */
			if( blocktype_old == Encoder.SHORT_TYPE ) {
				final float ecb_limit = rpelev * nb_l1_chn[b];
				if( ecb_limit > 0 ) {
					thr[b] = (ecb <= ecb_limit ? ecb : ecb_limit);
				} else {
					/* Robert 071209:
					   Because we don't calculate long block psy when we know a granule
					   should be of short blocks, we don't have any clue how the granule
					   before would have looked like as a long block. So we have to guess
					   a little bit for this END_TYPE block.
					   Most of the time we get away with this sloppyness. (fingers crossed :)
					   The speed increase is worth it.
					 */
					final float v = eb_l[b] * NS_PREECHO_ATT2;
					thr[b] = (ecb <= v ? ecb : v);
				}
			} else {
				float ecb_limit_2 = rpelev2 * nb_l2_chn[b];
				float ecb_limit_1 = rpelev * nb_l1_chn[b];
				float ecb_limit;
				if( ecb_limit_2 <= 0 ) {
					ecb_limit_2 = ecb;
				}
				if( ecb_limit_1 <= 0 ) {
					ecb_limit_1 = ecb;
				}
				if( blocktype_old == Encoder.NORM_TYPE ) {
					ecb_limit = (ecb_limit_1 <= ecb_limit_2 ? ecb_limit_1 : ecb_limit_2);
				} else {
					ecb_limit = ecb_limit_1;
				}
				thr[b] = (ecb <= ecb_limit ? ecb : ecb_limit);
			}
			nb_l2_chn[b] = nb_l1_chn[b];
			nb_l1_chn[b] = ecb;
			{
				/*  if THR exceeds EB, the quantization routines will take the difference
				 *  from other bands. in case of strong tonal samples (tonaltest.wav)
				 *  this leads to heavy distortions. that's why we limit THR here.
				 */
				float x = max[b];
				x *= gdl.minval[b];
				x *= avg_mask;
				if( thr[b] > x ) {
					thr[b] = x;
				}
			}
			if( masking_lower > 1 ) {
				thr[b] *= masking_lower;
			}
			if( thr[b] > eb_l[b] ) {
				thr[b] = eb_l[b];
			}
			if( masking_lower < 1 ) {
				thr[b] *= masking_lower;
			}
		}
		for(; b < Encoder.CBANDS; ++b ) {
			eb_l[b] = 0;
			thr[b] = 0;
		}
	}

	private static final void vbrpsy_compute_block_type(final SessionConfig cfg, final boolean[] uselongblock) {
		final int short_blocks = cfg.short_blocks;// java
		if( short_blocks == LAME_GlobalFlags.short_block_coupled
				/* force both channels to use the same block type */
				/* this is necessary if the frame is to be encoded in ms_stereo.  */
				/* But even without ms_stereo, FhG  does this */
				&& !(uselongblock[0] && uselongblock[1]) ) {
			uselongblock[0] = uselongblock[1] = false;
		}

		final boolean is_short_block_dispensed = (short_blocks == LAME_GlobalFlags.short_block_dispensed);
		final boolean is_short_block_forced = (short_blocks == LAME_GlobalFlags.short_block_forced);
		for( int chn = 0, channels_out = cfg.channels_out; chn < channels_out; chn++ ) {
			/* disable short blocks */
			if( is_short_block_dispensed ) {
				uselongblock[chn] = true;
			}
			if( is_short_block_forced ) {
				uselongblock[chn] = false;
			}
		}
	}

	private static final void vbrpsy_apply_block_type(final PsyStateVar psv, final int nch, final boolean[] uselongblock, final int[] blocktype_d) {
		final int[] blocktype_old = psv.blocktype_old;// java
		/* update the blocktype of the previous granule, since it depends on what
		 * happend in this granule */
		for( int chn = 0; chn < nch; chn++ ) {
			int blocktype = Encoder.NORM_TYPE;
			/* disable short blocks */

			int blocktype_old_chn = blocktype_old[chn];// java
			if( uselongblock[chn] ) {
				/* no attack : use long blocks */
				if( blocktype_old_chn == Encoder.SHORT_TYPE ) {
					blocktype = Encoder.STOP_TYPE;
				}
			} else {
				/* attack : use short blocks */
				blocktype = Encoder.SHORT_TYPE;
				if( blocktype_old_chn == Encoder.NORM_TYPE ) {
					blocktype_old_chn = Encoder.START_TYPE;
				}
				if( blocktype_old_chn == Encoder.STOP_TYPE) {
					blocktype_old_chn = Encoder.SHORT_TYPE;
				}
			}

			blocktype_d[chn] = blocktype_old_chn; /* value returned to calling program */
			blocktype_old[chn] = blocktype; /* save for next call to l3psy_anal */
		}
	}

	/***************************************************************
	 * compute M/S thresholds from Johnston & Ferreira 1992 ICASSP paper
	 ***************************************************************/
	private static final void vbrpsy_compute_MS_thresholds(final float eb[/*4*/][/*CBANDS*/], final float thr[/*4*/][/*CBANDS*/],
		final float cb_mld[/*CBANDS*/], final float ath_cb[/*CBANDS*/], final float athlower,
		final float msfix, final int n)
	{
		final float msfix2 = msfix * 2.f;
		float rside, rmid;
		for( int b = 0; b < n; ++b ) {
			final float ebM = eb[2][b];
			final float ebS = eb[3][b];
			final float thmL = thr[0][b];
			final float thmR = thr[1][b];
			float thmM = thr[2][b];
			float thmS = thr[3][b];

			/* use this fix if L & R masking differs by 2db or less */
			/* if db = 10*log10(x2/x1) < 2 */
			/* if( x2 < 1.58*x1 ) { */
			if( thmL <= 1.58f * thmR && thmR <= 1.58f * thmL ) {
				final float mld_m = cb_mld[b] * ebS;
				final float mld_s = cb_mld[b] * ebM;
				final float tmp_m = (thmS <= mld_m ? thmS : mld_m);
				final float tmp_s = (thmM <= mld_s ? thmM : mld_s);
				rmid = (thmM >= tmp_m ? thmM : tmp_m);
				rside = (thmS >= tmp_s ? thmS : tmp_s);
			} else {
				rmid = thmM;
				rside = thmS;
			}
			if( msfix > 0.f ) {
				/***************************************************************/
				/* Adjust M/S maskings if user set "msfix"                     */
				/***************************************************************/
				/* Naoki Shibata 2000 */
				final float ath = ath_cb[b] * athlower;
				final float tmp_l = (thmL >= ath ? thmL : ath);
				final float tmp_r = (thmR >= ath ? thmR : ath);
				final float thmLR = (tmp_l <= tmp_r ? tmp_l : tmp_r);
				thmM = (rmid >= ath ? rmid : ath);
				thmS = (rside >= ath ? rside : ath);
				final float thmMS = thmM + thmS;
				if( thmMS > 0.f && (thmLR * msfix2) < thmMS ) {
					final float f = thmLR * msfix2 / thmMS;
					thmM *= f;
					thmS *= f;
				}
				rmid = (thmM <= rmid ? thmM : rmid);
				rside = (thmS <= rside ? thmS : rside);
			}
			if( rmid > ebM ) {
				rmid = ebM;
			}
			if( rside > ebS ) {
				rside = ebS;
			}
			thr[2][b] = rmid;
			thr[3][b] = rside;
		}
	}

	/**
	 * NOTE: the bitrate reduction from the inter-channel masking effect is low
	 * compared to the chance of getting annyoing artefacts. L3psycho_anal_vbr does
	 * not use this feature. (Robert 071216)
	 */
	static final int L3psycho_anal_vbr(final LAME_InternalFlags gfc,
									   final float buffer[/*2*/][], final int boffset,
									   final int gr_out,
									   final III_PsyRatio masking_ratio[][],// [2][2],
									   final III_PsyRatio masking_MS_ratio[][],// [2][2],
									   final float percep_entropy[/*2*/], final float percep_MS_entropy[/*2*/],
									   final float energy[/*4*/], final int blocktype_d[/*2*/])
	{
		final SessionConfig cfg = gfc.cfg;
		final PsyStateVar psv = gfc.sv_psy;
		final PsyConst_CB2SB gdl = gfc.cd_psy.l;
		final PsyConst_CB2SB gds = gfc.cd_psy.s;

		/* fft and energy calculation   */
		final float fftenergy[] = new float[Encoder.HBLKSIZE];
		final float fftenergy_s[][] = new float[3][Encoder.HBLKSIZE_s];
		final float wsamp_L[][] = new float[2][Encoder.BLKSIZE];
		final float wsamp_S[][][] = new float[2][3][Encoder.BLKSIZE_s];
		final float eb[][] = new float[4][Encoder.CBANDS];
		final float thr[][] = new float[4][Encoder.CBANDS];

		final float sub_short_factor[][] = new float[4][3];
		final float pcfact = 0.6f;
		final float ath_factor = (cfg.msfix > 0.f) ? (cfg.ATH_offset_factor * gfc.ATH.adjust_factor) : 1.f;

		/* block type  */
		final int ns_attacks[][] = new int[4][4];// java: already zeroed = { {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0} };
		final boolean uselongblock[] = new boolean[2];

		/* chn=2 and 3 = Mid and Side channels */
		final int n_chn_psy = (cfg.mode == LAME.JOINT_STEREO) ? 4 : cfg.channels_out;

		final III_PsyXmin last_thm[] = new III_PsyXmin[4];
		last_thm[0] = new III_PsyXmin( psv.thm[0] );
		last_thm[1] = new III_PsyXmin( psv.thm[1] );
		last_thm[2] = new III_PsyXmin( psv.thm[2] );
		last_thm[3] = new III_PsyXmin( psv.thm[3] );

		vbrpsy_attack_detection( gfc, buffer, boffset, gr_out, masking_ratio, masking_MS_ratio, energy,
				sub_short_factor, ns_attacks, uselongblock );

		vbrpsy_compute_block_type( cfg, uselongblock );

		/* LONG BLOCK CASE */
		{
			for( int chn = 0; chn < n_chn_psy; chn++ ) {
				final int ch01 = chn & 0x01;
				vbrpsy_compute_fft_l( gfc, buffer, boffset, chn, gr_out, fftenergy, wsamp_L, ch01 );
				vbrpsy_compute_loudness_approximation_l( gfc, gr_out, chn, fftenergy );
				vbrpsy_compute_masking_l( gfc, fftenergy, eb[chn], thr[chn], chn );
			}
			if( cfg.mode == LAME.JOINT_STEREO ) {
				if( uselongblock[0] & uselongblock[1] ) {
					vbrpsy_compute_MS_thresholds( eb, thr, gdl.mld_cb, gfc.ATH.cb_l,
							ath_factor, cfg.msfix, gdl.npart );
				}
			}
			/* TODO: apply adaptive ATH masking here ?? */
			for( int chn = 0; chn < n_chn_psy; chn++ ) {
				convert_partition2scalefac_l( gfc, eb[chn], thr[chn], chn );
				convert_partition2scalefac_l_to_s( gfc, eb[chn], thr[chn], chn );
			}
		}
		/* SHORT BLOCKS CASE */
		{
			final boolean force_short_block_calc = gfc.cd_psy.force_short_block_calc;
			for( int sblock = 0; sblock < 3; sblock++ ) {
				for( int chn = 0; chn < n_chn_psy; ++chn ) {
					final int ch01 = chn & 0x01;
					if( uselongblock[ch01] && ! force_short_block_calc ) {
						vbrpsy_skip_masking_s( gfc, chn, sblock );
					} else {
						/* compute masking thresholds for short blocks */
						vbrpsy_compute_fft_s( gfc, buffer, boffset, chn, sblock, fftenergy_s, wsamp_S, ch01 );
						vbrpsy_compute_masking_s( gfc, fftenergy_s, eb[chn], thr[chn], chn, sblock );
					}
				}
				if( cfg.mode == LAME.JOINT_STEREO ) {
					if( !(uselongblock[0] | uselongblock[1]) ) {
						vbrpsy_compute_MS_thresholds( eb, thr, gds.mld_cb, gfc.ATH.cb_s,
								ath_factor, cfg.msfix, gds.npart );
					}
				}
				/* TODO: apply adaptive ATH masking here ?? */
				for( int chn = 0; chn < n_chn_psy; ++chn ) {
					final int ch01 = chn & 0x01;
					if( ! uselongblock[ch01] || force_short_block_calc ) {
						convert_partition2scalefac_s( gfc, eb[chn], thr[chn], chn, sblock );
					}
				}
			}

			/****   short block pre-echo control   ****/
			final float new_thmm[] = new float[3];
			for( int chn = 0; chn < n_chn_psy; chn++ ) {
				final float[][] last_thm_chn_s = last_thm[chn].s;// java
				final float[][] thm_chn_s = psv.thm[chn].s;// java
				final int[] ns_attacks_chn = ns_attacks[chn];// java
				final int last_attacks_chn = psv.last_attacks[chn];// java
				for(int sb = 0; sb < Encoder.SBMAX_s; sb++ ) {
					final float[] thm_chn_s_sb = thm_chn_s[sb];// java
					float prev_thm, t1, t2;
					for( int sblock = 0; sblock < 3; sblock++ ) {
						float thmm = thm_chn_s_sb[sblock];
						thmm *= NS_PREECHO_ATT0;

						t1 = t2 = thmm;

						if( sblock > 0 ) {
							prev_thm = new_thmm[sblock - 1];
						} else {
							prev_thm = last_thm_chn_s[sb][2];
						}
						if( ns_attacks_chn[sblock] >= 2 || ns_attacks_chn[sblock + 1] == 1 ) {
							t1 = NS_INTERP( prev_thm, thmm, NS_PREECHO_ATT1 * pcfact );
						}
						thmm = (t1 <= thmm ? t1 : thmm);
						if( ns_attacks_chn[sblock] == 1 ) {
							t2 = NS_INTERP( prev_thm, thmm, NS_PREECHO_ATT2 * pcfact );
						} else if( (sblock == 0 && last_attacks_chn == 3)
								|| (sblock > 0 && ns_attacks_chn[sblock - 1] == 3) ) { /* 2nd preceeding block */
							switch( sblock ) {
							case 0:
								prev_thm = last_thm_chn_s[sb][1];
								break;
							case 1:
								prev_thm = last_thm_chn_s[sb][2];
								break;
							case 2:
								prev_thm = new_thmm[0];
								break;
							}
							t2 = NS_INTERP( prev_thm, thmm, NS_PREECHO_ATT2 * pcfact );
						}

						thmm = (t1 <= thmm ? t1 : thmm);
						thmm = (t2 <= thmm ? t2 : thmm);

						/* pulse like signal detection for fatboy.wav and so on */
						thmm *= sub_short_factor[chn][sblock];

						new_thmm[sblock] = thmm;
					}
					thm_chn_s_sb[0] = new_thmm[0];
					thm_chn_s_sb[1] = new_thmm[1];
					thm_chn_s_sb[2] = new_thmm[2];
				}
			}
		}
		for( int chn = 0; chn < n_chn_psy; chn++ ) {
			psv.last_attacks[chn] = ns_attacks[chn][2];
		}


		/***************************************************************
		* determine final block type
		***************************************************************/
		vbrpsy_apply_block_type( psv, cfg.channels_out, uselongblock, blocktype_d );

		/*********************************************************************
		* compute the value of PE to return ... no delay and advance
		*********************************************************************/
		final boolean blocktype = (blocktype_d[0] == Encoder.SHORT_TYPE || blocktype_d[1] == Encoder.SHORT_TYPE);// java
		for( int chn = 0; chn < n_chn_psy; chn++ ) {
			float[] appe;// java
			int ppe, type;
			III_PsyRatio mr;

			if( chn > 1 ) {
				appe = percep_MS_entropy;
				ppe = chn - 2;
				type = Encoder.NORM_TYPE;
				if( blocktype ) {
					type = Encoder.SHORT_TYPE;
				}
				mr = masking_MS_ratio[gr_out][chn - 2];
			} else {
				appe = percep_entropy;
				ppe = chn;
				type = blocktype_d[chn];
				mr = masking_ratio[gr_out][chn];
			}
			if( type == Encoder.SHORT_TYPE ) {
				appe[ppe] = pecalc_s( mr, gfc.sv_qnt.masking_lower );
			} else {
				appe[ppe] = pecalc_l( mr, gfc.sv_qnt.masking_lower );
			}
		}
		return 0;
	}

	/** The spreading function.  Values returned in units of energy */
	private static final float s3_func(final float bark) {
		float tempx = bark;
		if( tempx >= 0 ) {
			tempx *= 3f;
		} else {
			tempx *= 1.5f;
		}

		float x;
		if( tempx >= 0.5f && tempx <= 2.5f ) {
			final float temp = tempx - 0.5f;
			x = 8.0f * (temp * temp - 2.0f * temp);
		} else {
			x = 0.0f;
		}
		tempx += 0.474;
		final float tempy = 15.811389f + 7.5f * tempx - 17.5f * (float)Math.sqrt( (double)(1.0f + tempx * tempx) );

		if( tempy <= -60.0 ) {
			return 0.0f;
		}

		tempx = (float)Math.exp( (float)((x + tempy) * LN_TO_LOG10) );

		/* Normalization.  The spreading function should be normalized so that:
		   +inf
		   /
		   |  s3 [ bark ]  d(bark)   =  1
		   /
		   -inf
		 */
		tempx /= .6609193f;
		return tempx;
	}

/* #if 0
	private static final float norm_s3_func() {
		double  lim_a = 0, lim_b = 0;
		double  x = 0, l, h;
		for( x = 0; s3_func( x ) > 1e-20; x -= 1 ) {
			;
		}
		l = x;
		h = 0;
		while( fabs(h - l) > 1e-12 ) {
			x = (h + l) / 2;
			if( s3_func(x) > 0 ) {
				h = x;
			} else {
				l = x;
			}
		}
		lim_a = l;
		for( x = 0; s3_func( x ) > 1e-20; x += 1 ) {
			;
		}
		l = 0;
		h = x;
		while( fabs(h - l) > 1e-12 ) {
			x = (h + l) / 2;
			if( s3_func( x ) > 0 ) {
				l = x;
			} else {
				h = x;
			}
		}
		lim_b = h;
		{
			double  sum = 0;
			final int m = 1000;
			int     i;
			for( i = 0; i <= m; ++i ) {
				final double  x = lim_a + i * (lim_b - lim_a) / m;
				final double  y = s3_func( x );
				sum += y;
			}
			{
				final double  norm = (m + 1) / (sum * (lim_b - lim_a));
				return norm;
			}
		}
	}
#endif */

	private static final float stereo_demask(final float f) {
		/* setup stereo demasking thresholds */
		/* formula reverse enginerred from plot in paper */
		float arg = Util.freq2bark( f );
		arg = ((arg <= 15.5f ? arg : 15.5f) / 15.5f);

		return (float)Math.pow( 10.0, 1.25 * (1. - Math.cos(Math.PI * arg)) - 2.5 );
	}

	private static final void init_numline(final PsyConst_CB2SB gd, float sfreq, final int fft_size,
										   final int mdct_size, final int sbmax, final int[] scalepos)
	{
		final float b_frq[] = new float[Encoder.CBANDS + 1];
		final float mdct_freq_frac = sfreq / (2.0f * mdct_size);
		final float deltafreq = fft_size / (2.0f * mdct_size);
		final int partition[] = new int[Encoder.HBLKSIZE];// = { 0 };
		sfreq /= fft_size;
		final int fftsize2 = fft_size >> 1;
		int j = 0;
		int ni = 0;
		/* compute numlines, the number of spectral lines in each partition band */
		/* each partition band should be about DELBARK wide. */
		int i = 0;
		for(; i < Encoder.CBANDS; i++ ) {
			final float bark1 = Util.freq2bark( sfreq * j );

			b_frq[i] = sfreq * j;

			int j2;
			for(j2 = j; Util.freq2bark( sfreq * j2 ) - bark1 < DELBARK && j2 <= fftsize2; j2++ ) {
				;
			}

			final int nl = j2 - j;
			gd.numlines[i] = nl;
			gd.rnumlines[i] = (nl > 0) ? (1.0f / nl) : 0;

			ni = i + 1;

			while( j < j2 ) {
				partition[j++] = i;
			}
			if( j > fftsize2 ) {
				j = fftsize2;
				++i;
				break;
			}
		}
		b_frq[i] = sfreq * j;

		gd.n_sb = sbmax;
		gd.npart = ni;

		{
			j = 0;
			for( i = 0; i < gd.npart; i++ ) {
				final int nl = gd.numlines[i];
				final float freq = sfreq * (j + nl / 2);
				gd.mld_cb[i] = stereo_demask( freq );
				j += nl;
			}
			for(; i < Encoder.CBANDS; ++i ) {
				gd.mld_cb[i] = 1;
			}
		}
		for( int sfb = 0; sfb < sbmax; sfb++ ) {
			final int start = scalepos[sfb];
			final int end = scalepos[sfb + 1];

			int i1 = (int)Math.floor( (double)(.5f + deltafreq * (start - .5f)) );
			if( i1 < 0 ) {
				i1 = 0;
			}
			int i2 = (int)Math.floor( (double)(.5f + deltafreq * (end - .5f)) );

			if( i2 > fftsize2 ) {
				i2 = fftsize2;
			}

			final int bo = partition[i2];
			gd.bm[sfb] = (partition[i1] + partition[i2]) >> 1;
			gd.bo[sfb] = bo;

			/* calculate how much of this band belongs to current scalefactor band */
			{
				final float f_tmp = mdct_freq_frac * end;
				float bo_w = (f_tmp - b_frq[bo]) / (b_frq[bo + 1] - b_frq[bo]);
				if( bo_w < 0 ) {
					bo_w = 0;
				} else {
					if( bo_w > 1 ) {
						bo_w = 1;
					}
				}
				gd.bo_weight[sfb] = bo_w;
			}
			gd.mld[sfb] = stereo_demask( mdct_freq_frac * start );
		}
	}

	private static final void compute_bark_values(final PsyConst_CB2SB gd, float sfreq, final int fft_size,
												  final float[] bval, final float[] bval_width)
	{
		final int[] numlines = gd.numlines;// java
		/* compute bark values of each critical band */
		final int ni = gd.npart;
		sfreq /= fft_size;
		for( int k = 0, j = 0; k < ni; k++ ) {
			final int w = j + numlines[k];
			float bark1 = Util.freq2bark( sfreq * (float)j );
			float bark2 = Util.freq2bark( sfreq * (float)(w - 1) );
			bval[k] = .5f * (bark1 + bark2);

			bark1 = Util.freq2bark( sfreq * ((float)j - .5f) );
			bark2 = Util.freq2bark( sfreq * ((float)w - .5f) );
			bval_width[k] = bark2 - bark1;
			j = w;
		}
	}

	/**
	 * @return java: p array
	 */
	private static final float[] init_s3_values(/*final float[][] p,*/ final int s3ind[/*2*/][], final int npart,
			final float[] bval, final float[] bval_width, final float[] norm)
	{
		final float s3[][] = new float[Encoder.CBANDS][Encoder.CBANDS];// java: already zeroed
		/* The s3 array is not linear in the bark scale.
		 * bval[x] should be used to get the bark value.
		 */
		int numberOfNoneZero = 0;

		/* s[i][j], the value of the spreading function,
		 * centered at band j (masker), for band i (maskee)
		 *
		 * i.e.: sum over j to spread into signal barkval=i
		 * NOTE: i and j are used opposite as in the ISO docs
		 */
		for( int i = 0; i < npart; i++ ) {
			for( int j = 0; j < npart; j++ ) {
				final float v = s3_func( bval[i] - bval[j] ) * bval_width[j];
				s3[i][j] = v * norm[i];
			}
		}

		for( int i = 0; i < npart; i++ ) {
			final float[] s3_i = s3[i];// java
			int j;
			for( j = 0; j < npart; j++ ) {
				if( s3_i[j] > 0.0f) {
					break;
				}
			}
			final int[] s3ind_i = s3ind[i];// java
			s3ind_i[0] = j;
			numberOfNoneZero -= j;

			for( j = npart - 1; j > 0; j-- ) {
				if( s3_i[j] > 0.0f ) {
					break;
				}
			}
			s3ind_i[1] = j;
			numberOfNoneZero += j;
			numberOfNoneZero++;
		}
		final float p[] = new float[ numberOfNoneZero ];

		for( int i = 0, k = 0; i < npart; i++ ) {
			final float[] s3_i = s3[i];// java
			for( int j = s3ind[i][0], je = s3ind[i][1]; j <= je; j++ ) {
				p[k++] = s3_i[j];
			}
		}

		return p;
	}

	private static final float sk[] =
		{ -7.4f, -7.4f, -7.4f, -9.5f, -7.4f, -6.1f, -5.5f, -4.7f, -4.7f, -4.7f, -4.7f };

	static final int psymodel_init(final LAME_GlobalFlags gfp) {
		final LAME_InternalFlags gfc = gfp.internal_flags;
		final SessionConfig cfg = gfc.cfg;
		final PsyStateVar psv = gfc.sv_psy;
		final float bvl_a = 13, bvl_b = 24;
		final float snr_l_a = 0, snr_l_b = 0;
		final float snr_s_a = -8.25f, snr_s_b = -4.5f;

		final float bval[] = new float[Encoder.CBANDS];
		final float bval_width[] = new float[Encoder.CBANDS];
		final float norm[] = new float[Encoder.CBANDS];// java: already zeroed
		final float sfreq = cfg.samplerate;

		final float xav = 10, xbv = 12;
		final float minval_low = (0.f - cfg.minval);

		if( gfc.cd_psy != null ) {
			return 0;
		}

		final PsyConst gd = new PsyConst();
		gfc.cd_psy = gd;

		gd.force_short_block_calc = gfp.experimentalZ;

		psv.blocktype_old[0] = psv.blocktype_old[1] = Encoder.NORM_TYPE; /* the vbr header is long blocks */

		for( int i = 0; i < 4; ++i ) {
			for(int j = 0; j < Encoder.CBANDS; ++j ) {
				psv.nb_l1[i][j] = 1e20f;
				psv.nb_l2[i][j] = 1e20f;
				psv.nb_s1[i][j] = psv.nb_s2[i][j] = 1.0f;
			}
			for(int sb = 0; sb < Encoder.SBMAX_l; sb++ ) {
				psv.en[i].l[sb] = 1e20f;
				psv.thm[i].l[sb] = 1e20f;
			}
			for( int j = 0; j < 3; ++j ) {
				for(int sb = 0; sb < Encoder.SBMAX_s; sb++ ) {
					psv.en[i].s[sb][j] = 1e20f;
					psv.thm[i].s[sb][j] = 1e20f;
				}
				psv.last_attacks[i] = 0;
			}
			for( int j = 0; j < 9; j++ ) {
				psv.last_en_subshort[i][j] = 10.f;
			}
		}

		/* init. for loudness approx. -jd 2001 mar 27 */
		psv.loudness_sq_save[0] = psv.loudness_sq_save[1] = 0.0f;

		/*************************************************************************
		 * now compute the psychoacoustic model specific constants
		 ************************************************************************/
		/* compute numlines, bo, bm, bval, bval_width, mld */
		init_numline( gd.l, sfreq, Encoder.BLKSIZE, 576, Encoder.SBMAX_l, gfc.scalefac_band.l );
		compute_bark_values( gd.l, sfreq, Encoder.BLKSIZE, bval, bval_width );

		/* compute the spreading function */
		for( int i = 0; i < gd.l.npart; i++ ) {
			double snr = snr_l_a;
			if( bval[i] >= bvl_a ) {
				snr = snr_l_b * (bval[i] - bvl_a) / (bvl_b - bvl_a)
						+ snr_l_a * (bvl_b - bval[i]) / (bvl_b - bvl_a);
			}
			norm[i] = (float)Math.pow( 10.0, snr / 10.0 );
		}
		gd.l.s3 = init_s3_values( /*gd.l.s3,*/ gd.l.s3ind, gd.l.npart, bval, bval_width, norm );
		//if( i != 0 ) {
		//	return i;
		//}

		/* compute long block specific values, ATH and MINVAL */
		int j = 0;
		for( int i = 0; i < gd.l.npart; i++ ) {
			/* ATH */
			float x = Float.MAX_VALUE;
			for( int k = 0; k < gd.l.numlines[i]; k++, j++ ) {
				final float freq = sfreq * j / (1000.0f * Encoder.BLKSIZE);
				float level;
				/* freq = Min(.1,freq); *//* ATH below 100 Hz constant, not further climbing */
				level = Util.ATHformula( cfg, freq * 1000 ) - 20; /* scale to FFT units; returned value is in dB */
				level = (float)Math.pow( 10., 0.1 * level ); /* convert from dB . energy */
				level *= gd.l.numlines[i];
				if( x > level ) {
					x = level;
				}
			}
			gfc.ATH.cb_l[i] = x;

			/* MINVAL.
			   For low freq, the strength of the masking is limited by minval
			   this is an ISO MPEG1 thing, dont know if it is really needed */
			/* FIXME: it does work to reduce low-freq problems in S53-Wind-Sax
			   and lead-voice samples, but introduces some 3 kbps bit bloat too.
			   TODO: Further refinement of the shape of this hack.
			 */
			x = 20.0f * (bval[i] / xav - 1.0f);
			if( x > 6 ) {
				x = 30;
			}
			if( x < minval_low ) {
				x = minval_low;
			}
			if( cfg.samplerate < 44000 ) {
				x = 30;
			}
			x -= 8.;
			gd.l.minval[i] = (float)Math.pow( 10.0, x / 10. ) * gd.l.numlines[i];
		}

		/************************************************************************
		 * do the same things for short blocks
		 ************************************************************************/
		init_numline( gd.s, sfreq, Encoder.BLKSIZE_s, 192, Encoder.SBMAX_s, gfc.scalefac_band.s );
		compute_bark_values( gd.s, sfreq, Encoder.BLKSIZE_s, bval, bval_width );

		/* SNR formula. short block is normalized by SNR. is it still right ? */
		j = 0;
		for( int i = 0; i < gd.s.npart; i++ ) {
			float snr = snr_s_a;
			if( bval[i] >= bvl_a ) {
				snr = snr_s_b * (bval[i] - bvl_a) / (bvl_b - bvl_a)
				+ snr_s_a * (bvl_b - bval[i]) / (bvl_b - bvl_a);
			}
			norm[i] = (float)Math.pow( 10.0, (double)(snr / 10.0f) );

			/* ATH */
			float x = Float.MAX_VALUE;
			for( int k = 0, numlines_i = gd.s.numlines[i]; k < numlines_i; k++, j++ ) {
				final float freq = sfreq * j / (1000.0f * Encoder.BLKSIZE_s);
				float level;
				/* freq = Min(.1,freq); *//* ATH below 100 Hz constant, not further climbing */
				level = Util.ATHformula( cfg, freq * 1000 ) - 20; /* scale to FFT units; returned value is in dB */
				level = (float)Math.pow( 10., 0.1 * level ); /* convert from dB . energy */
				level *= numlines_i;
				if( x > level ) {
					x = level;
				}
			}
			gfc.ATH.cb_s[i] = x;

			/* MINVAL.
			   For low freq, the strength of the masking is limited by minval
			   this is an ISO MPEG1 thing, dont know if it is really needed */
			x = 7.0f * (bval[i] / xbv - 1.0f);
			if( bval[i] > xbv ) {
				x *= 1 + (float)Math.log( 1 + x ) * 3.1f;
			}
			if( bval[i] < xbv ) {
				x *= 1 + (float)Math.log( 1 - x ) * 2.3f;
			}
			if( x > 6 ) {
				x = 30;
			}
			if( x < minval_low ) {
				x = minval_low;
			}
			if( cfg.samplerate < 44000 ) {
				x = 30;
			}
			x -= 8;
			gd.s.minval[i] = (float)Math.pow( 10.0, x / 10 ) * gd.s.numlines[i];
		}

		gd.s.s3 = init_s3_values( /*gd.s.s3,*/ gd.s.s3ind, gd.s.npart, bval, bval_width, norm );
		//if( i != 0 ) {
		//	return i;
		//}

		init_mask_add_max_values();
		FFT.init_fft( gfc );

		/* setup temporal masking */
		gd.decay = (float)Math.exp( -1.0 * Util.LOG10 / (temporalmask_sustain_sec * sfreq / 192.0) );

		{
			float msfix = NS_MSFIX;
			if( cfg.use_safe_joint_stereo ) {
				msfix = 1.0f;
			}
			if( Math.abs( cfg.msfix ) > 0.0 ) {
				msfix = cfg.msfix;
			}
			cfg.msfix = msfix;

			/* spread only from npart_l bands.  Normally, we use the spreading
			 * function to convolve from npart_l down to npart_l bands
			 */
			for( int b = 0, npart = gd.l.npart; b < npart; b++ ) {
				if( gd.l.s3ind[b][1] > npart - 1 ) {
					gd.l.s3ind[b][1] = npart - 1;
				}
			}
		}

		/*  prepare for ATH auto adjustment:
		 *  we want to decrease the ATH by 12 dB per second
		 */
		final float frame_duration = (576.f * cfg.mode_gr / sfreq);
		gfc.ATH.decay = (float)Math.pow( 10., (float)(-12.f / 10.f * frame_duration) );
		gfc.ATH.adjust_factor = 0.01f; /* minimum, for leading low loudness */
		gfc.ATH.adjust_limit = 1.0f; /* on lead, allow adjust up to maximum */

		if( cfg.ATHtype != -1 ) {
			/* compute equal loudness weights (eql_w) */
			final float freq_inc = (float) cfg.samplerate / (float) (Encoder.BLKSIZE);
			float eql_balance = 0.0f;
			float freq = 0.0f;
			for(int i = 0; i < Encoder.BLKSIZE / 2; ++i ) {
				/* convert ATH dB to relative power (not dB) */
				/*  to determine eql_w */
				freq += freq_inc;
				gfc.ATH.eql_w[i] = 1.f / (float)Math.pow( 10., (float)(Util.ATHformula( cfg, freq ) / 10f) );
				eql_balance += gfc.ATH.eql_w[i];
			}
			eql_balance = 1.0f / eql_balance;
			for(int i = Encoder.BLKSIZE / 2; --i >= 0; ) { /* scale weights */
				gfc.ATH.eql_w[i] *= eql_balance;
			}
		}
		{
			for( int b = j = 0; b < gd.s.npart; ++b ) {
				for( int i = 0; i < gd.s.numlines[b]; ++i ) {
					++j;
				}
			}
			for( int b = j = 0; b < gd.l.npart; ++b ) {
				for( int i = 0, ie = gd.l.numlines[b]; i < ie; ++i ) {
					++j;
				}
			}
		}
		/* short block attack threshold */
		{
			float x = gfp.attackthre;
			float y = gfp.attackthre_s;
			if( x < 0 ) {
				x = NSATTACKTHRE;
			}
			if( y < 0 ) {
				y = NSATTACKTHRE_S;
			}
			gd.attack_threshold[0] = gd.attack_threshold[1] = gd.attack_threshold[2] = x;
			gd.attack_threshold[3] = y;
		}
		{
			float sk_s = -10.f, sk_l = -4.7f;
			if( gfp.VBR_q < 4 ) {
				sk_l = sk_s = sk[0];
			} else {
				sk_l = sk_s = sk[gfp.VBR_q] + gfp.VBR_q_frac * (sk[gfp.VBR_q] - sk[gfp.VBR_q + 1]);
			}
			int b = 0;
			for( final int npart = gd.s.npart; b < npart; b++ ) {
				final float m = (float) (npart - b) / npart;
				gd.s.masking_lower[b] = (float)Math.pow( 10., (double)(sk_s * m * 0.1f) );
			}
			for(; b < Encoder.CBANDS; ++b ) {
				gd.s.masking_lower[b] = 1.f;
			}
			b = 0;
			for( final int npart = gd.l.npart; b < npart; b++ ) {
				final float m = (float) (npart - b) / npart;
				gd.l.masking_lower[b] = (float)Math.pow( 10., (double)(sk_l * m * 0.1f) );
			}
			for(; b < Encoder.CBANDS; ++b ) {
				gd.l.masking_lower[b] = 1.f;
			}
		}
		gd.l_to_s.copyFrom( gd.l );
		init_numline( gd.l_to_s, sfreq, Encoder.BLKSIZE, 192, Encoder.SBMAX_s, gfc.scalefac_band.s );
		return 0;
	}
}
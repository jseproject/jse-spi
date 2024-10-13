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

// util.c

public class Util {
	static final long MAX_U_32_NUM = 0xffffffffL;
	/** smallest such that 1.0+DBL_EPSILON != 1.0 */
	static final double DBL_EPSILON = 2.2204460492503131e-016;
	/* smallest such that 1.0+FLT_EPSILON != 1.0 */
	//static final float FLT_EPSILON  = 1.192092896e-07F;

	//private static final float LOG2 = (float)Math.log( 2 );// 0.69314718055994530942;
	static final float LOG10 = (float)Math.log( 10 );// 2.30258509299404568402
	static final float SQRT2 = (float)Math.sqrt( 2 );// 1.41421356237309504880
	/* log/log10 approximations */// java: extracted inplace
/* #ifdef USE_FAST_LOG
	static final float FAST_LOG10(final float x)                 { return fast_log2( x ) * (LOG2 / LOG10); }
	static final float FAST_LOG(final float x)                   { return fast_log2( x ) * LOG2; }
	static final float FAST_LOG10_X(final float x, final float y){ return fast_log2( x ) * (LOG2 / LOG10 * y); }
	static final float FAST_LOG_X(final float x, final float y)  { return fast_log2( x ) * (LOG2 * y); }
#else
	static final float FAST_LOG10(final float x)                 { return (float)Math.log10( (double)x ); }
	static final float FAST_LOG(final float x)                   { return (float)Math.log( (double)x ); }
	static final float FAST_LOG10_X(final float x, final float y){ return ((float)Math.log10( (double)x ) * (y)); }
	static final float FAST_LOG_X(final float x, final float y)  { return ((float)Math.log( (double)x ) * (y)); }
#endif */

	//private static final int CRC16_POLYNOMIAL = 0x8005;

	static final int MAX_BITS_PER_CHANNEL = 4095;
	static final int MAX_BITS_PER_GRANULE = 7680;

	/***********************************************************************
	*
	*  Global Function Definitions
	*
	***********************************************************************/

	/* those ATH formulas are returning their minimum value for input = -1*/

	private static final float ATHformula_GB(float f, final float value, final float f_min, final float f_max)
	{
		/* from Painter & Spanias
		   modified by Gabriel Bouvigne to better fit the reality
		   ath =    3.640 * pow(f,-0.8)
		   - 6.800 * exp(-0.6*pow(f-3.4,2.0))
		   + 6.000 * exp(-0.15*pow(f-8.7,2.0))
		   + 0.6* 0.001 * pow(f,4.0);


		   In the past LAME was using the Painter &Spanias formula.
		   But we had some recurrent problems with HF content.
		   We measured real ATH values, and found the older formula
		   to be inacurate in the higher part. So we made this new
		   formula and this solved most of HF problematic testcases.
		   The tradeoff is that in VBR mode it increases a lot the
		   bitrate. */


		/*this curve can be udjusted according to the VBR scale:
		it adjusts from something close to Painter & Spanias
		on V9 up to Bouvigne's formula for V0. This way the VBR
		bitrate is more balanced according to the -V value.*/

		/* the following Hack allows to ask for the lowest value */
		if( f < -.3 ) {
			f = 3410;
		}

		f /= 1000;          /* convert to khz */
		f = ( f_min >= f ? f_min : f );
		f = ( f_max <= f ? f_max : f );

		final float ath = (float)(3.640 * Math.pow( f, -0.8 )
			- 6.800 * Math.exp(-0.6 * Math.pow( f - 3.4, 2.0 ))
			+ 6.000 * Math.exp(-0.15 * Math.pow( f - 8.7, 2.0 ))
			+ (0.6 + 0.04 * value) * 0.001 * Math.pow( f, 4.0 ));
		return ath;
	}

	static final float ATHformula(final SessionConfig cfg, final float f) {
		float ath;
		switch( cfg.ATHtype ) {
		case 0:
			ath = ATHformula_GB( f, 9, 0.1f, 24.0f );
			break;
		case 1:
			ath = ATHformula_GB( f, -1, 0.1f, 24.0f ); /*over sensitive, should probably be removed */
			break;
		case 2:
			ath = ATHformula_GB( f, 0, 0.1f, 24.0f );
			break;
		case 3:
			ath = ATHformula_GB( f, 1, 0.1f, 24.0f ) + 6; /*modification of GB formula by Roel */
			break;
		case 4:
			ath = ATHformula_GB( f, cfg.ATHcurve, 0.1f, 24.0f );
			break;
		case 5:
			ath = ATHformula_GB( f, cfg.ATHcurve, 3.41f, 16.1f );
			break;
		default:
			ath = ATHformula_GB( f, 0, 0.1f, 24.0f );
			break;
		}
		return ath;
	}

	/* see for example "Zwicker: Psychoakustik, 1982; ISBN 3-540-11401-7 */
	/** input: freq in hz  output: barks */
	static final float freq2bark(float freq) {
		if( freq < 0 ) {
			freq = 0;
		}
		freq *= 0.001f;
		return 13.0f * (float)Math.atan( (double)(.76f * freq) ) + 3.5f * (float)Math.atan( (double)(freq * freq / (7.5f * 7.5f)) );
	}

	/**
	 *
	 * @param bRate legal rates from 8 to 320
	 * @param version MPEG-1 or MPEG-2 LSF
	 * @param samplerate
	 * @return
	 */
	static final int FindNearestBitrate(final int bRate, int version, final int samplerate) {
		if( samplerate < 16000 ) {
			version = 2;
		}

		final int[] bitrate_table = Tables.bitrate_table[version];// java
		int bitrate = bitrate_table[1];

		for( int i = 2; i <= 14; i++ ) {
			final int v = bitrate_table[i];// java
			if( v > 0 ) {
				if( Math.abs( v - bRate ) < Math.abs( bitrate - bRate ) ) {
					bitrate = v;
				}
			}
		}
		return bitrate;
	}

	private static final int full_bitrate_table[] =
		{ 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320 };
	/** Used to find table index when
	 * we need bitrate-based values
	 * determined using tables
	 *
	 * bitrate in kbps
	 *
	 * Gabriel Bouvigne 2002-11-03
	 */
	static final int nearestBitrateFullIndex(final int bitrate) {
		/* borrowed from DM abr presets */
		int lower_range = 0, lower_range_kbps = 0, upper_range = 0, upper_range_kbps = 0;

		/* We assume specified bitrate will be 320kbps */
		upper_range_kbps = full_bitrate_table[16];
		upper_range = 16;
		lower_range_kbps = full_bitrate_table[16];
		lower_range = 16;

	    /* Determine which significant bitrates the value specified falls between,
	     * if loop ends without breaking then we were correct above that the value was 320
	     */
		for( int b = 1; b < 17; b++ ) {
			if( Math.max( bitrate, full_bitrate_table[b] ) != bitrate ) {
				upper_range = b;
				upper_range_kbps = full_bitrate_table[b];
				lower_range = b - 1;
				lower_range_kbps = full_bitrate_table[lower_range];
				break;      /* We found upper range */
			}
		}

		/* Determine which range the value specified is closer to */
		if( (upper_range_kbps - bitrate) > (bitrate - lower_range_kbps) ) {
			return lower_range;
		}
		return upper_range;
	}

	/** map frequency to a valid MP3 sample frequency
	 *
	 * Robert Hegemann 2000-07-01
	 */
	static final int map2MP3Frequency(final int freq) {
		if( freq <= 8000 ) {
			return 8000;
		}
		if( freq <= 11025 ) {
			return 11025;
		}
		if( freq <= 12000 ) {
			return 12000;
		}
		if( freq <= 16000 ) {
			return 16000;
		}
		if( freq <= 22050 ) {
			return 22050;
		}
		if( freq <= 24000 ) {
			return 24000;
		}
		if( freq <= 32000 ) {
			return 32000;
		}
		if( freq <= 44100 ) {
			return 44100;
		}

		return 48000;
	}

	/**
	 * convert bitrate in kbps to index
	 *
	 * @param bRate legal rates from 32 to 448 kbps
	 * @param version MPEG-1 or MPEG-2/2.5 LSF
	 * @param samplerate
	 * @return
	 */
	static final int BitrateIndex(final int bRate, int version, final int samplerate) {
		if( samplerate < 16000 ) {
			version = 2;
		}
		for( int i = 0; i <= 14; i++ ) {
			if( Tables.bitrate_table[version][i] > 0 ) {
				if( Tables.bitrate_table[version][i] == bRate ) {
					return i;
				}
			}
		}
		return -1;
	}

	/** convert samp freq in Hz to index */
	public static final int SmpFrqIndex(final int sample_freq) {
		switch( sample_freq ) {
		case 44100:
			return 0;
		case 48000:
			return 1;
		case 32000:
			return 2;
		case 22050:
			return 0;
		case 24000:
			return 1;
		case 16000:
			return 2;
		case 11025:
			return 0;
		case 12000:
			return 1;
		case 8000:
			return 2;
		default:
			return -1;
		}
	}
	/** convert samp freq in Hz to version */
	static final int getVersion(final int sample_freq) {
		switch( sample_freq ) {
		case 44100:
			return 1;
		case 48000:
			return 1;
		case 32000:
			return 1;
		case 22050:
			return 0;
		case 24000:
			return 0;
		case 16000:
			return 0;
		case 11025:
			return 0;
		case 12000:
			return 0;
		case 8000:
			return 0;
		default:;
			return 0;
		}
	}

	/*****************************************************************************
	*
	*  End of bit_stream.c package
	*
	*****************************************************************************/

	/** copy in new samples from in_buffer into mfbuf, with resampling
	   if necessary.  n_in = number of samples from the input buffer that
	   were used.  n_out = number of samples copied into mfbuf

	   @return java: n_out | (n_in << 32)
	  */
	static final long fill_buffer(final LAME_InternalFlags gfc,
		final float mfbuf[/* 2 */][],
		final float in_buffer[/* 2 */][], final int inoffset,
		final int nsamples/*, final int[] n_in, final int[] n_out*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final int mf_size = gfc.sv_enc.mf_size;
		final int framesize = 576 * cfg.mode_gr;
		int ch = 0;
		final int nch = cfg.channels_out;

		/* copy in new samples into mfbuf */
		final int nout = (framesize <= nsamples ? framesize : nsamples);
		do {
			System.arraycopy( in_buffer[ch], inoffset, mfbuf[ch], mf_size, nout );
		} while( ++ch < nch );
		//n_out[0] = nout;
		//n_in[0] = nout;
		return (long)nout | ((long)nout << 32);
	}

/***********************************************************************
 *
 * Fast Log Approximation for log2, used to approximate every other log
 * (log10 and log)
 * maximum absolute error for log10 is around 10-6
 * maximum *relative* error can be high when x is almost 1 because error/log10(x) tends toward x/e
 *
 * use it if typical RESULT values are > 1e-5 (for example if x>1.00001 or x<0.99999)
 * or if the relative precision in the domain around 1 is not important (result in 1 is exact and 0)
 *
 ***********************************************************************/

	private static final int LOG2_SIZE = 512;
	// private static final int LOG2_SIZE_L2 = 9;

	private static final float log_table[] = new float[LOG2_SIZE + 1];
	private static boolean init = false;

	static final void init_log_table() {
		/* Range for log2(x) over [1,2[ is [0,1[ */
		// assert((1 << LOG2_SIZE_L2) == LOG2_SIZE);

		if( ! init ) {
			for( int j = 0; j < LOG2_SIZE + 1; j++ ) {
				log_table[j] = (float)(Math.log( 1.0 + j / (double) LOG2_SIZE) / Math.log( 2.0 ));
			}
		}
		init = true;
	}
/*
	private static final float fast_log2(final float x) {
		final int fi = Float.floatToRawIntBits( x );
		int mantisse = fi & 0x7fffff;
		float log2val = ((fi >> 23) & 0xFF) - 0x7f;
		float partial = (mantisse & ((1 << (23 - LOG2_SIZE_L2)) - 1));
		partial *= 1.0f / ((1 << (23 - LOG2_SIZE_L2)));

		mantisse >>= (23 - LOG2_SIZE_L2);

		// log2val += log_table[mantisse];  without interpolation the results are not good
		log2val += log_table[mantisse] * (1.0f - partial) + log_table[mantisse + 1] * partial;

		return log2val;
	}
*/
}
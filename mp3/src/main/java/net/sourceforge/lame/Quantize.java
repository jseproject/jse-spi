package net.sourceforge.lame;

import java.util.Arrays;

// quantize.c

class Quantize {

	static final boolean EQ(final float a, final float b ) {
		return (Math.abs(a) > Math.abs(b))
			 ? (Math.abs((a)-(b)) <= (Math.abs(a) * 1e-6f))
			 : (Math.abs((a)-(b)) <= (Math.abs(b) * 1e-6f));
	};

	static final void init_xrpow_core_init(final InternalFlags gfc) {
		// gfc.init_xrpow_core = init_xrpow_core_c;
	}

	static final boolean init_xrpow(final InternalFlags gfc, final III_GrInfo cod_info, final float xrpow[/*576*/]) {
		final int upper = cod_info.max_nonzero_coeff;

		cod_info.xrpow_max = 0;

		/*  check if there is some energy we have to quantize
		 *  and calculate xrpow matching our fresh scalefactors
		 */
		for( int i = upper; i < 576; i++ ) {
			xrpow[i] = 0;
		}

		final float sum = cod_info.init_xrpow_core_c( xrpow, upper/*, &sum*/ );

		/*  return 1 if we have something to quantize, else 0 */
		if( sum > 1E-20f ) {
			final boolean j = ( (gfc.sv_qnt.substep_shaping & 2) != 0 );

			for( int i = 0; i < cod_info.psymax; i++ ) {
				gfc.sv_qnt.pseudohalf[i] = j;
			}

			return true;
		}

		for( int i = 0; i < 576; i++ ) {
			cod_info.l3_enc[i] = 0;
		}
		return false;
	}

	/**
	Gabriel Bouvigne feb/apr 2003
	Analog silence detection in partitionned sfb21
	or sfb12 for short blocks

	From top to bottom of sfb, changes to 0
	coeffs which are below ath. It stops on the first
	coeff higher than ath.
	*/
	private static final void psfb21_analogsilence(final InternalFlags gfc, final III_GrInfo cod_info) {
		final ATH ATH = gfc.ATH;
		final float[] xr = cod_info.xr;

		if( cod_info.block_type != Encoder.SHORT_TYPE ) { /* NORM, START or STOP type, but not SHORT blocks */
			final float longfact_21 = gfc.sv_qnt.longfact[21];// java
			final boolean is_more = longfact_21 > 1e-12f;// java
			final int[] psfb21 = gfc.scalefac_band.psfb21;// java
			boolean stop = false;
			for(int gsfb = Encoder.PSFB21 - 1; gsfb >= 0 && ! stop; gsfb-- ) {
				final int start = psfb21[gsfb];
				final int end = psfb21[gsfb + 1];
				float ath21 = QuantizePVT.athAdjust( ATH.adjust_factor, ATH.psfb21[gsfb], ATH.floor, 0 );

				if( is_more ) {
					ath21 *= longfact_21;
				}

				for( int j = end - 1; j >= start; j-- ) {
					final float v = xr[j];
					if( ( v >= 0 ? v : -v ) < ath21 ) {
						xr[j] = 0;
					} else {
						stop = true;
						break;
					}
				}
			}
			return;
		}// else {
			final float shortfact_12 = gfc.sv_qnt.shortfact[12];// java
			final boolean is_more = shortfact_12 > 1e-12f;// java
			final int[] psfb12 = gfc.scalefac_band.psfb12;// java
			final int[] s = gfc.scalefac_band.s;// java
			/*note: short blocks coeffs are reordered */
			for( int block = 0; block < 3; block++ ) {
				boolean stop = false;
				for(int gsfb = Encoder.PSFB12 - 1; gsfb >= 0 && ! stop; gsfb-- ) {
					int start = s[12];
					start = start * 3 + (s[13] - start) * block + (psfb12[gsfb] - psfb12[0]);
					final int end = start + (psfb12[gsfb + 1] - psfb12[gsfb]);
					float ath12 = QuantizePVT.athAdjust( ATH.adjust_factor, ATH.psfb12[gsfb], ATH.floor, 0 );

					if( is_more ) {
						ath12 *= shortfact_12;
					}

					for( int j = end - 1; j >= start; j-- ) {
						final float v = xr[j];
						if( (v >= 0 ? v : -v) < ath12 ) {
							xr[j] = 0;
						} else {
							stop = true;
							break;
						}
					}
				}
			}
		//}
	}

	static final void init_outer_loop(final InternalFlags gfc, final III_GrInfo cod_info) {
		final SessionConfig cfg = gfc.cfg;
		/*  initialize fresh cod_info */
		cod_info.part2_3_length = 0;
		cod_info.big_values = 0;
		cod_info.count1 = 0;
		cod_info.global_gain = 210;
		cod_info.scalefac_compress = 0;
		/* mixed_block_flag, block_type was set in psymodel.c */
		cod_info.table_select[0] = 0;
		cod_info.table_select[1] = 0;
		cod_info.table_select[2] = 0;
		cod_info.subblock_gain[0] = 0;
		cod_info.subblock_gain[1] = 0;
		cod_info.subblock_gain[2] = 0;
		cod_info.subblock_gain[3] = 0; /* this one is always 0 */
		cod_info.region0_count = 0;
		cod_info.region1_count = 0;
		cod_info.preflag = false;
		cod_info.scalefac_scale = 0;
		cod_info.count1table_select = 0;
		cod_info.part2_length = 0;
		if( cfg.samplerate <= 8000 ) {
			cod_info.sfb_lmax = 17;
			cod_info.sfb_smin = 9;
			cod_info.psy_lmax = 17;
		} else {
			cod_info.sfb_lmax = Encoder.SBPSY_l;
			cod_info.sfb_smin = Encoder.SBPSY_s;
			cod_info.psy_lmax = gfc.sv_qnt.sfb21_extra ? Encoder.SBMAX_l : Encoder.SBPSY_l;
		}
		cod_info.psymax = cod_info.psy_lmax;
		cod_info.sfbmax = cod_info.sfb_lmax;
		cod_info.sfbdivide = 11;
		final int[] band_l = gfc.scalefac_band.l;// java
		final int[] width = cod_info.width;// java
		final int[] cod_info_window = cod_info.window;// java
		for(int sfb = 0; sfb < Encoder.SBMAX_l; sfb++ ) {
			width[sfb] = band_l[sfb + 1] - band_l[sfb];
			cod_info_window[sfb] = 3; /* which is always 0. */
		}
		if( cod_info.block_type == Encoder.SHORT_TYPE ) {
			final float ixwork[] = new float[576];

			cod_info.sfb_smin = 0;
			cod_info.sfb_lmax = 0;
			if( cod_info.mixed_block_flag ) {
				/*
				 *  MPEG-1:      sfbs 0-7 long block, 3-12 short blocks
				 *  MPEG-2(.5):  sfbs 0-5 long block, 3-12 short blocks
				 */
				cod_info.sfb_smin = 3;
				cod_info.sfb_lmax = (cfg.mode_gr << 1) + 4;
			}
			if( cfg.samplerate <= 8000 ) {
				cod_info.psymax
					= cod_info.sfb_lmax
					+ 3 * (9 - cod_info.sfb_smin);
				cod_info.sfbmax = cod_info.sfb_lmax + 3 * (9 - cod_info.sfb_smin);
			} else {
				cod_info.psymax
					= cod_info.sfb_lmax
					+ 3 * ((gfc.sv_qnt.sfb21_extra ? Encoder.SBMAX_s : Encoder.SBPSY_s) - cod_info.sfb_smin);
				cod_info.sfbmax = cod_info.sfb_lmax + 3 * (Encoder.SBPSY_s - cod_info.sfb_smin);
			}
			cod_info.sfbdivide = cod_info.sfbmax - 18;
			cod_info.psy_lmax = cod_info.sfb_lmax;
			/* re-order the short blocks, for more efficient encoding below */
			/* By Takehiro TOMINAGA */
			/*
			   Within each scalefactor band, data is given for successive
			   time windows, beginning with window 0 and ending with window 2.
			   Within each window, the quantized values are then arranged in
			   order of increasing frequency...
			 */
			final float[] x = cod_info.xr;// java
			int ix = band_l[cod_info.sfb_lmax];// x[ix]
			System.arraycopy( cod_info.xr, 0, ixwork, 0, 576 );
			final int[] band_s = gfc.scalefac_band.s;// java
			for(int sfb = cod_info.sfb_smin; sfb < Encoder.SBMAX_s; sfb++ ) {
				final int start = band_s[sfb];
				final int end = band_s[sfb + 1];
				for( int window = 0; window < 3; window++ ) {
					for( int l = start; l < end; l++ ) {
						x[ix++] = ixwork[3 * l + window];
					}
				}
			}

			int j = cod_info.sfb_lmax;
			for(int sfb = cod_info.sfb_smin; sfb < Encoder.SBMAX_s; sfb++ ) {
				width[j] = width[j + 1] = width[j + 2] = band_s[sfb + 1] - band_s[sfb];
				cod_info_window[j] = 0;
				cod_info_window[j + 1] = 1;
				cod_info_window[j + 2] = 2;
				j += 3;
			}
		}

		cod_info.count1bits = 0;
		cod_info.sfb_partition_table = QuantizePVT.nr_of_sfb_block[0][0];
		cod_info.slen[0] = 0;
		cod_info.slen[1] = 0;
		cod_info.slen[2] = 0;
		cod_info.slen[3] = 0;

		cod_info.max_nonzero_coeff = 575;

		/*  fresh scalefactors are all zero */
		final int[] buf = cod_info.scalefac;
		int i = buf.length;
		do {
			buf[--i] = 0;
		} while( i > 0 );

		if( cfg.vbr != LAME.vbr_mt && cfg.vbr != LAME.vbr_mtrh && cfg.vbr != LAME.vbr_abr && cfg.vbr != LAME.vbr_off ) {
			psfb21_analogsilence( gfc, cod_info );
		}
	}

/************************************************************************
 *
 *      bin_search_StepSize()
 *
 *  author/date??
 *
 *  binary step size search
 *  used by outer_loop to get a quantizer step size to start with
 *
 ************************************************************************/

	//typedef enum {
	private static final int BINSEARCH_NONE = 0;
	private static final int BINSEARCH_UP = 1;
	private static final int BINSEARCH_DOWN = 2;
	//} binsearchDirection_t;

	private static final int bin_search_StepSize(final InternalFlags gfc, final III_GrInfo cod_info, int desired_rate, final int ch, final float xrpow[/*576*/])
	{
		int CurrentStep = gfc.sv_qnt.CurrentStep[ch];
		boolean flag_GoneOver = false;
		final int start = gfc.sv_qnt.OldValue[ch];
		int /*binsearchDirection_t*/ Direction = BINSEARCH_NONE;
		cod_info.global_gain = start;
		desired_rate -= cod_info.part2_length;
		int nBits;
		for( ;; ) {
			nBits = Takehiro.count_bits( gfc, xrpow, cod_info, null );

			if( CurrentStep == 1 || nBits == desired_rate ) {
				break;      /* nothing to adjust anymore */
			}

			int step;
			if( nBits > desired_rate ) {
				/* increase Quantize_StepSize */
				if( Direction == BINSEARCH_DOWN ) {
					flag_GoneOver = true;
				}

				if( flag_GoneOver ) {
					CurrentStep >>= 1;
				}
				Direction = BINSEARCH_UP;
				step = CurrentStep;
			} else {
				/* decrease Quantize_StepSize */
				if( Direction == BINSEARCH_UP ) {
					flag_GoneOver = true;
				}

				if( flag_GoneOver ) {
					CurrentStep >>= 1;
				}
				Direction = BINSEARCH_DOWN;
				step = -CurrentStep;
			}
			cod_info.global_gain += step;
			if( cod_info.global_gain < 0 ) {
				cod_info.global_gain = 0;
				flag_GoneOver = true;
			}
			if( cod_info.global_gain > 255 ) {
				cod_info.global_gain = 255;
				flag_GoneOver = true;
			}
		}

		while( nBits > desired_rate && cod_info.global_gain < 255 ) {
			cod_info.global_gain++;
			nBits = Takehiro.count_bits( gfc, xrpow, cod_info, null );
		}
		gfc.sv_qnt.CurrentStep[ch] = (start - cod_info.global_gain >= 4) ? 4 : 2;
		gfc.sv_qnt.OldValue[ch] = cod_info.global_gain;
		cod_info.part2_3_length = nBits;
		return nBits;
	}

	/************************************************************************
	 *
	 *      trancate_smallspectrums()
	 *
	 *  Takehiro TOMINAGA 2002-07-21
	 *
	 *  trancate smaller nubmers into 0 as long as the noise threshold is allowed.
	 *
	 ************************************************************************/
	/* private static final int floatcompare(final float a, final float b) {
		if( a > b ) {
			return 1;
		}
		if( a < b ) {
			return -1;
		}
		return 0;
	} */

	static final void trancate_smallspectrums(final InternalFlags gfc, final III_GrInfo gi, final float[] l3_xmin, final float[] work) {
		final float distort[] = new float[Encoder.SFBMAX];
		final CalcNoiseResult dummy = new CalcNoiseResult();

		if( (0 == (gfc.sv_qnt.substep_shaping & 4) && gi.block_type == Encoder.SHORT_TYPE )
				|| (gfc.sv_qnt.substep_shaping & 0x80) != 0 ) {
			return;
		}
		QuantizePVT.calc_noise( gi, l3_xmin, distort, dummy, null );
		final int[] l3_enc = gi.l3_enc;// java
		final float[] gi_xr = gi.xr;// java
		for( int j = 0; j < 576; j++ ) {
			float xr = 0.0f;
			if( l3_enc[j] != 0 ) {
				xr = gi_xr[j];
				if( xr < 0 ) {
					xr = -xr;
				}
			}
			work[j] = xr;
		}

		int j = 0;
		int sfb = 8;
		if( gi.block_type == Encoder.SHORT_TYPE ) {
			sfb = 6;
		}
		do {
			final int width = gi.width[sfb];
			j += width;
			if( distort[sfb] >= 1.0f ) {
				continue;
			}

			// qsort( &work[j - width], width, sizeof(float), floatcompare );
			Arrays.sort( work, j - width, j );// TODO java: check sort order
			if( EQ( work[j - 1], 0.0f ) ) {
				continue;
			}   /* all zero sfb */

			float allowedNoise = (1.0f - distort[sfb]) * l3_xmin[sfb];
			float trancateThreshold = 0.0f;
			int jw = j - width;
			int start = 0;
			do {
				int nsame = 1;
				for( ; start + nsame < width; nsame++ ) {
					if( ! EQ( work[start + jw], work[start + jw + nsame] ) ) {
						break;
					}
				}

				final float noise = work[start + jw] * work[start + jw] * nsame;
				if( allowedNoise < noise ) {
					if( start != 0 ) {
						trancateThreshold = work[start + jw - 1];
					}
					break;
				}
				allowedNoise -= noise;
				start += nsame;
			} while( start < width );
			if( EQ( trancateThreshold, 0.0f ) ) {
				continue;
			}

/*      printf("%e %e %e\n", */
/*             trancateThreshold/l3_xmin[sfb], */
/*             trancateThreshold/(l3_xmin[sfb]*start), */
/*             trancateThreshold/(l3_xmin[sfb]*(start+width)) */
/*          ); */
/*      if( trancateThreshold > 1000*l3_xmin[sfb]*start) */
/*          trancateThreshold = 1000*l3_xmin[sfb]*start; */

			// java: check the loop
			do {
				final float v = gi_xr[jw];// xr[j - width]
				if( (v >= 0 ? v : -v) <= trancateThreshold ) {
					l3_enc[jw] = 0;// l3_enc[j - width]
				}
			} while( ++jw < j );// (--width > 0)
		} while( ++sfb < gi.psymax );

		gi.part2_3_length = Takehiro.noquant_count_bits( gfc, gi, null );
	}

/*  mt 5/99:  Function: Improved calc_noise for a single channel   */

/*************************************************************************
 *
 *      quant_compare()
 *
 *  author/date??
 *
 *  several different codes to decide which quantization is better
 *
 *************************************************************************/

	private static final float penalties(final float noise) {
		return (float)Math.log10( (double)(0.368f + 0.632f * noise * noise * noise) );// Util.FAST_LOG10( 0.368f + 0.632f * noise * noise * noise );
	}

	private static final float get_klemm_noise(final float[] distort, final III_GrInfo gi) {
		float klemm_noise = 1E-37f;
		for( int sfb = 0, psymax = gi.psymax; sfb < psymax; sfb++ ) {
			klemm_noise += penalties( distort[sfb] );
		}

		return (1e-20f >= klemm_noise ? 1e-20f : klemm_noise);
	}

	private static final boolean quant_compare(final int quant_comp,
											   final CalcNoiseResult best,
											   final CalcNoiseResult calc, final III_GrInfo gi, final float[] distort)
	{
		/*
		   noise is given in decibels (dB) relative to masking thesholds.

		   over_noise:  ??? (the previous comment is fully wrong)
		   tot_noise:   ??? (the previous comment is fully wrong)
		   max_noise:   max quantization noise

		 */
		boolean better;

		switch( quant_comp ) {
		default:
		case 9: {
			if( best.over_count > 0 ) {
				/* there are distorted sfb */
				better = calc.over_SSD <= best.over_SSD;
				if( calc.over_SSD == best.over_SSD ) {
					better = calc.bits < best.bits;
				}
			} else {
				/* no distorted sfb */
				better = ((calc.max_noise < 0) &&
						((calc.max_noise * 10 + calc.bits) <=
						(best.max_noise * 10 + best.bits)));
			}
			break;
		}

		case 0:
			better = calc.over_count < best.over_count
				|| (calc.over_count == best.over_count && calc.over_noise < best.over_noise)
				|| (calc.over_count == best.over_count &&
				EQ( calc.over_noise, best.over_noise) && calc.tot_noise < best.tot_noise );
			break;

		case 8:
			calc.max_noise = get_klemm_noise( distort, gi );
		/*lint --fallthrough */
		case 1:
			better = calc.max_noise < best.max_noise;
			break;
		case 2:
			better = calc.tot_noise < best.tot_noise;
			break;
		case 3:
			better = (calc.tot_noise < best.tot_noise)
			&& (calc.max_noise < best.max_noise);
			break;
		case 4:
			better = (calc.max_noise <= 0.0 && best.max_noise > 0.2f)
				|| (calc.max_noise <= 0.0f &&
				best.max_noise < 0.0f &&
				best.max_noise > calc.max_noise - 0.2f && calc.tot_noise < best.tot_noise)
				|| (calc.max_noise <= 0.0f &&
				best.max_noise > 0.0f &&
				best.max_noise > calc.max_noise - 0.2f &&
				calc.tot_noise < best.tot_noise + best.over_noise)
				|| (calc.max_noise > 0.0f &&
						best.max_noise > -0.05f &&
						best.max_noise > calc.max_noise - 0.1f &&
						calc.tot_noise + calc.over_noise < best.tot_noise + best.over_noise)
				|| (calc.max_noise > 0.0f &&
						best.max_noise > -0.1f &&
						best.max_noise > calc.max_noise - 0.15f &&
						calc.tot_noise + calc.over_noise + calc.over_noise <
						best.tot_noise + best.over_noise + best.over_noise);
			break;
		case 5:
			better = calc.over_noise < best.over_noise
				|| (EQ(calc.over_noise, best.over_noise) && calc.tot_noise < best.tot_noise);
			break;
		case 6:
			better = calc.over_noise < best.over_noise
				|| (EQ(calc.over_noise, best.over_noise) &&
					(calc.max_noise < best.max_noise
						|| (EQ(calc.max_noise, best.max_noise) && calc.tot_noise <= best.tot_noise)
				));
			break;
		case 7:
			better = calc.over_count < best.over_count || calc.over_noise < best.over_noise;
			break;
		}

		if( best.over_count == 0 ) {
			/*
			   If no distorted bands, only use this quantization
			   if it is better, and if it uses less bits.
			   Unfortunately, part2_3_length is sometimes a poor
			   estimator of the final size at low bitrates.
			 */
			better = better && calc.bits < best.bits;
		}

		return better;
	}

/*************************************************************************
 *
 *          amp_scalefac_bands()
 *
 *  author/date??
 *
 *  Amplify the scalefactor bands that violate the masking threshold.
 *  See ISO 11172-3 Section C.1.5.4.3.5
 *
 *  distort[] = noise/masking
 *  distort[] > 1   ==> noise is not masked
 *  distort[] < 1   ==> noise is masked
 *  max_dist = maximum value of distort[]
 *
 *  Three algorithms:
 *  noise_shaping_amp
 *        0             Amplify all bands with distort[]>1.
 *
 *        1             Amplify all bands with distort[] >= max_dist^(.5);
 *                     ( 50% in the db scale)
 *
 *        2             Amplify first band with distort[] >= max_dist;
 *
 *
 *  For algorithms 0 and 1, if max_dist < 1, then amplify all bands
 *  with distort[] >= .95*max_dist.  This is to make sure we always
 *  amplify at least one band.
 *
 *
 *************************************************************************/
	private static final void amp_scalefac_bands(final InternalFlags gfc,
												 final III_GrInfo cod_info, final float[] distort, final float xrpow[/*576*/], final boolean bRefine)
	{
		final SessionConfig cfg = gfc.cfg;

		float ifqstep34;
		if( cod_info.scalefac_scale == 0 ) {
			ifqstep34 = 1.29683955465100964055f; /* 2**(.75*.5) */
		} else {
			ifqstep34 = 1.68179283050742922612f; /* 2**(.75*1) */
		}

		/* compute maximum value of distort[]  */
		float trigger = 0;
		for( int sfb = 0; sfb < cod_info.sfbmax; sfb++ ) {
			if( trigger < distort[sfb] ) {
				trigger = distort[sfb];
			}
		}

		int noise_shaping_amp = cfg.noise_shaping_amp;
		if( noise_shaping_amp == 3 ) {
			if( bRefine ) {
				noise_shaping_amp = 2;
			} else {
				noise_shaping_amp = 1;
			}
		}
		switch (noise_shaping_amp ) {
		case 2:
			/* amplify exactly 1 band */
			break;

		case 1:
			/* amplify bands within 50% of max (on db scale) */
			if( trigger > 1.0f ) {
				trigger = (float)Math.pow( trigger, .5 );
			} else {
				trigger *= .95f;
			}
			break;

		case 0:
		default:
			/* ISO algorithm.  amplify all bands with distort>1 */
			if( trigger > 1.0f ) {
				trigger = 1.0f;
			} else {
				trigger *= .95f;
			}
			break;
		}

		final int[] cod_info_width = cod_info.width;// java
		final boolean[] pseudohalf = gfc.sv_qnt.pseudohalf;// java
		final int[] scalefac = cod_info.scalefac;// java
		int j = 0;
		for( int sfb = 0, sfbmax = cod_info.sfbmax; sfb < sfbmax; sfb++ ) {
			final int width = cod_info_width[sfb];
			j += width;
			if( distort[sfb] < trigger ) {
				continue;
			}

			if( (gfc.sv_qnt.substep_shaping & 2) != 0 ) {
				pseudohalf[sfb] = ! pseudohalf[sfb];
				if( ! pseudohalf[sfb] && cfg.noise_shaping_amp == 2 ) {
					return;
				}
			}
			scalefac[sfb]++;
			for( int l = -width + j; l < j; l++ ) {
				xrpow[l] *= ifqstep34;
				if( xrpow[l] > cod_info.xrpow_max ) {
					cod_info.xrpow_max = xrpow[l];
				}
			}

			if( cfg.noise_shaping_amp == 2 ) {
				return;
			}
		}
	}

/*************************************************************************
 *
 *      inc_subblock_gain()
 *
 *  Takehiro Tominaga 2000-xx-xx
 *
 *  increases the subblock gain and adjusts scalefactors
 *
 *************************************************************************/
	private static final boolean inc_subblock_gain(final InternalFlags gfc, final III_GrInfo cod_info, final float xrpow[/*576*/]) {
		final int[] scalefac = cod_info.scalefac;

		final int sfb_lmax = cod_info.sfb_lmax;// java
		final int sfbmax = cod_info.sfbmax;// java
		/* subbloc_gain can't do anything in the long block region */
		for( int sfb = 0; sfb < sfb_lmax; sfb++ ) {
			if( scalefac[sfb] >= 16 ) {
				return true;
			}
		}

		final int max = gfc.scalefac_band.l[sfb_lmax];// java
		final int scale4 = (4 >> cod_info.scalefac_scale);// java
		final int scale1 = cod_info.scalefac_scale + 1;// java
		final int sfbdivide = cod_info.sfbdivide;// java
		for( int window = 0; window < 3; window++ ) {
			int s1, s2, sfb;
			s1 = s2 = 0;

			for( sfb = sfb_lmax + window; sfb < sfbdivide; sfb += 3 ) {
				if( s1 < scalefac[sfb] ) {
					s1 = scalefac[sfb];
				}
			}
			for( ; sfb < sfbmax; sfb += 3 ) {
				if( s2 < scalefac[sfb] ) {
					s2 = scalefac[sfb];
				}
			}

			if( s1 < 16 && s2 < 8 ) {
				continue;
			}

			if( cod_info.subblock_gain[window] >= 7 ) {
				return true;
			}

			/* even though there is no scalefactor for sfb12
			 * subblock gain affects upper frequencies too, that's why
			 * we have to go up to SBMAX_s
			 */
			cod_info.subblock_gain[window]++;
			int j = max;
			for( sfb = sfb_lmax + window; sfb < sfbmax; sfb += 3 ) {
				final int width = cod_info.width[sfb];
				int s = scalefac[sfb];

				s = s - scale4;
				if( s >= 0 ) {
					scalefac[sfb] = s;
					j += width * 3;
					continue;
				}

				scalefac[sfb] = 0;
				//{
					final int gain = 210 + (s << scale1);
					final float amp = QuantizePVT.ipow20[ gain ];
				//}
				j += width * (window + 1);
				for( int l = -width + j; l < j; l++ ) {
					xrpow[l] *= amp;
					if( xrpow[l] > cod_info.xrpow_max ) {
						cod_info.xrpow_max = xrpow[l];
					}
				}
				j += width * (3 - window - 1);
			}

			{
				final float amp = QuantizePVT.ipow20[ 202 ];
				j += cod_info.width[sfb] * (window + 1);
				for( int l = -cod_info.width[sfb] + j; l < j; l++ ) {
					xrpow[l] *= amp;
					if( xrpow[l] > cod_info.xrpow_max ) {
						cod_info.xrpow_max = xrpow[l];
					}
				}
			}
		}
		return false;
	}

/********************************************************************
 *
 *      balance_noise()
 *
 *  Takehiro Tominaga /date??
 *  Robert Hegemann 2000-09-06: made a function of it
 *
 *  amplifies scalefactor bands,
 *   - if all are already amplified returns 0
 *   - if some bands are amplified too much:
 *      * try to increase scalefac_scale
 *      * if already scalefac_scale was set
 *          try on short blocks to increase subblock gain
 *
 ********************************************************************/
	private static final boolean balance_noise(final InternalFlags gfc,
											   final III_GrInfo cod_info, final float[] distort, final float xrpow[/*576*/], final boolean bRefine)
	{
		final SessionConfig cfg = gfc.cfg;

		amp_scalefac_bands( gfc, cod_info, distort, xrpow, bRefine );

		/* check to make sure we have not amplified too much
		 * loop_break returns 0 if there is an unamplified scalefac
		 * scale_bitcount returns 0 if no scalefactors are too large
		 */

		boolean status = cod_info.loop_break();

		if( status ) {
			return false;
		}       /* all bands amplified */

		/* not all scalefactors have been amplified.  so these
		 * scalefacs are possibly valid.  encode them:
		 */
		status = Takehiro.scale_bitcount( gfc, cod_info );

		if( ! status ) {
			return true;
		}       /* amplified some bands not exceeding limits */

		/*  some scalefactors are too large.
		 *  lets try setting scalefac_scale=1
		 */
		if( cfg.noise_shaping > 1 ) {
			final boolean[] pseudohalf = gfc.sv_qnt.pseudohalf;
			int i = pseudohalf.length;
			do {
				pseudohalf[--i] = false;
			} while( i > 0 );

			if( 0 == cod_info.scalefac_scale ) {
				cod_info.inc_scalefac_scale( xrpow );
				status = false;
			} else {
				if( cod_info.block_type == Encoder.SHORT_TYPE && cfg.subblock_gain > 0 ) {
					status = inc_subblock_gain( gfc, cod_info, xrpow )
					|| cod_info.loop_break();
				}
			}
		}

		if( ! status ) {
			status = Takehiro.scale_bitcount( gfc, cod_info );
		}
		return ! status;
	}

/************************************************************************
 *
 *  outer_loop ()
 *
 *  Function: The outer iteration loop controls the masking conditions
 *  of all scalefactorbands. It computes the best scalefac and
 *  global gain. This module calls the inner iteration loop
 *
 *  mt 5/99 completely rewritten to allow for bit reservoir control,
 *  mid/side channels with L/R or mid/side masking thresholds,
 *  and chooses best quantization instead of last quantization when
 *  no distortion free quantization can be found.
 *
 *  added VBR support mt 5/99
 *
 *  some code shuffle rh 9/00
 ************************************************************************/

	static final int outer_loop(final InternalFlags gfc, final III_GrInfo cod_info, final float[] l3_xmin, /* allowed distortion */
           final float xrpow[/*576*/], /* coloured magnitudes of spectral */
           final int ch, final int targ_bits)
	{                       /* maximum allowed bits */
		final SessionConfig cfg = gfc.cfg;
		final float save_xrpow[] = new float[576];
		final float distort[] = new float[Encoder.SFBMAX];
		final CalcNoiseResult best_noise_info = new CalcNoiseResult();
		final CalcNoiseData prev_noise = new CalcNoiseData();// java: already zeroed
		int best_part2_3_length = 9999999;
		boolean bEndOfSearch = false;
		boolean bRefine = false;
		int best_ggain_pass1 = 0;

		bin_search_StepSize( gfc, cod_info, targ_bits, ch, xrpow );

		if( 0 == cfg.noise_shaping ) {
			return 100;
		}     /* default noise_info.over_count */

		/* compute the distortion in this quantization */
		/* coefficients and thresholds both l/r (or both mid/side) */
		QuantizePVT.calc_noise( cod_info, l3_xmin, distort, best_noise_info, prev_noise );
		best_noise_info.bits = cod_info.part2_3_length;

		final III_GrInfo cod_info_w = new III_GrInfo( cod_info );
		int age = 0;
		/* if( cfg.vbr == vbr_rh || cfg.vbr == vbr_mtrh) */
		System.arraycopy( xrpow, 0, save_xrpow, 0, 576 );

		final CalcNoiseResult noise_info = new CalcNoiseResult();// java: moved up

		while( ! bEndOfSearch ) {
			/* BEGIN MAIN LOOP */
			do {
				int maxggain = 255;

				/* When quantization with no distorted bands is found,
				 * allow up to X new unsuccesful tries in serial. This
				 * gives us more possibilities for different quant_compare modes.
				 * Much more than 3 makes not a big difference, it is only slower.
				 */
				final int search_limit = ( (gfc.sv_qnt.substep_shaping & 2) != 0 ) ? 20 : 3;

				/* Check if the last scalefactor band is distorted.
				 * in VBR mode we can't get rid of the distortion, so quit now
				 * and VBR mode will try again with more bits.
				 * (makes a 10% speed increase, the files I tested were
				 * binary identical, 2000/05/20 Robert Hegemann)
				 * distort[] > 1 means noise > allowed noise
				 */
				if( gfc.sv_qnt.sfb21_extra ) {
					if( distort[cod_info_w.sfbmax] > 1.0f ) {
						break;
					}
					if( cod_info_w.block_type == Encoder.SHORT_TYPE
							&& (distort[cod_info_w.sfbmax + 1] > 1.0f
							|| distort[cod_info_w.sfbmax + 2] > 1.0f) ) {
						break;
					}
				}

				/* try a new scalefactor conbination on cod_info_w */
				if( ! balance_noise( gfc, cod_info_w, distort, xrpow, bRefine ) ) {
					break;
				}
				if( cod_info_w.scalefac_scale != 0 ) {
					maxggain = 254;
				}

				/* inner_loop starts with the initial quantization step computed above
				 * and slowly increases until the bits < huff_bits.
				 * Thus it is important not to start with too large of an inital
				 * quantization step.  Too small is ok, but inner_loop will take longer
				 */
				final int huff_bits = targ_bits - cod_info_w.part2_length;
				if( huff_bits <= 0 ) {
					break;
				}

				/*  increase quantizer stepsize until needed bits are below maximum */
				while( (cod_info_w.part2_3_length
						= Takehiro.count_bits( gfc, xrpow, cod_info_w, prev_noise )) > huff_bits
						&& cod_info_w.global_gain <= maxggain ) {
					cod_info_w.global_gain++;
				}

				if( cod_info_w.global_gain > maxggain ) {
					break;
				}

				if( best_noise_info.over_count == 0 ) {

				while( (cod_info_w.part2_3_length
					= Takehiro.count_bits( gfc, xrpow, cod_info_w, prev_noise) ) > best_part2_3_length
					&& cod_info_w.global_gain <= maxggain ) {
					cod_info_w.global_gain++;
				}

					if( cod_info_w.global_gain > maxggain ) {
						break;
					}
				}

				/* compute the distortion in this quantization */
				QuantizePVT.calc_noise( cod_info_w, l3_xmin, distort, noise_info, prev_noise );
				noise_info.bits = cod_info_w.part2_3_length;

				/* check if this quantization is better
				* than our saved quantization */
				final int quant = ( cod_info.block_type != Encoder.SHORT_TYPE ) ? cfg.quant_comp : cfg.quant_comp_short;

				final boolean better = quant_compare( quant, best_noise_info, noise_info, cod_info_w, distort );

				/* save data so we can restore this quantization later */
				if( better ) {
					best_part2_3_length = cod_info.part2_3_length;
					best_noise_info.copyFrom( noise_info );
					cod_info.copyFrom( cod_info_w );
					age = 0;
					/* save data so we can restore this quantization later */
					/*if( cfg.vbr == vbr_rh || cfg.vbr == vbr_mtrh) */  {
						/* store for later reuse */
						System.arraycopy( xrpow, 0, save_xrpow, 0, 576 );
					}
				} else {
					/* early stop? */
					if( cfg.full_outer_loop == 0 ) {
						if( ++age > search_limit && best_noise_info.over_count == 0 ) {
							break;
						}
						if( (cfg.noise_shaping_amp == 3) && bRefine && age > 30 ) {
							break;
						}
						if( (cfg.noise_shaping_amp == 3) && bRefine &&
								(cod_info_w.global_gain - best_ggain_pass1) > 15 ) {
							break;
						}
					}
				}
			}
			while( (cod_info_w.global_gain + cod_info_w.scalefac_scale) < 255 );

			if( cfg.noise_shaping_amp == 3 ) {
				if( ! bRefine ) {
					/* refine search */
					cod_info_w.copyFrom( cod_info );
					System.arraycopy( save_xrpow, 0, xrpow, 0, 576 );
					age = 0;
					best_ggain_pass1 = cod_info_w.global_gain;

					bRefine = true;
				} else {
					/* search already refined, stop */
					bEndOfSearch = true;
				}

			} else {
				bEndOfSearch = true;
			}
		}

		/*  finish up */
		if( cfg.vbr == LAME.vbr_rh || cfg.vbr == LAME.vbr_mtrh || cfg.vbr == LAME.vbr_mt ) {
			System.arraycopy( save_xrpow, 0, xrpow, 0, 576 );
		} else if( (gfc.sv_qnt.substep_shaping & 1) != 0 ) {
			trancate_smallspectrums( gfc, cod_info, l3_xmin, xrpow );
		}

		return best_noise_info.over_count;
	}

/************************************************************************
 *
 *      iteration_finish_one()
 *
 *  Robert Hegemann 2000-09-06
 *
 *  update reservoir status after FINAL quantization/bitrate
 *
 ************************************************************************/

	static final void iteration_finish_one(final InternalFlags gfc, final int gr, final int ch) {
		final SessionConfig cfg = gfc.cfg;
		final III_SideInfo l3_side = gfc.l3_side;
		final III_GrInfo cod_info = l3_side.tt[gr][ch];

		/*  try some better scalefac storage */
		Takehiro.best_scalefac_store( gfc, gr, ch, l3_side );

		/*  best huffman_divide may save some bits too
		*/
		if( cfg.use_best_huffman == 1 ) {
			Takehiro.best_huffman_divide( gfc, cod_info );
		}

		/*  update reservoir status after FINAL quantization/bitrate */
		Reservoir.ResvAdjust( gfc, cod_info );
	}

	/************************************************************************
	 *
	 *      get_framebits()
	 *
	 *  Robert Hegemann 2000-09-05
	 *
	 *  calculates
	 *  * how many bits are available for analog silent granules
	 *  * how many bits to use for the lowest allowed bitrate
	 *  * how many bits each bitrate would provide
	 *
	 ************************************************************************/
	static final void get_framebits(final InternalFlags gfc, final int frameBits[/*15*/]) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		// int     bitsPerFrame;// FIXME result never uses

		/*  always use at least this many bits per granule per channel
		 *  unless we detect analog silence, see below
		 */
		// eov.bitrate_index = cfg.vbr_min_bitrate_index;
		// bitsPerFrame = Bitstream.getframebits( gfc );

		/*  bits for analog silence */
		// eov.bitrate_index = 1;
		// bitsPerFrame = Bitstream.getframebits( gfc );

		for( int i = 1, max = cfg.vbr_max_bitrate_index; i <= max; i++ ) {
			eov.bitrate_index = i;
			frameBits[i] = (int) Reservoir.ResvFrameBegin( gfc/*, &bitsPerFrame*/ );
		}
	}
}
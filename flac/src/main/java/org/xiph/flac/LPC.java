package org.xiph.flac;

class LPC {
	/** 0.69314718055994530942 */
	private static final double M_LN2 = Math.log( 2.0 );// 0.69314718055994530942

	// private static final boolean DEBUG = false;
	// private static final boolean FLAC__OVERFLOW_DETECT = false;
	// private static final boolean FLAC__OVERFLOW_DETECT_VERBOSE = false;
	// private static final boolean FLAC__LPC_UNROLLED_FILTER_LOOPS = true;


	// bitmath.c
	/* private static int bitmath_silog2_wide(long v)
	{
		while( true ) {
			if( v == 0 ) {
				return 0;
			}
			else if( v > 0 ) {
				int l = 0;
				while( v != 0 ) {
					l++;
					v >>= 1;
				}
				return l + 1;
			}
			else if( v == -1 ) {
				return 2;
			}
			else {
				v++;
				v = -v;
			}
		}
	} */
	// end bitmath.c

	static void window_data(final int in[], final float window[], final float out[], final int data_len)
	{
		for( int i = 0; i < data_len; i++ ) {
			out[i] = in[i] * window[i];
		}
	}

	static void window_data_wide(final long in[], final float window[], final float out[], final int data_len)
	{
		for( int i = 0; i < data_len; i++ ) {
			out[i] = in[i] * window[i];
		}
	}

	static void window_data_partial(final int in[], final float window[], final float out[], final int data_len, final int part_size, final int data_shift)
	{
		int i, j;
		if( (part_size + data_shift) < data_len ) {
			for( i = 0; i < part_size; i++ ) {
				out[i] = in[data_shift+i] * window[i];
			}
			j = data_len - part_size - data_shift;
			i = (i <= j ? i : j);
			for( j = data_len - part_size; j < data_len; i++, j++ ) {
				out[i] = in[data_shift+i] * window[j];
			}
			if( i < data_len ) {
				out[i] = 0.0f;
			}
		}
	}

	static void window_data_partial_wide(final long in[], final float window[], final float out[], final int data_len, final int part_size, final int data_shift)
	{
		int i, j;
		if( (part_size + data_shift) < data_len ) {
			for( i = 0; i < part_size; i++ ) {
				out[i] = in[data_shift + i] * window[i];
			}
			j = data_len - part_size - data_shift;
			i = (i <= j ? i : j);
			for( j = data_len - part_size; j < data_len; i++, j++ ) {
				out[i] = in[data_shift+i] * window[j];
			}
			if( i < data_len ) {
				out[i] = 0.0f;
			}
		}
	}

	static void compute_autocorrelation(final float data[], final int data_len, final int lag, final double autoc[])
	{
		/* a readable, but slower, version */
/*#if 0
		double d;
		int i;
*/
		//FLAC__ASSERT(lag > 0);
		//FLAC__ASSERT(lag <= data_len);

		/*
		 * Technically we should subtract the mean first like so:
		 *   for(i = 0; i < data_len; i++)
		 *     data[i] -= mean;
		 * but it appears not to make enough of a difference to matter, and
		 * most signals are already closely centered around zero
		 */
/*		while( lag-- != 0 ) {
			for( i = lag, d = 0.0f; i < data_len; i++ )
				d += data[i] * (double)data[i - lag];
			autoc[lag] = d;
		}
#endif
*/
		/*
		 * this version tends to run faster because of better data locality
		 * ('data_len' is usually much larger than 'lag')
		 */
		int sample;
		final int limit = data_len - lag;

		//FLAC__ASSERT(lag > 0);
		//FLAC__ASSERT(lag <= data_len);

		for( int coeff = 0; coeff < lag; coeff++ ) {
			autoc[coeff] = 0.0f;
		}
		for( sample = 0; sample <= limit; sample++ ) {
			final double d = (double) data[sample];
			for( int coeff = 0; coeff < lag; coeff++ ) {
				autoc[coeff] += d * (double) data[sample + coeff];
			}
		}
		for( ; sample < data_len; sample++ ) {
			final double d = (double) data[sample];
			for( int coeff = 0; coeff < data_len - sample; coeff++ ) {
				autoc[coeff] += d * (double) data[sample + coeff];
			}
		}
	}

	/** @return max_order */
	static int compute_lp_coefficients(final double autoc[], final int max_order, final float lp_coeff[][]/*[Format.FLAC__MAX_LPC_ORDER]*/, final double error[])
	{
		final double lpc[] = new double[Format.FLAC__MAX_LPC_ORDER];

		//FLAC__ASSERT(0 != max_order);
		//FLAC__ASSERT(0 < *max_order);
		//FLAC__ASSERT(*max_order <= FLAC__MAX_LPC_ORDER);
		//FLAC__ASSERT(autoc[0] != 0.0);

		double err = autoc[0];

		for( int i = 0; i < max_order; i++ ) {
			/* Sum up this iteration's reflection coefficient. */
			double r = -autoc[i + 1];
			for( int j = 0; j < i; j++ ) {
				r -= lpc[j] * autoc[i - j];
			}
			r /= err;

			/* Update LPC coefficients and total error. */
			lpc[i] = r;
			int j = 0;
			for( ; j < (i >> 1); j++ ) {
				final double tmp = lpc[j];
				lpc[j] += r * lpc[i - 1 - j];
				lpc[i - 1 - j] += r * tmp;
			}
			if( (i & 1) != 0 ) {
				lpc[j] += lpc[j] * r;
			}

			err *= (1.0 - r * r);

			/* save this order */
			for( j = 0; j <= i; j++ ) {
				lp_coeff[i][j] = (float)(-lpc[j]); /* negate FIR filter coeff to get predictor coeff */
			}
			error[i] = err;

			/* see SF bug https://sourceforge.net/p/flac/bugs/234/ */
			if( err == 0.0 ) {
				return i + i;//max_order[0] = i + 1;
				//return;
			}
		}
		return max_order;
	}

	// java: changed shift is returned. non zero status changed to negative numbers
	static int quantize_coefficients(final float lp_coeff[], final int order, int precision, final int qlp_coeff[]/*, int[] shift*/)
	{

		//FLAC__ASSERT(precision > 0);
		//FLAC__ASSERT(precision >= FLAC__MIN_QLP_COEFF_PRECISION);

		/* drop one bit for the sign; from here on out we consider only |lp_coeff[i]| */
		precision--;
		int qmax = 1 << precision;
		final int qmin = -qmax;
		qmax--;

		/* calc cmax = max( |lp_coeff[i]| ) */
		float cmax = 0.0f;// FIXME why double?
		for( int i = 0; i < order; i++ ) {
			final float d = Math.abs( lp_coeff[i] );
			if( d > cmax ) {
				cmax = d;
			}
		}

		if( cmax <= 0.0f ) {
			/* => coefficients are all 0, which means our constant-detect didn't work */
			return -2;// return 2;
		}
		// else {
			final int max_shiftlimit = (1 << (Format.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN - 1)) - 1;
			final int min_shiftlimit = -max_shiftlimit - 1;
			int log2cmax = Math.getExponent( cmax );// TODO check java equivalent for (void)frexp(cmax, &log2cmax);
			log2cmax--;
			int shift = precision - log2cmax - 1;

			if( shift > max_shiftlimit ) {
				shift = max_shiftlimit;
			} else if( shift < min_shiftlimit ) {
				return -1;// return 1;
			}
		//}

		if( shift >= 0 ) {
			float error = 0.0f;// FIXME why double?
			int q;
			for( int i = 0; i < order; i++ ) {
				error += lp_coeff[i] * (1 << shift);
				q = Math.round( error );
/* if( FLAC__OVERFLOW_DETECT ) {
				if( q > qmax + 1 ) {
					System.err.printf("quantize_coefficients: quantizer overflow: q>qmax %d>%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmax,shift,cmax,precision+1,i,lp_coeff[i]);
				} else if(q < qmin) {
					System.err.printf("quantize_coefficients: quantizer overflow: q<qmin %d<%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmin,shift,cmax,precision+1,i,lp_coeff[i]);
				}
} */
				if( q > qmax ) {
					q = qmax;
				} else if( q < qmin ) {
					q = qmin;
				}
				error -= q;
				qlp_coeff[i] = q;
			}
		}
		/* negative shift is very rare but due to design flaw, negative shift is
		 * not allowed in the decoder, so it must be handled specially by scaling
		 * down coeffs
		 */
		else {
			final int nshift = -shift;
			float error = 0.0f;// FIXME why double?

/* if( DEBUG ) {
			System.err.printf("quantize_coefficients: negative shift=%d order=%d cmax=%f\n", shift, order, cmax);
} */
			for( int i = 0; i < order; i++ ) {
				error += lp_coeff[i] / (1 << nshift);
				int q = Math.round( error );
/* if( FLAC__OVERFLOW_DETECT ) {
				if( q > qmax + 1 ) {
					System.err.printf("quantize_coefficients: quantizer overflow: q>qmax %d>%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmax,shift,cmax,precision+1,i,lp_coeff[i]);
				} else if( q < qmin ) {
					System.err.printf("quantize_coefficients: quantizer overflow: q<qmin %d<%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmin,shift,cmax,precision+1,i,lp_coeff[i]);
				}
} */
				if( q > qmax ) {
					q = qmax;
				} else if( q < qmin ) {
					q = qmin;
				}
				error -= q;
				qlp_coeff[i] = q;
			}
			shift = 0;
		}

		return shift;//return 0;
	}

	static void compute_residual_from_qlp_coefficients(final int data[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int residual[])
	{// java: uses order as offset to data
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;// offset to residual
		int idata = order;// offset to data[]

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("compute_residual_from_qlp_coefficients: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sumo = 0;
			int sum = 0;
			int history = idata;// offset to data;
			for( int j = 0; j < order; j++ ) {
				sum += qlp_coeff[j] * data[--history];
				sumo += (long)qlp_coeff[j] * (long)data[history];
				if( sumo > 2147483647L || sumo < -2147483648L ) {
					System.err.printf("compute_residual_from_qlp_coefficients: OVERFLOW, i=%d, j=%d, c=%d, d=%d, sumo=%d\n",i,j,qlp_coeff[j],data[history],sumo);
				}
			}
			residual[r++] = data[idata++] - (sum >> lp_quantization);
		}

		// Here's a slower but clearer version:
		// for(i = 0; i < data_len; i++) {
		//	sum = 0;
		//	for(j = 0; j < order; j++)
		//		sum += qlp_coeff[j] * data[i-j-1];
		//	residual[i] = data[i] - (sum >> lp_quantization);
		//}
	}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[11] * data[order-12];
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							residual[i] = data[order] - ((qlp_coeff[0] * data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			for( int i = 0, di = order; i < data_len; i++, di++ ) {
				int sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * data[di-32];/* Falls through. */
					case 31: sum += qlp_coeff[30] * data[di-31];/* Falls through. */
					case 30: sum += qlp_coeff[29] * data[di-30];/* Falls through. */
					case 29: sum += qlp_coeff[28] * data[di-29];/* Falls through. */
					case 28: sum += qlp_coeff[27] * data[di-28];/* Falls through. */
					case 27: sum += qlp_coeff[26] * data[di-27];/* Falls through. */
					case 26: sum += qlp_coeff[25] * data[di-26];/* Falls through. */
					case 25: sum += qlp_coeff[24] * data[di-25];/* Falls through. */
					case 24: sum += qlp_coeff[23] * data[di-24];/* Falls through. */
					case 23: sum += qlp_coeff[22] * data[di-23];/* Falls through. */
					case 22: sum += qlp_coeff[21] * data[di-22];/* Falls through. */
					case 21: sum += qlp_coeff[20] * data[di-21];/* Falls through. */
					case 20: sum += qlp_coeff[19] * data[di-20];/* Falls through. */
					case 19: sum += qlp_coeff[18] * data[di-19];/* Falls through. */
					case 18: sum += qlp_coeff[17] * data[di-18];/* Falls through. */
					case 17: sum += qlp_coeff[16] * data[di-17];/* Falls through. */
					case 16: sum += qlp_coeff[15] * data[di-16];/* Falls through. */
					case 15: sum += qlp_coeff[14] * data[di-15];/* Falls through. */
					case 14: sum += qlp_coeff[13] * data[di-14];/* Falls through. */
					case 13: sum += qlp_coeff[12] * data[di-13];
					         sum += qlp_coeff[11] * data[di-12];
					         sum += qlp_coeff[10] * data[di-11];
					         sum += qlp_coeff[ 9] * data[di-10];
					         sum += qlp_coeff[ 8] * data[di- 9];
					         sum += qlp_coeff[ 7] * data[di- 8];
					         sum += qlp_coeff[ 6] * data[di- 7];
					         sum += qlp_coeff[ 5] * data[di- 6];
					         sum += qlp_coeff[ 4] * data[di- 5];
					         sum += qlp_coeff[ 3] * data[di- 4];
					         sum += qlp_coeff[ 2] * data[di- 3];
					         sum += qlp_coeff[ 1] * data[di- 2];
					         sum += qlp_coeff[ 0] * data[di- 1];
				}
				residual[i] = data[di] - (sum >> lp_quantization);
			}
		}
}
	}

	static void compute_residual_from_qlp_coefficients_wide(final int[] data, final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int residual[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;
		int idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("compute_residual_from_qlp_coefficients_wide: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%u]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sum = 0;
			int history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += (long)qlp_coeff[j] * (long)data[--history];
			}
			if(FLAC__bitmath_silog2( (long)data[idata] - (sum >> lp_quantization)) > 32 ) {
				System.err.printf("compute_residual_from_qlp_coefficients_wide: OVERFLOW, i=%u, data=%d, sum=%d, residual=%d\n", i, data[idata], (long)(sum >> lp_quantization), ((long)data[idata] - (sum >> lp_quantization)));
				break;
			}
			residual[r++] = data[idata++] - (int)(sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[11] * (long)data[order-12];
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++) {
							long sum = 0;
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							residual[i] = data[order] - (int)((qlp_coeff[0] * (long)data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			for( int i = 0, di = order; i < data_len; i++, di++ ) {
				long sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * (long)data[di-32];/* Falls through. */
					case 31: sum += qlp_coeff[30] * (long)data[di-31];/* Falls through. */
					case 30: sum += qlp_coeff[29] * (long)data[di-30];/* Falls through. */
					case 29: sum += qlp_coeff[28] * (long)data[di-29];/* Falls through. */
					case 28: sum += qlp_coeff[27] * (long)data[di-28];/* Falls through. */
					case 27: sum += qlp_coeff[26] * (long)data[di-27];/* Falls through. */
					case 26: sum += qlp_coeff[25] * (long)data[di-26];/* Falls through. */
					case 25: sum += qlp_coeff[24] * (long)data[di-25];/* Falls through. */
					case 24: sum += qlp_coeff[23] * (long)data[di-24];/* Falls through. */
					case 23: sum += qlp_coeff[22] * (long)data[di-23];/* Falls through. */
					case 22: sum += qlp_coeff[21] * (long)data[di-22];/* Falls through. */
					case 21: sum += qlp_coeff[20] * (long)data[di-21];/* Falls through. */
					case 20: sum += qlp_coeff[19] * (long)data[di-20];/* Falls through. */
					case 19: sum += qlp_coeff[18] * (long)data[di-19];/* Falls through. */
					case 18: sum += qlp_coeff[17] * (long)data[di-18];/* Falls through. */
					case 17: sum += qlp_coeff[16] * (long)data[di-17];/* Falls through. */
					case 16: sum += qlp_coeff[15] * (long)data[di-16];/* Falls through. */
					case 15: sum += qlp_coeff[14] * (long)data[di-15];/* Falls through. */
					case 14: sum += qlp_coeff[13] * (long)data[di-14];/* Falls through. */
					case 13: sum += qlp_coeff[12] * (long)data[di-13];
					         sum += qlp_coeff[11] * (long)data[di-12];
					         sum += qlp_coeff[10] * (long)data[di-11];
					         sum += qlp_coeff[ 9] * (long)data[di-10];
					         sum += qlp_coeff[ 8] * (long)data[di- 9];
					         sum += qlp_coeff[ 7] * (long)data[di- 8];
					         sum += qlp_coeff[ 6] * (long)data[di- 7];
					         sum += qlp_coeff[ 5] * (long)data[di- 6];
					         sum += qlp_coeff[ 4] * (long)data[di- 5];
					         sum += qlp_coeff[ 3] * (long)data[di- 4];
					         sum += qlp_coeff[ 2] * (long)data[di- 3];
					         sum += qlp_coeff[ 1] * (long)data[di- 2];
					         sum += qlp_coeff[ 0] * (long)data[di- 1];
				}
				residual[i] = data[di] - (int)(sum >> lp_quantization);
			}
		}
}
	}
//#endif

	static boolean compute_residual_from_qlp_coefficients_limit_residual(final int[] data, final int data_len, final int[] qlp_coeff, final int order, final int lp_quantization, final int[] residual)
	{
		long sum, residual_to_check;

		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		for( int i = 0, di = order; i < data_len; i++, di++ ) {
			sum = 0;
			switch( order ) {
				case 32: sum += qlp_coeff[31] * (long)data[di-32]; /* Falls through. */
				case 31: sum += qlp_coeff[30] * (long)data[di-31]; /* Falls through. */
				case 30: sum += qlp_coeff[29] * (long)data[di-30]; /* Falls through. */
				case 29: sum += qlp_coeff[28] * (long)data[di-29]; /* Falls through. */
				case 28: sum += qlp_coeff[27] * (long)data[di-28]; /* Falls through. */
				case 27: sum += qlp_coeff[26] * (long)data[di-27]; /* Falls through. */
				case 26: sum += qlp_coeff[25] * (long)data[di-26]; /* Falls through. */
				case 25: sum += qlp_coeff[24] * (long)data[di-25]; /* Falls through. */
				case 24: sum += qlp_coeff[23] * (long)data[di-24]; /* Falls through. */
				case 23: sum += qlp_coeff[22] * (long)data[di-23]; /* Falls through. */
				case 22: sum += qlp_coeff[21] * (long)data[di-22]; /* Falls through. */
				case 21: sum += qlp_coeff[20] * (long)data[di-21]; /* Falls through. */
				case 20: sum += qlp_coeff[19] * (long)data[di-20]; /* Falls through. */
				case 19: sum += qlp_coeff[18] * (long)data[di-19]; /* Falls through. */
				case 18: sum += qlp_coeff[17] * (long)data[di-18]; /* Falls through. */
				case 17: sum += qlp_coeff[16] * (long)data[di-17]; /* Falls through. */
				case 16: sum += qlp_coeff[15] * (long)data[di-16]; /* Falls through. */
				case 15: sum += qlp_coeff[14] * (long)data[di-15]; /* Falls through. */
				case 14: sum += qlp_coeff[13] * (long)data[di-14]; /* Falls through. */
				case 13: sum += qlp_coeff[12] * (long)data[di-13]; /* Falls through. */
				case 12: sum += qlp_coeff[11] * (long)data[di-12]; /* Falls through. */
				case 11: sum += qlp_coeff[10] * (long)data[di-11]; /* Falls through. */
				case 10: sum += qlp_coeff[ 9] * (long)data[di-10]; /* Falls through. */
				case  9: sum += qlp_coeff[ 8] * (long)data[di- 9]; /* Falls through. */
				case  8: sum += qlp_coeff[ 7] * (long)data[di- 8]; /* Falls through. */
				case  7: sum += qlp_coeff[ 6] * (long)data[di- 7]; /* Falls through. */
				case  6: sum += qlp_coeff[ 5] * (long)data[di- 6]; /* Falls through. */
				case  5: sum += qlp_coeff[ 4] * (long)data[di- 5]; /* Falls through. */
				case  4: sum += qlp_coeff[ 3] * (long)data[di- 4]; /* Falls through. */
				case  3: sum += qlp_coeff[ 2] * (long)data[di- 3]; /* Falls through. */
				case  2: sum += qlp_coeff[ 1] * (long)data[di- 2]; /* Falls through. */
				case  1: sum += qlp_coeff[ 0] * (long)data[di- 1];
			}
			residual_to_check = data[di] - (sum >> lp_quantization);
			 /* residual must not be INT32_MIN because abs(INT32_MIN) is undefined */
			if( residual_to_check <= Integer.MIN_VALUE || residual_to_check > Integer.MAX_VALUE ) {
				return false;
			} else {
				residual[i] = (int)residual_to_check;
			}
		}
		return true;
	}

	static boolean compute_residual_from_qlp_coefficients_limit_residual_33bit(final long[] data, final int data_len, final int[] qlp_coeff, final int order, final int lp_quantization, final int[] residual)
	{
		long sum, residual_to_check;

		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		for( int i = 0, di = order; i < data_len; i++, di++ ) {
			sum = 0;
			switch( order ) {
				case 32: sum += qlp_coeff[31] * data[di-32]; /* Falls through. */
				case 31: sum += qlp_coeff[30] * data[di-31]; /* Falls through. */
				case 30: sum += qlp_coeff[29] * data[di-30]; /* Falls through. */
				case 29: sum += qlp_coeff[28] * data[di-29]; /* Falls through. */
				case 28: sum += qlp_coeff[27] * data[di-28]; /* Falls through. */
				case 27: sum += qlp_coeff[26] * data[di-27]; /* Falls through. */
				case 26: sum += qlp_coeff[25] * data[di-26]; /* Falls through. */
				case 25: sum += qlp_coeff[24] * data[di-25]; /* Falls through. */
				case 24: sum += qlp_coeff[23] * data[di-24]; /* Falls through. */
				case 23: sum += qlp_coeff[22] * data[di-23]; /* Falls through. */
				case 22: sum += qlp_coeff[21] * data[di-22]; /* Falls through. */
				case 21: sum += qlp_coeff[20] * data[di-21]; /* Falls through. */
				case 20: sum += qlp_coeff[19] * data[di-20]; /* Falls through. */
				case 19: sum += qlp_coeff[18] * data[di-19]; /* Falls through. */
				case 18: sum += qlp_coeff[17] * data[di-18]; /* Falls through. */
				case 17: sum += qlp_coeff[16] * data[di-17]; /* Falls through. */
				case 16: sum += qlp_coeff[15] * data[di-16]; /* Falls through. */
				case 15: sum += qlp_coeff[14] * data[di-15]; /* Falls through. */
				case 14: sum += qlp_coeff[13] * data[di-14]; /* Falls through. */
				case 13: sum += qlp_coeff[12] * data[di-13]; /* Falls through. */
				case 12: sum += qlp_coeff[11] * data[di-12]; /* Falls through. */
				case 11: sum += qlp_coeff[10] * data[di-11]; /* Falls through. */
				case 10: sum += qlp_coeff[ 9] * data[di-10]; /* Falls through. */
				case  9: sum += qlp_coeff[ 8] * data[di- 9]; /* Falls through. */
				case  8: sum += qlp_coeff[ 7] * data[di- 8]; /* Falls through. */
				case  7: sum += qlp_coeff[ 6] * data[di- 7]; /* Falls through. */
				case  6: sum += qlp_coeff[ 5] * data[di- 6]; /* Falls through. */
				case  5: sum += qlp_coeff[ 4] * data[di- 5]; /* Falls through. */
				case  4: sum += qlp_coeff[ 3] * data[di- 4]; /* Falls through. */
				case  3: sum += qlp_coeff[ 2] * data[di- 3]; /* Falls through. */
				case  2: sum += qlp_coeff[ 1] * data[di- 2]; /* Falls through. */
				case  1: sum += qlp_coeff[ 0] * data[di- 1];
			}
			residual_to_check = data[di] - (sum >> lp_quantization);
			/* residual must not be INT32_MIN because abs(INT32_MIN) is undefined */
			if( residual_to_check <= Integer.MIN_VALUE || residual_to_check > Integer.MAX_VALUE ) {
				return false;
			} else {
				residual[i] = (int)residual_to_check;
			}
		}
		return true;
	}

//#endif /* !defined FLAC__INTEGER_ONLY_LIBRARY */

	static int max_prediction_before_shift_bps(final int subframe_bps, final int[] qlp_coeff, final int order)
	{
		/* This used to be subframe_bps + qlp_coeff_precision + FLAC__bitmath_ilog2(order)
		 * but that treats both the samples as well as the predictor as unknown. The
		 * predictor is known however, so taking the log2 of the sum of the absolute values
		 * of all coefficients is a more accurate representation of the predictor */
		int abs_sum_of_qlp_coeff = 0;
		for( int i = 0; i < order; i++ ) {
			abs_sum_of_qlp_coeff += Math.abs( qlp_coeff[i] );
		}
		if( abs_sum_of_qlp_coeff == 0 ) {
			abs_sum_of_qlp_coeff = 1;
		}
		return subframe_bps + Format.bitmath_silog2( abs_sum_of_qlp_coeff );
	}

	static int max_residual_bps(final int subframe_bps, final int[] qlp_coeff, final int order, final int lp_quantization)
	{
		final int predictor_sum_bps = max_prediction_before_shift_bps( subframe_bps, qlp_coeff, order ) - lp_quantization;
		if( subframe_bps > predictor_sum_bps ) {
			return subframe_bps + 1;
		} else {
			return predictor_sum_bps + 1;
		}
	}

//#ifdef FUZZING_BUILD_MODE_NO_SANITIZE_SIGNED_INTEGER_OVERFLOW
/* The attribute below is to silence the undefined sanitizer of oss-fuzz.
 * Because fuzzing feeds bogus predictors and residual samples to the
 * decoder, having overflows in this section is unavoidable. Also,
 * because the calculated values are audio path only, there is no
 * potential for security problems */
//__attribute__((no_sanitize("signed-integer-overflow")))
//#endif

	/**
	 * local_lpc_restore_signal<br>
	 * local_lpc_restore_signal_16bit<br>
	 * local_lpc_restore_signal_16bit_order8<br>
	 */
	static void restore_signal(final int residual[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int data[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;
		int idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("restore_signal: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sumo = 0;
			int sum = 0;
			int history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += qlp_coeff[j] * data[--history];
				sumo += (long)qlp_coeff[j] * (long)(data[history]);
#ifdef FLAC__OVERFLOW_DETECT
				if( sumo > 2147483647L || sumo < -2147483648L ) {
					System.err.printf("restore_signal: OVERFLOW, i=%d, j=%d, c=%d, d=%d, sumo=%d\n", i, j, qlp_coeff[j], data[history], sumo );
				}
#endif
			}
			data[idata++] = residual[r++] + (sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[11] * data[order-12];
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int  i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++) {
							int sum = 0;
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							data[order] = residual[i] + ((qlp_coeff[0] * data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			for( int i = 0, di = order; i < data_len; i++, di++ ) {
				int sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * data[di-32];/* Falls through. */
					case 31: sum += qlp_coeff[30] * data[di-31];/* Falls through. */
					case 30: sum += qlp_coeff[29] * data[di-30];/* Falls through. */
					case 29: sum += qlp_coeff[28] * data[di-29];/* Falls through. */
					case 28: sum += qlp_coeff[27] * data[di-28];/* Falls through. */
					case 27: sum += qlp_coeff[26] * data[di-27];/* Falls through. */
					case 26: sum += qlp_coeff[25] * data[di-26];/* Falls through. */
					case 25: sum += qlp_coeff[24] * data[di-25];/* Falls through. */
					case 24: sum += qlp_coeff[23] * data[di-24];/* Falls through. */
					case 23: sum += qlp_coeff[22] * data[di-23];/* Falls through. */
					case 22: sum += qlp_coeff[21] * data[di-22];/* Falls through. */
					case 21: sum += qlp_coeff[20] * data[di-21];/* Falls through. */
					case 20: sum += qlp_coeff[19] * data[di-20];/* Falls through. */
					case 19: sum += qlp_coeff[18] * data[di-19];/* Falls through. */
					case 18: sum += qlp_coeff[17] * data[di-18];/* Falls through. */
					case 17: sum += qlp_coeff[16] * data[di-17];/* Falls through. */
					case 16: sum += qlp_coeff[15] * data[di-16];/* Falls through. */
					case 15: sum += qlp_coeff[14] * data[di-15];/* Falls through. */
					case 14: sum += qlp_coeff[13] * data[di-14];/* Falls through. */
					case 13: sum += qlp_coeff[12] * data[di-13];
					         sum += qlp_coeff[11] * data[di-12];
					         sum += qlp_coeff[10] * data[di-11];
					         sum += qlp_coeff[ 9] * data[di-10];
					         sum += qlp_coeff[ 8] * data[di- 9];
					         sum += qlp_coeff[ 7] * data[di- 8];
					         sum += qlp_coeff[ 6] * data[di- 7];
					         sum += qlp_coeff[ 5] * data[di- 6];
					         sum += qlp_coeff[ 4] * data[di- 5];
					         sum += qlp_coeff[ 3] * data[di- 4];
					         sum += qlp_coeff[ 2] * data[di- 3];
					         sum += qlp_coeff[ 1] * data[di- 2];
					         sum += qlp_coeff[ 0] * data[di- 1];
				}
				data[di] = residual[i] + (sum >> lp_quantization);
			}
		}
}
	}

	/**
	 * local_lpc_restore_signal_64bit
	 */
	static void restore_signal_wide(final int residual[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int data[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {

		int r = 0, history, idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("restore_signal_wide: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sum = 0;
			history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += (long)qlp_coeff[j] * (long)data[--history];
			}
#ifdef FLAC__OVERFLOW_DETECT
			if( FLAC__bitmath_silog2((long)residual[r] + (sum >> lp_quantization)) > 32) {
				System.err.printf("restore_signal_wide: OVERFLOW, i=%u, residual=%d, sum=%d, data=%d\n", i, residual[r], (sum >> lp_quantization), ((long)residual[r] + (sum >> lp_quantization)));
				break;
			}
#endif
			data[idata++] = residual[r++] + (int)(sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
{

		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[11] * (long)data[order-12];
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = (int)(residual[i] + (sum >> lp_quantization));
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							data[order] = (int)(residual[i] + ((qlp_coeff[0] * (long)data[order-1]) >> lp_quantization));
						}
					}
				}
			}
		}
		else { /* order > 12 */
			for( int i = 0, di = order; i < data_len; i++, di++ ) {
				long sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * (long)data[di-32];/* Falls through. */
					case 31: sum += qlp_coeff[30] * (long)data[di-31];/* Falls through. */
					case 30: sum += qlp_coeff[29] * (long)data[di-30];/* Falls through. */
					case 29: sum += qlp_coeff[28] * (long)data[di-29];/* Falls through. */
					case 28: sum += qlp_coeff[27] * (long)data[di-28];/* Falls through. */
					case 27: sum += qlp_coeff[26] * (long)data[di-27];/* Falls through. */
					case 26: sum += qlp_coeff[25] * (long)data[di-26];/* Falls through. */
					case 25: sum += qlp_coeff[24] * (long)data[di-25];/* Falls through. */
					case 24: sum += qlp_coeff[23] * (long)data[di-24];/* Falls through. */
					case 23: sum += qlp_coeff[22] * (long)data[di-23];/* Falls through. */
					case 22: sum += qlp_coeff[21] * (long)data[di-22];/* Falls through. */
					case 21: sum += qlp_coeff[20] * (long)data[di-21];/* Falls through. */
					case 20: sum += qlp_coeff[19] * (long)data[di-20];/* Falls through. */
					case 19: sum += qlp_coeff[18] * (long)data[di-19];/* Falls through. */
					case 18: sum += qlp_coeff[17] * (long)data[di-18];/* Falls through. */
					case 17: sum += qlp_coeff[16] * (long)data[di-17];/* Falls through. */
					case 16: sum += qlp_coeff[15] * (long)data[di-16];/* Falls through. */
					case 15: sum += qlp_coeff[14] * (long)data[di-15];/* Falls through. */
					case 14: sum += qlp_coeff[13] * (long)data[di-14];/* Falls through. */
					case 13: sum += qlp_coeff[12] * (long)data[di-13];
					         sum += qlp_coeff[11] * (long)data[di-12];
					         sum += qlp_coeff[10] * (long)data[di-11];
					         sum += qlp_coeff[ 9] * (long)data[di-10];
					         sum += qlp_coeff[ 8] * (long)data[di- 9];
					         sum += qlp_coeff[ 7] * (long)data[di- 8];
					         sum += qlp_coeff[ 6] * (long)data[di- 7];
					         sum += qlp_coeff[ 5] * (long)data[di- 6];
					         sum += qlp_coeff[ 4] * (long)data[di- 5];
					         sum += qlp_coeff[ 3] * (long)data[di- 4];
					         sum += qlp_coeff[ 2] * (long)data[di- 3];
					         sum += qlp_coeff[ 1] * (long)data[di- 2];
					         sum += qlp_coeff[ 0] * (long)data[di- 1];
				}
				data[di] = (int)(residual[i] + (sum >> lp_quantization));
			}
		}
}
//#endif
	}

//#ifdef FUZZING_BUILD_MODE_NO_SANITIZE_SIGNED_INTEGER_OVERFLOW
/* The attribute below is to silence the undefined sanitizer of oss-fuzz.
 * Because fuzzing feeds bogus predictors and residual samples to the
 * decoder, having overflows in this section is unavoidable. Also,
 * because the calculated values are audio path only, there is no
 * potential for security problems */
//__attribute__((no_sanitize("signed-integer-overflow")))
//#endif
	static void restore_signal_wide_33bit(final int[] residual, final int data_len, final int[] qlp_coeff, final int order, final int lp_quantization, final long[] data)
/*#if defined(FLAC__OVERFLOW_DETECT) || !defined(FLAC__LPC_UNROLLED_FILTER_LOOPS)
	{
		int r = 0, history, idata = order;
		long sum;

		FLAC__ASSERT(order > 0);

		for( iny i = 0; i < data_len; i++ ) {
			sum = 0;
			history = idata;
			for( int j = 0; j < order; j++ )
				sum += (long)qlp_coeff[j] * (long)(data[ --history ]);
#ifdef FLAC__OVERFLOW_DETECT
			if( FLAC__bitmath_silog2((long)residual[ r ] + (sum >> lp_quantization)) > 33 ) {
				System.err.printf("restore_signal_33bit: OVERFLOW, i=%u, residual=%d, sum=%d, data=%d\n", i, residual[ r ], (sum >> lp_quantization), ((long)residual[ r ] + (sum >> lp_quantization)));
				break;
			}
#endif
			data[ idata++ ] = (long)residual[ r++ ] + (sum >> lp_quantization);
		}
	}
#else *//* unrolled version for normal use */
	{
		long sum;

		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		for( int i = 0, di = order; i < data_len; i++, di++ ) {
			sum = 0;
			switch( order ) {
				case 32: sum += qlp_coeff[31] * data[di-32]; /* Falls through. */
				case 31: sum += qlp_coeff[30] * data[di-31]; /* Falls through. */
				case 30: sum += qlp_coeff[29] * data[di-30]; /* Falls through. */
				case 29: sum += qlp_coeff[28] * data[di-29]; /* Falls through. */
				case 28: sum += qlp_coeff[27] * data[di-28]; /* Falls through. */
				case 27: sum += qlp_coeff[26] * data[di-27]; /* Falls through. */
				case 26: sum += qlp_coeff[25] * data[di-26]; /* Falls through. */
				case 25: sum += qlp_coeff[24] * data[di-25]; /* Falls through. */
				case 24: sum += qlp_coeff[23] * data[di-24]; /* Falls through. */
				case 23: sum += qlp_coeff[22] * data[di-23]; /* Falls through. */
				case 22: sum += qlp_coeff[21] * data[di-22]; /* Falls through. */
				case 21: sum += qlp_coeff[20] * data[di-21]; /* Falls through. */
				case 20: sum += qlp_coeff[19] * data[di-20]; /* Falls through. */
				case 19: sum += qlp_coeff[18] * data[di-19]; /* Falls through. */
				case 18: sum += qlp_coeff[17] * data[di-18]; /* Falls through. */
				case 17: sum += qlp_coeff[16] * data[di-17]; /* Falls through. */
				case 16: sum += qlp_coeff[15] * data[di-16]; /* Falls through. */
				case 15: sum += qlp_coeff[14] * data[di-15]; /* Falls through. */
				case 14: sum += qlp_coeff[13] * data[di-14]; /* Falls through. */
				case 13: sum += qlp_coeff[12] * data[di-13]; /* Falls through. */
				case 12: sum += qlp_coeff[11] * data[di-12]; /* Falls through. */
				case 11: sum += qlp_coeff[10] * data[di-11]; /* Falls through. */
				case 10: sum += qlp_coeff[ 9] * data[di-10]; /* Falls through. */
				case  9: sum += qlp_coeff[ 8] * data[di- 9]; /* Falls through. */
				case  8: sum += qlp_coeff[ 7] * data[di- 8]; /* Falls through. */
				case  7: sum += qlp_coeff[ 6] * data[di- 7]; /* Falls through. */
				case  6: sum += qlp_coeff[ 5] * data[di- 6]; /* Falls through. */
				case  5: sum += qlp_coeff[ 4] * data[di- 5]; /* Falls through. */
				case  4: sum += qlp_coeff[ 3] * data[di- 4]; /* Falls through. */
				case  3: sum += qlp_coeff[ 2] * data[di- 3]; /* Falls through. */
				case  2: sum += qlp_coeff[ 1] * data[di- 2]; /* Falls through. */
				case  1: sum += qlp_coeff[ 0] * data[di- 1];
			}
			data[di] = residual[i] + (sum >> lp_quantization);
		}
	}
//#endif

	static double compute_expected_bits_per_residual_sample(final double lpc_error, final int total_samples)
	{
		//FLAC__ASSERT(total_samples > 0);

		final double error_scale = 0.5 / (double)total_samples;

		return compute_expected_bits_per_residual_sample_with_error_scale( lpc_error, error_scale );
	}

	private static double compute_expected_bits_per_residual_sample_with_error_scale(final double lpc_error, final double error_scale)
	{
		if( lpc_error > 0.0 ) {
			final double bps = 0.5 * Math.log( error_scale * lpc_error ) / M_LN2;
			if( bps >= 0.0 ) {
				return bps;
			}// else {
				return 0.0;
			//}
		}
		else if( lpc_error < 0.0 ) { /* error should not be negative but can happen due to inadequate floating-point resolution */
			return 1e32;
		}
		//else {
			return 0.0;
		//}
	}

	static int compute_best_order(final double lpc_error[], final int max_order, final int total_samples, final int overhead_bits_per_order)
	{
		//FLAC__ASSERT(max_order > 0);
		//FLAC__ASSERT(total_samples > 0);

		final double error_scale = 0.5 / (double)total_samples;

		int best_index = 0;/* 'index' the index into lpc_error; index==order-1 since lpc_error[0] is for order==1, lpc_error[1] is for order==2, etc */
		double best_bits = Integer.MAX_VALUE;

		for( int indx = 0, order = 1; indx < max_order; indx++, order++ ) {
			final double bits = compute_expected_bits_per_residual_sample_with_error_scale( lpc_error[indx], error_scale ) * (double)(total_samples - order) + (double)(order * overhead_bits_per_order);
			if( bits < best_bits ) {
				best_index = indx;
				best_bits = bits;
			}
		}

		return best_index + 1; /* +1 since index of lpc_error[] is order-1 */
	}
}

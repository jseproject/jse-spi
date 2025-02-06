package net.sourceforge.lame;

// vbrquantize.c

class VBRQuantize {
/*
	struct algo_s;
	typedef struct algo_s algo_t;

	typedef void (*alloc_sf_f) (const algo_t *, const int *, const int *, int );
	typedef uint8_t (*find_sf_f) (const FLOAT *, const FLOAT *, FLOAT, unsigned int, uint8_t );
*/
	private static final int short_block_constrain = 1;// alloc
	private static final int long_block_constrain = 2;// alloc
	private static final int guess_scalefac_x34 = 1;// find
	private static final int find_scalefac_x34 = 2;// find

	private static final class ALGO_s {
		private int /* alloc_sf_f */ alloc;// java: using int instead function pointer
		private int /* find_sf_f */ find;// java: using int instead function pointer
		private float[] xr34orig;
		private InternalFlags gfc;
		private III_GrInfo cod_info;
		private int mingain_l;
		private final int mingain_s[] = new int[3];
	};

/*  Remarks on optimizing compilers:
 *
 *  the MSVC compiler may get into aliasing problems when accessing
 *  memory through the fi_union. declaring it volatile does the trick here
 *
 *  the calc_sfb_noise_* functions are not inlined because the intel compiler
 *  optimized executeables won't work as expected anymore
 */
/*
	typedef VOLATILE union {
	float   f;
	int     i;
	} fi_union;
*/
/*
#ifdef TAKEHIRO_IEEE754_HACK
#define DOUBLEX double
#else
#define DOUBLEX FLOAT
#endif
*/
	//private static final int MAGIC_FLOAT_def = (65536*(128));
	//private static final int MAGIC_INT_def   = 0x4b000000;

//#ifdef TAKEHIRO_IEEE754_HACK
//#else
/*********************************************************************
 * XRPOW_FTOI is a macro to convert floats to ints.
 * if XRPOW_FTOI(x) = nearest_int(x), then QUANTFAC(x)=adj43asm[x]
 *                                         ROUNDFAC= -0.0946
 *
 * if XRPOW_FTOI(x) = floor(x), then QUANTFAC(x)=asj43[x]
 *                                   ROUNDFAC=0.4054
 *********************************************************************/
/*	#  define QUANTFAC(rx)  adj43[rx]
	#  define ROUNDFAC_def 0.4054f
	#  define XRPOW_FTOI(src,dest) ((dest) = (int)(src))
#endif
*/
	//private static final int MAGIC_INT = MAGIC_INT_def;
/* #ifndef TAKEHIRO_IEEE754_HACK
	private static final double ROUNDFAC = ROUNDFAC_def;
#endif */
	//private static final double MAGIC_FLOAT = MAGIC_FLOAT_def;

	private static final float vec_max_c(final float[] xr34, int xoffset, final int bw ) {
		float xfsf = 0;
		int i = bw >>> 2;
		final int remaining = (bw & 0x03);

		while( i-- > 0 ) {
			if( xfsf < xr34[xoffset] ) {
				xfsf = xr34[xoffset];
			}
			xoffset++;
			if( xfsf < xr34[xoffset] ) {
				xfsf = xr34[xoffset];
			}
			xoffset++;
			if( xfsf < xr34[xoffset] ) {
				xfsf = xr34[xoffset];
			}
			xoffset++;
			if( xfsf < xr34[xoffset] ) {
				xfsf = xr34[xoffset];
			}
			xoffset++;
		}
		switch( remaining ) {
		case 3: if( xfsf < xr34[xoffset + 2] ) {
			xfsf = xr34[xoffset + 2];
		}
		case 2: if( xfsf < xr34[xoffset + 1] ) {
			xfsf = xr34[xoffset + 1];
		}
		case 1: if( xfsf < xr34[xoffset + 0] ) {
			xfsf = xr34[xoffset + 0];
		}
		default: break;
		}
		return xfsf;
	}

	private static final int find_lowest_scalefac(final float xr34) {
		int sf_ok = 255;
		int sf = 128, delsf = 64;
		final float ixmax_val = QuantizePVT.IXMAX_VAL;
		for( int i = 0; i < 8; ++i ) {
			final float xfsf = QuantizePVT.ipow20[sf] * xr34;
			if( xfsf <= ixmax_val ) {
				sf_ok = sf;
				sf -= delsf;
			} else {
				sf += delsf;
			}
			delsf >>= 1;
		}
		return sf_ok;
	}

	private static final void k_34_4(final float x[]/*[4]*/, final int l3[]/*[4]*/, int l3offset) {
/* if( QuantizePVT.TAKEHIRO_IEEE754_HACK ) {
		x[0] += MAGIC_FLOAT;
		float fi0 = x[0];
		x[1] += MAGIC_FLOAT;
		float fi1 = x[1];
		x[2] += MAGIC_FLOAT;
		float fi2 = x[2];
		x[3] += MAGIC_FLOAT;
		float fi3 = x[3];
		fi0 = x[0] + QuantizePVT.adj43[ Float.floatToIntBits( fi0 ) - MAGIC_INT ];
		fi1 = x[1] + QuantizePVT.adj43[ Float.floatToIntBits( fi1 ) - MAGIC_INT ];
		fi2 = x[2] + QuantizePVT.adj43[ Float.floatToIntBits( fi2 ) - MAGIC_INT ];
		fi3 = x[3] + QuantizePVT.adj43[ Float.floatToIntBits( fi3 ) - MAGIC_INT ];
		l3[l3offset++] = Float.floatToRawIntBits( fi0 ) - MAGIC_INT;
		l3[l3offset++] = Float.floatToRawIntBits( fi1 ) - MAGIC_INT;
		l3[l3offset++] = Float.floatToRawIntBits( fi2 ) - MAGIC_INT;
		l3[l3offset  ] = Float.floatToRawIntBits( fi3 ) - MAGIC_INT;
} else { */
		l3[  l3offset] = (int)x[0];
		l3[++l3offset] = (int)x[1];
		l3[++l3offset] = (int)x[2];
		l3[++l3offset] = (int)x[3];
		x[3] += QuantizePVT.adj43[ l3[  l3offset] ];
		x[2] += QuantizePVT.adj43[ l3[--l3offset] ];
		x[1] += QuantizePVT.adj43[ l3[--l3offset] ];
		x[0] += QuantizePVT.adj43[ l3[--l3offset] ];
		l3[  l3offset] = (int)x[0];
		l3[++l3offset] = (int)x[1];
		l3[++l3offset] = (int)x[2];
		l3[++l3offset] = (int)x[3];
//}
	}

	/*  do call the calc_sfb_noise_* functions only with sf values
	 *  for which holds: sfpow34*xr34 <= IXMAX_VAL
	 */
	private static final float calc_sfb_noise_x34(final float[] xr, int xroffset, final float[] xr34, int xr34offset, final int bw, final int sf) {
		final float x[] = new float[4];
		final int l3[] = new int[4];
		final float sfpow = QuantizePVT.pow20[sf + QuantizePVT.Q_MAX2]; /*pow(2.0,sf/4.0 ); */
		final float sfpow34 = QuantizePVT.ipow20[sf]; /*pow(sfpow,-3.0/4.0 ); */

		float xfsf = 0;
		int i = bw >>> 2;
		final int remaining = (bw & 0x03);

		while( i-- > 0 ) {
			x[0] = sfpow34 * xr34[xr34offset++];
			x[1] = sfpow34 * xr34[xr34offset++];
			x[2] = sfpow34 * xr34[xr34offset++];
			x[3] = sfpow34 * xr34[xr34offset++];

			k_34_4( x, l3, 0 );

			float v = xr[xroffset++];
			if( v < 0 ) {
				v = -v;
			}
			final float x0 = v - sfpow * QuantizePVT.pow43[ l3[0] ];
			v = xr[xroffset++];
			if( v < 0 ) {
				v = -v;
			}
			final float x1 = v - sfpow * QuantizePVT.pow43[ l3[1] ];
			v = xr[xroffset++];
			if( v < 0 ) {
				v = -v;
			}
			final float x2 = v - sfpow * QuantizePVT.pow43[ l3[2] ];
			v = xr[xroffset++];
			if( v < 0 ) {
				v = -v;
			}
			final float x3 = v - sfpow * QuantizePVT.pow43[ l3[3] ];
			xfsf += (x0 * x0 + x1 * x1) + (x2 * x2 + x3 * x3 );

			// xr += 4;
			// xr34 += 4;
		}
		if( remaining != 0 ) {
			x[0] = x[1] = x[2] = x[3] = 0;
			switch( remaining ) {
			case 3: x[2] = sfpow34 * xr34[xr34offset + 2];
			case 2: x[1] = sfpow34 * xr34[xr34offset + 1];
			case 1: x[0] = sfpow34 * xr34[xr34offset + 0];
			}

			k_34_4( x, l3, 0 );
			float x0 = 0, x1 = 0, x2 = 0;
			final float x3 = 0;

			switch( remaining ) {
			case 3:
				float v = xr[xroffset + 2];
				if( v < 0 ) {
					v = -v;
				}
				x2 = v - sfpow * QuantizePVT.pow43[l3[2]];
			case 2:
				v = xr[xroffset + 1];
				if( v < 0 ) {
					v = -v;
				}
				x1 = v - sfpow * QuantizePVT.pow43[l3[1]];
			case 1:
				v = xr[xroffset   ];
				if( v < 0 ) {
					v = -v;
				}
				x0 = v - sfpow * QuantizePVT.pow43[l3[0]];
			}
			xfsf += (x0 * x0 + x1 * x1) + (x2 * x2 + x3 * x3 );
		}
		return xfsf;
	}

	private static final class CalcNoiseCache {
		private boolean valid;
		private float   value;
	}

	private static final boolean tri_calc_sfb_noise_x34(final float[] xr, final int xroffset, final float[] xr34, final int xr34offset, final float l3_xmin, final int bw,
		final int sf, final CalcNoiseCache[] did_it)
	{
		CalcNoiseCache nc = did_it[sf];// java
		if( ! nc.valid ) {
			nc.valid = true;
			nc.value = calc_sfb_noise_x34( xr, xroffset, xr34, xr34offset, bw, sf );
		}
		if( l3_xmin < nc.value ) {
			return true;
		}
		if( sf < 255 ) {
			final int sf_x = sf + 1;
			nc = did_it[sf_x];// java
			if( ! nc.valid ) {
				nc.valid = true;
				nc.value = calc_sfb_noise_x34( xr, xroffset, xr34, xr34offset, bw, sf_x );
			}
			if( l3_xmin < nc.value ) {
				return true;
			}
		}
		if( sf > 0 ) {
			final int sf_x = sf - 1;
			nc = did_it[sf_x];// java
			if( ! nc.valid ) {
				nc.valid = true;
				nc.value = calc_sfb_noise_x34( xr, xroffset, xr34, xr34offset, bw, sf_x );
			}
			if( l3_xmin < nc.value ) {
				return true;
			}
		}
		return false;
	}

	/**
	 *  Robert Hegemann 2001-05-01
	 *  calculates quantization step size determined by allowed masking
	 */
	private static final int calc_scalefac(final float l3_xmin, final int bw) {
		final float c = 5.799142446f; /* 10 * 10^(2/3) * log10(4/3) */
		return 210 + (int) (c * (float)Math.log10( (double)(l3_xmin / bw) ) - .5f );
	}

	private static final int guess_scalefac_x34(final float[] xr, final int xroffset, final float[] xr34, final int xr34offset, final float l3_xmin, final int bw, final int sf_min)
	{
		final int guess = calc_scalefac( l3_xmin, bw );
		if( guess < sf_min ) {
			return sf_min;
		}
		if( guess >= 255 ) {
			return 255;
		}
		return guess;
	}

	/* the find_scalefac* routines calculate
	 * a quantization step size which would
	 * introduce as much noise as is allowed.
	 * The larger the step size the more
	 * quantization noise we'll get. The
	 * scalefactors are there to lower the
	 * global step size, allowing limited
	 * differences in quantization step sizes
	 * per band (shaping the noise).
	 */
	private static final int find_scalefac_x34(final float[] xr, final int xroffset, final float[] xr34, final int xr34offset, final float l3_xmin, final int bw,
		final int sf_min)
	{
		final CalcNoiseCache did_it[] = new CalcNoiseCache[256];
		int sf = 128, sf_ok = 255, delsf = 128, seen_good_one = 0, i;
		i = 256;
		do {
			did_it[--i] = new CalcNoiseCache();// already zeroed
		} while( i > 0 );
		for( i = 0; i < 8; ++i ) {
			delsf >>= 1;
			if( sf <= sf_min ) {
				sf += delsf;
			} else {
				final boolean bad = tri_calc_sfb_noise_x34( xr, xroffset, xr34, xr34offset, l3_xmin, bw, sf, did_it );
				if( bad ) {  /* distortion.  try a smaller scalefactor */
					sf -= delsf;
				} else {
					sf_ok = sf;
					sf += delsf;
					seen_good_one = 1;
				}
			}
		}
		/*  returning a scalefac without distortion, if possible */
		if( seen_good_one > 0 ) {
			sf = sf_ok;
		}
		if( sf <= sf_min ) {
			sf = sf_min;
		}
		return sf;
	}

/***********************************************************************
 *
 *      calc_short_block_vbr_sf()
 *      calc_long_block_vbr_sf()
 *
 *  Mark Taylor 2000-??-??
 *  Robert Hegemann 2000-10-25 made functions of it
 *
 ***********************************************************************/

/* a variation for vbr-mtrh */
	private static final int block_sf(final ALGO_s that, final float l3_xmin[/*SFBMAX*/], final int vbrsf[/*SFBMAX*/], final int vbrsfmin[/*SFBMAX*/])
	{
		final float[] xr = that.cod_info.xr;
		final float[] xr34_orig = that.xr34orig;
		final int[] width = that.cod_info.width;
		final boolean[] energy_above_cutoff = that.cod_info.energy_above_cutoff;
		final int max_nonzero_coeff = that.cod_info.max_nonzero_coeff;
		int maxsf = 0;
		int sfb = 0, m_o = -1;
		int j = 0, i = 0;
		final int psymax = that.cod_info.psymax;

		that.mingain_l = 0;
		that.mingain_s[0] = 0;
		that.mingain_s[1] = 0;
		that.mingain_s[2] = 0;
		while( j <= max_nonzero_coeff ) {
			final int w = width[sfb];
			final int m = (max_nonzero_coeff - j + 1);
			int l = w;
			if( l > m ) {
				l = m;
			}
			final float max_xr34 = vec_max_c( xr34_orig, j, l );

			final int m1 = find_lowest_scalefac( max_xr34 );
			vbrsfmin[sfb] = m1;
			if( that.mingain_l < m1 ) {
				that.mingain_l = m1;
			}
			if( that.mingain_s[i] < m1 ) {
				that.mingain_s[i] = m1;
			}
			if( ++i > 2 ) {
				i = 0;
			}
			int m2;
			if( sfb < psymax && w > 2 ) { /* mpeg2.5 at 8 kHz doesn't use all scalefactors, unused have width 2 */
				if( energy_above_cutoff[sfb] ) {
					// m2 = that.find( xr, j, xr34_orig, j, l3_xmin[sfb], l, m1 );
					if( that.find == guess_scalefac_x34 ) {
						m2 = guess_scalefac_x34( xr, j, xr34_orig, j, l3_xmin[sfb], l, m1 );
					} else if( that.find == find_scalefac_x34 ) {
						m2 = find_scalefac_x34( xr, j, xr34_orig, j, l3_xmin[sfb], l, m1 );
					} else {
						m2 = 0;// java: to suppress warning "The local variable m2 may not have been initialized"
					}
/* #if 0
					if( 0 ) {
						// Robert Hegemann 2007-09-29:
						//  It seems here is some more potential for speed improvements.
						//  Current find method does 11-18 quantization calculations.
						//  Using a "good guess" may help to reduce this amount.
						uint8_t guess = calc_scalefac(l3_xmin[sfb], l );
						DEBUGF(that.gfc, "sfb=%3d guess=%3d found=%3d diff=%3d\n", sfb, guess, m2,
						m2 - guess );
					}
#endif */
					if( maxsf < m2 ) {
						maxsf = m2;
					}
					if( m_o < m2 && m2 < 255 ) {
						m_o = m2;
					}
				} else {
					m2 = 255;
					maxsf = 255;
				}
			} else {
				if( maxsf < m1 ) {
					maxsf = m1;
				}
				m2 = maxsf;
			}
			vbrsf[sfb] = m2;
			++sfb;
			j += w;
		}
		for(; sfb < Encoder.SFBMAX; ++sfb ) {
			vbrsf[sfb] = maxsf;
			vbrsfmin[sfb] = 0;
		}
		if( m_o > -1 ) {
			maxsf = m_o;
			for(sfb = 0; sfb < Encoder.SFBMAX; ++sfb ) {
				if( vbrsf[sfb] == 255 ) {
					vbrsf[sfb] = m_o;
				}
			}
		}
		return maxsf;
	}

/***********************************************************************
 *
 *  quantize xr34 based on scalefactors
 *
 *  block_xr34
 *
 *  Mark Taylor 2000-??-??
 *  Robert Hegemann 2000-10-20 made functions of them
 *
 ***********************************************************************/
	private static final void quantize_x34(final ALGO_s that) {
		final float x[] = new float[4];
		final int tmp_l3[] = new int[4];
		final float[] xr34 = that.xr34orig;// java
		int xr34_orig = 0;// xr34[xr34_orig]
		final III_GrInfo cod_info = that.cod_info;
		final int ifqstep = (cod_info.scalefac_scale == 0) ? 2 : 4;
		final int[] l3_enc = cod_info.l3_enc;// java
		int l3 = 0;// l3_enc[l3]
		int j = 0, sfb = 0;
		final int max_nonzero_coeff = cod_info.max_nonzero_coeff;

		while( j <= max_nonzero_coeff ) {
			final int s =
					(cod_info.scalefac[sfb] + (cod_info.preflag ? QuantizePVT.pretab[sfb] : 0)) * ifqstep
					+ cod_info.subblock_gain[cod_info.window[sfb]] * 8;
			final int sfac = (cod_info.global_gain - s);
			final float sfpow34 = QuantizePVT.ipow20[sfac];
			final int w = cod_info.width[sfb];
			final int m = (max_nonzero_coeff - j + 1 );

			j += w;
			++sfb;

			int i = (w <= m) ? w : m;
			final int remaining = (i & 0x03);
			i >>>= 2;

			while( i-- > 0 ) {
				x[0] = sfpow34 * xr34[ xr34_orig + 0 ];
				x[1] = sfpow34 * xr34[ xr34_orig + 1 ];
				x[2] = sfpow34 * xr34[ xr34_orig + 2 ];
				x[3] = sfpow34 * xr34[ xr34_orig + 3 ];

				k_34_4( x, l3_enc, l3 );

				l3 += 4;
				xr34_orig += 4;
			}
			if( remaining != 0 ) {
				x[0] = x[1] = x[2] = x[3] = 0;
				switch( remaining ) {
				case 3: x[2] = sfpow34 * xr34[ xr34_orig + 2 ];
				case 2: x[1] = sfpow34 * xr34[ xr34_orig + 1 ];
				case 1: x[0] = sfpow34 * xr34[ xr34_orig + 0 ];
				}

				k_34_4( x, tmp_l3, 0 );

				switch( remaining ) {
				case 3: l3_enc[ l3 + 2 ] = tmp_l3[2];
				case 2: l3_enc[ l3 + 1 ] = tmp_l3[1];
				case 1: l3_enc[ l3 + 0 ] = tmp_l3[0];
				}

				l3 += remaining;
				xr34_orig += remaining;
			}
		}
	}

	private static final byte max_range_short[] = {// [Encoder.SBMAX_s * 3] = {
		15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
		0, 0, 0
	};

	private static final byte max_range_long[] = {// [Encoder.SBMAX_l] = {
		15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0
	};

	private static final byte max_range_long_lsf_pretab[] = {// [Encoder.SBMAX_l] = {
		7, 7, 7, 7, 7, 7, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};

/*
    sfb=0..5  scalefac < 16
    sfb>5     scalefac < 8

    ifqstep = ( cod_info.scalefac_scale == 0 ) ? 2 : 4;
    ol_sf =  (cod_info.global_gain-210.0 );
    ol_sf -= 8*cod_info.subblock_gain[i];
    ol_sf -= ifqstep*scalefac[gr][ch].s[sfb][i];
*/

	private static final void set_subblock_gain(final III_GrInfo cod_info, final int mingain_s[/*3*/], final int sf[]) {
		final int maxrange1 = 15, maxrange2 = 7;
		final int ifqstepShift = (cod_info.scalefac_scale == 0) ? 1 : 2;
		final int[] sbg = cod_info.subblock_gain;
		final int psymax = cod_info.psymax;
		int psydiv = 18;
		int min_sbg = 7;

		if( psydiv > psymax ) {
			psydiv = psymax;
		}
		for( int i = 0; i < 3; ++i ) {
			int maxsf1 = 0, maxsf2 = 0, minsf = 1000;
			/* see if we should use subblock gain */
			int sfb = i;
			for( ; sfb < psydiv; sfb += 3 ) { /* part 1 */
				final int v = -sf[sfb];
				if( maxsf1 < v ) {
					maxsf1 = v;
				}
				if( minsf > v ) {
					minsf = v;
				}
			}
			for(; sfb < Encoder.SFBMAX; sfb += 3 ) { /* part 2 */
				final int v = -sf[sfb];
				if( maxsf2 < v ) {
					maxsf2 = v;
				}
				if( minsf > v ) {
					minsf = v;
				}
			}

			/* boost subblock gain as little as possible so we can
			 * reach maxsf1 with scalefactors
			 * 8*sbg >= maxsf1
			 */
			{
				final int m1 = maxsf1 - (maxrange1 << ifqstepShift );
				final int m2 = maxsf2 - (maxrange2 << ifqstepShift );

				maxsf1 = (m1 >= m2 ? m1 : m2);
			}
			if( minsf > 0 ) {
				sbg[i] = minsf >> 3;
			} else {
				sbg[i] = 0;
			}
			if( maxsf1 > 0 ) {
				final int m1 = sbg[i];
				final int m2 = (maxsf1 + 7) >> 3;
				sbg[i] = (m1 >= m2 ? m1 : m2);
			}
			if( sbg[i] > 0 && mingain_s[i] > (cod_info.global_gain - (sbg[i] << 3)) ) {
				sbg[i] = (cod_info.global_gain - mingain_s[i]) >> 3;
			}
			if( sbg[i] > 7 ) {
				sbg[i] = 7;
			}
			if( min_sbg > sbg[i] ) {
				min_sbg = sbg[i];
			}
		}
		final int sbg0 = sbg[0] << 3;
		final int sbg1 = sbg[1] << 3;
		final int sbg2 = sbg[2] << 3;
		for( int sfb = 0; sfb < Encoder.SFBMAX; ) {
			sf[sfb++] += sbg0;
			sf[sfb++] += sbg1;
			sf[sfb++] += sbg2;
		}
		if( min_sbg > 0 ) {
			sbg[0] -= min_sbg;
			sbg[1] -= min_sbg;
			sbg[2] -= min_sbg;
			cod_info.global_gain -= min_sbg << 3;
		}
	}

/*
	  ifqstep = ( cod_info.scalefac_scale == 0 ) ? 2 : 4;
	  ol_sf =  (cod_info.global_gain-210.0 );
	  ol_sf -= ifqstep*scalefac[gr][ch].l[sfb];
	  if( cod_info.preflag && sfb>=11)
	  ol_sf -= ifqstep*pretab[sfb];
*/
	private static final void set_scalefacs(final III_GrInfo cod_info, final int[] vbrsfmin, final int sf[], final byte[] max_range) {
		final int ifqstep = (cod_info.scalefac_scale == 0) ? 2 : 4;
		final int ifqstepShift = (cod_info.scalefac_scale == 0) ? 1 : 2;
		final int[] scalefac = cod_info.scalefac;
		final int sfbmax = cod_info.sfbmax;
		final int[] sbg = cod_info.subblock_gain;
		final int[] window = cod_info.window;
		final boolean preflag = cod_info.preflag;

		if( preflag ) {
			for( int sfb = 11; sfb < sfbmax; ++sfb ) {
				sf[sfb] += QuantizePVT.pretab[sfb] * ifqstep;
			}
		}
		int sfb = 0;
		for( ; sfb < sfbmax; ++sfb ) {
			final int gain = cod_info.global_gain - (sbg[window[sfb]] << 3)
						- ((preflag ? QuantizePVT.pretab[sfb] : 0) * ifqstep );

			if( sf[sfb] < 0 ) {
				final int m = gain - vbrsfmin[sfb];
				/* ifqstep*scalefac >= -sf[sfb], so round UP */
				scalefac[sfb] = (ifqstep - 1 - sf[sfb]) >> ifqstepShift;

				if( scalefac[sfb] > max_range[sfb] ) {
					scalefac[sfb] = max_range[sfb];
				}
				if( scalefac[sfb] > 0 && (scalefac[sfb] << ifqstepShift) > m ) {
					scalefac[sfb] = m >> ifqstepShift;
				}
			} else {
				scalefac[sfb] = 0;
			}
		}
		for(; sfb < Encoder.SFBMAX; ++sfb ) {
			scalefac[sfb] = 0; /* sfb21 */
		}
	}

// #ifndef NDEBUG
	private static final boolean checkScalefactor(final III_GrInfo cod_info, final int vbrsfmin[/*SFBMAX*/]) {
		final int ifqstep = cod_info.scalefac_scale == 0 ? 2 : 4;
		for( int sfb = 0; sfb < cod_info.psymax; ++sfb ) {
			final int s = ((cod_info.scalefac[sfb] +
					(cod_info.preflag ? QuantizePVT.pretab[sfb] : 0)) * ifqstep) +
					(cod_info.subblock_gain[ cod_info.window[sfb] ] << 3);

			if( (cod_info.global_gain - s) < vbrsfmin[sfb] ) {
				/*
				   fprintf( stdout, "sf %d\n", sfb  );
				   fprintf( stdout, "min %d\n", vbrsfmin[sfb]  );
				   fprintf( stdout, "ggain %d\n", cod_info.global_gain  );
				   fprintf( stdout, "scalefac %d\n", cod_info.scalefac[sfb]  );
				   fprintf( stdout, "pretab %d\n", (cod_info.preflag ? pretab[sfb] : 0)  );
				   fprintf( stdout, "scale %d\n", (cod_info.scalefac_scale + 1)  );
				   fprintf( stdout, "subgain %d\n", cod_info.subblock_gain[cod_info.window[sfb]] * 8  );
				   fflush( stdout  );
				   exit(-1 );
				 */
				return false;
			}
		}
		return true;
	}
// #endif

/******************************************************************
 *
 *  short block scalefacs
 *
 ******************************************************************/
	private static final void short_block_constrain(final ALGO_s that, final int vbrsf[/*SFBMAX*/],
													final int vbrsfmin[/*SFBMAX*/], int vbrmax)
	{
		final III_GrInfo cod_info = that.cod_info;
		final InternalFlags gfc = that.gfc;
		final SessionConfig cfg = gfc.cfg;
		final int maxminsfb = that.mingain_l;
		int maxover0 = 0, maxover1 = 0, delta = 0;
		final int psymax = cod_info.psymax;

		for( int sfb = 0; sfb < psymax; ++sfb ) {
			final int v = vbrmax - vbrsf[sfb];
			if( delta < v ) {
				delta = v;
			}
			final int v0 = v - (4 * 14 + 2 * max_range_short[sfb] );
			final int v1 = v - (4 * 14 + 4 * max_range_short[sfb] );
			if( maxover0 < v0 ) {
				maxover0 = v0;
			}
			if( maxover1 < v1 ) {
				maxover1 = v1;
			}
		}
		int mover;
		if( cfg.noise_shaping == 2 ) {
			/* allow scalefac_scale=1 */
			mover = (maxover0 <= maxover1 ? maxover0 : maxover1);
		} else {
			mover = maxover0;
		}
		if( delta > mover ) {
			delta = mover;
		}
		vbrmax -= delta;
		maxover0 -= mover;
		maxover1 -= mover;

		if( maxover0 == 0 ) {
			cod_info.scalefac_scale = 0;
		} else if( maxover1 == 0 ) {
			cod_info.scalefac_scale = 1;
		}
		if( vbrmax < maxminsfb ) {
			vbrmax = maxminsfb;
		}
		cod_info.global_gain = vbrmax;

		if( cod_info.global_gain < 0 ) {
			cod_info.global_gain = 0;
		} else if( cod_info.global_gain > 255 ) {
			cod_info.global_gain = 255;
		}
		{
			final int sf_temp[] = new int[Encoder.SFBMAX];
			for(int sfb = 0; sfb < Encoder.SFBMAX; ++sfb ) {
				sf_temp[sfb] = vbrsf[sfb] - vbrmax;
			}
			set_subblock_gain( cod_info, that.mingain_s, sf_temp );
			set_scalefacs( cod_info, vbrsfmin, sf_temp, max_range_short );
		}
		checkScalefactor( cod_info, vbrsfmin );
	}

	/******************************************************************
	*
	*  long block scalefacs
	*
	******************************************************************/
	private static final void long_block_constrain(final ALGO_s that, final int vbrsf[/*SFBMAX*/], final int vbrsfmin[/*SFBMAX*/],
												   int vbrmax)
	{
		final III_GrInfo cod_info = that.cod_info;
		final InternalFlags gfc = that.gfc;
		final SessionConfig cfg = gfc.cfg;
		byte[] max_rangep;
		final int maxminsfb = that.mingain_l;
		int delta = 0;
		int vm0p = 1, vm1p = 1;
		final int psymax = cod_info.psymax;

		max_rangep = cfg.mode_gr == 2 ? max_range_long : max_range_long_lsf_pretab;

		int maxover0 = 0;
		int maxover1 = 0;
		int maxover0p = 0;      /* pretab */
		int maxover1p = 0;      /* pretab */

		for( int sfb = 0; sfb < psymax; ++sfb ) {
			final int v = vbrmax - vbrsf[sfb];
			if( delta < v ) {
				delta = v;
			}
			int v1 = max_range_long[sfb];// java
			final int v0 = v - (v1 << 1);
			v1 = v - (v1 << 2);
			int v1p = max_rangep[sfb] + QuantizePVT.pretab[sfb];// java
			final int v0p = v - (v1p << 1);
			v1p = v - (v1p << 2);
			if( maxover0 < v0 ) {
				maxover0 = v0;
			}
			if( maxover1 < v1 ) {
				maxover1 = v1;
			}
			if( maxover0p < v0p ) {
				maxover0p = v0p;
			}
			if( maxover1p < v1p ) {
				maxover1p = v1p;
			}
		}
		if( vm0p == 1 ) {
			int gain = vbrmax - maxover0p;
			if( gain < maxminsfb ) {
				gain = maxminsfb;
			}
			for( int sfb = 0; sfb < psymax; ++sfb ) {
				final int a = (gain - vbrsfmin[sfb]) - (QuantizePVT.pretab[sfb] << 1);
				if( a <= 0 ) {
					vm0p = 0;
					vm1p = 0;
					break;
				}
			}
		}
		if( vm1p == 1 ) {
			int gain = vbrmax - maxover1p;
			if( gain < maxminsfb ) {
				gain = maxminsfb;
			}
			for( int sfb = 0; sfb < psymax; ++sfb ) {
				final int b = (gain - vbrsfmin[sfb]) - (QuantizePVT.pretab[sfb] << 2);
				if( b <= 0 ) {
					vm1p = 0;
					break;
				}
			}
		}
		if( vm0p == 0 ) {
			maxover0p = maxover0;
		}
		if( vm1p == 0 ) {
			maxover1p = maxover1;
		}
		if( cfg.noise_shaping != 2 ) {
			maxover1 = maxover0;
			maxover1p = maxover0p;
		}
		int mover = (maxover0 <= maxover0p ? maxover0 : maxover0p);
		mover = (mover <= maxover1 ? mover : maxover1);
		mover = (mover <= maxover1p ? mover : maxover1p);

		if( delta > mover ) {
			delta = mover;
		}
		vbrmax -= delta;
		if( vbrmax < maxminsfb ) {
			vbrmax = maxminsfb;
		}
		maxover0 -= mover;
		maxover0p -= mover;
		maxover1 -= mover;
		maxover1p -= mover;

		if( maxover0 == 0 ) {
			cod_info.scalefac_scale = 0;
			cod_info.preflag = false;
			max_rangep = max_range_long;
		} else if( maxover0p == 0 ) {
			cod_info.scalefac_scale = 0;
			cod_info.preflag = true;
		} else if( maxover1 == 0 ) {
			cod_info.scalefac_scale = 1;
			cod_info.preflag = false;
			max_rangep = max_range_long;
		} else if( maxover1p == 0 ) {
			cod_info.scalefac_scale = 1;
			cod_info.preflag = true;
		} else {
			// assert( 0 );      /* this should not happen */
		}
		cod_info.global_gain = vbrmax;
		if( cod_info.global_gain < 0 ) {
			cod_info.global_gain = 0;
		} else if( cod_info.global_gain > 255 ) {
			cod_info.global_gain = 255;
		}
		{
			final int sf_temp[] = new int[Encoder.SFBMAX];
			for(int sfb = 0; sfb < Encoder.SFBMAX; ++sfb ) {
				sf_temp[sfb] = vbrsf[sfb] - vbrmax;
			}
			set_scalefacs(cod_info, vbrsfmin, sf_temp, max_rangep );
		}
		assert(checkScalefactor(cod_info, vbrsfmin) );
	}

	private static final void bitcount(final ALGO_s that) {
		final boolean rc = Takehiro.scale_bitcount( that.gfc, that.cod_info );

		if( ! rc ) {
			return;
		}
		/*  this should not happen due to the way the scalefactors are selected  */
		//System.err.print("INTERNAL ERROR IN VBR NEW CODE (986), please send bug report\n" );
		//System.exit( -1 );
		throw new InternalError("INTERNAL ERROR IN VBR NEW CODE (986), please send bug report");
	}

	private static final int quantizeAndCountBits(final ALGO_s that) {
		quantize_x34( that );
		that.cod_info.part2_3_length = Takehiro.noquant_count_bits( that.gfc, that.cod_info, null );
		return that.cod_info.part2_3_length;
	}

	private static final int tryGlobalStepsize(final ALGO_s that, final int sfwork[/*SFBMAX*/],
											   final int vbrsfmin[/*SFBMAX*/], final int delta)
	{
		final float xrpow_max = that.cod_info.xrpow_max;
		final int sftemp[] = new int[Encoder.SFBMAX];
		int vbrmax = 0;
		for(int i = 0; i < Encoder.SFBMAX; ++i ) {
			int gain = sfwork[i] + delta;
			if( gain < vbrsfmin[i] ) {
				gain = vbrsfmin[i];
			}
			if( gain > 255 ) {
				gain = 255;
			}
			if( vbrmax < gain ) {
				vbrmax = gain;
			}
			sftemp[i] = gain;
		}
		// that.alloc( that, sftemp, vbrsfmin, vbrmax );
		if( that.alloc == short_block_constrain ) {
			short_block_constrain( that, sftemp, vbrsfmin, vbrmax );
		} else if( that.alloc == long_block_constrain ) {
			long_block_constrain( that, sftemp, vbrsfmin, vbrmax );
		}
		bitcount( that );
		final int nbits = quantizeAndCountBits( that );
		that.cod_info.xrpow_max = xrpow_max;
		return nbits;
	}

	private static final void searchGlobalStepsizeMax(final ALGO_s that, final int sfwork[/*SFBMAX*/],
													  final int vbrsfmin[/*SFBMAX*/], final int target)
	{
		final III_GrInfo cod_info = that.cod_info;
		final int gain = cod_info.global_gain;
		int curr = gain;
		int gain_ok = 1024;
		int nbits = Takehiro.LARGE_BITS;
		int l = gain, r = 512;

		while( l <= r ) {
			curr = (l + r) >> 1;
			nbits = tryGlobalStepsize(that, sfwork, vbrsfmin, curr - gain );
			if( nbits == 0 || (nbits + cod_info.part2_length) < target ) {
				r = curr - 1;
				gain_ok = curr;
			} else {
				l = curr + 1;
				if( gain_ok == 1024 ) {
					gain_ok = curr;
				}
			}
		}
		if( gain_ok != curr ) {
			curr = gain_ok;
			nbits = tryGlobalStepsize( that, sfwork, vbrsfmin, curr - gain );
		}
	}

	private static final int sfDepth(final int sfwork[/*SFBMAX*/]) {
		int m = 0;
		for(int j = Encoder.SFBMAX, i = 0; j > 0; --j, ++i ) {
			final int di = 255 - sfwork[i];
			if( m < di ) {
				m = di;
			}
		}
		return m;
	}

	private static final void cutDistribution(final int sfwork[/*SFBMAX*/], final int sf_out[/*SFBMAX*/], final int cut) {
		for(int j = Encoder.SFBMAX, i = 0; j > 0; --j, ++i ) {
			final int x = sfwork[i];
			sf_out[i] = x < cut ? x : cut;
		}
	}

	private static final int flattenDistribution(final int sfwork[/*SFBMAX*/], final int sf_out[/*SFBMAX*/], final int dm, final int k, final int p)
	{
		int sfmax = 0;
		if( dm > 0 ) {
			for(int j = Encoder.SFBMAX, i = 0; j > 0; --j, ++i ) {
				final int di = p - sfwork[i];
				int x = sfwork[i] + (k * di) / dm;
				if( x < 0 ) {
					x = 0;
				} else {
					if( x > 255 ) {
						x = 255;
					}
				}
				sf_out[i] = x;
				if( sfmax < x ) {
					sfmax = x;
				}
			}
			return sfmax;
		}// else {
			for(int j = Encoder.SFBMAX, i = 0; j > 0; --j, ++i ) {
				final int x = sfwork[i];
				sf_out[i] = x;
				if( sfmax < x ) {
					sfmax = x;
				}
			}
		//}
		return sfmax;
	}

	private static final int tryThatOne(final ALGO_s that, final int sftemp[/*SFBMAX*/], final int vbrsfmin[/*SFBMAX*/], final int vbrmax) {
		final float xrpow_max = that.cod_info.xrpow_max;
		int nbits = Takehiro.LARGE_BITS;
		// that.alloc( that, sftemp, vbrsfmin, vbrmax );
		if( that.alloc == short_block_constrain ) {
			short_block_constrain( that, sftemp, vbrsfmin, vbrmax );
		} else if( that.alloc == long_block_constrain ) {
			long_block_constrain( that, sftemp, vbrsfmin, vbrmax );
		}
		bitcount( that );
		nbits = quantizeAndCountBits( that );
		nbits += that.cod_info.part2_length;
		that.cod_info.xrpow_max = xrpow_max;
		return nbits;
	}

	private static final void outOfBitsStrategy(final ALGO_s that, final int sfwork[/*SFBMAX*/], final int vbrsfmin[/*SFBMAX*/], final int target)
	{
		final int wrk[] = new int[Encoder.SFBMAX];
		final int dm = sfDepth( sfwork );
		final int p = that.cod_info.global_gain;

		/* PART 1 */
		{
			int bi = dm / 2;
			int bi_ok = -1;
			int bu = 0;
			int bo = dm;
			for( ;; ) {
				final int sfmax = flattenDistribution( sfwork, wrk, dm, bi, p );
				final int nbits = tryThatOne( that, wrk, vbrsfmin, sfmax );
				if( nbits <= target ) {
					bi_ok = bi;
					bo = bi - 1;
				} else {
					bu = bi + 1;
				}
				if( bu <= bo ) {
					bi = (bu + bo) / 2;
				} else {
					break;
				}
			}
			if( bi_ok >= 0 ) {
				if( bi != bi_ok ) {
					final int sfmax = flattenDistribution(sfwork, wrk, dm, bi_ok, p );
					tryThatOne(that, wrk, vbrsfmin, sfmax );
				}
				return;
			}
		}

		/* PART 2: */
		{
			int bi = (255 + p) / 2;
			int bi_ok = -1;
			int bu = p;
			int bo = 255;
			for( ;; ) {
				final int sfmax = flattenDistribution( sfwork, wrk, dm, dm, bi );
				final int nbits = tryThatOne( that, wrk, vbrsfmin, sfmax );
				if( nbits <= target ) {
					bi_ok = bi;
					bo = bi - 1;
				} else {
					bu = bi + 1;
				}
				if( bu <= bo ) {
					bi = (bu + bo) / 2;
				} else {
					break;
				}
			}
			if( bi_ok >= 0 ) {
				if( bi != bi_ok ) {
					final int sfmax = flattenDistribution( sfwork, wrk, dm, dm, bi_ok );
					tryThatOne( that, wrk, vbrsfmin, sfmax );
				}
				return;
			}
		}

		/* fall back to old code, likely to be never called */
		searchGlobalStepsizeMax( that, wrk, vbrsfmin, target );
	}

	private static int reduce_bit_usage(final InternalFlags gfc, final int gr, final int ch
/* #if 0
		, const float xr34orig[576], const float l3_xmin[SFBMAX], int maxbits
#endif */
		)
	{
		final SessionConfig cfg = gfc.cfg;
		final III_GrInfo cod_info = gfc.l3_side.tt[gr][ch];
		/*  try some better scalefac storage */
		Takehiro.best_scalefac_store( gfc, gr, ch, gfc.l3_side );

		/*  best huffman_divide may save some bits too */
		if( cfg.use_best_huffman == 1 ) {
			Takehiro.best_huffman_divide( gfc, cod_info );
		}
		return cod_info.part2_3_length + cod_info.part2_length;
	}

	@SuppressWarnings("boxing")
	static final int VBR_encode_frame(final InternalFlags gfc, final float xr34orig[][][]/*[2][2][576]*/,
                                      final float l3_xmin[][][]/*[2][2][SFBMAX]*/, final int max_bits[][]/*[2][2]*/)
	{
		final SessionConfig cfg = gfc.cfg;
		final int sfwork_[][][] = new int[2][2][Encoder.SFBMAX];
		final int vbrsfmin_[][][] = new int[2][2][Encoder.SFBMAX];
		final ALGO_s that_[][] = new ALGO_s[2][2];
		final int ngr = cfg.mode_gr;
		final int nch = cfg.channels_out;
		final int max_nbits_ch[][] = new int[2][2];// {{0, 0}, {0 ,0}};// java: already zeroed
		final int max_nbits_gr[] = new int[2];// {0, 0};// java: already zeroed
		int max_nbits_fr = 0;
		final int use_nbits_ch[][] = {{Util.MAX_BITS_PER_CHANNEL+1, Util.MAX_BITS_PER_CHANNEL+1},{Util.MAX_BITS_PER_CHANNEL+1, Util.MAX_BITS_PER_CHANNEL+1}};
		final int use_nbits_gr[] = { Util.MAX_BITS_PER_GRANULE+1, Util.MAX_BITS_PER_GRANULE+1 };
		int use_nbits_fr = Util.MAX_BITS_PER_GRANULE+ Util.MAX_BITS_PER_GRANULE;

		/* set up some encoding parameters */
		final III_GrInfo[][] tt = gfc.l3_side.tt;// java
		for( int gr = 0; gr < ngr; ++gr ) {
			max_nbits_gr[gr] = 0;
			for( int ch = 0; ch < nch; ++ch ) {
				max_nbits_ch[gr][ch] = max_bits[gr][ch];
				use_nbits_ch[gr][ch] = 0;
				max_nbits_gr[gr] += max_bits[gr][ch];
				max_nbits_fr += max_bits[gr][ch];
				final ALGO_s a = new ALGO_s();
				that_[gr][ch] = a;
				a.find = (cfg.full_outer_loop < 0) ? guess_scalefac_x34 : find_scalefac_x34;
				a.gfc = gfc;
				a.cod_info = tt[gr][ch];
				a.xr34orig = xr34orig[gr][ch];
				if( a.cod_info.block_type == Encoder.SHORT_TYPE ) {
					a.alloc = short_block_constrain;
				} else {
					a.alloc = long_block_constrain;
				}
			}/* for ch */
		}
		/* searches scalefactors */
		for( int gr = 0; gr < ngr; ++gr ) {
			for( int ch = 0; ch < nch; ++ch ) {
				if( max_bits[gr][ch] > 0 ) {
					final ALGO_s that = that_[gr][ch];
					final int[] sfwork = sfwork_[gr][ch];
					final int[] vbrsfmin = vbrsfmin_[gr][ch];

					final int vbrmax = block_sf( that, l3_xmin[gr][ch], sfwork, vbrsfmin );
					// that.alloc( that, sfwork, vbrsfmin, vbrmax );
					if( that.alloc == short_block_constrain ) {
						short_block_constrain( that, sfwork, vbrsfmin, vbrmax );
					} else if( that.alloc == long_block_constrain ) {
						long_block_constrain( that, sfwork, vbrsfmin, vbrmax );
					}

					bitcount( that );
				} else {
					/*  xr contains no energy
					 *  l3_enc, our encoding data, will be quantized to zero
					 *  continue with next channel
					 */
				}
			}/* for ch */
		}
		/* encode 'as is' */
		use_nbits_fr = 0;
		for( int gr = 0; gr < ngr; ++gr ) {
			use_nbits_gr[gr] = 0;
			for( int ch = 0; ch < nch; ++ch ) {
				final ALGO_s that = that_[gr][ch];
				if( max_bits[gr][ch] > 0 ) {
					final int[] buf = that.cod_info.l3_enc;
					int i = buf.length;
					do {
						buf[--i] = 0;
					} while( i > 0 );
					quantizeAndCountBits( that );
				} else {
					/*  xr contains no energy
					 *  l3_enc, our encoding data, will be quantized to zero
					 *  continue with next channel
					 */
				}
				use_nbits_ch[gr][ch] = reduce_bit_usage( gfc, gr, ch );
				use_nbits_gr[gr] += use_nbits_ch[gr][ch];
			}               /* for ch */
			use_nbits_fr += use_nbits_gr[gr];
		}

		/* check bit constrains */
		boolean ok;
		if( use_nbits_fr <= max_nbits_fr ) {
			ok = true;
			for( int gr = 0; gr < ngr; ++gr ) {
				if( use_nbits_gr[gr] > Util.MAX_BITS_PER_GRANULE ) {
					/* violates the rule that every granule has to use no more
					 * bits than MAX_BITS_PER_GRANULE
					 */
					ok = false;
				}
				for( int ch = 0; ch < nch; ++ch ) {
					if( use_nbits_ch[gr][ch] > Util.MAX_BITS_PER_CHANNEL ) {
						/* violates the rule that every gr_ch has to use no more
						 * bits than MAX_BITS_PER_CHANNEL
						 *
						 * This isn't explicitly stated in the ISO docs, but the
						 * part2_3_length field has only 12 bits, that makes it
						 * up to a maximum size of 4095 bits!!!
						 */
						ok = false;
					}
				}
			}
			if( ok ) {
				return use_nbits_fr;
			}
		}

		/* OK, we are in trouble and have to define how many bits are
		 * to be used for each granule
		 */
		{
			ok = true;
			int sum_fr = 0;

			for( int gr = 0; gr < ngr; ++gr ) {
				max_nbits_gr[gr] = 0;
				for( int ch = 0; ch < nch; ++ch ) {
					if( use_nbits_ch[gr][ch] > Util.MAX_BITS_PER_CHANNEL ) {
						max_nbits_ch[gr][ch] = Util.MAX_BITS_PER_CHANNEL;
					} else {
						max_nbits_ch[gr][ch] = use_nbits_ch[gr][ch];
					}
					max_nbits_gr[gr] += max_nbits_ch[gr][ch];
				}
				if( max_nbits_gr[gr] > Util.MAX_BITS_PER_GRANULE ) {
					final float f[] = new float[2];// {0.0f, 0.0f};// java: already zeroed
					float s = 0.0f;
					for( int ch = 0; ch < nch; ++ch ) {
						if( max_nbits_ch[gr][ch] > 0 ) {
							f[ch] = (float)Math.sqrt( Math.sqrt( (double)max_nbits_ch[gr][ch] ) );// TODO java: pow 0.25?
							s += f[ch];
						} else {
							f[ch] = 0;
						}
					}
					for( int ch = 0; ch < nch; ++ch ) {
						if( s > 0 ) {
							max_nbits_ch[gr][ch] = (int)(Util.MAX_BITS_PER_GRANULE * f[ch] / s);
						} else {
							max_nbits_ch[gr][ch] = 0;
						}
					}
					if( nch > 1 ) {
						if( max_nbits_ch[gr][0] > use_nbits_ch[gr][0] + 32 ) {
							max_nbits_ch[gr][1] += max_nbits_ch[gr][0];
							max_nbits_ch[gr][1] -= use_nbits_ch[gr][0] + 32;
							max_nbits_ch[gr][0] = use_nbits_ch[gr][0] + 32;
						}
						if( max_nbits_ch[gr][1] > use_nbits_ch[gr][1] + 32 ) {
							max_nbits_ch[gr][0] += max_nbits_ch[gr][1];
							max_nbits_ch[gr][0] -= use_nbits_ch[gr][1] + 32;
							max_nbits_ch[gr][1] = use_nbits_ch[gr][1] + 32;
						}
						if( max_nbits_ch[gr][0] > Util.MAX_BITS_PER_CHANNEL ) {
							max_nbits_ch[gr][0] = Util.MAX_BITS_PER_CHANNEL;
						}
						if( max_nbits_ch[gr][1] > Util.MAX_BITS_PER_CHANNEL ) {
							max_nbits_ch[gr][1] = Util.MAX_BITS_PER_CHANNEL;
						}
					}
					max_nbits_gr[gr] = 0;
					for( int ch = 0; ch < nch; ++ch ) {
						max_nbits_gr[gr] += max_nbits_ch[gr][ch];
					}
				}
				sum_fr += max_nbits_gr[gr];
			}
			if( sum_fr > max_nbits_fr ) {
				{
					final float f[] = {0.0f, 0.0f};
					float s = 0.0f;
					for( int gr = 0; gr < ngr; ++gr ) {
						if( max_nbits_gr[gr] > 0 ) {
							f[gr] = (float)Math.sqrt( (double)max_nbits_gr[gr] );
							s += f[gr];
						} else {
							f[gr] = 0;
						}
					}
					for( int gr = 0; gr < ngr; ++gr ) {
						if( s > 0 ) {
							max_nbits_gr[gr] = (int)(max_nbits_fr * f[gr] / s);
						} else {
							max_nbits_gr[gr] = 0;
						}
					}
				}
				if( ngr > 1 ) {
					if( max_nbits_gr[0] > use_nbits_gr[0] + 125 ) {
						max_nbits_gr[1] += max_nbits_gr[0];
						max_nbits_gr[1] -= use_nbits_gr[0] + 125;
						max_nbits_gr[0] = use_nbits_gr[0] + 125;
					}
					if( max_nbits_gr[1] > use_nbits_gr[1] + 125 ) {
						max_nbits_gr[0] += max_nbits_gr[1];
						max_nbits_gr[0] -= use_nbits_gr[1] + 125;
						max_nbits_gr[1] = use_nbits_gr[1] + 125;
					}
					for( int gr = 0; gr < ngr; ++gr ) {
						if( max_nbits_gr[gr] > Util.MAX_BITS_PER_GRANULE ) {
							max_nbits_gr[gr] = Util.MAX_BITS_PER_GRANULE;
						}
					}
				}
				for( int gr = 0; gr < ngr; ++gr ) {
					final float f[] = {0.0f, 0.0f};
					float s = 0.0f;
					for( int ch = 0; ch < nch; ++ch ) {
						if( max_nbits_ch[gr][ch] > 0 ) {
							f[ch] = (float)Math.sqrt( (double)max_nbits_ch[gr][ch] );
							s += f[ch];
						} else {
							f[ch] = 0;
						}
					}
					for( int ch = 0; ch < nch; ++ch ) {
						if( s > 0 ) {
							max_nbits_ch[gr][ch] = (int)(max_nbits_gr[gr] * f[ch] / s);
						} else {
							max_nbits_ch[gr][ch] = 0;
						}
					}
					if( nch > 1 ) {
						if( max_nbits_ch[gr][0] > use_nbits_ch[gr][0] + 32 ) {
							max_nbits_ch[gr][1] += max_nbits_ch[gr][0];
							max_nbits_ch[gr][1] -= use_nbits_ch[gr][0] + 32;
							max_nbits_ch[gr][0] = use_nbits_ch[gr][0] + 32;
						}
						if( max_nbits_ch[gr][1] > use_nbits_ch[gr][1] + 32 ) {
							max_nbits_ch[gr][0] += max_nbits_ch[gr][1];
							max_nbits_ch[gr][0] -= use_nbits_ch[gr][1] + 32;
							max_nbits_ch[gr][1] = use_nbits_ch[gr][1] + 32;
						}
						for( int ch = 0; ch < nch; ++ch ) {
							if( max_nbits_ch[gr][ch] > Util.MAX_BITS_PER_CHANNEL ) {
								max_nbits_ch[gr][ch] = Util.MAX_BITS_PER_CHANNEL;
							}
						}
					}
				}
			}
			/* sanity check */
			sum_fr = 0;
			for( int gr = 0; gr < ngr; ++gr ) {
				int sum_gr = 0;
				for( int ch = 0; ch < nch; ++ch ) {
					sum_gr += max_nbits_ch[gr][ch];
					if( max_nbits_ch[gr][ch] > Util.MAX_BITS_PER_CHANNEL ) {
						ok = false;
					}
				}
				sum_fr += sum_gr;
				if( sum_gr > Util.MAX_BITS_PER_GRANULE ) {
					ok = false;
				}
			}
			if( sum_fr > max_nbits_fr ) {
				ok = false;
			}
			if( ! ok ) {
				/* we must have done something wrong, fallback to 'on_pe' based constrain */
				for( int gr = 0; gr < ngr; ++gr ) {
					for( int ch = 0; ch < nch; ++ch ) {
						max_nbits_ch[gr][ch] = max_bits[gr][ch];
					}
				}
			}
		}

		/* we already called the 'best_scalefac_store' function, so we need to reset some
		 * variables before we can do it again.
		 */
		final int[][] scfsi = gfc.l3_side.scfsi;// java
		for( int ch = 0; ch < nch; ++ch ) {
			final int[] scfsi_ch = scfsi[ch];// java
			scfsi_ch[0] = 0;
			scfsi_ch[1] = 0;
			scfsi_ch[2] = 0;
			scfsi_ch[3] = 0;
		}
		for( int gr = 0; gr < ngr; ++gr ) {
			for( int ch = 0; ch < nch; ++ch ) {
				tt[gr][ch].scalefac_compress = 0;
			}
		}

		/* alter our encoded data, until it fits into the target bitrate */
		use_nbits_fr = 0;
		for( int gr = 0; gr < ngr; ++gr ) {
			use_nbits_gr[gr] = 0;
			for( int ch = 0; ch < nch; ++ch ) {
				final ALGO_s that = that_[gr][ch];
				use_nbits_ch[gr][ch] = 0;
				if( max_bits[gr][ch] > 0 ) {
					final int[] sfwork = sfwork_[gr][ch];
					final int[] vbrsfmin = vbrsfmin_[gr][ch];
					cutDistribution( sfwork, sfwork, that.cod_info.global_gain );
					outOfBitsStrategy( that, sfwork, vbrsfmin, max_nbits_ch[gr][ch] );
				}
				use_nbits_ch[gr][ch] = reduce_bit_usage( gfc, gr, ch );
				use_nbits_gr[gr] += use_nbits_ch[gr][ch];
			}               /* for ch */
			use_nbits_fr += use_nbits_gr[gr];
		}

		/* check bit constrains, but it should always be ok, iff there are no bugs ;-) */
		if( use_nbits_fr <= max_nbits_fr ) {
			return use_nbits_fr;
		}

		//System.err.printf("INTERNAL ERROR IN VBR NEW CODE (1313), please send bug report\n" +
		//		"maxbits=%d usedbits=%d\n", max_nbits_fr, use_nbits_fr );
		//System.exit( -1 );
		//return -1;
		throw new InternalError(String.format("INTERNAL ERROR IN VBR NEW CODE (1313), please send bug report\n" +
				"maxbits=%d usedbits=%d", max_nbits_fr, use_nbits_fr));
	}
}
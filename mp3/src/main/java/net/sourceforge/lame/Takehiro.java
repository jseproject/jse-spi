package net.sourceforge.lame;

// takehiro.c

class Takehiro {

	static final int LARGE_BITS = 100000;

	private static final class SubDVTable {
		private final int region0_count;
		private final int region1_count;
		SubDVTable(final int r0, final int r1 ) {
			this.region0_count = r0;
			this.region1_count = r1;
		}
	}
	private static final SubDVTable subdv_table[] = {// [23] = {
		new SubDVTable( 0, 0 ),              /* 0 bands */
		new SubDVTable( 0, 0 ),              /* 1 bands */
		new SubDVTable( 0, 0 ),              /* 2 bands */
		new SubDVTable( 0, 0 ),              /* 3 bands */
		new SubDVTable( 0, 0 ),              /* 4 bands */
		new SubDVTable( 0, 1 ),              /* 5 bands */
		new SubDVTable( 1, 1 ),              /* 6 bands */
		new SubDVTable( 1, 1 ),              /* 7 bands */
		new SubDVTable( 1, 2 ),              /* 8 bands */
		new SubDVTable( 2, 2 ),              /* 9 bands */
		new SubDVTable( 2, 3 ),              /* 10 bands */
		new SubDVTable( 2, 3 ),              /* 11 bands */
		new SubDVTable( 3, 4 ),              /* 12 bands */
		new SubDVTable( 3, 4 ),              /* 13 bands */
		new SubDVTable( 3, 4 ),              /* 14 bands */
		new SubDVTable( 4, 5 ),              /* 15 bands */
		new SubDVTable( 4, 5 ),              /* 16 bands */
		new SubDVTable( 4, 6 ),              /* 17 bands */
		new SubDVTable( 5, 6 ),              /* 18 bands */
		new SubDVTable( 5, 6 ),              /* 19 bands */
		new SubDVTable( 5, 7 ),              /* 20 bands */
		new SubDVTable( 6, 7 ),              /* 21 bands */
		new SubDVTable( 6, 7 )              /* 22 bands */
	};

	/*********************************************************************
	 * nonlinear quantization of xr
	 * More accurate formula than the ISO formula.  Takes into account
	 * the fact that we are quantizing xr . ix, but we want ix^4/3 to be
	 * as close as possible to x^4/3.  (taking the nearest int would mean
	 * ix is as close as possible to xr, which is different.)
	 *
	 * From Segher Boessenkool <segher@eastsite.nl>  11/1999
	 *
	 * 09/2000: ASM code removed in favor of IEEE754 hack by Takehiro
	 * Tominaga. If you need the ASM code, check CVS circa Aug 2000.
	 *
	 * 01/2004: Optimizations by Gabriel Bouvigne
	 *********************************************************************/
	private static final void quantize_lines_xrpow_01(int l, final float istep, final float[] xr, int xroffset, final int[] ix, int ixoffset) {
		final float compareval0 = (1.0f - 0.4054f) / istep;

		for( l += xroffset; xroffset < l; ) {
			final float xr_0 = xr[xroffset++];
			final float xr_1 = xr[xroffset++];
			final int ix_0 = (compareval0 > xr_0) ? 0 : 1;
			final int ix_1 = (compareval0 > xr_1) ? 0 : 1;
			ix[ixoffset++] = ix_0;
			ix[ixoffset++] = ix_1;
		}
	}

// #ifdef TAKEHIRO_IEEE754_HACK

	/* typedef union {
		final float   f;
		final int     i;
	} fi_union; */

	// private static final double MAGIC_FLOAT = (65536 * (128));
	// private static final int MAGIC_INT = 0x4b000000;

	private static final void quantize_lines_xrpow(int l, final float istep, final float[] xp, int xoffset, final int[] pi, int pioffset) {
/* if( QuantizePVT.TAKEHIRO_IEEE754_HACK ) {
		l = l >>> 1;
		final int remaining = l & 1;
		l = l >>> 1;
		while( l-- != 0 ) {
			float x0 = istep * xp[xoffset++];
			float x1 = istep * xp[xoffset++];
			float x2 = istep * xp[xoffset++];
			float x3 = istep * xp[xoffset++];

			x0 += MAGIC_FLOAT;
			float fi0 = x0;
			x1 += MAGIC_FLOAT;
			float fi1 = x1;
			x2 += MAGIC_FLOAT;
			float fi2 = x2;
			x3 += MAGIC_FLOAT;
			float fi3 = x3;

			fi0 = x0 + QuantizePVT.adj43[ Float.floatToIntBits( fi0 ) - MAGIC_INT ];
			fi1 = x1 + QuantizePVT.adj43[ Float.floatToIntBits( fi1 ) - MAGIC_INT ];
			fi2 = x2 + QuantizePVT.adj43[ Float.floatToIntBits( fi2 ) - MAGIC_INT ];
			fi3 = x3 + QuantizePVT.adj43[ Float.floatToIntBits( fi3 ) - MAGIC_INT ];

			pi[pioffset++] -= MAGIC_INT;
			pi[pioffset++] -= MAGIC_INT;
			pi[pioffset++] -= MAGIC_INT;
			pi[pioffset++] -= MAGIC_INT;
			// fi += 4;
			// xp += 4;
		}
		if( remaining != 0 ) {
			float x0 = istep * xp[xoffset++];
			float x1 = istep * xp[xoffset  ];

			x0 += MAGIC_FLOAT;
			float fi0 = x0;
			x1 += MAGIC_FLOAT;
			float fi1 = x1;

			fi0 = x0 + QuantizePVT.adj43[ Float.floatToIntBits( fi0 ) - MAGIC_INT ];
			fi1 = x1 + QuantizePVT.adj43[ Float.floatToIntBits( fi1 ) - MAGIC_INT ];

			pi[pioffset++] -= MAGIC_INT;
			pi[pioffset  ] -= MAGIC_INT;
		}
} else { */
		l = l >>> 1;
		final int remaining = l & 1;
		l = l >>> 1;
		while( l-- != 0 ) {
			float   x0, x1, x2, x3;
			final int     rx0, rx1, rx2, rx3;

			x0 = xp[xoffset++] * istep;
			x1 = xp[xoffset++] * istep;
			rx0 = (int)x0;// XRPOW_FTOI(x0, rx0);
			x2 = xp[xoffset++] * istep;
			rx1 = (int)x1;// XRPOW_FTOI(x1, rx1);
			x3 = xp[xoffset++] * istep;
			rx2 = (int)x2;// XRPOW_FTOI(x2, rx2);
			x0 += QuantizePVT.adj43[rx0];// QUANTFAC(rx0);
			rx3 = (int)x3;// XRPOW_FTOI(x3, rx3);
			x1 += QuantizePVT.adj43[rx1];// QUANTFAC(rx1);
			pi[pioffset++] = (int)x0;// XRPOW_FTOI(x0, *ix++);
			x2 += QuantizePVT.adj43[rx2];// QUANTFAC(rx2);
			pi[pioffset++]= (int)x1;// XRPOW_FTOI(x1, *ix++);
			x3 += QuantizePVT.adj43[rx3];// QUANTFAC(rx3);
			pi[pioffset++] = (int)x2;// XRPOW_FTOI(x2, *ix++);
			pi[pioffset++] = (int)x3;// XRPOW_FTOI(x3, *ix++);
		}
		if( remaining != 0 ) {
			float   x0, x1;
			final int     rx0, rx1;

			x0 = xp[xoffset++] * istep;
			x1 = xp[xoffset++] * istep;
			rx0 = (int)x0;// XRPOW_FTOI(x0, rx0);
			rx1 = (int)x1;// XRPOW_FTOI(x1, rx1);
			x0 += QuantizePVT.adj43[rx0];// QUANTFAC(rx0);
			x1 += QuantizePVT.adj43[rx1];// QUANTFAC(rx1);
			pi[pioffset++] = (int)x0;// XRPOW_FTOI(x0, *ix++);
			pi[pioffset++] = (int)x1;// XRPOW_FTOI(x1, *ix++);
		}
// }
	}

// #else

	/*********************************************************************
	 * XRPOW_FTOI is a macro to convert floats to ints.
	 * if XRPOW_FTOI(x) = nearest_int(x), then QUANTFAC(x)=adj43asm[x]
	 *                                         ROUNDFAC= -0.0946
	 *
	 * if XRPOW_FTOI(x) = floor(x), then QUANTFAC(x)=asj43[x]
	 *                                   ROUNDFAC=0.4054
	 *
	 * Note: using floor() or (int) is extremely slow. On machines where
	 * the TAKEHIRO_IEEE754_HACK code above does not work, it is worthwile
	 * to write some ASM for XRPOW_FTOI().
	 *********************************************************************/
/*	#define XRPOW_FTOI(src,dest) ((dest) = (int)(src))
	#define QUANTFAC(rx)  adj43[rx]
	#define ROUNDFAC 0.4054

	private static final void quantize_lines_xrpow(int l, final float istep, float[] xr, int[] ix) {
		unsigned int remaining;

		l = l >> 1;
		remaining = l % 2;
		l = l >> 1;
		while( l-- ) {
			float   x0, x1, x2, x3;
			final int     rx0, rx1, rx2, rx3;

			x0 = *xr++ * istep;
			x1 = *xr++ * istep;
			XRPOW_FTOI(x0, rx0);
			x2 = *xr++ * istep;
			XRPOW_FTOI(x1, rx1);
			x3 = *xr++ * istep;
			XRPOW_FTOI(x2, rx2);
			x0 += QUANTFAC(rx0);
			XRPOW_FTOI(x3, rx3);
			x1 += QUANTFAC(rx1);
			XRPOW_FTOI(x0, *ix++);
			x2 += QUANTFAC(rx2);
			XRPOW_FTOI(x1, *ix++);
			x3 += QUANTFAC(rx3);
			XRPOW_FTOI(x2, *ix++);
			XRPOW_FTOI(x3, *ix++);
		}
		if( remaining ) {
			FLOAT   x0, x1;
			final int     rx0, rx1;

			x0 = *xr++ * istep;
			x1 = *xr++ * istep;
			XRPOW_FTOI(x0, rx0);
			XRPOW_FTOI(x1, rx1);
			x0 += QUANTFAC(rx0);
			x1 += QUANTFAC(rx1);
			XRPOW_FTOI(x0, *ix++);
			XRPOW_FTOI(x1, *ix++);
		}

	}
*/
// #endif

	/*********************************************************************
	 * Quantization function
	 * This function will select which lines to quantize and call the
	 * proper quantization function
	 *********************************************************************/
	@SuppressWarnings("null")// java: erroneous warning "prev_noise may be null"
	private static final void quantize_xrpow(final float[] xp, final int[] pi, final float istep, final III_GrInfo cod_info,
		final CalcNoiseData prev_noise)
	{
		/* quantize on xr^(3/4) instead of xr */
		int j = 0;
		int accumulate = 0;
		int accumulate01 = 0;

		int xpi = 0;// java xp[xpi]
		int acc_xp = xpi;// xp[acc_xp]
		final int pioffset = 0;// java pi[pioffset]
		int iData = pioffset;// pi[iData]
		int acc_iData = iData;// pi[acc_iData]


		/* Reusing previously computed data does not seems to work if global gain
		   is changed. Finding why it behaves this way would allow to use a cache of
		   previously computed values (let's 10 cached values per sfb) that would
		   probably provide a noticeable speedup */
		final boolean prev_data_use = (prev_noise != null && (cod_info.global_gain == prev_noise.global_gain));

		final int sfbmax = ( cod_info.block_type == Encoder.SHORT_TYPE ) ? 38 : 21;

		for( int sfb = 0; sfb <= sfbmax; sfb++ ) {
			int step = -1;

			if( prev_data_use || cod_info.block_type == Encoder.NORM_TYPE ) {
				step =
						cod_info.global_gain
						- ((cod_info.scalefac[sfb] + (cod_info.preflag ? QuantizePVT.pretab[sfb] : 0))
								<< (cod_info.scalefac_scale + 1))
						- (cod_info.subblock_gain[ cod_info.window[sfb] ] << 3);
			}
			if( prev_data_use && (prev_noise.step[sfb] == step) ) {
				/* do not recompute this part,
				   but compute accumulated lines */
				if( accumulate != 0 ) {
					quantize_lines_xrpow( accumulate, istep, xp, acc_xp, pi, acc_iData );
					accumulate = 0;
				}
				if( accumulate01 != 0 ) {
					quantize_lines_xrpow_01( accumulate01, istep, xp, acc_xp, pi, acc_iData );
					accumulate01 = 0;
				}
			} else {          /*should compute this part */
				int l = cod_info.width[sfb];

				if( (j + cod_info.width[sfb]) > cod_info.max_nonzero_coeff ) {
					/*do not compute upper zero part */
					final int usefullsize = cod_info.max_nonzero_coeff - j + 1;
					for( int i = cod_info.max_nonzero_coeff; i < 576; i++ ) {
						pi[i] = 0;
					}

					l = usefullsize;

					if( l < 0 ) {
						l = 0;
					}

					/* no need to compute higher sfb values */
					sfb = sfbmax + 1;
				}

				/*accumulate lines to quantize */
				if( 0 == accumulate && 0 == accumulate01 ) {
					acc_iData = iData;
					acc_xp = xpi;
				}
				if( prev_noise != null &&
						prev_noise.sfb_count1 > 0 &&
						sfb >= prev_noise.sfb_count1 &&
						prev_noise.step[sfb] > 0 && step >= prev_noise.step[sfb] ) {

					if( accumulate != 0 ) {
						quantize_lines_xrpow( accumulate, istep, xp, acc_xp, pi, acc_iData );
						accumulate = 0;
						acc_iData = iData;
						acc_xp = xpi;
					}
					accumulate01 += l;
				} else {
					if( accumulate01 != 0 ) {
						quantize_lines_xrpow_01( accumulate01, istep, xp, acc_xp, pi, acc_iData );
						accumulate01 = 0;
						acc_iData = iData;
						acc_xp = xpi;
					}
					accumulate += l;
				}

				if( l <= 0 ) {
					/*  rh: 20040215
					 *  may happen due to "prev_data_use" optimization
					 */
					if( accumulate01 != 0 ) {
						quantize_lines_xrpow_01( accumulate01, istep, xp, acc_xp, pi, acc_iData );
						accumulate01 = 0;
					}
					if( accumulate != 0 ) {
						quantize_lines_xrpow( accumulate, istep, xp, acc_xp, pi, acc_iData );
						accumulate = 0;
					}

					break;  /* ends for-loop */
				}
			}
			if( sfb <= sfbmax ) {
				final int w = cod_info.width[sfb];
				iData += w;
				xpi += w;
				j += w;
			}
		}
		if( accumulate != 0 ) {   /*last data part */
			quantize_lines_xrpow( accumulate, istep, xp, acc_xp, pi, acc_iData );
			accumulate = 0;
		}
		if( accumulate01 != 0 ) { /*last data part */
			quantize_lines_xrpow_01( accumulate01, istep, xp, acc_xp, pi, acc_iData );
			accumulate01 = 0;
		}
	}

	/*************************************************************************/
	/*	      ix_max							 */
	/*************************************************************************/
	private static final int ix_max(final int[] ix, int ixoffset, final int end) {
		int max1 = 0, max2 = 0;

		do {
			final int x1 = ix[ixoffset++];
			final int x2 = ix[ixoffset++];
			if( max1 < x1 ) {
				max1 = x1;
			}

			if( max2 < x2 ) {
				max2 = x2;
			}
		} while( ixoffset < end );
		if( max1 < max2 ) {
			max1 = max2;
		}
		return max1;
	}

	private static final long count_bit_ESC(final int[] ix, int ixoffset, final int end, int t1, final int t2, int s) {
		/* ESC-table is used */
		final int linbits = (Tables.ht[t1].xlen << 16) + Tables.ht[t2].xlen;
		int sum = 0, sum2;

		do {
			int x = ix[ixoffset++];
			int y = ix[ixoffset++];

			if( x >= 15 ) {
				x = 15;
				sum += linbits;
			}
			if( y >= 15 ) {
				y = 15;
				sum += linbits;
			}
			x <<= 4;
			x += y;
			sum += Tables.largetbl[x];
		} while( ixoffset < end );

		sum2 = sum & 0xffff;
		sum >>>= 16;

		if( sum > sum2 ) {
			sum = sum2;
			t1 = t2;
		}

		s += sum;
		return (long)t1 | ((long) s << 32);
	}

	private static final long count_bit_noESC(final int[] ix, int ixoffset, final int end, final int mx, int s) {
		/* No ESC-words */
		int sum1 = 0;
		final byte[] hlen1 = Tables.ht[1].hlen;

		do {
			final int x0 = ix[ixoffset++];
			final int x1 = ix[ixoffset++];
			sum1 += hlen1[ x0 + x0 + x1 ];
		} while( ixoffset < end );

		s += sum1;// TODO java: check for neg
		return 1L | ((long)s << 32);
	}

	private static final int huf_tbl_noESC[] = {
		1, 2, 5, 7, 7, 10, 10, 13, 13, 13, 13, 13, 13, 13, 13
	};

	private static final long count_bit_noESC_from2(final int[] ix, int ixoffset, final int end, final int max, int s) {
		int t1 = huf_tbl_noESC[max - 1];
		/* No ESC-words */
		final int xlen = Tables.ht[t1].xlen;
		final int[] table = (t1 == 2) ? Tables.table23 : Tables.table56;
		int sum = 0, sum2;

		do {
			final int x0 = ix[ixoffset++];
			final int x1 = ix[ixoffset++];
			sum += table[ x0 * xlen + x1 ];
		} while( ixoffset < end );

		sum2 = sum & 0xffff;
		sum >>= 16;

		if( sum > sum2 ) {
			sum = sum2;
			t1++;
		}

		s += sum;
		return (long)t1 | ((long)s << 32);
	}

	private static final long count_bit_noESC_from3(final int[] ix, int ixoffset, final int end, final int max, int s) {
		final int t1 = huf_tbl_noESC[max - 1];
		/* No ESC-words */
		int sum1 = 0;
		int sum2 = 0;
		int sum3 = 0;
		final int xlen = Tables.ht[t1].xlen;
		final byte[] hlen1 = Tables.ht[t1].hlen;
		final byte[] hlen2 = Tables.ht[t1 + 1].hlen;
		final byte[] hlen3 = Tables.ht[t1 + 2].hlen;

		do {
			final int x0 = ix[ixoffset++];
			final int x1 = ix[ixoffset++];
			final int x = x0 * xlen + x1;
			sum1 += hlen1[x];
			sum2 += hlen2[x];
			sum3 += hlen3[x];
		} while( ixoffset < end );

		int t = t1;
		if( sum1 > sum2 ) {// TODO java: check for neg
			sum1 = sum2;
			t++;
		}
		if( sum1 > sum3 ) {
			sum1 = sum3;
			t = t1 + 2;
		}
		s += sum1;

		return (long)t | ((long)s << 32);
	}

	/*************************************************************************/
	/*	      choose table						 */
	/*************************************************************************/

	/*
	  Choose the Huffman table that will encode ix[begin..end] with
	  the fewest bits.

	  Note: This code contains knowledge about the sizes and characteristics
	  of the Huffman tables as defined in the IS (Table B.7), and will not work
	  with any arbitrary tables.
	*/
	/* private static final int count_bit_null(final int[] ix, final int start, final int end, final int max, final int[] s) {
		return 0;
	} */
/*
	private static final Jcount_fnc count_fncs[] = {
		&count_bit_null,
		&count_bit_noESC,
		&count_bit_noESC_from2,
		&count_bit_noESC_from2,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3,
		&count_bit_noESC_from3
	};
*/
	/**
	 * @return java: count | (s << 32)
	 */
	private static final long choose_table_nonMMX(final int[] ix, final int start, final int end, final int s) {
		int max = ix_max( ix, start, end );
		if( max <= 15 ) {
			// return count_fncs[max]( ix, start, end, max, s );
			if( max == 0 ) {
				return 0L | ((long)s << 32);// count_bit_null( ix, start, end, max, s );
			}
			if( max == 1 ) {
				return count_bit_noESC( ix, start, end, max, s );
			}
			if( max < 4 ) {
				return count_bit_noESC_from2( ix, start, end, max, s );
			}
			return count_bit_noESC_from3( ix, start, end, max, s );
		}
		/* try tables with linbits */
		if( max > QuantizePVT.IXMAX_VAL ) {
			// s[0] = LARGE_BITS;
			// return -1;
			return 0xffffffffL | ((long)LARGE_BITS << 32);
		}
		max -= 15;
		int choice2 = 24;
		for( ; choice2 < 32; choice2++ ) {
			if( Tables.ht[choice2].linmax >= max ) {
				break;
			}
		}
		int choice = choice2 - 8;
		for( ; choice < 24; choice++ ) {
			if( Tables.ht[choice].linmax >= max ) {
				break;
			}
		}
		return count_bit_ESC( ix, start, end, choice, choice2, s );
	}

	/*************************************************************************/
	/*	      count_bit							 */
	/*************************************************************************/
	static final int noquant_count_bits(final InternalFlags gfc,
										final III_GrInfo gi, final CalcNoiseData prev_noise)
	{
		final SessionConfig cfg = gfc.cfg;
		int bits = 0;
		final int[] ix = gi.l3_enc;

		int i = ((gi.max_nonzero_coeff + 2) >> 1) << 1;
		i = (576 <= i ? 576 : i);

		if( prev_noise != null ) {
			prev_noise.sfb_count1 = 0;
		}

		/* Determine count1 region */
		for( ; i > 1; i -= 2 ) {
			if( (ix[i - 1] | ix[i - 2]) != 0 ) {
				break;
			}
		}
		gi.count1 = i;

		/* Determines the number of bits to encode the quadruples. */
		int a1 = 0, a2 = 0;
		for( ; i > 3; i -= 4 ) {
			final int x4 = ix[i-4];
			final int x3 = ix[i-3];
			final int x2 = ix[i-2];
			final int x1 = ix[i-1];
			/* hack to check if all values <= 1 */
			if( (x4 | x3 | x2 | x1) > 1 ) {// java: ix this is indexes, so only positive numbers
				break;
			}

			final int p = (((((x4 << 1) + x3) << 1) + x2) << 1) + x1;
			a1 += Tables.t32l[p];
			a2 += Tables.t33l[p];
		}

		bits = a1;
		gi.count1table_select = 0;
		if( a1 > a2 ) {
			bits = a2;
			gi.count1table_select = 1;
		}

		gi.count1bits = bits;
		gi.big_values = i;
		if( i == 0 ) {
			return bits;
		}

		if( gi.block_type == Encoder.SHORT_TYPE ) {
			a1 = 3 * gfc.scalefac_band.s[3];
			if( a1 > gi.big_values ) {
				a1 = gi.big_values;
			}
			a2 = gi.big_values;

		} else if( gi.block_type == Encoder.NORM_TYPE ) {
			a1 = gi.region0_count = gfc.sv_qnt.bv_scf[i - 2];
			a2 = gi.region1_count = gfc.sv_qnt.bv_scf[i - 1];

			a2 = gfc.scalefac_band.l[a1 + a2 + 2];
			a1 = gfc.scalefac_band.l[a1 + 1];
			if( a2 < i ) {
				final long tmp = choose_table_nonMMX( ix, a2, i, bits );
				gi.table_select[2] = (int)tmp;
				bits = (int)(tmp >> 32);
			}

		} else {
			gi.region0_count = 7;
			/*gi.region1_count = SBPSY_l - 7 - 1; */
			gi.region1_count = Encoder.SBMAX_l - 1 - 7 - 1;
			a1 = gfc.scalefac_band.l[7 + 1];
			a2 = i;
			if( a1 > a2 ) {
				a1 = a2;
			}
		}

		/* have to allow for the case when bigvalues < region0 < region1 */
		/* (and region0, region1 are ignored) */
		a1 = (a1 <= i ? a1 : i);
		a2 = (a2 <= i ? a2 : i);

		/* Count the number of bits necessary to code the bigvalues region. */
		if( 0 < a1 ) {
			final long tmp = choose_table_nonMMX( ix, 0, a1, bits );
			gi.table_select[0] = (int)tmp;
			bits = (int)(tmp >> 32);
		}
		if( a1 < a2 ) {
			final long tmp = choose_table_nonMMX( ix, a1, a2, bits );
			gi.table_select[1] = (int)tmp;
			bits = (int)(tmp >> 32);
		}
		if( cfg.use_best_huffman == 2 ) {
			gi.part2_3_length = bits;
			best_huffman_divide(gfc, gi);
			bits = gi.part2_3_length;
		}

		if( prev_noise != null ) {
			if( gi.block_type == Encoder.NORM_TYPE ) {
				final int[] band_l = gfc.scalefac_band.l;// java
				int sfb = 0;
				while( band_l[sfb] < gi.big_values ) {
					sfb++;
				}
				prev_noise.sfb_count1 = sfb;
			}
		}

		return bits;
	}

	static final int count_bits(final InternalFlags gfc,
								final float[] xr, final III_GrInfo gi, final CalcNoiseData prev_noise)
	{
		final int[] ix = gi.l3_enc;

		/* since quantize_xrpow uses table lookup, we need to check this first: */
		final float w = (QuantizePVT.IXMAX_VAL) / QuantizePVT.ipow20[ gi.global_gain ];

		if( gi.xrpow_max > w ) {
			return LARGE_BITS;
		}

		quantize_xrpow( xr, ix, QuantizePVT.ipow20[gi.global_gain], gi, prev_noise );

		if( (gfc.sv_qnt.substep_shaping & 2) != 0 ) {
			int j = 0;
			/* 0.634521682242439 = 0.5946*2**(.5*0.1875) */
			final int gain = gi.global_gain + gi.scalefac_scale;
			final float roundfac = 0.634521682242439f / QuantizePVT.ipow20[ gain ];
			final boolean[] pseudohalf = gfc.sv_qnt.pseudohalf;// java
			final int[] gi_width = gi.width;// java
			for( int sfb = 0, sfbmax = gi.sfbmax; sfb < sfbmax; sfb++ ) {
				final int width = gi_width[sfb];

				if( ! pseudohalf[sfb] ) {
					j += width;
				} else {
					int k;
					for( k = j, j += width; k < j; ++k ) {
						ix[k] = (xr[k] >= roundfac) ? ix[k] : 0;
					}
				}
			}
		}
		return noquant_count_bits( gfc, gi, prev_noise );
	}

	/***********************************************************************
	  re-calculate the best scalefac_compress using scfsi
	  the saved bits are kept in the bit reservoir.
	 **********************************************************************/
	private static final void recalc_divide_init(final InternalFlags gfc,
												 final III_GrInfo cod_info, final int[] ix, final int r01_bits[], final int r01_div[], final int r0_tbl[], final int r1_tbl[])
	{
		final int bigv = cod_info.big_values;

		for( int r0 = 0; r0 <= 7 + 15; r0++ ) {
			r01_bits[r0] = LARGE_BITS;
		}

		for( int r0 = 0; r0 < 16; r0++ ) {
			final int a1 = gfc.scalefac_band.l[r0 + 1];
			if( a1 >= bigv ) {
				break;
			}
			int r0bits = 0;
			long tmp = choose_table_nonMMX( ix, 0, a1, r0bits );
			final int r0t = (int)tmp;
			r0bits = (int)(tmp >> 32);

			for( int r1 = r0, re = r0 + 8; r1 < re; r1++ ) {
				final int a2 = gfc.scalefac_band.l[r1 + 2];
				if( a2 >= bigv ) {
					break;
				}

				int bits = r0bits;
				tmp = choose_table_nonMMX( ix, a1, a2, bits );
				final int r1t = (int)tmp;
				bits = (int)(tmp >> 32);
				if( r01_bits[r1] > bits ) {
					r01_bits[r1] = bits;
					r01_div[r1] = r0;
					r0_tbl[r1] = r0t;
					r1_tbl[r1] = r1t;
				}
			}
		}
	}

	private static final void recalc_divide_sub(final InternalFlags gfc,
		final III_GrInfo cod_info2,
		final III_GrInfo gi,
		final int[] ix,
		final int r01_bits[], final int r01_div[], final int r0_tbl[], final int r1_tbl[])
	{
		final int bigv = cod_info2.big_values;

		final int[] table_select = gi.table_select;// java

		for(int r2 = 2, r = 0; r2 < Encoder.SBMAX_l + 1; r2++, r++ ) {
			final int a2 = gfc.scalefac_band.l[r2];
			if( a2 >= bigv ) {
				break;
			}

			int bits = r01_bits[r] + cod_info2.count1bits;
			if( gi.part2_3_length <= bits ) {
				break;
			}

			final long tmp = choose_table_nonMMX( ix, a2, bigv, bits );
			final int r2t = (int)tmp;
			bits = (int)(tmp >> 32);
			if( gi.part2_3_length <= bits ) {
				continue;
			}

			gi.copyFrom( cod_info2 );
			gi.part2_3_length = bits;
			gi.region0_count = r01_div[r];
			gi.region1_count = r - r01_div[r];
			table_select[0] = r0_tbl[r];
			table_select[1] = r1_tbl[r];
			table_select[2] = r2t;
		}
	}

	static final void best_huffman_divide(final InternalFlags gfc, final III_GrInfo gi) {
		final SessionConfig cfg = gfc.cfg;
		final int[] ix = gi.l3_enc;

		final int r01_bits[] = new int[7 + 15 + 1];
		final int r01_div[] = new int[7 + 15 + 1];
		final int r0_tbl[] = new int[7 + 15 + 1];
		final int r1_tbl[] = new int[7 + 15 + 1];


		/* SHORT BLOCK stuff fails for MPEG2 */
		if( gi.block_type == Encoder.SHORT_TYPE && cfg.mode_gr == 1 ) {
			return;
		}

		final III_GrInfo cod_info2 = new III_GrInfo( gi );
		if( gi.block_type == Encoder.NORM_TYPE ) {
			recalc_divide_init( gfc, gi, ix, r01_bits, r01_div, r0_tbl, r1_tbl );
			recalc_divide_sub( gfc, cod_info2, gi, ix, r01_bits, r01_div, r0_tbl, r1_tbl );
		}

		final int big_values = cod_info2.big_values;// java
		int i = big_values;
		if( i == 0 || (ix[i - 2] | ix[i - 1]) > 1 ) {// java: ix is positive numbers
			return;
		}

		i = gi.count1 + 2;
		if( i > 576 ) {
			return;
		}

		/* Determines the number of bits to encode the quadruples. */
		cod_info2.copyFrom( gi );
		cod_info2.count1 = i;
		int a1 = 0, a2 = 0;

		for( ; i > big_values; i -= 4 ) {
			final int p = (((ix[i - 4] << 1) + (ix[i - 3]) << 1) + (ix[i - 2]) << 1) + ix[i - 1];
			a1 += Tables.t32l[p];
			a2 += Tables.t33l[p];
		}
		cod_info2.big_values = i;

		cod_info2.count1table_select = 0;
		if( a1 > a2 ) {
			a1 = a2;
			cod_info2.count1table_select = 1;
		}

		cod_info2.count1bits = a1;

		if( cod_info2.block_type == Encoder.NORM_TYPE ) {
			recalc_divide_sub( gfc, cod_info2, gi, ix, r01_bits, r01_div, r0_tbl, r1_tbl );
		} else {
			/* Count the number of bits necessary to code the bigvalues region. */
			cod_info2.part2_3_length = a1;
			a1 = gfc.scalefac_band.l[7 + 1];
			if( a1 > i ) {
				a1 = i;
			}
			if( a1 > 0 ) {
				final long tmp = choose_table_nonMMX( ix, 0, a1, cod_info2.part2_3_length );
				cod_info2.table_select[0] = (int)tmp;
				cod_info2.part2_3_length = (int)(tmp >> 32);
			}
			if( i > a1 ) {
				final long tmp = choose_table_nonMMX( ix, a1, i, cod_info2.part2_3_length );
				cod_info2.table_select[1] = (int)tmp;
				cod_info2.part2_3_length = (int)(tmp >> 32);
			}
			if( gi.part2_3_length > cod_info2.part2_3_length ) {
				gi.copyFrom( cod_info2 );
			}
		}
	}

	private static final int slen1_n[/*16*/] = { 1, 1, 1, 1, 8, 2, 2, 2, 4, 4, 4, 8, 8, 8, 16, 16 };
	private static final int slen2_n[/*16*/] = { 1, 2, 4, 8, 1, 2, 4, 8, 2, 4, 8, 2, 4, 8, 4, 8 };
	static int slen1_tab[/*16*/] = { 0, 0, 0, 0, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4 };
	static int slen2_tab[/*16*/] = { 0, 1, 2, 3, 0, 1, 2, 3, 1, 2, 3, 1, 2, 3, 2, 3 };

	private static final void scfsi_calc(final int ch, final III_SideInfo l3_side) {
		final III_GrInfo gi = l3_side.tt[1][ch];
		final int[] scalefac0 = l3_side.tt[0][ch].scalefac;
		final int[] scfsi_ch = l3_side.scfsi[ch];// java
		final int[] scalefac = gi.scalefac;// java

		int sfb;
		for(int i = 0; i < Tables.scfsi_band.length - 1; i++ ) {
			final int be = Tables.scfsi_band[i + 1];// java
			for(sfb = Tables.scfsi_band[i]; sfb < be; sfb++ ) {
				if( scalefac0[sfb] != scalefac[sfb] && scalefac[sfb] >= 0 ) {
					break;
				}
			}
			if( sfb == be ) {
				for(sfb = Tables.scfsi_band[i]; sfb < be; sfb++ ) {
					scalefac[sfb] = -1;
				}
				scfsi_ch[i] = 1;
			}
		}

		int s1 = 0, c1 = 0;
		for( sfb = 0; sfb < 11; sfb++ ) {
			if( scalefac[sfb] == -1 ) {
				continue;
			}
			c1++;
			if( s1 < scalefac[sfb] ) {
				s1 = scalefac[sfb];
			}
		}

		int s2 = 0, c2 = 0;
		for(; sfb < Encoder.SBPSY_l; sfb++ ) {
			if( scalefac[sfb] == -1 ) {
				continue;
			}
			c2++;
			if( s2 < scalefac[sfb] ) {
				s2 = scalefac[sfb];
			}
		}

		for( int i = 0; i < 16; i++ ) {
			if( s1 < slen1_n[i] && s2 < slen2_n[i] ) {
				final int c = slen1_tab[i] * c1 + slen2_tab[i] * c2;
				if( gi.part2_length > c ) {
					gi.part2_length = c;
					gi.scalefac_compress = (int)i;
				}
			}
		}
	}

	/*
	Find the optimal way to store the scalefactors.
	Only call this routine after final scalefactors have been
	chosen and the channel/granule will not be re-encoded.
	 */
	static final void best_scalefac_store(final InternalFlags gfc,
		final int gr, final int ch, final III_SideInfo l3_side)
	{
		final SessionConfig cfg = gfc.cfg;
		final III_GrInfo[][] tt = l3_side.tt;// java
		/* use scalefac_scale if we can */
		final III_GrInfo gi = tt[gr][ch];
		final int sfbmax = gi.sfbmax;// java
		boolean recalc = false;

		/* remove scalefacs from bands with ix=0.  This idea comes
		 * from the AAC ISO docs.  added mt 3/00 */
		/* check if l3_enc=0 */
		final int[] scalefac = gi.scalefac;// java
		int j = 0;
		for( int sfb = 0; sfb < sfbmax; sfb++ ) {
			final int width = gi.width[sfb];
			int l;
			for( l = j, j += width; l < j; ++l ) {
				if( gi.l3_enc[l] != 0 ) {
					break;
				}
			}
			if( l == j ) {
				scalefac[sfb] = /*recalc =*/ -2;
				recalc = true;
			} /* anything goes. */
			/*  only best_scalefac_store and calc_scfsi
			 *  know--and only they should know--about the magic number -2.
			 */
		}

		if( 0 == gi.scalefac_scale && ! gi.preflag ) {
			int s = 0;
			for( int sfb = 0; sfb < sfbmax; sfb++ ) {
				if( scalefac[sfb] > 0 ) {
					s |= scalefac[sfb];
				}
			}

			if( 0 == (s & 1) && s != 0 ) {
				for( int sfb = 0; sfb < sfbmax; sfb++ ) {
					if( scalefac[sfb] > 0 ) {
						scalefac[sfb] >>= 1;
					}
				}

				gi.scalefac_scale = /* recalc = */ 1;
				recalc = true;
			}
		}

		if( !gi.preflag && gi.block_type != Encoder.SHORT_TYPE && cfg.mode_gr == 2 ) {
			int sfb;
			for(sfb = 11; sfb < Encoder.SBPSY_l; sfb++ ) {
				if( scalefac[sfb] < QuantizePVT.pretab[sfb] && scalefac[sfb] != -2 ) {
					break;
				}
			}
			if( sfb == Encoder.SBPSY_l ) {
				for(sfb = 11; sfb < Encoder.SBPSY_l; sfb++ ) {
					if( scalefac[sfb] > 0) {
						scalefac[sfb] -= QuantizePVT.pretab[sfb];
					}
				}

				gi.preflag = recalc = true;
			}
		}

		for( int i = 0; i < 4; i++ ) {
			l3_side.scfsi[ch][i] = 0;
		}

		if( cfg.mode_gr == 2 && gr == 1
				&& tt[0][ch].block_type != Encoder.SHORT_TYPE
				&& tt[1][ch].block_type != Encoder.SHORT_TYPE ) {
			scfsi_calc( ch, l3_side );
			recalc = false;
		}
		for( int sfb = 0; sfb < sfbmax; sfb++ ) {
			if( scalefac[sfb] == -2 ) {
				scalefac[sfb] = 0; /* if anything goes, then 0 is a good choice */
			}
		}
		if( recalc ) {
			scale_bitcount( gfc, gi );
		}
	}

/* #ifndef NDEBUG
	private static final int all_scalefactors_not_negative(final int[] scalefac, final int n) {
		for( int i = 0; i < n; ++i ) {
			if( scalefac[i] < 0 ) {
				return 0;
			}
		}
		return 1;
	}
#endif */

	/* number of bits used to encode scalefacs */

	/* 18*slen1_tab[i] + 18*slen2_tab[i] */
	private static final int scale_short[/*16*/] = {
			0, 18, 36, 54, 54, 36, 54, 72, 54, 72, 90, 72, 90, 108, 108, 126
	};

	/* 17*slen1_tab[i] + 18*slen2_tab[i] */
	private static final int scale_mixed[/*16*/] = {
			0, 18, 36, 54, 51, 35, 53, 71, 52, 70, 88, 69, 87, 105, 104, 122
	};

	/* 11*slen1_tab[i] + 10*slen2_tab[i] */
	private static final int scale_long[/*16*/] = {
			0, 10, 20, 30, 33, 21, 31, 41, 32, 42, 52, 43, 53, 63, 64, 74
	};

	/*************************************************************************/
	/*            scale_bitcount                                             */
	/*************************************************************************/

	/* Also calculates the number of bits necessary to code the scalefactors. */

	private static final boolean mpeg1_scale_bitcount(final InternalFlags gfc, final III_GrInfo cod_info) {
		int  max_slen1 = 0, max_slen2 = 0;

		/* maximum values */
		int[] tab;
		final int[] scalefac = cod_info.scalefac;

		// assert(all_scalefactors_not_negative(scalefac, cod_info.sfbmax));

		int sfb;
		if( cod_info.block_type == Encoder.SHORT_TYPE ) {
			tab = scale_short;
			if( cod_info.mixed_block_flag ) {
				tab = scale_mixed;
			}
		} else {              /* block_type == 1,2,or 3 */
			tab = scale_long;
			if( ! cod_info.preflag ) {
				for(sfb = 11; sfb < Encoder.SBPSY_l; sfb++ ) {
					if( scalefac[sfb] < QuantizePVT.pretab[sfb] ) {
						break;
					}
				}

				if( sfb == Encoder.SBPSY_l ) {
					cod_info.preflag = true;
					for(sfb = 11; sfb < Encoder.SBPSY_l; sfb++ ) {
						scalefac[sfb] -= QuantizePVT.pretab[sfb];
					}
				}
			}
		}

		for( sfb = 0; sfb < cod_info.sfbdivide; sfb++ ) {
			if( max_slen1 < scalefac[sfb]) {
				max_slen1 = scalefac[sfb];
			}
		}

		for( ; sfb < cod_info.sfbmax; sfb++) {
			if( max_slen2 < scalefac[sfb]) {
				max_slen2 = scalefac[sfb];
			}
		}

		/* from Takehiro TOMINAGA <tominaga@isoternet.org> 10/99
		 * loop over *all* posible values of scalefac_compress to find the
		 * one which uses the smallest number of bits.  ISO would stop
		 * at first valid index */
		cod_info.part2_length = LARGE_BITS;
		for( int k = 0; k < 16; k++ ) {
			if( max_slen1 < slen1_n[k] && max_slen2 < slen2_n[k]
					&& cod_info.part2_length > tab[k] ) {
				cod_info.part2_length = tab[k];
				cod_info.scalefac_compress = k;
			}
		}
		return cod_info.part2_length == LARGE_BITS;
	}

	/* table of largest scalefactor values for MPEG2 */
	private static final int max_range_sfac_tab[][] = {// [6][4] = {
		{15, 15, 7, 7},
		{15, 15, 7, 0},
		{7, 3, 0, 0},
		{15, 31, 31, 0},
		{7, 7, 7, 0},
		{3, 3, 0, 0}
	};

	/**
	   Since no bands have been over-amplified, we can set scalefac_compress
	   and slen[] for the formatter
	 */
	private static final int log2tab[] = { 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4 };
	/*************************************************************************/
	/*            scale_bitcount_lsf                                         */
	/*************************************************************************/

	/* Also counts the number of bits to encode the scalefacs but for MPEG 2 */
	/* Lower sampling frequencies  (24, 22.05 and 16 kHz.)                   */

	/*  This is reverse-engineered from section 2.4.3.2 of the MPEG2 IS,     */
	/* "Audio Decoding Layer III"                                            */
	private static final boolean mpeg2_scale_bitcount(final InternalFlags gfc, final III_GrInfo cod_info) {
		final int max_sfac[] = new int[4];
		int[] partition_table;
		final int[] scalefac = cod_info.scalefac;

		/*
		   Set partition table. Note that should try to use table one,
		   but do not yet...
		 */
		final int table_number = cod_info.preflag ? 2 : 0;

		for( int i = 0; i < 4; i++ ) {
			max_sfac[i] = 0;
		}

		int row_in_table;
		if( cod_info.block_type == Encoder.SHORT_TYPE ) {
			row_in_table = 1;
			partition_table = QuantizePVT.nr_of_sfb_block[table_number][row_in_table];//[0];
			for( int sfb = 0, partition = 0; partition < 4; partition++ ) {
				final int nr_sfb = partition_table[partition];
				for( final int end = sfb + nr_sfb; sfb < end; ) {
					if( scalefac[sfb] > max_sfac[partition]) {
						max_sfac[partition] = scalefac[sfb];
					}
					sfb++;
					if( scalefac[sfb] > max_sfac[partition]) {
						max_sfac[partition] = scalefac[sfb];
					}
					sfb++;
					if( scalefac[sfb] > max_sfac[partition]) {
						max_sfac[partition] = scalefac[sfb];
					}
					sfb++;
				}
			}
		} else {
			row_in_table = 0;
			partition_table = QuantizePVT.nr_of_sfb_block[table_number][row_in_table];//[0];
			for( int sfb = 0, partition = 0; partition < 4; partition++ ) {
				final int nr_sfb = partition_table[partition];
				for( int i = 0; i < nr_sfb; i++, sfb++ ) {
					if( scalefac[sfb] > max_sfac[partition] ) {
						max_sfac[partition] = scalefac[sfb];
					}
				}
			}
		}

		int over = 0;
		for( int partition = 0; partition < 4; partition++ ) {
			if( max_sfac[partition] > max_range_sfac_tab[table_number][partition] ) {
				over++;
			}
		}
		if( 0 == over ) {
			final int[] slen = cod_info.slen;// java
			/*
			   Since no bands have been over-amplified, we can set scalefac_compress
			   and slen[] for the formatter
			 */
			// static final int log2tab[] = { 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4 };

			cod_info.sfb_partition_table = QuantizePVT.nr_of_sfb_block[table_number][row_in_table];

			/* set scalefac_compress */
			final int slen1 = log2tab[ max_sfac[0] ];
			final int slen2 = log2tab[ max_sfac[1] ];
			final int slen3 = log2tab[ max_sfac[2] ];
			final int slen4 = log2tab[ max_sfac[3] ];

			slen[0] = slen1;
			slen[1] = slen2;
			slen[2] = slen3;
			slen[3] = slen4;

			switch( table_number ) {
			case 0:
				cod_info.scalefac_compress = (((slen1 * 5) + slen2) << 4) + (slen3 << 2) + slen4;
				break;

			case 1:
				cod_info.scalefac_compress = 400 + (((slen1 * 5) + slen2) << 2) + slen3;
				break;

			case 2:
				cod_info.scalefac_compress = 500 + (slen1 * 3) + slen2;
				break;

			default:
				//System.err.print("intensity stereo not implemented yet\n");
				break;
			}
		//}
		//if( 0 == over ) {
			cod_info.part2_length  = slen1 * cod_info.sfb_partition_table[0];
			cod_info.part2_length += slen2 * cod_info.sfb_partition_table[1];
			cod_info.part2_length += slen3 * cod_info.sfb_partition_table[2];
			cod_info.part2_length += slen4 * cod_info.sfb_partition_table[3];
		}
		return over != 0;
	}

	static final boolean scale_bitcount(final InternalFlags gfc, final III_GrInfo cod_info) {
		if( gfc.cfg.mode_gr == 2 ) {
			return mpeg1_scale_bitcount( gfc, cod_info );
		}// else {
			return mpeg2_scale_bitcount( gfc, cod_info );
		//}
	}

	static final void huffman_init(final InternalFlags gfc) {
		// gfc.choose_table = choose_table_nonMMX;
		final int[] band_l = gfc.scalefac_band.l;// java
		final byte[] bv_scf = gfc.sv_qnt.bv_scf;// java

		for( int i = 2; i <= 576; i += 2 ) {
			int scfb_anz = 0;
			while( band_l[++scfb_anz] < i ) {
				;
			}

			int bv_index = subdv_table[scfb_anz].region0_count;
			while( band_l[bv_index + 1] > i ) {
				bv_index--;
			}

			if( bv_index < 0 ) {
				/* this is an indication that everything is going to
				   be encoded as region0:  bigvalues < region0 < region1
				   so lets set region0, region1 to some value larger
				   than bigvalues */
				bv_index = subdv_table[scfb_anz].region0_count;
			}

			bv_scf[i - 2] = (byte)bv_index;

			bv_index = subdv_table[scfb_anz].region1_count;
			while( band_l[bv_index + bv_scf[i - 2] + 2] > i ) {
				bv_index--;
			}

			if( bv_index < 0 ) {
				bv_index = subdv_table[scfb_anz].region1_count;
			}

			bv_scf[i - 1] = (byte)bv_index;
		}
	}
}
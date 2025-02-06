package net.sourceforge.lame;

import net.sourceforge.lame.MpgSideInfo.CH;

// layer3.c

class Layer3 {

	private static boolean gd_are_hip_tables_layer3_initialized = false;

	private static final float ispow[] = new float[8207];
	private static final float aa_ca[] = new float[8];
	private static final float aa_cs[] = new float[8];
	private static final float COS1[][] = new float[12][6];
	private static final float win[][] = new float[4][36];
	private static final float win1[][] = new float[4][36];
	private static final float gainpow2[] = new float[256 + 118 + 4];
	private static final float COS9[] = new float[9];
	private static float COS6_1, COS6_2;
	private static final float tfcos36[] = new float[9];
	private static final float tfcos12[] = new float[3];

	private static final class BandInfoStruct {
		private final short longIdx[];// = new short[23];
		private final short longDiff[];// = new short[22];
		private final short shortIdx[];// = new short[14];
		private final short shortDiff[];// = new short[13];
		//
		private BandInfoStruct(final short[] lidx, final short[] ldiff, final short[] sidx, final short[] sdiff ) {
			this.longIdx = lidx;
			this.longDiff = ldiff;
			this.shortIdx = sidx;
			this.shortDiff = sdiff;
		}
	}

	private static final int longLimit[][] = new int[9][23];
	private static final int shortLimit[][] = new int[9][14];

	private static final BandInfoStruct bandInfo[] = {// [9] = {

		/* MPEG 1.0 */
		new BandInfoStruct( new short[]{0,4,8,12,16,20,24,30,36,44,52,62,74, 90,110,134,162,196,238,288,342,418,576},
			new short[]{4,4,4,4,4,4,6,6,8, 8,10,12,16,20,24,28,34,42,50,54, 76,158},
			new short[]{0,4*3,8*3,12*3,16*3,22*3,30*3,40*3,52*3,66*3, 84*3,106*3,136*3,192*3},
			new short[]{4,4,4,4,6,8,10,12,14,18,22,30,56} ),

		new BandInfoStruct( new short[]{0,4,8,12,16,20,24,30,36,42,50,60,72, 88,106,128,156,190,230,276,330,384,576},
			new short[]{4,4,4,4,4,4,6,6,6, 8,10,12,16,18,22,28,34,40,46,54, 54,192},
			new short[]{0,4*3,8*3,12*3,16*3,22*3,28*3,38*3,50*3,64*3, 80*3,100*3,126*3,192*3},
			new short[]{4,4,4,4,6,6,10,12,14,16,20,26,66} ),

		new BandInfoStruct( new short[]{0,4,8,12,16,20,24,30,36,44,54,66,82,102,126,156,194,240,296,364,448,550,576} ,
			new short[]{4,4,4,4,4,4,6,6,8,10,12,16,20,24,30,38,46,56,68,84,102, 26} ,
			new short[]{0,4*3,8*3,12*3,16*3,22*3,30*3,42*3,58*3,78*3,104*3,138*3,180*3,192*3} ,
			new short[]{4,4,4,4,6,8,12,16,20,26,34,42,12} ),

		/* MPEG 2.0 */
		new BandInfoStruct( new short[]{0,6,12,18,24,30,36,44,54,66,80,96,116,140,168,200,238,284,336,396,464,522,576},
			new short[]{6,6,6,6,6,6,8,10,12,14,16,20,24,28,32,38,46,52,60,68,58,54 } ,
			new short[]{0,4*3,8*3,12*3,18*3,24*3,32*3,42*3,56*3,74*3,100*3,132*3,174*3,192*3} ,
			new short[]{4,4,4,6,6,8,10,14,18,26,32,42,18 } ),
									 /* docs: 332. mpg123: 330 */
		new BandInfoStruct( new short[]{0,6,12,18,24,30,36,44,54,66,80,96,114,136,162,194,232,278,332,394,464,540,576},
			new short[]{6,6,6,6,6,6,8,10,12,14,16,18,22,26,32,38,46,54,62,70,76,36 } ,
			new short[]{0,4*3,8*3,12*3,18*3,26*3,36*3,48*3,62*3,80*3,104*3,136*3,180*3,192*3} ,
			new short[]{4,4,4,6,8,10,12,14,18,24,32,44,12 } ),

		new BandInfoStruct( new short[]{0,6,12,18,24,30,36,44,54,66,80,96,116,140,168,200,238,284,336,396,464,522,576},
			new short[]{6,6,6,6,6,6,8,10,12,14,16,20,24,28,32,38,46,52,60,68,58,54 },
			new short[]{0,4*3,8*3,12*3,18*3,26*3,36*3,48*3,62*3,80*3,104*3,134*3,174*3,192*3},
			new short[]{4,4,4,6,8,10,12,14,18,24,30,40,18 } ),
		/* MPEG 2.5 */
		new BandInfoStruct( new short[]{0,6,12,18,24,30,36,44,54,66,80,96,116,140,168,200,238,284,336,396,464,522,576} ,
			new short[]{6,6,6,6,6,6,8,10,12,14,16,20,24,28,32,38,46,52,60,68,58,54},
			new short[]{0,12,24,36,54,78,108,144,186,240,312,402,522,576},
			new short[]{4,4,4,6,8,10,12,14,18,24,30,40,18} ),
		new BandInfoStruct( new short[]{0,6,12,18,24,30,36,44,54,66,80,96,116,140,168,200,238,284,336,396,464,522,576} ,
			new short[]{6,6,6,6,6,6,8,10,12,14,16,20,24,28,32,38,46,52,60,68,58,54},
			new short[]{0,12,24,36,54,78,108,144,186,240,312,402,522,576},
			new short[]{4,4,4,6,8,10,12,14,18,24,30,40,18} ),
		new BandInfoStruct( new short[]{0,12,24,36,48,60,72,88,108,132,160,192,232,280,336,400,476,566,568,570,572,574,576},
			new short[]{12,12,12,12,12,12,16,20,24,28,32,40,48,56,64,76,90,2,2,2,2,2},
			new short[]{0, 24, 48, 72,108,156,216,288,372,480,486,492,498,576},
			new short[]{8,8,8,12,16,20,24,28,36,2,2,2,26} )
	};

	private static final int mapbuf0[][] = new int[9][152];
	private static final int mapbuf1[][] = new int[9][156];
	private static final int mapbuf2[][] = new int[9][44];
	private static final int map[][][] = new int[9][3][];
	private static final int mapend[][] = new int[9][3];

	private static final int n_slen2[] = new int[512]; /* MPEG 2.0 slen for 'normal' mode */
	private static final int i_slen2[] = new int[256]; /* MPEG 2.0 slen for intensity stereo */

	private static final float tan1_1[] = new float[16];
	private static final float tan2_1[] = new float[16];
	private static final float tan1_2[] = new float[16];
	private static final float tan2_2[] = new float[16];
	private static final float pow1_1[][] = new float[2][16];
	private static final float pow2_1[][] = new float[2][16];
	private static final float pow1_2[][] = new float[2][16];
	private static final float pow2_2[][] = new float[2][16];

	private static final int get1bit(final MpStrTag mp) {
		final int rval = (int)mp.wordbuf[ mp.wordpointer ] << mp.bitindex;

		mp.bitindex++;
		mp.wordpointer += (mp.bitindex >> 3);
		mp.bitindex &= 7;

		return (rval >> 7) & 1;
	}

	/* FIXME ugly code.
	// java: replaced by ArrayIndexException
	private static final float get_gain(float[] gain_ptr, int idx, int[] overflow)
	{
		static float[] gainpow2_end_ptr = gainpow2 + gainpow2.length - 1;
		float[] ptr = &gain_ptr[idx];
		if( &gain_ptr[idx] > gainpow2_end_ptr ) {
			ptr = gainpow2_end_ptr;
			if( overflow != null ) overflow[0] = 1;
		}
		return *ptr;
	}*/

	/** init tables for layer-3 */
	static final void hip_init_tables_layer3() {

		if( gd_are_hip_tables_layer3_initialized ) {
			return;
		}
		gd_are_hip_tables_layer3_initialized = true;

		for( int i = -256; i < 118 + 4; i++ ) {
			gainpow2[i + 256] = (float)Math.pow( 2.0, -0.25 * (double) (i + 210));
		}

		for( int i = 0; i < 8207; i++ ) {
			ispow[i] = (float)Math.pow( (double) i, 4.0 / 3.0 );
		}

		final double Ci[] = { -0.6, -0.535, -0.33, -0.185, -0.095, -0.041, -0.0142, -0.0037 };
		for( int i = 0; i < 8; i++ ) {
			final double c = Ci[i];
			final double sq = Math.sqrt( 1.0 + c * c );
			aa_cs[i] = (float)(1.0 / sq);
			aa_ca[i] = (float)(c / sq);
		}

		for( int i = 0; i < 18; i++ ) {
			int i2 = i << 1;
			win[0][i] = win[1][i] = (float)
					(0.5 * Math.sin( Math.PI / 72.0 * (double) (i2 + 1)) / Math.cos( Math.PI * (double) (i2 + 19) / 72.0 ));
			i2 += 2 * 18;
			win[0][i + 18] = win[3][i + 18] = (float)
					(0.5 * Math.sin( Math.PI / 72.0 * (double) (i2 + 1)) / Math.cos( Math.PI * (double) (i2 + 19) / 72.0));
		}
		for( int i = 0; i < 6; i++ ) {
			win[1][i + 18] = (float)(0.5 / Math.cos( Math.PI * (double) (2 * (i + 18) + 19) / 72.0));
			win[3][i + 12] = (float)(0.5 / Math.cos( Math.PI * (double) (2 * (i + 12) + 19) / 72.0));
			win[1][i + 24] = (float)(0.5 * Math.sin( Math.PI / 24.0 * (double) (2 * i + 13)) / Math.cos( Math.PI * (double) (2 * (i + 24) + 19) / 72.0));
			win[1][i + 30] = win[3][i] = 0.0f;
			win[3][i + 6] = (float)(0.5 * Math.sin( Math.PI / 24.0 * (double) (2 * i + 1)) / Math.cos( Math.PI * (double) (2 * (i + 6) + 19) / 72.0));
		}

		for( int i = 0; i < 9; i++ ) {
			COS9[i] = (float)Math.cos( Math.PI / 18.0 * (double) i);
		}

		for( int i = 0; i < 9; i++ ) {
			tfcos36[i] = (float)(0.5 / Math.cos( Math.PI * (double) ((i << 1) + 1) / 36.0));
		}
		for( int i = 0; i < 3; i++ ) {
			tfcos12[i] = (float)(0.5 / Math.cos( Math.PI * (double) ((i << 1) + 1) / 12.0));
		}

		COS6_1 = (float)Math.cos( Math.PI / 6.0 * (double) 1);
		COS6_2 = (float)Math.cos( Math.PI / 6.0 * (double) 2);

		for( int i = 0; i < 12; i++ ) {
			final int i2 = i << 1;
			win[2][i] = (float)(0.5 * Math.sin( Math.PI / 24.0 * (double) (i2 + 1)) / Math.cos( Math.PI * (double) (i2 + 7) / 24.0));
			for( int j = 0; j < 6; j++ ) {
				COS1[i][j] = (float)(Math.cos( Math.PI / 24.0 * (double) ((i2 + 7) * ((j << 1) + 1))));
			}
		}

		final int len[] = { 36, 36, 12, 36 };
		for( int j = 0; j < 4; j++ ) {
			for( int i = 0; i < len[j]; i += 2 ) {
				win1[j][i] = +win[j][i];
			}
			for( int i = 1; i < len[j]; i += 2 ) {
				win1[j][i] = -win[j][i];
			}
		}

		for( int i = 0; i < 16; i++ ) {
			final double t = Math.tan((double) i * Math.PI / 12.0);
			tan1_1[i] = (float)(t / (1.0 + t));
			tan2_1[i] = (float)(1.0 / (1.0 + t));
			tan1_2[i] = (float)(Mpg123.M_SQRT2 * t / (1.0 + t));
			tan2_2[i] = (float)(Mpg123.M_SQRT2 / (1.0 + t));

			for( int j = 0; j < 2; j++ ) {
				final double base = Math.pow( 2.0, -0.25 * (j + 1.0) );
				double p1 = 1.0, p2 = 1.0;
				if( i > 0 ) {
					if( (i & 1) != 0 ) {
						p1 = Math.pow( base, (i + 1.0) * 0.5 );
					} else {
						p2 = Math.pow( base, i * 0.5 );
					}
				}
				pow1_1[j][i] = (float)p1;
				pow2_1[j][i] = (float)p2;
				pow1_2[j][i] = (float)(Mpg123.M_SQRT2 * p1);
				pow2_2[j][i] = (float)(Mpg123.M_SQRT2 * p2);
			}
		}

		for( int j = 0; j < 9; j++ ) {
			final BandInfoStruct bi = bandInfo[j];
			final int switch_idx = (j < 3) ? 8 : 6;

			int[] mp = map[j][0] = mapbuf0[j];
			int mpi = 0;// mp [ mpi ]
			short[] diff = bi.longDiff;
			int bdf = 0;// long_diff[ bdf ]
			int i = 0, cb;
			for( cb = 0; cb < switch_idx; cb++, i += diff[ bdf++ ] ) {
				mp[ mpi++ ] = diff[bdf] >> 1;
				mp[ mpi++ ] = i;
				mp[ mpi++ ] = 3;
				mp[ mpi++ ] = cb;
			}
			diff = bi.shortDiff;
			bdf = 3;
			for( cb = 3; cb < 13; cb++ ) {
				final int l = diff[bdf++] >> 1;
				for( int lwin = 0; lwin < 3; lwin++ ) {
					mp[ mpi++ ] = l;
					mp[ mpi++ ] = i + lwin;
					mp[ mpi++ ] = lwin;
					mp[ mpi++ ] = cb;
				}
				i += 6 * l;
			}
			mapend[j][0] = mpi;

			mp = map[j][1] = mapbuf1[j];
			mpi = 0;
			diff = bi.shortDiff;
			bdf = 0;
			for( i = 0, cb = 0; cb < 13; cb++ ) {
				final int l = diff[bdf++] >> 1;
				for( int lwin = 0; lwin < 3; lwin++ ) {
					mp[ mpi++ ] = l;
					mp[ mpi++ ] = i + lwin;
					mp[ mpi++ ] = lwin;
					mp[ mpi++ ] = cb;
				}
				i += 6 * l;
			}
			mapend[j][1] = mpi;

			mp = map[j][2] = mapbuf2[j];
			mpi = 0;
			diff = bi.longDiff;
			bdf = 0;
			for( cb = 0; cb < 22; cb++ ) {
				mp[ mpi++ ] = diff[bdf++] >> 1;
				mp[ mpi++ ] = cb;
			}
			mapend[j][2] = mpi;

		}

		for( int j = 0; j < 9; j++ ) {
			for( int i = 0; i < 23; i++ ) {
				longLimit[j][i] = (bandInfo[j].longIdx[i] - 1 + 8) / 18 + 1;
				if( longLimit[j][i] > Mpg123.SBLIMIT ) {
					longLimit[j][i] = Mpg123.SBLIMIT;
				}
			}
			for( int i = 0; i < 14; i++ ) {
				shortLimit[j][i] = (bandInfo[j].shortIdx[i] - 1) / 18 + 1;
				if( shortLimit[j][i] > Mpg123.SBLIMIT ) {
					shortLimit[j][i] = Mpg123.SBLIMIT;
				}
			}
		}

		for( int i = 0; i < 5; i++ ) {
			for( int j = 0; j < 6; j++ ) {
				for( int k = 0; k < 6; k++ ) {
					final int n = k + j * 6 + i * 36;
					i_slen2[n] = i | (j << 3) | (k << 6) | (3 << 12);
				}
			}
		}
		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 4; j++ ) {
				for( int k = 0; k < 4; k++ ) {
					final int     n = k + j * 4 + i * 16;
					i_slen2[n + 180] = i | (j << 3) | (k << 6) | (4 << 12);
				}
			}
		}
		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 3; j++ ) {
				final int     n = j + i * 3;
				i_slen2[n + 244] = i | (j << 3) | (5 << 12);
				n_slen2[n + 500] = i | (j << 3) | (2 << 12) | (1 << 15);
			}
		}

		for( int i = 0; i < 5; i++ ) {
			for( int j = 0; j < 5; j++ ) {
				for( int k = 0; k < 4; k++ ) {
					for( int l = 0; l < 4; l++ ) {
						final int n = l + k * 4 + j * 16 + i * 80;
						n_slen2[n] = i | (j << 3) | (k << 6) | (l << 9) | (0 << 12);
					}
				}
			}
		}
		for( int i = 0; i < 5; i++ ) {
			for( int j = 0; j < 5; j++ ) {
				for( int k = 0; k < 4; k++ ) {
					final int n = k + j * 4 + i * 20;
					n_slen2[n + 400] = i | (j << 3) | (k << 6) | (1 << 12);
				}
			}
		}
	}

	/** read additional side information */
	@SuppressWarnings("boxing")
	private static final void III_get_side_info_1(final MpStrTag mp, final int stereo,
												  final boolean ms_stereo, final int sfreq, final int single)
	{
		final int powdiff = (single == 3) ? 4 + 256 : 0 + 256;

		mp.sideinfo.main_data_begin = mp.getbits( 9 );
		mp.sideinfo.private_bits = mp.getbits_fast( stereo == 1 ? 5 : 3 );

		final CH[] chs = mp.sideinfo.ch;// java
		for( int ch = 0; ch < stereo; ch++ ) {
			chs[ch].gr[0].scfsi = -1;
			chs[ch].gr[1].scfsi = mp.getbits_fast( 4 );
		}

		for( int gr = 0; gr < 2; gr++ ) {
			for( int ch = 0; ch < stereo; ch++ ) {
				final MpgGrInfo gr_infos = chs[ch].gr[gr];

				gr_infos.part2_3_length = mp.getbits( 12 );
				gr_infos.big_values = mp.getbits_fast( 9 );
				if( gr_infos.big_values > 288 ) {
					//System.err.printf("big_values too large! %d\n", gr_infos.big_values );
					gr_infos.big_values = 288;
				}
				{
					final int qss = mp.getbits_fast( 8 );
					gr_infos.pow2gain = /*256 +*/ powdiff - qss;
					gr_infos.pow2gain_base = gainpow2;
				}
				if( ms_stereo ) {
					gr_infos.pow2gain += 2;
				}
				gr_infos.scalefac_compress = mp.getbits_fast( 4 );
				/* window-switching flag == 1 for block_Type != 0 .. and block-type == 0 . win-sw-flag = 0 */
				if( get1bit( mp ) != 0 ) {
					gr_infos.block_type = mp.getbits_fast( 2 );
					gr_infos.mixed_block_flag = get1bit( mp );
					gr_infos.table_select[0] = mp.getbits_fast( 5 );
					gr_infos.table_select[1] = mp.getbits_fast( 5 );

					/*
					 * table_select[2] not needed, because there is no region2,
					 * but to satisfy some verifications tools we set it either.
					 */
					gr_infos.table_select[2] = 0;
					int i = 0;
					do {
						final int sbg = (mp.getbits_fast( 3 ) << 3);
						gr_infos.full_gain[i] = gr_infos.pow2gain_base;
						gr_infos.full_gain_pos[i] = gr_infos.pow2gain + sbg;
					} while( ++i < 3 );

					if( gr_infos.block_type == 0 ) {
						//System.err.printf("Blocktype == 0 and window-switching == 1 not allowed.\n");
						/* error seems to be very good recoverable, so don't exit */
						/* exit(1); */
					}
					/* region_count/start parameters are implicit in this case. */
					gr_infos.region1start = 36 >> 1;
					gr_infos.region2start = 576 >> 1;
				} else {
					gr_infos.table_select[0] = mp.getbits_fast( 5 );
					gr_infos.table_select[1] = mp.getbits_fast( 5 );
					gr_infos.table_select[2] = mp.getbits_fast( 5 );
					int region0index = mp.getbits_fast( 4 );
					region0index++;
					int region1index = region0index;
					region1index += mp.getbits_fast( 3 );
					region1index++;
					if( region0index > 22 ) {
						//System.err.printf("region0index=%d > 22\n", region0index);
						region0index = 22;
					}
					if( region1index > 22 ) {
						//System.err.printf("region1index=%d > 22\n", region1index);
						region1index = 22;
					}
					gr_infos.region1start = bandInfo[sfreq].longIdx[region0index] >> 1;
					gr_infos.region2start = bandInfo[sfreq].longIdx[region1index] >> 1;
					gr_infos.block_type = 0;
					gr_infos.mixed_block_flag = 0;
				}
				gr_infos.preflag = get1bit( mp ) != 0;
				gr_infos.scalefac_scale = get1bit( mp );
				gr_infos.count1table_select = get1bit( mp );
			}
		}
	}

	/** Side Info for MPEG 2.0 / LSF */
	@SuppressWarnings("boxing")
	private static final void III_get_side_info_2(final MpStrTag mp, final int stereo, final boolean ms_stereo, final int sfreq, final int single) {
		final int powdiff = (single == 3) ? 4 + 256 : 0 + 256;

		mp.sideinfo.main_data_begin = mp.getbits( 8 );

		if( stereo == 1 ) {
			mp.sideinfo.private_bits = get1bit( mp );
		} else {
			mp.sideinfo.private_bits = mp.getbits_fast( 2 );
		}

		final BandInfoStruct bi = bandInfo[sfreq];// java
		for( int ch = 0; ch < stereo; ch++ ) {
			final MpgGrInfo gr_infos = mp.sideinfo.ch[ch].gr[0];
			gr_infos.part2_3_length = mp.getbits( 12 );
			gr_infos.big_values = mp.getbits_fast( 9 );
			if( gr_infos.big_values > 288 ) {
				//System.err.printf("big_values too large! %d\n", gr_infos.big_values );
				gr_infos.big_values = 288;
			}
			final int qss = mp.getbits_fast( 8 );
			gr_infos.pow2gain_base = gainpow2;
			gr_infos.pow2gain = /*256 +*/ powdiff - qss;

			if( ms_stereo ) {
				gr_infos.pow2gain += 2;
			}
			gr_infos.scalefac_compress = mp.getbits( 9 );
			/* window-switching flag == 1 for block_Type != 0 .. and block-type == 0 . win-sw-flag = 0 */
			if( get1bit( mp ) != 0 ) {
				gr_infos.block_type = mp.getbits_fast( 2 );
				gr_infos.mixed_block_flag = get1bit( mp );
				gr_infos.table_select[0] = mp.getbits_fast( 5 );
				gr_infos.table_select[1] = mp.getbits_fast( 5 );
				/*
				 * table_select[2] not needed, because there is no region2,
				 * but to satisfy some verifications tools we set it either.
				 */
				gr_infos.table_select[2] = 0;
				int i = 0;
				do {
					final int sbg = (mp.getbits_fast( 3 ) << 3);
					gr_infos.full_gain[i] = gr_infos.pow2gain_base;
					gr_infos.full_gain_pos[i] = gr_infos.pow2gain + sbg;
				} while( ++i < 3 );

				if( gr_infos.block_type == 0 ) {
					//System.err.printf("Blocktype == 0 and window-switching == 1 not allowed.\n" );
					/* error seems to be very good recoverable, so don't exit */
					/* exit(1); */
				}
				/* region_count/start parameters are implicit in this case. */
				if( gr_infos.block_type == 2 ) {
					if( gr_infos.mixed_block_flag == 0 ) {
						gr_infos.region1start = 36 >> 1;
					} else {
						gr_infos.region1start = 48 >> 1;
					}
				} else {
					gr_infos.region1start = 54 >> 1;
				}
				if( sfreq == 8 ) {
					gr_infos.region1start <<= 1;
				}
				gr_infos.region2start = 576 >> 1;
			} else {
				gr_infos.table_select[0] = mp.getbits_fast( 5 );
				gr_infos.table_select[1] = mp.getbits_fast( 5 );
				gr_infos.table_select[2] = mp.getbits_fast( 5 );
				int region0index = mp.getbits_fast( 4 );
				region0index++;
				int region1index = region0index;
				region1index += mp.getbits_fast( 3 );
				region1index++;
				if( region0index > 22 ) {
					//System.err.printf("region0index=%d > 22\n", region0index );
					region0index = 22;
				}
				if( region1index > 22 ) {
					//System.err.printf("region1index=%d > 22\n", region0index);
					region1index = 22;
				}
				gr_infos.region1start = bi.longIdx[region0index] >> 1;
				gr_infos.region2start = bi.longIdx[region1index] >> 1;
				gr_infos.block_type = 0;
				gr_infos.mixed_block_flag = 0;
			}
			gr_infos.scalefac_scale = get1bit( mp );
			gr_infos.count1table_select = get1bit( mp );
		}
	}

	private static final byte slen[][] = {// [2][16] = {
			{0, 0, 0, 0, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4},
			{0, 1, 2, 3, 0, 1, 2, 3, 1, 2, 3, 1, 2, 3, 2, 3}
		};

	/** read scalefactors */
	private static final int III_get_scale_factors_1(final MpStrTag mp, final int[] scf, final MpgGrInfo gr_infos) {
		int scfoffset = 0;
		final int num0 = slen[0][gr_infos.scalefac_compress];
		final int num1 = slen[1][gr_infos.scalefac_compress];
		int numbits;

		if( gr_infos.block_type == 2 ) {
			int i = 18;
			numbits = (num0 + num1) * 18;

			if( gr_infos.mixed_block_flag != 0 ) {
				i = scfoffset + 8;
				do {
					scf[scfoffset++] = mp.getbits_fast( num0 );
				} while( scfoffset < i );
				i = 9;
				numbits -= num0; /* num0 * 17 + num1 * 18 */
			}

			i += scfoffset;
			do {
				scf[scfoffset++] = mp.getbits_fast( num0 );
			} while( scfoffset < i );
			i = scfoffset + 18;
			do {
				scf[scfoffset++] = mp.getbits_fast( num1 );
			} while( scfoffset < i );
			scf[scfoffset++] = 0;
			scf[scfoffset++] = 0;
			scf[scfoffset++] = 0;     /* short[13][0..2] = 0 */
			return numbits;
		}// else {
			final int scfsi = gr_infos.scfsi;
			if( scfsi < 0 ) { /* scfsi < 0 => granule == 0 */
				int i = 11 + scfoffset;
				do {
					scf[scfoffset++] = mp.getbits_fast( num0 );
				} while( scfoffset < i );
				i = 10 + scfoffset;
				do {
					scf[scfoffset++] = mp.getbits_fast( num1 );
				} while( scfoffset < i );
				numbits = (num0 + num1) * 10 + num0;
			} else {
				numbits = 0;
				if( 0 == (scfsi & 0x8) ) {
					final int i = 6 + scfoffset;
					do {
						scf[scfoffset++] = mp.getbits_fast( num0 );
					} while( scfoffset < i );
					numbits += num0 * 6;
				} else {
					scfoffset += 6;
				}

				if( 0 == (scfsi & 0x4) ) {
					final int i = 5 + scfoffset;
					do {
						scf[scfoffset++] = mp.getbits_fast( num0 );
					} while( scfoffset < i );
					numbits += num0 * 5;
				} else {
					scfoffset += 5;
				}

				if( 0 == (scfsi & 0x2) ) {
					final int i = 5 + scfoffset;
					do {
						scf[scfoffset++] = mp.getbits_fast( num1 );
					} while( scfoffset < i );
					numbits += num1 * 5;
				} else {
					scfoffset += 5;
				}

				if( 0 == (scfsi & 0x1) ) {
					final int i = 5 + scfoffset;
					do {
						scf[scfoffset++] = mp.getbits_fast( num1 );
					} while( scfoffset < i );
					numbits += num1 * 5;
				} else {
					scfoffset += 5;
				}
			}

			scf[scfoffset++] = 0;     /* no l[21] in original sources */
		//}
		return numbits;
	}

	private static final byte stab[][][] = {// [3][6][4] = {
		{ { 6, 5, 5,5 } , { 6, 5, 7,3 } , { 11,10,0,0} ,
		{ 7, 7, 7,0 } , { 6, 6, 6,3 } , {  8, 8,5,0} } ,
		{ { 9, 9, 9,9 } , { 9, 9,12,6 } , { 18,18,0,0} ,
		{12,12,12,0 } , {12, 9, 9,6 } , { 15,12,9,0} } ,
		{ { 6, 9, 9,9 } , { 6, 9,12,6 } , { 15,18,0,0} ,
		{ 6,15,12,0 } , { 6,12, 9,6 } , {  6,18,9,0} } };

	private static final int III_get_scale_factors_2(final MpStrTag mp, final int[] scf, final MpgGrInfo gr_infos, final boolean i_stereo) {
		int slen2;
		if( i_stereo ) {
			slen2 = i_slen2[gr_infos.scalefac_compress >> 1];
		} else {
			slen2 = n_slen2[gr_infos.scalefac_compress];
		}

		gr_infos.preflag = ((slen2 >>> 15) & 0x1) != 0;

		int n = 0;
		if( gr_infos.block_type == 2 ) {
			n++;
			if( gr_infos.mixed_block_flag != 0 ) {
				n++;
			}
		}

		final byte[] pnt = stab[n][(slen2 >>> 12) & 0x7];
		int numbits = 0;
		int scfoffset = 0;
		int i = 0;
		do {
			final int num = slen2 & 0x7;
			slen2 >>>= 3;
			if( num != 0 ) {
				for( int j = 0, je = (int) pnt[i]; j < je; j++ ) {
					scf[scfoffset++] = mp.getbits_fast( num );
				}
				numbits += pnt[i] * num;
			} else {
				for( int j = 0, je = (int) pnt[i]; j < je; j++) {
					scf[scfoffset++] = 0;
				}
			}
		} while( ++i < 4 );

		n <<= 1;
		for( i = 0; i <= n; i++ ) {
			scf[scfoffset++] = 0;
		}

		return numbits;
	}

	private static final short tab0[] = {
		0
	};

	private static final short tab1[] = {
		-5,  -3,  -1,  17,   1,  16,   0
	};

	private static final short tab2[] = {
		-15, -11, -9, -5, -3, -1, 34, 2, 18, -1, 33, 32, 17, -1, 1, 16, 0
	};

	private static final short tab3[] = {
		-13, -11, -9, -5, -3, -1, 34, 2, 18, -1, 33, 32, 16, 17, -1, 1, 0
	};

	private static final short tab5[] = {
		-29, -25, -23, -15, -7, -5, -3, -1, 51, 35, 50, 49, -3, -1, 19,
		3, -1, 48, 34, -3, -1, 18, 33, -1, 2, 32, 17, -1, 1, 16, 0
	};

	private static final short tab6[] = {
		-25, -19, -13,  -9,  -5,  -3,  -1,  51,   3,  35,  -1,  50,  48,  -1,  19,
		49,  -3,  -1,  34,   2,  18,  -3,  -1,  33,  32,   1,  -1,  17,  -1,  16,
		0
	};

	private static final short tab7[] = {
		-69, -65, -57, -39, -29, -17, -11,  -7,  -3,  -1,  85,  69,  -1,  84,  83,
		-1,  53,  68,  -3,  -1,  37,  82,  21,  -5,  -1,  81,  -1,   5,  52,  -1,
		80,  -1,  67,  51,  -5,  -3,  -1,  36,  66,  20,  -1,  65,  64, -11,  -7,
		-3,  -1,   4,  35,  -1,  50,   3,  -1,  19,  49,  -3,  -1,  48,  34,  18,
		-5,  -1,  33,  -1,   2,  32,  17,  -1,   1,  16,   0
	};

	private static final short tab8[] = {
		-65, -63, -59, -45, -31, -19, -13,  -7,  -5,  -3,  -1,  85,  84,  69,  83,
		-3,  -1,  53,  68,  37,  -3,  -1,  82,   5,  21,  -5,  -1,  81,  -1,  52,
		67,  -3,  -1,  80,  51,  36,  -5,  -3,  -1,  66,  20,  65,  -3,  -1,   4,
		64,  -1,  35,  50,  -9,  -7,  -3,  -1,  19,  49,  -1,   3,  48,  34,  -1,
		2,  32,  -1,  18,  33,  17,  -3,  -1,   1,  16,   0
	};

	private static final short tab9[] = {
		-63, -53, -41, -29, -19, -11,  -5,  -3,  -1,  85,  69,  53,  -1,  83,  -1,
		84,   5,  -3,  -1,  68,  37,  -1,  82,  21,  -3,  -1,  81,  52,  -1,  67,
		-1,  80,   4,  -7,  -3,  -1,  36,  66,  -1,  51,  64,  -1,  20,  65,  -5,
		-3,  -1,  35,  50,  19,  -1,  49,  -1,   3,  48,  -5,  -3,  -1,  34,   2,
		18,  -1,  33,  32,  -3,  -1,  17,   1,  -1,  16,   0
	};

	private static final short tab10[] = {
		-125,-121,-111, -83, -55, -35, -21, -13,  -7,  -3,  -1, 119, 103,  -1, 118,
		87,  -3,  -1, 117, 102,  71,  -3,  -1, 116,  86,  -1, 101,  55,  -9,  -3,
		-1, 115,  70,  -3,  -1,  85,  84,  99,  -1,  39, 114, -11,  -5,  -3,  -1,
		100,   7, 112,  -1,  98,  -1,  69,  53,  -5,  -1,   6,  -1,  83,  68,  23,
		-17,  -5,  -1, 113,  -1,  54,  38,  -5,  -3,  -1,  37,  82,  21,  -1,  81,
		-1,  52,  67,  -3,  -1,  22,  97,  -1,  96,  -1,   5,  80, -19, -11,  -7,
		-3,  -1,  36,  66,  -1,  51,   4,  -1,  20,  65,  -3,  -1,  64,  35,  -1,
		50,   3,  -3,  -1,  19,  49,  -1,  48,  34,  -7,  -3,  -1,  18,  33,  -1,
		2,  32,  17,  -1,   1,  16,   0
	};

	private static final short tab11[] = {
		-121,-113, -89, -59, -43, -27, -17,  -7,  -3,  -1, 119, 103,  -1, 118, 117,
		-3,  -1, 102,  71,  -1, 116,  -1,  87,  85,  -5,  -3,  -1,  86, 101,  55,
		-1, 115,  70,  -9,  -7,  -3,  -1,  69,  84,  -1,  53,  83,  39,  -1, 114,
		-1, 100,   7,  -5,  -1, 113,  -1,  23, 112,  -3,  -1,  54,  99,  -1,  96,
		-1,  68,  37, -13,  -7,  -5,  -3,  -1,  82,   5,  21,  98,  -3,  -1,  38,
		6,  22,  -5,  -1,  97,  -1,  81,  52,  -5,  -1,  80,  -1,  67,  51,  -1,
		36,  66, -15, -11,  -7,  -3,  -1,  20,  65,  -1,   4,  64,  -1,  35,  50,
		-1,  19,  49,  -5,  -3,  -1,   3,  48,  34,  33,  -5,  -1,  18,  -1,   2,
		32,  17,  -3,  -1,   1,  16,   0
	};

	private static final short tab12[] = {
		-115, -99, -73, -45, -27, -17,  -9,  -5,  -3,  -1, 119, 103, 118,  -1,  87,
		117,  -3,  -1, 102,  71,  -1, 116, 101,  -3,  -1,  86,  55,  -3,  -1, 115,
		85,  39,  -7,  -3,  -1, 114,  70,  -1, 100,  23,  -5,  -1, 113,  -1,   7,
		112,  -1,  54,  99, -13,  -9,  -3,  -1,  69,  84,  -1,  68,  -1,   6,   5,
		-1,  38,  98,  -5,  -1,  97,  -1,  22,  96,  -3,  -1,  53,  83,  -1,  37,
		82, -17,  -7,  -3,  -1,  21,  81,  -1,  52,  67,  -5,  -3,  -1,  80,   4,
		36,  -1,  66,  20,  -3,  -1,  51,  65,  -1,  35,  50, -11,  -7,  -5,  -3,
		-1,  64,   3,  48,  19,  -1,  49,  34,  -1,  18,  33,  -7,  -5,  -3,  -1,
		2,  32,   0,  17,  -1,   1,  16
	};

	private static final short tab13[] = {
		-509,-503,-475,-405,-333,-265,-205,-153,-115, -83, -53, -35, -21, -13,  -9,
		-7,  -5,  -3,  -1, 254, 252, 253, 237, 255,  -1, 239, 223,  -3,  -1, 238,
		207,  -1, 222, 191,  -9,  -3,  -1, 251, 206,  -1, 220,  -1, 175, 233,  -1,
		236, 221,  -9,  -5,  -3,  -1, 250, 205, 190,  -1, 235, 159,  -3,  -1, 249,
		234,  -1, 189, 219, -17,  -9,  -3,  -1, 143, 248,  -1, 204,  -1, 174, 158,
		-5,  -1, 142,  -1, 127, 126, 247,  -5,  -1, 218,  -1, 173, 188,  -3,  -1,
		203, 246, 111, -15,  -7,  -3,  -1, 232,  95,  -1, 157, 217,  -3,  -1, 245,
		231,  -1, 172, 187,  -9,  -3,  -1,  79, 244,  -3,  -1, 202, 230, 243,  -1,
		63,  -1, 141, 216, -21,  -9,  -3,  -1,  47, 242,  -3,  -1, 110, 156,  15,
		-5,  -3,  -1, 201,  94, 171,  -3,  -1, 125, 215,  78, -11,  -5,  -3,  -1,
		200, 214,  62,  -1, 185,  -1, 155, 170,  -1,  31, 241, -23, -13,  -5,  -1,
		240,  -1, 186, 229,  -3,  -1, 228, 140,  -1, 109, 227,  -5,  -1, 226,  -1,
		46,  14,  -1,  30, 225, -15,  -7,  -3,  -1, 224,  93,  -1, 213, 124,  -3,
		-1, 199,  77,  -1, 139, 184,  -7,  -3,  -1, 212, 154,  -1, 169, 108,  -1,
		198,  61, -37, -21,  -9,  -5,  -3,  -1, 211, 123,  45,  -1, 210,  29,  -5,
		-1, 183,  -1,  92, 197,  -3,  -1, 153, 122, 195,  -7,  -5,  -3,  -1, 167,
		151,  75, 209,  -3,  -1,  13, 208,  -1, 138, 168, -11,  -7,  -3,  -1,  76,
		196,  -1, 107, 182,  -1,  60,  44,  -3,  -1, 194,  91,  -3,  -1, 181, 137,
		28, -43, -23, -11,  -5,  -1, 193,  -1, 152,  12,  -1, 192,  -1, 180, 106,
		-5,  -3,  -1, 166, 121,  59,  -1, 179,  -1, 136,  90, -11,  -5,  -1,  43,
		-1, 165, 105,  -1, 164,  -1, 120, 135,  -5,  -1, 148,  -1, 119, 118, 178,
		-11,  -3,  -1,  27, 177,  -3,  -1,  11, 176,  -1, 150,  74,  -7,  -3,  -1,
		58, 163,  -1,  89, 149,  -1,  42, 162, -47, -23,  -9,  -3,  -1,  26, 161,
		-3,  -1,  10, 104, 160,  -5,  -3,  -1, 134,  73, 147,  -3,  -1,  57,  88,
		-1, 133, 103,  -9,  -3,  -1,  41, 146,  -3,  -1,  87, 117,  56,  -5,  -1,
		131,  -1, 102,  71,  -3,  -1, 116,  86,  -1, 101, 115, -11,  -3,  -1,  25,
		145,  -3,  -1,   9, 144,  -1,  72, 132,  -7,  -5,  -1, 114,  -1,  70, 100,
		40,  -1, 130,  24, -41, -27, -11,  -5,  -3,  -1,  55,  39,  23,  -1, 113,
		-1,  85,   7,  -7,  -3,  -1, 112,  54,  -1,  99,  69,  -3,  -1,  84,  38,
		-1,  98,  53,  -5,  -1, 129,  -1,   8, 128,  -3,  -1,  22,  97,  -1,   6,
		96, -13,  -9,  -5,  -3,  -1,  83,  68,  37,  -1,  82,   5,  -1,  21,  81,
		-7,  -3,  -1,  52,  67,  -1,  80,  36,  -3,  -1,  66,  51,  20, -19, -11,
		-5,  -1,  65,  -1,   4,  64,  -3,  -1,  35,  50,  19,  -3,  -1,  49,   3,
		-1,  48,  34,  -3,  -1,  18,  33,  -1,   2,  32,  -3,  -1,  17,   1,  16,
		0
	};

	private static final short tab15[] = {
		-495,-445,-355,-263,-183,-115, -77, -43, -27, -13,  -7,  -3,  -1, 255, 239,
		-1, 254, 223,  -1, 238,  -1, 253, 207,  -7,  -3,  -1, 252, 222,  -1, 237,
		191,  -1, 251,  -1, 206, 236,  -7,  -3,  -1, 221, 175,  -1, 250, 190,  -3,
		-1, 235, 205,  -1, 220, 159, -15,  -7,  -3,  -1, 249, 234,  -1, 189, 219,
		-3,  -1, 143, 248,  -1, 204, 158,  -7,  -3,  -1, 233, 127,  -1, 247, 173,
		-3,  -1, 218, 188,  -1, 111,  -1, 174,  15, -19, -11,  -3,  -1, 203, 246,
		-3,  -1, 142, 232,  -1,  95, 157,  -3,  -1, 245, 126,  -1, 231, 172,  -9,
		-3,  -1, 202, 187,  -3,  -1, 217, 141,  79,  -3,  -1, 244,  63,  -1, 243,
		216, -33, -17,  -9,  -3,  -1, 230,  47,  -1, 242,  -1, 110, 240,  -3,  -1,
		31, 241,  -1, 156, 201,  -7,  -3,  -1,  94, 171,  -1, 186, 229,  -3,  -1,
		125, 215,  -1,  78, 228, -15,  -7,  -3,  -1, 140, 200,  -1,  62, 109,  -3,
		-1, 214, 227,  -1, 155, 185,  -7,  -3,  -1,  46, 170,  -1, 226,  30,  -5,
		-1, 225,  -1,  14, 224,  -1,  93, 213, -45, -25, -13,  -7,  -3,  -1, 124,
		199,  -1,  77, 139,  -1, 212,  -1, 184, 154,  -7,  -3,  -1, 169, 108,  -1,
		198,  61,  -1, 211, 210,  -9,  -5,  -3,  -1,  45,  13,  29,  -1, 123, 183,
		-5,  -1, 209,  -1,  92, 208,  -1, 197, 138, -17,  -7,  -3,  -1, 168,  76,
		-1, 196, 107,  -5,  -1, 182,  -1, 153,  12,  -1,  60, 195,  -9,  -3,  -1,
		122, 167,  -1, 166,  -1, 192,  11,  -1, 194,  -1,  44,  91, -55, -29, -15,
		-7,  -3,  -1, 181,  28,  -1, 137, 152,  -3,  -1, 193,  75,  -1, 180, 106,
		-5,  -3,  -1,  59, 121, 179,  -3,  -1, 151, 136,  -1,  43,  90, -11,  -5,
		-1, 178,  -1, 165,  27,  -1, 177,  -1, 176, 105,  -7,  -3,  -1, 150,  74,
		-1, 164, 120,  -3,  -1, 135,  58, 163, -17,  -7,  -3,  -1,  89, 149,  -1,
		42, 162,  -3,  -1,  26, 161,  -3,  -1,  10, 160, 104,  -7,  -3,  -1, 134,
		73,  -1, 148,  57,  -5,  -1, 147,  -1, 119,   9,  -1,  88, 133, -53, -29,
		-13,  -7,  -3,  -1,  41, 103,  -1, 118, 146,  -1, 145,  -1,  25, 144,  -7,
		-3,  -1,  72, 132,  -1,  87, 117,  -3,  -1,  56, 131,  -1, 102,  71,  -7,
		-3,  -1,  40, 130,  -1,  24, 129,  -7,  -3,  -1, 116,   8,  -1, 128,  86,
		-3,  -1, 101,  55,  -1, 115,  70, -17,  -7,  -3,  -1,  39, 114,  -1, 100,
		23,  -3,  -1,  85, 113,  -3,  -1,   7, 112,  54,  -7,  -3,  -1,  99,  69,
		-1,  84,  38,  -3,  -1,  98,  22,  -3,  -1,   6,  96,  53, -33, -19,  -9,
		-5,  -1,  97,  -1,  83,  68,  -1,  37,  82,  -3,  -1,  21,  81,  -3,  -1,
		5,  80,  52,  -7,  -3,  -1,  67,  36,  -1,  66,  51,  -1,  65,  -1,  20,
		4,  -9,  -3,  -1,  35,  50,  -3,  -1,  64,   3,  19,  -3,  -1,  49,  48,
		34,  -9,  -7,  -3,  -1,  18,  33,  -1,   2,  32,  17,  -3,  -1,   1,  16,
		0
	};

	private static final short tab16[] = {
		-509,-503,-461,-323,-103, -37, -27, -15,  -7,  -3,  -1, 239, 254,  -1, 223,
		253,  -3,  -1, 207, 252,  -1, 191, 251,  -5,  -1, 175,  -1, 250, 159,  -3,
		-1, 249, 248, 143,  -7,  -3,  -1, 127, 247,  -1, 111, 246, 255,  -9,  -5,
		-3,  -1,  95, 245,  79,  -1, 244, 243, -53,  -1, 240,  -1,  63, -29, -19,
		-13,  -7,  -5,  -1, 206,  -1, 236, 221, 222,  -1, 233,  -1, 234, 217,  -1,
		238,  -1, 237, 235,  -3,  -1, 190, 205,  -3,  -1, 220, 219, 174, -11,  -5,
		-1, 204,  -1, 173, 218,  -3,  -1, 126, 172, 202,  -5,  -3,  -1, 201, 125,
		94, 189, 242, -93,  -5,  -3,  -1,  47,  15,  31,  -1, 241, -49, -25, -13,
		-5,  -1, 158,  -1, 188, 203,  -3,  -1, 142, 232,  -1, 157, 231,  -7,  -3,
		-1, 187, 141,  -1, 216, 110,  -1, 230, 156, -13,  -7,  -3,  -1, 171, 186,
		-1, 229, 215,  -1,  78,  -1, 228, 140,  -3,  -1, 200,  62,  -1, 109,  -1,
		214, 155, -19, -11,  -5,  -3,  -1, 185, 170, 225,  -1, 212,  -1, 184, 169,
		-5,  -1, 123,  -1, 183, 208, 227,  -7,  -3,  -1,  14, 224,  -1,  93, 213,
		-3,  -1, 124, 199,  -1,  77, 139, -75, -45, -27, -13,  -7,  -3,  -1, 154,
		108,  -1, 198,  61,  -3,  -1,  92, 197,  13,  -7,  -3,  -1, 138, 168,  -1,
		153,  76,  -3,  -1, 182, 122,  60, -11,  -5,  -3,  -1,  91, 137,  28,  -1,
		192,  -1, 152, 121,  -1, 226,  -1,  46,  30, -15,  -7,  -3,  -1, 211,  45,
		-1, 210, 209,  -5,  -1,  59,  -1, 151, 136,  29,  -7,  -3,  -1, 196, 107,
		-1, 195, 167,  -1,  44,  -1, 194, 181, -23, -13,  -7,  -3,  -1, 193,  12,
		-1,  75, 180,  -3,  -1, 106, 166, 179,  -5,  -3,  -1,  90, 165,  43,  -1,
		178,  27, -13,  -5,  -1, 177,  -1,  11, 176,  -3,  -1, 105, 150,  -1,  74,
		164,  -5,  -3,  -1, 120, 135, 163,  -3,  -1,  58,  89,  42, -97, -57, -33,
		-19, -11,  -5,  -3,  -1, 149, 104, 161,  -3,  -1, 134, 119, 148,  -5,  -3,
		-1,  73,  87, 103, 162,  -5,  -1,  26,  -1,  10, 160,  -3,  -1,  57, 147,
		-1,  88, 133,  -9,  -3,  -1,  41, 146,  -3,  -1, 118,   9,  25,  -5,  -1,
		145,  -1, 144,  72,  -3,  -1, 132, 117,  -1,  56, 131, -21, -11,  -5,  -3,
		-1, 102,  40, 130,  -3,  -1,  71, 116,  24,  -3,  -1, 129, 128,  -3,  -1,
		8,  86,  55,  -9,  -5,  -1, 115,  -1, 101,  70,  -1,  39, 114,  -5,  -3,
		-1, 100,  85,   7,  23, -23, -13,  -5,  -1, 113,  -1, 112,  54,  -3,  -1,
		99,  69,  -1,  84,  38,  -3,  -1,  98,  22,  -1,  97,  -1,   6,  96,  -9,
		-5,  -1,  83,  -1,  53,  68,  -1,  37,  82,  -1,  81,  -1,  21,   5, -33,
		-23, -13,  -7,  -3,  -1,  52,  67,  -1,  80,  36,  -3,  -1,  66,  51,  20,
		-5,  -1,  65,  -1,   4,  64,  -1,  35,  50,  -3,  -1,  19,  49,  -3,  -1,
		3,  48,  34,  -3,  -1,  18,  33,  -1,   2,  32,  -3,  -1,  17,   1,  16,
		0
	};

	private static final short tab24[] = {
		-451,-117, -43, -25, -15,  -7,  -3,  -1, 239, 254,  -1, 223, 253,  -3,  -1,
		207, 252,  -1, 191, 251,  -5,  -1, 250,  -1, 175, 159,  -1, 249, 248,  -9,
		-5,  -3,  -1, 143, 127, 247,  -1, 111, 246,  -3,  -1,  95, 245,  -1,  79,
		244, -71,  -7,  -3,  -1,  63, 243,  -1,  47, 242,  -5,  -1, 241,  -1,  31,
		240, -25,  -9,  -1,  15,  -3,  -1, 238, 222,  -1, 237, 206,  -7,  -3,  -1,
		236, 221,  -1, 190, 235,  -3,  -1, 205, 220,  -1, 174, 234, -15,  -7,  -3,
		-1, 189, 219,  -1, 204, 158,  -3,  -1, 233, 173,  -1, 218, 188,  -7,  -3,
		-1, 203, 142,  -1, 232, 157,  -3,  -1, 217, 126,  -1, 231, 172, 255,-235,
		-143, -77, -45, -25, -15,  -7,  -3,  -1, 202, 187,  -1, 141, 216,  -5,  -3,
		-1,  14, 224,  13, 230,  -5,  -3,  -1, 110, 156, 201,  -1,  94, 186,  -9,
		-5,  -1, 229,  -1, 171, 125,  -1, 215, 228,  -3,  -1, 140, 200,  -3,  -1,
		78,  46,  62, -15,  -7,  -3,  -1, 109, 214,  -1, 227, 155,  -3,  -1, 185,
		170,  -1, 226,  30,  -7,  -3,  -1, 225,  93,  -1, 213, 124,  -3,  -1, 199,
		77,  -1, 139, 184, -31, -15,  -7,  -3,  -1, 212, 154,  -1, 169, 108,  -3,
		-1, 198,  61,  -1, 211,  45,  -7,  -3,  -1, 210,  29,  -1, 123, 183,  -3,
		-1, 209,  92,  -1, 197, 138, -17,  -7,  -3,  -1, 168, 153,  -1,  76, 196,
		-3,  -1, 107, 182,  -3,  -1, 208,  12,  60,  -7,  -3,  -1, 195, 122,  -1,
		167,  44,  -3,  -1, 194,  91,  -1, 181,  28, -57, -35, -19,  -7,  -3,  -1,
		137, 152,  -1, 193,  75,  -5,  -3,  -1, 192,  11,  59,  -3,  -1, 176,  10,
		26,  -5,  -1, 180,  -1, 106, 166,  -3,  -1, 121, 151,  -3,  -1, 160,   9,
		144,  -9,  -3,  -1, 179, 136,  -3,  -1,  43,  90, 178,  -7,  -3,  -1, 165,
		27,  -1, 177, 105,  -1, 150, 164, -17,  -9,  -5,  -3,  -1,  74, 120, 135,
		-1,  58, 163,  -3,  -1,  89, 149,  -1,  42, 162,  -7,  -3,  -1, 161, 104,
		-1, 134, 119,  -3,  -1,  73, 148,  -1,  57, 147, -63, -31, -15,  -7,  -3,
		-1,  88, 133,  -1,  41, 103,  -3,  -1, 118, 146,  -1,  25, 145,  -7,  -3,
		-1,  72, 132,  -1,  87, 117,  -3,  -1,  56, 131,  -1, 102,  40, -17,  -7,
		-3,  -1, 130,  24,  -1,  71, 116,  -5,  -1, 129,  -1,   8, 128,  -1,  86,
		101,  -7,  -5,  -1,  23,  -1,   7, 112, 115,  -3,  -1,  55,  39, 114, -15,
		-7,  -3,  -1,  70, 100,  -1,  85, 113,  -3,  -1,  54,  99,  -1,  69,  84,
		-7,  -3,  -1,  38,  98,  -1,  22,  97,  -5,  -3,  -1,   6,  96,  53,  -1,
		83,  68, -51, -37, -23, -15,  -9,  -3,  -1,  37,  82,  -1,  21,  -1,   5,
		80,  -1,  81,  -1,  52,  67,  -3,  -1,  36,  66,  -1,  51,  20,  -9,  -5,
		-1,  65,  -1,   4,  64,  -1,  35,  50,  -1,  19,  49,  -7,  -5,  -3,  -1,
		3,  48,  34,  18,  -1,  33,  -1,   2,  32,  -3,  -1,  17,   1,  -1,  16,
		0
	};

	private static final short tab_c0[] = {
		-29, -21, -13,  -7,  -3,  -1,  11,  15,  -1,  13,  14,  -3,  -1,   7,   5,
		9,  -3,  -1,   6,   3,  -1,  10,  12,  -3,  -1,   2,   1,  -1,   4,   8,
		0
	};

	private static final short tab_c1[] = {
		-15,  -7,  -3,  -1,  15,  14,  -1,  13,  12,  -3,  -1,  11,  10,  -1,   9,
		8,  -7,  -3,  -1,   7,   6,  -1,   5,   4,  -3,  -1,   3,   2,  -1,   1,
		0
	};

	private static final class NewHuff {
		final int linbits;
		final short[] table;
		//
		public NewHuff(final int bits, final short[] t) {
			this.linbits = bits;
			this.table = t;
		}
	}

	private static final NewHuff sHt[] = {
		new NewHuff( /* 0 */ 0 , tab0 ),
		new NewHuff( /* 2 */ 0 , tab1  ),
		new NewHuff( /* 3 */ 0 , tab2  ),
		new NewHuff( /* 3 */ 0 , tab3  ),
		new NewHuff( /* 0 */ 0 , tab0  ),
		new NewHuff( /* 4 */ 0 , tab5  ),
		new NewHuff( /* 4 */ 0 , tab6  ),
		new NewHuff( /* 6 */ 0 , tab7  ),
		new NewHuff( /* 6 */ 0 , tab8  ),
		new NewHuff( /* 6 */ 0 , tab9  ),
		new NewHuff( /* 8 */ 0 , tab10 ),
		new NewHuff( /* 8 */ 0 , tab11 ),
		new NewHuff( /* 8 */ 0 , tab12 ),
		new NewHuff( /* 16 */ 0 , tab13 ),
		new NewHuff( /* 0  */ 0 , tab0  ),
		new NewHuff( /* 16 */ 0 , tab15 ),

		new NewHuff( /* 16 */ 1 , tab16 ),
		new NewHuff( /* 16 */ 2 , tab16 ),
		new NewHuff( /* 16 */ 3 , tab16 ),
		new NewHuff( /* 16 */ 4 , tab16 ),
		new NewHuff( /* 16 */ 6 , tab16 ),
		new NewHuff( /* 16 */ 8 , tab16 ),
		new NewHuff( /* 16 */ 10, tab16 ),
		new NewHuff( /* 16 */ 13, tab16 ),
		new NewHuff( /* 16 */ 4 , tab24 ),
		new NewHuff( /* 16 */ 5 , tab24 ),
		new NewHuff( /* 16 */ 6 , tab24 ),
		new NewHuff( /* 16 */ 7 , tab24 ),
		new NewHuff( /* 16 */ 8 , tab24 ),
		new NewHuff( /* 16 */ 9 , tab24 ),
		new NewHuff( /* 16 */ 11, tab24 ),
		new NewHuff( /* 16 */ 13, tab24 )
	};

	private static final NewHuff sHtc[] =  {
		new NewHuff( /* 1 , 1 , */ 0 , tab_c0 ),
		new NewHuff( /* 1 , 1 , */ 0 , tab_c1 )
	};

	private static final int pretab1[/*22*/] = {0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,2,2,3,3,3,2,0}; /* char enough ? */
	private static final int pretab2[/*22*/] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

	/* don't forget to apply the same changes to III_dequantize_sample_ms() !!! */
	@SuppressWarnings("boxing")
	private static final boolean III_dequantize_sample(final MpStrTag mp, final float xr[]/*[SBLIMIT][SSLIMIT]*/, final int[] scf,
													   final MpgGrInfo gr_infos, final int sfreq, final int part2bits)
	{
		int scfoffset = 0;
		final int shift = 1 + gr_infos.scalefac_scale;
		int xrpnt = 0;// xr[ xrpnt ]
		float xr_value = 0;
		final int l[] = new int[3];
		int part2remain = gr_infos.part2_3_length - part2bits;
		// real const * const xr_endptr = &xr[SBLIMIT-1][SSLIMIT-1];
		final int xr_endptr = (Mpg123.SBLIMIT * Mpg123.SSLIMIT) - 1;

		boolean isbug = false;
		boolean bobug = false;
		int bobug_sb = 0, bobug_l3 = 0;
// #define BUFFER_OVERFLOW_BUG() if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; } else

		{
			// for( int i = (&xr[Mpg123.SBLIMIT][0] - xrpnt) >> 1; i > 0; i-- ) {// FIXME WTF?
			do {
				xr[ xrpnt++ ] = 0.0f;
				xr[ xrpnt++ ] = 0.0f;
			} while( xrpnt < (Mpg123.SBLIMIT * Mpg123.SSLIMIT) );

			xrpnt = 0;// xr[ xrpnt ]
		}

		int l3;
		{
			final int bv = gr_infos.big_values;
			final int region1 = gr_infos.region1start;
			final int region2 = gr_infos.region2start;

			l3 = ((576 >> 1) - bv) >> 1;
			/* we may lose the 'odd' bit here !! check this later again */
			if( bv <= region1 ) {
				l[0] = bv;
				l[1] = 0;
				l[2] = 0;
			} else {
				l[0] = region1;
				if( bv <= region2 ) {
					l[1] = bv - l[0];
					l[2] = 0;
				} else {
					l[1] = region2 - l[0];
					l[2] = bv - region2;
				}
			}
		}
		/* MDH crash fix */
		{
			for( int i = 0; i < 3; i++ ) {
				if( l[i] < 0 ) {
					//System.err.printf("hip: Bogus region length (%d)\n", l[i] );
					l[i] = 0;
				}
			}
		}
		/* end MDH crash fix */

		if( gr_infos.block_type == 2 ) {
	        /* decoding with short or mixed mode BandIndex table */
			final int max[] = new int[4];
			int step = 0, lwin = 0, cb = 0;
			float v = 0.0f;
			int[] m;
			int me;

			if( gr_infos.mixed_block_flag != 0 ) {
				max[3] = -1;
				max[0] = max[1] = max[2] = 2;
				m = map[sfreq][0];
				me = mapend[sfreq][0];
			} else {
				max[0] = max[1] = max[2] = max[3] = -1;
				/* max[3] not really needed in this case */
				m = map[sfreq][1];
				me = mapend[sfreq][1];
			}

			int mc = 0;
			int moffset = 0;
			int i = 0;
			do {
				int lp = l[i];
				final NewHuff h[] = sHt;
				final int hoffset = gr_infos.table_select[i];
				for( ; lp != 0; lp--, mc-- ) {
					int x, y;
					if( 0 == mc ) {
						mc = m[moffset++];
						xrpnt = m[moffset++];
						lwin = m[moffset++];
						cb = m[moffset++];
						if( lwin == 3 ) {
							try { v = gr_infos.pow2gain_base[ gr_infos.pow2gain + (scf[scfoffset++] << shift) ];
							} catch( final ArrayIndexOutOfBoundsException ae ) {
								v = gr_infos.pow2gain_base[ gr_infos.pow2gain_base.length - 1 ];
								isbug = true;
							}
							step = 1;
						} else {
							try { v = gr_infos.full_gain[lwin][ gr_infos.full_gain_pos[lwin] + (scf[scfoffset++] << shift) ];
							} catch( final ArrayIndexOutOfBoundsException ae ) {
								v = gr_infos.full_gain[lwin][ gr_infos.full_gain[lwin].length - 1 ];
								isbug = true;
							}
							step = 3;
						}
					}
					{
						final short[] table = h[hoffset].table;
						int val = 0;// table[ val ]
						while( (y = table[val++]) < 0 ) {
							if( get1bit( mp ) != 0 ) {
								val -= y;
							}
							part2remain--;
						}
						x = y >> 4;
						y &= 0xf;
					}
					if( x == 15 ) {
						max[lwin] = cb;
						part2remain -= h[hoffset].linbits + 1;
						x += mp.getbits( h[hoffset].linbits );
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[x] * v;
						} else {
							xr_value = ispow[x] * v;
						}
					} else if( x != 0 ) {
						max[lwin] = cb;
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[x] * v;
						} else {
							xr_value = ispow[x] * v;
						}
						part2remain--;
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
					xrpnt += step;
					if( y == 15 ) {
						max[lwin] = cb;
						part2remain -= h[hoffset].linbits + 1;
						y += mp.getbits( h[hoffset].linbits );
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[y] * v;
						} else {
							xr_value = ispow[y] * v;
						}
					} else if( y != 0 ) {
						max[lwin] = cb;
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[y] * v;
						} else {
							xr_value = ispow[y] * v;
						}
						part2remain--;
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
					xrpnt += step;
				}
			} while( ++i < 2 );
			for( ; l3 != 0 && (part2remain > 0); l3-- ) {
				final NewHuff h[] = sHtc;
				final int hoffset = gr_infos.count1table_select;// h[ hoffset ]
				final short[] table = h[ hoffset ].table;
				int val = 0;
				short a;

				while( (a = table[val++]) < 0 ) {
					part2remain--;
					if( part2remain < 0 ) {
						part2remain++;
						a = 0;
						break;
					}
					if( get1bit( mp ) != 0 ) {
						val -= a;
					}
				}
				i = 0;
				do {
					if( 0 == (i & 1) ) {
						if( 0 == mc ) {
							mc = m[moffset++];
							xrpnt = m[moffset++];
							lwin = m[moffset++];
							cb = m[moffset++];
							if( lwin == 3 ) {
								try { v = gr_infos.pow2gain_base[ gr_infos.pow2gain + (scf[scfoffset++] << shift) ];
								} catch( final ArrayIndexOutOfBoundsException ae ) {
									v = gr_infos.pow2gain_base[ gr_infos.pow2gain_base.length - 1 ];
									isbug = true;
								}
								step = 1;
							} else {
								try{ v = gr_infos.full_gain[lwin][ gr_infos.full_gain_pos[lwin] + (scf[scfoffset++] << shift) ];
								} catch( final ArrayIndexOutOfBoundsException ae ) {
									v = gr_infos.full_gain[lwin][ gr_infos.full_gain[lwin].length - 1 ];
									isbug = true;
								}
								step = 3;
							}
						}
						mc--;
					}
					if( (a & (0x8 >> i)) != 0 ) {
						max[lwin] = cb;
						part2remain--;
						if( part2remain < 0 ) {
							part2remain++;
							break;
						}
						if( get1bit( mp ) != 0 ) {
							xr_value = -v;
						} else {
							xr_value = v;
						}
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
					xrpnt += step;
				} while( ++i < 4 );
			}

			while( moffset < me ) {
				if( 0 == mc ) {
					mc = m[moffset++];
					xrpnt = m[moffset++];
					if( m[moffset++] == 3 ) {
						step = 1;
					} else {
						step = 3;
					}
					moffset++;    /* cb */
				}
				mc--;
				if( xrpnt <= xr_endptr ) {
					xr[ xrpnt ] = 0.0f;
				} else {
					// BUFFER_OVERFLOW_BUG();
					if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
				}
				xrpnt += step;
				if( xrpnt <= xr_endptr ) {
					xr[ xrpnt ] = 0.0f;
				} else {
					// BUFFER_OVERFLOW_BUG();
					if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
				}
				xrpnt += step;
				/* we could add a little opt. here:
				 * if we finished a band for window 3 or a long band
				 * further bands could copied in a simple loop without a
				 * special 'map' decoding
				 */
			}

			gr_infos.maxband[0] = max[0] + 1;
			gr_infos.maxband[1] = max[1] + 1;
			gr_infos.maxband[2] = max[2] + 1;
			gr_infos.maxbandl = max[3] + 1;

			{
				int rmax = max[0] > max[1] ? max[0] : max[1];
				rmax = (rmax > max[2] ? rmax : max[2]) + 1;
				gr_infos.maxb = rmax != 0 ? shortLimit[sfreq][rmax] : longLimit[sfreq][max[3] + 1];
			}

		} else {
			/* decoding with 'long' BandIndex table (block_type != 2) */
			final int[] pretab = (gr_infos.preflag ? pretab1 : pretab2);
			int ipretab = 0;// pretab[ ipretab ]
			int max = -1;
			int cb = 0;
			final int[] m = map[sfreq][2];
			int moffset = 0;// m[ moffset ]
			float v = 0.0f;
			int mc = 0;

			/* long hash table values */
			int i = 0;
			do {
				int lp = l[i];
				final NewHuff ht[] = sHt;
				final int h = gr_infos.table_select[i];

				for( ; lp != 0; lp--, mc-- ) {
					if( 0 == mc ) {
						mc = m[moffset++];
						try { v = gr_infos.pow2gain_base[ gr_infos.pow2gain + ((scf[scfoffset++] + pretab[ipretab++]) << shift)];
						} catch( final ArrayIndexOutOfBoundsException ae ) {
							v = gr_infos.pow2gain_base[ gr_infos.pow2gain_base.length - 1 ];
							isbug = true;
						}
						cb = m[moffset++];
					}
					final short[] table = ht[ h ].table;
					int val = 0;
					int y;
					while( (y = table[val++]) < 0 ) {
						if( get1bit( mp ) != 0 ) {
							val -= y;
						}
						part2remain--;
					}
					int x = y >> 4;
					y &= 0xf;
					if( x == 15 ) {
						max = cb;
						part2remain -= ht[ h ].linbits + 1;
						x += mp.getbits( ht[ h ].linbits );
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[x] * v;
						} else {
							xr_value = ispow[x] * v;
						}
					} else if( x != 0 ) {
						max = cb;
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[x] * v;
						} else {
							xr_value = ispow[x] * v;
						}
						part2remain--;
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt++ ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
					if( y == 15 ) {
						max = cb;
						part2remain -= ht[ h ].linbits + 1;
						y += mp.getbits( ht[ h ].linbits );
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[y] * v;
						} else {
							xr_value = ispow[y] * v;
						}
					} else if( y != 0 ) {
						max = cb;
						if( get1bit( mp ) != 0 ) {
							xr_value = -ispow[y] * v;
						} else {
							xr_value = ispow[y] * v;
						}
						part2remain--;
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt++ ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
				}
			} while( ++i < 3 );
			/* short (count1table) values */
			for( ; l3 != 0 && (part2remain > 0); l3-- ) {
				final NewHuff htc[] = sHtc;
				final int h = gr_infos.count1table_select;
				final short[] table = htc[h].table;
				int val = 0;// table[ val ]
				short   a;

				while( (a = table[val++]) < 0 ) {
					part2remain--;
					if( part2remain < 0 ) {
						part2remain++;
						a = 0;
						break;
					}
					if( get1bit( mp ) != 0 ) {
						val -= a;
					}
				}
				i = 0;
				do {
					if( 0 == (i & 1) ) {
						if( 0 == mc ) {
							mc = m[moffset++];
							cb = m[moffset++];
							try { v = gr_infos.pow2gain_base[ gr_infos.pow2gain + ((scf[scfoffset++] + pretab[ipretab++]) << shift)];
							} catch( final ArrayIndexOutOfBoundsException ae ) {
								v = gr_infos.pow2gain_base[ gr_infos.pow2gain_base.length - 1 ];
								isbug = true;
							}
						}
						mc--;
					}
					if( (a & (0x8 >> i)) != 0 ) {
						max = cb;
						part2remain--;
						if( part2remain < 0 ) {
							part2remain++;
							break;
						}
						if( get1bit( mp ) != 0 ) {
							xr_value = -v;
						} else {
							xr_value = v;
						}
					} else {
						xr_value = 0.0f;
					}

					if( xrpnt <= xr_endptr ) {
						xr[ xrpnt++ ] = xr_value;
					} else {
						// BUFFER_OVERFLOW_BUG();
						if( ! bobug ) { bobug = true; bobug_sb = cb; bobug_l3 = l3; }
					}
				} while( ++i < 4 );
			}

	        /* zero part */
			// java: already zeroed at start of the function
			/* while( xrpnt <= xr_endptr ) {
				xr[xrpnt++] = 0.0f;
			} */

			gr_infos.maxbandl = max + 1;
			gr_infos.maxb = longLimit[sfreq][gr_infos.maxbandl];
		}

// #undef BUFFER_OVERFLOW_BUG
		if( bobug ) {
			/* well, there was a bug report, where this happened!
			   The problem was, that mixed blocks summed up to over 576,
 			   because of a wrong long/short switching index.
			   It's likely, that the buffer overflow is fixed now, after correcting mixed block map.
			 */
			/*
			System.err.printf
				(
					"hip: OOPS, part2remain=%d l3=%d cb=%d bv=%d region1=%d region2=%d b-type=%d mixed=%d\n"
					,part2remain
					,bobug_l3
					,bobug_sb
					,gr_infos.big_values
					,gr_infos.region1start
					,gr_infos.region2start
					,gr_infos.block_type
					,gr_infos.mixed_block_flag
				);
			 */
		}
		if( isbug ) {
			/* there is a bug report, where there is trouble with IS coded short block gain.
	           Is intensity stereo coding implementation correct? Likely not.
			 */
			int i_stereo = 0;
			if( mp.fr.mode == Mpg123.MPG_MD_JOINT_STEREO ) {
				i_stereo = mp.fr.mode_ext & 0x1;
			}
			/*
			System.err.printf
				(
					"hip: OOPS, 'gainpow2' buffer overflow  lsf=%d i-stereo=%d b-type=%d mixed=%d\n"
					,mp.fr.lsf
					,i_stereo
					,gr_infos.block_type
					,gr_infos.mixed_block_flag
					);
			 */
		}

		while( part2remain > 16 ) {
			mp.getbits( 16 ); /* Dismiss stuffing Bits */
			part2remain -= 16;
		}
		if( part2remain > 0 ) {
			mp.getbits( part2remain );
		} else if( part2remain < 0 ) {
			//System.err.printf("hip: Can't rewind stream by %d bits!\n", -part2remain );
			return true;       /* . error */
		}
		return false;
	}

	/* intensity position, transmitted via a scalefactor value, allowed range is 0 - 15 */
	private static final int scalefac_to_is_pos(final int sf)
	{
		if( 0 <= sf && sf <= 15 ) {
			return sf;
		}
		return (sf < 0 ? 0 : 15);
	}

	/** III_stereo: calculate real channel values for Joint-I-Stereo-mode */
	private static final void III_i_stereo(final float xr_buf[][]/*[2][SBLIMIT][SSLIMIT]*/, final int[] scalefac,
										   final MpgGrInfo gr_infos, final int sfreq, final boolean ms_stereo, final int lsf)
	{
		final float[] xr0 = xr_buf[0];
		final float[] xr1 = xr_buf[1];
		final BandInfoStruct bi = bandInfo[sfreq];
		float[] tabl1, tabl2;

		if( lsf != 0 ) {
			final int p = gr_infos.scalefac_compress & 0x1;
			if( ms_stereo ) {
				tabl1 = pow1_2[p];
				tabl2 = pow2_2[p];
			} else {
				tabl1 = pow1_1[p];
				tabl2 = pow2_1[p];
			}
		} else {
			if( ms_stereo ) {
				tabl1 = tan1_2;
				tabl2 = tan2_2;
			} else {
				tabl1 = tan1_1;
				tabl2 = tan2_1;
			}
		}

		if( gr_infos.block_type == 2 ) {
			boolean do_l = ( gr_infos.mixed_block_flag != 0 );
			final short[] shortDiff = bi.shortDiff;// java
			final short[] shortIdx = bi.shortIdx;// java
			int lwin = 0;
			do { /* process each window */
				/* get first band with zero values */
				int sfb = gr_infos.maxband[lwin]; /* sfb is minimal 3 for mixed mode */
				if( sfb > 3 ) {
					do_l = false;
				}

				for( ; sfb < 12; sfb++ ) {
					int is_p = scalefac[sfb * 3 + lwin - gr_infos.mixed_block_flag]; /* scale: 0-15 */
					is_p = scalefac_to_is_pos( is_p );
					if( is_p != 7 ) {
						int sb = shortDiff[sfb];
						int idx = shortIdx[sfb] + lwin;
						final float t1 = tabl1[is_p];
						final float t2 = tabl2[is_p];
						for( ; sb > 0; sb--, idx += 3 ) {
							final float v = xr0[idx];
							xr0[idx] = v * t1;
							xr1[idx] = v * t2;
						}
					}
				}

// #if 1
				// in the original: copy 10 to 11 , here: copy 11 to 12 maybe still wrong??? (copy 12 to 13?)
				int is_p = scalefac[11 * 3 + lwin - gr_infos.mixed_block_flag]; // scale: 0-15
				int sb = shortDiff[12];
				int idx = shortIdx[12] + lwin;
/* #else
				is_p = scalefac[10 * 3 + lwin - gr_infos.mixed_block_flag]; // scale: 0-15
				sb = bi.shortDiff[11];
				idx = bi.shortIdx[11] + lwin;
#endif */
				is_p = scalefac_to_is_pos( is_p );
				if( is_p != 7 ) {
					final float t1 = tabl1[is_p];
					final float t2 = tabl2[is_p];
					for( ; sb > 0; sb--, idx += 3 ) {
						final float v = xr0[idx];
						xr0[idx] = v * t1;
						xr1[idx] = v * t2;
					}
				}
			} while( ++lwin < 3 ); /* end for(lwin; .. ; . ) */

			if( do_l ) {
				/* also check l-part, if ALL bands in the three windows are 'empty'
				 * and mode = mixed_mode */
				int sfb = gr_infos.maxbandl;
				int idx = bi.longIdx[sfb];

				for( ; sfb < 8; sfb++ ) {
					int sb = bi.longDiff[sfb];
					int is_p = scalefac[sfb]; /* scale: 0-15 */
					is_p = scalefac_to_is_pos( is_p );
					if( is_p != 7 ) {
						final float t1 = tabl1[is_p];
						final float t2 = tabl2[is_p];
						for( ; sb > 0; sb--, idx++ ) {
							final float v = xr0[idx];
							xr0[idx] = v * t1;
							xr1[idx] = v * t2;
						}
					} else {
						idx += sb;
					}
				}
			}
			return;
		}// else {              /* ((gr_infos.block_type != 2)) */
			final short[] longDiff = bi.longDiff;// java
			int sfb = gr_infos.maxbandl;
			int idx = bi.longIdx[sfb];
			for( ; sfb < 21; sfb++ ) {
				int sb = longDiff[sfb];
				int is_p = scalefac[sfb]; /* scale: 0-15 */
				is_p = scalefac_to_is_pos( is_p );
				if( is_p != 7 ) {
					final float t1 = tabl1[is_p];
					final float t2 = tabl2[is_p];
					for( ; sb > 0; sb--, idx++ ) {
						final float v = xr0[idx];
						xr0[idx] = v * t1;
						xr1[idx] = v * t2;
					}
				} else {
					idx += sb;
				}
			}

			int is_p = scalefac[20]; /* copy l-band 20 to l-band 21 */
			is_p = scalefac_to_is_pos( is_p );
			idx = bi.longIdx[21];
			if( is_p != 7 ) {
				final float t1 = tabl1[is_p], t2 = tabl2[is_p];

				for( int sb = longDiff[21]; sb > 0; sb--, idx++ ) {
					final float v = xr0[idx];
					xr0[idx] = v * t1;
					xr1[idx] = v * t2;
				}
			}
		//}                   /* ... */
	}

	private static final void III_antialias(final float xr[]/*[SBLIMIT][SSLIMIT]*/, final MpgGrInfo gr_infos) {
		int sblim;
		if( gr_infos.block_type == 2 ) {
			if( 0 == gr_infos.mixed_block_flag ) {
				return;
			}
			sblim = 1;
		} else {
			sblim = gr_infos.maxb - 1;
		}

		/* 31 alias-reduction operations between each pair of sub-bands */
		/* with 8 butterflies between each pair                         */

		{
			int xr1 = Mpg123.SSLIMIT;// xr[1];// FIXME why to declare as 2 dim array?
			final float[] cs = aa_cs, ca = aa_ca;
			for( int sb = sblim; sb != 0; sb--, xr1 += 10 ) {
				int csoffset = 0;
				int caoffset = 0;
				int xr2 = xr1;// xr1[ xr2 ]

				for( int ss = 7; ss >= 0; ss-- ) { /* upper and lower butterfly inputs */
					final float bu = xr[ --xr2 ], bd = xr[xr1];
					xr[ xr2 ] = (bu * cs[csoffset]) - (bd * ca[caoffset]);
					xr[xr1++] = (bd * cs[csoffset++]) + (bu * ca[caoffset++]);
				}
			}
		}
	}

	/*
	 DCT insipired by Jeff Tsay's DCT from the maplay package
	 this is an optimized version with manual unroll.

	 References:
	 [1] S. Winograd: "On Computing the Discrete Fourier Transform",
	     Mathematics of Computation, Volume 32, Number 141, January 1978,
	     Pages 175-199
	*/
	private static void dct36(final float[] in, final int inoffset,
			final float[] out1, final int o1, final float[] out2, final int o2,
			final float[] wintab, final float[] ts, final int tsoffset) {
		{
			{
				int i = inoffset + 17; int j = i - 1;
				in[i] += in[j]; in[--i] += in[--j]; in[--i] += in[--j];
				in[--i] += in[--j]; in[--i] += in[--j]; in[--i] +=in [--j];
				in[--i] += in[--j]; in[--i] += in[--j]; in[--i] += in[--j];
				in[--i] += in[--j]; in[--i] += in[--j]; in[--i] += in[--j];
				in[--i] += in[--j]; in[--i] += in[--j]; in[--i] += in[--j];
				in[--i] += in[--j]; in[--i] += in[--j];
				i = inoffset + 17; j = i - 2;
				in[i] += in[j]; i -= 4; in[j] += in[i]; j -= 4; in[i] += in[j]; i -= 4; in[j] += in[i]; j -= 4;
				in[i] += in[j]; i -= 4; in[j] += in[i]; j -= 4; in[i] += in[j]; i -= 4; in[j] += in[i];
			}

			{
				final float[] c = COS9;
				final float[] w = wintab;

				final float ta33 = in[inoffset + 2*3+0] * c[3];
				final float ta66 = in[inoffset + 2*6+0] * c[6];
				final float tb33 = in[inoffset + 2*3+1] * c[3];
				final float tb66 = in[inoffset + 2*6+1] * c[6];

				{
					final float tmp1a =                        in[inoffset + 2*1+0] * c[1] + ta33 + in[inoffset + 2*5+0] * c[5] + in[inoffset + 2*7+0] * c[7];
					final float tmp1b =                        in[inoffset + 2*1+1] * c[1] + tb33 + in[inoffset + 2*5+1] * c[5] + in[inoffset + 2*7+1] * c[7];
					final float tmp2a = in[inoffset + 2*0+0] + in[inoffset + 2*2+0] * c[2] + in[inoffset + 2*4+0] * c[4] + ta66 + in[inoffset + 2*8+0] * c[8];
					final float tmp2b = in[inoffset + 2*0+1] + in[inoffset + 2*2+1] * c[2] + in[inoffset + 2*4+1] * c[4] + tb66 + in[inoffset + 2*8+1] * c[8];

					//MACRO1(0);
					{
						float sum0 = tmp1a + tmp2a;
						final float sum1 = (tmp1b + tmp2b) * tfcos36[0];
						//MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+0] = (tmp = sum0 + sum1) * w[27+0];
							out2[o2 + 8-0] = tmp * w[26-0];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-0)] = out1[o1 + 8-0] + sum0 * w[8-0];
						ts[tsoffset + Mpg123.SBLIMIT *(9+0)] = out1[o1 + 9+0] + sum0 * w[9+0];
					}

					//MACRO2(8);
					{
						float sum0 = tmp2a - tmp1a;
						final float sum1 = (tmp2b - tmp1b) * tfcos36[8];
						//MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+8] = (tmp = sum0 + sum1) * w[27+8];
							out2[o2 + 8-8] = tmp * w[26-8];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-8)] = out1[o1 + 8-8] + sum0 * w[8-8];
						ts[tsoffset + Mpg123.SBLIMIT *(9+8)] = out1[o1 + 9+8] + sum0 * w[9+8];
					}
				}

				{
					final float tmp1a = ( in[inoffset + 2*1+0] - in[inoffset + 2*5+0] - in[inoffset + 2*7+0] ) * c[3];
					final float tmp1b = ( in[inoffset + 2*1+1] - in[inoffset + 2*5+1] - in[inoffset + 2*7+1] ) * c[3];
					final float tmp2a = ( in[inoffset + 2*2+0] - in[inoffset + 2*4+0] - in[inoffset + 2*8+0] ) * c[6] - in[inoffset + 2*6+0] + in[inoffset + 2*0+0];
					final float tmp2b = ( in[inoffset + 2*2+1] - in[inoffset + 2*4+1] - in[inoffset + 2*8+1] ) * c[6] - in[inoffset + 2*6+1] + in[inoffset + 2*0+1];

					// MACRO1(1);
					{
						float sum0 = tmp1a + tmp2a;
						final float sum1 = (tmp1b + tmp2b) * tfcos36[1];
						// MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+1] = (tmp = sum0 + sum1) * w[27+1];
							out2[o2 + 8-1] = tmp * w[26-1];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-1)] = out1[o1 + 8-1] + sum0 * w[8-1];
						ts[tsoffset + Mpg123.SBLIMIT *(9+1)] = out1[o1 + 9+1] + sum0 * w[9+1];
					}

					// MACRO2(7);
					{
						float sum0 = tmp2a - tmp1a;
						final float sum1 = (tmp2b - tmp1b) * tfcos36[7];
						//MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+7] = (tmp = sum0 + sum1) * w[27+7];
							out2[o2 + 8-7] = tmp * w[26-7];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-7)] = out1[o1 + 8-7] + sum0 * w[8-7];
						ts[tsoffset + Mpg123.SBLIMIT *(9+7)] = out1[o1 + 9+7] + sum0 * w[9+7];
					}
				}

				{
					final float tmp1a =                        in[inoffset + 2*1+0] * c[5] - ta33 - in[inoffset + 2*5+0] * c[7] + in[inoffset + 2*7+0] * c[1];
					final float tmp1b =                        in[inoffset + 2*1+1] * c[5] - tb33 - in[inoffset + 2*5+1] * c[7] + in[inoffset + 2*7+1] * c[1];
					final float tmp2a = in[inoffset + 2*0+0] - in[inoffset + 2*2+0] * c[8] - in[inoffset + 2*4+0] * c[2] + ta66 + in[inoffset + 2*8+0] * c[4];
					final float tmp2b = in[inoffset + 2*0+1] - in[inoffset + 2*2+1] * c[8] - in[inoffset + 2*4+1] * c[2] + tb66 + in[inoffset + 2*8+1] * c[4];

					// MACRO1(2);
					{
						float sum0 = tmp1a + tmp2a;
						final float sum1 = (tmp1b + tmp2b) * tfcos36[2];
						// MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+2] = (tmp = sum0 + sum1) * w[27+2];
							out2[o2 + 8-2] = tmp * w[26-2];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-2)] = out1[o1 + 8-2] + sum0 * w[8-2];
						ts[tsoffset + Mpg123.SBLIMIT *(9+2)] = out1[o1 + 9+2] + sum0 * w[9+2];
					}

					// MACRO2(6);
					{
						float sum0 = tmp2a - tmp1a;
						final float sum1 = (tmp2b - tmp1b) * tfcos36[6];
						// MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+6] = (tmp = sum0 + sum1) * w[27+6];
							out2[o2 + 8-6] = tmp * w[26-6];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-6)] = out1[o1 + 8-6] + sum0 * w[8-6];
						ts[tsoffset + Mpg123.SBLIMIT *(9+6)] = out1[o1 + 9+6] + sum0 * w[9+6];
					}
				}

				{
					final float tmp1a =                        in[inoffset + 2*1+0] * c[7] - ta33 + in[inoffset + 2*5+0] * c[1] - in[inoffset + 2*7+0] * c[5];
					final float tmp1b =                        in[inoffset + 2*1+1] * c[7] - tb33 + in[inoffset + 2*5+1] * c[1] - in[inoffset + 2*7+1] * c[5];
					final float tmp2a = in[inoffset + 2*0+0] - in[inoffset + 2*2+0] * c[4] + in[inoffset + 2*4+0] * c[8] + ta66 - in[inoffset + 2*8+0] * c[2];
					final float tmp2b = in[inoffset + 2*0+1] - in[inoffset + 2*2+1] * c[4] + in[inoffset + 2*4+1] * c[8] + tb66 - in[inoffset + 2*8+1] * c[2];

					// MACRO1(3);
					{
						float sum0 = tmp1a + tmp2a;
						final float sum1 = (tmp1b + tmp2b) * tfcos36[3];
						// MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+3] = (tmp = sum0 + sum1) * w[27+3];
							out2[o2 + 8-3] = tmp * w[26-3];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-3)] = out1[o1 + 8-3] + sum0 * w[8-3];
						ts[tsoffset + Mpg123.SBLIMIT *(9+3)] = out1[o1 + 9+3] + sum0 * w[9+3];
					}

					// MACRO2(5);
					{
						float sum0 = tmp2a - tmp1a;
						final float sum1 = (tmp2b - tmp1b) * tfcos36[5];
						// MACRO0(v);
						{
							float tmp;
							out2[o2 + 9+5] = (tmp = sum0 + sum1) * w[27+5];
							out2[o2 + 8-5] = tmp * w[26-5];
						}
						sum0 -= sum1;
						ts[tsoffset + Mpg123.SBLIMIT *(8-5)] = out1[o1 + 8-5] + sum0 * w[8-5];
						ts[tsoffset + Mpg123.SBLIMIT *(9+5)] = out1[o1 + 9+5] + sum0 * w[9+5];
					}
				}

				{
					float sum0 =        in[inoffset + 2*0+0] - in[inoffset + 2*2+0] + in[inoffset + 2*4+0] - in[inoffset + 2*6+0] + in[inoffset + 2*8+0];
					final float sum1 = (in[inoffset + 2*0+1] - in[inoffset + 2*2+1] + in[inoffset + 2*4+1] - in[inoffset + 2*6+1] + in[inoffset + 2*8+1] ) * tfcos36[4];
					// MACRO0(4);
					{
						float tmp;
						out2[o2 + 9+4] = (tmp = sum0 + sum1) * w[27+4];
						out2[o2 + 8-4] = tmp * w[26-4];
					}
					sum0 -= sum1;
					ts[tsoffset + Mpg123.SBLIMIT *(8-4)] = out1[o1 + 8-4] + sum0 * w[8-4];
					ts[tsoffset + Mpg123.SBLIMIT *(9+4)] = out1[o1 + 9+4] + sum0 * w[9+4];
				}
			}
		}
	}

	/** new DCT12 */
	private static final void dct12(final float[] in, int inoffset,
			final float[] rawout1, final int out1, final float[] rawout2, final int out2,
			final float[] wi, final float[] ts, final int tsoffset)
	{

		{
			float in0, in1, in2, in3, in4, in5;
			ts[tsoffset + Mpg123.SBLIMIT *0] = rawout1[out1 + 0]; ts[tsoffset + Mpg123.SBLIMIT *1] = rawout1[out1 + 1]; ts[tsoffset + Mpg123.SBLIMIT *2] = rawout1[out1 + 2];
			ts[tsoffset + Mpg123.SBLIMIT *3] = rawout1[out1 + 3]; ts[tsoffset + Mpg123.SBLIMIT *4] = rawout1[out1 + 4]; ts[tsoffset + Mpg123.SBLIMIT *5] = rawout1[out1 + 5];

			// DCT12_PART1
			in5 = in[inoffset + 5*3];
			in5 += (in4 = in[inoffset + 4*3]);
			in4 += (in3 = in[inoffset + 3*3]);
			in3 += (in2 = in[inoffset + 2*3]);
			in2 += (in1 = in[inoffset + 1*3]);
			in1 += (in0 = in[inoffset + 0*3]);

			in5 += in3; in3 += in1;

			in2 *= COS6_1;
			in3 *= COS6_1;

			{
				float tmp0, tmp1 = (in0 - in4);
				{
					final float tmp2 = (in1 - in5) * tfcos12[1];
					tmp0 = tmp1 + tmp2;
					tmp1 -= tmp2;
				}
				ts[tsoffset + (17-1)* Mpg123.SBLIMIT ] = rawout1[out1 + 17-1] + tmp0 * wi[11-1];
				ts[tsoffset + (12+1)* Mpg123.SBLIMIT ] = rawout1[out1 + 12+1] + tmp0 * wi[6+1];
				ts[tsoffset + (6 +1)* Mpg123.SBLIMIT ] = rawout1[out1 + 6 +1] + tmp1 * wi[1];
				ts[tsoffset + (11-1)* Mpg123.SBLIMIT ] = rawout1[out1 + 11-1] + tmp1 * wi[5-1];
			}

			// DCT12_PART2
			in0 += in4 * COS6_2;

			in4 = in0 + in2;
			in0 -= in2;

			in1 += in5 * COS6_2;

			in5 = (in1 + in3) * tfcos12[0];
			in1 = (in1 - in3) * tfcos12[2];

			in3 = in4 + in5;
			in4 -= in5;

			in2 = in0 + in1;
			in0 -= in1;

			ts[tsoffset + (17-0)* Mpg123.SBLIMIT ] = rawout1[out1 + 17-0] + in2 * wi[11-0];
			ts[tsoffset + (12+0)* Mpg123.SBLIMIT ] = rawout1[out1 + 12+0] + in2 * wi[6+0];
			ts[tsoffset + (12+2)* Mpg123.SBLIMIT ] = rawout1[out1 + 12+2] + in3 * wi[6+2];
			ts[tsoffset + (17-2)* Mpg123.SBLIMIT ] = rawout1[out1 + 17-2] + in3 * wi[11-2];

			ts[tsoffset + (6+0)* Mpg123.SBLIMIT ]  = rawout1[out1 + 6+0] + in0 * wi[0];
			ts[tsoffset + (11-0)* Mpg123.SBLIMIT ] = rawout1[out1 + 11-0] + in0 * wi[5-0];
			ts[tsoffset + (6+2)* Mpg123.SBLIMIT ]  = rawout1[out1 + 6+2] + in4 * wi[2];
			ts[tsoffset + (11-2)* Mpg123.SBLIMIT ] = rawout1[out1 + 11-2] + in4 * wi[5-2];
		}

		inoffset++;

		{
			float in0, in1, in2, in3, in4, in5;

			// DCT12_PART1
			in5 = in[inoffset + 5*3];
			in5 += (in4 = in[inoffset + 4*3]);
			in4 += (in3 = in[inoffset + 3*3]);
			in3 += (in2 = in[inoffset + 2*3]);
			in2 += (in1 = in[inoffset + 1*3]);
			in1 += (in0 = in[inoffset + 0*3]);

			in5 += in3; in3 += in1;

			in2 *= COS6_1;
			in3 *= COS6_1;

			{
				float tmp0, tmp1 = (in0 - in4);
				{
					final float tmp2 = (in1 - in5) * tfcos12[1];
					tmp0 = tmp1 + tmp2;
					tmp1 -= tmp2;
				}
				rawout2[out2 + 5-1] = tmp0 * wi[11-1];
				rawout2[out2 + 0+1] = tmp0 * wi[6+1];
				ts[tsoffset + (12+1)* Mpg123.SBLIMIT ] += tmp1 * wi[1];
				ts[tsoffset + (17-1)* Mpg123.SBLIMIT ] += tmp1 * wi[5-1];
			}

			// DCT12_PART2
			in0 += in4 * COS6_2;

			in4 = in0 + in2;
			in0 -= in2;

			in1 += in5 * COS6_2;

			in5 = (in1 + in3) * tfcos12[0];
			in1 = (in1 - in3) * tfcos12[2];

			in3 = in4 + in5;
			in4 -= in5;

			in2 = in0 + in1;
			in0 -= in1;

			rawout2[out2 + 5-0] = in2 * wi[11-0];
			rawout2[out2 + 0+0] = in2 * wi[6+0];
			rawout2[out2 + 0+2] = in3 * wi[6+2];
			rawout2[out2 + 5-2] = in3 * wi[11-2];

			ts[tsoffset + (12+0)* Mpg123.SBLIMIT ] += in0 * wi[0];
			ts[tsoffset + (17-0)* Mpg123.SBLIMIT ] += in0 * wi[5-0];
			ts[tsoffset + (12+2)* Mpg123.SBLIMIT ] += in4 * wi[2];
			ts[tsoffset + (17-2)* Mpg123.SBLIMIT ] += in4 * wi[5-2];
		}

		inoffset++;

		{
			float in0, in1, in2, in3, in4, in5;
			rawout2[out2 + 12] = rawout2[out2 + 13] = rawout2[out2 + 14] = rawout2[out2 + 15] = rawout2[out2 + 16] = rawout2[out2 + 17] = 0.0f;

			// DCT12_PART1
			in5 = in[inoffset + 5*3];
			in5 += (in4 = in[inoffset + 4*3]);
			in4 += (in3 = in[inoffset + 3*3]);
			in3 += (in2 = in[inoffset + 2*3]);
			in2 += (in1 = in[inoffset + 1*3]);
			in1 += (in0 = in[inoffset + 0*3]);

			in5 += in3; in3 += in1;

			in2 *= COS6_1;
			in3 *= COS6_1;

			{
				float tmp0, tmp1 = (in0 - in4);
				{
					final float tmp2 = (in1 - in5) * tfcos12[1];
					tmp0 = tmp1 + tmp2;
					tmp1 -= tmp2;
				}
				rawout2[out2 + 11-1] = tmp0 * wi[11-1];
				rawout2[out2 + 6 +1] = tmp0 * wi[6+1];
				rawout2[out2 + 0+1] += tmp1 * wi[1];
				rawout2[out2 + 5-1] += tmp1 * wi[5-1];
			}

			// DCT12_PART2
			in0 += in4 * COS6_2;

			in4 = in0 + in2;
			in0 -= in2;

			in1 += in5 * COS6_2;

			in5 = (in1 + in3) * tfcos12[0];
			in1 = (in1 - in3) * tfcos12[2];

			in3 = in4 + in5;
			in4 -= in5;

			in2 = in0 + in1;
			in0 -= in1;

			rawout2[out2 + 11-0] = in2 * wi[11-0];
			rawout2[out2 + 6 +0] = in2 * wi[6+0];
			rawout2[out2 + 6 +2] = in3 * wi[6+2];
			rawout2[out2 + 11-2] = in3 * wi[11-2];

			rawout2[out2 + 0+0] += in0 * wi[0];
			rawout2[out2 + 5-0] += in0 * wi[5-0];
			rawout2[out2 + 0+2] += in4 * wi[2];
			rawout2[out2 + 5-2] += in4 * wi[5-2];
		}
	}

	/** III_hybrid */
	private static final void III_hybrid(final MpStrTag mp, final float fsIn[]/*[SBLIMIT][SSLIMIT]*/, final float tsOut[]/*[SSLIMIT][SBLIMIT]*/,
										 final int ch, final MpgGrInfo gr_infos)
	{
		int tspnt = 0;// tsOut[ tspnt ]
		final int[] blc = mp.hybrid_blc;
		int sb = 0;

		int b = blc[ch];
		final float[] block1 = mp.hybrid_block[b][ch];
		int rawout1 = 0;// block[ rawout1 ]
		b = -b + 1;
		final float[] block2 = mp.hybrid_block[b][ch];
		int rawout2 = 0;// block[ rawout2 ]
		blc[ch] = b;

		if( gr_infos.mixed_block_flag != 0 ) {
			sb = 2 * Mpg123.SSLIMIT;
			dct36( fsIn, 0, block1, rawout1, block2, rawout2, win[0], tsOut, tspnt++ );
			rawout1 += 18;
			rawout2 += 18;
			dct36( fsIn, Mpg123.SSLIMIT, block1, rawout1, block2, rawout2, win1[0], tsOut, tspnt++ );
			rawout1 += 18;
			rawout2 += 18;
		}

		final int bt = gr_infos.block_type;
		if( bt == 2 ) {
			for(final int maxb = gr_infos.maxb * Mpg123.SSLIMIT; sb < maxb; ) {
				dct12( fsIn, sb, block1, rawout1, block2, rawout2, win[2], tsOut, tspnt++ );
				sb += Mpg123.SSLIMIT;
				rawout1 += 18;
				rawout2 += 18;
				dct12( fsIn, sb, block1, rawout1, block2, rawout2, win1[2], tsOut, tspnt++ );
				sb += Mpg123.SSLIMIT;
				rawout1 += 18;
				rawout2 += 18;
			}
		} else {
			for(final int maxb = gr_infos.maxb * Mpg123.SSLIMIT; sb < maxb; ) {
				dct36( fsIn, sb, block1, rawout1, block2, rawout2, win[bt], tsOut, tspnt++ );
				sb += Mpg123.SSLIMIT;
				rawout1 += 18;
				rawout2 += 18;
				dct36( fsIn, sb, block1, rawout1, block2, rawout2, win1[bt], tsOut, tspnt++ );
				sb += Mpg123.SSLIMIT;
				rawout1 += 18;
				rawout2 += 18;
			}
		}
		for(; tspnt < Mpg123.SBLIMIT; tspnt++ ) {
			for(int i = tspnt, ie = tspnt + Mpg123.SSLIMIT * Mpg123.SBLIMIT; i < ie; i += Mpg123.SBLIMIT ) {
				tsOut[ i ] = block1[ rawout1++ ];
				block2[ rawout2++ ] = 0.0f;
			}
		}
	}

	/** main layer3 handler */
	static final int layer3_audiodata_precedesframes(final MpStrTag mp) {
		/* specific to Layer 3, since Layer 1 & 2 the audio data starts at the frame that describes it. */
		/* determine how many bytes and therefore bitstream frames the audio data precedes it's matching frame */
		/* lame_report_fnc(mp.report_err, "hip: main_data_begin = %d, mp.bsize %d, mp.fsizeold %d, mp.ssize %d\n",
		   sideinfo.main_data_begin, mp.bsize, mp.fsizeold, mp.ssize); */
		/* compute the number of frames to backtrack, 4 for the header, ssize already holds the CRC */
		/* TODO Erroneously assumes current frame is same as previous frame. */
		final int audioDataInFrame = mp.bsize - 4 - mp.ssize;
		final int framesToBacktrack = (mp.sideinfo.main_data_begin + audioDataInFrame - 1) / audioDataInFrame;
		/* lame_report_fnc(mp.report_err, "hip: audioDataInFrame %d framesToBacktrack %d\n", audioDataInFrame, framesToBacktrack); */
		return framesToBacktrack;
	}

	static final int decode_layer3_sideinfo(final MpStrTag mp) {
		final Frame frame = mp.fr;
		final int stereo = frame.stereo;
		int single = frame.single;

		if( stereo == 1 ) {  /* stream is mono */
			single = 0;
		}

		boolean ms_stereo;
		if( frame.mode == Mpg123.MPG_MD_JOINT_STEREO ) {
			ms_stereo = (frame.mode_ext & 0x2) != 0;
		} else {
			ms_stereo = false;
		}

		int granules;
		if( frame.lsf != 0 ) {
			granules = 1;
			III_get_side_info_2( mp, stereo, ms_stereo, frame.sampling_frequency, single );
		} else {
			granules = 2;
			III_get_side_info_1( mp, stereo, ms_stereo, frame.sampling_frequency, single );
		}

		int databits = 0;
		final CH[] chs = mp.sideinfo.ch;// java
		for( int gr = 0; gr < granules; ++gr ) {
			for( int ch = 0; ch < stereo; ++ch ) {
				final MpgGrInfo gr_infos = chs[ch].gr[gr];
				databits += gr_infos.part2_3_length;
			}
		}
		return databits - (mp.sideinfo.main_data_begin << 3);
	}

	static final int decode_layer3_frame(final MpStrTag mp, final Object pcm_sample, final int[] pcm_point, final Synth synth) {
		if( mp.set_pointer( mp.sideinfo.main_data_begin ) == Mpg123.MP3_ERR ) {
			return 0;
		}

		int clip = 0;
		final int scalefacs[][] = new int[2][39]; /* max 39 for short[13][3] mode, mixed: 38, long: 22 */
		/*  struct III_sideinfo sideinfo; */
		final Frame frame = mp.fr;
		final int stereo = frame.stereo;
		int single = frame.single;
		final int  sfreq = frame.sampling_frequency;
		final float hybridIn[][] = new float[2][Mpg123.SBLIMIT * Mpg123.SSLIMIT];// FIXME why 3-dim hybridIn[2][SBLIMIT][SSLIMIT];?
		final float[] hybridIn0 = hybridIn[0], hybridIn1 = hybridIn[1];// java
		final float hybridOut[][] = new float[2][Mpg123.SSLIMIT * Mpg123.SBLIMIT];// FIXME why 3-dim hybridOut[2][SSLIMIT][SBLIMIT];?
		final float[] hybridOut0 = hybridOut[0], hybridOut1 = hybridOut[1];// java
		int stereo1;
		if( stereo == 1 ) {  /* stream is mono */
			stereo1 = 1;
			single = 0;
		} else if( single >= 0 ) {
			stereo1 = 1;
		} else {
			stereo1 = 2;
		}

		boolean ms_stereo, i_stereo;
		if( frame.mode == Mpg123.MPG_MD_JOINT_STEREO ) {
			ms_stereo = (frame.mode_ext & 0x2) != 0;
			i_stereo = (frame.mode_ext & 0x1) != 0;
		} else {
			ms_stereo = i_stereo = false;
		}

		final int granules = ( frame.lsf != 0 ) ? 1 : 2;

		final CH[] chs = mp.sideinfo.ch;// java
		final int p1[] = new int[1];// TODO java: find a better way
		int gr = 0;
		do {
			{
				final MpgGrInfo gr_infos = chs[0].gr[gr];
				int part2bits;

				if( frame.lsf != 0 ) {
					part2bits = III_get_scale_factors_2( mp, scalefacs[0], gr_infos, false );
				} else {
					part2bits = III_get_scale_factors_1( mp, scalefacs[0], gr_infos );
				}

				/* lame_report_fnc(mp.report_err, "calling III dequantize sample 1 gr_infos.part2_3_length %d\n", gr_infos.part2_3_length); */
				if( III_dequantize_sample( mp, hybridIn0, scalefacs[0], gr_infos, sfreq, part2bits ) ) {
					return clip;
				}
			}
			if( stereo == 2 ) {
				final MpgGrInfo gr_infos = chs[1].gr[gr];
				int part2bits;
				if( frame.lsf != 0 ) {
					part2bits = III_get_scale_factors_2( mp, scalefacs[1], gr_infos, i_stereo );
				} else {
					part2bits = III_get_scale_factors_1( mp, scalefacs[1], gr_infos );
				}

				/* lame_report_fnc(mp.report_err, "calling III dequantize sample 2  gr_infos.part2_3_length %d\n", gr_infos.part2_3_length); */
				if( III_dequantize_sample( mp, hybridIn1, scalefacs[1], gr_infos, sfreq, part2bits ) ) {
					return clip;
				}

				if( ms_stereo ) {
					for(int i = 0; i < Mpg123.SBLIMIT * Mpg123.SSLIMIT; i++ ) {
						final float tmp0 = hybridIn0[i];
						final float tmp1 = hybridIn1[i];
						hybridIn1[i] = tmp0 - tmp1;
						hybridIn0[i] = tmp0 + tmp1;
					}
				}

				if( i_stereo ) {
					III_i_stereo( hybridIn, scalefacs[1], gr_infos, sfreq, ms_stereo, frame.lsf );
				}

				if( ms_stereo || i_stereo || (single == 3) ) {
					if( gr_infos.maxb > chs[0].gr[gr].maxb ) {
						chs[0].gr[gr].maxb = gr_infos.maxb;
					} else {
						gr_infos.maxb = chs[0].gr[gr].maxb;
					}
				}

				switch( single ) {
				case 3: {
					for(int inoffset0 = 0, inoffset1 = 0, ie = Mpg123.SSLIMIT * gr_infos.maxb; inoffset0 < ie; inoffset0++ ) {
						hybridIn0[inoffset0] = (hybridIn0[inoffset0] + hybridIn1[inoffset1++]); /* *0.5 done by pow-scale */
					}
					break;
				}
				case 1: {
					for(int inoffset0 = 0, inoffset1 = 0, ie = Mpg123.SSLIMIT * gr_infos.maxb; inoffset0 < ie; ) {
						hybridIn0[inoffset0++] = hybridIn1[inoffset1++];
					}
					break;
				}
				}
			}

			for( int ch = 0; ch < stereo1; ch++ ) {
				final MpgGrInfo gr_infos = chs[ch].gr[gr];
				III_antialias( hybridIn[ch], gr_infos );
				III_hybrid( mp, hybridIn[ch], hybridOut[ch], ch, gr_infos );
			}
			int ss = 0;
			do {
				if( single >= 0 ) {
					clip += synth.synth_1to1_mono( mp, hybridOut0, ss, pcm_sample, pcm_point );
				} else {
					p1[0] = pcm_point[0];
					clip += synth.synth_1to1( mp, hybridOut0, ss, 0, pcm_sample, p1 );
					clip += synth.synth_1to1( mp, hybridOut1, ss, 1, pcm_sample, pcm_point );
				}
				ss += Mpg123.SBLIMIT;
			} while( ss < Mpg123.SSLIMIT * Mpg123.SBLIMIT );
		} while( ++gr < granules );

		return clip;
	}
}
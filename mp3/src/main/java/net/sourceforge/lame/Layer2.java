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

// layer2.c

class Layer2 {

	private static boolean gd_are_hip_tables_layer2_initialized = false;

	// java: already zeroed
	private static final byte grp_3tab[] = new byte[32 * 3];// = { 0, }; /* used: 27 */
	private static final byte grp_5tab[] = new byte[128 * 3];// = { 0, }; /* used: 125 */
	private static final byte grp_9tab[] = new byte[1024 * 3];// = { 0, }; /* used: 729 */

	private static final double mulmul[] = {// [27] = {
			0.0, -2.0 / 3.0, 2.0 / 3.0,
			2.0 / 7.0, 2.0 / 15.0, 2.0 / 31.0, 2.0 / 63.0, 2.0 / 127.0, 2.0 / 255.0,
			2.0 / 511.0, 2.0 / 1023.0, 2.0 / 2047.0, 2.0 / 4095.0, 2.0 / 8191.0,
			2.0 / 16383.0, 2.0 / 32767.0, 2.0 / 65535.0,
			-4.0 / 5.0, -2.0 / 5.0, 2.0 / 5.0, 4.0 / 5.0,
			-8.0 / 9.0, -4.0 / 9.0, -2.0 / 9.0, 2.0 / 9.0, 4.0 / 9.0, 8.0 / 9.0
		};
	private static final byte base[][] = {// [3][9] = {
			{1, 0, 2,},
			{17, 18, 0, 19, 20,},
			{21, 1, 22, 23, 0, 24, 25, 2, 26}
		};

	static final void hip_init_tables_layer2() {
		if( gd_are_hip_tables_layer2_initialized ) {
			return;
		}
		gd_are_hip_tables_layer2_initialized = true;
		// final int tablen[] = { 3, 5, 9 };// java: tables[i].length
		final byte grp_tables[][] = { grp_3tab, grp_5tab, grp_9tab };
		for( int i = 0; i < 3; i++ ) {
			final byte[] t = grp_tables[i];// java
			int itable = 0;
			final int len = base[i].length;// tablen[i];
			for( int j = 0; j < len; j++ ) {
				for( int k = 0; k < len; k++ ) {
					for( int l = 0; l < len; l++ ) {
						t[itable++] = base[i][l];
						t[itable++] = base[i][k];
						t[itable++] = base[i][j];
					}
				}
			}
		}

		for( int k = 0; k < 27; k++ ) {
			final double m = mulmul[k];
			final float table[] = MpStrTag.muls[k];
			int ti = 0;
			for( int j = 3, i = 0; i < 63; i++, j-- ) {
				table[ti++] = (float) (m * Math.pow( 2.0, (double) j / 3.0 ) );
			}
			table[ti++] = 0.0f;
		}
	}

	/* private static final byte dummy_table[] = { 0, 0, 0 };
	private static final byte[] grp_table_select(final short d1, int idx) {// java: extracted iplace to avoid extra ops
		// RH: it seems to be common, that idx is larger than the table's sizes.
		// is it OK to return a zero vector in this case? FIXME

		int x;
		switch( d1 ) {
		case 3:
			x = 3 * 3 * 3;
			idx = idx < x ? idx : x;
			return &grp_3tab[3 * idx];
		case 5:
			x = 5 * 5 * 5;
			idx = idx < x ? idx : x;
			return &grp_5tab[3 * idx];
		case 9:
			x = 9 * 9 * 9;
			idx = idx < x ? idx : x;
			return &grp_9tab[3 * idx];
		default:
			// fatal error
			// assert( 0 );
			return 0;
		}
		return &dummy_table[0];
	} */

	private static final class SideInfoLayerII {
		private final byte allocation[][] = new byte[Mpg123.SBLIMIT][2];
		private final byte scalefactor[][][] = new byte[Mpg123.SBLIMIT][2][3]; /* subband / channel / block */
	};

	private static final void II_step_one(final MpStrTag mp, final SideInfoLayerII si, final Frame frame) {
		final int nch = frame.stereo;
		final int sblimit = frame.II_sblimit;
		int jsbound = (frame.mode == Mpg123.MPG_MD_JOINT_STEREO) ? (frame.mode_ext << 2) + 4 : frame.II_sblimit;
		final ALTable2[] fr_alloc = frame.alloc;
		int alloc1 = 0;// fr_alloc[ alloc1 ]
		final byte scfsi[][] = new byte[Mpg123.SBLIMIT][2];

		// si.clear();// java don't need, values will be rewritten
		if( jsbound > sblimit ) {
			jsbound = sblimit;
		}
		final byte[][] allocation = si.allocation;// java
		final byte[][][] scalefactor = si.scalefactor;// java
		if( nch == 2 ) {
			for( int i = 0; i < jsbound; ++i ) {
				final short step = fr_alloc[ alloc1 ].bits;
				final byte b0 = mp.get_leq_8_bits( step );
				final byte b1 = mp.get_leq_8_bits( step );
				alloc1 += (1 << step );
				allocation[i][0] = b0;
				allocation[i][1] = b1;
			}
			for( int i = jsbound; i < sblimit; ++i ) {
				final short step = fr_alloc[ alloc1 ].bits;
				final byte b0 = mp.get_leq_8_bits( step );
				alloc1 += (1 << step );
				allocation[i][0] = b0;
				allocation[i][1] = b0;
			}
			for( int i = 0; i < sblimit; ++i ) {
				final byte n0 = allocation[i][0];
				final byte n1 = allocation[i][1];
				final byte b0 = n0 != 0 ? mp.get_leq_8_bits( 2 ) : 0;
				final byte b1 = n1 != 0 ? mp.get_leq_8_bits( 2 ) : 0;
				scfsi[i][0] = b0;
				scfsi[i][1] = b1;
			}
		} else {              /* mono */
			for( int i = 0; i < sblimit; ++i ) {
				final short step = fr_alloc[ alloc1 ].bits;
				final byte b0 = mp.get_leq_8_bits( step );
				alloc1 += (1 << step );
				allocation[i][0] = b0;
			}
			for( int i = 0; i < sblimit; ++i ) {
				final byte n0 = allocation[i][0];
				final byte b0 = n0 != 0 ? mp.get_leq_8_bits( 2 ) : 0;
				scfsi[i][0] = b0;
			}
		}
		for( int i = 0; i < sblimit; ++i ) {
			final byte[][] scalefactor_i = scalefactor[i];// java
			final byte[] allocation_i = allocation[i];// java
			final byte[] scfsi_i = scfsi[i];// java
			for( int ch = 0; ch < nch; ++ch ) {
				byte s0 = 0, s1 = 0, s2 = 0;
				if( allocation_i[ch] != 0 ) {
					switch( scfsi_i[ch] ) {
					case 0:
						s0 = mp.get_leq_8_bits( 6 );
						s1 = mp.get_leq_8_bits( 6 );
						s2 = mp.get_leq_8_bits( 6 );
						break;
					case 1:
						s0 = mp.get_leq_8_bits( 6 );
						s1 = s0;
						s2 = mp.get_leq_8_bits( 6 );
						break;
					case 2:
						s0 = mp.get_leq_8_bits( 6 );
						s1 = s0;
						s2 = s0;
						break;
					case 3:
						s0 = mp.get_leq_8_bits( 6 );
						s1 = mp.get_leq_8_bits( 6 );
						s2 = s1;
						break;
					default:
						//assert( 0 );
						break;
					}
				}
				final byte[] scalefactor_i_ch = scalefactor_i[ch];// java
				scalefactor_i_ch[0] = s0;
				scalefactor_i_ch[1] = s1;
				scalefactor_i_ch[2] = s2;
			}
		}
	}

	private static final void II_step_two(final MpStrTag mp, final SideInfoLayerII si, final Frame frame, final int gr, final float fraction[][][]/*[2][4][SBLIMIT]*/) {
		final ALTable2[] fr_alloc = frame.alloc;
		int alloc1 = 0;// fr_alloc[ alloc1 ]
		int sblimit = frame.II_sblimit;
		int jsbound = (frame.mode == Mpg123.MPG_MD_JOINT_STEREO) ? (frame.mode_ext << 2) + 4 : frame.II_sblimit;
		final int nch = frame.stereo;
		float r0, r1, r2;

		if( jsbound > sblimit ) {
			jsbound = sblimit;
		}

		final float[][] muls = MpStrTag.muls;// java
		final byte[][] allocation = si.allocation;// java
		final byte[][][] scalefactor = si.scalefactor;// java
		for( int i = 0; i < jsbound; ++i ) {
			final byte[] allocation_i = allocation[i];// java
			final byte[][] scalefactor_i = scalefactor[i];// java
			final short step = fr_alloc[ alloc1 ].bits;
			for( int ch = 0; ch < nch; ++ch ) {
				final int ba = (int)allocation_i[ch];
				if( ba != 0 ) {
					int x1 = (int)scalefactor_i[ch][gr];
					final ALTable2 alloc2 = fr_alloc[alloc1 + ba];
					short k = alloc2.bits;
					final short d1 = alloc2.d;
					k = (k <= 16) ? k : 16;
					x1 = (x1 < 64) ? x1 : 63;
					if( d1 < 0 ) {
						final int v0 = mp.getbits( k );
						final int v1 = mp.getbits( k );
						final int v2 = mp.getbits( k );
						final float cm = muls[k][x1];
						r0 = (v0 + d1) * cm;
						r1 = (v1 + d1) * cm;
						r2 = (v2 + d1) * cm;
					} else {
						int idx = mp.getbits( k );
						// final unsigned char *tab = common.grp_table_select( d1, idx );
						//
						byte[] tab;
						switch( d1 ) {
						case 3:
							idx = idx < 3 * 3 * 3 ? idx * 3 : 3 * 3 * 3 * 3;
							//idx *= 3;
							tab = grp_3tab;
							break;
						case 5:
							idx = idx < 5 * 5 * 5 ? idx * 3 : 5 * 5 * 5 * 3;
							//idx *= 3;
							tab = grp_5tab;
							break;
						case 9:
							idx = idx < 9 * 9 * 9 ? idx * 3 : 9 * 9 * 9 * 3;
							//idx *= 3;
							tab = grp_9tab;
							break;
						default:
							/* fatal error */
							// assert( 0 );
							tab = grp_3tab;// java: to avoid warning
							break;
						}
						//
						final int k0 = (int)tab[idx++];
						final int k1 = (int)tab[idx++];
						final int k2 = (int)tab[idx  ];
						r0 = muls[k0][x1];
						r1 = muls[k1][x1];
						r2 = muls[k2][x1];
					}
					final float[][] fraction_ch = fraction[ch];// java
					fraction_ch[0][i] = r0;
					fraction_ch[1][i] = r1;
					fraction_ch[2][i] = r2;
				} else {
					final float[][] fraction_ch = fraction[ch];// java
					fraction_ch[0][i] = fraction_ch[1][i] = fraction_ch[2][i] = 0.0f;
				}
			}
			alloc1 += (1 << step );
		}

		for( int i = jsbound; i < sblimit; i++ ) {
			final short step = fr_alloc[ alloc1 ].bits;
			final int ba = (int)allocation[i][0];
			if( ba != 0 ) {
				final ALTable2 alloc2 = fr_alloc[ alloc1 + ba ];
				short k = alloc2.bits;
				final short d1 = alloc2.d;
				// assert( k <= 16  );
				k = (k <= 16) ? k : 16;// FIXME why min?
				if( d1 < 0 ) {
					final int v0 = mp.getbits( k );
					final int v1 = mp.getbits( k );
					final int v2 = mp.getbits( k );
					for( int ch = 0; ch < nch; ++ch ) {
						int x1 = (int)scalefactor[i][ch][gr];
						// assert( x1 < 64  );
						x1 = (x1 < 64) ? x1 : 63;// FIXME why min?
						final float cm = muls[k][x1];
						r0 = (v0 + d1) * cm;
						r1 = (v1 + d1) * cm;
						r2 = (v2 + d1) * cm;
						fraction[ch][0][i] = r0;
						fraction[ch][1][i] = r1;
						fraction[ch][2][i] = r2;
					}
				} else {
					int idx = mp.getbits( k );
					// final unsigned char *tab = common.grp_table_select( d1, idx );
					//
					byte[] tab;
					switch( d1 ) {
					case 3:
						idx = idx < 3 * 3 * 3 ? idx * 3 : 3 * 3 * 3 * 3;
						//idx *= 3;
						tab = grp_3tab;
						break;
					case 5:
						idx = idx < 5 * 5 * 5 ? idx * 3 : 5 * 5 * 5 * 3;
						//idx *= 3;
						tab = grp_5tab;
						break;
					case 9:
						idx = idx < 9 * 9 * 9 ? idx * 3 : 9 * 9 * 9 * 3;
						//idx *= 3;
						tab = grp_9tab;
						break;
					default:
						/* fatal error */
						// assert( 0 );
						tab = grp_3tab;// java: to avoid warning
						break;
					}
					//
					final int k0 = (int)tab[idx + 0];
					final int k1 = (int)tab[idx + 1];
					final int k2 = (int)tab[idx + 2];
					for( int ch = 0; ch < nch; ++ch ) {
						byte x1 = scalefactor[i][ch][gr];
						//assert( x1 < 64  );
						x1 = (x1 < 64) ? x1 : 63;// FIXME why min?
						r0 = muls[k0][x1];
						r1 = muls[k1][x1];
						r2 = muls[k2][x1];
						final float[][] fraction_ch = fraction[ch];// java
						fraction_ch[0][i] = r0;
						fraction_ch[1][i] = r1;
						fraction_ch[2][i] = r2;
					}
				}
			} else {
				fraction[0][0][i] = fraction[0][1][i] = fraction[0][2][i] = 0.0f;
				fraction[1][0][i] = fraction[1][1][i] = fraction[1][2][i] = 0.0f;
			}
			alloc1 += (1 << step );
		}
		if( sblimit > frame.down_sample_sblimit ) {
			sblimit = frame.down_sample_sblimit;
		}
		for( int ch = 0; ch < nch; ++ch ) {
			for(int i = sblimit; i < Mpg123.SBLIMIT; ++i ) {
				final float[][] fraction_ch = fraction[ch];// java
				fraction_ch[0][i] = fraction_ch[1][i] = fraction_ch[2][i] = 0.0f;
			}
		}
	}

	private static final int translate[][][] = //[3][2][16] =
		{ { { 0,2,2,2,2,2,2,0,0,0,1,1,1,1,1,0 } ,
		{ 0,2,2,0,0,0,1,1,1,1,1,1,1,1,1,0 } } ,
		{ { 0,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0 } ,
		{ 0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0 } } ,
		{ { 0,3,3,3,3,3,3,0,0,0,1,1,1,1,1,0 } ,
		{ 0,3,3,0,0,0,1,1,1,1,1,1,1,1,1,0 } } };

	private static final ALTable2 alloc_0[] = {
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3), new ALTable2(4, -7), new ALTable2(5, -15),
		new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10, -511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383),
		new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191),
		new ALTable2(15, -16383), new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3),
		new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095),
		new ALTable2(14, -8191), new ALTable2(15, -16383), new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3),
		new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15),
		new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10, -511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767), new ALTable2(4, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9),
		new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095),
		new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3),
		new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047),
		new ALTable2(13, -4095), new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5),
		new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31),
		new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023),
		new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767), new ALTable2(4, 0), new ALTable2(5, 3),
		new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15),
		new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10, -511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767), new ALTable2(4, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9),
		new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095),
		new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3),
		new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5),
		new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9),
		new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3),
		new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15),
		new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3),
		new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5),
		new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9),
		new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3),
		new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15),
		new ALTable2(16, -32767), new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3),
		new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(3, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7),
		new ALTable2(5, -15), new ALTable2(16, -32767), new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5),
		new ALTable2(16, -32767), new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767), new ALTable2(2, 0),
		new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767)
	};

	private static final ALTable2 alloc_1[] = {
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10,
		-511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10,
		-511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(3, -3), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255), new ALTable2(10,
		-511),
		new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(16, -32767)
	};

	private static final ALTable2 alloc_2[] = {
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63)
	};

	private static final ALTable2 alloc_3[] = {
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127), new ALTable2(9, -255),
		new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191), new ALTable2(15, -16383),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63)
	};

	private static final ALTable2 alloc_4[] = {
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191),
		new ALTable2(4, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(3, -3), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63), new ALTable2(8, -127),
		new ALTable2(9, -255), new ALTable2(10, -511), new ALTable2(11, -1023), new ALTable2(12, -2047), new ALTable2(13, -4095), new ALTable2(14, -8191),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(3, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9), new ALTable2(4, -7), new ALTable2(5, -15), new ALTable2(6, -31), new ALTable2(7, -63),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9),
		new ALTable2(2, 0), new ALTable2(5, 3), new ALTable2(7, 5), new ALTable2(10, 9)
	};

	private static final ALTable2 tables[/*5*/][] = { alloc_0, alloc_1, alloc_2, alloc_3, alloc_4 };
	private static final int sblims[/*5*/] = { 27, 30, 8, 12, 30 };

	private static final void II_select_table(final Frame fr) {
		int table;

		if( fr.lsf != 0 ) {
			table = 4;
		} else {
			table = translate[fr.sampling_frequency][2 - fr.stereo][fr.bitrate_index];
		}
		final int sblim = sblims[table];

		fr.alloc = tables[table];
		fr.II_sblimit = sblim;
	}

	@SuppressWarnings("static-method")
	final int decode_layer2_sideinfo() {
		/* FIXME: extract side information and check values */
		return 0;
	}

	static final int decode_layer2_frame(final MpStrTag mp, final Object pcm_sample, final int[] pcm_point, final Synth synth) {// java: added synth to fix the bug
		final float fraction[][][] = new float[2][4][Mpg123.SBLIMIT]; /* pick_table clears unused subbands */
		final SideInfoLayerII si = new SideInfoLayerII();
		final Frame frame = mp.fr;
		int single = frame.single;
		int clip = 0;

		II_select_table( frame );
		II_step_one( mp, si, frame );

		if( frame.stereo == 1 || single == 3 ) {
			single = 0;
		}

		if( single >= 0 ) {
			final float[] fraction0 = fraction[single][0];// java
			final float[] fraction1 = fraction[single][1];// java
			final float[] fraction2 = fraction[single][2];// java
			int i = 0;
			do {
				II_step_two( mp, si, frame, i >> 2, fraction );
				clip += synth.synth_1to1_mono( mp, fraction0, 0, pcm_sample, pcm_point );
				clip += synth.synth_1to1_mono( mp, fraction1, 0, pcm_sample, pcm_point );
				clip += synth.synth_1to1_mono( mp, fraction2, 0, pcm_sample, pcm_point );
			} while( ++i < Mpg123.SCALE_BLOCK );
			return clip;
		}// else {
			final float[] fraction00 = fraction[0][0];// java
			final float[] fraction10 = fraction[1][0];// java
			final float[] fraction01 = fraction[0][1];// java
			final float[] fraction11 = fraction[1][1];// java
			final float[] fraction02 = fraction[0][2];// java
			final float[] fraction12 = fraction[1][2];// java
			final int p1[] = new int[1];// TODO java: find better way
			int i = 0;
			do {
				II_step_two( mp, si, frame, i >> 2, fraction );
				p1[0] = pcm_point[0];
				clip += synth.synth_1to1( mp, fraction00, 0, 0, pcm_sample, p1 );
				clip += synth.synth_1to1( mp, fraction10, 0, 1, pcm_sample, pcm_point );
				p1[0] = pcm_point[0];
				clip += synth.synth_1to1( mp, fraction01, 0, 0, pcm_sample, p1 );
				clip += synth.synth_1to1( mp, fraction11, 0, 1, pcm_sample, pcm_point );
				p1[0] = pcm_point[0];
				clip += synth.synth_1to1( mp, fraction02, 0, 0, pcm_sample, p1 );
				clip += synth.synth_1to1( mp, fraction12, 0, 1, pcm_sample, pcm_point );
			} while( ++i < Mpg123.SCALE_BLOCK );
		//}

		return clip;
	}
}
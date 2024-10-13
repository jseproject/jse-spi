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

// layer1.c

class Layer1 {
	private static final class SideInfoLayerI {
		private final byte allocation[][] = new byte[Mpg123.SBLIMIT][2];
		private final byte scalefactor[][] = new byte[Mpg123.SBLIMIT][2];
		/*
		private final void clear() {
			int i = 32;
			do {
				byte[] b = allocation[--i];
				b[0] = 0; b[1] = 0;
				b = scalefactor[i];
				b[0] = 0; b[1] = 0;
			} while( i > 0 );
		}
		*/
	}

	private static boolean gd_are_hip_tables_layer1_initialized = false;

	static final void hip_init_tables_layer1() {
		if( gd_are_hip_tables_layer1_initialized ) {
			return;
		}
		gd_are_hip_tables_layer1_initialized = true;
	}

	private static final boolean I_step_one(final MpStrTag mp, final SideInfoLayerI si) {
		final Frame frame = mp.fr;
		final int jsbound = (frame.mode == Mpg123.MPG_MD_JOINT_STEREO) ? (frame.mode_ext << 2) + 4 : 32;
		boolean illegal_value_detected = false;
		final byte ba15 = 15; /* bit pattern not allowed, looks like sync(?) */
		// memset(si, 0, sizeof(*si));// FIXME why it need?
		// si.clear();// java: already zeroed

		final byte[][] allocation = si.allocation;// java
		final byte[][] scalefactor = si.scalefactor;// java
		if( frame.stereo == 2 ) {
			int i = 0;
			for( ; i < jsbound; i++ ) {
				final byte b0 = mp.get_leq_8_bits( 4 );       /* values 0-15 */
				final byte b1 = mp.get_leq_8_bits( 4 );       /* values 0-15 */
				allocation[i][0] = b0;
				allocation[i][1] = b1;
				if( b0 == ba15 || b1 == ba15 ) {
					illegal_value_detected = true;
				}
			}
			for(; i < Mpg123.SBLIMIT; i++ ) {
				final byte b = mp.get_leq_8_bits( 4 );        /* values 0-15 */
				allocation[i][0] = b;
				allocation[i][1] = b;
				if( b == ba15 ) {
					illegal_value_detected = true;
				}
			}
			i = 0;
			do {
				final byte n0 = allocation[i][0];
				final byte n1 = allocation[i][1];
				final byte b0 = n0 != 0 ? mp.get_leq_8_bits( 6 ) : 0;  /* values 0-63 */
				final byte b1 = n1 != 0 ? mp.get_leq_8_bits( 6 ) : 0;  /* values 0-63 */
				scalefactor[i][0] = b0;
				scalefactor[i][1] = b1;
			} while( ++i < Mpg123.SBLIMIT );
			return illegal_value_detected;
		}// else {
			int i = 0;
			do {
				final byte b0 = mp.get_leq_8_bits( 4 );          /* values 0-15 */
				allocation[i][0] = b0;
				if( b0 == ba15 ) {
					illegal_value_detected = true;
				}
			} while( ++i < Mpg123.SBLIMIT );
			i = 0;
			do {
				final byte n0 = allocation[i][0];
				final byte b0 = n0 != 0 ? mp.get_leq_8_bits( 6 ) : 0;  /* values 0-63 */
				scalefactor[i][0] = b0;
			} while( ++i < Mpg123.SBLIMIT );
		//}
		return illegal_value_detected;
	}

	private static final void I_step_two(final MpStrTag mp, final SideInfoLayerI si, final float fraction[][]/*[2][SBLIMIT]*/) {
		float r0, r1;
		final Frame frame = mp.fr;
		final int ds_limit = frame.down_sample_sblimit;
		final byte[][] allocation = si.allocation;// java
		final byte[][] scalefactor = si.scalefactor;// java
		final float[][] muls = MpStrTag.muls;// java

		if( frame.stereo == 2 ) {
			final float[] fraction0 = fraction[0];// java
			final float[] fraction1 = fraction[1];// java
			final int jsbound = (frame.mode == Mpg123.MPG_MD_JOINT_STEREO) ? (frame.mode_ext << 2) + 4 : 32;
			for( int i = 0; i < jsbound; i++ ) {
				final byte i0 = scalefactor[i][0];
				final byte i1 = scalefactor[i][1];
				final int n0 = (int)allocation[i][0];
				final int n1 = (int)allocation[i][1];

				if( n0 > 0 ) {
					final int n = n0 + 1;
					final char v = mp.get_leq_16_bits( n ); /* 0-65535 */
					r0 = (((-1) << n0) + v + 1) * muls[n][i0];
				} else {
					r0 = 0;
				}
				if( n1 > 0 ) {
					final int n = n1 + 1;
					final char v = mp.get_leq_16_bits( n ); /* 0-65535 */
					r1 = (((-1) << n1) + v + 1) * muls[n][i1];
				} else {
					r1 = 0;
				}
				fraction0[i] = r0;
				fraction1[i] = r1;
			}
			for(int i = jsbound; i < Mpg123.SBLIMIT; i++ ) {
				final byte i0 = scalefactor[i][0];
				final byte i1 = scalefactor[i][1];
				final byte n = allocation[i][0];
				if( n > 0 ) {
					final int n1 = n + 1;
					final char v = mp.get_leq_16_bits( n1 ); /* 0-65535 */
					// unsigned int w = (((-1) << n) + v + 1);// FIXME must be signed int w!
					final float w = (float)(((-1) << n) + v + 1);
					r0 = w * muls[n1][i0];
					r1 = w * muls[n1][i1];
				} else {
					r0 = r1 = 0;
				}
				fraction0[i] = r0;
				fraction1[i] = r1;
			}
			for(int i = ds_limit; i < Mpg123.SBLIMIT; i++ ) {
				fraction0[i] = 0.0f;
				fraction1[i] = 0.0f;
			}
			return;
		}// else {
			final float[] fraction0 = fraction[0];// java
			for(int i = 0; i < Mpg123.SBLIMIT; i++ ) {
				final byte n = allocation[i][0];
				final byte j = scalefactor[i][0];
				if( n > 0 ) {
					final int n1 = n + 1;
					final char v = mp.get_leq_16_bits( n1 );
					r0 = (((-1) << n) + v + 1) * muls[n1][j];
				} else {
					r0 = 0;
				}
				fraction0[i] = r0;
			}
			for(int i = ds_limit; i < Mpg123.SBLIMIT; i++ ) {
				fraction0[i] = 0.0f;
			}
		// }
	}

	@SuppressWarnings("static-method")
	public final int decode_layer1_sideinfo() {
		/* FIXME: extract side information and check values */
		return 0;
	}

	static final int decode_layer1_frame(final MpStrTag mp, final Object pcm_sample, final int[] pcm_point, final Synth synth) {// java: added synth to fix the bug
		final float fraction[][] = new float[2][Mpg123.SBLIMIT]; /* FIXME: change real . double ? */
		final SideInfoLayerI si = new SideInfoLayerI();
		final Frame frame = mp.fr;
		int single = frame.single;

		if( I_step_one( mp, si ) ) {
			//System.err.printf("hip: Aborting layer 1 decode, illegal bit allocation value\n");
			return -1;
		}

		if( frame.stereo == 1 || single == 3 ) {
			single = 0;
		}

		int clip = 0;
		if( single >= 0 ) {
			/* decoding one of possibly two channels */
			int i = 0;
			do {
				I_step_two( mp, si, fraction );
				clip += synth.synth_1to1_mono( mp, fraction[single], 0, pcm_sample, pcm_point );
			} while( ++i < Mpg123.SCALE_BLOCK );
			return clip;
		}// else {
			final int p1[] = new int[1];// TODO java: try to find a beter way
			int i = 0;
			do {
				p1[0] = pcm_point[0];
				I_step_two( mp, si, fraction );
				clip += synth.synth_1to1( mp, fraction[0], 0, 0, pcm_sample, p1 );
				clip += synth.synth_1to1( mp, fraction[1], 0, 1, pcm_sample, pcm_point );
			} while( ++i < Mpg123.SCALE_BLOCK );
		//}

		return clip;
	}
}
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

class III_GrInfo {
	final float xr[] = new float[576];
	final int l3_enc[] = new int[576];
	final int scalefac[] = new int[Encoder.SFBMAX];
	float   xrpow_max;

	int     part2_3_length;
	int     big_values;
	int     count1;
	int     global_gain;
	int     scalefac_compress;
	int     block_type;
	boolean mixed_block_flag;
	final int table_select[] = new int[3];
	final int subblock_gain[] = new int[3 + 1];
	int     region0_count;
	int     region1_count;
	boolean preflag;
	int     scalefac_scale;
	int     count1table_select;

	int     part2_length;
	int     sfb_lmax;
	int     sfb_smin;
	int     psy_lmax;
	int     sfbmax;
	int     psymax;
	int     sfbdivide;
	final int width[] = new int[Encoder.SFBMAX];
	final int window[] = new int[Encoder.SFBMAX];
	int     count1bits;
	/* added for LSF */
	int[]   sfb_partition_table;
	final int slen[] = new int[4];

	int     max_nonzero_coeff;
	final boolean energy_above_cutoff[] = new boolean[Encoder.SFBMAX];
	//
	III_GrInfo() {
	}
	III_GrInfo(final III_GrInfo g) {
		copyFrom( g );
	}
	final void copyFrom(final III_GrInfo g) {
		System.arraycopy( g.xr, 0, this.xr, 0, 576 );
		System.arraycopy( g.l3_enc, 0, this.l3_enc, 0, 576 );
		System.arraycopy( g.scalefac, 0, this.scalefac, 0, Encoder.SFBMAX );
		this.xrpow_max = g.xrpow_max;

		this.part2_3_length = g.part2_3_length;
		this.big_values = g.big_values;
		this.count1 = g.count1;
		this.global_gain = g.global_gain;
		this.scalefac_compress = g.scalefac_compress;
		this.block_type = g.block_type;
		this.mixed_block_flag = g.mixed_block_flag;
		System.arraycopy( g.table_select, 0, this.table_select, 0, 3 );
		System.arraycopy( g.subblock_gain, 0, this.subblock_gain, 0, 3 + 1 );
		this.region0_count = g.region0_count;
		this.region1_count = g.region1_count;
		this.preflag = g.preflag;
		this.scalefac_scale = g.scalefac_scale;
		this.count1table_select = g.count1table_select;

		this.part2_length = g.part2_length;
		this.sfb_lmax = g.sfb_lmax;
		this.sfb_smin = g.sfb_smin;
		this.psy_lmax = g.psy_lmax;
		this.sfbmax = g.sfbmax;
		this.psymax = g.psymax;
		this.sfbdivide = g.sfbdivide;
		System.arraycopy( g.width, 0, this.width, 0, Encoder.SFBMAX );
		System.arraycopy( g.window, 0, this.window, 0, Encoder.SFBMAX );
		this.count1bits = g.count1bits;
		/* added for LSF */
		this.sfb_partition_table = g.sfb_partition_table;
		System.arraycopy( g.slen, 0, this.slen, 0, 4 );

		this.max_nonzero_coeff = g.max_nonzero_coeff;
		System.arraycopy( g.energy_above_cutoff, 0, this.energy_above_cutoff, 0, Encoder.SFBMAX );
	}

	/************************************************************************
	 *
	 *      init_outer_loop()
	 *  mt 6/99
	 *
	 *  initializes cod_info, scalefac and xrpow
	 *
	 *  returns 0 if all energies in xr are zero, else 1
	 *
	 ************************************************************************/
	final float init_xrpow_core_c(final float xrpow[/*576*/], final int upper/*, final float[] sum*/) {
		float sum = 0;
		for( int i = 0; i <= upper; ++i ) {
			float tmp = this.xr[i];
			if( tmp < 0 ) {
				tmp = -tmp;
			}
			sum += tmp;
			xrpow[i] = (float)Math.sqrt( tmp * Math.sqrt( tmp ) );

			if( xrpow[i] > this.xrpow_max ) {
				this.xrpow_max = xrpow[i];
			}
		}
		return sum;
	}

	/*************************************************************************
	 *
	 *      loop_break()
	 *
	 *  author/date??
	 *
	 *  Function: Returns zero if there is a scalefac which has not been
	 *            amplified. Otherwise it returns one.
	 *
	 *************************************************************************/
	final boolean loop_break() {
		for( int sfb = 0, sfb_max = this.sfbmax; sfb < sfb_max; sfb++ ) {
			if( this.scalefac[sfb] + this.subblock_gain[ this.window[sfb] ] == 0 ) {
				return false;
			}
		}

		return true;
	}

	/*************************************************************************
	 *
	 *      inc_scalefac_scale()
	 *
	 *  Takehiro Tominaga 2000-xx-xx
	 *
	 *  turns on scalefac scale and adjusts scalefactors
	 *
	 *************************************************************************/

	final void inc_scalefac_scale(final float xrpow[/*576*/]) {
		final float ifqstep34 = 1.29683955465100964055f;
		for( int j = 0, sfb = 0, max = this.sfbmax; sfb < max; sfb++ ) {
			final int w = this.width[sfb];
			int s = this.scalefac[sfb];
			if( this.preflag ) {
				s += QuantizePVT.pretab[sfb];
			}
			j += w;
			if( (s & 1) != 0 ) {
				s++;
				for( int l = -w; l < 0; l++ ) {
					xrpow[j + l] *= ifqstep34;
					if( xrpow[j + l] > this.xrpow_max ) {
						this.xrpow_max = xrpow[j + l];
					}
				}
			}
			this.scalefac[sfb] = s >> 1;
		}
		this.preflag = false;
		this.scalefac_scale = 1;
	}
}

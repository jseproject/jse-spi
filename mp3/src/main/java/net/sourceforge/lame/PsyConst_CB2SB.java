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

@SuppressWarnings("unused")
class PsyConst_CB2SB {
	final float masking_lower[] = new float[Encoder.CBANDS];
	final float minval[] = new float[Encoder.CBANDS];
	final float rnumlines[] = new float[Encoder.CBANDS];
	final float mld_cb[] = new float[Encoder.CBANDS];

	final float mld[] = new float[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];
	final float bo_weight[] = new float[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)]; /* band weight long scalefactor bands, at transition */
	float attack_threshold; /* short block tuning */
	int     s3ind[][] = new int[Encoder.CBANDS][2];
	int     numlines[] = new int[Encoder.CBANDS];
	int     bm[] = new int[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];// FIXME never uses bm
	int     bo[] = new int[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];
	int     npart;
	int     n_sb; /* SBMAX_l or SBMAX_s */
	float[] s3;
	//
	final void copyFrom(final PsyConst_CB2SB p) {
		System.arraycopy( p.masking_lower, 0, this.masking_lower, 0, Encoder.CBANDS );
		System.arraycopy( p.minval, 0, this.minval, 0, Encoder.CBANDS );
		System.arraycopy( p.rnumlines, 0, this.rnumlines, 0, Encoder.CBANDS );
		System.arraycopy( p.mld_cb, 0, this.mld_cb, 0, Encoder.CBANDS );
		System.arraycopy( p.mld, 0, this.mld, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		System.arraycopy( p.bo_weight, 0, this.bo_weight, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		this.attack_threshold = p.attack_threshold;
		int i = Encoder.CBANDS;
		final int[][] buf = this.s3ind;
		final int[][] ibuf = p.s3ind;
		do {
			final int[] ib = ibuf[--i];
			final int[] b = buf[i];
			b[0] = ib[0];
			b[1] = ib[1];
		} while( i > 0 );
		System.arraycopy( p.numlines, 0, this.numlines, 0, Encoder.CBANDS );
		System.arraycopy( p.bm, 0, this.bm, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		System.arraycopy( p.bo, 0, this.bo, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		this.npart = p.npart;
		this.n_sb = p.n_sb;
		this.s3 = p.s3;
	}
}

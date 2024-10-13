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

class PsyStateVar {
	final float nb_l1[][] = new float[4][Encoder.CBANDS];
	final float nb_l2[][] = new float[4][Encoder.CBANDS];
	final float nb_s1[][] = new float[4][Encoder.CBANDS];
	final float nb_s2[][] = new float[4][Encoder.CBANDS];

	final III_PsyXmin thm[] = new III_PsyXmin[4];
	final III_PsyXmin en[] = new III_PsyXmin[4];

	/* loudness calculation (for adaptive threshold of hearing) */
	final float loudness_sq_save[] = new float[2]; /* account for granule delay of L3psycho_anal */

	final float tot_ener[] = new float[4];

	final float last_en_subshort[][] = new float[4][9];
	final int last_attacks[] = new int[4];

	final int blocktype_old[] = new int[2];
	//
	PsyStateVar() {
		thm[0] = new III_PsyXmin();
		thm[1] = new III_PsyXmin();
		thm[2] = new III_PsyXmin();
		thm[3] = new III_PsyXmin();
		en[0] = new III_PsyXmin();
		en[1] = new III_PsyXmin();
		en[2] = new III_PsyXmin();
		en[3] = new III_PsyXmin();
	}
}

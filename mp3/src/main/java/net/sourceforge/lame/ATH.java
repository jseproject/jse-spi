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

/**
 *  ATH related stuff, if something new ATH related has to be added,
 *  please plugg it here into the ATH_t struct
 */
class ATH {
	/** method for the auto adjustment  */
	int   use_adjust;
	/** factor for tuning the (sample power)
    point below which adaptive threshold
    of hearing adjustment occurs */
	float aa_sensitivity_p;
	/** lowering based on peak volume, 1 = no lowering */
	float adjust_factor;
	/** limit for dynamic ATH adjust */
	float adjust_limit;
	/** determined to lower x dB each second */
	float decay;
	/** lowest ATH value */
	float floor;
	/** ATH for sfbs in long blocks */
	final float l[] = new float[Encoder.SBMAX_l];
	/** ATH for sfbs in short blocks */
	final float s[] = new float[Encoder.SBMAX_s];
	/** ATH for partitionned sfb21 in long blocks */
	final float psfb21[] = new float[Encoder.PSFB21];
	/** ATH for partitionned sfb12 in short blocks */
	final float psfb12[] = new float[Encoder.PSFB12];
	/** ATH for long block convolution bands */
	final float cb_l[] = new float[Encoder.CBANDS];
	/** ATH for short block convolution bands */
	final float cb_s[] = new float[Encoder.CBANDS];
	/** equal loudness weights (based on ATH) */
	final float eql_w[] = new float[Encoder.BLKSIZE / 2];
}

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

/** variables used by quantize.c */
class QntStateVar {
	/* variables for nspsytune */
	final float longfact[] = new float[ Encoder.SBMAX_l ];
	final float shortfact[] = new float[ Encoder.SBMAX_s ];
	float masking_lower;
	float mask_adjust; /* the dbQ stuff */
	float mask_adjust_short; /* the dbQ stuff */
	final int OldValue[] = new int[2];
	final int CurrentStep[] = new int[2];
	final boolean pseudohalf[] = new boolean[ Encoder.SFBMAX ];
	boolean sfb21_extra; /* will be set in lame_init_params */
	/** 0 = no substep
	 * 1 = use substep shaping at last step(VBR only) (not implemented yet)
	 * 2 = use substep inside loop
	 * 3 = use substep inside loop and last step
	 */
	int substep_shaping;

	final byte bv_scf[] = new byte[576];
}

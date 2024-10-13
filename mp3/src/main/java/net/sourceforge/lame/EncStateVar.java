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

/** variables used by encoder.c */
class EncStateVar {
	/* variables for newmdct.c */
	final float   sb_sample[][][][] = new float[2][2][18][Encoder.SBLIMIT];
	final float   amp_filter[] = new float[32];

	/* variables used by util.c */
	/** BPC = maximum number of filter convolution windows to precompute */
	static final int BPC = 320;
	final double  itime[] = new double[2]; /* float precision seems to be not enough */
	final float inbuf_old[][] = new float[2][];
	final float blackfilt[][] = new float[2 * BPC + 1][];

	final float pefirbuf[] = new float[19];

	/* used for padding */
	int     frac_SpF;
	int     slot_lag;

	/* variables for bitstream.c */
	/* mpeg1: buffer=511 bytes  smallest frame: 96-38(sideinfo)=58
	 * max number of frames in reservoir:  8
	 * mpeg2: buffer=255 bytes.  smallest frame: 24-23bytes=1
	 * with VBR, if you are encoding all silence, it is possible to
	 * have 8kbs/24khz frames with 1byte of data each, which means we need
	 * to buffer up to 255 headers! */
	/* also, max_header_buf has to be a power of two */
	static final int MAX_HEADER_BUF = 256;
	private static final int MAX_HEADER_LEN = 40;    /* max size of header is 38 */
	//
	static final class Header {
		int  write_timing;
		int  ptr;
		final byte buf[] = new byte[MAX_HEADER_LEN];
	};
	final Header header[] = new Header[MAX_HEADER_BUF];

	int     h_ptr;
	int     w_ptr;
	boolean ancillary_flag;

	/* variables for reservoir.c */
	int     ResvSize;    /* in bits */
	int     ResvMax;     /* in bits */

	int     in_buffer_nsamples;
	float[] in_buffer_0;
	float[] in_buffer_1;

	private static final int MFSIZE = (3 * 1152 + Encoder.ENCDELAY - Encoder.MDCTDELAY);

	final float mfbuf[][] = new float[2][MFSIZE];

	int     mf_samples_to_encode;
	int     mf_size;
	//
	EncStateVar() {
		int i = MAX_HEADER_BUF;
		do {
			this.header[--i] = new Header();
		} while( i > 0 );
	}
}

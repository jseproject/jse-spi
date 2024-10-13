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

// util.h

/********************************************************************
 * internal variables NOT set by calling program, and should not be *
 * modified by the calling program                                  *
 ********************************************************************/
class LAME_InternalFlags {
	/*
	 * Some remarks to the Class_ID field:
	 * The Class ID is an Identifier for a pointer to this struct.
	 * It is very unlikely that a pointer to lame_global_flags has the same 32 bits
	 * in it's structure (large and other special properties, for instance prime).
	 *
	 * To test that the structure is right and initialized, use:
	 *     if ( gfc -> Class_ID == LAME_ID ) ...
	 * Other remark:
	 *     If you set a flag to 0 for uninit data and 1 for init data, the right test
	 *     should be "if (flag == 1)" and NOT "if (flag)". Unintended modification
	 *     of this element will be otherwise misinterpreted as an init.
	 */
	static final int LAME_ID = 0xFFF88E3B;
	int class_id;

	boolean lame_init_params_successful;
	boolean lame_encode_frame_init;
	boolean iteration_init_init;
	boolean fill_buffer_resample_init;

	final SessionConfig cfg = new SessionConfig();

	/* variables used by lame.c */
	final Bitstream bs = new Bitstream();
	final III_SideInfo l3_side = new III_SideInfo();

	final ScaleFacStruct scalefac_band = new ScaleFacStruct();

	final PsyStateVar sv_psy = new PsyStateVar(); /* DATA FROM PSYMODEL.C */
	final PsyResult ov_psy = new PsyResult();
	final EncStateVar sv_enc = new EncStateVar(); /* DATA FROM ENCODER.C */
	final EncResult ov_enc = new EncResult();
	final QntStateVar sv_qnt = new QntStateVar(); /* DATA FROM QUANTIZE.C */

	/* optional ID3 tags, used in id3tag.c  */
	final ID3TagSpec tag_spec = new ID3TagSpec();
	char nMusicCRC;

	//char _unused;

	final VBRSeekInfo VBR_seek_table = new VBRSeekInfo(); /* used for Xing VBR header */

	net.sourceforge.lame.ATH ATH;         /* all ATH related stuff */

	PsyConst cd_psy;

	boolean is_lame_internal_flags_valid() {
		/* if( gfc == null ) {
			return false;
		} */
		return this.class_id == LAME_InternalFlags.LAME_ID && this.lame_init_params_successful;
	}

	/*empty and close mallocs in gfc */

	final void free_id3tag() {
		this.tag_spec.language = "";
		this.tag_spec.title = null;
		this.tag_spec.artist = null;
		this.tag_spec.album = null;
		this.tag_spec.comment = null;

		this.tag_spec.albumart = null;
		// this.tag_spec.albumart_size = 0;
		this.tag_spec.albumart_mimetype = ID3Tag.MIMETYPE_NONE;

		if( this.tag_spec.v2_head != null ) {
			FrameDataNode node = this.tag_spec.v2_head;
			do {
				node.dsc = null;
				node.txt = null;
				node = node.nxt;
			} while( node != null );
			this.tag_spec.v2_head = null;
			this.tag_spec.v2_tail = null;
		}
	}

	private final void free_global_data() {
		if( /*gfc != null &&*/ this.cd_psy != null ) {
			this.cd_psy.l.s3 = null;
			this.cd_psy.s.s3 = null;
			this.cd_psy = null;
			this.cd_psy = null;
		}
	}

	/* bit stream structure */
	final void freegfc() {
		for(int i = 0; i <= 2 * EncStateVar.BPC; i++ ) {
			this.sv_enc.blackfilt[i] = null;
		}
		this.sv_enc.inbuf_old[0] = null;
		this.sv_enc.inbuf_old[1] = null;

		this.bs.buf = null;

		this.VBR_seek_table.bag = null;
		// this.VBR_seek_table.size = 0;

		this.ATH = null;

		this.sv_enc.in_buffer_0 = null;
		this.sv_enc.in_buffer_1 = null;

		free_id3tag();

		free_global_data();
	}
}

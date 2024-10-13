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

class ID3TagSpec {
	/* private data members */
	int flags;
	int year;
	byte[] title;
	byte[] artist;
	byte[] album;
	byte[] comment;
	int track_id3v1;
	int genre_id3v1;
	byte[] albumart;
	//int albumart_size;// albumart.length
	int padding_size;
	int albumart_mimetype;
	/** the language of the frame's content, according to ISO-639-2 */
	String language = "";// char[4];
	FrameDataNode v2_head, v2_tail;
	//
	final void clear() {
		flags = 0;
		year = 0;
		title = null;
		artist = null;
		album = null;
		comment = null;
		track_id3v1 = 0;
		genre_id3v1 = 0;
		albumart = null;
		// albumart_size = 0;
		padding_size = 0;
		albumart_mimetype = 0;
		v2_head = null;
		v2_tail = null;
	}
}

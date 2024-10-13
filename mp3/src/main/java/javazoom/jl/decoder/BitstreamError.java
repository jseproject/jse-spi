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

package javazoom.jl.decoder;

/**
 * This interface describes all error codes that can be thrown 
 * in <code>BistreamException</code>s.
 * 
 * @see BitstreamException
 * 
 * @author	MDM		12/12/99
 * @author  Naoko Mitsurugi   10/12/24
 * @since	0.0.6
 */
// Naoko: changed to class from interface
public class BitstreamError {

	/**
	 * The first bitstream error code.
	 */
	static public final int BITSTREAM_ERROR = 0x100;
	
	/**
	 * An undeterminable error occurred. 
	 */
	static public final int UNKNOWN_ERROR = BITSTREAM_ERROR + 0;
	
	/**
	 * The header describes an unknown sample rate.
	 */
	static public final int UNKNOWN_SAMPLE_RATE = BITSTREAM_ERROR + 1;

	/**
	 * A problem occurred reading from the stream.
	 */
	static public final int STREAM_ERROR = BITSTREAM_ERROR + 2;
	
	/**
	 * The end of the stream was reached prematurely. 
	 */
	static public final int UNEXPECTED_EOF = BITSTREAM_ERROR + 3;
	
	/**
	 * The end of the stream was reached. 
	 */
	static public final int STREAM_EOF = BITSTREAM_ERROR + 4;
	
	/**
	 * Frame data are missing. 
	 */
	static public final int INVALIDFRAME = BITSTREAM_ERROR + 5;

	/**
	 * 
	 */
	static public final int BITSTREAM_LAST = 0x1ff;
	
}

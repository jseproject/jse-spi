/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2008 Christopher G. Jennings
 * Copyright (c) 1999-2010 JavaZOOM
 * Copyright (c) 1999 Mat McGowan
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1993-1994 Tobias Bading
 * Copyright (c) 1991 MPEG Software Simulation Group
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * - You should have received a copy of the GNU Library General Public
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
 * @author  Naoko Mitsurugi   10/08/24
 * @since	0.0.6
 */
// Naoko: changed to class from interface
public class BitstreamError extends JavaLayerError {
	
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

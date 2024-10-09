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
 * This interface provides constants describing the error
 * codes used by the Decoder to indicate errors. 
 * 
 * @author	MDM
 * @author Naoko Mitsurugi
 */
// Naoko: changed to class from interface
public class DecoderError extends JavaLayerError {

	public static final int UNKNOWN_ERROR = DECODER_ERROR + 0;
	
	/**
	 * Layer not supported by the decoder. 
	 */
	public static final int UNSUPPORTED_LAYER = DECODER_ERROR + 1;

    /**
	 * Illegal allocation in subband layer. Indicates a corrupt stream.
	 */
	public static final int ILLEGAL_SUBBAND_ALLOCATION = DECODER_ERROR + 2;

}

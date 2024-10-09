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

import static javazoom.jl.decoder.DecoderError.*;

/**
 * The <code>DecoderException</code> represents the class of
 * errors that can occur when decoding MPEG audio. 
 * 
 * @author MDM
 * @author Naoko Mitsurugi
 */
public class DecoderException extends JavaLayerException {

	private static final long serialVersionUID = 5406764631157694872L;

	private int		errorcode = UNKNOWN_ERROR;
	
	public DecoderException(String msg, Throwable t)
	{
		super(msg, t);	
	}
	
	public DecoderException(int errorcode, Throwable t)
	{
		this(getErrorString(errorcode), t);
		this.errorcode = errorcode;
	}
	
	public int getErrorCode()
	{
		return errorcode;	
	}
	
	
	static public String getErrorString(int errorcode)
	{
		// REVIEW: use resource file to map error codes
		// to locale-sensitive strings. 
		
		return "Decoder error code 0x" + Integer.toHexString(errorcode);
	}
	
	
}


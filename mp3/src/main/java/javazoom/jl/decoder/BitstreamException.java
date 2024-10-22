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

import java.io.PrintStream;

import static javazoom.jl.decoder.BitstreamError.*;

/**
 * Instances of <code>BitstreamException</code> are thrown 
 * when operations on a <code>Bitstream</code> fail. 
 * <p>
 * The exception provides details of the exception condition 
 * in two ways:
 * <ol><li>
 *		as an error-code describing the nature of the error
 * </li><br></br><li>
 *		as the <code>Throwable</code> instance, if any, that was thrown
 *		indicating that an exceptional condition has occurred. 
 * </li></ol></p>
 * 
 * @since 0.0.6
 * @author MDM	12/12/99
 * @author Naoko Mitsurugi 10/12/24
 */

public class BitstreamException extends Exception {

	private static final long serialVersionUID = -2409061956095472925L;

	private Throwable		exception;

	private int errorcode = UNKNOWN_ERROR;
	
	public BitstreamException(String msg, Throwable t)
	{
		super(msg);
		exception = t;
	}
	
	public BitstreamException(int errorcode, Throwable t)
	{
		this(getErrorString(errorcode), t);
		this.errorcode = errorcode;
	}

	public Throwable getException()
	{
		return exception;
	}

	@Override
	public void printStackTrace()
	{
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream ps)
	{
		if (this.exception==null)
		{
			super.printStackTrace(ps);
		}
		else
		{
			exception.printStackTrace();
		}
	}
	
	public int getErrorCode()
	{
		return errorcode;	
	}
	
	static public String getErrorString(int errorcode)
	{
		// REVIEW: use resource bundle to map error codes
		// to locale-sensitive strings.
		
		return "Bitstream error code " + Integer.toHexString(errorcode);
	}
	
}

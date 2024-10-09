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

import java.io.PrintStream;


/**
 * The JavaLayerException is the base class for all API-level
 * exceptions thrown by JavaLayer. To facilitate conversion and 
 * common handling of exceptions from other domains, the class 
 * can delegate some functionality to a contained Throwable instance. 
 * <p> 
 * 
 * @author MDM
 * @author Naoko Mitsurugi
 */
public class JavaLayerException extends Exception
{

	private static final long serialVersionUID = 7212135786131434159L;

	private Throwable		exception;
	
	
	public JavaLayerException()
	{
	}
	
	public JavaLayerException(String msg)
	{
		super(msg);
	}
	
	public JavaLayerException(String msg, Throwable t)
	{
		super(msg);
		exception = t;
	}
	
	public Throwable getException()
	{
		return exception;	
	}
	
	
	public void printStackTrace()
	{
		printStackTrace(System.err);	
	}
	
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
	
	
}

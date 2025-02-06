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

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

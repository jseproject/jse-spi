package org.concentus;

/// <summary>
/// Note that since most API-level errors are detected and thrown as
/// OpusExceptions, direct use of this class is not usually needed
/// </summary>
public class OpusError {

    /**
     * No error
     */
    public static int OPUS_OK = 0;

    /**
     * One or more invalid/out of range arguments
     */
    public static int OPUS_BAD_ARG = -1;

    /**
     * Not enough bytes allocated in the buffer
     */
    public static int OPUS_BUFFER_TOO_SMALL = -2;

    /**
     * An public error was detected
     */
    public static int OPUS_INTERNAL_ERROR = -3;

    /**
     * The compressed data passed is corrupted
     */
    public static int OPUS_INVALID_PACKET = -4;

    /**
     * Invalid/unsupported request number
     */
    public static int OPUS_UNIMPLEMENTED = -5;

    /**
     * An encoder or decoder structure is invalid or already freed
     */
    public static int OPUS_INVALID_STATE = -6;

    /**
     * Memory allocation has failed
     */
    public static int OPUS_ALLOC_FAIL = -7;
}

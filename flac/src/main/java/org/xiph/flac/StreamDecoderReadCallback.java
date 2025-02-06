package org.xiph.flac;

import java.io.IOException;

//typedef FLAC__StreamDecoderReadStatus (*FLAC__StreamDecoderReadCallback)(const FLAC__StreamDecoder *decoder, FLAC__byte buffer[], size_t *bytes, void *client_data);
public interface StreamDecoderReadCallback {
	// java: changed. return read bytes instead FLAC__StreamDecoderReadStatus. uses IOException.
	/**
     * Reads up to <code>bytes</code> bytes of data from this file into an
     * array of bytes. This method blocks until at least one byte of input
     * is available.
     * <p>
     *
     * @param      buffer the buffer into which the data is read.
     * @param      offset the start offset in array <code>buffer</code>
     *                   at which the data is written.
     * @param      bytes  the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>buffer</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>offset</code> is negative,
     * <code>bytes</code> is negative, or <code>bytes</code> is greater than
     * <code>buffer.length - offset</code>
     */
	public int dec_read_callback(final StreamDecoder decoder, byte buffer[], int offset, int bytes/*, Object client_data*/) throws IOException;
}

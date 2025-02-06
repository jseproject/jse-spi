package org.xiph.flac;

import java.io.IOException;

public interface BitReaderReadCallback {
	//FLAC__bool (*FLAC__BitReaderReadCallback)(FLAC__byte buffer[], size_t *bytes, void *client_data);
	/**
	 *
	 * @param      buffer     the buffer into which the data is read.
	 * @param      bytes the maximum number of bytes read.
	 * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
	 * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
	 */
	int bit_read_callback(byte buffer[], int bytes/*, Object client_data*/) throws IOException;// java: changed
}

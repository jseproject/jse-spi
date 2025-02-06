package org.xiph.flac;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessInputStream extends InputStream {
	private RandomAccessFile f;

	public RandomAccessInputStream(String path) throws FileNotFoundException {
		f = new RandomAccessFile( path, "r" );
	}

	/**
     * Closes this random access file stream and releases any system 
     * resources associated with the stream. A closed random access 
     * file cannot perform input or output operations and cannot be 
     * reopened.
     *
     * <p> If this file has an associated channel then the channel is closed
     * as well.
     *
     * @exception  IOException  if an I/O error occurs.
     */
	@Override
	public final void close() throws IOException {
		f.close();
		f = null;
	}

	/*
	@Override
	protected final void finalize() throws Throwable {
		if( f != null ) close();
	}
	 */

	/**
     * Returns the length of this file.
     *
     * @return     the length of this file, measured in bytes.
     * @exception  IOException  if an I/O error occurs.
     */
	public final long length() throws IOException {
		return f.length();
	}

	/**
     * Returns the current offset in this file. 
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     * @exception  IOException  if an I/O error occurs.
     */
	public final long getFilePointer() throws IOException {
		return f.getFilePointer();
	}

	/**
     * Reads a byte of data from this file. The byte is returned as an 
     * integer in the range 0 to 255 (<code>0x00-0x0ff</code>). This 
     * method blocks if no input is yet available. 
     * <p>
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             file has been reached.
     * @exception  IOException  if an I/O error occurs. Not thrown if  
     *                          end-of-file has been reached.
     */
	@Override
	public final int read() throws IOException {
		return f.read();
	}

	/**
     * Reads up to <code>len</code> bytes of data from this file into an 
     * array of bytes. This method blocks until at least one byte of input 
     * is available. 
     * <p>
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes read.
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
	@Override
	public final int read(byte[] b, int off, int len ) throws IOException {
		return f.read( b, off, len );
	}

	/**
     * Reads up to <code>b.length</code> bytes of data from this file 
     * into an array of bytes. This method blocks until at least one byte 
     * of input is available. 
     * <p>
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             this file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     */
	@Override
	public final int read(byte[] b) throws IOException {
		return f.read( b );
	}

	/**
     * Sets the file-pointer offset, measured from the beginning of this 
     * file, at which the next read or write occurs.  The offset may be 
     * set beyond the end of the file. Setting the offset beyond the end 
     * of the file does not change the file length.  The file length will 
     * change only by writing after the offset has been set beyond the end 
     * of the file. 
     *
     * @param      pos   the offset position, measured in bytes from the 
     *                   beginning of the file, at which to set the file 
     *                   pointer.
     * @exception  IOException  if <code>pos</code> is less than 
     *                          <code>0</code> or if an I/O error occurs.
     */
	public final void seek(long pos) throws IOException {
		f.seek( pos );
	}

	// only for compatibility with InputStream
	@Override
	public final int available() throws IOException {
		return (int)(f.length() - f.getFilePointer());
	}

	@Override
	public final synchronized void mark(int readlimit) {
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void reset() throws IOException {
	}

	@Override
	public long skip(long n) throws IOException {
		return (long) f.skipBytes( (int)n );
	}
}

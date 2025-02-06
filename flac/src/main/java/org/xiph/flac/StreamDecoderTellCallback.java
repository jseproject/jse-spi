package org.xiph.flac;

import java.io.IOException;

//typedef FLAC__StreamDecoderTellStatus (*FLAC__StreamDecoderTellCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *absolute_byte_offset, void *client_data);
public interface StreamDecoderTellCallback {
	/** Signature for the tell callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_decoder_init*_stream().  The supplied function will be
	 *  called when the decoder wants to know the current position of the
	 *  stream.  The callback should return the byte offset from the
	 *  beginning of the stream.
	 *
	 * Here is an example of a tell callback for stdio streams:
	 * <pre><code>
	 * FLAC__StreamDecoderTellStatus tell_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 *absolute_byte_offset, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   off_t pos;
	 *   if(file == stdin)
	 *     return FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED;
	 *   else if((pos = ftello(file)) < 0)
	 *     return FLAC__STREAM_DECODER_TELL_STATUS_ERROR;
	 *   else {
	 *     *absolute_byte_offset = (FLAC__uint64)pos;
	 *     return FLAC__STREAM_DECODER_TELL_STATUS_OK;
	 *   }
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 * @implNote java: changed: Returns the current offset in this file.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @return     the offset from the beginning of the file, in bytes,
	 *             at which the next read or write occurs.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public long dec_tell_callback(final StreamDecoder decoder/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}

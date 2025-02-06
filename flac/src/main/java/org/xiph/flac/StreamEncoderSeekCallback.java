package org.xiph.flac;

//typedef FLAC__StreamEncoderSeekStatus (*FLAC__StreamEncoderSeekCallback)(const FLAC__StreamEncoder *encoder, FLAC__uint64 absolute_byte_offset, void *client_data);
public interface StreamEncoderSeekCallback {
	/** Signature for the seek callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_encoder_init*_stream().  The supplied function will be called
	 *  when the encoder needs to seek the output stream.  The encoder will pass
	 *  the absolute byte offset to seek to, 0 meaning the beginning of the stream.
	 *
	 * Here is an example of a seek callback for stdio streams:
	 * <pre><code>
	 * FLAC__StreamEncoderSeekStatus seek_cb(const FLAC__StreamEncoder *encoder, FLAC__uint64 absolute_byte_offset, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   if(file == stdin)
	 *     return FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED;
	 *   else if(fseeko(file, (off_t)absolute_byte_offset, SEEK_SET) < 0)
	 *     return FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
	 *   else
	 *     return FLAC__STREAM_ENCODER_SEEK_STATUS_OK;
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamEncoder functions which change the
	 * state should not be called on the {@code encoder} while in the callback.
	 *
	 * @param  encoder  The encoder instance calling the callback.
	 * @param  absolute_byte_offset  The offset from the beginning of the stream
	 *                               to seek to.
	 * @return FLAC__StreamEncoderSeekStatus
	 *    The callee's return status.
	 */
	public int enc_seek_callback(final StreamEncoder encoder, long absolute_byte_offset/*, Object client_data*/);
}

package org.xiph.flac;

//typedef FLAC__StreamEncoderWriteStatus (*FLAC__StreamEncoderWriteCallback)(const FLAC__StreamEncoder *encoder, const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data);
public interface StreamEncoderWriteCallback {
	/** Signature for the write callback.
	 *
	 *  A function pointer matching this signature must be passed to
	 *  FLAC__stream_encoder_init*_stream().  The supplied function will be called
	 *  by the encoder anytime there is raw encoded data ready to write.  It may
	 *  include metadata mixed with encoded audio frames and the data is not
	 *  guaranteed to be aligned on frame or metadata block boundaries.
	 *
	 *  The only duty of the callback is to write out the {@code bytes} worth of data
	 *  in {@code} buffer to the current position in the output stream.  The arguments
	 *  {@code samples} and {@code current_frame are purely informational.  If {@code samples}
	 *  is greater than {@code 0}, then {@code current_frame} will hold the current frame
	 *  number that is being written; otherwise it indicates that the write
	 *  callback is being called to write metadata.
	 *
	 * @implNote 
	 * Unlike when writing to native FLAC, when writing to Ogg FLAC the
	 * write callback will be called twice when writing each audio
	 * frame; once for the page header, and once for the page body.
	 * When writing the page header, the {@code samples} argument to the
	 * write callback will be {@code 0}.
	 *
	 * @apiNote In general, FLAC__StreamEncoder functions which change the
	 * state should not be called on the {@code encoder} while in the callback.
	 *
	 * @param  encoder  The encoder instance calling the callback.
	 * @param  buffer   An array of encoded data of length {@code bytes}.
	 * @param  bytes    The byte length of {@code buffer}.
	 * @param  samples  The number of samples encoded by {@code buffer}.
	 *                  {@code 0} has a special meaning; see above.
	 * @param  current_frame  The number of the current frame being encoded.
	 * @return FLAC__StreamEncoderWriteStatus
	 *    The callee's return status.
	 */
	public int enc_write_callback(final StreamEncoder encoder, final byte buffer[], int offset, int bytes, int samples, int current_frame/*, Object client_data*/);
}

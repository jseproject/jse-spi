package org.xiph.flac;

//typedef FLAC__StreamDecoderWriteStatus (*FLAC__StreamDecoderWriteCallback)(const FLAC__StreamDecoder *decoder, const FLAC__Frame *frame, const FLAC__int32 * const buffer[], void *client_data);
public interface StreamDecoderWriteCallback {
	/** Signature for the write callback.
	 *
	 *  A function pointer matching this signature must be passed to one of
	 *  the FLAC__stream_decoder_init_*() functions.
	 *  The supplied function will be called when the decoder has decoded a
	 *  single audio frame.  The decoder will pass the frame metadata as well
	 *  as an array of pointers (one for each channel) pointing to the
	 *  decoded audio.
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @param  frame    The description of the decoded frame.  See
	 *                  FLAC__Frame.
	 * @param  buffer   An array of pointers to decoded channels of data.
	 *                  Each pointer will point to an array of signed
	 *                  samples of length {@code frame->header.blocksize}.
	 *                  Channels will be ordered according to the FLAC
	 *                  specification; see the documentation for the
	 *                  <A HREF="../format.html#frame_header">frame header</A>.
	 * @return FLAC__StreamDecoderWriteStatus
	 *    The callee's return status.
	 */
	public int dec_write_callback(final StreamDecoder decoder, final Frame frame, final int buffer[][], int offset/*, Object client_data*/);
}

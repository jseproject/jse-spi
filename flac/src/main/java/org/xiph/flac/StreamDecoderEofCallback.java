package org.xiph.flac;

//typedef FLAC__bool (*FLAC__StreamDecoderEofCallback)(const FLAC__StreamDecoder *decoder, void *client_data);
public interface StreamDecoderEofCallback {
	/** Signature for the EOF callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_decoder_init*_stream().  The supplied function will be
	 *  called when the decoder needs to know if the end of the stream has
	 *  been reached.
	 *
	 * Here is an example of a EOF callback for stdio streams:
	 * <pre><code>
	 * FLAC__bool eof_cb(const FLAC__StreamDecoder *decoder, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   return feof(file)? true : false;
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @return FLAC__bool
	 *    {@code true} if the currently at the end of the stream, else {@code false}.
	 */
	public boolean dec_eof_callback(final StreamDecoder decoder/*, Object client_data*/);
}

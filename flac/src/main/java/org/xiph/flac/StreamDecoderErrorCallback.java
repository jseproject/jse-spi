package org.xiph.flac;

import java.io.IOException;


//typedef void (*FLAC__StreamDecoderErrorCallback)(const FLAC__StreamDecoder *decoder, FLAC__StreamDecoderErrorStatus status, void *client_data);
public interface StreamDecoderErrorCallback {
	// XXX java: added throws IOException to abort decoder if it need
	/** Signature for the error callback.
	 *
	 *  A function pointer matching this signature must be passed to one of
	 *  the FLAC__stream_decoder_init_*() functions.
	 *  The supplied function will be called whenever an error occurs during
	 *  decoding.
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @param  status   The error encountered by the decoder.
	 */
	public void dec_error_callback(final StreamDecoder decoder, int /*StreamDecoderErrorStatus*/ status/*, Object client_data*/) throws IOException;
}

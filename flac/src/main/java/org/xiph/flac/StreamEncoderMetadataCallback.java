package org.xiph.flac;

//typedef void (*FLAC__StreamEncoderMetadataCallback)(const FLAC__StreamEncoder *encoder, const FLAC__StreamMetadata *metadata, void *client_data);
public interface StreamEncoderMetadataCallback {
	/** Signature for the metadata callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_encoder_init*_stream().  The supplied function will be called
	 *  once at the end of encoding with the populated STREAMINFO structure.  This
	 *  is so the client can seek back to the beginning of the file and write the
	 *  STREAMINFO block with the correct statistics after encoding (like
	 *  minimum/maximum frame size and total samples).
	 *
	 * @apiNote In general, FLAC__StreamEncoder functions which change the
	 * state should not be called on the {@code encoder} while in the callback.
	 *
	 * @param  encoder      The encoder instance calling the callback.
	 * @param  metadata     The final populated STREAMINFO block.
	 */
	public void enc_metadata_callback(final StreamEncoder encoder, final StreamMetadata metadata/*, Object client_data*/);
}

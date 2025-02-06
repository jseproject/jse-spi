package org.xiph.flac;

/** typedef FLAC__StreamEncoderWriteStatus (*FLAC__OggEncoderAspectWriteCallbackProxy)(const void *encoder, const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data); */
public interface OggEncoderAspectWriteCallbackProxy {
	int ogg_write_callback(final StreamEncoder encoder, final byte buffer[], int offset, int bytes, int samples, int current_frame/*, Object client_data*/);
}

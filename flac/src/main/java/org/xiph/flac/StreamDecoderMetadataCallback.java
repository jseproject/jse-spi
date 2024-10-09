/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.flac;

import java.io.IOException;

//typedef void (*FLAC__StreamDecoderMetadataCallback)(const FLAC__StreamDecoder *decoder, const FLAC__StreamMetadata *metadata, void *client_data);
public interface StreamDecoderMetadataCallback {
	/** Signature for the metadata callback.
	 *
	 *  A function pointer matching this signature must be passed to one of
	 *  the FLAC__stream_decoder_init_*() functions.
	 *  The supplied function will be called when the decoder has decoded a
	 *  metadata block.  In a valid FLAC file there will always be one
	 *  {@code STREAMINFO} block, followed by zero or more other metadata blocks.
	 *  These will be supplied by the decoder in the same order as they
	 *  appear in the stream and always before the first audio frame (i.e.
	 *  write callback).  The metadata block that is passed in must not be
	 *  modified, and it doesn't live beyond the callback, so you should make
	 *  a copy of it with FLAC__metadata_object_clone() if you will need it
	 *  elsewhere.  Since metadata blocks can potentially be large, by
	 *  default the decoder only calls the metadata callback for the
	 *  {@code STREAMINFO} block; you can instruct the decoder to pass or filter
	 *  other blocks with FLAC__stream_decoder_set_metadata_*() calls.
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @param  metadata The decoded metadata block.
	 */
	public void dec_metadata_callback(final StreamDecoder decoder, final StreamMetadata metadata/*, Object client_data*/) throws IOException;
}

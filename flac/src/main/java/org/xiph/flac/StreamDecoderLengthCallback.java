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


//typedef FLAC__StreamDecoderLengthStatus (*FLAC__StreamDecoderLengthCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *stream_length, void *client_data);
public interface StreamDecoderLengthCallback {
	//java: changed. stream_length is returned. uses IOException
	/** Signature for the length callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_decoder_init*_stream().  The supplied function will be
	 *  called when the decoder wants to know the total length of the stream
	 *  in bytes.
	 *
	 * Here is an example of a length callback for stdio streams:
	 * <pre><code>
	 * FLAC__StreamDecoderLengthStatus length_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 *stream_length, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   struct stat filestats;
	 *
	 *   if(file == stdin)
	 *     return FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED;
	 *   else if(fstat(fileno(file), &filestats) != 0)
	 *     return FLAC__STREAM_DECODER_LENGTH_STATUS_ERROR;
	 *   else {
	 *     *stream_length = (FLAC__uint64)filestats.st_size;
	 *     return FLAC__STREAM_DECODER_LENGTH_STATUS_OK;
	 *   }
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the {@code decoder} while in the callback.
	 * @implNote java: changed. Returns the length of this file.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 *
	 * @return the length of this file, measured in bytes.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public long dec_length_callback(final StreamDecoder decoder/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}

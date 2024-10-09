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

//typedef FLAC__StreamEncoderTellStatus (*FLAC__StreamEncoderTellCallback)(const FLAC__StreamEncoder *encoder, FLAC__uint64 *absolute_byte_offset, void *client_data);
public interface StreamEncoderTellCallback {
	// java: changed: absolute_byte_offset is returned. uses IOException
	/** Signature for the tell callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_encoder_init*_stream().  The supplied function will be called
	 *  when the encoder needs to know the current position of the output stream.
	 *
	 * Here is an example of a tell callback for stdio streams:
	 * <pre><code>
	 * FLAC__StreamEncoderTellStatus tell_cb(const FLAC__StreamEncoder *encoder, FLAC__uint64 *absolute_byte_offset, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   off_t pos;
	 *   if(file == stdin)
	 *     return FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED;
	 *   else if((pos = ftello(file)) < 0)
	 *     return FLAC__STREAM_ENCODER_TELL_STATUS_ERROR;
	 *   else {
	 *     *absolute_byte_offset = (FLAC__uint64)pos;
	 *     return FLAC__STREAM_ENCODER_TELL_STATUS_OK;
	 *   }
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamEncoder functions which change the
	 * state should not be called on the \a encoder while in the callback.
	 *
	 * @implNote java: changed: Returns the current offset in this file.
	 * @implSpec The callback must return the true current byte offset of the output to
	 * which the encoder is writing.  If you are buffering the output, make
	 * sure and take this into account.  If you are writing directly to a
	 * FILE* from your write callback, ftell() is sufficient.  If you are
	 * writing directly to a file descriptor from your write callback, you
	 * can use lseek(fd, SEEK_CUR, 0).  The encoder may later seek back to
	 * these points to rewrite metadata after encoding.
	 *
	 * @param  encoder  The encoder instance calling the callback.
	 * @return the offset from the beginning of the file, in bytes,
	 *             at which the next read or write occurs.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public long enc_tell_callback(final StreamEncoder encoder/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}

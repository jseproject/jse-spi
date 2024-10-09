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

//typedef FLAC__StreamDecoderSeekStatus (*FLAC__StreamDecoderSeekCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 absolute_byte_offset, void *client_data);
public interface StreamDecoderSeekCallback {
	/** Signature for the seek callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_decoder_init*_stream().  The supplied function will be
	 *  called when the decoder needs to seek the input stream.  The decoder
	 *  will pass the absolute byte offset to seek to, 0 meaning the
	 *  beginning of the stream.
	 *
	 * Here is an example of a seek callback for stdio streams:
	 * <pre><code>
	 * FLAC__StreamDecoderSeekStatus seek_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 absolute_byte_offset, void *client_data)
	 * {
	 *   FILE *file = ((MyClientData*)client_data)->file;
	 *   if(file == stdin)
	 *     return FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED;
	 *   else if(fseeko(file, (off_t)absolute_byte_offset, SEEK_SET) < 0)
	 *     return FLAC__STREAM_DECODER_SEEK_STATUS_ERROR;
	 *   else
	 *     return FLAC__STREAM_DECODER_SEEK_STATUS_OK;
	 * }
	 * </code></pre>
	 *
	 * @apiNote In general, FLAC__StreamDecoder functions which change the
	 * state should not be called on the <code>decoder</code> while in the callback.
	 *
	 * @param  decoder  The decoder instance calling the callback.
	 * @param  absolute_byte_offset  The offset from the beginning of the stream
	 *                               to seek to.
	 * @return FLAC__StreamDecoderSeekStatus
	 *    The callee's return status.
	 */
	public int dec_seek_callback(final StreamDecoder decoder, long absolute_byte_offset/*, Object client_data*/);
}

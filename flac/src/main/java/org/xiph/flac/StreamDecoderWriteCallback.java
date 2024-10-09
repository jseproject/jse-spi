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

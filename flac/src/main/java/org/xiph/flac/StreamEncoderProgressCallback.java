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

//typedef void (*FLAC__StreamEncoderProgressCallback)(const FLAC__StreamEncoder *encoder, FLAC__uint64 bytes_written, FLAC__uint64 samples_written, unsigned frames_written, unsigned total_frames_estimate, void *client_data);
public interface StreamEncoderProgressCallback {
	/** Signature for the progress callback.
	 *
	 *  A function pointer matching this signature may be passed to
	 *  FLAC__stream_encoder_init*_file() or FLAC__stream_encoder_init*_FILE().
	 *  The supplied function will be called when the encoder has finished
	 *  writing a frame.  The {@code total_frames_estimate argument} to the
	 *  callback will be based on the value from
	 *  FLAC__stream_encoder_set_total_samples_estimate().
	 *
	 * @apiNote In general, FLAC__StreamEncoder functions which change the
	 * state should not be called on the {@code encoder} while in the callback.
	 *
	 * @param  encoder          The encoder instance calling the callback.
	 * @param  bytes_written    Bytes written so far.
	 * @param  samples_written  Samples written so far.
	 * @param  frames_written   Frames written so far.
	 * @param  total_frames_estimate  The estimate of the total number of
	 *                                frames to be written.
	 */
	public void enc_progress_callback(final StreamEncoder encoder, long bytes_written, long samples_written, int frames_written, int total_frames_estimate/*, Object client_data*/);
}

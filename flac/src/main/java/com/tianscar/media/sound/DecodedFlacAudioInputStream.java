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

package com.tianscar.media.sound;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.xiph.flac.Format;
import org.xiph.flac.Frame;
import org.xiph.flac.StreamDecoder;
import org.xiph.flac.StreamDecoderErrorCallback;
import org.xiph.flac.StreamDecoderWriteCallback;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedFlacAudioInputStream extends TAsynchronousFilteredAudioInputStream
        implements StreamDecoderWriteCallback, StreamDecoderErrorCallback {

    private StreamDecoder mDecoder = new StreamDecoder();
    private final byte[] mBuffer = new byte[Format.FLAC__MAX_BLOCK_SIZE * Format.FLAC__MAX_CHANNELS * (Integer.SIZE >>> 3)]; /* WATCHOUT: can be up to 2 megs */

    public DecodedFlacAudioInputStream(AudioFormat outputFormat, FlacAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        mDecoder.set_md5_checking(true);
        int init_status;
        if (inputStream.isOgg()) init_status = mDecoder.init_ogg_FILE(inputStream.getFilteredInputStream(), this /* write_callback */, null /* metadata_callback */, this /* error_callback */);
        else init_status = mDecoder.init_FILE(inputStream.getFilteredInputStream(), this /* write_callback */, null /* metadata_callback */, this /* error_callback */);
        if (init_status != StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK) mDecoder.delete();
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedFlacAudioInputStream(AudioFormat, AudioInputStream)");
    }

    @Override
    public int read(byte[] abData, int nOffset, int nLength) throws IOException {
        int n = super.read(abData, nOffset, nLength);
        while (n == 0) n = super.read(abData, nOffset, nLength);
        return n;
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        if (!mDecoder.process_single() ||
                mDecoder.get_state() == StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM) {
            mDecoder.delete();
            mDecoder = null;
            getCircularBuffer().close();
        }
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    @Override
    public void dec_error_callback(StreamDecoder decoder, int status) throws IOException {
        if (TDebug.TraceAudioConverter) TDebug.out(new IOException(StreamDecoder.FLAC__StreamDecoderErrorStatusString[status]));
        getCircularBuffer().close();
    }

    @Override
    public int dec_write_callback(StreamDecoder decoder, Frame frame, int[][] buffer, int offset) {
        int sample, channel, ibyte;
        int bps = frame.header.bits_per_sample, channels = frame.header.channels;
        int blocksize = frame.header.blocksize;
        int written = 0;
        /* generic code for the rest */
        switch (bps) {
            case 8:
                for (sample = 0; sample < blocksize; sample ++) {
                    for (channel = 0; channel < channels; channel ++) {
                        mBuffer[written ++] = (byte) buffer[channel][sample];
                    }
                }
                break;
            case 16:
                switch (channels) {
                    case 1:
                        for (sample = 0; sample < blocksize; sample ++) {
                            ibyte = buffer[0][sample];
                            mBuffer[written ++] = (byte) (ibyte & 0xFF);
                            mBuffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        }
                        break;
                    case 2:
                        for (sample = 0; sample < blocksize; sample ++) {
                            ibyte = buffer[0][sample];
                            mBuffer[written ++] = (byte) (ibyte & 0xFF);
                            mBuffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                            ibyte = buffer[1][sample];
                            mBuffer[written ++] = (byte) (ibyte & 0xFF);
                            mBuffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        }
                        break;
                    default: /* works for any 'channels' but above flavors are faster for 1 and 2 */
                        for (sample = 0; sample < blocksize; sample ++) {
                            for (channel = 0; channel < channels; channel ++) {
                                ibyte = buffer[channel][sample];
                                mBuffer[written ++] = (byte) (ibyte & 0xFF);
                                mBuffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                            }
                        }
                        break;
                }
                break;
            case 24:
                for (sample = 0; sample < blocksize; sample ++) {
                    for (channel = 0; channel < channels; channel ++) {
                        ibyte = buffer[channel][sample];
                        mBuffer[written ++] = (byte) (ibyte & 0xFF);
                        mBuffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        mBuffer[written ++] = (byte) ((ibyte >> 16) & 0xFF);
                    }
                }
                break;
            default: return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
        }
		if (written > 0) getCircularBuffer().write(mBuffer, 0, written);
        return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
    }

}

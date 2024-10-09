/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2004 Wimba S.A.
 * Copyright (c) 2002-2004 Xiph.org Foundation
 * Copyright (c) 2002-2004 Jean-Marc Valin
 * Copyright (c) 1993, 2002 David Rowe
 * Copyright (c) 1992-1994	Jutta Degener, Carsten Bormann
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
 * - Neither the name of the Xiph.org Foundation nor the names of its
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

import org.gagravarr.ogg.OggFile;
import org.gagravarr.speex.SpeexAudioData;
import org.gagravarr.speex.SpeexFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.xiph.speex.SpeexDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedSpeexAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private final SpeexFile speexFile;
    private final byte[] pcm;
    private final AudioFormat audioFormat;
    private SpeexDecoder decoder = null;

    public DecodedSpeexAudioInputStream(AudioFormat outputFormat, SpeexAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        try {
            speexFile = new SpeexFile(new OggFile(inputStream.getFilteredInputStream()));
            pcm = new byte[2 * 256 * speexFile.getInfo().getFramesPerPacket() * speexFile.getInfo().getNumChannels()];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        audioFormat = inputStream.getFormat();
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedSpeexAudioInputStream(AudioFormat, AudioInputStream)");
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
        try {
            SpeexAudioData packet = speexFile.getNextAudioPacket();
            if (packet == null) {
                getCircularBuffer().close();
                return;
            }
            if (decoder == null) {
                decoder = new SpeexDecoder();
                decoder.init(speexFile.getInfo().getMode(),
                        (int) audioFormat.getSampleRate(), audioFormat.getChannels(), true);
            }
            byte[] samples = packet.getData();
            decoder.processData(samples, 0, samples.length);
            int decoded = decoder.getProcessedData(pcm, 0);
            getCircularBuffer().write(pcm, 0, decoded);
        } catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        } finally {
            if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        speexFile.close();
    }

}

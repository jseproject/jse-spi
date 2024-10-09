/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2016 Logan Stromberg
 * Copyright (c) 2007-2008 CSIRO
 * Copyright (c) 2007-2011 Xiph.Org Foundation
 * Copyright (c) 2006-2011 Skype Limited
 * Copyright (c) 2003-2004 Mark Borgerding
 * Copyright (c) 2001-2011 Microsoft Corporation,
 *                         Jean-Marc Valin, Gregory Maxwell,
 *                         Koen Vos, Timothy B. Terriberry
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
 * - Neither the name of Internet Society, IETF or IETF Trust, nor the
 * names of specific contributors, may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedOpusAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private final OpusFile opusFile;
    private final AudioFormat audioFormat;
    private OpusDecoder decoder = null;

    public DecodedOpusAudioInputStream(AudioFormat outputFormat, OpusAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        try {
            opusFile = new OpusFile(new OggFile(inputStream.getFilteredInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        audioFormat = inputStream.getFormat();
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedOpusAudioInputStream(AudioFormat, AudioInputStream)");
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
            OpusAudioData packet = opusFile.getNextAudioPacket();
            if (packet == null) {
                getCircularBuffer().close();
                return;
            }
            if (decoder == null)
                decoder = new OpusDecoder((int) audioFormat.getSampleRate(), audioFormat.getChannels());
            int packetSamples = packet.getNumberOfSamples();
            byte[] samples = packet.getData();
            short[] pcm = new short[packetSamples * audioFormat.getChannels()];
            decoder.decode(samples, 0, samples.length, pcm, 0, packetSamples, false);
            getCircularBuffer().write(toBytes(pcm, 0, pcm.length));
        } catch (IOException | OpusException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        } finally {
            if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
        }
    }

    private static byte[] toBytes(short[] input, int offset, int length) {
        byte[] bytes = new byte[length * 2];
        for (int c = 0; c < length; c++) {
            bytes[c * 2] = (byte) (input[c + offset] & 0xFF);
            bytes[c * 2 + 1] = (byte) ((input[c + offset] >> 8) & 0xFF);
        }
        return bytes;
    }

    @Override
    public void close() throws IOException {
        super.close();
        opusFile.close();
    }

}

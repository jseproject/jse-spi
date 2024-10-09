/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class DecodedAACAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private final InputStream audioInputStream;
    private final ADTSDemultiplexer demultiplexer;
    private final Decoder decoder;
    private final SampleBuffer sampleBuffer;
    private byte[] saved;

    public DecodedAACAudioInputStream(AudioFormat outputFormat, AACAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedAACAudioInputStream(AudioFormat, AudioInputStream)");
        audioInputStream = inputStream.getFilteredInputStream();
        this.demultiplexer = inputStream.getDemultiplexer();
        this.decoder = inputStream.getDecoder();
        this.sampleBuffer = inputStream.getSampleBuffer();
        saved = sampleBuffer.getData();
        TConversionTool.changeOrderOrSign(saved, 0, saved.length, 2);
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        try {
            if (saved == null) {
                decoder.decodeFrame(demultiplexer.readNextFrame(), sampleBuffer);
                byte[] data = sampleBuffer.getData();
                TConversionTool.changeOrderOrSign(data, 0, data.length, 2);
                getCircularBuffer().write(sampleBuffer.getData());
            }
            else {
                getCircularBuffer().write(saved);
                saved = null;
            }
        }
        catch (IOException e) {
            getCircularBuffer().close();
        }
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    @Override
    public void close() throws IOException {
        super.close();
        audioInputStream.close();
    }

}

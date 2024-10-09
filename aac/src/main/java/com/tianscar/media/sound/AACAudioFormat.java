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

import net.sourceforge.jaad.aac.SampleBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class AACAudioFormat extends AudioFormat {

    public static AACAudioFormat of(SampleBuffer sampleBuffer, double bitrate, boolean vbr) {
        Map<String, Object> properties = new HashMap<>();
        int sampleRate = sampleBuffer.getSampleRate();
        int channels = sampleBuffer.getChannels();
        properties.put("bitrate", bitrate);
        properties.put("vbr", vbr);
        return new AACAudioFormat(
                AACEncoding.AAC,
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, AudioSystem.NOT_SPECIFIED,
                sampleRate, true,
                properties);
    }

    public static AACAudioFormat of(SampleBuffer sampleBuffer) {
        int sampleRate = sampleBuffer.getSampleRate();
        int channels = sampleBuffer.getChannels();
        return new AACAudioFormat(
                AACEncoding.AAC,
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, AudioSystem.NOT_SPECIFIED,
                sampleRate, false);
    }

    public AACAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

    public AACAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

}

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

import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusStatistics;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class OpusAudioFormat extends AudioFormat {

    public static OpusAudioFormat of(OpusInfo opusInfo, OpusStatistics statistics) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", statistics.getAverageAudioBitrate());
        boolean vbr = statistics.getMinPacketBytes() != statistics.getMaxPacketBytes()
                || statistics.getMinPacketDuration() != statistics.getMaxPacketDuration();
        properties.put("vbr", vbr);
        return new OpusAudioFormat(OpusEncoding.OPUS,
                opusInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getNumChannels(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getSampleRate(), false,
                properties);
    }

    public static OpusAudioFormat of(OpusInfo opusInfo) {
        return new OpusAudioFormat(OpusEncoding.OPUS,
                opusInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getNumChannels(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getSampleRate(), false);
    }

    public OpusAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

    public OpusAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

}

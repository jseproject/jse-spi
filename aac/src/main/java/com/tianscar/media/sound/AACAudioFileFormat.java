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

import net.sourceforge.jaad.aac.AACDecoderConfig;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.ChannelConfiguration;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.Profile;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.aac.SampleFrequency;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

public class AACAudioFileFormat extends ExtendedAudioFileFormat {

    public static AACAudioFileFormat of(ADTSDemultiplexer demultiplexer) throws AACException {
        Decoder decoder = new Decoder(demultiplexer.getDecoderSpecificInfo());
        Map<String, Object> properties = new HashMap<>();
        SampleBuffer sampleBuffer = new SampleBuffer();
        AACDecoderConfig config = decoder.getConfig();
        properties.put("aac.corecoder", config.isDependsOnCoreCoder());
        if (config.isDependsOnCoreCoder()) properties.put("aac.corecoder.delay", config.getCoreCoderDelay());
        properties.put("aac.frame.size.bytes", config.getFrameLength());
        properties.put("aac.sbr", config.isSBRPresent());
        if (config.isSBRPresent()) properties.put("aac.sbr.downsampled", config.isSBRDownSampled());
        properties.put("aac.frame.small", config.isSmallFrameUsed());
        properties.put("aac.resilience.scalefactor", config.isScalefactorResilienceUsed());
        properties.put("aac.resilience.sectiondata", config.isSectionDataResilienceUsed());
        properties.put("aac.resilience.spectraldata", config.isSpectralDataResilienceUsed());
        SampleFrequency sampleFrequency = config.getSampleFrequency();
        properties.put("aac.frequency.hz", sampleFrequency.getFrequency());
        properties.put("aac.frequency.maximalprediction.sfb", sampleFrequency.getMaximalPredictionSFB());
        properties.put("aac.frequency.maximaltns.sfb", sampleFrequency.getMaximalTNS_SFB(false));
        properties.put("aac.frequency.maximaltns.sfb.short", sampleFrequency.getMaximalTNS_SFB(true));
        properties.put("aac.frequency.predictors", sampleFrequency.getPredictorCount());
        ChannelConfiguration channelConfiguration = config.getChannelConfiguration();
        properties.put("aac.channels", channelConfiguration.getChannelCount());
        properties.put("aac.channels.description", channelConfiguration.getDescription());
        Profile profile = config.getProfile();
        properties.put("aac.profile", profile.getDescription());
        BigDecimal totalBitrate = BigDecimal.ZERO;
        BigDecimal lastBitrate = null;
        BigDecimal currentBitrate;
        double minBitrate = Double.MAX_VALUE;
        double maxBitrate = Double.MIN_VALUE;
        double seconds = 0;
        int blocks = 0;
        boolean vbr = false;
        long frameLength = 0;
        try {
            while (true) {
                decoder.decodeFrame(demultiplexer.readNextFrame(), sampleBuffer);
                minBitrate = Math.min(minBitrate, sampleBuffer.getBitrate());
                maxBitrate = Math.max(maxBitrate, sampleBuffer.getBitrate());
                currentBitrate = BigDecimal.valueOf(sampleBuffer.getBitrate());
                if (!vbr) {
                    if (lastBitrate != null && !lastBitrate.equals(currentBitrate)) vbr = true;
                    lastBitrate = currentBitrate;
                }
                totalBitrate = totalBitrate.add(currentBitrate);
                seconds += sampleBuffer.getLength();
                frameLength += sampleBuffer.getBitsRead() / sampleBuffer.getBitsPerSample();
                blocks ++;
            }
        }
        catch (IOException ignored) {
        }
        double averageBitrate = totalBitrate.divide(BigDecimal.valueOf(blocks), MathContext.DECIMAL64).doubleValue();
        properties.put("duration", Math.round(seconds * 1_000_000L));
        properties.put("aac.duration.seconds", seconds);
        properties.put("aac.duration.blocks", blocks);
        properties.put("aac.duration.frames", frameLength);
        properties.put("aac.bitrate.lower", minBitrate);
        properties.put("aac.bitrate.nominal", averageBitrate);
        properties.put("aac.bitrate.upper", maxBitrate);
        properties.put("aac.vbr", vbr);
        return new AACAudioFileFormat(AACFileFormatType.AAC, AACAudioFormat.of(sampleBuffer, averageBitrate, vbr),
                frameLength,
                AudioSystem.NOT_SPECIFIED,
                properties);
    }

    public AACAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

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

import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacInfo;
import org.gagravarr.flac.FlacOggInfo;
import org.gagravarr.flac.FlacTags;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlacAudioFileFormat extends ExtendedAudioFileFormat {

    public static FlacAudioFileFormat of(FlacFile flacFile) {
        return of(flacFile.getInfo(), flacFile.getTags());
    }

    public static FlacAudioFileFormat of(FlacInfo flacInfo, FlacTags flacTags) {
        Map<String, Object> properties = new HashMap<>();
        long numberOfSamples = flacInfo.getNumberOfSamples();
        if (numberOfSamples != 0) {
            properties.put("duration", flacInfo.getNumberOfSamples() * 1_000_000L / flacInfo.getSampleRate());
            properties.put("flac.duration.frames", flacInfo.getNumberOfSamples());
        }
        properties.put("flac.channels", flacInfo.getNumChannels());
        properties.put("flac.frequency.hz", flacInfo.getSampleRate());
        if (flacInfo instanceof FlacOggInfo) {
            properties.put("ogg.channels", flacInfo.getNumChannels());
            properties.put("ogg.frequency.hz", flacInfo.getSampleRate());
            properties.put("ogg.bitrate", flacInfo.getBitsPerSample() * flacInfo.getSampleRate());
            properties.put("ogg.preskip.frames", flacInfo.getPreSkip());
            properties.put("ogg.version", ((FlacOggInfo) flacInfo).getVersionString());
        }
        properties.put("flac.frame.size.min", flacInfo.getMinimumFrameSize());
        properties.put("flac.frame.size.max", flacInfo.getMaximumFrameSize());
        properties.put("flac.block.size.min", flacInfo.getMinimumBlockSize());
        properties.put("flac.block.size.max", flacInfo.getMaximumBlockSize());
        properties.put("flac.vbr", flacInfo.getMaximumFrameSize() != flacInfo.getMinimumFrameSize());
        properties.put("flac.type", flacInfo.getType());
        properties.put("flac.signature", flacInfo.getSignature().clone());
        properties.put("flac.frame.size", flacInfo.getBitsPerSample());
        properties.put("flac.frame.size.bytes", flacInfo.getBitsPerSample() / 8);
        properties.put("title", flacTags.getTitle());
        properties.put("author", flacTags.getArtist());
        properties.put("album", flacTags.getAlbum());
        properties.put("date", flacTags.getDate());
        properties.put("copyright", getSingleComment(flacTags, "copyright"));
        properties.put("comment", getSingleComment(flacTags, "comment"));
        for (Map.Entry<String, List<String>> entry : flacTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new FlacAudioFileFormat(FlacFileFormatType.OGG_FLAC, FlacAudioFormat.of(flacInfo),
                numberOfSamples == 0 ? AudioSystem.NOT_SPECIFIED : numberOfSamples,
                AudioSystem.NOT_SPECIFIED,
                properties);
    }

    private static String getSingleComment(FlacTags flacTags, String tag) {
        List<String> comments = flacTags.getComments(tag);
        if (comments != null && !comments.isEmpty()) return comments.get(0);
        else return null;
    }

    public FlacAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

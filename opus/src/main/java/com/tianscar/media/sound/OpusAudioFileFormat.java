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

import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusStatistics;
import org.gagravarr.opus.OpusTags;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpusAudioFileFormat extends ExtendedAudioFileFormat {

    public static OpusAudioFileFormat of(OpusFile opusFile) throws IOException {
        OpusStatistics statistics = new OpusStatistics(opusFile);
        statistics.calculate();
        return of(opusFile.getInfo(), opusFile.getTags(), statistics);
    }

    public static OpusAudioFileFormat of(OpusInfo opusInfo, OpusTags opusTags, OpusStatistics statistics) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ogg.data.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.data.length", statistics.getAudioDataSize());
        properties.put("ogg.duration.seconds", statistics.getDurationSeconds());
        properties.put("ogg.duration", statistics.getDuration());
        properties.put("duration", Math.round(statistics.getDurationSeconds() * 1_000_000L));
        properties.put("ogg.bitrate", statistics.getAverageAudioBitrate());
        properties.put("opus.packet.bytes.min", statistics.getMinPacketBytes());
        properties.put("opus.packet.bytes.max", statistics.getMaxPacketBytes());
        properties.put("opus.packet.duration.min", statistics.getMinPacketDuration());
        properties.put("opus.packet.duration.max", statistics.getMaxPacketDuration());
        properties.put("opus.page.duration.min", statistics.getMinPageDuration());
        properties.put("opus.page.duration.max", statistics.getMaxPageDuration());
        boolean vbr = statistics.getMinPacketBytes() != statistics.getMaxPacketBytes()
                || statistics.getMinPacketDuration() != statistics.getMaxPacketDuration();
        properties.put("opus.vbr", vbr);
        properties.put("ogg.channels", opusInfo.getNumChannels());
        properties.put("ogg.frequency.hz", opusInfo.getSampleRate());
        properties.put("ogg.preskip.frames", opusInfo.getPreSkip());
        properties.put("ogg.version", (int) opusInfo.getVersion());
        properties.put("opus.version.major", opusInfo.getMajorVersion());
        properties.put("opus.version.minor", opusInfo.getMinorVersion());
        properties.put("opus.version.string", opusInfo.getVersionString());
        properties.put("opus.version", opusInfo.getVersion());
        properties.put("title", opusTags.getTitle());
        properties.put("author", opusTags.getArtist());
        properties.put("album", opusTags.getAlbum());
        properties.put("date", opusTags.getDate());
        properties.put("copyright", getSingleComment(opusTags, "copyright"));
        properties.put("comment", getSingleComment(opusTags, "comment"));
        for (Map.Entry<String, List<String>> entry : opusTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new OpusAudioFileFormat(OpusFileFormatType.OPUS, OpusAudioFormat.of(opusInfo, statistics),
                AudioSystem.NOT_SPECIFIED,
                statistics.getAudioDataSize(),
                properties);
    }

    private static String getSingleComment(OpusTags opusTags, String tag) {
        List<String> comments = opusTags.getComments(tag);
        if (comments != null && !comments.isEmpty()) return comments.get(0);
        else return null;
    }

    public OpusAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

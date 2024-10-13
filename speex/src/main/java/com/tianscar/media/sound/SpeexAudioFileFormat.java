/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2004 Marc Gimpel, Wimba S.A.
 * Copyright (c) 2002-2004 Xiph.org Foundation
 * Copyright (c) 2002-2004 Jean-Marc Valin
 * Copyright (c) 1993, 2002 David Rowe
 * Copyright (c) 1992-1994 Jutta Degener,
 *                         Carsten Bormann,
 *                         Berlin University of Technology
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

import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.speex.SpeexFile;
import org.gagravarr.speex.SpeexInfo;
import org.gagravarr.speex.SpeexTags;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeexAudioFileFormat extends ExtendedAudioFileFormat {

    public static SpeexAudioFileFormat of(SpeexFile speexFile) throws IOException {
        OggAudioStatistics statistics = new OggAudioStatistics(speexFile, speexFile);
        statistics.calculate();
        return of(speexFile.getInfo(), speexFile.getTags(), statistics);
    }

    public static SpeexAudioFileFormat of(SpeexInfo speexInfo, SpeexTags speexTags, OggAudioStatistics statistics) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ogg.data.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.data.length", statistics.getAudioDataSize());
        properties.put("ogg.duration.seconds", statistics.getDurationSeconds());
        properties.put("ogg.duration", statistics.getDuration());
        properties.put("duration", Math.round(statistics.getDurationSeconds() * 1_000_000L));
        properties.put("ogg.bitrate", statistics.getAverageAudioBitrate());
        properties.put("speex.extraheaders", speexInfo.getExtraHeaders());
        properties.put("speex.bitrate", speexInfo.getBitrate());
        properties.put("speex.frame.size.bytes", speexInfo.getFrameSize());
        properties.put("speex.mode", speexInfo.getMode());
        properties.put("speex.mode.bitstreamversion", speexInfo.getModeBitstreamVersion());
        properties.put("speex.packet.frames", speexInfo.getFramesPerPacket());
        properties.put("speex.vbr", speexInfo.getVbr() == 1);
        properties.put("ogg.channels", speexInfo.getNumChannels());
        properties.put("ogg.frequency.hz", speexInfo.getSampleRate());
        properties.put("ogg.preskip.frames", speexInfo.getPreSkip());
        properties.put("ogg.version", speexInfo.getVersionId());
        properties.put("speex.version", speexInfo.getVersionId());
        properties.put("speex.version.string", speexInfo.getVersionString());
        properties.put("speex.reserved.1", speexInfo.getReserved1());
        properties.put("speex.reserved.2", speexInfo.getReserved2());
        properties.put("title", speexTags.getTitle());
        properties.put("author", speexTags.getArtist());
        properties.put("album", speexTags.getAlbum());
        properties.put("date", speexTags.getDate());
        properties.put("copyright", getSingleComment(speexTags, "copyright"));
        properties.put("comment", getSingleComment(speexTags, "comment"));
        for (Map.Entry<String, List<String>> entry : speexTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new SpeexAudioFileFormat(SpeexFileFormatType.SPEEX, SpeexAudioFormat.of(speexInfo),
                AudioSystem.NOT_SPECIFIED,
                statistics.getAudioDataSize(),
                properties);
    }

    private static String getSingleComment(SpeexTags speexTags, String tag) {
        List<String> comments = speexTags.getComments(tag);
        if (comments != null && !comments.isEmpty()) return comments.get(0);
        else return null;
    }

    public SpeexAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
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
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisFile;
import org.gagravarr.vorbis.VorbisInfo;
import org.gagravarr.vorbis.VorbisSetup;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VorbisAudioFileFormat extends ExtendedAudioFileFormat {

    public static VorbisAudioFileFormat of(VorbisFile vorbisFile) throws IOException {
        OggAudioStatistics statistics = new OggAudioStatistics(vorbisFile, vorbisFile);
        statistics.calculate();
        return of(vorbisFile.getInfo(), vorbisFile.getSetup(), vorbisFile.getTags(), statistics);
    }

    public static VorbisAudioFileFormat of(VorbisInfo vorbisInfo, VorbisSetup vorbisSetup, VorbisComments vorbisComments, OggAudioStatistics statistics) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ogg.data.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.data.length", statistics.getAudioDataSize());
        properties.put("ogg.duration.seconds", statistics.getDurationSeconds());
        properties.put("ogg.duration", statistics.getDuration());
        properties.put("duration", Math.round(statistics.getDurationSeconds() * 1_000_000L));
        properties.put("ogg.bitrate", statistics.getAverageAudioBitrate());
        properties.put("vorbis.vbr", vorbisInfo.getBitrateUpper() != vorbisInfo.getBitrateLower());
        properties.put("ogg.channels", vorbisInfo.getNumChannels());
        properties.put("ogg.frequency.hz", vorbisInfo.getSampleRate());
        properties.put("ogg.preskip.frames", vorbisInfo.getPreSkip());
        properties.put("ogg.version", vorbisInfo.getVersion());
        properties.put("vorbis.block.size.0", vorbisInfo.getBlocksize0());
        properties.put("vorbis.block.size.1", vorbisInfo.getBlocksize1());
        properties.put("vorbis.bitrate.lower", vorbisInfo.getBitrateLower());
        properties.put("vorbis.bitrate.nominal", vorbisInfo.getBitrateNominal());
        properties.put("vorbis.bitrate.upper", vorbisInfo.getBitrateUpper());
        properties.put("vorbis.version", vorbisInfo.getVersion());
        properties.put("vorbis.version.string", vorbisInfo.getVersionString());
        properties.put("vorbis.codebooks", vorbisSetup.getNumberOfCodebooks());
        properties.put("title", vorbisComments.getTitle());
        properties.put("author", vorbisComments.getArtist());
        properties.put("album", vorbisComments.getAlbum());
        properties.put("date", vorbisComments.getDate());
        properties.put("copyright", getSingleComment(vorbisComments, "copyright"));
        properties.put("comment", getSingleComment(vorbisComments, "comment"));
        for (Map.Entry<String, List<String>> entry : vorbisComments.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new VorbisAudioFileFormat(VorbisFileFormatType.VORBIS, VorbisAudioFormat.of(vorbisInfo),
                AudioSystem.NOT_SPECIFIED,
                statistics.getAudioDataSize(),
                properties);
    }

    private static String getSingleComment(VorbisComments vorbisComments, String tag) {
        List<String> comments = vorbisComments.getComments(tag);
        if (comments != null && !comments.isEmpty()) return comments.get(0);
        else return null;
    }

    public VorbisAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

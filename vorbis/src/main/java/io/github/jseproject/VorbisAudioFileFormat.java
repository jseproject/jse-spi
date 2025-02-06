package io.github.jseproject;

import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisFile;
import org.gagravarr.vorbis.VorbisInfo;
import org.gagravarr.vorbis.VorbisSetup;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        properties.put("ogg.duration.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.duration.bytes", statistics.getAudioDataSize());
        properties.put("ogg.duration.seconds", statistics.getDurationSeconds());
        properties.put("ogg.duration", statistics.getDuration());
        properties.put("duration", Math.round(statistics.getDurationSeconds() * 1_000_000L));
        properties.put("ogg.bitrate", statistics.getAverageAudioBitrate());
        properties.put("vorbis.vbr", vorbisInfo.getBitrateUpper() != vorbisInfo.getBitrateLower());
        properties.put("ogg.channels", vorbisInfo.getNumChannels());
        properties.put("ogg.frequency.hz", vorbisInfo.getSampleRate());
        properties.put("ogg.preskip.samples", vorbisInfo.getPreSkip());
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
        properties.put("date", parseDate(vorbisComments.getDate()));
        properties.put("copyright", getSingleComment(vorbisComments, "copyright"));
        properties.put("comment", getSingleComment(vorbisComments, "comment"));
        for (Map.Entry<String, List<String>> entry : vorbisComments.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new VorbisAudioFileFormat(VorbisFileFormatType.VORBIS, VorbisAudioFormat.of(vorbisInfo),
                AudioSystem.NOT_SPECIFIED,
                statistics.getAudioDataSize() + statistics.getHeaderOverheadSize() + statistics.getOggOverheadSize(),
                properties);
    }

    private static String getSingleComment(VorbisComments vorbisComments, String tag) {
        List<String> comments = vorbisComments.getComments(tag);
        if (comments != null && !comments.isEmpty()) return comments.get(0);
        else return null;
    }

    private static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final SimpleDateFormat YYYY_MM = new SimpleDateFormat("yyyy-MM'T'HH:mm:ssXXX");
    private static final SimpleDateFormat YYYY = new SimpleDateFormat("yyyy'T'HH:mm:ssXXX");
    private static Date parseDate(String dateString) {
        if (dateString == null) return null;
        else {
            if (dateString.endsWith("Z")) dateString = dateString.substring(0, dateString.length() - 1) + "+00:00";
            try {
                return YYYY_MM_DD.parse(dateString);
            } catch (ParseException e) {
                try {
                    return YYYY_MM.parse(dateString);
                } catch (ParseException ex) {
                    try {
                        return YYYY.parse(dateString);
                    } catch (ParseException exception) {
                        return null;
                    }
                }
            }
        }
    }

    public VorbisAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

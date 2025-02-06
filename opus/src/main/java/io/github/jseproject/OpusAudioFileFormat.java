package io.github.jseproject;

import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusStatistics;
import org.gagravarr.opus.OpusTags;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        properties.put("ogg.duration.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.duration.bytes", statistics.getAudioDataSize());
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
        //boolean vbr = statistics.getMinPacketBytes() != statistics.getMaxPacketBytes()
        //        || statistics.getMinPacketDuration() != statistics.getMaxPacketDuration();
        //properties.put("opus.vbr", vbr);
        properties.put("ogg.channels", opusInfo.getNumChannels());
        properties.put("ogg.frequency.hz", opusInfo.getSampleRate());
        properties.put("ogg.preskip.samples", opusInfo.getPreSkip());
        properties.put("ogg.version", (int) opusInfo.getVersion());
        properties.put("opus.version.major", opusInfo.getMajorVersion());
        properties.put("opus.version.minor", opusInfo.getMinorVersion());
        properties.put("opus.version.string", opusInfo.getVersionString());
        properties.put("opus.version", opusInfo.getVersion());
        properties.put("title", opusTags.getTitle());
        properties.put("author", opusTags.getArtist());
        properties.put("album", opusTags.getAlbum());
        properties.put("date", parseDate(opusTags.getDate()));
        properties.put("copyright", getSingleComment(opusTags, "copyright"));
        properties.put("comment", getSingleComment(opusTags, "comment"));
        for (Map.Entry<String, List<String>> entry : opusTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new OpusAudioFileFormat(OpusFileFormatType.OPUS, OpusAudioFormat.of(opusInfo, statistics),
                AudioSystem.NOT_SPECIFIED,
                statistics.getAudioDataSize() + statistics.getHeaderOverheadSize() + statistics.getOggOverheadSize(),
                properties);
    }

    private static String getSingleComment(OpusTags opusTags, String tag) {
        List<String> comments = opusTags.getComments(tag);
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

    public OpusAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

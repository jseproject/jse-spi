package io.github.jseproject;

import org.gagravarr.ogg.audio.OggAudioStatistics;
import org.gagravarr.speex.SpeexFile;
import org.gagravarr.speex.SpeexInfo;
import org.gagravarr.speex.SpeexTags;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        properties.put("ogg.duration.packets", statistics.getAudioPacketsCount());
        properties.put("ogg.duration.bytes", statistics.getAudioDataSize());
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
        properties.put("ogg.preskip.samples", speexInfo.getPreSkip());
        properties.put("ogg.version", speexInfo.getVersionId());
        properties.put("speex.version", speexInfo.getVersionId());
        properties.put("speex.version.string", speexInfo.getVersionString());
        properties.put("speex.reserved.1", speexInfo.getReserved1());
        properties.put("speex.reserved.2", speexInfo.getReserved2());
        properties.put("title", speexTags.getTitle());
        properties.put("author", speexTags.getArtist());
        properties.put("album", speexTags.getAlbum());
        properties.put("date", parseDate(speexTags.getDate()));
        properties.put("copyright", getSingleComment(speexTags, "copyright"));
        properties.put("comment", getSingleComment(speexTags, "comment"));
        for (Map.Entry<String, List<String>> entry : speexTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new SpeexAudioFileFormat(SpeexFileFormatType.SPEEX, SpeexAudioFormat.of(speexInfo),
                (long) statistics.getAudioPacketsCount() * speexInfo.getFramesPerPacket(),
                statistics.getAudioDataSize() + statistics.getHeaderOverheadSize() + statistics.getOggOverheadSize(),
                properties);
    }

    private static String getSingleComment(SpeexTags speexTags, String tag) {
        List<String> comments = speexTags.getComments(tag);
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

    public SpeexAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

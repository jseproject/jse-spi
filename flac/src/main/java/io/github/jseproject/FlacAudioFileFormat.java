package io.github.jseproject;

import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacInfo;
import org.gagravarr.flac.FlacOggInfo;
import org.gagravarr.flac.FlacTags;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            properties.put("duration", Math.round((double) numberOfSamples / flacInfo.getSampleRate() * 1_000_000L));
            properties.put("flac.duration.samples", numberOfSamples);
        }
        properties.put("flac.channels", flacInfo.getNumChannels());
        properties.put("flac.frequency.hz", flacInfo.getSampleRate());
        if (flacInfo instanceof FlacOggInfo) {
            properties.put("ogg.channels", flacInfo.getNumChannels());
            properties.put("ogg.frequency.hz", flacInfo.getSampleRate());
            properties.put("ogg.bitrate", flacInfo.getBitsPerSample() * flacInfo.getSampleRate());
            properties.put("ogg.preskip.samples", flacInfo.getPreSkip());
            properties.put("ogg.version", ((FlacOggInfo) flacInfo).getVersionString());
        }
        properties.put("flac.frame.size.min", flacInfo.getMinimumFrameSize());
        properties.put("flac.frame.size.max", flacInfo.getMaximumFrameSize());
        properties.put("flac.block.size.min", flacInfo.getMinimumBlockSize());
        properties.put("flac.block.size.max", flacInfo.getMaximumBlockSize());
        //properties.put("flac.vbr", flacInfo.getMaximumFrameSize() != flacInfo.getMinimumFrameSize());
        properties.put("flac.type", flacInfo.getType());
        properties.put("flac.signature", flacInfo.getSignature().clone());
        //properties.put("flac.sample.size.bits", flacInfo.getBitsPerSample());
        properties.put("flac.sample.size.bytes", flacInfo.getBitsPerSample() / 8);
        properties.put("title", flacTags.getTitle());
        properties.put("author", flacTags.getArtist());
        properties.put("album", flacTags.getAlbum());
        properties.put("date", parseDate(flacTags.getDate()));
        properties.put("copyright", getSingleComment(flacTags, "copyright"));
        properties.put("comment", getSingleComment(flacTags, "comment"));
        for (Map.Entry<String, List<String>> entry : flacTags.getAllComments().entrySet()) {
            properties.put("ogg.comment." + entry.getKey(), entry.getValue());
        }
        return new FlacAudioFileFormat(
                flacInfo instanceof FlacOggInfo ? FlacFileFormatType.OGG_FLAC : FlacFileFormatType.FLAC,
                FlacAudioFormat.of(flacInfo),
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                properties);
    }

    private static String getSingleComment(FlacTags flacTags, String tag) {
        List<String> comments = flacTags.getComments(tag);
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

    public FlacAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

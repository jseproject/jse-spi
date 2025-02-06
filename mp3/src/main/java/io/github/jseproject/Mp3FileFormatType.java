package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class Mp3FileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type MP3 = new Mp3FileFormatType("MP3", "mp3");
    public static final AudioFileFormat.Type MP2 = new Mp3FileFormatType("MP2", "mp2");
    public static final AudioFileFormat.Type MP1 = new Mp3FileFormatType("MP1", "mp1");

    public Mp3FileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof Mp3FileFormatType) ((Mp3FileFormatType) type).properties = properties;
        return type;
    }

}

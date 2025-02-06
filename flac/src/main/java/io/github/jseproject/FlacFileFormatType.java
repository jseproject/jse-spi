package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class FlacFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type FLAC = new FlacFileFormatType("FLAC", "flac");
    public static final AudioFileFormat.Type OGG_FLAC = new FlacFileFormatType("OGG-FLAC", "ogg");

    public FlacFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof FlacFileFormatType) ((FlacFileFormatType) type).properties = properties;
        return type;
    }

}

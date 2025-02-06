package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class OpusFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type OPUS = new OpusFileFormatType("OGG-OPUS", "opus");

    public OpusFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof OpusFileFormatType) ((OpusFileFormatType) type).properties = properties;
        return type;
    }

}

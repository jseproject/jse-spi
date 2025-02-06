package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class SpeexFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type SPEEX = new SpeexFileFormatType("OGG-SPEEX", "spx");

    public SpeexFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof SpeexFileFormatType) ((SpeexFileFormatType) type).properties = properties;
        return type;
    }

}

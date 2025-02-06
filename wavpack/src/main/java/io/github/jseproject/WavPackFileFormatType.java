package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class WavPackFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type WAVPACK = new WavPackFileFormatType("WAVPACK", "wv");

    public WavPackFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof WavPackFileFormatType) ((WavPackFileFormatType) type).properties = properties;
        return type;
    }

}

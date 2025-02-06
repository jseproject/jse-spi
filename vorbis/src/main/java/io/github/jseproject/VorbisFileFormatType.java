package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class VorbisFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type VORBIS = new VorbisFileFormatType("OGG-VORBIS", "ogg");

    public VorbisFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof VorbisFileFormatType) ((VorbisFileFormatType) type).properties = properties;
        return type;
    }

}

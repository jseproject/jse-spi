package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;
import java.util.Map;

public class APEFileFormatType extends AudioFileFormat.Type {

    public static final APEFileFormatType APE = new APEFileFormatType("Monkey's Audio", "ape");
    public static final APEFileFormatType MAC = new APEFileFormatType("Monkey's Audio", "mac");

    public APEFileFormatType(String name, String extension) {
        super(name, extension);
    }

    Map<String, Object> properties;
    static AudioFileFormat.Type withProperties(AudioFileFormat.Type type, Map<String, Object> properties) {
        if (type instanceof APEFileFormatType) ((APEFileFormatType) type).properties = properties;
        return type;
    }

}

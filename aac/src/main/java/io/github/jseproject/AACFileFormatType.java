package io.github.jseproject;

import javax.sound.sampled.AudioFileFormat;

public class AACFileFormatType extends AudioFileFormat.Type {

    public static final AudioFileFormat.Type AAC = new AACFileFormatType("AAC", "aac");

    public AACFileFormatType(String name, String extension) {
        super(name, extension);
    }

}

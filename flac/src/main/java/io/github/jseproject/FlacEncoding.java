package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class FlacEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding FLAC = new FlacEncoding("FLAC");

    public FlacEncoding(String name) {
        super(name);
    }

}

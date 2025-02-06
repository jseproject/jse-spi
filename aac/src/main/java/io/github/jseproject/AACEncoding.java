package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class AACEncoding extends AudioFormat.Encoding {

    public static final AACEncoding AAC = new AACEncoding("AAC");

    public AACEncoding(String name) {
        super(name);
    }

}

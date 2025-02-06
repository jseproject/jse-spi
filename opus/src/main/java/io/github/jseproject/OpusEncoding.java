package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class OpusEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding OPUS = new OpusEncoding("OPUS");

    public OpusEncoding(String name) {
        super(name);
    }

}

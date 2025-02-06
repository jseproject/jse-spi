package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class WavPackEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding WAVPACK = new WavPackEncoding("WAVPACK");

    public WavPackEncoding(String name) {
        super(name);
    }

}

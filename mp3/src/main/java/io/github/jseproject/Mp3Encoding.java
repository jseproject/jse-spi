package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class Mp3Encoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding MPEG1_L1 = new Mp3Encoding("MPEG1_L1");
    public static final AudioFormat.Encoding MPEG2_L1 = new Mp3Encoding("MPEG2_L1");
    public static final AudioFormat.Encoding MPEG2DOT5_L1 = new Mp3Encoding("MPEG2DOT5_L1");

    public static final AudioFormat.Encoding MPEG1_L2 = new Mp3Encoding("MPEG1_L2");
    public static final AudioFormat.Encoding MPEG2_L2 = new Mp3Encoding("MPEG2_L2");
    public static final AudioFormat.Encoding MPEG2DOT5_L2 = new Mp3Encoding("MPEG2DOT5_L2");

    public static final AudioFormat.Encoding MPEG1_L3 = new Mp3Encoding("MPEG1_L3");
    public static final AudioFormat.Encoding MPEG2_L3 = new Mp3Encoding("MPEG2_L3");
    public static final AudioFormat.Encoding MPEG2DOT5_L3 = new Mp3Encoding("MPEG2DOT5_L3");

    public Mp3Encoding(String name) {
        super(name);
    }

}

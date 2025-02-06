package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class APEEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding APE = new APEEncoding("APE");

    public APEEncoding(String name) {
        super(name);
    }

}

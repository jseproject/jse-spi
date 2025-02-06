package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class SpeexEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding SPEEX = new SpeexEncoding("SPEEX");

    public SpeexEncoding(String name) {
        super(name);
    }

}

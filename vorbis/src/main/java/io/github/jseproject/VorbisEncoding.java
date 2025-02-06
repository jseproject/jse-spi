package io.github.jseproject;

import javax.sound.sampled.AudioFormat;

public class VorbisEncoding extends AudioFormat.Encoding {

    public static final AudioFormat.Encoding VORBIS = new VorbisEncoding("VORBIS");

    public VorbisEncoding(String name) {
        super(name);
    }

}

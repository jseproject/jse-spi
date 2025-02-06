package io.github.jseproject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;

public class OpusAudioInputStream extends AudioInputStream {

    private final InputStream source;

    OpusAudioInputStream(InputStream stream, AudioFormat format, long length) {
        super(stream, format, length);
        source = stream;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

}

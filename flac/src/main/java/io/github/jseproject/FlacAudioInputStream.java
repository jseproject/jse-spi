package io.github.jseproject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;

public class FlacAudioInputStream extends AudioInputStream {

    private final InputStream source;
    private final boolean isogg;

    FlacAudioInputStream(InputStream stream, AudioFormat format, long length, boolean isogg) {
        super(stream, format, length);
        source = stream;
        this.isogg = isogg;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

    public boolean isOgg() {
        return isogg;
    }

}

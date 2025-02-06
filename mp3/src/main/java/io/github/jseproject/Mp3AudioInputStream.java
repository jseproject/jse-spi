package io.github.jseproject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;

public class Mp3AudioInputStream extends AudioInputStream {

    private final InputStream source;

    public Mp3AudioInputStream(InputStream stream, AudioFormat format, long length) {
        super(stream, format, length);
        source = stream;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

}

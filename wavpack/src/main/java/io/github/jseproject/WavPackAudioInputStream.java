package io.github.jseproject;

import com.beatofthedrum.wv.WavPackContext;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class WavPackAudioInputStream extends AudioInputStream {

    private final InputStream source;
    WavPackContext context;

    public WavPackAudioInputStream(AudioFormat format, InputStream stream, WavPackContext context) {
        super(stream, format, AudioSystem.NOT_SPECIFIED);
        source = stream;
        this.context = context;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

    @Override
    public void close() throws IOException {
        super.close();
        context = null;
    }

}

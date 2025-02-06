package io.github.jseproject;

import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AACAudioInputStream extends AudioInputStream {

    private final InputStream source;

    ADTSDemultiplexer demultiplexer;
    Decoder decoder;
    SampleBuffer sampleBuffer;

    AACAudioInputStream(InputStream stream, AudioFormat format,
                        ADTSDemultiplexer demultiplexer, Decoder decoder, SampleBuffer sampleBuffer,
                        long length) {
        super(stream, format, length);
        source = stream;
        this.demultiplexer = demultiplexer;
        this.decoder = decoder;
        this.sampleBuffer = sampleBuffer;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

    @Override
    public void close() throws IOException {
        super.close();
        demultiplexer = null;
        decoder = null;
        sampleBuffer = null;
    }

}

package io.github.jseproject;

import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.util.IoFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;

public class APEAudioInputStream extends AudioInputStream {

    private final InputStream source;
    IoFile io;
    IAPEDecompress decoder;

    APEAudioInputStream(InputStream stream, AudioFormat format, IoFile io, IAPEDecompress decoder) {
        super(stream, format, decoder.getApeInfoTotalFrames());
        source = stream;
        this.io = io;
        this.decoder = decoder;
    }

    public InputStream getFilteredInputStream() {
        return source;
    }

    @Override
    public void close() throws IOException {
        super.close();
        io.close();
        io = null;
        decoder = null;
    }

}

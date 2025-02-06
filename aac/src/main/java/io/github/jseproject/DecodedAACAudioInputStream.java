package io.github.jseproject;

import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedAACAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private AACAudioInputStream audioInputStream;
    private ADTSDemultiplexer demultiplexer;
    private Decoder decoder;
    private SampleBuffer sampleBuffer;
    private byte[] saved;

    public DecodedAACAudioInputStream(AudioFormat outputFormat, AACAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedAACAudioInputStream(AudioFormat, AudioInputStream)");
        audioInputStream = inputStream;
        this.demultiplexer = inputStream.demultiplexer;
        this.decoder = inputStream.decoder;
        this.sampleBuffer = inputStream.sampleBuffer;
        saved = sampleBuffer.getData();
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        try {
            if (saved == null) {
                decoder.decodeFrame(demultiplexer.readNextFrame(), sampleBuffer);
                getCircularBuffer().write(sampleBuffer.getData());
            }
            else {
                getCircularBuffer().write(saved);
                saved = null;
            }
        }
        catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        }
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    @Override
    public void close() throws IOException {
        super.close();
        audioInputStream.close();
        audioInputStream = null;
        demultiplexer = null;
        decoder = null;
        sampleBuffer = null;
        saved = null;
    }

}

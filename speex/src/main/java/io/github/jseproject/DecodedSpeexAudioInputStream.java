package io.github.jseproject;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.speex.SpeexAudioData;
import org.gagravarr.speex.SpeexFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.xiph.speex.SpeexDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedSpeexAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private SpeexFile speexFile;
    private byte[] pcm;
    private AudioFormat audioFormat;
    private SpeexDecoder decoder = null;

    public DecodedSpeexAudioInputStream(AudioFormat outputFormat, SpeexAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        try {
            speexFile = new SpeexFile(new OggFile(inputStream.getFilteredInputStream()));
            pcm = new byte[2048];
        } catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            throw new IllegalArgumentException("conversion not supported");
        }
        audioFormat = inputStream.getFormat();
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedSpeexAudioInputStream(AudioFormat, AudioInputStream)");
    }

    @Override
    public int read(byte[] abData, int nOffset, int nLength) throws IOException {
        int n = super.read(abData, nOffset, nLength);
        while (n == 0) n = super.read(abData, nOffset, nLength);
        return n;
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        try {
            SpeexAudioData packet = speexFile.getNextAudioPacket();
            if (packet == null) {
                getCircularBuffer().close();
                return;
            }
            if (decoder == null) {
                decoder = new SpeexDecoder();
                decoder.init(speexFile.getInfo().getMode(),
                        (int) audioFormat.getSampleRate(), audioFormat.getChannels(), true);
            }
            byte[] samples = packet.getData();
            decoder.processData(samples, 0, samples.length);
            int decoded = decoder.getProcessedData(pcm, 0);
            getCircularBuffer().write(pcm, 0, decoded);
        } catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        } finally {
            if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        speexFile.close();
        speexFile = null;
        pcm = null;
        audioFormat = null;
        decoder = null;
    }

}

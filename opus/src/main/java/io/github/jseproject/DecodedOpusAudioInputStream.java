package io.github.jseproject;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedOpusAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private OpusFile opusFile;
    private AudioFormat audioFormat;
    private OpusDecoder decoder = null;

    public DecodedOpusAudioInputStream(AudioFormat outputFormat, OpusAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        try {
            opusFile = new OpusFile(new OggFile(inputStream.getFilteredInputStream()));
        } catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            throw new IllegalArgumentException("conversion not supported");
        }
        audioFormat = inputStream.getFormat();
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedOpusAudioInputStream(AudioFormat, AudioInputStream)");
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
            OpusAudioData packet = opusFile.getNextAudioPacket();
            if (packet == null) {
                getCircularBuffer().close();
                return;
            }
            if (decoder == null)
                decoder = new OpusDecoder((int) audioFormat.getSampleRate(), audioFormat.getChannels());
            int packetSamples = packet.getNumberOfSamples();
            byte[] samples = packet.getData();
            short[] pcm = new short[packetSamples * audioFormat.getChannels()];
            int decoded = decoder.decode(samples, 0, samples.length, pcm, 0, packetSamples, false);
            getCircularBuffer().write(shortsToBytes(pcm, 0, decoded * audioFormat.getChannels()));
        } catch (IOException | OpusException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        } finally {
            if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
        }
    }

    private static byte[] shortsToBytes(short[] input, int offset, int length) {
        byte[] bytes = new byte[length * 2];
        for (int c = 0; c < length; c ++) {
            bytes[c * 2] = (byte) (input[c + offset] & 0xFF);
            bytes[c * 2 + 1] = (byte) ((input[c + offset] >> 8) & 0xFF);
        }
        return bytes;
    }

    @Override
    public void close() throws IOException {
        super.close();
        opusFile.close();
        opusFile = null;
        audioFormat = null;
        decoder = null;
    }

}

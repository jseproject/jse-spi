package io.github.jseproject;

import net.sourceforge.lame.Mpg123;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedMp3AudioInputStream extends TAudioInputStream {

    private byte[] singleByte = null;
    private Mpg123 decoder;
    public DecodedMp3AudioInputStream(AudioFormat outputFormat, Mp3AudioInputStream inputStream) {
        super(inputStream, outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedMp3AudioInputStream(AudioFormat, AudioInputStream)");
        decoder = new Mpg123(outputFormat.getSampleSizeInBits(),
                outputFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED),
                format.isBigEndian());
        decoder.InitMP3();
        if (decoder.open(inputStream.getFilteredInputStream()) < 0) {
            decoder.ExitMP3();
            decoder = null;
            if (TDebug.TraceAudioConverter) TDebug.out("DecodedMp3AudioInputStream : Failed to initialize MP3 decoder");
            throw new IllegalArgumentException("conversion not supported");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (decoder != null) {
            decoder.ExitMP3();
            decoder = null;
        }
        singleByte = null;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        if (singleByte == null) singleByte = new byte[1];
        if (read(singleByte) <= 0) return -1; // we have a weird situation if read(byte[]) returns 0!
        else return ((int) singleByte[0]) & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return decoder.read(b, off, len);
    }

}

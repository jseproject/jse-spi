package io.github.jseproject;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class AACAudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 128000 * 32;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public AACAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(InputStream): begin (class: " + getClass().getSimpleName() + ")");
        inputStream.mark(MARK_LIMIT);
        AudioFileFormat	audioFileFormat;
        try {
            audioFileFormat = getAudioFileFormat(inputStream, AudioSystem.NOT_SPECIFIED);
        }
        catch (UnsupportedAudioFileException | IOException e) {
            inputStream.reset();
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(InputStream): end");
        return audioFileFormat;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out(">AACAudioFileReader.getAudioFileFormat(InputStream): begin");
        PushbackInputStream pis = new PushbackInputStream(inputStream, 22);
        byte[] head = new byte[22];
        pis.read(head);
        if (TDebug.TraceAudioFileReader) TDebug.out("InputStream : " + inputStream + " =>" + new String(head));
        // Check for WAV, AU/SND, AIFF, MAC/APE, FLAC, OGG file formats.
        if (head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'A' && head[10] == 'V' && head[11] == 'E') {
            if ((((head[21] << 8) & 0x0000FF00) | (head[20] & 0x00000FF)) == 1)
                throw new UnsupportedAudioFileException("WAV PCM stream found");
            else if (TDebug.TraceAudioFileReader) TDebug.out("WAV stream found");
        }
        else if (head[0] == '.' && head[1] == 's' && head[2] == 'n' && head[3] == 'd')
            throw new UnsupportedAudioFileException("AU/SND stream found");
        else if (head[0] == 'F' && head[1] == 'O' && head[2] == 'R' && head[3] == 'M'
                && head[8] == 'A' && head[9] == 'I' && head[10] == 'F' && head[11] == 'F')
            throw new UnsupportedAudioFileException("AIFF stream found");
        else if (head[0] == 'M' && head[1] == 'A' && head[2] == 'C' && head[3] == ' ')
            throw new UnsupportedAudioFileException("APE stream found");
        else if (head[0] == 'f' && head[1] == 'L' && head[2] == 'a' && head[3] == 'C')
            throw new UnsupportedAudioFileException("FLAC stream found");
        else if (head[0] == 'O' && head[1] == 'g' && head[2] == 'g' && head[3] == 'S')
            throw new UnsupportedAudioFileException("Ogg stream found");
            // Not either of them, so pushback for further reading.
        else pis.unread(head);
        AudioFileFormat format;
        try {
            ADTSDemultiplexer demultiplexer = new ADTSDemultiplexer(pis);
            format = AACAudioFileFormat.of(demultiplexer);
        }
        catch (AACException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("AACAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream): begin (class: " + getClass().getSimpleName() + ")");
        AudioInputStream audioInputStream;
        inputStream.mark(MARK_LIMIT);
        try {
            audioInputStream = getAudioInputStream(inputStream, AudioSystem.NOT_SPECIFIED);
        }
        catch (UnsupportedAudioFileException | IOException e) {
            inputStream.reset();
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("AACAudioFileReader.getAudioInputStream(InputStream): begin");
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        PushbackInputStream pis = new PushbackInputStream(inputStream, 22);
        byte[] head = new byte[22];
        pis.read(head);
        if (TDebug.TraceAudioFileReader) TDebug.out("InputStream : " + inputStream + " =>" + new String(head));
        // Check for WAV, AU/SND, AIFF, MAC/APE, FLAC, OGG file formats.
        if (head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'A' && head[10] == 'V' && head[11] == 'E') {
            if ((((head[21] << 8) & 0x0000FF00) | (head[20] & 0x00000FF)) == 1)
                throw new UnsupportedAudioFileException("WAV PCM stream found");
            else if (TDebug.TraceAudioFileReader) TDebug.out("WAV stream found");
        }
        else if (head[0] == '.' && head[1] == 's' && head[2] == 'n' && head[3] == 'd')
            throw new UnsupportedAudioFileException("AU/SND stream found");
        else if (head[0] == 'F' && head[1] == 'O' && head[2] == 'R' && head[3] == 'M'
                && head[8] == 'A' && head[9] == 'I' && head[10] == 'F' && head[11] == 'F')
            throw new UnsupportedAudioFileException("AIFF stream found");
        else if (head[0] == 'M' && head[1] == 'A' && head[2] == 'C' && head[3] == ' ')
            throw new UnsupportedAudioFileException("APE stream found");
        else if (head[0] == 'f' && head[1] == 'L' && head[2] == 'a' && head[3] == 'C')
            throw new UnsupportedAudioFileException("FLAC stream found");
        else if (head[0] == 'O' && head[1] == 'g' && head[2] == 'g' && head[3] == 'S')
            throw new UnsupportedAudioFileException("Ogg stream found");
            // Not either of them, so pushback for further reading.
        else pis.unread(head);
        AudioFormat audioFormat;
        ADTSDemultiplexer demultiplexer;
        Decoder decoder;
        SampleBuffer sampleBuffer;
        try {
            demultiplexer = new ADTSDemultiplexer(pis);
            decoder = new Decoder(demultiplexer.getDecoderSpecificInfo());
            sampleBuffer = new SampleBuffer();
            decoder.decodeFrame(demultiplexer.readNextFrame(), sampleBuffer);
            audioFormat = AACAudioFormat.of(decoder.getConfig(), sampleBuffer);
        }
        catch (AACException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        catch (IOException e) {
            inputStream.reset();
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("AACAudioFileReader.getAudioInputStream(InputStream): end");
        return new AACAudioInputStream(inputStream, audioFormat, demultiplexer, decoder, sampleBuffer, AudioSystem.NOT_SPECIFIED);
    }

}
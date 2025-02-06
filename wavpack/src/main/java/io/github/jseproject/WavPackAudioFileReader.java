package io.github.jseproject;

import com.beatofthedrum.wv.WavPackContext;
import com.beatofthedrum.wv.WavPackUtils;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class WavPackAudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 1048576 + 32;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public WavPackAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(File): begin");
        AudioFileFormat format;
        try (DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(file.toPath(), StandardOpenOption.READ))) {
            WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
            if (context.error) throw new UnsupportedAudioFileException(context.error_message);
            format = WavPackAudioFileFormat.of(context);
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(File): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (DataInputStream dataInputStream = new DataInputStream(url.openStream())) {
            WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
            if (context.error) throw new UnsupportedAudioFileException(context.error_message);
            format = WavPackAudioFileFormat.of(context);
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(InputStream): begin");
        AudioFileFormat format;
        inputStream.mark(MARK_LIMIT);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
        if (context.error) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(context.error_message);
        }
        format = WavPackAudioFileFormat.of(context);
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(File): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
        if (context.error) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(context.error_message);
        }
        AudioFormat audioFormat = WavPackAudioFormat.of(context);
        audioInputStream = new WavPackAudioInputStream(audioFormat, inputStream, context);
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(File): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(URL): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = url.openStream();
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
        if (context.error) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(context.error_message);
        }
        AudioFormat audioFormat = WavPackAudioFormat.of(context);
        audioInputStream = new WavPackAudioInputStream(audioFormat, inputStream, context);
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(URL): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(InputStream): begin");
        AudioInputStream audioInputStream;
        inputStream.mark(MARK_LIMIT);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        WavPackContext context = WavPackUtils.OpenFileInput(dataInputStream);
        if (context.error) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(context.error_message);
        }
        AudioFormat audioFormat = WavPackAudioFormat.of(context);
        audioInputStream = new WavPackAudioInputStream(audioFormat, inputStream, context);
        if (TDebug.TraceAudioFileReader) TDebug.out("WavPackAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(inputStream);
    }

}

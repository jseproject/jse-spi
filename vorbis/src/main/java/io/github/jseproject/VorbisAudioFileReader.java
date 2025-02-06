package io.github.jseproject;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.vorbis.VorbisFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class VorbisAudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 64000 * 2;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public VorbisAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(File): begin");
        AudioFileFormat format;
        try (VorbisFile vorbisFile = new VorbisFile(file)) {
            format = VorbisAudioFileFormat.of(vorbisFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(File): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (VorbisFile vorbisFile = new VorbisFile(new OggFile(url.openStream()))) {
            format = VorbisAudioFileFormat.of(vorbisFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(InputStream): begin");
        AudioFileFormat format;
        inputStream.mark(MARK_LIMIT);
        try {
            OggFile oggFile = new OggFile(inputStream);
            VorbisFile vorbisFile = new VorbisFile(oggFile);
            format = VorbisAudioFileFormat.of(vorbisFile);
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(File): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        try {
            OggFile oggFile = new OggFile(inputStream);
            VorbisFile vorbisFile = new VorbisFile(oggFile);
            AudioFormat audioFormat = VorbisAudioFormat.of(vorbisFile.getInfo());
            inputStream.reset();
            audioInputStream = new VorbisAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(File): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(URL): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = url.openStream();
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        try {
            OggFile oggFile = new OggFile(inputStream);
            VorbisFile vorbisFile = new VorbisFile(oggFile);
            AudioFormat audioFormat = VorbisAudioFormat.of(vorbisFile.getInfo());
            inputStream.reset();
            audioInputStream = new VorbisAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(URL): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(InputStream): begin");
        AudioInputStream audioInputStream;
        inputStream.mark(MARK_LIMIT);
        try {
            OggFile oggFile = new OggFile(inputStream);
            VorbisFile vorbisFile = new VorbisFile(oggFile);
            inputStream.reset();
            audioInputStream = new VorbisAudioInputStream(inputStream, VorbisAudioFormat.of(vorbisFile.getInfo()), AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("VorbisAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(inputStream);
    }

}

/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package io.github.jseproject;

import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.util.APEException;
import davaguine.jmac.util.InputStreamIoFile;
import davaguine.jmac.util.IoFile;
import davaguine.jmac.util.RandomAccessIoFile;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class APEAudioFileReader extends TAudioFileReader {

    private final static int INITIAL_READ_LENGTH = 16384;
    private final static int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public APEAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(File): begin");
        AudioFileFormat format;
        try (IoFile io = new RandomAccessIoFile(file, "r")) {
            format = APEAudioFileFormat.of(IAPEDecompress.CreateIAPEDecompress(io));
        }
        catch (APEException | EOFException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(File): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (IoFile io = new InputStreamIoFile(url)) {
            format = APEAudioFileFormat.of(IAPEDecompress.CreateIAPEDecompress(io));
        }
        catch (APEException | EOFException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(InputStream): begin");
        AudioFileFormat format;
        inputStream.mark(MARK_LIMIT);
        IoFile io = new InputStreamIoFile(inputStream);
        try {
            format = APEAudioFileFormat.of(IAPEDecompress.CreateIAPEDecompress(io));
        }
        catch (APEException | EOFException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        catch (IOException e) {
            inputStream.reset();
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioInputStream(File): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        try {
            IoFile io = new InputStreamIoFile(inputStream);
            IAPEDecompress decoder = IAPEDecompress.CreateIAPEDecompress(io);
            AudioFormat audioFormat = APEAudioFormat.of(decoder);
            audioInputStream = new APEAudioInputStream(inputStream, audioFormat, io, decoder);
        }
        catch (APEException | EOFException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        catch (IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("APEAudioFileReader.getAudioInputStream(File): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(URL): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = url.openStream();
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(MARK_LIMIT);
        try {
            IoFile io = new InputStreamIoFile(inputStream);
            IAPEDecompress decoder = IAPEDecompress.CreateIAPEDecompress(io);
            AudioFormat audioFormat = APEAudioFormat.of(decoder);
            audioInputStream = new APEAudioInputStream(inputStream, audioFormat, io, decoder);
        }
        catch (APEException | EOFException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        catch (IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(URL): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(InputStream): begin");
        AudioInputStream audioInputStream;
        inputStream.mark(MARK_LIMIT);
        try {
            IoFile io = new InputStreamIoFile(inputStream);
            IAPEDecompress decoder = IAPEDecompress.CreateIAPEDecompress(io);
            AudioFormat audioFormat = APEAudioFormat.of(decoder);
            audioInputStream = new APEAudioInputStream(inputStream, audioFormat, io, decoder);
        }
        catch (APEException | EOFException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        catch (IOException e) {
            inputStream.reset();
            throw e;
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(inputStream);
    }

}

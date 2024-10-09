/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2004 Wimba S.A.
 * Copyright (c) 2002-2004 Xiph.org Foundation
 * Copyright (c) 2002-2004 Jean-Marc Valin
 * Copyright (c) 1993, 2002 David Rowe
 * Copyright (c) 1992-1994	Jutta Degener, Carsten Bormann
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.speex.SpeexFile;
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

public class SpeexAudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 64000 * 2;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public SpeexAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (SpeexFile speexFile = new SpeexFile(file)) {
            format = SpeexAudioFileFormat.of(speexFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (SpeexFile speexFile = new SpeexFile(new OggFile(url.openStream()))) {
            format = SpeexAudioFileFormat.of(speexFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(InputStream): begin");
        AudioFileFormat format;
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            OggFile oggFile = new OggFile(inputStream);
            SpeexFile speexFile = new SpeexFile(oggFile);
            format = SpeexAudioFileFormat.of(speexFile);
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(File): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            OggFile oggFile = new OggFile(inputStream);
            SpeexFile speexFile = new SpeexFile(oggFile);
            AudioFormat audioFormat = SpeexAudioFormat.of(speexFile.getInfo());
            inputStream.reset();
            audioInputStream = new SpeexAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(File): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(URL): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = url.openStream();
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            OggFile oggFile = new OggFile(inputStream);
            SpeexFile speexFile = new SpeexFile(oggFile);
            AudioFormat audioFormat = SpeexAudioFormat.of(speexFile.getInfo());
            inputStream.reset();
            audioInputStream = new SpeexAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(URL): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(InputStream): begin");
        AudioInputStream audioInputStream;
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            OggFile oggFile = new OggFile(inputStream);
            SpeexFile speexFile = new SpeexFile(oggFile);
            inputStream.reset();
            audioInputStream = new SpeexAudioInputStream(inputStream, SpeexAudioFormat.of(speexFile.getInfo()), AudioSystem.NOT_SPECIFIED);
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("SpeexAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(inputStream);
    }

}

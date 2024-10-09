/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import javasound.enhancement.sampled.spi.AudioResourceReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

public class AACAudioResourceReader extends AudioResourceReader {

    private static final AACAudioFileReader READER = new AACAudioFileReader();

    @Override
    public AudioFileFormat getAudioFileFormat(ClassLoader resourceLoader, String name) throws UnsupportedAudioFileException, IOException {
        try (InputStream stream = resourceLoader.getResourceAsStream(name)) {
            if (stream == null) throw new IOException("could not load resource \"" + name + "\" with ClassLoader \"" + resourceLoader + "\"");
            else return READER.getAudioFileFormat(stream);
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(ClassLoader resourceLoader, String name) throws UnsupportedAudioFileException, IOException {
        InputStream stream = resourceLoader.getResourceAsStream(name);
        if (stream == null) throw new IOException("could not load resource \"" + name + "\" with ClassLoader \"" + resourceLoader + "\"");
        else return READER.getAudioInputStream(stream);
    }

}

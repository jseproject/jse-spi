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

import javasound.enhancement.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SpeexFormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] ENCODINGS = new AudioFormat.Encoding[] { SpeexEncoding.SPEEX };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        return SpeexEncoding.SPEEX.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        return SpeexEncoding.SPEEX.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioFileFormat.Type fileType) {
        if (isWriterSupportedFileType(fileType)) return ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding, AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return isWriterSupportedEncoding(encoding);
        else return false;
    }

    @Override
    public AudioFormat.Encoding getEncodingByFormatName(String name) {
        if (SpeexEncoding.SPEEX.toString().equalsIgnoreCase(name)) return SpeexEncoding.SPEEX;
        else return null;
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { SpeexFileFormatType.SPEEX };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        return SpeexFileFormatType.SPEEX.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        return SpeexFileFormatType.SPEEX.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes(AudioInputStream stream) {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes(stream)) {
            if (isWriterSupportedFileType(type)) return TYPES.clone();
        }
        return EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType, AudioInputStream stream) {
        return isWriterSupportedFileType(fileType) && AudioSystem.isFileTypeSupported(fileType, stream);
    }

    @Override
    public AudioFileFormat.Type getFileTypeByFormatName(String name) {
        return SpeexFileFormatType.SPEEX.toString().equalsIgnoreCase(name) ? SpeexFileFormatType.SPEEX : null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        return "spx".equalsIgnoreCase(suffix) ? SpeexFileFormatType.SPEEX : null;
    }

}

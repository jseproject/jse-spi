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

import javasound.enhancement.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class AACFormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] ENCODINGS = new AudioFormat.Encoding[] { AACEncoding.AAC };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        return AACEncoding.AAC.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        return false;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioFileFormat.Type fileType) {
        return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioInputStream stream) {
        return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding, AudioInputStream stream) {
        return false;
    }

    @Override
    public AudioFormat.Encoding getEncodingByFormatName(String name) {
        if (AACEncoding.AAC.toString().equalsIgnoreCase(name)) return AACEncoding.AAC;
        else return null;
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { AACFileFormatType.AAC };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        return AACFileFormatType.AAC.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        return false;
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes(AudioInputStream stream) {
        return EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType, AudioInputStream stream) {
        return false;
    }

    @Override
    public AudioFileFormat.Type getFileTypeByFormatName(String name) {
        return AACFileFormatType.AAC.toString().equalsIgnoreCase(name) ? AACFileFormatType.AAC : null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        return "aac".equalsIgnoreCase(suffix) ? AACFileFormatType.AAC : null;
    }

}

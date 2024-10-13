/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.tianscar.media.sound;

import javasound.enhancement.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Mp3FormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] READER_ENCODINGS = new AudioFormat.Encoding[] {
            Mp3Encoding.MPEG1_L1, Mp3Encoding.MPEG2_L1, Mp3Encoding.MPEG2DOT5_L1,
            Mp3Encoding.MPEG1_L2, Mp3Encoding.MPEG2_L2, Mp3Encoding.MPEG2DOT5_L2,
            Mp3Encoding.MPEG1_L3, Mp3Encoding.MPEG2_L3, Mp3Encoding.MPEG2DOT5_L3
    };
    private static final AudioFormat.Encoding[] WRITER_ENCODINGS = new AudioFormat.Encoding[] {
           Mp3Encoding.MPEG1_L3, Mp3Encoding.MPEG2_L3, Mp3Encoding.MPEG2DOT5_L3
    };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return READER_ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        for (AudioFormat.Encoding e : READER_ENCODINGS) {
            if (e.equals(encoding)) return true;
        }
        return false;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return WRITER_ENCODINGS.clone();
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        for (AudioFormat.Encoding e : WRITER_ENCODINGS) {
            if (e.equals(encoding)) return true;
        }
        return false;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioFileFormat.Type fileType) {
        if (isWriterSupportedFileType(fileType)) return WRITER_ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return WRITER_ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding, AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return isWriterSupportedEncoding(encoding);
        else return false;
    }

    @Override
    public AudioFormat.Encoding getEncodingByFormatName(String name) {
        for (AudioFormat.Encoding encoding : READER_ENCODINGS) {
            if (encoding.toString().equalsIgnoreCase(name)) return encoding;
        }
        return null;
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { Mp3FileFormatType.MP3 };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        return Mp3FileFormatType.MP3.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        return Mp3FileFormatType.MP3.equals(fileType);
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
        return Mp3FileFormatType.MP3.toString().equalsIgnoreCase(name) ? Mp3FileFormatType.MP3 : null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        return "mp3".equalsIgnoreCase(suffix) ? Mp3FileFormatType.MP3 : null;
    }

}

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

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class Mp3AudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 128000 * 32;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public Mp3AudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream, lFileLengthInBytes, inputStream instanceof FileInputStream);
    }

    public AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes, boolean readID3v1) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out(">Mp3AudioFileReader.getAudioFileFormat(InputStream inputStream, long mediaLength): begin");
        int nMediaLength = lFileLengthInBytes > Integer.MAX_VALUE ? AudioSystem.NOT_SPECIFIED : (int) lFileLengthInBytes;
        int nAvailable = inputStream.available();
        PushbackInputStream pis = new PushbackInputStream(inputStream, MARK_LIMIT);
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
        Header header;
        byte[] id3v2;
        byte[] id3v1;
        try {
            Bitstream bitstream = new Bitstream(pis);
            int streamPos = bitstream.header_pos();
            if (streamPos > 0 && nMediaLength != AudioSystem.NOT_SPECIFIED && streamPos < nMediaLength)
                nMediaLength -= streamPos;
            header = bitstream.readFrame();
            if (header == null) throw new IOException("header is null (end of mpeg stream)");
            id3v2 = bitstream.getRawID3v2();
            if (readID3v1 && lFileLengthInBytes != AudioSystem.NOT_SPECIFIED && nAvailable == lFileLengthInBytes) {
                id3v1 = new byte[128];
                inputStream.skip(inputStream.available() - id3v1.length);
                inputStream.read(id3v1, 0, id3v1.length);
            }
            else id3v1 = null;
            if (TDebug.TraceAudioFileReader) TDebug.out(header.toString());
        }
        catch (Exception e) {
            throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: " + e.getMessage());
        }
        // Deeper checks
        int nHeader = header.getSyncHeader();
        int cVersion = (nHeader >> 19) & 0x3;
        if (cVersion == 1) throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: wrong version");
        int cSFIndex = (nHeader >> 10) & 0x3;
        if (cSFIndex == 3) throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: wrong sampling rate");
        AudioFileFormat format = Mp3AudioFileFormat.of(header, nMediaLength, id3v2, id3v1);
        if (TDebug.TraceAudioFileReader) TDebug.out("Mp3AudioFileReader.getAudioFileFormat(InputStream inputStream, long mediaLength): end");
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
        if (TDebug.TraceAudioFileReader) TDebug.out("Mp3AudioFileReader.getAudioInputStream(InputStream inputStream, long mediaLength): begin");
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(INITIAL_READ_LENGTH);
        PushbackInputStream pis = new PushbackInputStream(inputStream, MARK_LIMIT);
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
        Header header;
        try {
            Bitstream bitstream = new Bitstream(pis);
            header = bitstream.readFrame();
            if (header == null) throw new IOException("header is null (end of mpeg stream)");
            if (TDebug.TraceAudioFileReader) TDebug.out(header.toString());
        }
        catch (Exception e) {
            throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: " + e.getMessage());
        }
        // Deeper checks
        int nHeader = header.getSyncHeader();
        int cVersion = (nHeader >> 19) & 0x3;
        if (cVersion == 1) throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: wrong version");
        int cSFIndex = (nHeader >> 10) & 0x3;
        if (cSFIndex == 3) throw new UnsupportedAudioFileException("Not a MPEG1/2/2.5 stream: wrong sampling rate");
        AudioFormat audioFormat = Mp3AudioFormat.of(header);
        inputStream.reset();
        if (TDebug.TraceAudioFileReader) TDebug.out("Mp3AudioFileReader.getAudioInputStream(InputStream inputStream, long mediaLength): end");
        return new Mp3AudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
    }

}
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

import net.sourceforge.lame.ID3Tag;
import net.sourceforge.lame.LAME;
import net.sourceforge.lame.LAME_GlobalFlags;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Mp3AudioFileWriter extends TAudioFileWriter {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false),
            new AudioFormat(8000.0f, 16, 1, true, true),
            new AudioFormat(8000.0f, 16, 2, true, false),
            new AudioFormat(8000.0f, 16, 2, true, true),
            new AudioFormat(8000.0f, 24, 1, false, false),
            new AudioFormat(8000.0f, 24, 1, false, true),
            new AudioFormat(8000.0f, 24, 2, false, false),
            new AudioFormat(8000.0f, 24, 2, false, true),
            new AudioFormat(8000.0f, 8, 1, false, false),
            new AudioFormat(8000.0f, 8, 2, false, false),
            new AudioFormat(11025.0f, 16, 1, true, false),
            new AudioFormat(11025.0f, 16, 1, true, true),
            new AudioFormat(11025.0f, 16, 2, true, false),
            new AudioFormat(11025.0f, 16, 2, true, true),
            new AudioFormat(11025.0f, 24, 1, false, false),
            new AudioFormat(11025.0f, 24, 1, false, true),
            new AudioFormat(11025.0f, 24, 2, false, false),
            new AudioFormat(11025.0f, 24, 2, false, true),
            new AudioFormat(11025.0f, 8, 1, false, false),
            new AudioFormat(11025.0f, 8, 2, false, false),
            new AudioFormat(12000.0f, 16, 1, true, false),
            new AudioFormat(12000.0f, 16, 1, true, true),
            new AudioFormat(12000.0f, 16, 2, true, false),
            new AudioFormat(12000.0f, 16, 2, true, true),
            new AudioFormat(12000.0f, 24, 1, false, false),
            new AudioFormat(12000.0f, 24, 1, false, true),
            new AudioFormat(12000.0f, 24, 2, false, false),
            new AudioFormat(12000.0f, 24, 2, false, true),
            new AudioFormat(12000.0f, 8, 1, false, false),
            new AudioFormat(12000.0f, 8, 2, false, false),
            new AudioFormat(16000.0f, 16, 1, true, false),
            new AudioFormat(16000.0f, 16, 1, true, true),
            new AudioFormat(16000.0f, 16, 2, true, false),
            new AudioFormat(16000.0f, 16, 2, true, true),
            new AudioFormat(16000.0f, 24, 1, false, false),
            new AudioFormat(16000.0f, 24, 1, false, true),
            new AudioFormat(16000.0f, 24, 2, false, false),
            new AudioFormat(16000.0f, 24, 2, false, true),
            new AudioFormat(16000.0f, 8, 1, false, false),
            new AudioFormat(16000.0f, 8, 2, false, false),
            new AudioFormat(22050.0f, 16, 1, true, false),
            new AudioFormat(22050.0f, 16, 1, true, true),
            new AudioFormat(22050.0f, 16, 2, true, false),
            new AudioFormat(22050.0f, 16, 2, true, true),
            new AudioFormat(22050.0f, 24, 1, false, false),
            new AudioFormat(22050.0f, 24, 1, false, true),
            new AudioFormat(22050.0f, 24, 2, false, false),
            new AudioFormat(22050.0f, 24, 2, false, true),
            new AudioFormat(22050.0f, 8, 1, false, false),
            new AudioFormat(22050.0f, 8, 2, false, false),
            new AudioFormat(24000.0f, 16, 1, true, false),
            new AudioFormat(24000.0f, 16, 1, true, true),
            new AudioFormat(24000.0f, 16, 2, true, false),
            new AudioFormat(24000.0f, 16, 2, true, true),
            new AudioFormat(24000.0f, 24, 1, false, false),
            new AudioFormat(24000.0f, 24, 1, false, true),
            new AudioFormat(24000.0f, 24, 2, false, false),
            new AudioFormat(24000.0f, 24, 2, false, true),
            new AudioFormat(24000.0f, 8, 1, false, false),
            new AudioFormat(24000.0f, 8, 2, false, false),
            new AudioFormat(32000.0f, 16, 1, true, false),
            new AudioFormat(32000.0f, 16, 1, true, true),
            new AudioFormat(32000.0f, 16, 2, true, false),
            new AudioFormat(32000.0f, 16, 2, true, true),
            new AudioFormat(32000.0f, 24, 1, false, false),
            new AudioFormat(32000.0f, 24, 1, false, true),
            new AudioFormat(32000.0f, 24, 2, false, false),
            new AudioFormat(32000.0f, 24, 2, false, true),
            new AudioFormat(32000.0f, 8, 1, false, false),
            new AudioFormat(32000.0f, 8, 2, false, false),
            new AudioFormat(44100.0f, 16, 1, true, false),
            new AudioFormat(44100.0f, 16, 1, true, true),
            new AudioFormat(44100.0f, 16, 2, true, false),
            new AudioFormat(44100.0f, 16, 2, true, true),
            new AudioFormat(44100.0f, 24, 1, false, false),
            new AudioFormat(44100.0f, 24, 1, false, true),
            new AudioFormat(44100.0f, 24, 2, false, false),
            new AudioFormat(44100.0f, 24, 2, false, true),
            new AudioFormat(44100.0f, 8, 1, false, false),
            new AudioFormat(44100.0f, 8, 2, false, false),
            new AudioFormat(48000.0f, 16, 1, true, false),
            new AudioFormat(48000.0f, 16, 1, true, true),
            new AudioFormat(48000.0f, 16, 2, true, false),
            new AudioFormat(48000.0f, 16, 2, true, true),
            new AudioFormat(48000.0f, 24, 1, false, false),
            new AudioFormat(48000.0f, 24, 1, false, true),
            new AudioFormat(48000.0f, 24, 2, false, false),
            new AudioFormat(48000.0f, 24, 2, false, true),
            new AudioFormat(48000.0f, 8, 1, false, false),
            new AudioFormat(48000.0f, 8, 2, false, false),
    };

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { Mp3FileFormatType.MP3 };

    public Mp3AudioFileWriter() {
        super(Arrays.asList(TYPES), Arrays.asList(SUPPORTED_FORMATS));
    }

    @Override
    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, File file) throws IOException {
        try (OutputStream out = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            return write(audioInputStream, fileType, out);
        }
    }

    @Override
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat, long lLengthInBytes, AudioFileFormat.Type fileType, TDataOutputStream dataOutputStream) throws IOException {
        return new Mp3AudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((Mp3FileFormatType) fileType).quality);
    }

    // 2024-10-12: Workaround for TAudioFileWriter
    private static final int BUFFER_SIZE = 1152;
    @Override
    protected int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion) throws IOException {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out(">TAudioFileWriter.writeImpl(): called");
            TDebug.out("class: " + getClass().getName());
        }
        int	nTotalWritten = 0;
        AudioFormat	outputFormat = audioOutputStream.getFormat();

        // TODO: handle case when frame size is unknown ?
        int	nBytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();

        // 2024-09-26: Edit for MP3 Encoder
        Mp3AudioOutputParams params = (Mp3AudioOutputParams) audioOutputStream;
        OutputStream out = params.getOutputStream();
        float quality = params.getQuality();

        int channels = outputFormat.getChannels();
        int sampleRate = (int) outputFormat.getSampleRate();

        byte[] abBuffer = new byte[BUFFER_SIZE * 2 * 2]; // 2 channels, 2 bytes per sample
        short[] pcm = new short[2 * BUFFER_SIZE]; // 2 channels

        LAME_GlobalFlags gf = LAME.lame_init();
        if (gf == null) throw new IOException("Failed to initialize MP3 encoder");
        ID3Tag.id3tag_init(gf);
        // set encoding parameters
        gf.lame_set_num_channels(channels);
        gf.lame_set_out_samplerate(sampleRate);
        if (gf.lame_get_VBR() == LAME.vbr_off) gf.lame_set_VBR(LAME.vbr_default);
        gf.lame_set_VBR_quality(quality);
        // end set encoding parameters
        gf.lame_set_write_id3tag_automatic(false);
        int ret = LAME.lame_init_params(gf);
        if (ret < 0) throw new IOException("Failed to initialize MP3 encoder");

        while (true) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            int nBytesRead = audioInputStream.read(abBuffer);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            if (nBytesRead == -1) break;
            ByteBuffer.wrap(abBuffer, 0, nBytesRead).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().get(pcm, 0, nBytesRead >> 1);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
            ret = LAME.lame_encode_buffer_interleaved(gf, pcm, (nBytesRead / channels) >> 1,
                    abBuffer, 0, abBuffer.length);
            // was our output buffer big enough?
            if (ret < 0) throw new IOException("Failed to encode MP3");
            out.write(abBuffer, 0, ret);
            nTotalWritten += ret;
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
        }

        if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
        ret = LAME.lame_encode_flush(gf, abBuffer, abBuffer.length);
        if (ret < 0) throw new IOException("Failed to encode MP3");
        out.write(abBuffer, 0, ret);
        nTotalWritten += ret;
        if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");

        ret = writeID3v1(gf, out);
        nTotalWritten += ret;

        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        LAME.lame_close( gf );
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    private static int writeID3v1(LAME_GlobalFlags gf, OutputStream out) throws IOException {
        byte[] buf = new byte[128];
        int ret = ID3Tag.lame_get_id3v1_tag(gf, buf, buf.length);
        if (ret <= 0) return 0;
        if (ret > buf.length) return 0;
        out.write(buf, 0, ret);
        return ret;
    }

    private static class Mp3AudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final OutputStream out;
        private final float quality;
        public Mp3AudioOutputParams(AudioFormat format, long length, TNonSeekableDataOutputStream out, float quality) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.quality = quality;
        }
        public OutputStream getOutputStream() {
            return out;
        }
        public float getQuality() {
            return quality;
        }
        @Override
        public AudioFormat getFormat() {
            return format;
        }
        @Override
        public long getLength() {
            return length;
        }
        @Override
        public int write(byte[] abData, int nOffset, int nLength) throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public void close() {}
    }

}

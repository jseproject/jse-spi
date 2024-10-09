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

import org.gagravarr.speex.SpeexAudioData;
import org.gagravarr.speex.SpeexFile;
import org.gagravarr.speex.SpeexInfo;
import org.gagravarr.speex.SpeexTags;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.xiph.speex.SpeexEncoder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class SpeexAudioFileWriter extends TAudioFileWriter {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false), // 0
            new AudioFormat(8000.0f, 16, 1, true, true),  // 1
            new AudioFormat(8000.0f, 16, 2, true, false), // 2
            new AudioFormat(8000.0f, 16, 2, true, true),  // 3
            new AudioFormat(16000.0f, 16, 1, true, false), // 12
            new AudioFormat(16000.0f, 16, 1, true, true),  // 13
            new AudioFormat(16000.0f, 16, 2, true, false), // 14
            new AudioFormat(16000.0f, 16, 2, true, true),  // 15
            new AudioFormat(32000.0f, 16, 1, true, false), // 24
            new AudioFormat(32000.0f, 16, 1, true, true),  // 25
            new AudioFormat(32000.0f, 16, 2, true, false), // 26
            new AudioFormat(32000.0f, 16, 2, true, true),  // 27
    };

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { SpeexFileFormatType.SPEEX };

    public SpeexAudioFileWriter() {
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
        return new SpeexAudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((SpeexFileFormatType) fileType).quality);
    }

    // 2024-09-24: Workaround for TAudioFileWriter
    /** The size of the buffer (UWB stereo requires at least 2560b). */
    private static final int BUFFER_SIZE = 2560;
    /** The number of Speex frames that will be put in each Ogg packet. */
    private static final int FRAMES_PER_PACKET = 1;
    @Override
    protected int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion) throws IOException {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out(">TAudioFileWriter.writeImpl(): called");
            TDebug.out("class: " + getClass().getName());
        }
        int	nTotalWritten = 0;
        AudioFormat	outputFormat = audioOutputStream.getFormat();

        // 2024-09-24: Edit for Speex Encoder
        SpeexAudioOutputParams params = (SpeexAudioOutputParams) audioOutputStream;
        int quality = params.getQuality();
        int sampleRate = (int) outputFormat.getSampleRate();
        int mode = sampleRate < 12000 ? 0 : (sampleRate < 24000 ? 1 : 2);

        SpeexEncoder encoder = new SpeexEncoder();
        encoder.init(mode, quality, sampleRate, outputFormat.getChannels());
        encoder.getEncoder().setVbr(false);
        SpeexInfo speexInfo = new SpeexInfo();
        speexInfo.setVersionString("1.0.0");
        speexInfo.setVersionId(1);
        speexInfo.setRate(sampleRate);
        speexInfo.setMode(mode);
        speexInfo.setModeBitstreamVersion(4);
        speexInfo.setNumChannels(outputFormat.getChannels());
        speexInfo.setBitrate(sampleRate * outputFormat.getChannels());
        speexInfo.setFrameSize(160 << mode);
        speexInfo.setVbr(0 /* false */);
        speexInfo.setFramesPerPacket(FRAMES_PER_PACKET);
        SpeexTags speexTags = new SpeexTags();
        speexTags.setVendor(SpeexEncoder.VERSION);
        SpeexFile speexFile = new SpeexFile(
                new FilterOutputStream(params.getOutputStream()) {@Override public void close() {}},
                speexInfo, speexTags);

        int	nBytesPerSample = 2 * encoder.getFrameSize();
        int nBufferSize = 2 * outputFormat.getChannels() * encoder.getFrameSize();
        byte[] abBuffer = new byte[nBufferSize];

        byte[] buffer = new byte[BUFFER_SIZE];
        long granulepos = 0;
        while (true) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            int	nBytesRead = audioInputStream.read(abBuffer);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            if (nBytesRead < 1) break;
            if (bNeedsConversion) TConversionTool.changeOrderOrSign(abBuffer, 0, nBytesRead, nBytesPerSample);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
            if (nBytesRead < nBufferSize) Arrays.fill(abBuffer, nBytesRead, nBufferSize, (byte) 0);
            encoder.processData(abBuffer, 0, nBufferSize);
            int nWritten = encoder.getProcessedData(buffer, 0);
            byte[] page = new byte[nWritten];
            System.arraycopy(buffer, 0, page, 0, nWritten);
            SpeexAudioData speexAudioData = new SpeexAudioData(page);
            // The ogg library should be handling granule positions automatically but for some reason it doesn't.
            // File a bug on vorbis-java or find a new ogg library...
            granulepos += FRAMES_PER_PACKET * nBufferSize / encoder.getChannels() / 2;
            speexAudioData.setGranulePosition(granulepos);
            speexFile.writeAudioData(speexAudioData);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
            nTotalWritten += nWritten;
        }
        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        speexFile.close();
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    private static class SpeexAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final OutputStream out;
        private final int quality;
        public SpeexAudioOutputParams(AudioFormat format, long length, OutputStream out, int quality) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.quality = quality;
        }
        public OutputStream getOutputStream() {
            return out;
        }
        public int getQuality() {
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

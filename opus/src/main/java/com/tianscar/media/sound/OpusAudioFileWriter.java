/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2016 Logan Stromberg
 * Copyright (c) 2007-2008 CSIRO
 * Copyright (c) 2007-2011 Xiph.Org Foundation
 * Copyright (c) 2006-2011 Skype Limited
 * Copyright (c) 2003-2004 Mark Borgerding
 * Copyright (c) 2001-2011 Microsoft Corporation,
 *                         Jean-Marc Valin, Gregory Maxwell,
 *                         Koen Vos, Timothy B. Terriberry
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
 * - Neither the name of Internet Society, IETF or IETF Trust, nor the
 * names of specific contributors, may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.concentus.CodecHelpers;
import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import org.concentus.OpusSignal;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusTags;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;

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

public class OpusAudioFileWriter extends TAudioFileWriter {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false), // 0
            new AudioFormat(8000.0f, 16, 1, true, true),  // 1
            new AudioFormat(8000.0f, 16, 2, true, false), // 2
            new AudioFormat(8000.0f, 16, 2, true, true),  // 3
            /*	24 and 32 bit not yet possible
                            new AudioFormat(8000.0f, 24, 1, true, false),
                            new AudioFormat(8000.0f, 24, 1, true, true),
                            new AudioFormat(8000.0f, 24, 2, true, false),
                            new AudioFormat(8000.0f, 24, 2, true, true),
                            new AudioFormat(8000.0f, 32, 1, true, false),
                            new AudioFormat(8000.0f, 32, 1, true, true),
                            new AudioFormat(8000.0f, 32, 2, true, false),
                            new AudioFormat(8000.0f, 32, 2, true, true),
             */
            new AudioFormat(12000.0f, 16, 1, true, false), // 8
            new AudioFormat(12000.0f, 16, 1, true, true),  // 9
            new AudioFormat(12000.0f, 16, 2, true, false), // 10
            new AudioFormat(12000.0f, 16, 2, true, true),  // 11
            /*	24 and 32 bit not yet possible
                            new AudioFormat(12000.0f, 24, 1, true, false),
                            new AudioFormat(12000.0f, 24, 1, true, true),
                            new AudioFormat(12000.0f, 24, 2, true, false),
                            new AudioFormat(12000.0f, 24, 2, true, true),
                            new AudioFormat(12000.0f, 32, 1, true, false),
                            new AudioFormat(12000.0f, 32, 1, true, true),
                            new AudioFormat(12000.0f, 32, 2, true, false),
                            new AudioFormat(12000.0f, 32, 2, true, true),
             */
            new AudioFormat(16000.0f, 16, 1, true, false), // 12
            new AudioFormat(16000.0f, 16, 1, true, true),  // 13
            new AudioFormat(16000.0f, 16, 2, true, false), // 14
            new AudioFormat(16000.0f, 16, 2, true, true),  // 15
            /*	24 and 32 bit not yet possible
                            new AudioFormat(16000.0f, 24, 1, true, false),
                            new AudioFormat(16000.0f, 24, 1, true, true),
                            new AudioFormat(16000.0f, 24, 2, true, false),
                            new AudioFormat(16000.0f, 24, 2, true, true),
                            new AudioFormat(16000.0f, 32, 1, true, false),
                            new AudioFormat(16000.0f, 32, 1, true, true),
                            new AudioFormat(16000.0f, 32, 2, true, false),
                            new AudioFormat(16000.0f, 32, 2, true, true),
             */
            new AudioFormat(24000.0f, 16, 1, true, false), // 20
            new AudioFormat(24000.0f, 16, 1, true, true),  // 21
            new AudioFormat(24000.0f, 16, 2, true, false), // 22
            new AudioFormat(24000.0f, 16, 2, true, true),  // 23
            /*	24 and 32 bit not yet possible
                            new AudioFormat(24000.0f, 24, 1, true, false),
                            new AudioFormat(24000.0f, 24, 1, true, true),
                            new AudioFormat(24000.0f, 24, 2, true, false),
                            new AudioFormat(24000.0f, 24, 2, true, true),
                            new AudioFormat(24000.0f, 32, 1, true, false),
                            new AudioFormat(24000.0f, 32, 1, true, true),
                            new AudioFormat(24000.0f, 32, 2, true, false),
                            new AudioFormat(24000.0f, 32, 2, true, true),
             */
            new AudioFormat(48000.0f, 16, 1, true, false), // 32
            new AudioFormat(48000.0f, 16, 1, true, true),  // 33
            new AudioFormat(48000.0f, 16, 2, true, false), // 34
            new AudioFormat(48000.0f, 16, 2, true, true),  // 35
            /*	24 and 32 bit not yet possible
                            new AudioFormat(48000.0f, 24, 1, true, false),
                            new AudioFormat(48000.0f, 24, 1, true, true),
                            new AudioFormat(48000.0f, 24, 2, true, false),
                            new AudioFormat(48000.0f, 24, 2, true, true),
                            new AudioFormat(48000.0f, 32, 1, true, false),
                            new AudioFormat(48000.0f, 32, 1, true, true),
                            new AudioFormat(48000.0f, 32, 2, true, false),
                            new AudioFormat(48000.0f, 32, 2, true, true),
             */
    };

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { OpusFileFormatType.OPUS };

    public OpusAudioFileWriter() {
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
        return new OpusAudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((OpusFileFormatType) fileType).complexity);
    }

    // 2024-09-15: Workaround for TAudioFileWriter
    private static final int FRAME_SIZE_IN_MILLISECONDS = 20;
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

        // 2024-09-15: Edit for Opus Encoder
        int nBufferSize = (int) outputFormat.getFrameRate() * FRAME_SIZE_IN_MILLISECONDS / 1000 * outputFormat.getFrameSize();
        byte[] abBuffer = new byte[nBufferSize];

        OpusAudioOutputParams params = (OpusAudioOutputParams) audioOutputStream;

        OpusInfo opusInfo = new OpusInfo();
        opusInfo.setSampleRate((long) outputFormat.getSampleRate());
        opusInfo.setNumChannels(outputFormat.getChannels());
        OpusTags opusTags = new OpusTags();
        opusTags.setVendor(CodecHelpers.GetVersionString());
        OpusFile opusFile = new OpusFile(new FilterOutputStream(params.getOutputStream()) {@Override public void close() {}},
                opusInfo, opusTags);
        OpusEncoder encoder;
        try {
            encoder = new OpusEncoder(opusInfo.getSampleRate(), opusInfo.getNumChannels(), OpusApplication.OPUS_APPLICATION_AUDIO);
        } catch (OpusException e) {
            throw new IOException(e.getMessage());
        }
        encoder.setBitrate(opusInfo.getSampleRate() * opusInfo.getNumChannels());
        encoder.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
        encoder.setComplexity(params.getComplexity());

        byte[] buffer = new byte[1275];
        long granulepos = 0;
        int samples = encoder.getSampleRate() * FRAME_SIZE_IN_MILLISECONDS / 1000;
        while (true) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            int	nBytesRead = audioInputStream.read(abBuffer);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            if (nBytesRead == -1) break;
            if (bNeedsConversion) TConversionTool.changeOrderOrSign(abBuffer, 0, nBytesRead, nBytesPerSample);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
            short[] pcm = toShorts(abBuffer, 0, nBytesRead);
            if (pcm.length < samples * opusInfo.getNumChannels()) pcm = Arrays.copyOf(pcm, samples * opusInfo.getNumChannels());
            int nWritten;
            try {
                nWritten = encoder.encode(pcm, 0, samples, buffer, 0, buffer.length);
            } catch (OpusException e) {
                throw new IOException(e.getMessage());
            }
            byte[] packet = new byte[nWritten];
            System.arraycopy(buffer, 0, packet, 0, nWritten);
            OpusAudioData opusAudioData = new OpusAudioData(packet);
            // The ogg library should be handling granule positions automatically but for some reason it doesn't.
            // BUT when we do this, it ends up writing only a single packet per ogg page which has an absurd
            // level of container overhead. File a bug on vorbis-java or find a new ogg library...
            granulepos += 48000 * FRAME_SIZE_IN_MILLISECONDS / 1000;
            opusAudioData.setGranulePosition(granulepos);
            opusFile.writeAudioData(opusAudioData);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
            nTotalWritten += nWritten;
        }
        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        opusFile.close();
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    private static short[] toShorts(byte[] input, int offset, int length) {
        short[] shorts = new short[length / 2];
        for (int c = 0; c < shorts.length; c ++) {
            short a = (short) (((int) input[(c * 2) + offset]) & 0xFF);
            short b = (short) (((int) input[(c * 2) + 1 + offset]) << 8);
            shorts[c] = (short) (a | b);
        }
        return shorts;
    }

    private static class OpusAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final OutputStream out;
        private final int complexity;
        public OpusAudioOutputParams(AudioFormat format, long length, OutputStream out, int complexity) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.complexity = complexity;
        }
        public OutputStream getOutputStream() {
            return out;
        }
        public int getComplexity() {
            return complexity;
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

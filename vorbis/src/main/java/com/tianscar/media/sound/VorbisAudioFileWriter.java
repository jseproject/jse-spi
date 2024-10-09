/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
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
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.xiph.vorbis.Packet;
import org.xiph.vorbis.Page;
import org.xiph.vorbis.StreamState;
import org.xiph.vorbis.Block;
import org.xiph.vorbis.Comment;
import org.xiph.vorbis.DspState;
import org.xiph.vorbis.Info;
import org.xiph.vorbis.PcmHelperStruct;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class VorbisAudioFileWriter extends TAudioFileWriter {

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
            new AudioFormat(11025.0f, 16, 1, true, false), // 4
            new AudioFormat(11025.0f, 16, 1, true, true),  // 5
            new AudioFormat(11025.0f, 16, 2, true, false), // 6
            new AudioFormat(11025.0f, 16, 2, true, true),  // 7
            /*	24 and 32 bit not yet possible
                            new AudioFormat(11025.0f, 24, 1, true, false),
                            new AudioFormat(11025.0f, 24, 1, true, true),
                            new AudioFormat(11025.0f, 24, 2, true, false),
                            new AudioFormat(11025.0f, 24, 2, true, true),
                            new AudioFormat(11025.0f, 32, 1, true, false),
                            new AudioFormat(11025.0f, 32, 1, true, true),
                            new AudioFormat(11025.0f, 32, 2, true, false),
                            new AudioFormat(11025.0f, 32, 2, true, true),
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
            new AudioFormat(22050.0f, 16, 1, true, false), // 16
            new AudioFormat(22050.0f, 16, 1, true, true),  // 17
            new AudioFormat(22050.0f, 16, 2, true, false), // 18
            new AudioFormat(22050.0f, 16, 2, true, true),  // 19
            /*	24 and 32 bit not yet possible
                            new AudioFormat(22050.0f, 24, 1, true, false),
                            new AudioFormat(22050.0f, 24, 1, true, true),
                            new AudioFormat(22050.0f, 24, 2, true, false),
                            new AudioFormat(22050.0f, 24, 2, true, true),
                            new AudioFormat(22050.0f, 32, 1, true, false),
                            new AudioFormat(22050.0f, 32, 1, true, true),
                            new AudioFormat(22050.0f, 32, 2, true, false),
                            new AudioFormat(22050.0f, 32, 2, true, true),
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
            new AudioFormat(32000.0f, 16, 1, true, false), // 24
            new AudioFormat(32000.0f, 16, 1, true, true),  // 25
            new AudioFormat(32000.0f, 16, 2, true, false), // 26
            new AudioFormat(32000.0f, 16, 2, true, true),  // 27
            /*	24 and 32 bit not yet possible
                            new AudioFormat(32000.0f, 24, 1, true, false),
                            new AudioFormat(32000.0f, 24, 1, true, true),
                            new AudioFormat(32000.0f, 24, 2, true, false),
                            new AudioFormat(32000.0f, 24, 2, true, true),
                            new AudioFormat(32000.0f, 32, 1, true, false),
                            new AudioFormat(32000.0f, 32, 1, true, true),
                            new AudioFormat(32000.0f, 32, 2, true, false),
                            new AudioFormat(32000.0f, 32, 2, true, true),
             */
            new AudioFormat(44100.0f, 16, 1, true, false), // 28
            new AudioFormat(44100.0f, 16, 1, true, true),  // 29
            new AudioFormat(44100.0f, 16, 2, true, false), // 30
            new AudioFormat(44100.0f, 16, 2, true, true),  // 31
            /*	24 and 32 bit not yet possible
                            new AudioFormat(44100.0f, 24, 1, true, false),
                            new AudioFormat(44100.0f, 24, 1, true, true),
                            new AudioFormat(44100.0f, 24, 2, true, false),
                            new AudioFormat(44100.0f, 24, 2, true, true),
                            new AudioFormat(44100.0f, 32, 1, true, false),
                            new AudioFormat(44100.0f, 32, 1, true, true),
                            new AudioFormat(44100.0f, 32, 2, true, false),
                            new AudioFormat(44100.0f, 32, 2, true, true),
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

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { VorbisFileFormatType.VORBIS };

    public VorbisAudioFileWriter() {
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
        return new VorbisAudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((VorbisFileFormatType) fileType).quality);
    }

    // 2024-09-26: Workaround for TAudioFileWriter
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

        // 2024-09-26: Edit for Vorbis Encoder
        VorbisAudioOutputParams params = (VorbisAudioOutputParams) audioOutputStream;
        OutputStream out = params.getOutputStream();
        float quality = params.getQuality();

        int channels = outputFormat.getChannels();
        int channels2 = channels << 1; // 2 bytes per sample

        byte[] abBuffer = new byte[4096];
        int maxSamples = abBuffer.length / channels2; // 2 bytes per sample
        boolean eos = false;

        StreamState oggStreamState = new StreamState();
        Page oggPage = new Page();
        Packet oggPacket = new Packet();
        Info vorbisInfo = new Info();
        Comment vorbisComment = new Comment();
        DspState vorbisDspState = new DspState();
        Block vorbisBlock = new Block();
        vorbisInfo.init();
        if (vorbisInfo.encode_init_vbr(channels, (int) outputFormat.getSampleRate(), quality) < 0)
            throw new IOException("Failed to initialize encoder");
        // add a comment
        vorbisComment.init();
        //vorbisComment.vorbis_comment_add_tag("ENCODER", "VorbisAudioFileWriter.java");
        // set up the analysis state and auxiliary encoding storage
        vorbisDspState.analysis_init(vorbisInfo);
        vorbisDspState.block_init(vorbisBlock);
        oggStreamState.init(ThreadLocalRandom.current().nextInt());
        Packet header = new Packet();
        Packet header_comm = new Packet();
        Packet header_code = new Packet();
        vorbisDspState.analysis_headerout( vorbisComment, header, header_comm, header_code );
        oggStreamState.packetin( header );
        oggStreamState.packetin( header_comm );
        oggStreamState.packetin( header_code );
        /* Ensures the audio data will start on a new page. */
        while (oggStreamState.flush(oggPage) != 0) {
            out.write( oggPage.header_base, oggPage.header, oggPage.header_len );
            out.write( oggPage.body_base, oggPage.body, oggPage.body_len );
            nTotalWritten += oggPage.header_len;
            nTotalWritten += oggPage.body_len;
        }
        while (!eos) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            int	nBytesRead = audioInputStream.read(abBuffer);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            if (nBytesRead <= 0) {
					/* end of file.  this can be done implicitly in the mainline,
					 but it's easier to see here in non-clever fashion.
					 Tell the library we're at end of stream so that it can handle
					 the last frame and mark end of stream in the output properly */
                vorbisDspState.analysis_wrote(0);
            }
            else { /* data to encode */
                if (bNeedsConversion) TConversionTool.changeOrderOrSign(abBuffer, 0, nBytesRead, nBytesPerSample);
                if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
                /* expose the buffer to submit data */
                PcmHelperStruct vpcm = vorbisDspState.analysis_buffer(maxSamples);
                float[][] pcmf = vpcm.pcm;
                int mono = vpcm.pcmret;

                /* uninterleave samples */
                int i;
                for (int c = 0; c < channels; c ++) {
                    float[] p = pcmf[c];
                    int dest = mono;
                    for (i = c << 1; i < nBytesRead; i += channels2) {
                        p[dest ++] = ((float) (((int) abBuffer[i + 1] << 8) | (0x00FF & (int) abBuffer[i]))) / 32768.f;
                    }
                }
                /* tell the library how much we actually submitted */
                vorbisDspState.analysis_wrote(nBytesRead / channels2);
            }
            /* vorbis does some data preanalysis, then divvies up blocks for
			   more involved (potentially parallel) processing.  Get a single
			   block for encoding now */
            while (vorbisDspState.analysis_blockout(vorbisBlock)) {
                /* analysis, assume we want to use bitrate management */
                vorbisBlock.analysis(null);
                vorbisBlock.bitrate_addblock();
                while (vorbisDspState.bitrate_flushpacket(oggPacket)) {
                    /* weld the packet into the bitstream */
                    oggStreamState.packetin(oggPacket);
                    /* write out pages (if any) */
                    while (!eos && oggStreamState.pageout(oggPage) != 0) {
                        out.write(oggPage.header_base, oggPage.header, oggPage.header_len);
                        out.write(oggPage.body_base, oggPage.body, oggPage.body_len);
                        nTotalWritten += oggPage.header_len;
                        nTotalWritten += oggPage.body_len;
                        /* this could be set above, but for illustrative purposes, I do
						   it here (to show that vorbis does know where the stream ends) */
                        if (oggPage.eos()) eos = true;
                    }
                }
            }
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
        }
        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        oggStreamState.clear();
        vorbisBlock.clear();
        vorbisDspState.clear();
        vorbisComment.clear();
        vorbisInfo.clear();
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    private static class VorbisAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final OutputStream out;
        private final float quality;
        public VorbisAudioOutputParams(AudioFormat format, long length, TNonSeekableDataOutputStream out, float quality) {
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

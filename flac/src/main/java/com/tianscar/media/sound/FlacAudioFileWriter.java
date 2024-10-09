/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
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
 * - Neither the name of the Xiph.Org Foundation nor the names of its
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

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.tritonus.share.sampled.file.TSeekableDataOutputStream;
import org.xiph.flac.Format;
import org.xiph.flac.OggEncoderAspectWriteCallbackProxy;
import org.xiph.flac.StreamEncoder;
import org.xiph.flac.StreamEncoderReadCallback;
import org.xiph.flac.StreamEncoderSeekCallback;
import org.xiph.flac.StreamEncoderTellCallback;
import org.xiph.flac.StreamEncoderWriteCallback;
import org.xiph.flac.StreamMetadata;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class FlacAudioFileWriter extends TAudioFileWriter 
        implements StreamEncoderWriteCallback, StreamEncoderSeekCallback, StreamEncoderTellCallback, StreamEncoderReadCallback,
        OggEncoderAspectWriteCallbackProxy {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[40];
    static {
        for (int channels = 0; channels < 8; channels ++) {
            SUPPORTED_FORMATS[channels * 5] = new AudioFormat(-1.0f, 8, channels + 1, true, false);
            SUPPORTED_FORMATS[channels * 5 + 1] = new AudioFormat(-1.0f, 16, channels + 1, true, false);
            SUPPORTED_FORMATS[channels * 5 + 2] = new AudioFormat(-1.0f, 16, channels + 1, true, true);
            SUPPORTED_FORMATS[channels * 5 + 3] = new AudioFormat(-1.0f, 24, channels + 1, true, false);
            SUPPORTED_FORMATS[channels * 5 + 4] = new AudioFormat(-1.0f, 24, channels + 1, true, true);
        }
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { FlacFileFormatType.FLAC, FlacFileFormatType.OGG_FLAC };

    public FlacAudioFileWriter() {
        super(Arrays.asList(TYPES), Arrays.asList(SUPPORTED_FORMATS));
    }

    @Override
    protected boolean isAudioFormatSupportedImpl(AudioFormat audioFormat, AudioFileFormat.Type fileType) {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out("> TAudioFileWriter.isAudioFormatSupportedImpl(): format to test: " + audioFormat);
            TDebug.out("class: " + getClass().getName());
        }
        Iterator<AudioFormat> audioFormats = getSupportedAudioFormats(fileType);
        while (audioFormats.hasNext()) {
            AudioFormat	handledFormat = audioFormats.next();
            if (TDebug.TraceAudioFileWriter) TDebug.out("matching against format : " + handledFormat);
            if (AudioFormats.matches(handledFormat, audioFormat) && FlacFormatConversionProvider.checkFormat(audioFormat)) {
                if (TDebug.TraceAudioFileWriter) TDebug.out("<...succeeded.");
                return true;
            }
        }
        if (TDebug.TraceAudioFileWriter) TDebug.out("< ... failed");
        return false;
    }

    @Override
    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, File file) throws IOException {
        try (OutputStream ignored = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            return super.write(audioInputStream, fileType, file);
        }
    }

    @Override
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat, long lLengthInBytes, AudioFileFormat.Type fileType, TDataOutputStream dataOutputStream) throws IOException {
        return new FlacAudioOutputParams(audioFormat, lLengthInBytes, dataOutputStream, ((FlacFileFormatType) fileType).compressionLevel, FlacFileFormatType.OGG_FLAC.equals(fileType));
    }

    // 2024-09-26: Workaround for TAudioFileWriter
    private final ThreadLocal<TDataOutputStream> outRef = new ThreadLocal<>();
    private final ThreadLocal<Integer> writtenRef = new ThreadLocal<>();
    @Override
    protected int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion) throws IOException {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out(">TAudioFileWriter.writeImpl(): called");
            TDebug.out("class: " + getClass().getName());
        }
        //int nTotalWritten = 0;
        writtenRef.set(0);
        AudioFormat	outputFormat = audioOutputStream.getFormat();

        // TODO: handle case when frame size is unknown ?
        //int nBytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();

        // 2024-10-05: Edit for Flac Encoder
        FlacAudioOutputParams params = (FlacAudioOutputParams) audioOutputStream;
        TDataOutputStream out = params.getTDataOutputStream();

        boolean is_big_endian = outputFormat.isBigEndian();
        boolean is_unsigned = AudioFormat.Encoding.PCM_UNSIGNED.equals(outputFormat.getEncoding());

        int bps = outputFormat.getSampleSizeInBits();
        int channels = outputFormat.getChannels();
        int sampleRate = (int) outputFormat.getSampleRate();
        int compressionLevel = params.getCompressionLevel();

        StreamEncoder encoder = new StreamEncoder();
        boolean ok = true;

        StreamMetadata[] metadata = new StreamMetadata[2];

        ok &= encoder.set_verify(true);
        ok &= encoder.set_compression_level(compressionLevel);
        ok &= encoder.set_channels(channels);
        ok &= encoder.set_bits_per_sample(bps);
        ok &= encoder.set_sample_rate(sampleRate);
        if (params.isOgg()) ok &= encoder.set_ogg_serial_number(ThreadLocalRandom.current().nextInt());
        //ok &= encoder.FLAC__stream_encoder_set_total_samples_estimate(total_samples);

        if (!ok) throw new IOException("Failed to initializing encoder");

        /* now add some metadata; we'll add some tags and a padding block */
        if ((metadata[0] = StreamMetadata.metadata_new(Format.FLAC__METADATA_TYPE_VORBIS_COMMENT)) == null
                || (metadata[1] = StreamMetadata.metadata_new(Format.FLAC__METADATA_TYPE_PADDING)) == null) ok = false;

        if (ok) {
            metadata[1].length = 4 + 4; /* set the padding length */
            ok = encoder.set_metadata(metadata, 2);
        }
        if (!ok) throw new IOException("Failed to initializing encoder");

        /* initialize encoder */
        outRef.set(out);
        int init_status;
        if (params.isOgg()) init_status = encoder.init_ogg_stream(this, this, this, this, null);
        else init_status = encoder.init_stream(this, this, this, null);
        if (init_status != StreamEncoder.FLAC__STREAM_ENCODER_INIT_STATUS_OK)
            throw new IOException("Failed to initializing encoder: " + StreamEncoder.FLAC__StreamEncoderInitStatusString[init_status]);
        byte[] abBuffer = new byte[4096];
        bps >>>= 3;
        int wide_samples = abBuffer.length / bps / channels;
        int[] pcm = new int[channels * wide_samples];
        int nBytesRead;
        while (ok) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            nBytesRead = audioInputStream.read(abBuffer, 0, abBuffer.length);
            if (nBytesRead < 0) break;
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            format_input(abBuffer, pcm, wide_samples, is_big_endian, is_unsigned, channels, bps);
            ok = encoder.process_interleaved(pcm, nBytesRead / channels / bps);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
        }
        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + writtenRef.get() + " bytes");

        encoder.finish();

        /* now that encoding is finished, the metadata can be freed */
        StreamMetadata.delete(metadata[0]);
        StreamMetadata.delete(metadata[1]);

        encoder.delete();

        if (out instanceof TNonSeekableDataOutputStream) {
            ((TNonSeekableDataOutputStream) out).flush();
        }
        else if (out instanceof TSeekableDataOutputStream) {
            out.close();
        }
        outRef.remove();
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        //return nTotalWritten;
        return writtenRef.get();
    }

    private static boolean format_input(byte[] scbuffer, int[] dest, int wide_samples, boolean is_big_endian, boolean is_unsigned_samples, int channels, int bytes_ps) {
        int wide_sample, sample, channel, ibyte;
        switch (bytes_ps) {
            case 1:
                if (is_unsigned_samples) {
                    for (sample = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++, sample ++) {
                            dest[wide_sample * channels + channel] = ((int) scbuffer[sample] & 0xFF) - 0x80;
                        }
                    }
                }
                else {
                    for (sample = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++, sample ++) {
                            dest[wide_sample * channels + channel] = scbuffer[sample];
                        }
                    }
                }
                break;
            case 2:
                if (is_big_endian) {
                    int bytes = (wide_samples * channels) << 1 /* (*bytes_ps) */;
                    for (ibyte = 0; ibyte < bytes; ibyte += 2) {
                        byte tmp = scbuffer[ibyte];
                        scbuffer[ibyte] = scbuffer[ibyte + 1];
                        scbuffer[ibyte + 1] = tmp;
                    }
                }
                if (is_unsigned_samples) {
                    for (sample = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++) {
                            int tmp = (int) scbuffer[sample ++] & 0xFF;
                            tmp |= ((int) scbuffer[sample ++] & 0xFF) << 8;
                            tmp -= 0x8000;
                            dest[wide_sample * channels + channel] = tmp;
                        }
                    }
                }
                else {
                    for (sample = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++) {
                            int tmp = (int) scbuffer[sample ++] & 0xFF;
                            tmp |= ((int) scbuffer[sample ++]) << 8;
                            dest[wide_sample * channels + channel] = tmp;
                        }
                    }
                }
                break;
            case 3:
                if (!is_big_endian) {
                    int bytes = wide_samples * channels * 3;
                    for (ibyte = 0; ibyte < bytes; ibyte += 3) {
                        byte tmp = scbuffer[ibyte];
                        scbuffer[ibyte] = scbuffer[ibyte + 2];
                        scbuffer[ibyte + 2] = tmp;
                    }
                }
                if (is_unsigned_samples) {
                    for (ibyte = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++) {
                            int tmp = (int) scbuffer[ibyte ++] & 0xFF;
                            tmp |= ((int) scbuffer[ibyte ++] & 0xFF) << 8;
                            tmp |= ((int) scbuffer[ibyte ++] & 0xFF) << 16;
                            tmp -= 0x800000;
                            dest[wide_sample * channels + channel] = tmp;
                        }
                    }
                }
                else {
                    for (ibyte = wide_sample = 0; wide_sample < wide_samples; wide_sample ++) {
                        for (channel = 0; channel < channels; channel ++) {
                            int tmp = (int) scbuffer[ibyte ++] & 0xFF;
                            tmp |= ((int) scbuffer[ibyte ++] & 0xFF) << 8;
                            tmp |= ((int) scbuffer[ibyte ++]) << 16;
                            dest[wide_sample * channels + channel] = tmp;
                        }
                    }
                }
                break;
            default: return false;
        }
        return true;
    }

    @Override
    public int enc_write_callback(StreamEncoder encoder, byte[] buffer, int offset, int bytes, int samples, int current_frame) {
        TDataOutputStream out = outRef.get();
        try {
            out.write(buffer, offset, bytes);
            writtenRef.set(writtenRef.get() + bytes);
        }
        catch (IOException e) {
            return StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
        }
        return StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
    }

    @Override
    public long enc_tell_callback(StreamEncoder encoder) throws IOException {
        TDataOutputStream out = outRef.get();
        if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).getFilePointer();
        throw new UnsupportedOperationException();
    }

    @Override
    public int enc_seek_callback(StreamEncoder encoder, long absolute_byte_offset) {
        TDataOutputStream out = outRef.get();
        if (out instanceof TSeekableDataOutputStream) {
            try {
                ((TSeekableDataOutputStream) out).seek(absolute_byte_offset);
            }
            catch (IOException e) {
                return StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
            }
            return StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_OK;
        }
        return StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
    }

    @Override
    public int ogg_write_callback(StreamEncoder encoder, byte[] buffer, int offset, int bytes, int samples, int current_frame) {
        TDataOutputStream out = outRef.get();
        try {
            out.write(buffer, offset, bytes);
            writtenRef.set(writtenRef.get() + bytes);
        }
        catch (IOException e) {
            return StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
        }
        return StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
    }

    @Override
    public int enc_read_callback(StreamEncoder encoder, byte[] buffer, int offset, int bytes) throws IOException, UnsupportedOperationException {
        TDataOutputStream out = outRef.get();
        if (out instanceof TSeekableDataOutputStream) {
            return ((TSeekableDataOutputStream) out).read(buffer, offset, bytes);
        }
        throw new UnsupportedOperationException();
    }

    private static class FlacAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final TDataOutputStream out;
        private final int compressionLevel;
        private final boolean isogg;
        public FlacAudioOutputParams(AudioFormat format, long length, TDataOutputStream out, int compressionLevel, boolean isogg) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.compressionLevel = compressionLevel;
            this.isogg = isogg;
        }
        public TDataOutputStream getTDataOutputStream() {
            return out;
        }
        public int getCompressionLevel() {
            return compressionLevel;
        }
        public boolean isOgg() {
            return isogg;
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

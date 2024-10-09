/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2008 Christopher G. Jennings
 * Copyright (c) 1999-2010 JavaZOOM
 * Copyright (c) 1999 Mat McGowan
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1993-1994 Tobias Bading
 * Copyright (c) 1991 MPEG Software Simulation Group
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * - You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.tianscar.media.sound;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.Obuffer;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class DecodedMp3AudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private Header header;
    private final InputStream encodedStream;
    private final Bitstream bitstream;
    private final Decoder decoder;
    private final Equalizer equalizer;
    private final float[] equalizer_values;
    private final DMAISObuffer oBuffer;

    public DecodedMp3AudioInputStream(AudioFormat outputFormat, Mp3AudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedMp3AudioInputStream(AudioFormat, AudioInputStream)");
        encodedStream = inputStream;
        bitstream = new Bitstream(inputStream);
        decoder = new Decoder(null);
        equalizer = new Equalizer();
        equalizer_values = new float[32];
        for (int b = 0; b < equalizer.getBandCount(); b ++) {
            equalizer_values[b] = equalizer.getBand(b);
        }
        decoder.setEqualizer(equalizer);
        oBuffer = new DMAISObuffer(outputFormat.getChannels(), format.isBigEndian());
        decoder.setOutputBuffer(oBuffer);
        try {
            header = bitstream.readFrame();
        }
        catch (BitstreamException e) {
            TDebug.out("DecodedMp3AudioInputStream : Cannot read first frame : " + e.getMessage());
        }
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        try {
            if (header == null) header = bitstream.readFrame();
            if (TDebug.TraceAudioConverter) TDebug.out("execute(): header = " + header);
            if (header == null) {
                if (TDebug.TraceAudioConverter) TDebug.out("header is null (end of mpeg stream)");
                getCircularBuffer().close();
                return;
            }
            for (int b = 0; b < equalizer_values.length; b ++) {
                equalizer.setBand(b, equalizer_values[b]);
            }
            decoder.setEqualizer(equalizer);
            decoder.decodeFrame(header, bitstream);
            bitstream.closeFrame();
            getCircularBuffer().write(oBuffer.getBuffer(), 0, oBuffer.getCurrentBufferSize());
            oBuffer.reset();
            if (header != null) header = null;
        }
        catch (BitstreamException | DecoderException | ArrayIndexOutOfBoundsException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
        }
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    @Override
    public void close() throws IOException {
        super.close();
        encodedStream.close();
    }

    private static class DMAISObuffer extends Obuffer {
        private final int nChannels;
        private final byte[] abBuffer;
        private final int[] anBufferPointers;
        private final boolean isBigEndian;
        public DMAISObuffer(int nChannels, boolean isBigEndian) {
            this.nChannels = nChannels;
            abBuffer = new byte[OBUFFERSIZE * nChannels];
            anBufferPointers = new int[nChannels];
            reset();
            this.isBigEndian = isBigEndian;
        }
        @Override
        public void append(int nChannel, short sValue) {
            byte bFirstByte;
            byte bSecondByte;
            if (isBigEndian) {
                bFirstByte = (byte) ((sValue >>> 8) & 0xFF);
                bSecondByte = (byte) (sValue & 0xFF);
            }
            else {
                bFirstByte = (byte) (sValue & 0xFF);
                bSecondByte = (byte) ((sValue >>> 8) & 0xFF);
            }
            abBuffer[anBufferPointers[nChannel]] = bFirstByte;
            abBuffer[anBufferPointers[nChannel] + 1] = bSecondByte;
            anBufferPointers[nChannel] += nChannels * 2;
        }
        @Override
        public void set_stop_flag() {}
        @Override
        public void close() {}
        @Override
        public void write_buffer(int nValue) {}
        @Override
        public void clear_buffer() {}
        public byte[] getBuffer() {
            return abBuffer;
        }
        public int getCurrentBufferSize() {
            return anBufferPointers[0];
        }
        public void reset() {
            for (int i = 0; i < nChannels; i ++) {
                /*	Points to byte location,
                 *	implicitely assuming 16 bit
                 *	samples.
                 */
                anBufferPointers[i] = i * 2;
            }
        }
    }

}

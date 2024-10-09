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
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.xiph.vorbis.Packet;
import org.xiph.vorbis.Page;
import org.xiph.vorbis.StreamState;
import org.xiph.vorbis.SyncState;
import org.xiph.vorbis.Block;
import org.xiph.vorbis.Comment;
import org.xiph.vorbis.DspState;
import org.xiph.vorbis.Info;
import org.xiph.vorbis.PcmHelperStruct;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class DecodedVorbisAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private final InputStream oggBitStream;

    private SyncState oggSyncState = null;
    private StreamState oggStreamState = null;
    private Page oggPage = null;
    private Packet oggPacket = null;

    private Info vorbisInfo = null;
    private Comment vorbisComment = null;
    private DspState vorbisDspState = null;
    private Block vorbisBlock = null;

    private static final int PLAY_STATE_NEED_HEADERS = 0;
    private static final int PLAY_STATE_READ_DATA = 1;
    private static final int PLAY_STATE_WRITE_DATA = 2;
    private static final int PLAY_STATE_DONE = 3;
    private static final int PLAY_STATE_BUFFER_FULL = 4;
    private static final int PLAY_STATE_CORRUPT = -1;
    private int playState;

    private static final int BUFFER_MULTIPLE = 4;
    private static final int BUFFER_SIZE = BUFFER_MULTIPLE * 256 * 2;

    private int bufferSize = BUFFER_SIZE * 2;
    private byte[] buffer = new byte[bufferSize];
    private int dataRead = 0;
    // bout is now a global so that we can continue from when we have a buffer full.
    private int bufferOutSize = 0;

    //private long currentBytes = 0;

    public DecodedVorbisAudioInputStream(AudioFormat outputFormat, VorbisAudioInputStream bitStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        this.oggBitStream = bitStream.getFilteredInputStream();
        initDecoder();
        execute();
    }

    /**
     * Initializes all the jOrbis and jOgg vars that are used for song playback.
     */
    private void initDecoder() {
        oggSyncState = new SyncState();
        oggStreamState = new StreamState();
        oggPage = new Page();
        oggPacket = new Packet();
        vorbisInfo = new Info();
        vorbisComment = new Comment();
        vorbisDspState = new DspState();
        vorbisBlock = new Block();
        vorbisDspState.block_init(vorbisBlock);
        //currentBytes = 0L;
        oggSyncState.init();
        playState = PLAY_STATE_NEED_HEADERS;
    }

    @Override
    public int read(byte[] abData, int nOffset, int nLength) throws IOException {
        int n = super.read(abData, nOffset, nLength);
        while (n == 0) n = super.read(abData, nOffset, nLength);
        return n;
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) {
            switch (playState) {
                case PLAY_STATE_NEED_HEADERS:
                    TDebug.out("playState = playState_NeedHeaders");
                    break;
                case PLAY_STATE_READ_DATA:
                    TDebug.out("playState = playState_ReadData");
                    break;
                case PLAY_STATE_WRITE_DATA:
                    TDebug.out("playState = playState_WriteData");
                    break;
                case PLAY_STATE_DONE:
                    TDebug.out("playState = playState_Done");
                    break;
                case PLAY_STATE_BUFFER_FULL:
                    TDebug.out("playState = playState_BufferFull");
                    break;
                case PLAY_STATE_CORRUPT:
                    TDebug.out("playState = playState_Corrupt");
                    break;
            }
        }
        switch (playState) {
            case PLAY_STATE_NEED_HEADERS:
                try {
                    // Headers (+ Comments).
                    readHeaders();
                }
                catch (IOException e) {
                    playState = PLAY_STATE_CORRUPT;
                    return;
                }
                playState = PLAY_STATE_READ_DATA;
                break;
            case PLAY_STATE_READ_DATA:
                int result;
                int bufferOffset = oggSyncState.buffer(BUFFER_SIZE);
                byte[] data = oggSyncState.data;
                dataRead = readFromStream(data, bufferOffset, BUFFER_SIZE);
                if (TDebug.TraceAudioConverter) TDebug.out("More data: " + dataRead);
                if (dataRead == -1) {
                    playState = PLAY_STATE_DONE;
                    if (TDebug.TraceAudioConverter) TDebug.out("Ogg Stream empty. Settings playState to playState_Done.");
                    break;
                }
                else {
                    oggSyncState.wrote(dataRead);
                    if (dataRead == 0) {
                        if (oggPage.eos() || oggStreamState.eos() || oggPacket.e_o_s) {
                            if (TDebug.TraceAudioConverter) TDebug.out("oggSyncState wrote 0 bytes: settings playState to playState_Done.");
                            playState = PLAY_STATE_DONE;
                        }
                        if (TDebug.TraceAudioConverter) TDebug.out("oggSyncState wrote 0 bytes: but stream not yet empty.");
                        break;
                    }
                }
                result = oggSyncState.pageout(oggPage);
                if (result == 0) {
                    if (TDebug.TraceAudioConverter) TDebug.out("Setting playState to playState_ReadData.");
                    playState = PLAY_STATE_READ_DATA;
                    break;
                } // need more data
                if (result == -1) {
                    // missing or corrupt data at this page position
                    if (TDebug.TraceAudioConverter) TDebug.out("Corrupt or missing data in bitstream; setting playState to playState_ReadData");
                    playState = PLAY_STATE_READ_DATA;
                    break;
                }
                oggStreamState.pagein(oggPage);
                if (TDebug.TraceAudioConverter) TDebug.out("Setting playState to playState_WriteData.");
                playState = PLAY_STATE_WRITE_DATA;
                break;
            case PLAY_STATE_WRITE_DATA:
                // Decoding !
                if (TDebug.TraceAudioConverter) TDebug.out("Decoding");
                while (true) {
                    result = oggStreamState.packetout(oggPacket);
                    if (result == 0) {
                        if( TDebug.TraceAudioConverter) TDebug.out("Packetout returned 0, going to read state.");
                        playState = PLAY_STATE_READ_DATA;
                        break;
                    } // need more data
                    else if (result == -1) {
                        // missing or corrupt data at this page position
                        // no reason to complain; already complained above
                        if (TDebug.TraceAudioConverter) TDebug.out("Corrupt or missing data in packetout bitstream; going to read state...");
                        // playState = playState_ReadData;
                        // break;
                        continue;
                    }
                    else {
                        // we have a packet.  Decode it
                        if (vorbisBlock.synthesis(oggPacket) == 0) {
                            // test for success!
                            vorbisDspState.synthesis_blockin(vorbisBlock);
                        }
                        else {
                            // if(TDebug.TraceAudioConverter) TDebug.out("vorbisBlock.synthesis() returned !0, going to read state");
                            if (TDebug.TraceAudioConverter) TDebug.out("VorbisBlock.synthesis() returned !0, continuing.");
                            continue;
                        }
                        outputSamples();
                        if (playState == PLAY_STATE_BUFFER_FULL) return;
                    } // else result != -1
                } // while(true)
                if (oggPage.eos()) {
                    if (TDebug.TraceAudioConverter) TDebug.out("Settings playState to playState_Done.");
                    playState = PLAY_STATE_DONE;
                }
                break;
            case PLAY_STATE_BUFFER_FULL:
                continueFromBufferFull();
                break;
            case PLAY_STATE_CORRUPT:
                if (TDebug.TraceAudioConverter) TDebug.out("Corrupt Song.");
                // drop through to playState_Done...
            case PLAY_STATE_DONE:
                oggStreamState.clear();
                vorbisBlock.clear();
                vorbisDspState.clear();
                vorbisInfo.clear();
                oggSyncState.clear();
                if (TDebug.TraceAudioConverter) TDebug.out("Done Song.");
                try {
                    if (oggBitStream != null) oggBitStream.close();
                    getCircularBuffer().close();
                }
                catch (Exception e) {
                    if(TDebug.TraceAudioConverter) TDebug.out(e.getMessage());
                }
                break;
        } // switch
    }

    /**
     * This routine was extracted so that when the output buffer fills up,
     * we can break out of the loop, let the music channel drain, then
     * continue from where we were.
     */
    private void outputSamples() {
        while (true) {
            PcmHelperStruct pcm = vorbisDspState.synthesis_pcmout(true);
            if (pcm.samples <= 0) break;
            float[][] pcmf = pcm.pcm;
            bufferOutSize = Math.min(pcm.samples, bufferSize);
            double fVal;
            // convert doubles to 16 bit signed ints (host order) and
            // interleave
            for (int i = 0; i < vorbisInfo.channels; i ++) {
                int pointer = i * 2;
                //int ptr=i;
                int mono = pcm.pcmret;
                for (int j = 0; j < bufferOutSize; j ++) {
                    fVal = pcmf[i][mono + j] * 32767.0;
                    int val = (int) fVal;
                    if (val > 32767) val = 32767;
                    if (val < -32768) val = -32768;
                    if (val < 0) val = val | 0x8000;
                    buffer[pointer] = (byte) val;
                    buffer[pointer + 1] = (byte) (val >>> 8);
                    pointer += 2 * (vorbisInfo.channels);
                }
            }
            if (TDebug.TraceAudioConverter) TDebug.out("about to write: " + 2 * vorbisInfo.channels * bufferOutSize);
            if (getCircularBuffer().availableWrite() < 2 * vorbisInfo.channels * bufferOutSize) {
                if (TDebug.TraceAudioConverter) TDebug.out("Too much data in this data packet, better return, let the channel drain, and try again...");
                playState = PLAY_STATE_BUFFER_FULL;
                return;
            }
            getCircularBuffer().write(buffer, 0, 2 * vorbisInfo.channels * bufferOutSize);
            if (dataRead < BUFFER_SIZE)
                if (TDebug.TraceAudioConverter) TDebug.out("Finished with final buffer of music?");
            if (vorbisDspState.synthesis_read(bufferOutSize) != 0)
                if (TDebug.TraceAudioConverter) TDebug.out("VorbisDspState.synthesis_read returned -1.");
        } // while(samples...)
        playState = PLAY_STATE_READ_DATA;
    }

    private void continueFromBufferFull() {
        if (getCircularBuffer().availableWrite() < 2 * vorbisInfo.channels * bufferOutSize) {
            if (TDebug.TraceAudioConverter) TDebug.out("Too much data in this data packet, better return, let the channel drain, and try again...");
            // Don't change play state.
            return;
        }
        getCircularBuffer().write(buffer, 0, 2 * vorbisInfo.channels * bufferOutSize);
        // Don't change play state. Let outputSamples change play state, if necessary.
        outputSamples();
    }

    private void readHeaders() throws IOException {
        if (TDebug.TraceAudioConverter) TDebug.out("readHeaders(");
        int bufferOffset = oggSyncState.buffer(BUFFER_SIZE);
        byte[] data = oggSyncState.data;
        dataRead = readFromStream(data, bufferOffset, BUFFER_SIZE);
        if (dataRead == -1) {
            if (TDebug.TraceAudioConverter) TDebug.out("Cannot get any data from selected Ogg bitstream.");
            throw new IOException("Cannot get any data from selected Ogg bitstream.");
        }
        oggSyncState.wrote(dataRead);
        if (oggSyncState.pageout(oggPage) != 1) {
            if (dataRead < BUFFER_SIZE) throw new IOException("EOF");
            if (TDebug.TraceAudioConverter) TDebug.out("Input does not appear to be an Ogg bitstream.");
            throw new IOException("Input does not appear to be an Ogg bitstream.");
        }
        oggStreamState.init(oggPage.serialno());
        vorbisInfo.init();
        vorbisComment.init();
        if (oggStreamState.pagein(oggPage) < 0) {
            // error; stream version mismatch perhaps
            if (TDebug.TraceAudioConverter) TDebug.out("Error reading first page of Ogg bitstream data.");
            throw new IOException("Error reading first page of Ogg bitstream data.");
        }
        if (oggStreamState.packetout(oggPacket) != 1) {
            // no page? must not be vorbis
            if (TDebug.TraceAudioConverter) TDebug.out("Error reading initial header packet.");
            throw new IOException("Error reading initial header packet.");
        }
        if (Info.synthesis_headerin(vorbisInfo, vorbisComment, oggPacket) < 0) {
            // error case; not a vorbis header
            if (TDebug.TraceAudioConverter) TDebug.out("This Ogg bitstream does not contain Vorbis audio data.");
            throw new IOException("This Ogg bitstream does not contain Vorbis audio data.");
        }
        int i = 0;
        while (i < 2) {
            while (i < 2) {
                int result = oggSyncState.pageout(oggPage);
                if (result == 0) break; // Need more data
                if (result == 1) {
                    oggStreamState.pagein(oggPage);
                    while (i < 2) {
                        result = oggStreamState.packetout(oggPacket);
                        if (result == 0) break;
                        if (result == -1) {
                            if (TDebug.TraceAudioConverter) TDebug.out("Corrupt secondary header.  Exiting.");
                            throw new IOException("Corrupt secondary header.  Exiting.");
                        }
                        Info.synthesis_headerin(vorbisInfo, vorbisComment, oggPacket);
                        i ++;
                    }
                }
            }
            bufferOffset = oggSyncState.buffer(BUFFER_SIZE);
            data = oggSyncState.data;
            dataRead = readFromStream(data, bufferOffset, BUFFER_SIZE);
            if (dataRead == -1) break;
            if (dataRead == 0 && i < 2) {
                if (TDebug.TraceAudioConverter) TDebug.out("End of file before finding all Vorbis headers!");
                throw new IOException("End of file before finding all Vorbis  headers!");
            }
            oggSyncState.wrote(dataRead);
        }

        if (TDebug.TraceAudioConverter) {
            String[] ptr = vorbisComment.user_comments;

            for (int j = 0; j < ptr.length; j ++) {
                if (ptr[j] == null) break;
                TDebug.out("Comment: " + ptr[j]);
            }
        }
        bufferSize = BUFFER_SIZE / vorbisInfo.channels;
        vorbisDspState.synthesis_init(vorbisInfo);
        vorbisDspState.block_init(vorbisBlock);
    }

    /**
     * Reads from the oggBitStream a specified number of Bytes(bufferSize) worth
     * starting at index and puts them in the specified buffer[].
     *
     * @param buffer
     * @param index
     * @param bufferSize
     * @return             the number of bytes read or -1 if error.
     */
    private int readFromStream(byte[] buffer, int index, int bufferSize) {
        int bytesRead;
        try {
            bytesRead = oggBitStream.read(buffer, index, bufferSize);
        }
        catch (Exception e) {
            if (TDebug.TraceAudioConverter) TDebug.out("Cannot Read Selected Song");
            bytesRead = -1;
        }
        //currentBytes = currentBytes + bytesRead;
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();
        oggBitStream.close();
    }

}

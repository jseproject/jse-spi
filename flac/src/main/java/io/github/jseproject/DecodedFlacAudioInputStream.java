package io.github.jseproject;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.xiph.flac.Format;
import org.xiph.flac.Frame;
import org.xiph.flac.StreamDecoder;
import org.xiph.flac.StreamDecoderErrorCallback;
import org.xiph.flac.StreamDecoderWriteCallback;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedFlacAudioInputStream extends TAsynchronousFilteredAudioInputStream
        implements StreamDecoderWriteCallback, StreamDecoderErrorCallback {

    private StreamDecoder decoder = new StreamDecoder();
    private byte[] buffer = new byte[Format.FLAC__MAX_BLOCK_SIZE * Format.FLAC__MAX_CHANNELS * (Integer.SIZE >>> 3)]; /* WATCHOUT: can be up to 2 megs */

    public DecodedFlacAudioInputStream(AudioFormat outputFormat, FlacAudioInputStream inputStream) {
        super(outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedFlacAudioInputStream(AudioFormat, AudioInputStream)");
        decoder.set_md5_checking(true);
        int init_status;
        if (inputStream.isOgg()) init_status = decoder.init_ogg_FILE(inputStream.getFilteredInputStream(), this /* write_callback */, null /* metadata_callback */, this /* error_callback */);
        else init_status = decoder.init_FILE(inputStream.getFilteredInputStream(), this /* write_callback */, null /* metadata_callback */, this /* error_callback */);
        if (init_status != StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK) {
            decoder.delete(false);
            decoder = null;
            if (TDebug.TraceAudioConverter) TDebug.out(StreamDecoder.FLAC__StreamDecoderInitStatusString[init_status]);
            throw new IllegalArgumentException("conversion not supported");
        }
    }

    @Override
    public int read(byte[] abData, int nOffset, int nLength) throws IOException {
        int n = super.read(abData, nOffset, nLength);
        while (n == 0) n = super.read(abData, nOffset, nLength);
        return n;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (decoder != null) {
            decoder.delete();
            decoder = null;
        }
        buffer = null;
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): begin");
        if (!decoder.process_single() ||
                decoder.get_state() == StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM) {
            getCircularBuffer().close();
        }
        if (TDebug.TraceAudioConverter) TDebug.out("execute(): end");
    }

    @Override
    public void dec_error_callback(StreamDecoder decoder, int status) throws IOException {
        if (TDebug.TraceAudioConverter) TDebug.out(new IOException(StreamDecoder.FLAC__StreamDecoderErrorStatusString[status]));
        getCircularBuffer().close();
    }

    @Override
    public int dec_write_callback(StreamDecoder decoder, Frame frame, int[][] buffer, int offset) {
        int sample, channel, ibyte;
        int bps = frame.header.bits_per_sample, channels = frame.header.channels;
        int blocksize = frame.header.blocksize;
        int written = 0;
        /* generic code for the rest */
        switch (bps) {
            case 8:
                for (sample = 0; sample < blocksize; sample ++) {
                    for (channel = 0; channel < channels; channel ++) {
                        this.buffer[written ++] = (byte) buffer[channel][sample];
                    }
                }
                break;
            case 16:
                switch (channels) {
                    case 1:
                        for (sample = 0; sample < blocksize; sample ++) {
                            ibyte = buffer[0][sample];
                            this.buffer[written ++] = (byte) (ibyte & 0xFF);
                            this.buffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        }
                        break;
                    case 2:
                        for (sample = 0; sample < blocksize; sample ++) {
                            ibyte = buffer[0][sample];
                            this.buffer[written ++] = (byte) (ibyte & 0xFF);
                            this.buffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                            ibyte = buffer[1][sample];
                            this.buffer[written ++] = (byte) (ibyte & 0xFF);
                            this.buffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        }
                        break;
                    default: /* works for any 'channels' but above flavors are faster for 1 and 2 */
                        for (sample = 0; sample < blocksize; sample ++) {
                            for (channel = 0; channel < channels; channel ++) {
                                ibyte = buffer[channel][sample];
                                this.buffer[written ++] = (byte) (ibyte & 0xFF);
                                this.buffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                            }
                        }
                        break;
                }
                break;
            case 24:
                for (sample = 0; sample < blocksize; sample ++) {
                    for (channel = 0; channel < channels; channel ++) {
                        ibyte = buffer[channel][sample];
                        this.buffer[written ++] = (byte) (ibyte & 0xFF);
                        this.buffer[written ++] = (byte) ((ibyte >> 8) & 0xFF);
                        this.buffer[written ++] = (byte) ((ibyte >> 16) & 0xFF);
                    }
                }
                break;
            default: return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
        }
		if (written > 0) getCircularBuffer().write(this.buffer, 0, written);
        return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
    }

}

package io.github.jseproject;

import net.sourceforge.lame.GlobalFlags;
import net.sourceforge.lame.ID3Tag;
import net.sourceforge.lame.LAME;
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
import java.util.Map;

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
        return new Mp3AudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((Mp3FileFormatType) fileType).properties);
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

        Object qualityObject = params.getProperties().get("quality");
        float quality = qualityObject instanceof Float ? Math.min(1, Math.max(0, (Float) qualityObject)) : -1;
        Object vbrObject = params.getProperties().get("vbr");
        boolean vbr = vbrObject instanceof Boolean ? (Boolean) vbrObject : true;
        Object vbrModeObject = params.getProperties().get("mp3.vbr.mode");
        int vbrMode = vbrModeObject instanceof Integer ? (Integer) vbrModeObject : -1;
        if (vbrMode < -1 || vbrMode > 4) vbrMode = -1;

        int channels = outputFormat.getChannels();
        int sampleRate = (int) outputFormat.getSampleRate();

        byte[] abBuffer = new byte[BUFFER_SIZE * 2 * 2]; // 2 channels, 2 bytes per sample
        short[] pcm = new short[2 * BUFFER_SIZE]; // 2 channels

        GlobalFlags gf = LAME.init();
        if (gf == null) throw new IOException("Failed to initialize MP3 encoder");
        ID3Tag.id3tag_init(gf);
        // set encoding parameters
        gf.set_num_channels(channels);
        gf.set_out_samplerate(sampleRate);
        if (vbr && vbrMode != 0) {
            /* if (gf.get_VBR() == LAME.vbr_off) */ gf.set_VBR(vbrMode == -1 ? LAME.vbr_default : vbrMode);
            gf.set_VBR_quality(10 * (1 - quality));
        }
        else {
            gf.set_VBR(LAME.vbr_off);
            gf.set_quality(quality < 0 ? 5 : Math.round(9 * (1 - quality)));
        }
        // end set encoding parameters
        gf.set_write_id3tag_automatic(false);
        int ret = LAME.init_params(gf);
        if (ret < 0) throw new IOException("Failed to initialize MP3 encoder");

        while (true) {
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
            int nBytesRead = audioInputStream.read(abBuffer);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
            if (nBytesRead == -1) break;
            ByteBuffer.wrap(abBuffer, 0, nBytesRead).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().get(pcm, 0, nBytesRead >> 1);
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
            ret = LAME.encode_buffer_interleaved(gf, pcm, (nBytesRead / channels) >> 1,
                    abBuffer, 0, abBuffer.length);
            // was our output buffer big enough?
            if (ret < 0) throw new IOException("Failed to encode MP3");
            out.write(abBuffer, 0, ret);
            nTotalWritten += ret;
            if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");
        }

        byte[] flushBuffer = new byte[128000];
        if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): begin");
        ret = LAME.encode_flush(gf, flushBuffer, flushBuffer.length);
        if (ret < 0) throw new IOException("Failed to encode MP3");
        out.write(flushBuffer, 0, ret);
        nTotalWritten += ret;
        if (TDebug.TraceAudioFileWriter) TDebug.out("write(byte[], int, int): end");

        ret = writeID3v1(gf, out);
        nTotalWritten += ret;

        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        LAME.close( gf );
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    private static int writeID3v1(GlobalFlags gf, OutputStream out) throws IOException {
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
        private final Map<String, Object> properties;
        public Mp3AudioOutputParams(AudioFormat format, long length, TNonSeekableDataOutputStream out, Map<String, Object> properties) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.properties = properties;
        }
        public OutputStream getOutputStream() {
            return out;
        }
        public Map<String, Object> getProperties() {
            return properties;
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

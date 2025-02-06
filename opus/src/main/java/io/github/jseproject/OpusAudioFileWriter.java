package io.github.jseproject;

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
import java.util.Map;

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
        return new OpusAudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((OpusFileFormatType) fileType).properties);
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
        Object applicationObject = params.getProperties().get("opus.application");
        int application = applicationObject instanceof Integer ? (Integer) applicationObject : -1;
        if (application < -1 || application > 3) application = -1;
        OpusEncoder encoder;
        try {
            encoder = new OpusEncoder(opusInfo.getSampleRate(), opusInfo.getNumChannels(), OpusApplication.values()[application < 0 ? 2 : application]);
        } catch (OpusException e) {
            throw new IOException(e.getMessage());
        }
        encoder.setBitrate(opusInfo.getSampleRate() * opusInfo.getNumChannels());
        Object signalObject = params.getProperties().get("opus.signal");
        int signal = signalObject instanceof Integer ? (Integer) signalObject : -1;
        if (signal < -1 || signal > 3) signal = -1;
        encoder.setSignalType(signal < 0 ? OpusSignal.OPUS_SIGNAL_MUSIC : OpusSignal.values()[signal]);

        Object qualityObject = params.getProperties().get("quality");
        float quality = qualityObject instanceof Float ? Math.min(1, Math.max(0, (Float) qualityObject)) : -1;
        encoder.setComplexity(quality < 0 ? 10 : Math.round(10 * quality));
        Object vbrObject = params.getProperties().get("vbr");
        if (vbrObject instanceof Boolean) {
            encoder.setUseVBR((Boolean) vbrObject);
            Object vbrConstrainedObject = params.getProperties().get("opus.vbr.constrained");
            if (vbrConstrainedObject instanceof Boolean) encoder.setUseConstrainedVBR((Boolean) vbrConstrainedObject);
        }

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
        private final Map<String, Object> properties;
        public OpusAudioOutputParams(AudioFormat format, long length, OutputStream out, Map<String, Object> properties) {
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

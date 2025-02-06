package io.github.jseproject;

import com.beatofthedrum.wv.Defines;
import com.beatofthedrum.wv.WavPackConfig;
import com.beatofthedrum.wv.WavPackContext;
import com.beatofthedrum.wv.WavPackUtils;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

public class WavPackAudioFileWriter extends TAudioFileWriter {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[] {
            new AudioFormat(6000.0f, 8, 1, false, false),
            new AudioFormat(6000.0f, 8, 1, false, true),
            new AudioFormat(6000.0f, 8, 2, false, false),
            new AudioFormat(6000.0f, 8, 2, false, true),
            new AudioFormat(6000.0f, 16, 1, true, false),
            new AudioFormat(6000.0f, 16, 1, true, true),
            new AudioFormat(6000.0f, 16, 2, true, false),
            new AudioFormat(6000.0f, 16, 2, true, true),
            new AudioFormat(6000.0f, 24, 1, false, false),
            new AudioFormat(6000.0f, 24, 1, false, true),
            new AudioFormat(6000.0f, 24, 2, false, false),
            new AudioFormat(6000.0f, 24, 2, false, true),
            new AudioFormat(6000.0f, 32, 1, true, false),
            new AudioFormat(6000.0f, 32, 1, true, true),
            new AudioFormat(6000.0f, 32, 2, true, false),
            new AudioFormat(6000.0f, 32, 2, true, true),
            new AudioFormat(8000.0f, 8, 1, false, false),
            new AudioFormat(8000.0f, 8, 1, false, true),
            new AudioFormat(8000.0f, 8, 2, false, false),
            new AudioFormat(8000.0f, 8, 2, false, true),
            new AudioFormat(8000.0f, 16, 1, true, false),
            new AudioFormat(8000.0f, 16, 1, true, true),
            new AudioFormat(8000.0f, 16, 2, true, false),
            new AudioFormat(8000.0f, 16, 2, true, true),
            new AudioFormat(8000.0f, 24, 1, false, false),
            new AudioFormat(8000.0f, 24, 1, false, true),
            new AudioFormat(8000.0f, 24, 2, false, false),
            new AudioFormat(8000.0f, 24, 2, false, true),
            new AudioFormat(8000.0f, 32, 1, true, false),
            new AudioFormat(8000.0f, 32, 1, true, true),
            new AudioFormat(8000.0f, 32, 2, true, false),
            new AudioFormat(8000.0f, 32, 2, true, true),
            new AudioFormat(9600.0f, 8, 1, false, false),
            new AudioFormat(9600.0f, 8, 1, false, true),
            new AudioFormat(9600.0f, 8, 2, false, false),
            new AudioFormat(9600.0f, 8, 2, false, true),
            new AudioFormat(9600.0f, 16, 1, true, false),
            new AudioFormat(9600.0f, 16, 1, true, true),
            new AudioFormat(9600.0f, 16, 2, true, false),
            new AudioFormat(9600.0f, 16, 2, true, true),
            new AudioFormat(9600.0f, 24, 1, false, false),
            new AudioFormat(9600.0f, 24, 1, false, true),
            new AudioFormat(9600.0f, 24, 2, false, false),
            new AudioFormat(9600.0f, 24, 2, false, true),
            new AudioFormat(9600.0f, 32, 1, true, false),
            new AudioFormat(9600.0f, 32, 1, true, true),
            new AudioFormat(9600.0f, 32, 2, true, false),
            new AudioFormat(9600.0f, 32, 2, true, true),
            new AudioFormat(11025.0f, 8, 1, false, false),
            new AudioFormat(11025.0f, 8, 1, false, true),
            new AudioFormat(11025.0f, 8, 2, false, false),
            new AudioFormat(11025.0f, 8, 2, false, true),
            new AudioFormat(11025.0f, 16, 1, true, false),
            new AudioFormat(11025.0f, 16, 1, true, true),
            new AudioFormat(11025.0f, 16, 2, true, false),
            new AudioFormat(11025.0f, 16, 2, true, true),
            new AudioFormat(11025.0f, 24, 1, false, false),
            new AudioFormat(11025.0f, 24, 1, false, true),
            new AudioFormat(11025.0f, 24, 2, false, false),
            new AudioFormat(11025.0f, 24, 2, false, true),
            new AudioFormat(11025.0f, 32, 1, true, false),
            new AudioFormat(11025.0f, 32, 1, true, true),
            new AudioFormat(11025.0f, 32, 2, true, false),
            new AudioFormat(11025.0f, 32, 2, true, true),
            new AudioFormat(12000.0f, 8, 1, false, false),
            new AudioFormat(12000.0f, 8, 1, false, true),
            new AudioFormat(12000.0f, 8, 2, false, false),
            new AudioFormat(12000.0f, 8, 2, false, true),
            new AudioFormat(12000.0f, 16, 1, true, false),
            new AudioFormat(12000.0f, 16, 1, true, true),
            new AudioFormat(12000.0f, 16, 2, true, false),
            new AudioFormat(12000.0f, 16, 2, true, true),
            new AudioFormat(12000.0f, 24, 1, false, false),
            new AudioFormat(12000.0f, 24, 1, false, true),
            new AudioFormat(12000.0f, 24, 2, false, false),
            new AudioFormat(12000.0f, 24, 2, false, true),
            new AudioFormat(12000.0f, 32, 1, true, false),
            new AudioFormat(12000.0f, 32, 1, true, true),
            new AudioFormat(12000.0f, 32, 2, true, false),
            new AudioFormat(12000.0f, 32, 2, true, true),
            new AudioFormat(16000.0f, 8, 1, false, false),
            new AudioFormat(16000.0f, 8, 1, false, true),
            new AudioFormat(16000.0f, 8, 2, false, false),
            new AudioFormat(16000.0f, 8, 2, false, true),
            new AudioFormat(16000.0f, 16, 1, true, false),
            new AudioFormat(16000.0f, 16, 1, true, true),
            new AudioFormat(16000.0f, 16, 2, true, false),
            new AudioFormat(16000.0f, 16, 2, true, true),
            new AudioFormat(16000.0f, 24, 1, false, false),
            new AudioFormat(16000.0f, 24, 1, false, true),
            new AudioFormat(16000.0f, 24, 2, false, false),
            new AudioFormat(16000.0f, 24, 2, false, true),
            new AudioFormat(16000.0f, 32, 1, true, false),
            new AudioFormat(16000.0f, 32, 1, true, true),
            new AudioFormat(16000.0f, 32, 2, true, false),
            new AudioFormat(16000.0f, 32, 2, true, true),
            new AudioFormat(22050.0f, 8, 1, false, false),
            new AudioFormat(22050.0f, 8, 1, false, true),
            new AudioFormat(22050.0f, 8, 2, false, false),
            new AudioFormat(22050.0f, 8, 2, false, true),
            new AudioFormat(22050.0f, 16, 1, true, false),
            new AudioFormat(22050.0f, 16, 1, true, true),
            new AudioFormat(22050.0f, 16, 2, true, false),
            new AudioFormat(22050.0f, 16, 2, true, true),
            new AudioFormat(22050.0f, 24, 1, false, false),
            new AudioFormat(22050.0f, 24, 1, false, true),
            new AudioFormat(22050.0f, 24, 2, false, false),
            new AudioFormat(22050.0f, 24, 2, false, true),
            new AudioFormat(22050.0f, 32, 1, true, false),
            new AudioFormat(22050.0f, 32, 1, true, true),
            new AudioFormat(22050.0f, 32, 2, true, false),
            new AudioFormat(22050.0f, 32, 2, true, true),
            new AudioFormat(24000.0f, 8, 1, false, false),
            new AudioFormat(24000.0f, 8, 1, false, true),
            new AudioFormat(24000.0f, 8, 2, false, false),
            new AudioFormat(24000.0f, 8, 2, false, true),
            new AudioFormat(24000.0f, 16, 1, true, false),
            new AudioFormat(24000.0f, 16, 1, true, true),
            new AudioFormat(24000.0f, 16, 2, true, false),
            new AudioFormat(24000.0f, 16, 2, true, true),
            new AudioFormat(24000.0f, 24, 1, false, false),
            new AudioFormat(24000.0f, 24, 1, false, true),
            new AudioFormat(24000.0f, 24, 2, false, false),
            new AudioFormat(24000.0f, 24, 2, false, true),
            new AudioFormat(24000.0f, 32, 1, true, false),
            new AudioFormat(24000.0f, 32, 1, true, true),
            new AudioFormat(24000.0f, 32, 2, true, false),
            new AudioFormat(24000.0f, 32, 2, true, true),
            new AudioFormat(32000.0f, 8, 1, false, false),
            new AudioFormat(32000.0f, 8, 1, false, true),
            new AudioFormat(32000.0f, 8, 2, false, false),
            new AudioFormat(32000.0f, 8, 2, false, true),
            new AudioFormat(32000.0f, 16, 1, true, false),
            new AudioFormat(32000.0f, 16, 1, true, true),
            new AudioFormat(32000.0f, 16, 2, true, false),
            new AudioFormat(32000.0f, 16, 2, true, true),
            new AudioFormat(32000.0f, 24, 1, false, false),
            new AudioFormat(32000.0f, 24, 1, false, true),
            new AudioFormat(32000.0f, 24, 2, false, false),
            new AudioFormat(32000.0f, 24, 2, false, true),
            new AudioFormat(32000.0f, 32, 1, true, false),
            new AudioFormat(32000.0f, 32, 1, true, true),
            new AudioFormat(32000.0f, 32, 2, true, false),
            new AudioFormat(32000.0f, 32, 2, true, true),
            new AudioFormat(44100.0f, 8, 1, false, false),
            new AudioFormat(44100.0f, 8, 1, false, true),
            new AudioFormat(44100.0f, 8, 2, false, false),
            new AudioFormat(44100.0f, 8, 2, false, true),
            new AudioFormat(44100.0f, 16, 1, true, false),
            new AudioFormat(44100.0f, 16, 1, true, true),
            new AudioFormat(44100.0f, 16, 2, true, false),
            new AudioFormat(44100.0f, 16, 2, true, true),
            new AudioFormat(44100.0f, 24, 1, false, false),
            new AudioFormat(44100.0f, 24, 1, false, true),
            new AudioFormat(44100.0f, 24, 2, false, false),
            new AudioFormat(44100.0f, 24, 2, false, true),
            new AudioFormat(44100.0f, 32, 1, true, false),
            new AudioFormat(44100.0f, 32, 1, true, true),
            new AudioFormat(44100.0f, 32, 2, true, false),
            new AudioFormat(44100.0f, 32, 2, true, true),
            new AudioFormat(48000.0f, 8, 1, false, false),
            new AudioFormat(48000.0f, 8, 1, false, true),
            new AudioFormat(48000.0f, 8, 2, false, false),
            new AudioFormat(48000.0f, 8, 2, false, true),
            new AudioFormat(48000.0f, 16, 1, true, false),
            new AudioFormat(48000.0f, 16, 1, true, true),
            new AudioFormat(48000.0f, 16, 2, true, false),
            new AudioFormat(48000.0f, 16, 2, true, true),
            new AudioFormat(48000.0f, 24, 1, false, false),
            new AudioFormat(48000.0f, 24, 1, false, true),
            new AudioFormat(48000.0f, 24, 2, false, false),
            new AudioFormat(48000.0f, 24, 2, false, true),
            new AudioFormat(48000.0f, 32, 1, true, false),
            new AudioFormat(48000.0f, 32, 1, true, true),
            new AudioFormat(48000.0f, 32, 2, true, false),
            new AudioFormat(48000.0f, 32, 2, true, true),
            new AudioFormat(64000.0f, 8, 1, false, false),
            new AudioFormat(64000.0f, 8, 1, false, true),
            new AudioFormat(64000.0f, 8, 2, false, false),
            new AudioFormat(64000.0f, 8, 2, false, true),
            new AudioFormat(64000.0f, 16, 1, true, false),
            new AudioFormat(64000.0f, 16, 1, true, true),
            new AudioFormat(64000.0f, 16, 2, true, false),
            new AudioFormat(64000.0f, 16, 2, true, true),
            new AudioFormat(64000.0f, 24, 1, false, false),
            new AudioFormat(64000.0f, 24, 1, false, true),
            new AudioFormat(64000.0f, 24, 2, false, false),
            new AudioFormat(64000.0f, 24, 2, false, true),
            new AudioFormat(64000.0f, 32, 1, true, false),
            new AudioFormat(64000.0f, 32, 1, true, true),
            new AudioFormat(64000.0f, 32, 2, true, false),
            new AudioFormat(64000.0f, 32, 2, true, true),
            new AudioFormat(82000.0f, 8, 1, false, false),
            new AudioFormat(82000.0f, 8, 1, false, true),
            new AudioFormat(82000.0f, 8, 2, false, false),
            new AudioFormat(82000.0f, 8, 2, false, true),
            new AudioFormat(82000.0f, 16, 1, true, false),
            new AudioFormat(82000.0f, 16, 1, true, true),
            new AudioFormat(82000.0f, 16, 2, true, false),
            new AudioFormat(82000.0f, 16, 2, true, true),
            new AudioFormat(82000.0f, 24, 1, false, false),
            new AudioFormat(82000.0f, 24, 1, false, true),
            new AudioFormat(82000.0f, 24, 2, false, false),
            new AudioFormat(82000.0f, 24, 2, false, true),
            new AudioFormat(82000.0f, 32, 1, true, false),
            new AudioFormat(82000.0f, 32, 1, true, true),
            new AudioFormat(82000.0f, 32, 2, true, false),
            new AudioFormat(82000.0f, 32, 2, true, true),
            new AudioFormat(96000.0f, 8, 1, false, false),
            new AudioFormat(96000.0f, 8, 1, false, true),
            new AudioFormat(96000.0f, 8, 2, false, false),
            new AudioFormat(96000.0f, 8, 2, false, true),
            new AudioFormat(96000.0f, 16, 1, true, false),
            new AudioFormat(96000.0f, 16, 1, true, true),
            new AudioFormat(96000.0f, 16, 2, true, false),
            new AudioFormat(96000.0f, 16, 2, true, true),
            new AudioFormat(96000.0f, 24, 1, false, false),
            new AudioFormat(96000.0f, 24, 1, false, true),
            new AudioFormat(96000.0f, 24, 2, false, false),
            new AudioFormat(96000.0f, 24, 2, false, true),
            new AudioFormat(96000.0f, 32, 1, true, false),
            new AudioFormat(96000.0f, 32, 1, true, true),
            new AudioFormat(96000.0f, 32, 2, true, false),
            new AudioFormat(96000.0f, 32, 2, true, true),
            new AudioFormat(192000.0f, 8, 1, false, false),
            new AudioFormat(192000.0f, 8, 1, false, true),
            new AudioFormat(192000.0f, 8, 2, false, false),
            new AudioFormat(192000.0f, 8, 2, false, true),
            new AudioFormat(192000.0f, 16, 1, true, false),
            new AudioFormat(192000.0f, 16, 1, true, true),
            new AudioFormat(192000.0f, 16, 2, true, false),
            new AudioFormat(192000.0f, 16, 2, true, true),
            new AudioFormat(192000.0f, 24, 1, false, false),
            new AudioFormat(192000.0f, 24, 1, false, true),
            new AudioFormat(192000.0f, 24, 2, false, false),
            new AudioFormat(192000.0f, 24, 2, false, true),
            new AudioFormat(192000.0f, 32, 1, true, false),
            new AudioFormat(192000.0f, 32, 1, true, true),
            new AudioFormat(192000.0f, 32, 2, true, false),
            new AudioFormat(192000.0f, 32, 2, true, true),
    };

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { WavPackFileFormatType.WAVPACK };

    public WavPackAudioFileWriter() {
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
        return new WavPackAudioOutputParams(audioFormat, lLengthInBytes, (TNonSeekableDataOutputStream) dataOutputStream, ((WavPackFileFormatType) fileType).properties);
    }

    @Override
    protected int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion) throws IOException {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out(">TAudioFileWriter.writeImpl(): called");
            TDebug.out("class: " + getClass().getName());
        }
        int	nTotalWritten = 0;
        AudioFormat	outputFormat = audioOutputStream.getFormat();

        WavPackAudioOutputParams params = (WavPackAudioOutputParams) audioOutputStream;
        OutputStream outputStream = params.getOutputStream();
        DataInputStream dataInputStream = new DataInputStream(audioInputStream);

        WavPackConfig config = new WavPackConfig();
        Object qualityObject = params.getProperties().get("quality");
        float quality = qualityObject instanceof Float ? Math.min(1, Math.max(0, (Float) qualityObject)) : -1;
        switch (Math.round((1 - Math.max(0, Math.min(1, quality))) * 3f)) {
            case 0: config.flags |= Defines.CONFIG_FAST_FLAG; break;
            case 2: config.flags |= Defines.CONFIG_HIGH_FLAG; break;
            case 3: config.flags |= Defines.CONFIG_VERY_HIGH_FLAG; break;
            default: return 0;
        }
        config.num_channels = outputFormat.getChannels();
        config.bits_per_sample = outputFormat.getSampleSizeInBits();
        config.bytes_per_sample = config.bits_per_sample / 8;
        config.sample_rate = (int) outputFormat.getSampleRate();

        WavPackContext context = WavPackUtils.OpenFileOutput(outputStream);

        long sampleLength = audioInputStream.getFrameLength();
        if (sampleLength != AudioSystem.NOT_SPECIFIED) sampleLength /= config.num_channels;
        WavPackUtils.SetConfiguration(context, config, sampleLength);

        // pack the audio portion of the file now
        packAudio(context, dataInputStream);

        try {
            dataInputStream.close(); // we're now done with input file, so close
        }
        catch (IOException ignored) {
        }

        // we're now done with any WavPack blocks, so flush any remaining data
        if (WavPackUtils.FlushSamples(context) == 0) throw new IOException(context.error_message);
        nTotalWritten = (int) WavPackUtils.GetOutFileLength(context);

        // At this point we're done writing to the output files. However, in some
        // situations we might have to back up and re-write the initial blocks.
        // Currently the only case is if we're ignoring length.
        if (WavPackUtils.GetNumSamples(context) != WavPackUtils.GetSampleIndex(context))
            throw new IOException("couldn't read all samples, file may be corrupt!!");

        if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
        // TODO: get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    // This function handles the actual audio data compression. It assumes that the
    // input file is positioned at the beginning of the audio data and that the
    // WavPack configuration has been set. This is where the conversion from RIFF
    // little-endian standard the executing processor's format is done.
    private static long packAudio(WavPackContext context, java.io.DataInputStream inFile) throws IOException {
        WavPackUtils.PackInit(context);

        int bytesPerFrame = WavPackUtils.GetBytesPerSample(context) * WavPackUtils.GetNumChannels(context);
        long remainingFrames = WavPackUtils.GetNumSamples(context);
        byte[] pcmBuffer = new byte[Defines.INPUT_SAMPLES * bytesPerFrame];
        long[] sampleBuffer = new long[(Defines.INPUT_SAMPLES * 4 * WavPackUtils.GetNumChannels(context))];

        while (true) {

            int bytesToRead;
            if (remainingFrames > Defines.INPUT_SAMPLES) bytesToRead = Defines.INPUT_SAMPLES * bytesPerFrame;
            else bytesToRead = (int) (remainingFrames * bytesPerFrame);

            remainingFrames -= (bytesToRead / bytesPerFrame);
            if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + bytesToRead);
            long bytesRead = doReadFile(inFile, pcmBuffer, bytesToRead);
            if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + bytesRead);
            long frames = bytesRead / bytesPerFrame;

            if (frames == 0) break;
            else if (frames > 0) {
                int sampleCount = (int) (frames * WavPackUtils.GetNumChannels(context));

                int loopBps = WavPackUtils.GetBytesPerSample(context);

                if (loopBps == 1) {
                    int intermalCount = 0;

                    while (sampleCount > 0) {
                        sampleBuffer[intermalCount] = (pcmBuffer[intermalCount] & 0xFF) - 128;
                        intermalCount ++;
                        sampleCount --;
                    }
                }
                else if (loopBps == 2) {
                    int dcounter = 0;
                    int scounter = 0;

                    while (sampleCount > 0) {
                        sampleBuffer[dcounter] = (pcmBuffer[scounter] & 0xff) | (pcmBuffer[scounter + 1] << 8);
                        scounter = scounter + 2;
                        dcounter ++;
                        sampleCount --;
                    }
                }
                else if (loopBps == 3) {
                    int dcounter = 0;
                    int scounter = 0;

                    while (sampleCount > 0) {
                        sampleBuffer[dcounter] = (pcmBuffer[scounter] & 0xff)
                                | ((pcmBuffer[scounter + 1] & 0xff) << 8) | (pcmBuffer[scounter + 2] << 16);
                        scounter = scounter + 3;
                        dcounter ++;
                        sampleCount --;
                    }
                }
            }

            context.byte_idx = 0; // new WAV buffer data so reset the buffer index to zero

            if (WavPackUtils.PackSamples(context, sampleBuffer, frames) == 0)
                throw new IOException(context.error_message);
        }

        if (WavPackUtils.FlushSamples(context) == 0) throw new IOException(context.error_message);
        return WavPackUtils.GetOutFileLength(context);
    }

    private static long doReadFile(java.io.DataInputStream inFile, byte[] outBuffer, int bytesToRead) {
        byte[] tempBuffer = new byte[(int) (bytesToRead + (long) 1)];
        int bytesRead;
        long totalBytesRead = 0;
        while (bytesToRead > 0) {
            try {
                bytesRead = inFile.read(tempBuffer, 0, bytesToRead);
            }
            catch (Exception e) {
                bytesRead = 0;
            }
            if (bytesRead > 0) {
                for (long i = 0; i < bytesToRead; i ++) {
                    outBuffer[(int) i] = tempBuffer[(int) i];
                }
                totalBytesRead += bytesRead;
                bytesToRead -= bytesRead;
            }
            else break;
        }
        return totalBytesRead;
    }

    private static class WavPackAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final OutputStream out;
        private final Map<String, Object> properties;
        public WavPackAudioOutputParams(AudioFormat format, long length, OutputStream out, Map<String, Object> properties) {
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

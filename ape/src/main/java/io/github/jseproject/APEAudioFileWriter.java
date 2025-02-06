package io.github.jseproject;

import davaguine.jmac.encoder.IAPECompress;
import davaguine.jmac.info.CompressionLevel;
import davaguine.jmac.info.InputSource;
import davaguine.jmac.info.WAVInputSource;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.util.APEException;
import davaguine.jmac.util.InputStreamIoFile;
import davaguine.jmac.util.IntegerPointer;
import davaguine.jmac.util.IoFile;
import org.tritonus.sampled.file.WaveTool;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.tritonus.share.sampled.file.TSeekableDataOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Map;

public class APEAudioFileWriter extends TAudioFileWriter {

    private static final AudioFormat[] SUPPORTED_FORMATS = new AudioFormat[] {
            // encoding, rate, bits, channels, frameSize, frameRate, big endian
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 1, 3, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 1, 3, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 2, 6, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 2, 6, AudioSystem.NOT_SPECIFIED, true)
    };

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] {
            APEFileFormatType.APE, APEFileFormatType.MAC
    };

    public APEAudioFileWriter() {
        super(Arrays.asList(TYPES), Arrays.asList(SUPPORTED_FORMATS));
    }

    @Override
    public int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion)
            throws IOException {
        if (TDebug.TraceAudioFileWriter) {
            TDebug.out(">TAudioFileWriter.writeImpl(): called");
            TDebug.out("class: " + getClass().getName());
        }
        //int nTotalWritten = 0;
        AudioFormat	outputFormat = audioOutputStream.getFormat();

        // TODO: handle case when frame size is unknown ?
        //int nBytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();

        // 2024-10-17: Edit for APE Encoder
        long frameLength = audioInputStream.getFrameLength();
        if (frameLength == AudioSystem.NOT_SPECIFIED)
            throw new IllegalArgumentException("Couldn't write APE file: frame length not specified");
        else if (frameLength < 0 || frameLength > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Couldn't write APE file: frame length too large");

        APEAudioOutputParams params = (APEAudioOutputParams) audioOutputStream;
        Object qualityObject = params.getProperties().get("quality");
        float quality = qualityObject instanceof Float ? Math.min(1, Math.max(0, (Float) qualityObject)) : -1;
        int compressionLevel = quality < 0 ? CompressionLevel.COMPRESSION_LEVEL_HIGH : 1000 * (1 + Math.round(4 * (1 - quality)));

        // declare the variables
        IAPECompress compressor = null;
        InputSource inputSource = null;

        try {
            IoFile ioInput = new InputStreamIoFile(getWAVFileStream(outputFormat, audioInputStream, (int) frameLength));
            IoFile ioOutput = new TDataOutputStreamIoFile(params.getTDataOutputStream());

            byte[] abBuffer = null;

            WaveFormat WaveFormatEx = new WaveFormat();

            // create the input source
            IntegerPointer pAudioBlocks = new IntegerPointer();
            pAudioBlocks.value = 0;
            IntegerPointer pHeaderBytes = new IntegerPointer();
            pHeaderBytes.value = 0;
            IntegerPointer pTerminatingBytes = new IntegerPointer();
            pTerminatingBytes.value = 0;
            inputSource = new WAVInputSource(
                    ioInput,
                    WaveFormatEx, pAudioBlocks,
                    pHeaderBytes, pTerminatingBytes);

            // create the compressor
            compressor = IAPECompress.CreateIAPECompress();

            // figure the audio bytes
            int audioBytes = pAudioBlocks.value * WaveFormatEx.nBlockAlign;

            // start the encoder
            if (pHeaderBytes.value > 0) abBuffer = new byte[pHeaderBytes.value];
            inputSource.GetHeaderData(abBuffer);
            compressor.StartEx(ioOutput, WaveFormatEx, audioBytes, compressionLevel, abBuffer, pHeaderBytes.value);

            // main loop
            int nBytesLeft = audioBytes;

            while (nBytesLeft > 0) {
                if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + compressor.GetIdealFrameBytes());
                int nBytesAdded = compressor.AddDataFromInputSource(inputSource, nBytesLeft);
                if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesAdded);

                nBytesLeft -= nBytesAdded;
            }

            if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + audioBytes + " bytes");

            // finalize the file
            if (pTerminatingBytes.value > 0) abBuffer = new byte[pTerminatingBytes.value];
            inputSource.GetTerminatingData(abBuffer);
            compressor.Finish(abBuffer, pTerminatingBytes.value, pTerminatingBytes.value);

            ioOutput.close();
            // TODO: get bytes written for header etc. from AudioOutputStream and add to nTotalWrittenBytes
            return audioBytes;
        }
        finally {
            // kill the compressor if we failed
            if (compressor != null) compressor.Kill();
            if (inputSource != null) inputSource.Close();
        }
    }

    @Override
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat, long lLengthInBytes, AudioFileFormat.Type fileType, TDataOutputStream dataOutputStream) throws IOException {
        return new APEAudioOutputParams(audioFormat, lLengthInBytes, dataOutputStream, ((APEFileFormatType) fileType).properties);
    }

    private static class APEAudioOutputParams implements AudioOutputStream {
        private final AudioFormat format;
        private final long length;
        private final TDataOutputStream out;
        private final Map<String, Object> properties;
        public APEAudioOutputParams(AudioFormat format, long length, TDataOutputStream out, Map<String, Object> properties) {
            this.format = format;
            this.length = length;
            this.out = out;
            this.properties = properties;
        }
        public TDataOutputStream getTDataOutputStream() {
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

    private static class TDataOutputStreamIoFile extends IoFile {
        private final TDataOutputStream out;
        private TDataOutputStreamIoFile(TDataOutputStream out) {
            this.out = out;
        }
        @Override
        public void mark(int readlimit) throws IOException {
            throw new APEException("Unsupported Method");
        }
        @Override
        public void reset() throws IOException {
            throw new APEException("Unsupported Method");
        }
        @Override
        public int read() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).read();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int read(byte[] b) throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).read(b);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int read(byte[] b, int offs, int len) throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).read(b, offs, len);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public void readFully(byte[] b) throws IOException {
            if (out instanceof TSeekableDataOutputStream) ((TSeekableDataOutputStream) out).readFully(b);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public void readFully(byte[] b, int offs, int len) throws IOException {
            if (out instanceof TSeekableDataOutputStream) ((TSeekableDataOutputStream) out).readFully(b, offs, len);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public void close() throws IOException {
            if (out instanceof TNonSeekableDataOutputStream) ((TNonSeekableDataOutputStream) out).flush();
            else out.close();
        }
        @Override
        public boolean readBoolean() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readBoolean();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public byte readByte() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readByte();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public char readChar() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readChar();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public double readDouble() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readDouble();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public float readFloat() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readFloat();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int readInt() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readInt();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public String readLine() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readLine();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public long readLong() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readLong();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public short readShort() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readShort();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int readUnsignedByte() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readUnsignedByte();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int readUnsignedShort() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readUnsignedShort();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public String readUTF() throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).readUTF();
            else throw new APEException("Unsupported Method");
        }
        @Override
        public int skipBytes(int n) throws IOException {
            if (out instanceof TSeekableDataOutputStream) return ((TSeekableDataOutputStream) out).skipBytes(n);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public long length() throws IOException {
            return out.length();
        }
        @Override
        public void seek(long pos) throws IOException {
            if (out.supportsSeek()) out.seek(pos);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public long getFilePointer() throws IOException {
            return out.getFilePointer();
        }
        @Override
        public void setLength(long newLength) throws IOException {
            if (out instanceof TSeekableDataOutputStream) ((TSeekableDataOutputStream) out).setLength(newLength);
            else throw new APEException("Unsupported Method");
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
        @Override
        public boolean isLocal() {
            return out.supportsSeek();
        }
        @Override
        public String getFilename() {
            return null;
        }
    }

    // Naoko: ported from JDK class com.sun.media.sound.WaveFileWriter

    private static final int RIFF_MAGIC = 1380533830;
    private static final int WAVE_MAGIC = 1463899717;
    private static final int FMT_MAGIC  = 0x666d7420;
    private static final int DATA_MAGIC = 0x64617461;
    private static final int STANDARD_HEADER_SIZE = 28;
    private static final int STANDARD_FMT_CHUNK_SIZE = 16;
    private static final int WAVE_FORMAT_PCM = 0x0001;

    private static InputStream getWAVFileStream(AudioFormat audioFormat, InputStream audioStream, int frameLength) throws IOException {
        // WAVE header fields
        int headerLength       = STANDARD_HEADER_SIZE + STANDARD_FMT_CHUNK_SIZE;
        short wav_type         = WaveTool.getFormatCode(audioFormat);
        short channels         = (short) audioFormat.getChannels();
        short sampleSizeInBits = (short) audioFormat.getSampleSizeInBits();
        int sampleRate         = (int) audioFormat.getSampleRate();
        int frameSizeInBytes   = audioFormat.getFrameSize();
        int avgBytesPerSec     = channels * sampleSizeInBits * sampleRate / 8;
        short blockAlign       = (short) ((sampleSizeInBits / 8) * channels);
        int dataLength         = frameLength * frameSizeInBytes;
        int riffLength         = dataLength + headerLength - 8;

        AudioFormat audioStreamFormat;
        AudioFormat.Encoding encoding;
        InputStream codedAudioStream = audioStream;

        // if audioStream is an AudioInputStream and we need to convert, do it here...
        if (audioStream instanceof AudioInputStream) {
            audioStreamFormat = ((AudioInputStream) audioStream).getFormat();
            encoding = audioStreamFormat.getEncoding();
            if (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) {
                if (sampleSizeInBits == 8) {
                    wav_type = WAVE_FORMAT_PCM;
                    // plug in the transcoder to convert from PCM_SIGNED to PCM_UNSIGNED
                    codedAudioStream = AudioSystem.getAudioInputStream(new AudioFormat(
                                    AudioFormat.Encoding.PCM_UNSIGNED,
                                    audioStreamFormat.getSampleRate(),
                                    audioStreamFormat.getSampleSizeInBits(),
                                    audioStreamFormat.getChannels(),
                                    audioStreamFormat.getFrameSize(),
                                    audioStreamFormat.getFrameRate(),
                                    false),
                            (AudioInputStream) audioStream);
                }
            }
            if ((AudioFormat.Encoding.PCM_SIGNED.equals(encoding) && audioStreamFormat.isBigEndian()) ||
                    (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding) && !audioStreamFormat.isBigEndian()) ||
                    (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding) && audioStreamFormat.isBigEndian())) {
                if (sampleSizeInBits != 8) {
                    wav_type = WAVE_FORMAT_PCM;
                    // plug in the transcoder to convert to PCM_SIGNED_LITTLE_ENDIAN
                    codedAudioStream = AudioSystem.getAudioInputStream(new AudioFormat(
                                    AudioFormat.Encoding.PCM_SIGNED,
                                    audioStreamFormat.getSampleRate(),
                                    audioStreamFormat.getSampleSizeInBits(),
                                    audioStreamFormat.getChannels(),
                                    audioStreamFormat.getFrameSize(),
                                    audioStreamFormat.getFrameRate(),
                                    false),
                            (AudioInputStream) audioStream);
                }
            }
        }

        // Now push the header into a stream, concat, and return the new SequenceInputStream
        byte[] header;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            // we write in little endian...
            dos.writeInt(RIFF_MAGIC);
            dos.writeInt(big2little(riffLength));
            dos.writeInt(WAVE_MAGIC);
            dos.writeInt(FMT_MAGIC);
            dos.writeInt(big2little(STANDARD_FMT_CHUNK_SIZE));
            dos.writeShort(big2littleShort(wav_type));
            dos.writeShort(big2littleShort(channels));
            dos.writeInt(big2little(sampleRate));
            dos.writeInt(big2little(avgBytesPerSec));
            dos.writeShort(big2littleShort(blockAlign));
            dos.writeShort(big2littleShort(sampleSizeInBits));
            dos.writeInt(DATA_MAGIC);
            dos.writeInt(big2little(dataLength));
            header = baos.toByteArray();
        }
        return new SequenceInputStream(new ByteArrayInputStream(header), codedAudioStream);
    }

    /**
     * big2little
     * Protected helper method to swap the order of bytes in a 32 bit int
     * @return 32 bits swapped value
     */
    private static int big2little(int i) {
        int b1, b2, b3, b4;
        b1 = (i & 0xFF) << 24;
        b2 = (i & 0xFF00) << 8;
        b3 = (i & 0xFF0000) >> 8;
        b4 = (i & 0xFF000000) >>> 24;
        i = b1 | b2 | b3 | b4;
        return i;
    }

    /**
     * big2little
     * Protected helper method to swap the order of bytes in a 16 bit short
     * @return 16 bits swapped value
     */
    private static short big2littleShort(short i) {
        short high, low;
        high = (short) ((i & 0xFF) << 8);
        low  = (short) ((i & 0xFF00) >>> 8);
        i = (short) (high | low);
        return i;
    }

}

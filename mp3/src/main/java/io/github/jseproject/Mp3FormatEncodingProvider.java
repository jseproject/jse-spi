package io.github.jseproject;

import javasound.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Mp3FormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] READER_ENCODINGS = new AudioFormat.Encoding[] {
            Mp3Encoding.MPEG1_L1, Mp3Encoding.MPEG2_L1, Mp3Encoding.MPEG2DOT5_L1,
            Mp3Encoding.MPEG1_L2, Mp3Encoding.MPEG2_L2, Mp3Encoding.MPEG2DOT5_L2,
            Mp3Encoding.MPEG1_L3, Mp3Encoding.MPEG2_L3, Mp3Encoding.MPEG2DOT5_L3
    };
    private static final AudioFormat.Encoding[] WRITER_ENCODINGS = new AudioFormat.Encoding[] {
           Mp3Encoding.MPEG1_L3, Mp3Encoding.MPEG2_L3, Mp3Encoding.MPEG2DOT5_L3
    };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return READER_ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        for (AudioFormat.Encoding e : READER_ENCODINGS) {
            if (e.equals(encoding)) return true;
        }
        return false;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return WRITER_ENCODINGS.clone();
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        for (AudioFormat.Encoding e : WRITER_ENCODINGS) {
            if (e.equals(encoding)) return true;
        }
        return false;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioFileFormat.Type fileType) {
        if (isWriterSupportedFileType(fileType)) return WRITER_ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return WRITER_ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding, AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return isWriterSupportedEncoding(encoding);
        else return false;
    }

    @Override
    public AudioFormat.Encoding getEncodingByFormatName(String name) {
        for (AudioFormat.Encoding encoding : READER_ENCODINGS) {
            if (encoding.toString().equalsIgnoreCase(name)) return encoding;
        }
        return null;
    }

    private static final AudioFileFormat.Type[] READER_TYPES = new AudioFileFormat.Type[] {
            Mp3FileFormatType.MP1, Mp3FileFormatType.MP2, Mp3FileFormatType.MP3
    };
    private static final AudioFileFormat.Type[] WRITER_TYPES = new AudioFileFormat.Type[] { Mp3FileFormatType.MP3 };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return READER_TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        for (AudioFileFormat.Type type : READER_TYPES) {
            if (type.equals(fileType)) return true;
        }
        return false;
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return WRITER_TYPES.clone();
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        return Mp3FileFormatType.MP3.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes(AudioInputStream stream) {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes(stream)) {
            if (isWriterSupportedFileType(type)) return WRITER_TYPES.clone();
        }
        return EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType, AudioInputStream stream) {
        return isWriterSupportedFileType(fileType) && AudioSystem.isFileTypeSupported(fileType, stream);
    }

    @Override
    public AudioFileFormat.Type getFileTypeByFormatName(String name) {
        for (AudioFileFormat.Type type : READER_TYPES) {
            if (type.toString().equals(name)) return type;
        }
        return null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        for (AudioFileFormat.Type type : READER_TYPES) {
            if (type.getExtension().equalsIgnoreCase(suffix)) return type;
        }
        return null;
    }

}

package io.github.jseproject;

import javasound.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class FlacFormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] ENCODINGS = new AudioFormat.Encoding[] { FlacEncoding.FLAC };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        return FlacEncoding.FLAC.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        return FlacEncoding.FLAC.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioFileFormat.Type fileType) {
        if (isWriterSupportedFileType(fileType)) return ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings(AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return ENCODINGS.clone();
        else return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding, AudioInputStream stream) {
        if (getWriterFileTypes(stream).length > 0) return isWriterSupportedEncoding(encoding);
        else return false;
    }

    @Override
    public AudioFormat.Encoding getEncodingByFormatName(String name) {
        if (FlacEncoding.FLAC.toString().equalsIgnoreCase(name)) return FlacEncoding.FLAC;
        else return null;
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] {
            FlacFileFormatType.FLAC, FlacFileFormatType.OGG_FLAC
    };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        for (AudioFileFormat.Type type : TYPES) {
            if (type.equals(fileType)) return true;
        }
        return false;
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        for (AudioFileFormat.Type type : TYPES) {
            if (type.equals(fileType)) return true;
        }
        return false;
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes(AudioInputStream stream) {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes(stream)) {
            if (isWriterSupportedFileType(type)) return TYPES.clone();
        }
        return EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType, AudioInputStream stream) {
        return isWriterSupportedFileType(fileType) && AudioSystem.isFileTypeSupported(fileType, stream);
    }

    @Override
    public AudioFileFormat.Type getFileTypeByFormatName(String name) {
        for (AudioFileFormat.Type type : TYPES) {
            if (type.toString().equals(name)) return type;
        }
        return null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        return "flac".equalsIgnoreCase(suffix) ? FlacFileFormatType.FLAC : null;
    }

}

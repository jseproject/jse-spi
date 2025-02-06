package io.github.jseproject;

import javasound.sampled.spi.FormatEncodingProvider;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SpeexFormatEncodingProvider extends FormatEncodingProvider {

    private static final AudioFormat.Encoding[] ENCODINGS = new AudioFormat.Encoding[] { SpeexEncoding.SPEEX };
    private static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];

    @Override
    public AudioFormat.Encoding[] getReaderEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isReaderSupportedEncoding(AudioFormat.Encoding encoding) {
        return SpeexEncoding.SPEEX.equals(encoding);
    }

    @Override
    public AudioFormat.Encoding[] getWriterEncodings() {
        return ENCODINGS.clone();
    }

    @Override
    public boolean isWriterSupportedEncoding(AudioFormat.Encoding encoding) {
        return SpeexEncoding.SPEEX.equals(encoding);
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
        if (SpeexEncoding.SPEEX.toString().equalsIgnoreCase(name)) return SpeexEncoding.SPEEX;
        else return null;
    }

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { SpeexFileFormatType.SPEEX };
    private static final AudioFileFormat.Type[] EMPTY_TYPE_ARRAY = new AudioFileFormat.Type[0];

    @Override
    public AudioFileFormat.Type[] getReaderFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isReaderSupportedFileType(AudioFileFormat.Type fileType) {
        return SpeexFileFormatType.SPEEX.equals(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getWriterFileTypes() {
        return TYPES.clone();
    }

    @Override
    public boolean isWriterSupportedFileType(AudioFileFormat.Type fileType) {
        return SpeexFileFormatType.SPEEX.equals(fileType);
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
        return SpeexFileFormatType.SPEEX.toString().equalsIgnoreCase(name) ? SpeexFileFormatType.SPEEX : null;
    }

    @Override
    public AudioFileFormat.Type getFileTypeBySuffix(String suffix) {
        return "spx".equalsIgnoreCase(suffix) ? SpeexFileFormatType.SPEEX : null;
    }

}

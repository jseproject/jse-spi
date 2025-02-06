package io.github.jseproject;

import javasound.sampled.spi.AudioCompressionWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class WavPackAudioCompressionWriter extends AudioCompressionWriter {

    private static final WavPackAudioFileWriter WRITER = new WavPackAudioFileWriter();

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { WavPackFileFormatType.WAVPACK };

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes() {
        return TYPES.clone();
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream) {
        return WRITER.getAudioFileTypes(stream);
    }

    @Override
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, Map<String, Object> properties, OutputStream out) throws IOException {
        return WRITER.write(stream, WavPackFileFormatType.withProperties(fileType, properties), out);
    }

    @Override
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, Map<String, Object> properties, File out) throws IOException {
        return WRITER.write(stream, WavPackFileFormatType.withProperties(fileType, properties), out);
    }

}

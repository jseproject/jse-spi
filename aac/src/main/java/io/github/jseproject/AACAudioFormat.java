package io.github.jseproject;

import net.sourceforge.jaad.aac.AACDecoderConfig;
import net.sourceforge.jaad.aac.SampleBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class AACAudioFormat extends AudioFormat {

    public static AACAudioFormat of(AACDecoderConfig config, SampleBuffer sampleBuffer, int bitrate, boolean vbr) {
        Map<String, Object> properties = new HashMap<>();
        int sampleRate = sampleBuffer.getSampleRate();
        int channels = sampleBuffer.getChannels();
        properties.put("bitrate", bitrate);
        properties.put("vbr", vbr);
        return new AACAudioFormat(
                AACEncoding.AAC,
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, config.getFrameLength(),
                AudioSystem.NOT_SPECIFIED, true,
                properties);
    }

    public static AACAudioFormat of(AACDecoderConfig config, SampleBuffer sampleBuffer) {
        int sampleRate = sampleBuffer.getSampleRate();
        int channels = sampleBuffer.getChannels();
        return new AACAudioFormat(
                AACEncoding.AAC,
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, config.getFrameLength(),
                AudioSystem.NOT_SPECIFIED, true);
    }

    public AACAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

    public AACAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

}

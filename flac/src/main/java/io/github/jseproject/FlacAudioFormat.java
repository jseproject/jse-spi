package io.github.jseproject;

import org.gagravarr.flac.FlacInfo;
import org.tritonus.share.sampled.TAudioFormat;

import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class FlacAudioFormat extends TAudioFormat {

    public static FlacAudioFormat of(FlacInfo flacInfo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", flacInfo.getBitsPerSample() * flacInfo.getSampleRate());
        //boolean vbr = flacInfo.getMaximumFrameSize() != flacInfo.getMinimumFrameSize();
        //properties.put("vbr", vbr);
        int frameSize = flacInfo.getMaximumFrameSize();
        //if (vbr || frameSize == 0) frameSize = AudioSystem.NOT_SPECIFIED;
        if (frameSize == 0) frameSize = AudioSystem.NOT_SPECIFIED;
        return new FlacAudioFormat(FlacEncoding.FLAC,
                flacInfo.getSampleRate(), flacInfo.getBitsPerSample(), flacInfo.getNumChannels(),
                frameSize,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public FlacAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

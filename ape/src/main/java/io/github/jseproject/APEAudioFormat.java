package io.github.jseproject;

import davaguine.jmac.decoder.IAPEDecompress;
import org.tritonus.share.sampled.TAudioFormat;

import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class APEAudioFormat extends TAudioFormat {

    public static APEAudioFormat of(IAPEDecompress decoder) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", decoder.getApeInfoDecompressAverageBitrate());
        properties.put("vbr", true);
        return new APEAudioFormat(APEEncoding.APE,
                decoder.getApeInfoSampleRate(),
                decoder.getApeInfoBitsPerSample(),
                decoder.getApeInfoChannels(),
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public APEAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

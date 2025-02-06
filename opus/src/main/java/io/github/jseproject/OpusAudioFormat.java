package io.github.jseproject;

import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusStatistics;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class OpusAudioFormat extends AudioFormat {

    public static OpusAudioFormat of(OpusInfo opusInfo, OpusStatistics statistics) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", (int) Math.round(statistics.getAverageAudioBitrate()));
        //boolean vbr = statistics.getMinPacketBytes() != statistics.getMaxPacketBytes()
        //        || statistics.getMinPacketDuration() != statistics.getMaxPacketDuration();
        //properties.put("vbr", vbr);
        return new OpusAudioFormat(OpusEncoding.OPUS,
                opusInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getNumChannels(), AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public static OpusAudioFormat of(OpusInfo opusInfo) {
        return new OpusAudioFormat(OpusEncoding.OPUS,
                opusInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                opusInfo.getNumChannels(), AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false);
    }

    public OpusAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

    public OpusAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

}

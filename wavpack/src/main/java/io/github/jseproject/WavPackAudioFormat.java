package io.github.jseproject;

import com.beatofthedrum.wv.WavPackContext;
import com.beatofthedrum.wv.WavPackUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class WavPackAudioFormat extends AudioFormat {

    public static WavPackAudioFormat of(WavPackContext context) {
        Map<String, Object> properties = new HashMap<>();
        int sampleRate = (int) WavPackUtils.GetSampleRate(context);
        properties.put("bitrate", WavPackUtils.GetBitsPerSample(context) * sampleRate);
        properties.put("vbr", false);
        return new WavPackAudioFormat(WavPackEncoding.WAVPACK,
                sampleRate, AudioSystem.NOT_SPECIFIED,
                WavPackUtils.GetReducedChannels(context), AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public WavPackAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

package io.github.jseproject;

import com.beatofthedrum.wv.WavPackContext;
import com.beatofthedrum.wv.WavPackUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class WavPackAudioFileFormat extends ExtendedAudioFileFormat {

    public static WavPackAudioFileFormat of(WavPackContext context) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("wavpack.channels", WavPackUtils.GetNumChannels(context));
        properties.put("wavpack.channels.reduced", WavPackUtils.GetReducedChannels(context));
        properties.put("wavpack.lossy.blocks", WavPackUtils.LossyBlocks(context) == 1);
        properties.put("wavpack.sample.size.bytes", WavPackUtils.GetBytesPerSample(context));
        properties.put("wavpack.duration.samples", WavPackUtils.GetNumSamples(context));
        properties.put("wavpack.frequency.hz", WavPackUtils.GetSampleRate(context));
        return new WavPackAudioFileFormat(WavPackFileFormatType.WAVPACK, WavPackAudioFormat.of(context),
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                properties);
    }

    public WavPackAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

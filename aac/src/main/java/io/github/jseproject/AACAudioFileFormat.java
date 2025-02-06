package io.github.jseproject;

import net.sourceforge.jaad.aac.AACDecoderConfig;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.ChannelConfiguration;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.Profile;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.aac.SampleFrequency;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

public class AACAudioFileFormat extends ExtendedAudioFileFormat {

    public static AACAudioFileFormat of(ADTSDemultiplexer demultiplexer) throws AACException {
        Decoder decoder = new Decoder(demultiplexer.getDecoderSpecificInfo());
        Map<String, Object> properties = new HashMap<>();
        SampleBuffer sampleBuffer = new SampleBuffer();
        AACDecoderConfig config = decoder.getConfig();
        properties.put("aac.corecoder", config.isDependsOnCoreCoder());
        if (config.isDependsOnCoreCoder()) properties.put("aac.corecoder.delay", config.getCoreCoderDelay());
        properties.put("aac.frame.size.bytes", config.getFrameLength());
        properties.put("aac.sbr", config.isSBRPresent());
        if (config.isSBRPresent()) properties.put("aac.sbr.downsampled", config.isSBRDownSampled());
        properties.put("aac.frame.small", config.isSmallFrameUsed());
        properties.put("aac.resilience.scalefactor", config.isScalefactorResilienceUsed());
        properties.put("aac.resilience.sectiondata", config.isSectionDataResilienceUsed());
        properties.put("aac.resilience.spectraldata", config.isSpectralDataResilienceUsed());
        SampleFrequency sampleFrequency = config.getSampleFrequency();
        properties.put("aac.frequency.hz", sampleFrequency.getFrequency());
        properties.put("aac.frequency.maximalprediction.sfb", sampleFrequency.getMaximalPredictionSFB());
        properties.put("aac.frequency.maximaltns.sfb", sampleFrequency.getMaximalTNS_SFB(false));
        properties.put("aac.frequency.maximaltns.sfb.short", sampleFrequency.getMaximalTNS_SFB(true));
        properties.put("aac.frequency.predictors", sampleFrequency.getPredictorCount());
        ChannelConfiguration channelConfiguration = config.getChannelConfiguration();
        properties.put("aac.channels", channelConfiguration.getChannelCount());
        properties.put("aac.channels.description", channelConfiguration.getDescription());
        Profile profile = config.getProfile();
        properties.put("aac.profile", profile.getDescription());
        BigDecimal totalBitrate = BigDecimal.ZERO;
        BigDecimal lastBitrate = null;
        BigDecimal currentBitrate;
        double minBitrate = Double.MAX_VALUE;
        double maxBitrate = Double.MIN_VALUE;
        double seconds = 0;
        int frameLength = 0;
        boolean vbr = false;
        try {
            while (true) {
                decoder.decodeFrame(demultiplexer.readNextFrame(), sampleBuffer);
                minBitrate = Math.min(minBitrate, sampleBuffer.getBitrate());
                maxBitrate = Math.max(maxBitrate, sampleBuffer.getBitrate());
                currentBitrate = BigDecimal.valueOf(sampleBuffer.getBitrate());
                if (!vbr) {
                    if (lastBitrate != null && !lastBitrate.equals(currentBitrate)) vbr = true;
                    lastBitrate = currentBitrate;
                }
                totalBitrate = totalBitrate.add(currentBitrate);
                seconds += sampleBuffer.getLength();
                frameLength ++;
            }
        }
        catch (IOException ignored) {
        }
        if (frameLength == 0) throw new AACException("no frames found");
        double averageBitrate = totalBitrate.divide(BigDecimal.valueOf(frameLength), MathContext.DECIMAL64).doubleValue();
        properties.put("duration", Math.round(seconds * 1_000_000L));
        properties.put("aac.duration.seconds", seconds);
        properties.put("aac.duration.frames", frameLength);
        properties.put("aac.bitrate.lower", minBitrate);
        properties.put("aac.bitrate.nominal", averageBitrate);
        properties.put("aac.bitrate.upper", maxBitrate);
        properties.put("aac.vbr", vbr);
        return new AACAudioFileFormat(AACFileFormatType.AAC,
                AACAudioFormat.of(config, sampleBuffer, (int) Math.round(averageBitrate), vbr),
                frameLength,
                AudioSystem.NOT_SPECIFIED,
                properties);
    }

    public AACAudioFileFormat(Type type, AudioFormat format, long frameLength, long byteLength, Map<String, Object> properties) {
        super(type, format, frameLength, byteLength, properties);
    }

}

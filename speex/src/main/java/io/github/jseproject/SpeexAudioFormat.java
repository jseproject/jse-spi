package io.github.jseproject;

import org.gagravarr.speex.SpeexInfo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class SpeexAudioFormat extends AudioFormat {

    public static SpeexAudioFormat of(SpeexInfo speexInfo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", speexInfo.getBitrate());
        boolean vbr = speexInfo.getVbr() == 1;
        properties.put("vbr", vbr);
        properties.put("speex.mode", speexInfo.getMode());
        properties.put("speex.mode.bitstreamversion", speexInfo.getModeBitstreamVersion());
        properties.put("speex.packet.frames", speexInfo.getFramesPerPacket());
        return new SpeexAudioFormat(SpeexEncoding.SPEEX,
                speexInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                speexInfo.getNumChannels(), vbr ? AudioSystem.NOT_SPECIFIED : speexInfo.getFrameSize(),
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public SpeexAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

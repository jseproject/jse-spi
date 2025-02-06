package io.github.jseproject;

import org.gagravarr.vorbis.VorbisInfo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.util.HashMap;
import java.util.Map;

public class VorbisAudioFormat extends AudioFormat {

    public static VorbisAudioFormat of(VorbisInfo vorbisInfo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", vorbisInfo.getBitrateNominal());
        properties.put("vbr", vorbisInfo.getBitrateLower() != vorbisInfo.getBitrateUpper());
        return new VorbisAudioFormat(VorbisEncoding.VORBIS,
                vorbisInfo.getSampleRate(), AudioSystem.NOT_SPECIFIED,
                vorbisInfo.getNumChannels(), AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public VorbisAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

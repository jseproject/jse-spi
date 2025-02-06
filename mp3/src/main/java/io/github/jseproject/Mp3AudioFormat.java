package io.github.jseproject;

import javazoom.jl.decoder.Header;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.HashMap;
import java.util.Map;

public class Mp3AudioFormat extends AudioFormat {

    private static final Encoding[][] ENCODINGS = new Encoding[][] {
            new Encoding[] { Mp3Encoding.MPEG2_L1, Mp3Encoding.MPEG2_L2, Mp3Encoding.MPEG2_L3 },
            new Encoding[] { Mp3Encoding.MPEG1_L1, Mp3Encoding.MPEG1_L2, Mp3Encoding.MPEG1_L3 },
            new Encoding[] { Mp3Encoding.MPEG2DOT5_L1, Mp3Encoding.MPEG2DOT5_L2, Mp3Encoding.MPEG2DOT5_L3 }
    };

    public static Mp3AudioFormat of(Header header) throws UnsupportedAudioFileException {
        int frameSize = header.calculate_framesize();
        if (frameSize < 0) throw new UnsupportedAudioFileException("Invalid frame size: " + frameSize);
        int sampleRate = header.frequency();
        float frameRate = (float) (1.0 / header.ms_per_frame() * 1000.0);
        if (frameRate < 0) throw new UnsupportedAudioFileException("Invalid frame rate: " + frameRate);
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", header.bitrate());
        boolean vbr = header.vbr();
        properties.put("vbr", vbr);
        int channels = header.mode() == 3 ? 1 : 2;
        return new Mp3AudioFormat(
                ENCODINGS[header.version()][header.layer() - 1],
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, vbr ? AudioSystem.NOT_SPECIFIED : frameSize,
                AudioSystem.NOT_SPECIFIED, false,
                properties);
    }

    public static Mp3AudioFormat of(float sampleRate, int channels, boolean bigEndian, Map<String, Object> properties) {
        Encoding encoding;
        switch ((int) sampleRate) {
            case 8000: case 11025: case 12000: encoding = Mp3Encoding.MPEG2DOT5_L3; break;
            case 16000: case 22025: case 24000: encoding = Mp3Encoding.MPEG2_L3; break;
            case 32000: case 44100: case 48000: encoding = Mp3Encoding.MPEG1_L3; break;
            default: throw new IllegalArgumentException("Unknown sample rate: " + sampleRate);
        }
        return new Mp3AudioFormat(encoding, sampleRate, -1, channels, -1, -1, bigEndian, properties);
    }

    public Mp3AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

package io.github.jseproject;

import org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class Mp3FormatConversionProvider extends TMatrixFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(Mp3Encoding.MPEG1_L1, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG1_L1, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG1_L1, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG1_L1, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG1_L1, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG1_L1, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG1_L1, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG1_L1, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG1_L1, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG1_L1, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG1_L1, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG1_L1, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG1_L1, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG1_L1, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG1_L1, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG1_L1, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG1_L1, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG1_L1, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG1_L2, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG1_L2, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG1_L2, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG1_L2, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG1_L2, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG1_L2, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG1_L2, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG1_L2, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG1_L2, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG1_L2, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG1_L2, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG1_L2, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG1_L2, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG1_L2, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG1_L2, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG1_L2, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG1_L2, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG1_L2, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG1_L3, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG1_L3, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG1_L3, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG1_L3, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG1_L3, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG1_L3, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG1_L3, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG1_L3, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG1_L3, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG1_L3, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG1_L3, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG1_L3, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG1_L3, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG1_L3, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG1_L3, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG1_L3, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG1_L3, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG1_L3, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2_L1, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2_L1, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2_L1, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2_L1, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2_L1, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2_L1, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2_L1, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2_L1, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2_L1, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2_L1, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2_L1, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2_L1, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2_L1, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2_L1, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2_L1, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2_L1, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2_L1, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2_L1, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2_L2, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2_L2, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2_L2, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2_L2, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2_L2, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2_L2, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2_L2, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2_L2, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2_L2, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2_L2, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2_L2, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2_L2, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2_L2, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2_L2, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2_L2, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2_L2, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2_L2, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2_L2, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2_L3, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2_L3, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2_L3, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2_L3, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2_L3, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2_L3, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2_L3, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2_L3, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2_L3, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2_L3, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2_L3, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2_L3, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2_L3, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2_L3, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2_L3, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2_L3, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2_L3, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2_L3, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, 12000.0f, -1, 2, -1, -1, false), // 41

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 32000.0f, -1, 1, -1, -1, false), // 0
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 32000.0f, -1, 2, -1, -1, false), // 1
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 44100.0f, -1, 1, -1, -1, false), // 2
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 44100.0f, -1, 2, -1, -1, false), // 3
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 48000.0f, -1, 1, -1, -1, false), // 4
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 48000.0f, -1, 2, -1, -1, false), // 5

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 16000.0f, -1, 1, -1, -1, false), // 18
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 16000.0f, -1, 2, -1, -1, false), // 19
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 22050.0f, -1, 1, -1, -1, false), // 20
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 22050.0f, -1, 2, -1, -1, false), // 21
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 24000.0f, -1, 1, -1, -1, false), // 22
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 24000.0f, -1, 2, -1, -1, false), // 23

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 8000.0f, -1, 1, -1, -1, false), // 36
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 8000.0f, -1, 2, -1, -1, false), // 37
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 11025.0f, -1, 1, -1, -1, false), // 38
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 11025.0f, -1, 2, -1, -1, false), // 39
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 12000.0f, -1, 1, -1, -1, false), // 40
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, 12000.0f, -1, 2, -1, -1, false), // 41
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(8000.0f, 16, 1, true, false), // 0
            new AudioFormat(8000.0f, 16, 1, true, true),  // 1
            new AudioFormat(8000.0f, 16, 2, true, false), // 2
            new AudioFormat(8000.0f, 16, 2, true, true),  // 3
            new AudioFormat(11025.0f, 16, 1, true, false), // 4
            new AudioFormat(11025.0f, 16, 1, true, true),  // 5
            new AudioFormat(11025.0f, 16, 2, true, false), // 6
            new AudioFormat(11025.0f, 16, 2, true, true),  // 7
            new AudioFormat(12000.0f, 16, 1, true, false), // 8
            new AudioFormat(12000.0f, 16, 1, true, true),  // 9
            new AudioFormat(12000.0f, 16, 2, true, false), // 10
            new AudioFormat(12000.0f, 16, 2, true, true),  // 11
            new AudioFormat(16000.0f, 16, 1, true, false), // 12
            new AudioFormat(16000.0f, 16, 1, true, true),  // 13
            new AudioFormat(16000.0f, 16, 2, true, false), // 14
            new AudioFormat(16000.0f, 16, 2, true, true),  // 15
            new AudioFormat(22050.0f, 16, 1, true, false), // 16
            new AudioFormat(22050.0f, 16, 1, true, true),  // 17
            new AudioFormat(22050.0f, 16, 2, true, false), // 18
            new AudioFormat(22050.0f, 16, 2, true, true),  // 19
            new AudioFormat(24000.0f, 16, 1, true, false), // 20
            new AudioFormat(24000.0f, 16, 1, true, true),  // 21
            new AudioFormat(24000.0f, 16, 2, true, false), // 22
            new AudioFormat(24000.0f, 16, 2, true, true),  // 23
            new AudioFormat(32000.0f, 16, 1, true, false), // 24
            new AudioFormat(32000.0f, 16, 1, true, true),  // 25
            new AudioFormat(32000.0f, 16, 2, true, false), // 26
            new AudioFormat(32000.0f, 16, 2, true, true),  // 27
            new AudioFormat(44100.0f, 16, 1, true, false), // 28
            new AudioFormat(44100.0f, 16, 1, true, true),  // 29
            new AudioFormat(44100.0f, 16, 2, true, false), // 30
            new AudioFormat(44100.0f, 16, 2, true, true),  // 31
            new AudioFormat(48000.0f, 16, 1, true, false), // 32
            new AudioFormat(48000.0f, 16, 1, true, true),  // 33
            new AudioFormat(48000.0f, 16, 2, true, false), // 34
            new AudioFormat(48000.0f, 16, 2, true, true),  // 35
    };

    private static final boolean t = true;
    private static final boolean f = false;

    private static final boolean[][] CONVERSIONS = new boolean[][] {
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
            new boolean[] {f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

            new boolean[] {t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
            new boolean[] {f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
            new boolean[] {f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
            new boolean[] {f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
            new boolean[] {f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
            new boolean[] {f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41
    };

    public Mp3FormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS), CONVERSIONS);
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof Mp3AudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedMp3AudioInputStream(targetFormat, (Mp3AudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}

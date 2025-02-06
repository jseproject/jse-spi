/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package io.github.jseproject;

import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.util.Arrays;

public class APEFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            // encoding, rate, bits, channels, frameSize, frameRate, big endian
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 8, 1, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 8, 2, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 16, 1, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 16, 2, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 24, 1, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(APEEncoding.APE, AudioSystem.NOT_SPECIFIED, 24, 2, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false)
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            // encoding, rate, bits, channels, frameSize, frameRate, big endian
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 1, 3, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 1, 3, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 2, 6, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 24, 2, 6, AudioSystem.NOT_SPECIFIED, true)
    };

    public APEFormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS));
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof APEAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedAPEAudioInputStream(targetFormat, (APEAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

}

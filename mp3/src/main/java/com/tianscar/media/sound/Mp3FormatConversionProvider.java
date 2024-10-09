/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2008 Christopher G. Jennings
 * Copyright (c) 1999-2010 JavaZOOM
 * Copyright (c) 1999 Mat McGowan
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1993-1994 Tobias Bading
 * Copyright (c) 1991 MPEG Software Simulation Group
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * - You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 *   MpegFormatConversionProvider.
 *
 * JavaZOOM : mp3spi@javazoom.net
 * 			  http://www.javazoom.net
 *
 * ---------------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --------------------------------------------------------------------------
 */

package com.tianscar.media.sound;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

public class Mp3FormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(Mp3Encoding.MPEG2_L1, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L1, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2_L1, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L1, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2_L2, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L2, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2_L2, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L2, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2_L3, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L3, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2_L3, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2_L3, -1.0f, -1, 2, -1, -1.0f, true),

            new AudioFormat(Mp3Encoding.MPEG1_L1, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L1, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG1_L1, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L1, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG1_L2, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L2, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG1_L2, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L2, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG1_L3, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L3, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG1_L3, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG1_L3, -1.0f, -1, 2, -1, -1.0f, true),

            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L1, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L2, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, -1.0f, -1, 1, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, -1.0f, -1, 2, -1, -1.0f, false),
            new AudioFormat(Mp3Encoding.MPEG2DOT5_L3, -1.0f, -1, 2, -1, -1.0f, true)
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, true),
    };

    public Mp3FormatConversionProvider() {
        super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS));
    }

    private static boolean checkSampleRate(AudioFormat format) {
        int sampleRate = (int) format.getSampleRate();
        return sampleRate >= 1 && sampleRate <= 48000;
    }

    @Override
    protected boolean isAllowedSourceFormat(AudioFormat sourceFormat) {
        return checkSampleRate(sourceFormat) && super.isAllowedSourceFormat(sourceFormat);
    }

    @Override
    protected boolean isAllowedTargetFormat(AudioFormat targetFormat) {
        if (!checkSampleRate(targetFormat)) return false;
        if (!isAllowedTargetEncoding(targetFormat.getEncoding())) return false;
        if (targetFormat.getSampleSizeInBits() != 16) return false;
        int channels = targetFormat.getChannels();
        if (channels != 1 && channels != 2) return false;
        return targetFormat.getFrameSize() == 2 * channels;
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        if (audioInputStream instanceof Mp3AudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedMp3AudioInputStream(targetFormat, (Mp3AudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">Mp3FormatConversionProvider.isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat):");
            TDebug.out("checking if conversion possible");
            TDebug.out("from: " + sourceFormat);
            TDebug.out("to: " + targetFormat);
        }
        for (AudioFormat handledFormat : getTargetFormats(targetFormat.getEncoding(), sourceFormat)) {
            if (TDebug.TraceAudioConverter) TDebug.out("checking against possible target format: " + handledFormat);
            if (handledFormat != null && AudioFormats.matches(handledFormat, targetFormat)
                    && isAllowedSourceFormat(sourceFormat) && isAllowedTargetFormat(targetFormat)) {
                if (TDebug.TraceAudioConverter) TDebug.out("<result=true");
                return true;
            }
        }
        if (TDebug.TraceAudioConverter) TDebug.out("<result=false");
        return false;
    }

}

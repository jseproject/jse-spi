/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.Arrays;

public class AACFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final AudioFormat[] INPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 1, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 2, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 3, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 4, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 5, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 6, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 7, -1, -1.0f, true),
            new AudioFormat(AACEncoding.AAC, -1.0f, -1, 8, -1, -1.0f, true)
    };

    private static final AudioFormat[] OUTPUT_FORMATS = new AudioFormat[] {
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, true),
    };

    public AACFormatConversionProvider() {
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
        if (audioInputStream instanceof AACAudioInputStream && isConversionSupported(targetFormat, audioInputStream.getFormat()))
            return new DecodedAACAudioInputStream(targetFormat, (AACAudioInputStream) audioInputStream);
        else throw new IllegalArgumentException("conversion not supported");
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">AACFormatConversionProvider.isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat):");
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

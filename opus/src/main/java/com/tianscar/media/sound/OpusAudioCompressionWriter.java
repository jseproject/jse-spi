/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2016 Logan Stromberg
 * Copyright (c) 2007-2008 CSIRO
 * Copyright (c) 2007-2011 Xiph.Org Foundation
 * Copyright (c) 2006-2011 Skype Limited
 * Copyright (c) 2003-2004 Mark Borgerding
 * Copyright (c) 2001-2011 Microsoft Corporation,
 *                         Jean-Marc Valin, Gregory Maxwell,
 *                         Koen Vos, Timothy B. Terriberry
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of Internet Society, IETF or IETF Trust, nor the
 * names of specific contributors, may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import javasound.enhancement.sampled.spi.AudioCompressionWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class OpusAudioCompressionWriter extends AudioCompressionWriter {

    private static final OpusAudioFileWriter WRITER = new OpusAudioFileWriter();

    private static final AudioFileFormat.Type[] TYPES = new AudioFileFormat.Type[] { OpusFileFormatType.OPUS };

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes() {
        return TYPES.clone();
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream) {
        return WRITER.getAudioFileTypes(stream);
    }

    @Override
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, float quality, OutputStream out) throws IOException, IllegalArgumentException {
        return WRITER.write(stream, OpusFileFormatType.withComplexity(fileType, Math.round(quality * 10)), out);
    }

    @Override
    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, float quality, File out) throws IOException, IllegalArgumentException {
        return WRITER.write(stream, OpusFileFormatType.withComplexity(fileType, Math.round(quality * 10)), out);
    }

}

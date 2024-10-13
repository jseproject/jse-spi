/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 1999-2010 The LAME Project
 * Copyright (c) 1999-2008 JavaZOOM
 * Copyright (c) 2001-2002 Naoki Shibata
 * Copyright (c) 2001 Jonathan Dee
 * Copyright (c) 2000-2017 Robert Hegemann
 * Copyright (c) 2000-2008 Gabriel Bouvigne
 * Copyright (c) 2000-2005 Alexander Leidinger
 * Copyright (c) 2000 Don Melton
 * Copyright (c) 1999-2005 Takehiro Tominaga
 * Copyright (c) 1999-2001 Mark Taylor
 * Copyright (c) 1999 Albert L. Faber
 * Copyright (c) 1988, 1993 Ron Mayer
 * Copyright (c) 1998 Michael Cheng
 * Copyright (c) 1997 Jeff Tsay
 * Copyright (c) 1995-1997 Michael Hipp
 * Copyright (c) 1993-1994 Tobias Bading,
 *                         Berlin University of Technology
 *
 * - This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * - This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * - You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.tianscar.media.sound;

import net.sourceforge.lame.Mpg123;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedMp3AudioInputStream extends TAudioInputStream {

    private byte[] singleByte = null;
    private Mpg123 decoder;
    public DecodedMp3AudioInputStream(AudioFormat outputFormat, Mp3AudioInputStream inputStream) {
        super(inputStream, outputFormat, AudioSystem.NOT_SPECIFIED);
        if (TDebug.TraceAudioConverter) TDebug.out(">DecodedMp3AudioInputStream(AudioFormat, AudioInputStream)");
        decoder = new Mpg123(outputFormat.getSampleSizeInBits(),
                outputFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED),
                format.isBigEndian());
        decoder.InitMP3();
        if (decoder.open(inputStream.getFilteredInputStream()) < 0) {
            decoder.ExitMP3();
            decoder = null;
            if (TDebug.TraceAudioConverter) TDebug.out("DecodedMp3AudioInputStream : Failed to initialize MP3 decoder");
        }
    }

    @Override
    public void close() throws IOException {
        if (decoder != null) decoder.ExitMP3();
        decoder = null;
        super.close();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        if (singleByte == null) singleByte = new byte[1];
        if (read(singleByte) <= 0) return -1; // we have a weird situation if read(byte[]) returns 0!
        else return ((int) singleByte[0]) & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return decoder.read(b, off, len);
    }

}

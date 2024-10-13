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

import javazoom.jl.decoder.Header;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.HashMap;
import java.util.Map;

public class Mp3AudioFormat extends AudioFormat {

    private static final AudioFormat.Encoding[][] ENCODINGS = new AudioFormat.Encoding[][] {
            new AudioFormat.Encoding[] { Mp3Encoding.MPEG2_L1, Mp3Encoding.MPEG2_L2, Mp3Encoding.MPEG2_L3 },
            new AudioFormat.Encoding[] { Mp3Encoding.MPEG1_L1, Mp3Encoding.MPEG1_L2, Mp3Encoding.MPEG1_L3 },
            new AudioFormat.Encoding[] { Mp3Encoding.MPEG2DOT5_L1, Mp3Encoding.MPEG2DOT5_L2, Mp3Encoding.MPEG2DOT5_L3 }
    };

    public static Mp3AudioFormat of(Header header) throws UnsupportedAudioFileException {
        int frameSize = header.calculate_framesize();
        if (frameSize < 0) throw new UnsupportedAudioFileException("Invalid frame size: " + frameSize);
        int sampleRate = header.frequency();
        float frameRate = (float) (1.0 / header.ms_per_frame() * 1000.0);
        if (frameRate < 0) throw new UnsupportedAudioFileException("Invalid frame rate: " + frameRate);
        Map<String, Object> properties = new HashMap<>();
        properties.put("bitrate", header.bitrate());
        properties.put("vbr", header.vbr());
        int channels = header.mode() == 3 ? 1 : 2;
        return new Mp3AudioFormat(
                ENCODINGS[header.version()][header.layer() - 1],
                sampleRate, AudioSystem.NOT_SPECIFIED,
                channels, AudioSystem.NOT_SPECIFIED,
                sampleRate, false,
                properties);
    }

    public Mp3AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, properties);
    }

}

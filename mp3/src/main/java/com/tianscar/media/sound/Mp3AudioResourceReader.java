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

package com.tianscar.media.sound;

import javasound.enhancement.sampled.spi.AudioResourceReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

public class Mp3AudioResourceReader extends AudioResourceReader {

    private static final Mp3AudioFileReader READER = new Mp3AudioFileReader();

    @Override
    public AudioFileFormat getAudioFileFormat(ClassLoader resourceLoader, String name) throws UnsupportedAudioFileException, IOException {
        try (InputStream stream = resourceLoader.getResourceAsStream(name)) {
            if (stream == null) throw new IOException("could not load resource \"" + name + "\" with ClassLoader \"" + resourceLoader + "\"");
            else return READER.getAudioFileFormat(stream, stream.available(), true);
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(ClassLoader resourceLoader, String name) throws UnsupportedAudioFileException, IOException {
        InputStream stream = resourceLoader.getResourceAsStream(name);
        if (stream == null) throw new IOException("could not load resource \"" + name + "\" with ClassLoader \"" + resourceLoader + "\"");
        else return READER.getAudioInputStream(stream);
    }

}

/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
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
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tianscar.media.sound;

import org.gagravarr.flac.FlacAudioFrame;
import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.flac.FlacMetadataBlock;
import org.gagravarr.flac.FlacNativeFile;
import org.gagravarr.flac.FlacOggInfo;
import org.gagravarr.flac.FlacTags;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioSetupHeader;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class FlacAudioFileReader extends TAudioFileReader {

    private static final int INITIAL_READ_LENGTH = 64000 * 8;
    private static final int MARK_LIMIT = INITIAL_READ_LENGTH + 1;

    public FlacAudioFileReader() {
        super(MARK_LIMIT);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (FlacFile flacFile = FlacFile.open(file)) {
            format = FlacAudioFileFormat.of(flacFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(URL): begin");
        AudioFileFormat format;
        try (FlacFile flacFile = FlacFile.open(url.openStream())) {
            format = FlacAudioFileFormat.of(flacFile);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(URL): end");
        return format;
    }

    private static FlacFile openFlacFile(InputStream stream) throws IOException {
        PushbackInputStream pis = new PushbackInputStream(stream, 4);
        byte[] header = new byte[4];
        IOUtils.readFully(pis, header);
        pis.unread(header);
        if (header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S') {
            return new FlacOggFile(new OggFile(pis));
        }
        else if (header[0] == 'f' && header[1] == 'L' && header[2] == 'a' && header[3] == 'C')
            return new FlacNativeFile(pis);
        else throw new IllegalArgumentException("File type not recognized");
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(InputStream): begin");
        AudioFileFormat format;
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            format = FlacAudioFileFormat.of(openFlacFile(inputStream));
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioFileFormat(InputStream): end");
        return format;
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(inputStream);
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(File): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            FlacFile flacFile = openFlacFile(inputStream);
            AudioFormat audioFormat = FlacAudioFormat.of(flacFile.getInfo());
            inputStream.reset();
            audioInputStream = new FlacAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED, flacFile instanceof FlacOggFile);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(File): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(URL): begin");
        AudioInputStream audioInputStream;
        InputStream inputStream = url.openStream();
        if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream, MARK_LIMIT);
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            FlacFile flacFile = openFlacFile(inputStream);
            AudioFormat audioFormat = FlacAudioFormat.of(flacFile.getInfo());
            inputStream.reset();
            audioInputStream = new FlacAudioInputStream(inputStream, audioFormat, AudioSystem.NOT_SPECIFIED, flacFile instanceof FlacOggFile);
        }
        catch (IllegalArgumentException | IOException e) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
            }
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(URL): end");
        return audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(InputStream): begin");
        AudioInputStream audioInputStream;
        inputStream.mark(INITIAL_READ_LENGTH);
        try {
            FlacFile flacFile = openFlacFile(inputStream);
            inputStream.reset();
            audioInputStream = new FlacAudioInputStream(inputStream, FlacAudioFormat.of(flacFile.getInfo()), AudioSystem.NOT_SPECIFIED, flacFile instanceof FlacOggFile);
        }
        catch (IllegalArgumentException | IOException e) {
            inputStream.reset();
            throw new UnsupportedAudioFileException(e.getMessage());
        }
        if (TDebug.TraceAudioFileReader) TDebug.out("FlacAudioFileReader.getAudioInputStream(InputStream): end");
        return audioInputStream;
    }

    @Override
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(inputStream);
    }

    private static class FlacOggFile extends FlacFile implements OggAudioHeaders {
        private OggFile ogg;
        private OggPacketReader r;
        private int sid = -1;
        private final FlacFirstOggPacket firstPacket;
        public FlacOggFile(OggFile ogg) throws IOException {
            this(ogg.getPacketReader());
            this.ogg = ogg;
        }
        private FlacOggFile(OggPacketReader r) throws IOException {
            this.r = r;

            OggPacket p;
            while ((p = r.getNextPacket()) != null) {
                if (p.isBeginningOfStream() && p.getData().length > 10) {
                    if (FlacFirstOggPacket.isFlacStream(p)) {
                        sid = p.getSid();
                        break;
                    }
                }
            }
            if (sid == -1) throw new IllegalArgumentException("Supplied File is not Speex");

            // First packet is special
            firstPacket = new FlacFirstOggPacket(p);
            info = firstPacket.getInfo();

            // Next must be the Tags (Comments)
            tags = new FlacTags(r.getNextPacketWithSid(sid));

            // Then continue until the last metadata
            otherMetadata = new ArrayList<>();
            while ((p = r.getNextPacketWithSid(sid)) != null) {
                FlacMetadataBlock block = FlacMetadataBlock.create(new ByteArrayInputStream(p.getData()));
                otherMetadata.add(block);
                if (block.isLastMetadataBlock()) break;
            }

            // Everything else should be audio data
        }
        public FlacFirstOggPacket getFirstPacket() {
            return firstPacket;
        }
        @Override
        public FlacAudioFrame getNextAudioPacket() throws IOException {
            OggPacket p;
            while ((p = r.getNextPacketWithSid(sid)) != null) {
                return new FlacAudioFrame(p.getData(), info);
            }
            return null;
        }
        @Override
        public void skipToGranule(long granulePosition) throws IOException {
            r.skipToGranulePosition(sid, granulePosition);
        }
        @Override
        public int getSid() {
            return sid;
        }
        @Override
        public OggStreamIdentifier.OggStreamType getType() {
            return OggStreamIdentifier.OGG_FLAC;
        }
        @Override
        public void close() throws IOException {
            if(r != null) {
                r = null;
                ogg.close();
                ogg = null;
            }
        }
        @Override
        public FlacOggInfo getInfo() {
            return (FlacOggInfo) info;
        }
        @Override
        public OggAudioSetupHeader getSetup() {
            return null;
        }
        public OggFile getOggFile() {
            return ogg;
        }
    }

}

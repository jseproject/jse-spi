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

package net.sourceforge.jaad.aac;

import net.sourceforge.jaad.aac.filterbank.FilterBank;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.IBitStream;
import net.sourceforge.jaad.aac.syntax.PCE;
import net.sourceforge.jaad.aac.syntax.SyntacticElements;
import net.sourceforge.jaad.aac.syntax.SyntaxConstants;
import net.sourceforge.jaad.aac.transport.ADIFHeader;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * Main AAC decoder class
 * 
 * @author in-somnia
 */
public class Decoder implements SyntaxConstants {
    private final AACDecoderConfig config;
    private final SyntacticElements syntacticElements;
    private final FilterBank filterBank;
    private IBitStream _in;
    private ADIFHeader adifHeader;

    /**
     * The methods returns true, if a profile is supported by the decoder.
     * 
     * @param profile
     *            an AAC profile
     * @return true if the specified profile can be decoded
     * @see Profile#isDecodingSupported()
     */
    public static boolean canDecode(Profile profile) {
        return profile.isDecodingSupported();
    }

    /**
     * Initializes the decoder with a MP4 decoder specific info.
     *
     * After this the MP4 frames can be passed to the
     * <code>decodeFrame(byte[], SampleBuffer)</code> method to decode them.
     * 
     * @param decoderSpecificInfo
     *            a byte array containing the decoder specific info from an MP4
     *            container
     * @throws AACException
     *             if the specified profile is not supported
     */
    public Decoder(byte[] decoderSpecificInfo) throws AACException {
        config = AACDecoderConfig.parseMP4DecoderSpecificInfo(decoderSpecificInfo);
        if (config == null)
            throw new IllegalArgumentException("illegal MP4 decoder specific info");

        if (!canDecode(config.getProfile()))
            throw new AACException("unsupported profile: " + config.getProfile().getDescription());

        syntacticElements = new SyntacticElements(config);
        filterBank = new FilterBank(config.isSmallFrameUsed(), config.getChannelConfiguration().getChannelCount());

        _in = new BitStream();

        Logger.debug("profile: {0}", config.getProfile());
        Logger.debug("sf: {0}", config.getSampleFrequency().getFrequency());
        Logger.debug("channels: {0}", config.getChannelConfiguration().getDescription());
    }

    public AACDecoderConfig getConfig() {
        return config;
    }

    /**
     * Decodes one frame of AAC data in frame mode and returns the raw PCM data.
     * 
     * @param frame
     *            the AAC frame
     * @param buffer
     *            a buffer to hold the decoded PCM data
     * @throws AACException
     *             if decoding fails
     */
    public void decodeFrame(byte[] frame, SampleBuffer buffer) throws AACException {
        if (frame != null)
            _in.setData(frame);
        Logger.debug("bits left " + _in.getBitsLeft());
        try {
            decode(buffer);
        } catch (AACException e) {
            if (!e.isEndOfStream())
                throw e;
            else
                Logger.warn("unexpected end of frame");
        }
    }

    private void decode(SampleBuffer buffer) throws AACException {
        if (ADIFHeader.isPresent(_in)) {
            adifHeader = ADIFHeader.readHeader(_in);
            final PCE pce = adifHeader.getFirstPCE();
            config.setProfile(pce.getProfile());
            config.setSampleFrequency(pce.getSampleFrequency());
            config.setChannelConfiguration(ChannelConfiguration.forInt(pce.getChannelCount()));
        }

        if (!canDecode(config.getProfile()))
            throw new AACException("unsupported profile: " + config.getProfile().getDescription());

        syntacticElements.startNewFrame();

        try {
            //1: bitstream parsing and noiseless coding
            syntacticElements.decode(_in);
            //2: spectral processing
            syntacticElements.process(filterBank);
            //3: send to output buffer
            syntacticElements.sendToOutput(buffer);
        } catch (Exception e) {
            buffer.setData(new byte[0], 0, 0, 0, 0);
            throw AACException.wrap(e);
        }
    }
}

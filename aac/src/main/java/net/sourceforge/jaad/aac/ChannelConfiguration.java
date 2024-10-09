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

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * All possible channel configurations for AAC.
 * 
 * @author in-somnia
 */
public final class ChannelConfiguration {

    public final static ChannelConfiguration CHANNEL_CONFIG_UNSUPPORTED = new ChannelConfiguration(-1, "invalid");
    public final static ChannelConfiguration CHANNEL_CONFIG_NONE = new ChannelConfiguration(0, "No channel");
    public final static ChannelConfiguration CHANNEL_CONFIG_MONO = new ChannelConfiguration(1, "Mono");
    public final static ChannelConfiguration CHANNEL_CONFIG_STEREO = new ChannelConfiguration(2, "Stereo");
    public final static ChannelConfiguration CHANNEL_CONFIG_STEREO_PLUS_CENTER = new ChannelConfiguration(3,
            "Stereo+Center");
    public final static ChannelConfiguration CHANNEL_CONFIG_STEREO_PLUS_CENTER_PLUS_REAR_MONO = new ChannelConfiguration(
            4, "Stereo+Center+Rear");
    public final static ChannelConfiguration CHANNEL_CONFIG_FIVE = new ChannelConfiguration(5, "Five channels");
    public final static ChannelConfiguration CHANNEL_CONFIG_FIVE_PLUS_ONE = new ChannelConfiguration(6,
            "Five channels+LF");
    public final static ChannelConfiguration CHANNEL_CONFIG_SEVEN_PLUS_ONE = new ChannelConfiguration(8,
            "Seven channels+LF");

    public static ChannelConfiguration forInt(int i) {
        ChannelConfiguration c;
        switch (i) {
        case 0:
            c = CHANNEL_CONFIG_NONE;
            break;
        case 1:
            c = CHANNEL_CONFIG_MONO;
            break;
        case 2:
            c = CHANNEL_CONFIG_STEREO;
            break;
        case 3:
            c = CHANNEL_CONFIG_STEREO_PLUS_CENTER;
            break;
        case 4:
            c = CHANNEL_CONFIG_STEREO_PLUS_CENTER_PLUS_REAR_MONO;
            break;
        case 5:
            c = CHANNEL_CONFIG_FIVE;
            break;
        case 6:
            c = CHANNEL_CONFIG_FIVE_PLUS_ONE;
            break;
        case 7:
        case 8:
            c = CHANNEL_CONFIG_SEVEN_PLUS_ONE;
            break;
        default:
            c = CHANNEL_CONFIG_UNSUPPORTED;
            break;
        }
        return c;
    }

    private final int chCount;
    private final String descr;

    private ChannelConfiguration(int chCount, String descr) {
        this.chCount = chCount;
        this.descr = descr;
    }

    /**
     * Returns the number of channels in this configuration.
     */
    public int getChannelCount() {
        return chCount;
    }

    /**
     * Returns a short description of this configuration.
     * 
     * @return the channel configuration's description
     */
    public String getDescription() {
        return descr;
    }

    /**
     * Returns a string representation of this channel configuration. The method
     * is identical to <code>getDescription()</code>.
     * 
     * @return the channel configuration's description
     */
    @Override
    public String toString() {
        return descr;
    }
}

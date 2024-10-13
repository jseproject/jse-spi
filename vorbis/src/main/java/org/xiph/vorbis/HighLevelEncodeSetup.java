/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 * Copyright (c) 1994-1996 James Gosling,
 *                         Kevin A. Smith, Sun Microsystems, Inc.
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
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.vorbis;

/**
 * highlevel encoder setup struct separated out for vorbisenc clarity
 */
class HighLevelEncodeSetup {
    int set_in_stone = 0;
    EncSetupDataTemplate setup = null;
    double base_setting = 0;

    double impulse_noisetune = 0;

    /* bitrate management below all settable */
    float req = 0;
    boolean managed = false;
    int bitrate_min = 0;
    int bitrate_av = 0;
    double bitrate_av_damp = 0;
    int bitrate_max = 0;
    int bitrate_reservoir = 0;
    double bitrate_reservoir_bias = 0;

    boolean impulse_block_p = false;
    boolean noise_normalize_p = false;
    boolean coupling_p = false;

    double stereo_point_setting = 0.0;
    double lowpass_kHz = 0.0;
    boolean lowpass_altered = false;

    double ath_floating_dB = 0.0;
    double ath_absolute_dB = 0.0;

    double amplitude_track_dBpersec = 0.0;
    double trigger_setting = 0.0;

    /**
     * padding, impulse, transition, long
     */
    final HighLevelByBlockType[] block = new HighLevelByBlockType[4];

    //
    HighLevelEncodeSetup() {
        for (int i = 0, ie = block.length; i < ie; i++) {
            block[i] = new HighLevelByBlockType();
        }
    }
}

/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
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
 * psychoacoustic setup
 */
public class InfoPsyGlobal {
    int eighth_octave_lines = 0;

    /* for block long/short tuning; encode only */
    final float[] preecho_thresh = new float[EnvelopeLookup.VE_BANDS];
    final float[] postecho_thresh = new float[EnvelopeLookup.VE_BANDS];
    float stretch_penalty = 0.0f;
    float preecho_minenergy = 0.0f;

    float ampmax_att_per_sec = 0.0f;

    /* channel coupling config */
    final int[] coupling_pkHz = new int[Info.PACKETBLOBS];
    final int[][] coupling_pointlimit = new int[2][Info.PACKETBLOBS];
    final int[] coupling_prepointamp = new int[Info.PACKETBLOBS];
    final int[] coupling_postpointamp = new int[Info.PACKETBLOBS];
    final int[][] sliding_lowpass = new int[2][Info.PACKETBLOBS];

    //
    InfoPsyGlobal() {
    }

    public InfoPsyGlobal(final int eol, final float[] pre_thresh, final float[] post_thresh,
                         final float penalty, final float preecho, final float ampmax,
                         final int[] pkHz, final int[][] pointlimit, final int[] prepointamp, final int[] postpointamp,
                         final int[][] lowpass) {
        eighth_octave_lines = eol;
        System.arraycopy(pre_thresh, 0, preecho_thresh, 0, pre_thresh.length);
        System.arraycopy(post_thresh, 0, postecho_thresh, 0, post_thresh.length);
        stretch_penalty = penalty;
        preecho_minenergy = preecho;
        ampmax_att_per_sec = ampmax;
        System.arraycopy(pkHz, 0, coupling_pkHz, 0, pkHz.length);
        for (int i = 0; i < pointlimit.length; i++) {
            System.arraycopy(pointlimit[i], 0, coupling_pointlimit[i], 0, pointlimit[i].length);
        }
        System.arraycopy(prepointamp, 0, coupling_prepointamp, 0, prepointamp.length);
        System.arraycopy(postpointamp, 0, coupling_postpointamp, 0, postpointamp.length);
        for (int i = 0; i < lowpass.length; i++) {
            System.arraycopy(lowpass[i], 0, sliding_lowpass[i], 0, lowpass[i].length);
        }
    }

    final void copyFrom(final InfoPsyGlobal g) {
        eighth_octave_lines = g.eighth_octave_lines;
        System.arraycopy(g.preecho_thresh, 0, preecho_thresh, 0, preecho_thresh.length);
        System.arraycopy(g.postecho_thresh, 0, postecho_thresh, 0, postecho_thresh.length);
        stretch_penalty = g.stretch_penalty;
        preecho_minenergy = g.preecho_minenergy;
        ampmax_att_per_sec = g.ampmax_att_per_sec;
        System.arraycopy(g.coupling_pkHz, 0, coupling_pkHz, 0, coupling_pkHz.length);
        for (int i = 0; i < coupling_pointlimit.length; i++) {
            System.arraycopy(g.coupling_pointlimit[i], 0, coupling_pointlimit[i], 0, coupling_pointlimit.length);
        }
        System.arraycopy(g.coupling_prepointamp, 0, coupling_prepointamp, 0, coupling_prepointamp.length);
        System.arraycopy(g.coupling_postpointamp, 0, coupling_postpointamp, 0, coupling_postpointamp.length);
        for (int i = 0; i < sliding_lowpass.length; i++) {
            System.arraycopy(g.sliding_lowpass[i], 0, sliding_lowpass[i], 0, sliding_lowpass.length);
        }
    }
}

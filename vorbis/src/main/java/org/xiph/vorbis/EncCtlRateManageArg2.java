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
 * struct ovectl_ratemanage2_arg
 * <p>
 * The ovectl_ratemanage2_arg structure is used with vorbis_encode_ctl() and
 * the OV_ECTL_RATEMANAGE2_GET and OV_ECTL_RATEMANAGE2_SET calls in order to
 * query and modify specifics of the encoder's bitrate management
 * configuration.
 */
public class EncCtlRateManageArg2 {
    /**
     * nonzero if bitrate management is active
     */
    public boolean management_active = false;
    /**
     * Lower allowed bitrate limit in kilobits per second
     */
    public int bitrate_limit_min_kbps = 0;
    /**
     * Upper allowed bitrate limit in kilobits per second
     */
    public int bitrate_limit_max_kbps = 0;
    /**
     * Size of the bitrate reservoir in bits
     */
    public int bitrate_limit_reservoir_bits = 0;
    /**
     * Regulates the bitrate reservoir's preferred fill level in a range from 0.0
     * to 1.0; 0.0 tries to bank bits to buffer against future bitrate spikes, 1.0
     * buffers against future sudden drops in instantaneous bitrate. Default is
     * 0.1
     */
    public double bitrate_limit_reservoir_bias = 0.0;
    /**
     * Average bitrate setting in kilobits per second
     */
    public int bitrate_average_kbps = 0;
    /**
     * Slew rate limit setting for average bitrate adjustment; sets the minimum
     * time in seconds the bitrate tracker may swing from one extreme to the
     * other when boosting or damping average bitrate.
     */
    public double bitrate_average_damping = 0.0;
}

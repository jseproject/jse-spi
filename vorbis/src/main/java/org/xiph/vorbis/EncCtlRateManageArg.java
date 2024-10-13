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
 * @deprecated This is a deprecated interface. Please use vorbis_encode_ctl()
 * with the {@link EncCtlRateManageArg2} struct and
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE2_GET} and
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE2_SET} calls in new code.
 * <p>
 * The {@link EncCtlRateManageArg} structure is used with vorbis_encode_ctl()
 * and the {@link EncCtlCodes#OV_ECTL_RATEMANAGE_GET},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_SET},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_AVG},
 * {@link EncCtlCodes#OV_ECTL_RATEMANAGE_HARD} calls in order to
 * query and modify specifics of the encoder's bitrate management
 * configuration.
 */
@Deprecated
public class EncCtlRateManageArg {
    /**
     * < nonzero if bitrate management is active
     */
    public boolean management_active = false;
    /**
     * hard lower limit (in kilobits per second) below which the stream bitrate
     * will never be allowed for any given bitrate_hard_window seconds of time.
     */
    public int bitrate_hard_min = 0;
    /**
     * hard upper limit (in kilobits per second) above which the stream bitrate
     * will never be allowed for any given bitrate_hard_window seconds of time.
     */
    public int bitrate_hard_max = 0;
    /**
     * the window period (in seconds) used to regulate the hard bitrate minimum
     * and maximum
     */
    public double bitrate_hard_window = 0.0;
    /**
     * soft lower limit (in kilobits per second) below which the average bitrate
     * tracker will start nudging the bitrate higher.
     */
    public int bitrate_av_lo = 0;
    /**
     * soft upper limit (in kilobits per second) above which the average bitrate
     * tracker will start nudging the bitrate lower.
     */
    public int bitrate_av_hi = 0;
    /**
     * the window period (in seconds) used to regulate the average bitrate
     * minimum and maximum.
     */
    public double bitrate_av_window = 0.0;
    /**
     * Regulates the relative centering of the average and hard windows; in
     * libvorbis 1.0 and 1.0.1, the hard window regulation overlapped but
     * followed the average window regulation. In libvorbis 1.1 a bit-reservoir
     * interface replaces the old windowing interface; the older windowing
     * interface is simulated and this field has no effect.
     */
    public double bitrate_av_window_center = 0.0;
}

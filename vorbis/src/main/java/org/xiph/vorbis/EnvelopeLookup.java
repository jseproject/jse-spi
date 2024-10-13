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
 * PCM data envelope analysis and manipulation
 */
class EnvelopeLookup {
    private static final int VE_PRE = 16;
    static final int VE_WIN = 4;
    static final int VE_POST = 2;
    private static final int VE_AMP = (VE_PRE + VE_POST - 1);
    //
    static final int VE_BANDS = 7;
    private static final int VE_NEARDC = 15;
    /**
     * a bit less than short block
     */
    private static final int VE_MINSTRETCH = 2;
    /**
     * one-third full block
     */
    static final int VE_MAXSTRETCH = 12;

    //
    private static class EnvelopeFilterState {
        private final float[] ampbuf = new float[VE_AMP];
        private int ampptr;

        private final float[] nearDC = new float[VE_NEARDC];
        private float nearDC_acc;
        private float nearDC_partialacc;
        private int nearptr;
    }

    //
    private static class EnvelopeBand {
        private int begin;
        private int end;
        private float[] window;
        private float total;
    }

    //
    int ch = 0;
    private int winlength = 0;
    int searchstep = 0;
    private float minenergy = 0.0f;

    private final MDCTLookup mdct = new MDCTLookup();
    private float[] mdct_win = null;

    final EnvelopeBand[] band = new EnvelopeBand[VE_BANDS];
    EnvelopeFilterState[] filter = null;
    int m_stretch = 0;

    boolean[] mark = null;

    int storage = 0;
    int current = 0;
    int curmark = 0;
    int cursor = 0;

    /*#if 0
        private static int seq = 0;
        private static long totalshift = -1024;
    #endif */
    //
    EnvelopeLookup() {
        for (int i = 0; i < VE_BANDS; i++) {
            band[i] = new EnvelopeBand();
        }
    }

    private final void m_clear() {
        ch = 0;
        winlength = 0;
        searchstep = 0;
        minenergy = 0.0f;

        mdct.clear();
        mdct_win = null;

        for (int i = 0; i < VE_BANDS; i++) {
            band[i] = null;
        }
        filter = null;
        m_stretch = 0;

        mark = null;

        storage = 0;
        current = 0;
        curmark = 0;
        cursor = 0;
    }

    // envelope.c
    final void _init(final Info vi) {
        final CodecSetupInfo ci = vi.codec_setup;
        final InfoPsyGlobal gi = ci.psy_g_param;
        final int channels = vi.channels;
        int n = this.winlength = 128;
        this.searchstep = 64; /* not random */

        this.minenergy = gi.preecho_minenergy;
        this.ch = channels;
        this.storage = 128;
        this.cursor = ci.blocksizes[1] >> 1;
        float[] buff = new float[n];// java
        this.mdct_win = buff;
        this.mdct.init(n);

        for (int i = 0; i < n; i++) {
            final float s = (float) Math.sin((double) i / (n - 1.) * Math.PI);
            buff[i] = s * s;
        }

        /* magic follows */
        this.band[0].begin = 2;
        this.band[0].end = 4;
        this.band[1].begin = 4;
        this.band[1].end = 5;
        this.band[2].begin = 6;
        this.band[2].end = 6;
        this.band[3].begin = 9;
        this.band[3].end = 8;
        this.band[4].begin = 13;
        this.band[4].end = 8;
        this.band[5].begin = 17;
        this.band[5].end = 8;
        this.band[6].begin = 22;
        this.band[6].end = 8;

        for (int j = 0; j < VE_BANDS; j++) {
            final EnvelopeBand b = this.band[j];// java
            n = b.end;
            buff = new float[n];// java
            for (int i = 0; i < n; i++) {
                buff[i] = (float) Math.sin(((double) i + .5) / (double) n * Math.PI);
                b.total += buff[i];
            }
            b.window = buff;
            b.total = 1.f / b.total;
        }

        this.filter = new EnvelopeFilterState[VE_BANDS * channels];
        for (int i = 0, ie = VE_BANDS * channels; i < ie; i++) {
            this.filter[i] = new EnvelopeFilterState();
        }
        this.mark = new boolean[this.storage];

    }

    final void _clear() {
        //int i;
        this.mdct.clear();
        //for( i = 0; i < VE_BANDS; i++ )
        //	e.band[i].window = null;
        //e.mdct_win = null;
        //e.filter = null;
        //e.mark = null;
        m_clear();
    }

    /**
     * fairly straight threshhold-by-band based until we find something
     * that works better and isn't patented.
     */
    final int _amp(
            final InfoPsyGlobal gi,
            final float[] data, int data_offset,
            final EnvelopeBand[] bands,
            final EnvelopeFilterState[] filters,
            final int filter_offset) {
        int n = this.winlength;
        int ret = 0;

		/* we want to have a 'minimum bar' for energy, else we're just
		 basing blocks on quantization noise that outweighs the signal
		 itself (for low power signals) */

        final float minV = this.minenergy;
        final float[] vec = new float[n];

		/* stretch is used to gradually lengthen the number of windows
		 considered prevoius-to-potential-trigger */
        int i = this.m_stretch >>> 1;
        final int stretch = VE_MINSTRETCH >= i ? VE_MINSTRETCH : i;
        float penalty = gi.stretch_penalty - (i - VE_MINSTRETCH);
        if (penalty < 0.f) {
            penalty = 0.f;
        }
        if (penalty > gi.stretch_penalty) {
            penalty = gi.stretch_penalty;
        }

		/*Analysis._analysis_output_always("lpcm", seq2, data, data_offset, n, false, false,
			totalshift + pos * this.m_searchstep);*/

        /* window and transform */
        for (i = 0; i < n; i++) {
            vec[i] = data[data_offset++] * this.mdct_win[i];
        }
        this.mdct.forward(vec, vec);

        /*Analysis._analysis_output_always("mdct", seq2, vec, 0, n / 2, false, true, 0);*/

		/* near-DC spreading function; this has nothing to do with
		 psychoacoustics, just sidelobe leakage and window size */
        float decay;
        {
            float temp = vec[0];// java
            temp *= temp;
            float v = vec[1];
            temp += .7f * v * v;
            v = vec[2];
            temp += .2f * v * v;
            final EnvelopeFilterState flt = filters[filter_offset];
            final int ptr = flt.nearptr;

			/* the accumulation is regularly refreshed from scratch to avoid
			   floating point creep */
            if (ptr == 0) {
                decay = flt.nearDC_acc = flt.nearDC_partialacc + temp;
                flt.nearDC_partialacc = temp;
            } else {
                decay = flt.nearDC_acc += temp;
                flt.nearDC_partialacc += temp;
            }
            flt.nearDC_acc -= flt.nearDC[ptr];
            flt.nearDC[ptr] = temp;

            decay *= (1.f / (VE_NEARDC + 1));
            flt.nearptr++;
            if (flt.nearptr >= VE_NEARDC) {
                flt.nearptr = 0;
            }
            decay = Codec.todB(decay) * .5f - 15.f;
        }

		/* perform spreading and limiting, also smooth the spectrum.  yes,
		 the MDCT results in all real coefficients, but it still *behaves*
		 like real/imaginary pairs */
        n >>>= 1;
        for (i = 0; i < n; i += 2) {
            float val = vec[i];// java
            val *= val;
            final float v = vec[i + 1];// java
            val += v * v;
            val = Codec.todB(val) * .5f;
            if (val < decay) {
                val = decay;
            }
            if (val < minV) {
                val = minV;
            }
            vec[i >> 1] = val;
            decay -= 8.f;
        }

        /*Analysis._analysis_output_always("spread", seq2++, vec, 0, n / 2, false, false, 0);*/

        /* perform preecho/postecho triggering by band */
        for (int j = 0; j < VE_BANDS; j++) {
            float acc = 0.f;
            float valmax, valmin;
            final EnvelopeBand eb_j = bands[j];

            /* accumulate amplitude */
            final float w[] = eb_j.window;// java
            final int end = eb_j.end;// java
            for (i = 0, n = eb_j.begin; i < end; i++) {
                acc += vec[n++] * w[i];
            }

            acc *= eb_j.total;

            /* convert amplitude to delta */
            {
                final EnvelopeFilterState filters_j = filters[filter_offset + j];
                final int ithis = filters_j.ampptr;
                float postmax, postmin, premax = -99999.f, premin = 99999.f;

                int p = ithis;
                p--;
                if (p < 0) {
                    p += VE_AMP;
                }
                final float[] ampbuf = filters_j.ampbuf;// java
                postmin = postmax = ampbuf[p];
                if (postmax < acc) {
                    postmax = acc;
                }
                if (postmin > acc) {
                    postmin = acc;
                }

                for (i = 0; i < stretch; i++) {
                    p--;
                    if (p < 0) {
                        p += VE_AMP;
                    }
                    final float val = ampbuf[p];
                    premax = (premax >= val) ? premax : val;
                    premin = (premin <= val) ? premin : val;
                }

                valmin = postmin - premin;
                valmax = postmax - premax;

                /*filters[j].markers[pos]=valmax;*/
                ampbuf[ithis] = acc;
                filters_j.ampptr++;
                if (filters_j.ampptr >= VE_AMP) {
                    filters_j.ampptr = 0;
                }
            }

            /* look at min/max, decide trigger */
            if (valmax > gi.preecho_thresh[j] + penalty) {
                ret |= 1;
                ret |= 4;
            }
            if (valmin < gi.postecho_thresh[j] - penalty) {
                ret |= 2;
            }
        }

        return (ret);
    }

    final void _shift(final int shift) {
        /* adjust for placing marks ahead of ve->current */
        final int smallsize = this.current / this.searchstep + VE_POST;
        final int smallshift = shift / this.searchstep;

        System.arraycopy(this.mark, smallshift, this.mark, 0, smallsize - smallshift);

/* #if 0
		for( i = 0; i < DspState.VE_BANDS * e.ch; i++ )
			System.arraycopy( this.m_filter[i].markers, smallshift, this.m_filter[i].markers, 0, 1024 - smallshift );
		totalshift += shift;
#endif */

        this.current -= shift;
        if (this.curmark >= 0) {
            this.curmark -= shift;
        }
        this.cursor -= shift;
    }
}

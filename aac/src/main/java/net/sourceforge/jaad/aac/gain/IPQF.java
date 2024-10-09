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

package net.sourceforge.jaad.aac.gain;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 *
 * @author in-somnia
 */
//inverse polyphase quadrature filter
class IPQF implements GCConstants, PQFTables {

	private final float[] buf;
	private final float[][] tmp1, tmp2;

	IPQF() {
		buf = new float[BANDS];
		tmp1 = new float[BANDS/2][NPQFTAPS/BANDS];
		tmp2 = new float[BANDS/2][NPQFTAPS/BANDS];
	}

	void process(float[][] _in, int frameLen, int maxBand, float[] out) {
		int i, j;
		for(i = 0; i<frameLen; i++) {
			out[i] = 0.0f;
		}

		for(i = 0; i<frameLen/BANDS; i++) {
			for(j = 0; j<BANDS; j++) {
				buf[j] = _in[j][i];
			}
			performSynthesis(buf, out, i*BANDS);
		}
	}

	private void performSynthesis(float[] _in, float[] out, int outOff) {
		final int kk = NPQFTAPS/(2*BANDS);
		int i, n, k;
		float acc;

		for(n = 0; n<BANDS/2; ++n) {
			for(k = 0; k<2*kk-1; ++k) {
				tmp1[n][k] = tmp1[n][k+1];
				tmp2[n][k] = tmp2[n][k+1];
			}
		}

		for(n = 0; n<BANDS/2; ++n) {
			acc = 0.0f;
			for(i = 0; i<BANDS; ++i) {
				acc += COEFS_Q0[n][i]*_in[i];
			}
			tmp1[n][2*kk-1] = acc;

			acc = 0.0f;
			for(i = 0; i<BANDS; ++i) {
				acc += COEFS_Q1[n][i]*_in[i];
			}
			tmp2[n][2*kk-1] = acc;
		}

		for(n = 0; n<BANDS/2; ++n) {
			acc = 0.0f;
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T0[n][k]*tmp1[n][2*kk-1-2*k];
			}
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T1[n][k]*tmp2[n][2*kk-2-2*k];
			}
			out[outOff+n] = acc;

			acc = 0.0f;
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T0[BANDS-1-n][k]*tmp1[n][2*kk-1-2*k];
			}
			for(k = 0; k<kk; ++k) {
				acc -= COEFS_T1[BANDS-1-n][k]*tmp2[n][2*kk-2-2*k];
			}
			out[outOff+BANDS-1-n] = acc;
		}
	}
}

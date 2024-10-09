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

package net.sourceforge.jaad.aac.filterbank;

import net.sourceforge.jaad.aac.AACException;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 *
 * @author in-somnia
 */
class MDCT implements MDCTTables {

	private final int N, N2, N4, N8;
	private final float[][] sincos;
	private final FFT fft;
	private final float[][] buf;
	private final float[] tmp;

	MDCT(int length) throws AACException {
		N = length;
		N2 = length>>1;
		N4 = length>>2;
		N8 = length>>3;
		switch(length) {
			case 2048:
				sincos = MDCT_TABLE_2048;
				break;
			case 256:
				sincos = MDCT_TABLE_128;
				break;
			case 1920:
				sincos = MDCT_TABLE_1920;
				break;
			case 240:
				sincos = MDCT_TABLE_240;
			default:
				throw new AACException("unsupported MDCT length: "+length);
		}
		fft = new FFT(N4);
		buf = new float[N4][2];
		tmp = new float[2];
	}

	void process(float[] _in, int inOff, float[] out, int outOff) {
		int k;

		//pre-IFFT complex multiplication
		for(k = 0; k<N4; k++) {
			buf[k][1] = (_in[inOff+2*k]*sincos[k][0])+(_in[inOff+N2-1-2*k]*sincos[k][1]);
			buf[k][0] = (_in[inOff+N2-1-2*k]*sincos[k][0])-(_in[inOff+2*k]*sincos[k][1]);
		}

		//complex IFFT, non-scaling
		fft.process(buf, false);

		//post-IFFT complex multiplication
		for(k = 0; k<N4; k++) {
			tmp[0] = buf[k][0];
			tmp[1] = buf[k][1];
			buf[k][1] = (tmp[1]*sincos[k][0])+(tmp[0]*sincos[k][1]);
			buf[k][0] = (tmp[0]*sincos[k][0])-(tmp[1]*sincos[k][1]);
		}

		//reordering
		for(k = 0; k<N8; k += 2) {
			out[outOff+2*k] = buf[N8+k][1];
			out[outOff+2+2*k] = buf[N8+1+k][1];

			out[outOff+1+2*k] = -buf[N8-1-k][0];
			out[outOff+3+2*k] = -buf[N8-2-k][0];

			out[outOff+N4+2*k] = buf[k][0];
			out[outOff+N4+2+2*k] = buf[1+k][0];

			out[outOff+N4+1+2*k] = -buf[N4-1-k][1];
			out[outOff+N4+3+2*k] = -buf[N4-2-k][1];

			out[outOff+N2+2*k] = buf[N8+k][0];
			out[outOff+N2+2+2*k] = buf[N8+1+k][0];

			out[outOff+N2+1+2*k] = -buf[N8-1-k][1];
			out[outOff+N2+3+2*k] = -buf[N8-2-k][1];

			out[outOff+N2+N4+2*k] = -buf[k][1];
			out[outOff+N2+N4+2+2*k] = -buf[1+k][1];

			out[outOff+N2+N4+1+2*k] = buf[N4-1-k][0];
			out[outOff+N2+N4+3+2*k] = buf[N4-2-k][0];
		}
	}

	void processForward(float[] _in, float[] out) {
		int n, k;
		//pre-FFT complex multiplication
		for(k = 0; k<N8; k++) {
			n = k<<1;
			tmp[0] = _in[N-N4-1-n]+_in[N-N4+n];
			tmp[1] = _in[N4+n]-_in[N4-1-n];

			buf[k][0] = (tmp[0]*sincos[k][0])+(tmp[1]*sincos[k][1]);
			buf[k][1] = (tmp[1]*sincos[k][0])-(tmp[0]*sincos[k][1]);

			buf[k][0] *= N;
			buf[k][1] *= N;

			tmp[0] = _in[N2-1-n]-_in[n];
			tmp[1] = _in[N2+n]+_in[N-1-n];

			buf[k+N8][0] = (tmp[0]*sincos[k+N8][0])+(tmp[1]*sincos[k+N8][1]);
			buf[k+N8][1] = (tmp[1]*sincos[k+N8][0])-(tmp[0]*sincos[k+N8][1]);

			buf[k+N8][0] *= N;
			buf[k+N8][1] *= N;
		}

		//complex FFT, non-scaling
		fft.process(buf, true);

		//post-FFT complex multiplication
		for(k = 0; k<N4; k++) {
			n = k<<1;

			tmp[0] = (buf[k][0]*sincos[k][0])+(buf[k][1]*sincos[k][1]);
			tmp[1] = (buf[k][1]*sincos[k][0])-(buf[k][0]*sincos[k][1]);

			out[n] = -tmp[0];
			out[N2-1-n] = tmp[1];
			out[N2+n] = -tmp[1];
			out[N-1-n] = tmp[0];
		}
	}
}

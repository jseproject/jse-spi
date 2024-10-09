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

package net.sourceforge.jaad.aac.huffman;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.IBitStream;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 *
 * @author in-somnia
 */
//TODO: implement decodeSpectralDataER
public class Huffman implements Codebooks {

	private static final boolean[] UNSIGNED = {false, false, true, true, false, false, true, true, true, true, true};
	private static final int QUAD_LEN = 4, PAIR_LEN = 2;

	private Huffman() {
	}

	private static int findOffset(IBitStream _in, int[][] table) throws AACException {
		int off = 0;
		int len = table[off][0];
		int cw = _in.readBits(len);
		int j;
		while(cw!=table[off][1]) {
			off++;
			j = table[off][0]-len;
			len = table[off][0];
			cw <<= j;
			cw |= _in.readBits(j);
		}
		return off;
	}

	private static void signValues(IBitStream _in, int[] data, int off, int len) throws AACException {
		for(int i = off; i<off+len; i++) {
			if(data[i]!=0) {
				if(_in.readBool()) data[i] = -data[i];
			}
		}
	}

	private static int getEscape(IBitStream _in, int s) throws AACException {
		final boolean neg = s<0;

		int i = 4;
		while(_in.readBool()) {
			i++;
		}
		final int j = _in.readBits(i)|(1<<i);

		return (neg ? -j : j);
	}

	public static int decodeScaleFactor(IBitStream _in) throws AACException {
		final int offset = findOffset(_in, HCB_SF);
		return HCB_SF[offset][2];
	}

	public static void decodeSpectralData(IBitStream _in, int cb, int[] data, int off) throws AACException {
		final int[][] HCB = CODEBOOKS[cb-1];

		//find index
		final int offset = findOffset(_in, HCB);

		//copy data
		data[off] = HCB[offset][2];
		data[off+1] = HCB[offset][3];
		if(cb<5) {
			data[off+2] = HCB[offset][4];
			data[off+3] = HCB[offset][5];
		}

		//sign & escape
		if(cb<11) {
			if(UNSIGNED[cb-1]) signValues(_in, data, off, cb<5 ? QUAD_LEN : PAIR_LEN);
		}
		else if(cb==11||cb>15) {
			signValues(_in, data, off, cb<5 ? QUAD_LEN : PAIR_LEN); //virtual codebooks are always unsigned
			if(Math.abs(data[off])==16) data[off] = getEscape(_in, data[off]);
			if(Math.abs(data[off+1])==16) data[off+1] = getEscape(_in, data[off+1]);
		}
		else throw new AACException("Huffman: unknown spectral codebook: "+cb);
	}
}

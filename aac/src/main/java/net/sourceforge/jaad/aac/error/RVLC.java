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

package net.sourceforge.jaad.aac.error;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.syntax.IBitStream;
import net.sourceforge.jaad.aac.syntax.ICSInfo;
import net.sourceforge.jaad.aac.syntax.ICStream;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * Reversable variable length coding
 * Decodes scalefactors if error resilience is used.
 * 
 * @author in-somnia
 */
public class RVLC implements RVLCTables {

	private static final int ESCAPE_FLAG = 7;

	public void decode(IBitStream _in, ICStream ics, int[][] scaleFactors) throws AACException {
		final int bits = (ics.getInfo().isEightShortFrame()) ? 11 : 9;
		final boolean sfConcealment = _in.readBool();
		final int revGlobalGain = _in.readBits(8);
		final int rvlcSFLen = _in.readBits(bits);

		final ICSInfo info = ics.getInfo();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[][] sfbCB = {{}}; //ics.getSectionData().getSfbCB();

		int sf = ics.getGlobalGain();
		int intensityPosition = 0;
		int noiseEnergy = sf-90-256;
		boolean intensityUsed = false, noiseUsed = false;

		int sfb;
		for(int g = 0; g<windowGroupCount; g++) {
			for(sfb = 0; sfb<maxSFB; sfb++) {
				switch(sfbCB[g][sfb]) {
					case HCB.ZERO_HCB:
						scaleFactors[g][sfb] = 0;
						break;
					case HCB.INTENSITY_HCB:
					case HCB.INTENSITY_HCB2:
						if(!intensityUsed) intensityUsed = true;
						intensityPosition += decodeHuffman(_in);
						scaleFactors[g][sfb] = intensityPosition;
						break;
					case HCB.NOISE_HCB:
						if(noiseUsed) {
							noiseEnergy += decodeHuffman(_in);
							scaleFactors[g][sfb] = noiseEnergy;
						}
						else {
							noiseUsed = true;
							noiseEnergy = decodeHuffman(_in);
						}
						break;
					default:
						sf += decodeHuffman(_in);
						scaleFactors[g][sfb] = sf;
						break;
				}
			}
		}

		int lastIntensityPosition = 0;
		if(intensityUsed) lastIntensityPosition = decodeHuffman(_in);
		noiseUsed = false;
		if(_in.readBool()) decodeEscapes(_in, ics, scaleFactors);
	}

	private void decodeEscapes(IBitStream _in, ICStream ics, int[][] scaleFactors) throws AACException {
		final ICSInfo info = ics.getInfo();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[][] sfbCB = {{}}; //ics.getSectionData().getSfbCB();

		final int escapesLen = _in.readBits(8);

		boolean noiseUsed = false;

		int sfb, val;
		for(int g = 0; g<windowGroupCount; g++) {
			for(sfb = 0; sfb<maxSFB; sfb++) {
				if(sfbCB[g][sfb]==HCB.NOISE_HCB&&!noiseUsed) noiseUsed = true;
				else if(Math.abs(sfbCB[g][sfb])==ESCAPE_FLAG) {
					val = decodeHuffmanEscape(_in);
					if(sfbCB[g][sfb]==-ESCAPE_FLAG) scaleFactors[g][sfb] -= val;
					else scaleFactors[g][sfb] += val;
				}
			}
		}
	}

	private int decodeHuffman(IBitStream _in) throws AACException {
		int off = 0;
		int i = RVLC_BOOK[off][1];
		int cw = _in.readBits(i);

		int j;
		while((cw!=RVLC_BOOK[off][2])&&(i<10)) {
			off++;
			j = RVLC_BOOK[off][1]-i;
			i += j;
			cw <<= j;
			cw |= _in.readBits(j);
		}

		return RVLC_BOOK[off][0];
	}

	private int decodeHuffmanEscape(IBitStream _in) throws AACException {
		int off = 0;
		int i = ESCAPE_BOOK[off][1];
		int cw = _in.readBits(i);

		int j;
		while((cw!=ESCAPE_BOOK[off][2])&&(i<21)) {
			off++;
			j = ESCAPE_BOOK[off][1]-i;
			i += j;
			cw <<= j;
			cw |= _in.readBits(j);
		}

		return ESCAPE_BOOK[off][0];
	}
}

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

package net.sourceforge.jaad.aac.syntax;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.AACDecoderConfig;
import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.huffman.Huffman;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license. 
 *
 * @author in-somnia
 */
class CCE extends Element implements SyntaxConstants {

	public static final int BEFORE_TNS = 0;
	public static final int AFTER_TNS = 1;
	public static final int AFTER_IMDCT = 2;
	private static final float[] CCE_SCALE = {
		1.09050773266525765921f,
		1.18920711500272106672f,
		1.4142135623730950488016887f,
		2f};
	private final ICStream ics;
	private float[] iqData;
	private int couplingPoint;
	private int coupledCount;
	private final boolean[] channelPair;
	private final int[] idSelect;
	private final int[] chSelect;
	/*[0] shared list of gains; [1] list of gains for right channel;
	 *[2] list of gains for left channel; [3] lists of gains for both channels
	 */
	private final float[][] gain;

	CCE(int frameLength) {
		super();
		ics = new ICStream(frameLength);
		channelPair = new boolean[8];
		idSelect = new int[8];
		chSelect = new int[8];
		gain = new float[16][120];
	}

	int getCouplingPoint() {
		return couplingPoint;
	}

	int getCoupledCount() {
		return coupledCount;
	}

	boolean isChannelPair(int index) {
		return channelPair[index];
	}

	int getIDSelect(int index) {
		return idSelect[index];
	}

	int getCHSelect(int index) {
		return chSelect[index];
	}

	void decode(IBitStream _in, AACDecoderConfig conf) throws AACException {
		couplingPoint = 2*_in.readBit();
		coupledCount = _in.readBits(3);
		int gainCount = 0;
		int i;
		for(i = 0; i<=coupledCount; i++) {
			gainCount++;
			channelPair[i] = _in.readBool();
			idSelect[i] = _in.readBits(4);
			if(channelPair[i]) {
				chSelect[i] = _in.readBits(2);
				if(chSelect[i]==3) gainCount++;
			}
			else chSelect[i] = 2;
		}
		couplingPoint += _in.readBit();
		couplingPoint |= (couplingPoint>>1);

		final boolean sign = _in.readBool();
		final double scale = CCE_SCALE[_in.readBits(2)];

		ics.decode(_in, false, conf);
		final ICSInfo info = ics.getInfo();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		//TODO:
		final int[][] sfbCB = {{}};//ics.getSectionData().getSfbCB();

		for(i = 0; i<gainCount; i++) {
			int idx = 0;
			int cge = 1;
			int xg = 0;
			float gainCache = 1.0f;
			if(i>0) {
				cge = couplingPoint==2 ? 1 : _in.readBit();
				xg = cge==0 ? 0 : Huffman.decodeScaleFactor(_in)-60;
				gainCache = (float) Math.pow(scale, -xg);
			}
			if(couplingPoint==2) gain[i][0] = gainCache;
			else {
				int sfb;
				for(int g = 0; g<windowGroupCount; g++) {
					for(sfb = 0; sfb<maxSFB; sfb++, idx++) {
						if(sfbCB[g][sfb]!=HCB.ZERO_HCB) {
							if(cge==0) {
								int t = Huffman.decodeScaleFactor(_in)-60;
								if(t!=0) {
									int s = 1;
									t = xg += t;
									if(!sign) {
										s -= 2*(t&0x1);
										t >>= 1;
									}
									gainCache = (float) (Math.pow(scale, -t)*s);
								}
							}
							gain[i][idx] = gainCache;
						}
					}
				}
			}
		}
	}

	void process() throws AACException {
		iqData = ics.getInvQuantData();
	}

	void applyIndependentCoupling(int index, float[] data) {
		final double g = gain[index][0];
		for(int i = 0; i<data.length; i++) {
			data[i] += g*iqData[i];
		}
	}

	void applyDependentCoupling(int index, float[] data) {
		final ICSInfo info = ics.getInfo();
		final int[] swbOffsets = info.getSWBOffsets();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		//TODO:
		final int[][] sfbCB = {{}}; //ics.getSectionData().getSfbCB();

		int srcOff = 0;
		int dstOff = 0;

		int len, sfb, group, k, idx = 0;
		float x;
		for(int g = 0; g<windowGroupCount; g++) {
			len = info.getWindowGroupLength(g);
			for(sfb = 0; sfb<maxSFB; sfb++, idx++) {
				if(sfbCB[g][sfb]!=HCB.ZERO_HCB) {
					x = gain[index][idx];
					for(group = 0; group<len; group++) {
						for(k = swbOffsets[sfb]; k<swbOffsets[sfb+1]; k++) {
							data[dstOff+group*128+k] += x*iqData[srcOff+group*128+k];
						}
					}
				}
			}
			dstOff += len*128;
			srcOff += len*128;
		}
	}
}

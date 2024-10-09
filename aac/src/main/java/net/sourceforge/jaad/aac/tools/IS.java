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

package net.sourceforge.jaad.aac.tools;

import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.syntax.CPE;
import net.sourceforge.jaad.aac.syntax.SyntaxConstants;
import net.sourceforge.jaad.aac.syntax.ICSInfo;
import net.sourceforge.jaad.aac.syntax.ICStream;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * Intensity stereo
 * @author in-somnia
 */
public final class IS implements SyntaxConstants, ISScaleTable, HCB {

	private IS() {
	}

	public static void process(CPE cpe, float[] specL, float[] specR) {
		final ICStream ics = cpe.getRightChannel();
		final ICSInfo info = ics.getInfo();
		final int[] offsets = info.getSWBOffsets();
		final int windowGroups = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[] sfbCB = ics.getSfbCB();
		final int[] sectEnd = ics.getSectEnd();
		final float[] scaleFactors = ics.getScaleFactors();

		int w, i, j, c, end, off;
		int idx = 0, groupOff = 0;
		float scale;
		for(int g = 0; g<windowGroups; g++) {
			for(i = 0; i<maxSFB;) {
				if(sfbCB[idx]==INTENSITY_HCB||sfbCB[idx]==INTENSITY_HCB2) {
					end = sectEnd[idx];
					for(; i<end; i++, idx++) {
						c = sfbCB[idx]==INTENSITY_HCB ? 1 : -1;
						if(cpe.isMSMaskPresent())
							c *= cpe.isMSUsed(idx) ? -1 : 1;
						scale = c*scaleFactors[idx];
						for(w = 0; w<info.getWindowGroupLength(g); w++) {
							off = groupOff+w*128+offsets[i];
							for(j = 0; j<offsets[i+1]-offsets[i]; j++) {
								specR[off+j] = specL[off+j]*scale;
							}
						}
					}
				}
				else {
					end = sectEnd[idx];
					idx += end-i;
					i = end;
				}
			}
			groupOff += info.getWindowGroupLength(g)*128;
		}
	}
}

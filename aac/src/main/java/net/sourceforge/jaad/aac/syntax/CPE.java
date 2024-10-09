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
import java.util.Arrays;

import net.sourceforge.jaad.aac.AACDecoderConfig;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Profile;
import net.sourceforge.jaad.aac.SampleFrequency;
import net.sourceforge.jaad.aac.tools.MSMask;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license. 
 *
 * @author in-somnia
 */
public class CPE extends Element implements SyntaxConstants {

	private MSMask msMask;
	private boolean[] msUsed;
	private boolean commonWindow;
	ICStream icsL, icsR;

	CPE(int frameLength) {
		super();
		msUsed = new boolean[MAX_MS_MASK];
		icsL = new ICStream(frameLength);
		icsR = new ICStream(frameLength);
	}

	void decode(IBitStream _in, AACDecoderConfig conf) throws AACException {
		final Profile profile = conf.getProfile();
		final SampleFrequency sf = conf.getSampleFrequency();
		if(sf.equals(SampleFrequency.SAMPLE_FREQUENCY_NONE)) throw new AACException("invalid sample frequency");

		readElementInstanceTag(_in);

		commonWindow = _in.readBool();
		final ICSInfo info = icsL.getInfo();
		if(commonWindow) {
			info.decode(_in, conf, commonWindow);
			icsR.getInfo().setData(info);

			msMask = CPE.msMaskFromInt(_in.readBits(2));
			if(msMask.equals(MSMask.TYPE_USED)) {
				final int maxSFB = info.getMaxSFB();
				final int windowGroupCount = info.getWindowGroupCount();

				for(int idx = 0; idx<windowGroupCount*maxSFB; idx++) {
					msUsed[idx] = _in.readBool();
				}
			}
			else if(msMask.equals(MSMask.TYPE_ALL_1)) Arrays.fill(msUsed, true);
			else if(msMask.equals(MSMask.TYPE_ALL_0)) Arrays.fill(msUsed, false);
			else throw new AACException("reserved MS mask type used");
		}
		else {
			msMask = MSMask.TYPE_ALL_0;
			Arrays.fill(msUsed, false);
		}

		if(profile.isErrorResilientProfile()&&(info.isLTPrediction1Present())) {
			if(info.ltpData2Present = _in.readBool()) info.getLTPrediction2().decode(_in, info, profile);
		}

		icsL.decode(_in, commonWindow, conf);
		icsR.decode(_in, commonWindow, conf);
	}

	public ICStream getLeftChannel() {
		return icsL;
	}

	public ICStream getRightChannel() {
		return icsR;
	}

	public MSMask getMSMask() {
		return msMask;
	}

	public boolean isMSUsed(int off) {
		return msUsed[off];
	}

	public boolean isMSMaskPresent() {
		return !msMask.equals(MSMask.TYPE_ALL_0);
	}

	public boolean isCommonWindow() {
		return commonWindow;
	}

    public static MSMask msMaskFromInt(int i) throws AACException {
        MSMask[] values = MSMask.values();
        if (i >= values.length) {
            throw new AACException("unknown MS mask type");
        }
        return values[i];
    }
}

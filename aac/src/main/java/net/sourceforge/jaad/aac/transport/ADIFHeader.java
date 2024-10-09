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

package net.sourceforge.jaad.aac.transport;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.IBitStream;
import net.sourceforge.jaad.aac.syntax.PCE;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * @author in-somnia
 */
public final class ADIFHeader {

	private static final long ADIF_ID = 0x41444946; //'ADIF'
	private long id;
	private boolean copyrightIDPresent;
	private byte[] copyrightID;
	private boolean originalCopy, home, bitstreamType;
	private int bitrate;
	private int pceCount;
	private int[] adifBufferFullness;
	private PCE[] pces;

	public static boolean isPresent(IBitStream _in) throws AACException {
		return _in.peekBits(32)==ADIF_ID;
	}

	private ADIFHeader() {
		copyrightID = new byte[9];
	}

	public static ADIFHeader readHeader(IBitStream _in) throws AACException {
		final ADIFHeader h = new ADIFHeader();
		h.decode(_in);
		return h;
	}

	private void decode(IBitStream _in) throws AACException {
		int i;
		id = _in.readBits(32); //'ADIF'
		copyrightIDPresent = _in.readBool();
		if(copyrightIDPresent) {
			for(i = 0; i<9; i++) {
				copyrightID[i] = (byte) _in.readBits(8);
			}
		}
		originalCopy = _in.readBool();
		home = _in.readBool();
		bitstreamType = _in.readBool();
		bitrate = _in.readBits(23);
		pceCount = _in.readBits(4)+1;
		pces = new PCE[pceCount];
		adifBufferFullness = new int[pceCount];
		for(i = 0; i<pceCount; i++) {
			if(bitstreamType) adifBufferFullness[i] = -1;
			else adifBufferFullness[i] = _in.readBits(20);
			pces[i] = new PCE();
			pces[i].decode(_in);
		}
	}

	public PCE getFirstPCE() {
		return pces[0];
	}
}

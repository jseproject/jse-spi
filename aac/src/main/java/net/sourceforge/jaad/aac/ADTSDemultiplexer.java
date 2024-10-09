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

package net.sourceforge.jaad.aac;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class ADTSDemultiplexer {

	private static final int MAXIMUM_FRAME_SIZE = 6144;
	private PushbackInputStream in;
	private DataInputStream din;
	private boolean first;
	private ADTSFrame frame;

	public ADTSDemultiplexer(InputStream in) throws IOException {
		this.in = in instanceof PushbackInputStream ? (PushbackInputStream) in : new PushbackInputStream(in);
		din = new DataInputStream(this.in);
		first = true;
		if(!findNextFrame()) throw new AACException("no ADTS header found");
	}

	public byte[] getDecoderSpecificInfo() {
		return frame.createDecoderSpecificInfo();
	}

	public byte[] readNextFrame() throws IOException {
		if(first) first = false;
		else findNextFrame();

		byte[] b = new byte[frame.getFrameLength()];
		din.readFully(b);
		return b;
	}

	private boolean findNextFrame() throws IOException {
		//find next ADTS ID
		boolean found = false;
		int left = MAXIMUM_FRAME_SIZE;
		int i;
		while(!found&&left>0) {
			i = in.read();
			left--;
			if(i==0xFF) {
				i = in.read();
				if((i&0xF6)==0xF0) found = true;
				in.unread(i);
			}
		}

		if(found) frame = new ADTSFrame(din);
		return found;
	}

	public int getSampleFrequency() {
		return frame.getSampleFrequency();
	}

	public int getChannelCount() {
		return frame.getChannelCount();
	}
}

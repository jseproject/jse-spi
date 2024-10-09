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

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * The SampleBuffer holds the decoded AAC frame. It contains the raw PCM data
 * and its format.
 * @author in-somnia
 * @author Naoko Mitsurugi
 */
public class SampleBuffer {

	private int sampleRate, channels, bitsPerSample;
	private double length, bitrate, encodedBitrate;
	private byte[] data;
	private boolean bigEndian;
	private int bitsRead;

	public SampleBuffer() {
		data = new byte[0];
		sampleRate = 0;
		channels = 0;
		bitsPerSample = 0;
		bigEndian = true;
	}

	/**
	 * Returns the buffer's PCM data.
	 * @return the audio data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Returns the data's sample rate.
	 * @return the sample rate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * Returns the number of channels stored in the data buffer.
	 * @return the number of channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * Returns the number of bits per sample. Usually this is 16, meaning a
	 * sample is stored in two bytes.
	 * @return the number of bits per sample
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Returns the number of bits read.
	 * @return the number of bits read
	 */
	public int getBitsRead() {
		return bitsRead;
	}

	/**
	 * Returns the length of the current frame in seconds.
	 * length = samplesPerChannel / sampleRate
	 * @return the length in seconds
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the bitrate of the decoded PCM data.
	 * <code>bitrate = (samplesPerChannel * bitsPerSample) / length</code>
	 * @return the bitrate
	 */
	public double getBitrate() {
		return bitrate;
	}

	/**
	 * Returns the AAC bitrate of the current frame.
	 * @return the AAC bitrate
	 */
	public double getEncodedBitrate() {
		return encodedBitrate;
	}

	/**
	 * Indicates the endianness for the data.
	 * 
	 * @return true if the data is in big endian, false if it is in little endian
	 */
	public boolean isBigEndian() {
		return bigEndian;
	}

	/**
	 * Sets the endianness for the data.
	 * 
	 * @param bigEndian if true the data will be in big endian, else in little 
	 * endian
	 */
	public void setBigEndian(boolean bigEndian) {
		if(bigEndian!=this.bigEndian) {
			byte tmp;
			for(int i = 0; i<data.length; i += 2) {
				tmp = data[i];
				data[i] = data[i+1];
				data[i+1] = tmp;
			}
			this.bigEndian = bigEndian;
		}
	}

	public void setData(byte[] data, int sampleRate, int channels, int bitsPerSample, int bitsRead) {
		this.data = data;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitsPerSample = bitsPerSample;
		this.bitsRead = bitsRead;

		if(sampleRate==0) {
			length = 0;
			bitrate = 0;
			encodedBitrate = 0;
		}
		else {
			final int bytesPerSample = bitsPerSample/8; //usually 2
			final int samplesPerChannel = data.length/(bytesPerSample*channels); //=1024
			length = (double) samplesPerChannel/(double) sampleRate;
			bitrate = (double) (samplesPerChannel*bitsPerSample*channels)/length;
			encodedBitrate = (double) bitsRead/length;
		}
	}
}

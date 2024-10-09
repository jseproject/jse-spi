/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.flac;

/** FLAC STREAMINFO structure.  (c.f. <A HREF="../format.html#metadata_block_streaminfo">format specification</A>)
 */
public class StreamInfo extends StreamMetadata {
	/** The total stream length of the STREAMINFO block in bytes. */
	static final int FLAC__STREAM_METADATA_STREAMINFO_LENGTH = 34;

	int min_blocksize = 0, max_blocksize = 0;
	int min_framesize = 0, max_framesize = 0;
	public int sample_rate = 0;
	public int channels = 0;
	public int bits_per_sample = 0;
	public long total_samples = 0;
	public final byte md5sum[] = new byte[16];

	public StreamInfo() {
		super.type = Format.FLAC__METADATA_TYPE_STREAMINFO;
	}

	StreamInfo(final StreamInfo m) {
		copyFrom( m );
	}
	private final void copyFrom(final StreamInfo m) {
		super.copyFrom( m );
		min_blocksize = m.min_blocksize;
		max_blocksize = m.max_blocksize;
		min_framesize = m.min_framesize;
		max_framesize = m.max_framesize;
		sample_rate = m.sample_rate;
		channels = m.channels;
		bits_per_sample = m.bits_per_sample;
		total_samples = m.total_samples;
		System.arraycopy( m.md5sum, 0, md5sum, 0, md5sum.length );
	}

	static boolean compare_block_data_(final StreamInfo block1, final StreamInfo block2)
	{
		if( block1.min_blocksize != block2.min_blocksize ) {
			return false;
		}
		if( block1.max_blocksize != block2.max_blocksize ) {
			return false;
		}
		if( block1.min_framesize != block2.min_framesize ) {
			return false;
		}
		if( block1.max_framesize != block2.max_framesize ) {
			return false;
		}
		if( block1.sample_rate != block2.sample_rate ) {
			return false;
		}
		if( block1.channels != block2.channels ) {
			return false;
		}
		if( block1.bits_per_sample != block2.bits_per_sample ) {
			return false;
		}
		if( block1.total_samples != block2.total_samples ) {
			return false;
		}
		if( Format.memcmp( block1.md5sum, 0, block2.md5sum, 0, 16 ) != 0 ) {
			return false;
		}
		return true;
	}

	// metadata_iterators.c
	public final boolean metadata_get_streaminfo(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != streaminfo);

		final StreamInfo object = (StreamInfo) this.get_one_metadata_block_( filename, Format.FLAC__METADATA_TYPE_STREAMINFO );

		if( object != null ) {
			/* can just copy the contents since STREAMINFO has no internal structure */
			this.copyFrom( object );
			delete( object );
			return true;
		}
		//else {
			return false;
		//}
	}
}

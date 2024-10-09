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

/** SeekPoint structure used in SEEKTABLE blocks.  (c.f. <A HREF="../format.html#seekpoint">format specification</A>)
 */
public class SeekPoint implements Comparable<SeekPoint> {
	/**  The sample number of the target frame. */
	long sample_number = 0;

	/** The offset, in bytes, of the target frame with respect to
	 * beginning of the first frame. */
	long stream_offset = 0;

	/** The number of samples in the target frame. */
	int frame_samples = 0;

	@Override
	public int compareTo(final SeekPoint r) {
		/** used as the sort predicate for qsort() */
	//static int seekpoint_compare_(final SeekPoint l, final SeekPoint r)
		/* we don't just 'return l->sample_number - r->sample_number' since the result (FLAC__int64) might overflow an 'int' */
		if( sample_number == r.sample_number ) {
			return 0;
		} else if( sample_number < r.sample_number ) {
			return -1;
		}
		// else
			return 1;
	}

	static final SeekPoint[] array_new_(final int num_points)
	{
		//FLAC__ASSERT(num_points > 0);

		try {
			final SeekPoint[] object_array = new SeekPoint[num_points];

			// if( object_array != null ) {// java: do not
				for( int i = 0; i < num_points; i++ ) {
					object_array[i].sample_number = Format.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER;
					object_array[i].stream_offset = 0;
					object_array[i].frame_samples = 0;
				}
			// }

			return object_array;
		} catch (final OutOfMemoryError e) {
		}
		return null;
	}
}

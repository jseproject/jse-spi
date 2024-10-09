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

/** FLAC CUESHEET track index structure.  (See the
 * <A HREF="../format.html#cuesheet_track_index">format specification</A> for
 * the full description of each field.)
 */
public class CueSheetTrackIndex {
	/** Offset in samples, relative to the track offset, of the index
	 * point.
	 */
	public long offset = 0;

	/** The index point number. */
	byte number = 0;// TODO check using. value must be less then 128! also check castings.

	CueSheetTrackIndex() {
	}

	CueSheetTrackIndex(final CueSheetTrackIndex cs) {
		copyFrom( cs );
	}

	private final void copyFrom(final CueSheetTrackIndex cs) {
		offset = cs.offset;
		number = cs.number;
	}

	static CueSheetTrackIndex[] array_new_(final int num_indices)
	{
		//FLAC__ASSERT(num_indices > 0);

		final CueSheetTrackIndex[] array = new CueSheetTrackIndex[num_indices];
		for( int i = 0; i < num_indices; i++ ) {
			array[i] = new CueSheetTrackIndex();
		}
		return array;
	}
}

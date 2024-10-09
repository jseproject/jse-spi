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

import java.util.Arrays;

/** FLAC CUESHEET structure.  (See the
 * <A HREF="../format.html#metadata_block_cuesheet">format specification</A>
 * for the full description of each field.)
 */
public class CueSheet extends StreamMetadata {
	/** Media catalog number, in ASCII printable characters 0x20-0x7e.  In
	 * general, the media catalog number may be 0 to 128 bytes long; any
	 * unused characters should be right-padded with NUL characters.
	 */
	public final byte media_catalog_number[] = new byte[128 /* 129 */];// java: zero end do not need. TODO may be String?

	/** The number of lead-in samples. */
	public long lead_in;

	/** \c true if CUESHEET corresponds to a Compact Disc, else \c false. */
	public boolean is_cd;

	/** The number of tracks. */
	public int num_tracks;// TODO can be changed by the tracks.length

	/** NULL if num_tracks == 0, else pointer to array of tracks. */
	public CueSheetTrack[] tracks;

	CueSheet() {
		super.type = Format.FLAC__METADATA_TYPE_CUESHEET;
	}

	CueSheet(final CueSheet m) {
		copyFrom( m );
	}
	private final void copyFrom(final CueSheet m) {
		super.copyFrom( m );
		System.arraycopy( m.media_catalog_number, 0, media_catalog_number, 0, media_catalog_number.length );
		lead_in = m.lead_in;
		is_cd = m.is_cd;
		num_tracks = m.num_tracks;
		tracks = CueSheetTrack.array_copy_( m.tracks, m.num_tracks );
	}

	final void clear() {
		Arrays.fill( media_catalog_number, (byte)0 );
		lead_in = 0;
		is_cd = false;
		num_tracks = 0;
		tracks = null;
	}

	final void cuesheet_calculate_length_()
	{
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);

		this.length = (
			Format.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN
		) / 8;

		this.length += this.num_tracks * (
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN +
			Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN
		) / 8;

		for( int i = 0; i < this.num_tracks; i++ ) {
			this.length += this.tracks[i].num_indices * (
				Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN +
				Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN +
				Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN
			) / 8;
		}
	}

	private final boolean cuesheet_set_track_(final CueSheetTrack dest, final CueSheetTrack src, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(dest != NULL);
		//FLAC__ASSERT(src != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT((src->indices != NULL && src->num_indices > 0) || (src->indices == NULL && src->num_indices == 0));

		/* do the copy first so that if we fail we leave the object untouched */
		if( copy ) {
			if( ! CueSheetTrack.copy_track_( dest, src ) ) {
				return false;
			}
		}
		else {
			dest.copyFrom( src );
		}

		cuesheet_calculate_length_();
		return true;
	}

	static boolean compare_block_data_(final CueSheet block1, final CueSheet block2)
	{
		if( 0 != Format.memcmp( block1.media_catalog_number, 0, block2.media_catalog_number, 0, block1.media_catalog_number.length )) {
			return false;
		}

		if( block1.lead_in != block2.lead_in ) {
			return false;
		}

		if( block1.is_cd != block2.is_cd ) {
			return false;
		}

		if( block1.num_tracks != block2.num_tracks ) {
			return false;
		}

		if( block1.tracks != null && block2.tracks != null ) {
			//FLAC__ASSERT(block1->num_tracks > 0);
			final CueSheetTrack[] tracks1 = block1.tracks;// java
			final CueSheetTrack[] tracks2 = block2.tracks;// java
			for( int i = 0; i < block1.num_tracks; i++ ) {
				final CueSheetTrack t1 = tracks1[i];// java
				final CueSheetTrack t2 = tracks2[i];// java
				if( t1.offset != t2.offset ) {
					return false;
				}
				if( t1.number != t2.number ) {
					return false;
				}
				if( 0 != Format.memcmp( t1.isrc, 0, t2.isrc, 0, t1.isrc.length ) ) {
					return false;
				}
				if( t1.type != t2.type) {
					return false;
				}
				if( t1.pre_emphasis != t2.pre_emphasis) {
					return false;
				}
				if( t1.num_indices != t2.num_indices) {
					return false;
				}
				if( t1.indices != null && t2.indices != null ) {
					//FLAC__ASSERT(block1->tracks[i].num_indices > 0);
					final CueSheetTrackIndex[] indices1 = t1.indices;// java
					final CueSheetTrackIndex[] indices2 = t2.indices;// java
					for( int j = 0; j < t1.num_indices; j++ ) {
						final CueSheetTrackIndex i1 = indices1[j];// java
						final CueSheetTrackIndex i2 = indices2[j];// java
						if( i1.offset != i2.offset ) {
							return false;
						}
						if( i1.number != i2.number ) {
							return false;
						}
					}
				}
				else if( t1.indices != t2.indices ) {
					return false;
				}
			}
		}
		else if( block1.tracks != block2.tracks ) {
			return false;
		}
		return true;
	}

	public final boolean track_resize_indices(final int track_num, final int new_num_indices)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT(track_num < object->data.cue_sheet.num_tracks);

		final CueSheetTrack track = this.tracks[track_num];

		if( track.indices == null ) {
			//FLAC__ASSERT(track->num_indices == 0);
			if( new_num_indices == 0 ) {
				return true;
			} else if( (track.indices = CueSheetTrackIndex.array_new_( new_num_indices )) == null ) {
				return false;
			}
		}
		else {
			final int new_size = new_num_indices;

			/* overflow check */
			if( new_num_indices > Format.SIZE_MAX ) {
				return false;
			}

			//FLAC__ASSERT(track->num_indices > 0);

			if( new_size == 0 ) {
				track.indices = null;
			}
			else if( (track.indices = Arrays.copyOf( track.indices, new_size )) == null ) {
				return false;
			}

			/* if growing, zero all the lengths/pointers of new elements */
			//if( new_size > old_size )// java: already zeroed
			//	memset(track.indices + track.num_indices, 0, new_size - old_size);
		}

		track.num_indices = (byte)new_num_indices;// FIXME hidden casting int to byte

		this.cuesheet_calculate_length_();
		return true;
	}

	public final boolean track_insert_index(final int track_num, final int index_num, final CueSheetTrackIndex indx)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT(track_num < object->data.cue_sheet.num_tracks);
		//FLAC__ASSERT(index_num <= object->data.cue_sheet.tracks[track_num].num_indices);

		final CueSheetTrack track = this.tracks[track_num];

		if( ! track_resize_indices( track_num, track.num_indices + 1 ) ) {
			return false;
		}

		/* move all indices >= index_num forward one space */
		System.arraycopy( track.indices, index_num, track.indices, index_num + 1, track.num_indices - 1 - index_num );

		track.indices[index_num] = indx;// TODO check, C uses copy
		cuesheet_calculate_length_();
		return true;
	}

	public final boolean track_insert_blank_index(final int track_num, final int index_num)
	{
		final CueSheetTrackIndex indx = new CueSheetTrackIndex();// java: already zeroed
		return track_insert_index( track_num, index_num, indx );
	}

	public final boolean track_delete_index(final int track_num, final int index_num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT(track_num < object->data.cue_sheet.num_tracks);
		//FLAC__ASSERT(index_num < object->data.cue_sheet.tracks[track_num].num_indices);

		final CueSheetTrack track = this.tracks[track_num];

		/* move all indices > index_num backward one space */
		System.arraycopy( track.indices, index_num + 1, track.indices, index_num, track.num_indices - index_num - 1 );

		track_resize_indices( track_num, track.num_indices - 1 );
		cuesheet_calculate_length_();
		return true;
	}

	public final boolean resize_tracks(final int new_num_tracks)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);

		if( this.tracks == null ) {
			//FLAC__ASSERT(object->data.cue_sheet.num_tracks == 0);
			if( new_num_tracks == 0 ) {
				return true;
			} else if( (this.tracks = CueSheetTrack.array_new_( new_num_tracks )) == null ) {
				return false;
			}
		}
		else {
			final int new_size = new_num_tracks;

			/* overflow check */
			if( new_num_tracks > Format.SIZE_MAX ) {
				return false;
			}

			//FLAC__ASSERT(object->data.cue_sheet.num_tracks > 0);

			/* if shrinking, free the truncated entries */
			if( new_num_tracks < this.num_tracks ) {
				int i;
				for( i = new_num_tracks; i < this.num_tracks; i++ ) {
					this.tracks[i].indices = null;
				}
			}

			if( new_size == 0 ) {
				this.tracks = null;
			}
			else if( (this.tracks = Arrays.copyOf( this.tracks, new_size )) == null ) {
				return false;
			}

			/* if growing, zero all the lengths/pointers of new elements */
			//if( new_size > old_size )// java: already zeroed
			//	memset(object.data.cue_sheet.tracks + object.data.cue_sheet.num_tracks, 0, new_size - old_size);
		}

		this.num_tracks = new_num_tracks;

		cuesheet_calculate_length_();
		return true;
	}

	public final boolean set_track(final int track_num, final CueSheetTrack track, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(track_num < object->data.cue_sheet.num_tracks);

		return cuesheet_set_track_( this.tracks[track_num], track, copy );
	}

	public final boolean insert_track(final int track_num, final CueSheetTrack track, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT(track_num <= object->data.cue_sheet.num_tracks);

		if( ! resize_tracks( this.num_tracks + 1 ) ) {
			return false;
		}

		/* move all tracks >= track_num forward one space */
		System.arraycopy( this.tracks, track_num, this.tracks, track_num + 1, this.num_tracks - 1 - track_num );
		this.tracks[track_num].num_indices = 0;
		this.tracks[track_num].indices = null;

		return set_track( track_num, track, copy );
	}

	public final boolean insert_blank_track(final int track_num)
	{
		final CueSheetTrack track = new CueSheetTrack();// java: already zeroed
		return insert_track( track_num, track, /*copy=*/false );
	}

	public final boolean delete_track(final int track_num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);
		//FLAC__ASSERT(track_num < object->data.cue_sheet.num_tracks);

		/* free the track at track_num */
		this.tracks[track_num].indices = null;

		/* move all tracks > track_num backward one space */
		final int n1 = this.num_tracks - 1;// java
		System.arraycopy( this.tracks, track_num + 1, this.tracks, track_num, n1 - track_num );
		this.tracks[n1].num_indices = 0;
		this.tracks[n1].indices = null;

		return resize_tracks( n1 );
	}

	/** @return String if error, null otherwise */
	public final String /* boolean */ is_legal(final boolean check_cd_da_subset/*, const char **violation*/)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);

		return format_cuesheet_is_legal( check_cd_da_subset );
	}

	private final long get_index_01_offset_(final int track)
	{
		if( track >= (this.num_tracks - 1) || this.tracks[track].num_indices < 1 ) {
			return 0;
		} else if( this.tracks[track].indices[0].number == 1 ) {
			return this.tracks[track].indices[0].offset + this.tracks[track].offset + this.lead_in;
		} else if( this.tracks[track].num_indices < 2 ) {
			return 0;
		} else if( this.tracks[track].indices[1].number == 1 ) {
			return this.tracks[track].indices[1].offset + this.tracks[track].offset + this.lead_in;
		} else {
			return 0;
		}
	}

	private static int cddb_add_digits_(int x)
	{
		int n = 0;
		while( x != 0 ) {
			n += (x % 10);
			x /= 10;
		}
		return n;
	}

	/*@@@@add to tests*/
	public final int calculate_cddb_id()
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_CUESHEET);

		if( this.num_tracks < 2 ) {
			return 0;
		}

		{
			final int n1 = this.num_tracks - 1;// java
			int sum = 0;
			for( int i = 0; i < n1; i++ ) {
				sum += cddb_add_digits_( (int)(get_index_01_offset_( i ) / 44100) );
			}
			final int size = (int)((this.tracks[n1].offset + this.lead_in) / 44100) - (int)(get_index_01_offset_( 0 ) / 44100);

			return (sum % 0xFF) << 24 | size << 8 | n1;
		}
	}

	/* @@@@ add to unit tests; it is already indirectly tested by the metadata_object tests */
	/** @return null if legal, String if not legal */
	public final String format_cuesheet_is_legal(final boolean check_cd_da_subset)
	{
		if( check_cd_da_subset ) {
			if( this.lead_in < 2 * 44100 ) {
				return "CD-DA cue sheet must have a lead-in length of at least 2 seconds";
			}
			if( this.lead_in % 588 != 0 ) {
				return "CD-DA cue sheet lead-in length must be evenly divisible by 588 samples";
			}
		}

		if( this.num_tracks == 0 ) {
			return "cue sheet must have at least one track (the lead-out)";
		}

		final int ntracks1 = this.num_tracks - 1;// java
		if( check_cd_da_subset && (int)this.tracks[ntracks1].number != 0xffffffAA /*170*/ ) {
			return "CD-DA cue sheet must have a lead-out track number 170 (0xAA)";
		}

		for( int i = 0; i <= ntracks1; i++ ) {
			final CueSheetTrack t = this.tracks[i];// java
			if( t.number == 0 ) {
				return "cue sheet may not have a track number 0";
			}

			if( check_cd_da_subset ) {
				if( !((t.number >= 1 && t.number <= 99) || (int)t.number == 0xffffffAA /*170*/ ) ) {
					return "CD-DA cue sheet track number must be 1-99 or 170";
				}
			}

			if( check_cd_da_subset && t.offset % 588 != 0 ) {
				if( i == ntracks1 ) {
					return "CD-DA cue sheet lead-out offset must be evenly divisible by 588 samples";
				}
				return "CD-DA cue sheet track offset must be evenly divisible by 588 samples";
			}

			if( i < ntracks1 ) {
				if( t.num_indices == 0 ) {
					return "cue sheet track must have at least one index point";
				}

				if( t.indices[0].number > 1 ) {
					return "cue sheet track's first index number must be 0 or 1";
				}
			}

			for( int j = 0, n = t.num_indices; j < n; j++ ) {
				final CueSheetTrackIndex ti = t.indices[j];// java
				if( check_cd_da_subset && ti.offset % 588 != 0 ) {
					return "CD-DA cue sheet track index offset must be evenly divisible by 588 samples";
				}

				if( j > 0 ) {
					if( ti.number != t.indices[j - 1].number + 1 ) {
						return "cue sheet track index numbers must increase by 1";
					}
				}
			}
		}

		return null;
	}

	// metadata_iterators.c
	public final boolean metadata_get_cuesheet(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != cuesheet);
		final CueSheet cuesheet = (CueSheet) get_one_metadata_block_( filename, Format.FLAC__METADATA_TYPE_CUESHEET );
		if( cuesheet != null ) {
			copyFrom( cuesheet );
		}

		return null != cuesheet;
	}
}

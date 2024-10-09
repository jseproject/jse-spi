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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/** FLAC metadata block structure.  (c.f. <A HREF="../format.html#metadata_block">format specification</A>)
 */
public class StreamMetadata implements StreamDecoderWriteCallback
{
	/** The type of the metadata block; used determine which member of the
	 * \a data union to dereference.  If type >= FLAC__METADATA_TYPE_UNDEFINED
	 * then \a data.unknown must be used. */
	public int /*FLAC__MetadataType*/ type = 0;

	/** \c true if this metadata block is the last, else \a false */
	public boolean is_last = false;

	/** Length, in bytes, of the block data as it appears in the stream. */
	public int length = 0;// FIXME may be FLAC__off_t instead of unsigned?

	/** Polymorphic block data; use the \a type value to determine which
	 * to use. */
	/*union {
		FLAC__StreamMetadata_StreamInfo stream_info;
		FLAC__StreamMetadata_Padding padding;
		FLAC__StreamMetadata_Application application;
		FLAC__StreamMetadata_SeekTable seek_table;
		FLAC__StreamMetadata_VorbisComment vorbis_comment;
		FLAC__StreamMetadata_CueSheet cue_sheet;
		FLAC__StreamMetadata_Picture picture;
		FLAC__StreamMetadata_Unknown unknown;
	} data;*/
	//public Object data;// java moved to child classes

	StreamMetadata() {
	}
	StreamMetadata(final StreamMetadata m) {
		copyFrom( m );
	}

	final void copyFrom(final StreamMetadata m) {
		type = m.type;
		is_last = m.is_last;
		length = m.length;
	}
	/****************************************************************************
	 *
	 * Metadata object routines
	 *
	 ***************************************************************************/

	public static StreamMetadata metadata_new(final int /* FLAC__MetadataType */ type)
	{
		final StreamMetadata object;

		if( type > Format.FLAC__MAX_METADATA_TYPE ) {
			return null;
		}

		//object = (FLAC__StreamMetadata*)calloc(1, sizeof(FLAC__StreamMetadata));
		//if( object != NULL ) {
			//object->is_last = false;
			//object->type = type;
			switch( type ) {
				case Format.FLAC__METADATA_TYPE_STREAMINFO:
					object = new StreamInfo();
					object.length = StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH;
					break;
				case Format.FLAC__METADATA_TYPE_PADDING:
					object = new Padding();
					/* calloc() and java took care of this for us:
					object->length = 0;
					*/
					break;
				case Format.FLAC__METADATA_TYPE_APPLICATION:
					object = new Application();
					object.length = Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8;
					/* calloc() took care of this for us:
					object->data.application.data = 0;
					*/
					break;
				case Format.FLAC__METADATA_TYPE_SEEKTABLE:
					object = new SeekTable();
					/* calloc() and java took care of this for us:
					object->length = 0;
					object->data.seek_table.num_points = 0;
					object->data.seek_table.points = 0;
					*/
					break;
				case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
					object = new VorbisComment();
					((VorbisComment)object).vendor_string = Format.FLAC__VENDOR_STRING;
					((VorbisComment)object).calculate_length_();
					break;
				case Format.FLAC__METADATA_TYPE_CUESHEET:
					object = new CueSheet();
					((CueSheet)object).cuesheet_calculate_length_();
					break;
				case Format.FLAC__METADATA_TYPE_PICTURE:
					object = new Picture();
					final Picture picture = (Picture) object;
					object.length = (
						Format.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN +
						Format.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN + /* empty mime_type string */
						Format.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN + /* empty description string */
						Format.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN +
						Format.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN +
						Format.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN +
						Format.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN +
						Format.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN +
						0 /* no data */
					) / 8;
					picture.type = Format.FLAC__STREAM_METADATA_PICTURE_TYPE_OTHER;
					//picture.mime_type = null;
					//picture.description = null;
					/* calloc() and java took care of this for us:
					object->data.picture.width = 0;
					object->data.picture.height = 0;
					object->data.picture.depth = 0;
					object->data.picture.colors = 0;
					object->data.picture.data_length = 0;
					object->data.picture.data = 0;
					*/
					/* now initialize mime_type and description with empty strings to make things easier on the client */
					picture.mime_type = new String();
					picture.description = new String();
					break;
				default:
					object = new StreamMetadata();
					/* calloc() took care of this for us:
					object->length = 0;
					object->data.unknown.data = 0;
					*/
					break;
			}
		//}
		object.is_last = false;
		object.type = type;

		return object;
	}

	public static StreamMetadata metadata_clone(final StreamMetadata object)
	{
		StreamMetadata to;

		//FLAC__ASSERT(object != NULL);

		/*if( (to = FLAC__metadata_object_new( object.type )) != NULL ) {
			to.is_last = object.is_last;
			to.type = object.type;
			to.length = object.length;*/// java: inside creating every object
			switch( object.type /*to.type*/ ) {
				case Format.FLAC__METADATA_TYPE_STREAMINFO:
					to = new StreamInfo( (StreamInfo) object );
					break;
				case Format.FLAC__METADATA_TYPE_PADDING:
					to = new Padding( (Padding) object );
					break;
				case Format.FLAC__METADATA_TYPE_APPLICATION:
					to = new Application( (Application) object );
					if( to.length < Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) { /* underflow check */
						to = null;
						return null;
					}
					break;
				case Format.FLAC__METADATA_TYPE_SEEKTABLE:
					to = new SeekTable( (SeekTable) object );
					if( ((SeekTable)to).num_points > Format.SIZE_MAX ) { /* overflow check */
						to = null;
						return null;
					}
					break;
				case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
					to = new VorbisComment( (VorbisComment) object );
					break;
				case Format.FLAC__METADATA_TYPE_CUESHEET:
					to = new CueSheet( (CueSheet) object );
					break;
				case Format.FLAC__METADATA_TYPE_PICTURE:
					to = new Picture( (Picture) object );
					break;
				default:
					to = new Unknown( (Unknown) object );
					break;
			}
		//}

		return to;
	}

	static void delete_data(final StreamMetadata object)
	{
		//FLAC__ASSERT(object != NULL);

		switch(object.type) {
			case Format.FLAC__METADATA_TYPE_STREAMINFO:
			case Format.FLAC__METADATA_TYPE_PADDING:
				break;
			case Format.FLAC__METADATA_TYPE_APPLICATION:
				((Application)object).data = null;
				break;
			case Format.FLAC__METADATA_TYPE_SEEKTABLE:
				((SeekTable)object).points = null;
				break;
			case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				((VorbisComment)object).vendor_string = null;
				((VorbisComment)object).comments = null;
				break;
			case Format.FLAC__METADATA_TYPE_CUESHEET:
				((CueSheet)object).tracks = null;
				break;
			case Format.FLAC__METADATA_TYPE_PICTURE:
				((Picture)object).mime_type = null;
				((Picture)object).description = null;
				((Picture)object).data = null;
				break;
			default:
				((Unknown)object).data = null;
				break;
		}
	}

	public static void delete(final StreamMetadata object)
	{
		delete_data( object );
		//free(object);
	}

	public static boolean is_equal(final StreamMetadata block1, final StreamMetadata block2)
	{
		//FLAC__ASSERT(block1 != NULL);
		//FLAC__ASSERT(block2 != NULL);

		if( block1.type != block2.type ) {
			return false;
		}
		if( block1.is_last != block2.is_last ) {
			return false;
		}
		if( block1.length != block2.length ) {
			return false;
		}
		switch( block1.type ) {
			case Format.FLAC__METADATA_TYPE_STREAMINFO:
				return StreamInfo.compare_block_data_( (StreamInfo)block1, (StreamInfo)block2 );
			case Format.FLAC__METADATA_TYPE_PADDING:
				return true; /* we don't compare the padding guts */
			case Format.FLAC__METADATA_TYPE_APPLICATION:
				return Application.compare_block_data_( (Application)block1, (Application)block2, block1.length );
			case Format.FLAC__METADATA_TYPE_SEEKTABLE:
				return SeekTable.compare_block_data_( (SeekTable)block1, (SeekTable)block2 );
			case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				return VorbisComment.compare_block_data_( (VorbisComment)block1, (VorbisComment)block2 );
			case Format.FLAC__METADATA_TYPE_CUESHEET:
				return CueSheet.compare_block_data_( (CueSheet)block1, (CueSheet)block2 );
			case Format.FLAC__METADATA_TYPE_PICTURE:
				return Picture.compare_block_data_( (Picture)block1, (Picture)block2 );
			default:
				return Unknown.compare_block_data_( (Unknown)block1, (Unknown)block2, block1.length );
		}
	}

	// stream_encoder_framing.c
	static boolean add_metadata_block(final StreamMetadata metadata, final BitWriter bw)
	{
		if( ! bw.write_raw_uint32( metadata.is_last ? 1 : 0, Format.FLAC__STREAM_METADATA_IS_LAST_LEN ) ) {
			return false;
		}

		if( ! bw.write_raw_uint32( metadata.type, Format.FLAC__STREAM_METADATA_TYPE_LEN ) ) {
			return false;
		}

		/*
		 * First, for VORBIS_COMMENTs, adjust the length to reflect our vendor string
		 */
		final int vendor_string_length = Format.FLAC__VENDOR_STRING_BYTES.length;
		int i = metadata.length;
		if( metadata.type == Format.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
			//FLAC__ASSERT(metadata->data.vorbis_comment.vendor_string.length == 0 || 0 != metadata->data.vorbis_comment.vendor_string.entry);
			if( ((VorbisComment)metadata).vendor_string != null ) {
				try {
					i -= ((VorbisComment)metadata).vendor_string.getBytes( VorbisComment.ENCODING ).length;
				} catch( final UnsupportedEncodingException e ) {
				}
			}
			i += vendor_string_length;
		}
		//FLAC__ASSERT(i < (1u << FLAC__STREAM_METADATA_LENGTH_LEN));
		/* double protection */
		if( i >= (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN ) ) {
			return false;
		}
		if( ! bw.write_raw_uint32( i, Format.FLAC__STREAM_METADATA_LENGTH_LEN ) ) {
			return false;
		}

		switch( metadata.type ) {
			case Format.FLAC__METADATA_TYPE_STREAMINFO:
				final StreamInfo stream_info = (StreamInfo)metadata;
				//FLAC__ASSERT(metadata->data.stream_info.min_blocksize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN));
				if( ! bw.write_raw_uint32( stream_info.min_blocksize, Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.max_blocksize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN));
				if( ! bw.write_raw_uint32( stream_info.max_blocksize, Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.min_framesize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN));
				if( ! bw.write_raw_uint32( stream_info.min_framesize, Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.max_framesize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN));
				if( ! bw.write_raw_uint32( stream_info.max_framesize, Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(FLAC__format_sample_rate_is_valid(metadata->data.stream_info.sample_rate));
				if( ! bw.write_raw_uint32( stream_info.sample_rate, Format.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.channels > 0);
				//FLAC__ASSERT(metadata->data.stream_info.channels <= (1u << FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN));
				if( ! bw.write_raw_uint32( stream_info.channels - 1, Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.bits_per_sample > 0);
				//FLAC__ASSERT(metadata->data.stream_info.bits_per_sample <= (1u << FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN));
				if( ! bw.write_raw_uint32( stream_info.bits_per_sample - 1, Format.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN ) ) {
					return false;
				}
				if( stream_info.total_samples >= (1L << Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) ) {
					if( ! bw.write_raw_uint64( 0, Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) ) {
						return false;
					}
				} else {
					if( ! bw.write_raw_uint64( stream_info.total_samples, Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) ) {
						return false;
					}
				}
				if( ! bw.write_byte_block( stream_info.md5sum, 16 ) ) {
					return false;
				}
				break;
			case Format.FLAC__METADATA_TYPE_PADDING:
				if( ! bw.write_zeroes( metadata.length << 3 ) ) {
					return false;
				}
				break;
			case Format.FLAC__METADATA_TYPE_APPLICATION:
				final Application application = (Application) metadata;
				if( ! bw.write_byte_block( application.id, Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) ) {
					return false;
				}
				if( ! bw.write_byte_block( application.data, metadata.length - (Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) )) {
					return false;
				}
				break;
			case Format.FLAC__METADATA_TYPE_SEEKTABLE:
				final SeekTable seek_table = (SeekTable) metadata;
				for( i = 0; i < seek_table.num_points; i++ ) {
					final SeekPoint p = seek_table.points[i];// java
					if( ! bw.write_raw_uint64( p.sample_number, Format.FLAC__STREAM_METADATA_SEEKPOINT_SAMPLE_NUMBER_LEN ) ) {
						return false;
					}
					if( ! bw.write_raw_uint64( p.stream_offset, Format.FLAC__STREAM_METADATA_SEEKPOINT_STREAM_OFFSET_LEN ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( p.frame_samples, Format.FLAC__STREAM_METADATA_SEEKPOINT_FRAME_SAMPLES_LEN ) ) {
						return false;
					}
				}
				break;
			case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				final VorbisComment vorbis_comment = (VorbisComment) metadata;
				if( ! bw.write_raw_uint32_little_endian( vendor_string_length ) ) {
					return false;
				}
				if( ! bw.write_byte_block( Format.FLAC__VENDOR_STRING_BYTES, vendor_string_length ) ) {
					return false;
				}
				if( ! bw.write_raw_uint32_little_endian( vorbis_comment.num_comments ) ) {
					return false;
				}
				for( i = 0; i < vorbis_comment.num_comments; i++ ) {
					try {
						final byte[] entry = vorbis_comment.comments[i].getBytes( VorbisComment.ENCODING );
						if( ! bw.write_raw_uint32_little_endian( entry.length ) ) {
							return false;
						}
						if( ! bw.write_byte_block( entry, entry.length ) ) {
							return false;
						}
					} catch( final UnsupportedEncodingException e ) {
					}
				}
				break;
			case Format.FLAC__METADATA_TYPE_CUESHEET:
				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN % 8 == 0);
				final CueSheet cue_sheet = (CueSheet) metadata;
				if( ! bw.write_byte_block( cue_sheet.media_catalog_number, Format.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN / 8 ) ) {
					return false;
				}
				if( ! bw.write_raw_uint64( cue_sheet.lead_in, Format.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN ) ) {
					return false;
				}
				if( ! bw.write_raw_uint32( cue_sheet.is_cd ? 1 : 0, Format.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN ) ) {
					return false;
				}
				if( ! bw.write_zeroes( Format.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN ) ) {
					return false;
				}
				if( ! bw.write_raw_uint32( cue_sheet.num_tracks, Format.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN ) ) {
					return false;
				}
				for( i = 0; i < cue_sheet.num_tracks; i++ ) {
					final CueSheetTrack track = cue_sheet.tracks[i];

					if( ! bw.write_raw_uint64( track.offset, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( ((int)track.number) & 0xff, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN ) ) {
						return false;
					}
					//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN % 8 == 0);
					if( ! bw.write_byte_block( track.isrc, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN/8 ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( track.type, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( track.pre_emphasis, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN ) ) {
						return false;
					}
					if( ! bw.write_zeroes( Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN ) ) {
						return false;
					}
					if( ! bw.write_raw_uint32( track.num_indices, Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN ) ) {
						return false;
					}
					for( int j = 0; j < track.num_indices; j++ ) {
						final CueSheetTrackIndex indx = track.indices[j];

						if( ! bw.write_raw_uint64( indx.offset, Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( indx.number, Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN ) ) {
							return false;
						}
						if( ! bw.write_zeroes( Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN ) ) {
							return false;
						}
					}
				}
				break;
			case Format.FLAC__METADATA_TYPE_PICTURE:
				{
					try {
						final Picture picture = (Picture) metadata;
						int len;
						if( ! bw.write_raw_uint32( picture.type, Format.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN ) ) {
							return false;
						}
						len = picture.mime_type.length();
						if( ! bw.write_raw_uint32( len, Format.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.write_byte_block( picture.mime_type.getBytes( Picture.MIME_ENCODING ), len ) ) {
							return false;
						}
						len = picture.description.length();
						if( ! bw.write_raw_uint32( len, Format.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.write_byte_block( picture.description.getBytes( Picture.DESCRIPTION_ENCODING ), len ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( picture.width, Format.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( picture.height, Format.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( picture.depth, Format.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( picture.colors, Format.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN ) ) {
							return false;
						}
						if( ! bw.write_raw_uint32( picture.data_length, Format.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.write_byte_block( picture.data, picture.data_length ) ) {
							return false;
						}
					} catch(final UnsupportedEncodingException e) {
						return false;
					}
				}
				break;
			default:
				if( ! bw.write_byte_block( ((Unknown)metadata).data, metadata.length ) ) {
					return false;
				}
				break;
		}

		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(bw));
		return true;
	}

	// metadata_iterators.c
	private class level0_client_data implements StreamDecoderMetadataCallback, StreamDecoderErrorCallback {
		private boolean got_error;
		private StreamMetadata object;

		@Override// metadata_callback_
		public final void dec_metadata_callback(final StreamDecoder decoder, final StreamMetadata metadata/*, Object client_data*/) throws IOException {

			// final level0_client_data cd = (level0_client_data)client_data;// java: this

			/*
			 * we assume we only get here when the one metadata block we were
			 * looking for was passed to us
			 */
			if( ! this.got_error && null == this.object ) {
				if( null == (this.object = metadata_clone( metadata )) ) {
					this.got_error = true;
				}
			}
		}

		@Override// error_callback_
		public final void dec_error_callback(final StreamDecoder decoder, final int status/*, Object client_data*/) {

			// final level0_client_data cd = (level0_client_data)client_data;// java: this

			if( status != StreamDecoder.FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC ) {
				this.got_error = true;
			}
		}
	};

	final StreamMetadata get_one_metadata_block_(final String filename, final int /* FLAC__MetadataType */ metadata_type)
	{
		final level0_client_data cd = new level0_client_data();

		//FLAC__ASSERT(0 != filename);

		cd.got_error = false;
		cd.object = null;

		final StreamDecoder decoder = new StreamDecoder();

		decoder.set_md5_checking( false );
		decoder.set_metadata_ignore_all();
		decoder.set_metadata_respond( metadata_type );

		if( decoder.init_file( filename,
				this,// write_callback_,
				cd,// metadata_callback_,
				cd// ,// error_callback_,
				/* cd */ ) != StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK || cd.got_error ) {
			decoder.finish();
			decoder.delete();
			return null;
		}

		if( ! decoder.process_until_end_of_metadata() || cd.got_error ) {
			decoder.finish();
			decoder.delete();
			if( null != cd.object ) {
				delete( cd.object );
			}
			return null;
		}

		decoder.finish();
		decoder.delete();

		return cd.object;
	}

	// metadata_iterators.c
	@Override// write_callback_
	public final int dec_write_callback(final StreamDecoder decoder, final Frame frame, final int[][] buffer, final int offset/*, Object client_data*/) {
		return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}
}

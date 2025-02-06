package org.xiph.flac;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * space for shared functions
 */
abstract class MetadataBase {// FIXME why shared functions return FLAC__Metadata_SimpleIterator statuses?

	/** Status type for FLAC__Metadata_SimpleIterator.
	 *
	 *  The iterator's current status can be obtained by calling FLAC__metadata_simple_iterator_status().
	 */
	//typedef enum {

		/**< The iterator is in the normal OK state */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK = 0;

		/**< The data passed into a function violated the function's usage criteria */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ILLEGAL_INPUT = 1;

		/**< The iterator could not open the target file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE = 2;

		/**< The iterator could not find the FLAC signature at the start of the file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_A_FLAC_FILE = 3;

		/**< The iterator tried to write to a file that was not writable */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_NOT_WRITABLE = 4;

		/**< The iterator encountered input that does not conform to the FLAC metadata specification */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA = 5;

		/**< The iterator encountered an error while reading the FLAC file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR = 6;

		/**< The iterator encountered an error while seeking in the FLAC file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR = 7;

		/**< The iterator encountered an error while writing the FLAC file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR = 8;

		/**< The iterator encountered an error renaming the FLAC file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_RENAME_ERROR = 9;

		/**< The iterator encountered an error removing the temporary file */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_UNLINK_ERROR = 10;

		/**< Memory allocation failed */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR = 11;

		/**< The caller violated an assertion or an unexpected error occurred */
		static final int FLAC__METADATA_SIMPLE_ITERATOR_STATUS_INTERNAL_ERROR = 12;

	//} FLAC__Metadata_SimpleIteratorStatus;

	static final class metadata_block_header_helper {
		boolean is_last;
		int /*FLAC__MetadataType*/ type;
		int length;
	}

	private static void pack_uint32_(int val, final byte[] b, int offset, final int bytes)
	{
		offset += bytes;

		for( int i = 0; i < bytes; i++ ) {
			b[--offset] = (byte)(val & 0xff);
			val >>>= 8;
		}
	}

	private static void pack_uint32_little_endian_(int val, final byte[] b, final int bytes)
	{
		int offset = 0;

		for( int i = 0; i < bytes; i++ ) {
			b[offset++] = (byte)(val & 0xff);
			val >>>= 8;
		}
	}

	private static void pack_uint64_(long val, final byte[] b, int offset, final int bytes)
	{
		offset += bytes;

		for( int i = 0; i < bytes; i++ ) {
			b[--offset] = (byte)(val & 0xff);
			val >>>= 8;
		}
	}

	private static int unpack_uint32_(final byte[] b, int offset, final int bytes)
	{
		int ret = 0;

		for( int i = 0; i < bytes; i++ ) {
			ret <<= 8;
			ret |= ((int)b[offset++]) & 0xff;
			ret |= (((int)b[offset++]) & 0xff) << 8;
			ret |= (((int)b[offset++]) & 0xff) << 16;
			ret |= ((int)b[offset++]) << 24;
		}

		return ret;
	}

	private static int unpack_uint32_little_endian_(final byte[] b, final int bytes)
	{
		int ret = 0;
		int offset = bytes;

		for( int i = 0; i < bytes; i++ ) {
			ret <<= 8;
			ret |= ((int)b[--offset]) << 24;
			ret |= (((int)b[--offset]) & 0xff) << 16;
			ret |= (((int)b[--offset]) & 0xff) << 8;
			ret |= ((int)b[--offset]) & 0xff;
		}

		return ret;
	}

	private static long unpack_uint64_(final byte[] b, int offset, final int bytes)
	{
		long ret = 0;

		for( int i = 0; i < bytes; i++ ) {
			ret <<= 8;
			ret |= ((int)b[offset++]) & 0xff;
			ret |= (((int)b[offset++]) & 0xff) << 8;
			ret |= (((int)b[offset++]) & 0xff) << 16;
			ret |= (((int)b[offset++]) & 0xff) << 24;
			ret |= (((int)b[offset++]) & 0xff) << 32;
			ret |= (((int)b[offset++]) & 0xff) << 40;
			ret |= (((int)b[offset++]) & 0xff) << 48;
			ret |= (((int)b[offset++]) & 0xff) << 56;
		}

		return ret;
	}

	/** return meanings:
	 * 0: ok
	 * 1: read error
	 * 2: seek error
	 * 3: not a FLAC file
	 */
	static int seek_to_first_metadata_block_cb_(
			final RandomAccessFile handle)// java: changed
			//IOHandle handle, IOCallback_Read read_cb, IOCallback_Seek seek_cb)
	{
		final byte buffer[] = new byte[4];

		//FLAC__ASSERT(FLAC__STREAM_SYNC_LENGTH == sizeof(buffer));

		/* skip any id3v2 tag */
		try {
			final int n = handle.read( buffer, 0, 4 );
			if( n != 4 ) {
				return 3;
			}
		} catch(final Exception e) {
			return 1;
		}
		if( 0 == Format.memcmp( buffer, 0, StreamDecoder.ID3V2_TAG_, 0, 3 ) ) {
			int tag_length = 0;

			/* skip to the tag length */
			try {
				handle.seek( 2 + handle.getFilePointer()/*, SEEK_CUR*/ );
			} catch(final IOException e) {
				return 2;
			}

			/* read the length */
			for( int i = 0; i < 4; i++ ) {
				try {
					if( handle.read( buffer, 0, 1 ) < 1 || (buffer[0] & 0x80) != 0 ) {
						return 1;
					}
				} catch(final Exception e) {
					return 1;
				}
				tag_length <<= 7;
				tag_length |= (buffer[0] & 0x7f);
			}

			/* skip the rest of the tag */
			try {
				handle.seek( tag_length + handle.getFilePointer()/*, SEEK_CUR*/ );
			} catch(final IOException e) {
				return 2;
			}

			/* read the stream sync code */
			try {
				final int n = handle.read( buffer, 0, 4 );
				if( n != 4 ) {
					return 3;
				}
			} catch(final Exception e) {
				return 1;
			}
		}

		/* check for the fLaC signature */
		if( 0 == Format.memcmp( Format.FLAC__STREAM_SYNC_STRING, 0, buffer, 0, Format.FLAC__STREAM_SYNC_LENGTH ) ) {
			return 0;
		} else {
			return 3;
		}
	}

	/** @return metadata_block_header_helper */// java: non static for correct access to helper class
	static metadata_block_header_helper /* boolean */ read_metadata_block_header_cb_(//IOHandle handle, IOCallback_Read read_cb,
																					 final RandomAccessFile handle)// java: changed
			//boolean[] is_last, int[] /*FLAC__MetadataType*/ type, int[] length)
	{
		final byte raw_header[] = new byte[Format.FLAC__STREAM_METADATA_HEADER_LENGTH];

		try {
			if( handle.read( raw_header, 0, Format.FLAC__STREAM_METADATA_HEADER_LENGTH ) != Format.FLAC__STREAM_METADATA_HEADER_LENGTH )
			 {
				return null;// false;
			}
		} catch(final Exception e) {
			return null;// false;
		}

		final metadata_block_header_helper helper = new metadata_block_header_helper();
		helper.is_last = ((int)raw_header[0] & 0x80) != 0;
		helper.type = /*(FLAC__MetadataType)*/((int)raw_header[0] & 0x7f);
		helper.length = unpack_uint32_( raw_header, 1, 3 );

		/* Note that we don't check:
		 *    if(iterator->type >= FLAC__METADATA_TYPE_UNDEFINED)
		 * we just will read in an opaque block
		 */

		return helper;// true;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_streaminfo_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			final StreamInfo block)
	{
		final byte buffer[] = new byte[StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH];

		try {
			handle.read( buffer, 0, StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH );
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}

		int b = 0;//b = buffer;

		/* we are using hardcoded numbers for simplicity but we should
		 * probably eventually write a bit-level unpacker and use the
		 * _STREAMINFO_ constants.
		 */
		block.min_blocksize = unpack_uint32_( buffer, b, 2 ); b += 2;
		block.max_blocksize = unpack_uint32_( buffer, b, 2 ); b += 2;
		block.min_framesize = unpack_uint32_( buffer, b, 3 ); b += 3;
		block.max_framesize = unpack_uint32_( buffer, b, 3 ); b += 3;
		block.sample_rate = (unpack_uint32_( buffer, b, 2 ) << 4) | (((int)buffer[b + 2] & 0xf0) >> 4);
		block.channels = (((int)buffer[b + 2] & 0x0e) >> 1) + 1;
		block.bits_per_sample = (((((int)buffer[b + 2] & 0x01)) << 4) | (((int)buffer[b + 3] & 0xf0) >> 4)) + 1;
		block.total_samples = (((long)((int)buffer[b + 3] & 0x0f)) << 32) | unpack_uint64_( buffer, b + 4, 4 );
		System.arraycopy( buffer, b + 8, block.md5sum, 0, 16 );

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_padding_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Seek seek_cb,
																									  final RandomAccessFile handle,// java: changed
																									  final Padding block, final int block_length)
	{
		//(void)block; /* nothing to do; we don't care about reading the padding bytes */

		try {
			handle.seek( block_length + handle.getFilePointer()/*, SEEK_CUR*/ );
		} catch (final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
		}
		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_application_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
																										  final RandomAccessFile handle,// java: changed
																										  final Application block, int block_length)
	{
		final int id_bytes = Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8;

		try {
			handle.read( block.id, 0, id_bytes );

			if( block_length < id_bytes ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}

			block_length -= id_bytes;

			if( block_length == 0 ) {
				block.data = null;
			}
			else {
				block.data = new byte[block_length];

				handle.read( block.data, 0, block_length );
			}
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		} catch(final OutOfMemoryError e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
		}
		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_seektable_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
																										final RandomAccessFile handle,// java: changed
																										final SeekTable block, final int block_length)
	{
		final byte buffer[] = new byte[Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];

		//FLAC__ASSERT(block_length % FLAC__STREAM_METADATA_SEEKPOINT_LENGTH == 0);

		block.num_points = block_length / Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH;

		if( block.num_points == 0 ) {
			block.points = null;
		} else if( null == (block.points = new SeekPoint[block.num_points]) ) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
		}

		try {
			for( int i = 0; i < block.num_points; i++ ) {
				final SeekPoint p = new SeekPoint();
				block.points[i] = p;
				if( handle.read( buffer, 0, Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ) != Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
				/* some MAGIC NUMBERs here */
				p.sample_number = unpack_uint64_( buffer, 0, 8 );
				p.stream_offset = unpack_uint64_( buffer, 8, 8 );
				p.frame_samples = unpack_uint32_( buffer, 16, 2 );
			}
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	/** @return null - error,  byte[] - FLAC__StreamMetadata_VorbisComment_Entry */
	private static byte[] /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_vorbis_comment_entry_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			// FLAC__StreamMetadata_VorbisComment_Entry entry,
			int max_length)
	throws IOException, IllegalArgumentException// java: instead FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR, FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA
	{
		final int entry_length_len = Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN / 8;
		final byte buffer[] = new byte[4]; /* magic number is asserted below */
		final byte entry_entry[];// java: entry->entry

		//FLAC__ASSERT(FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN / 8 == sizeof(buffer));

		if( max_length < entry_length_len ) {
			throw new IllegalArgumentException();// return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA;
		}

		max_length -= entry_length_len;

		if( handle.read( buffer, 0, entry_length_len ) != entry_length_len ) {
			throw new IOException();// return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}

		int entry_length = unpack_uint32_little_endian_( buffer, entry_length_len );
		if( max_length < entry_length ) {
			entry_length = 0;
			throw new IllegalArgumentException();// return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA;
		}
		else {
			max_length -= entry_length;// FIXME why?
		}

		//if( 0 != entry->entry )
		//	free( entry->entry );

		entry_entry = new byte[entry_length_len];

		if( entry_length_len > 0 && handle.read( entry_entry, 0, entry_length_len ) != entry_length_len ) {
			if( handle.read( entry_entry, 0, entry_length_len ) != entry_length_len ) {
				throw new IOException("FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR");// return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
		}

		return entry_entry;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_vorbis_comment_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb, FLAC__IOCallback_Seek seek_cb,
			final RandomAccessFile handle,// java: changed
			final VorbisComment block,
			int block_length)
	{
		//FLAC__Metadata_SimpleIteratorStatus status;
		final int num_comments_len = Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN / 8;
		final byte buffer[] = new byte[4]; /* magic number is asserted below */
		byte[] data;
		//FLAC__ASSERT(FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN / 8 == sizeof(buffer));

		try{
			try {
				data = read_metadata_block_data_vorbis_comment_entry_cb_( handle/*, read_cb, &(block->vendor_string)*/, block_length );
				block.vendor_string = new String( data, VorbisComment.ENCODING );
			} catch(final IllegalArgumentException ie) {// java: if( status == FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA )
				if( block_length >= 4 ) {
					block_length -= 4;
				}
				throw new IllegalArgumentException();
			} catch(final IOException ie) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			if( block_length >= 4 ) {
				block_length -= 4;
			}
			block_length -= data.length;

			if( block_length < num_comments_len ) {
				throw new IllegalArgumentException();/*goto skip; */
			} else {
				block_length -= num_comments_len;
			}
			try {
				if( handle.read( buffer, 0, num_comments_len ) != num_comments_len ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
			} catch(final IOException e) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.num_comments = unpack_uint32_little_endian_( buffer, num_comments_len );

			if( block.num_comments == 0 ) {
				block.comments = null;
			}
			else if( block.num_comments > (block_length >>> 2) ) { /* each comment needs at least 4 byte */
				block.num_comments = 0;
				// status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA;// FIXME is not used
				// goto skip;
				if( block_length > 0 ) {
					/* bad metadata */
					try {
						handle.seek( block_length + handle.getFilePointer()/*, SEEK_CUR*/ );
					} catch(final IOException ie) {
						return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
					}
				}
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
			}
			else {
				try {
					block.comments = new String[block.num_comments];
				} catch ( final OutOfMemoryError e ) {
					block.comments = null;
					block.num_comments = 0;
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
				}
			}

			for( int i = 0; i < block.num_comments; i++ ) {
				try{
					data = read_metadata_block_data_vorbis_comment_entry_cb_( handle/*, read_cb, block->comments + i*/, block_length );
					block.comments[i] = new String( data, VorbisComment.ENCODING );
				} catch(final IllegalArgumentException e) {
					if( block_length >= 4 ) {
						block_length -= 4;
					}
					block.num_comments = i;
					break;// goto skip; FIXME why need goto skip? break enough
				} catch(final IOException e) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
				}
				if( block_length >= 4 ) {
					block_length -= 4;
				}
				block_length -= data.length;
			}
		} catch(final IllegalArgumentException ie) {// java: change for label skip:
		}
//skip:
		if( block_length > 0 ) {
			/* bad metadata */
			try {
				handle.seek( block_length + handle.getFilePointer()/*, SEEK_CUR*/ );
			} catch(final IOException ie) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_SEEK_ERROR;
			}
		}
		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_cuesheet_track_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			final CueSheetTrack track)
	{
		final byte buffer[] = new byte[32]; /* asserted below that this is big enough */

		//FLAC__ASSERT(sizeof(buffer) >= sizeof(FLAC__uint64));
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= (FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN) / 8);

		//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN % 8 == 0);
		try {
			int len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			track.offset = unpack_uint64_( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			track.number = (byte)unpack_uint32_( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN / 8;
			if( handle.read( track.isrc, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}

			//FLAC__ASSERT((FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN) % 8 == 0);
			len = (Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN) / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN == 1);
			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN == 1);
			track.type = ((int)buffer[0] & 0xff) >>> 7;
			track.pre_emphasis = ((int)buffer[0] >> 6) & 1;

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			track.num_indices = (byte)unpack_uint32_( buffer, 0, len );

			if( track.num_indices == 0 ) {
				track.indices = null;
			}
			else if( null == (track.indices = new CueSheetTrackIndex[track.num_indices]) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
			}

			for( int i = 0; i < track.num_indices; i++ ) {
				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN % 8 == 0);
				final CueSheetTrackIndex ti = new CueSheetTrackIndex();
				track.indices[i] = ti;
				len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN / 8;
				if( handle.read( buffer, 0, len ) != len ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
				ti.offset = unpack_uint64_( buffer, 0, len );

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN / 8;
				if( handle.read( buffer, 0, len ) != len ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
				ti.number = (byte)unpack_uint32_( buffer, 0, len );

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN / 8;
				if( handle.read( buffer, 0, len ) != len ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
			}
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}
		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_cuesheet_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			final CueSheet block)
	{
		final byte buffer[] = new byte[1024]; /* MSVC needs a constant expression so we put a magic number and assert */

		//FLAC__ASSERT((FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN)/8 <= sizeof(buffer));
		//FLAC__ASSERT(sizeof(FLAC__uint64) <= sizeof(buffer));

		//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN % 8 == 0);
		try {
			int len = Format.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN / 8;
			if( handle.read( block.media_catalog_number, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.lead_in = unpack_uint64_( buffer, 0, len );

			//FLAC__ASSERT((FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN) % 8 == 0);
			len = (Format.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN) / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.is_cd = (buffer[0] & 0x80) != 0;

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.num_tracks = unpack_uint32_( buffer, 0, len );

			if( block.num_tracks == 0 ) {
				block.tracks = null;
			}
			else if( null == (block.tracks = new CueSheetTrack[block.num_tracks]) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
			}

			for( int i = 0; i < block.num_tracks; i++ ) {
				block.tracks[i] = new CueSheetTrack();
				int /* FLAC__Metadata_SimpleIteratorStatus */ status;
				if( FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK != (status = read_metadata_block_data_cuesheet_track_cb_( handle, block.tracks[i] )) ) {
					return status;
				}
			}
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}
		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	/** @return null - error, byte[] - data */
	private static byte[] /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_picture_cstring_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			//FLAC__byte **data, FLAC__uint32 *length,
			int length_len)
	{
		final byte buffer[] = new byte[4];
		byte[] data = null;

		//FLAC__ASSERT(0 != data);
		//FLAC__ASSERT(length_len%8 == 0);

		length_len >>>= 3; /* convert to bytes */

		//FLAC__ASSERT(sizeof(buffer) >= length_len);
		try {
			if( handle.read( buffer, 0, length_len ) != length_len ) {
				return null;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			final int length = unpack_uint32_( buffer, 0, length_len );

			if( length > (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) { /* data cannot be larger than FLAC metadata block */
				return null;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_BAD_METADATA;
			}

			/*if( null == (*/data = new byte[length];//) )
				//return null;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;

			if( length > 0 ) {
				if( handle.read( data, 0, length ) != length ) {
					return null;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
			}
		} catch(final IOException e) {
			return null;
		} catch(final OutOfMemoryError e) {
			return null;
		}

		return data;// FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_picture_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
			final RandomAccessFile handle,// java: changed
			final Picture block)
	{
		//FLAC__Metadata_SimpleIteratorStatus status;
		final byte buffer[] = new byte[4]; /* asserted below that this is big enough */

		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_TYPE_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_COLORS_LEN/8);

		//FLAC__ASSERT(FLAC__STREAM_METADATA_PICTURE_TYPE_LEN % 8 == 0);
		try {

			int len = Format.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.type = (int /* FLAC__StreamMetadata_Picture_Type */)unpack_uint32_( buffer, 0, len );

			byte[] data;
			if( null == (data = read_metadata_block_data_picture_cstring_cb_( handle, Format.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN )) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;// status;
			}
			block.mime_type = new String( data, Picture.MIME_ENCODING );

			if( null == (data = read_metadata_block_data_picture_cstring_cb_( handle, Format.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN ) ) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;// status;
			}
			block.description = new String( data, Picture.DESCRIPTION_ENCODING );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.width = unpack_uint32_( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.height = unpack_uint32_( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.depth = unpack_uint32_( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_PICTURE_COLORS_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN / 8;
			if( handle.read( buffer, 0, len ) != len ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
			block.colors = unpack_uint32_( buffer, 0, len );

			/* for convenience we use read_metadata_block_data_picture_cstring_cb_() even though it adds an extra terminating NUL we don't use */
			if( null == (block.data = read_metadata_block_data_picture_cstring_cb_( handle, Format.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN )) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;//status;
			}
			block.data_length = block.data.length;
		} catch(final IOException e) {
			return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	private static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_unknown_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb,
																									  final RandomAccessFile handle,// java: changed
																									  final Unknown block, final int block_length)
	{
		if( block_length == 0 ) {
			block.data = null;
		}
		else {
			if( null == (block.data = new byte[block_length]) ) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_MEMORY_ALLOCATION_ERROR;
			}

			try {
				if( handle.read( block.data, 0, block_length ) != block_length ) {
					return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				}
			} catch(final IOException e) {
				return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
			}
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
	}

	static int /* FLAC__Metadata_SimpleIteratorStatus */ read_metadata_block_data_cb_(
			final RandomAccessFile handle,// java: changed
			//FLAC__IOHandle handle, FLAC__IOCallback_Read read_cb, FLAC__IOCallback_Seek seek_cb,
			final StreamMetadata block)
	{
		switch( block.type ) {
			case Format.FLAC__METADATA_TYPE_STREAMINFO:
				return read_metadata_block_data_streaminfo_cb_( handle, /*handle, read_cb,*/ (StreamInfo)block/*.data.stream_info*/ );
			case Format.FLAC__METADATA_TYPE_PADDING:
				return read_metadata_block_data_padding_cb_( handle, /*handle, seek_cb,*/ (Padding)block/*.data.padding*/, block.length );
			case Format.FLAC__METADATA_TYPE_APPLICATION:
				return read_metadata_block_data_application_cb_( handle, /*handle, read_cb,*/ (Application)block/*.data.application*/, block.length );
			case Format.FLAC__METADATA_TYPE_SEEKTABLE:
				return read_metadata_block_data_seektable_cb_( handle, /*handle, read_cb,*/ (SeekTable)block/*.data.seek_table*/, block.length );
			case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				return read_metadata_block_data_vorbis_comment_cb_( handle, /*handle, read_cb, seek_cb,*/ (VorbisComment)block/*.data.vorbis_comment*/, block.length );
			case Format.FLAC__METADATA_TYPE_CUESHEET:
				return read_metadata_block_data_cuesheet_cb_( handle, /*handle, read_cb,*/ (CueSheet)block/*.data.cue_sheet*/ );
			case Format.FLAC__METADATA_TYPE_PICTURE:
				return read_metadata_block_data_picture_cb_( handle, /*handle, read_cb,*/ (Picture)block/*.data.picture*/ );
			default:
				return read_metadata_block_data_unknown_cb_( handle, /*handle, read_cb,*/ (Unknown)block/*.data.unknown*/, block.length );
		}
	}

	static boolean write_metadata_block_header_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final StreamMetadata block)
	{
		final byte buffer[] = new byte[Format.FLAC__STREAM_METADATA_HEADER_LENGTH];

		//FLAC__ASSERT(block.length < (1u << FLAC__STREAM_METADATA_LENGTH_LEN));
		/* double protection */
		if( block.length >= (1 << Format.FLAC__STREAM_METADATA_LENGTH_LEN) ) {
			return false;
		}

		buffer[0] = (byte)((block.is_last ? 0x80 : 0) | block.type);
		pack_uint32_( block.length, buffer, 1, 3 );

		try {
			handle.write( buffer, 0, Format.FLAC__STREAM_METADATA_HEADER_LENGTH );
		} catch(final IOException e) {
			return false;
		}

		return true;
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	static int write_metadata_block_header_(final RandomAccessFile file, final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != file);
		//FLAC__ASSERT(0 != status);

		if( ! write_metadata_block_header_cb_( file, block ) ) {
			return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
			//return false;
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;// true;
	}

	private static boolean write_metadata_block_data_streaminfo_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final StreamInfo block)
	{
		final byte buffer[] = new byte[Format.FLAC__STREAM_METADATA_STREAMINFO_LENGTH];
		final int channels1 = block.channels - 1;
		final int bps1 = block.bits_per_sample - 1;

		/* we are using hardcoded numbers for simplicity but we should
		 * probably eventually write a bit-level packer and use the
		 * _STREAMINFO_ constants.
		 */
		pack_uint32_( block.min_blocksize, buffer, 0, 2 );
		pack_uint32_( block.max_blocksize, buffer, 2, 2 );
		pack_uint32_( block.min_framesize, buffer, 4, 3 );
		pack_uint32_( block.max_framesize, buffer, 7, 3 );
		buffer[10] = (byte)(block.sample_rate >>> 12);
		buffer[11] = (byte)(block.sample_rate >>> 4);
		buffer[12] = (byte)(((block.sample_rate & 0x0f) << 4) | (channels1 << 1) | (bps1 >>> 4));
		buffer[13] = (byte)(((bps1 & 0x0f) << 4) | ((block.total_samples >>> 32) & 0x0f));
		pack_uint32_( (int)block.total_samples, buffer, 14, 4 );
		System.arraycopy( block.md5sum, 0, buffer, 18, 16 );

		try {
			handle.write( buffer, 0, Format.FLAC__STREAM_METADATA_STREAMINFO_LENGTH );
		} catch(final IOException e) {
			return false;
		}

		return true;
	}

	private static boolean write_metadata_block_data_padding_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
																 final RandomAccessFile handle,// java: changed
																 final Padding block, final int block_length)
	{
		int n = block_length;
		final byte buffer[] = new byte[1024];// already zeroed

		//(void)block;
		try {
			for( int i = 0; i < (n >>> 10); i++ ) {
				handle.write( buffer, 0, 1024 );
			}

			n &= 1023;

			handle.write( buffer, 1, n );

		} catch(final IOException e) {
			return false;
		}
		return true;
	}

	private static boolean write_metadata_block_data_application_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
																	 final RandomAccessFile handle,// java: changed
																	 final Application block, int block_length)
	{
		final int id_bytes = Format.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8;
		try {
			handle.write( block.id, 0, id_bytes );

			block_length -= id_bytes;

			handle.write( block.data, 0, block_length );
		} catch(final IOException e) {
			return false;
		}
		return true;
	}

	private static boolean write_metadata_block_data_seektable_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final SeekTable block)
	{
		final byte buffer[] = new byte[Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];

		try {
			for( int i = 0; i < block.num_points; i++ ) {
				/* some MAGIC NUMBERs here */
				pack_uint64_( block.points[i].sample_number, buffer, 0, 8 );
				pack_uint64_( block.points[i].stream_offset, buffer, 8, 8 );
				pack_uint32_( block.points[i].frame_samples, buffer, 16, 2 );
				handle.write( buffer, 0, Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH );
			}
		} catch(final IOException e) {
			return false;
		}

		return true;
	}

	private static boolean write_metadata_block_data_vorbis_comment_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final VorbisComment block)
	{
		final int entry_length_len = Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN / 8;
		final int num_comments_len = Format.FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN / 8;
		final byte buffer[] = new byte[4]; /* magic number is asserted below */

		//FLAC__ASSERT(max(FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN, FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN) / 8 == sizeof(buffer));
		try {
			byte string[] = block.vendor_string.getBytes( VorbisComment.ENCODING );
			pack_uint32_little_endian_( string.length, buffer, entry_length_len );
			handle.write( buffer, 0, entry_length_len );
			handle.write( string, 0, string.length );

			pack_uint32_little_endian_( block.num_comments, buffer, num_comments_len );
			handle.write( buffer, 0, num_comments_len );

			for( int i = 0; i < block.num_comments; i++ ) {
				string = block.comments[i].getBytes( VorbisComment.ENCODING );
				pack_uint32_little_endian_( string.length, buffer, entry_length_len );
				handle.write( buffer, 0, entry_length_len );
				handle.write( string, 0, string.length );
			}

		} catch(final UnsupportedEncodingException e) {
			return false;
		} catch(final IOException e) {
			return false;
		}
		return true;
	}

	private static boolean write_metadata_block_data_cuesheet_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final CueSheet block)
	{
		final byte buffer[] = new byte[1024]; /* asserted below that this is big enough */

		//FLAC__ASSERT(sizeof(buffer) >= sizeof(FLAC__uint64));
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= (FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN)/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN/8);

		//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN % 8 == 0);
		try {
			int len = Format.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN / 8;
			handle.write( block.media_catalog_number, 1, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN / 8;
			pack_uint64_( block.lead_in, buffer, 0, len );
			handle.write( buffer, 0, len );

			//FLAC__ASSERT((FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN) % 8 == 0);
			len = (Format.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN) / 8;
			Arrays.fill( buffer, 0, len, (byte)0 );
			if( block.is_cd ) {
				buffer[0] |= 0x80;
			}
			handle.write( buffer, 0, len );

			//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN % 8 == 0);
			len = Format.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN / 8;
			pack_uint32_( block.num_tracks, buffer, 0, len );
			handle.write( buffer, 0, len );

			for( int i = 0; i < block.num_tracks; i++ ) {
				final CueSheetTrack track = block.tracks[i];

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN / 8;
				pack_uint64_( track.offset, buffer, 0, len );
				handle.write( buffer, 0, len );

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN / 8;
				pack_uint32_( track.number, buffer, 0, len );
				handle.write( buffer, 0, len );

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN / 8;
				handle.write( track.isrc, 0, len );

				//FLAC__ASSERT((FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN) % 8 == 0);
				len = (Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN + Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN) / 8;
				Arrays.fill( buffer, 0, len, (byte)0 );
				buffer[0] = (byte)((track.type << 7) | (track.pre_emphasis << 6));
				handle.write( buffer, 0, len );

				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN % 8 == 0);
				len = Format.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN / 8;
				pack_uint32_( track.num_indices, buffer, 0, len );
				handle.write( buffer, 0, len );

				for( int j = 0; j < track.num_indices; j++ ) {
					final CueSheetTrackIndex indx = track.indices[j];

					//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN % 8 == 0);
					len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN / 8;
					pack_uint64_( indx.offset, buffer, 0, len );
					handle.write( buffer, 0, len );

					//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN % 8 == 0);
					len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN / 8;
					pack_uint32_( indx.number, buffer, 0, len );
					handle.write( buffer, 0, len );

					//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN % 8 == 0);
					len = Format.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN / 8;
					Arrays.fill( buffer, 0, len, (byte)0 );
					handle.write( buffer, 0, len );
				}
			}
		} catch(final IOException e) {
			return false;
		}

		return true;
	}

	private static boolean write_metadata_block_data_picture_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final Picture block)
	{
		final byte buffer[] = new byte[4]; /* magic number is asserted below */

		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_TYPE_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_COLORS_LEN%8);
		//FLAC__ASSERT(0 == FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN%8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_TYPE_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_COLORS_LEN/8);
		//FLAC__ASSERT(sizeof(buffer) >= FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN/8);

		try {

			int len = Format.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN / 8;
			pack_uint32_( block.type, buffer, 0, len );
			handle.write( buffer, 0, len );

			len = Format.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN / 8;
			int slen = block.mime_type.length();// java: using US_ASCII, so byte length = string length
			pack_uint32_( slen, buffer, 0, len );
			handle.write( buffer, 0, len );
			handle.write( block.mime_type.getBytes( Picture.MIME_ENCODING ), 0, slen );

			len = Format.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN / 8;
			final byte[] description = block.description.getBytes( Picture.DESCRIPTION_ENCODING );
			slen = description.length;
			pack_uint32_( slen, buffer, 0, len );
			handle.write( buffer, 0, len );
			handle.write( description, 1, slen );

			len = Format.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN / 8;
			pack_uint32_( block.width, buffer, 0, len );
			handle.write( buffer, 0, len );

			len = Format.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN / 8;
			pack_uint32_( block.height, buffer, 0, len );
			handle.write( buffer, 0, len );

			len = Format.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN / 8;
			pack_uint32_( block.depth, buffer, 0, len );
			handle.write( buffer, 0, len );

			len = Format.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN / 8;
			pack_uint32_( block.colors, buffer, 0, len );
			handle.write( buffer, 0, len );

			len = Format.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN / 8;
			pack_uint32_( block.data_length, buffer, 0, len );
			handle.write( buffer, 0, len );
			handle.write( block.data, 0, block.data_length );
		} catch(final IOException e) {
			return false;
		}
		return true;
	}

	private static boolean write_metadata_block_data_unknown_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
																 final RandomAccessFile handle,// java: changed
																 final Unknown block, final int block_length)
	{
		try {
			handle.write( block.data, 0, block_length );
		} catch(final IOException e) {
			return false;
		}

		return true;
	}

	static boolean write_metadata_block_data_cb_(//FLAC__IOHandle handle, FLAC__IOCallback_Write write_cb,
			final RandomAccessFile handle,// java: changed
			final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != block);

		switch( block.type ) {
			case Format.FLAC__METADATA_TYPE_STREAMINFO:
				return write_metadata_block_data_streaminfo_cb_( handle, /*handle, write_cb,*/ (StreamInfo)block/*.data.stream_info*/ );
			case Format.FLAC__METADATA_TYPE_PADDING:
				return write_metadata_block_data_padding_cb_( handle, /*handle, write_cb,*/ (Padding)block/*.data.padding*/, block.length );
			case Format.FLAC__METADATA_TYPE_APPLICATION:
				return write_metadata_block_data_application_cb_( handle, /*handle, write_cb,*/ (Application)block/*.data.application*/, block.length );
			case Format.FLAC__METADATA_TYPE_SEEKTABLE:
				return write_metadata_block_data_seektable_cb_( handle, /*handle, write_cb,*/ (SeekTable)block/*.data.seek_table*/ );
			case Format.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				return write_metadata_block_data_vorbis_comment_cb_( handle, /*handle, write_cb,*/ (VorbisComment)block/*.data.vorbis_comment*/ );
			case Format.FLAC__METADATA_TYPE_CUESHEET:
				return write_metadata_block_data_cuesheet_cb_( handle, /*handle, write_cb,*/ (CueSheet)block/*.data.cue_sheet*/ );
			case Format.FLAC__METADATA_TYPE_PICTURE:
				return write_metadata_block_data_picture_cb_( handle, /*handle, write_cb,*/ (Picture)block/*.data.picture*/ );
			default:
				return write_metadata_block_data_unknown_cb_( handle, /*handle, write_cb,*/ (Unknown)block/*.data.unknown*/, block.length );
		}
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	static int write_metadata_block_data_(final RandomAccessFile file, final StreamMetadata block)
	{
		//FLAC__ASSERT(0 != file);
		//FLAC__ASSERT(0 != status);

		if( write_metadata_block_data_cb_( file, block ) ) {
			return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;
			//return true;
		}
		//else {
			return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
			//return false;
		//}
	}

	/** @return File - stat, null - error */
	static File get_file_stats_(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != stats);
		return new File( filename );
	}

	static void set_file_stats_(final String filename, final File stats)
	{
/* #if defined(_POSIX_C_SOURCE) && (_POSIX_C_SOURCE >= 200809L)
		struct timespec srctime[2] = {};
		srctime[0].tv_sec = stats->st_atime;
		srctime[1].tv_sec = stats->st_mtime;
#else
		struct utimbuf srctime;
		srctime.actime = stats->st_atime;
		srctime.modtime = stats->st_mtime;
#endif

		FLAC__ASSERT(0 != filename);
		FLAC__ASSERT(0 != stats); */

		final File f = new File( filename );
		f.setExecutable( stats.canExecute() );
		f.setLastModified( stats.lastModified() );
		f.setReadable( stats.canRead() );
		f.setWritable( stats.canWrite() );
	}

	private static final String tempfile_suffix = ".metadata_edit";
	/** java: open_tempfile_ changed to get_tempfile_name_
	 * <p><code><pre>
	 * tempfilename = get_tempfile_name_( filename, tempfile_path_prefix );
	 * try {
	 *   tempfile = new RandomAccessFile( tempfilename, "rw" );
	 *	} catch(FileNotFoundException e) {
	 *   status = FLAC__METADATA_SIMPLE_ITERATOR_STATUS_ERROR_OPENING_FILE;
	 *   cleanup_tempfile_( tempfile, tempfilename );
	 *   return false;
	 *	}</code></pre>
	 */
	static String get_tempfile_name_(final String filename, final String tempfile_path_prefix)
	{
		String tempfilename;
		if( null == tempfile_path_prefix ) {
			tempfilename = filename;
			tempfilename += tempfile_suffix;
			return tempfilename;
		}
		//else {
			tempfilename = tempfile_path_prefix;
			tempfilename += "/";
			final int p = filename.lastIndexOf('/');
			tempfilename += p < 0 ? filename : filename.substring( p + 1 );
			tempfilename += tempfile_suffix;
		//}

		return tempfilename;
	}

	static void cleanup_tempfile_(final RandomAccessFile tempfile, final String tempfilename)
	{
		if( null != tempfile ) {
			try { tempfile.close(); } catch( final IOException e ) {}
		}

		if( null != tempfilename ) {
			new File( tempfilename ).delete();
		}
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	static int transport_tempfile_(final String filename, final RandomAccessFile tempfile, final String tempfilename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != tempfile);
		//FLAC__ASSERT(0 != *tempfile);
		//FLAC__ASSERT(0 != tempfilename);
		//FLAC__ASSERT(0 != *tempfilename);
		//FLAC__ASSERT(0 != status);

		try { tempfile.close(); } catch( final IOException e ) {}

		final File f = new File( filename );

		/* on some flavors of windows, rename() will fail if the destination already exists */
		if( ! f.delete() ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_UNLINK_ERROR;
			//return false;
		}

		/*@@@ to fully support the tempfile_path_prefix we need to update this piece to actually copy across filesystems instead of just rename(): */
		if( ! new File( tempfilename ).renameTo( f ) ) {
			cleanup_tempfile_( tempfile, tempfilename );
			return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_RENAME_ERROR;
			//return false;
		}

		cleanup_tempfile_( tempfile, tempfilename );

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;// true;
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	static int copy_n_bytes_from_file_(final RandomAccessFile file, final RandomAccessFile tempfile, long bytes)
	{
		final byte buffer[] = new byte[8192];

		//FLAC__ASSERT(bytes >= 0);
		while( bytes > 0 ) {
			final int n = Math.min( buffer.length, (int)bytes );
			try {
				if( file.read( buffer, 0, n ) != n ) {
					return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
					//return false;
				}
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				//return false;
			}
			try {
				tempfile.write( buffer, 0, n );
			} catch (final Exception e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
				//return false;
			}
			bytes -= n;
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;// true;
	}

	/** @return FLAC__Metadata_SimpleIteratorStatus */
	static int copy_remaining_bytes_from_file_(final RandomAccessFile file, final RandomAccessFile tempfile)
	{
		final byte buffer[] = new byte[8192];

		while( true ) {
			int n;
			try {
				n = file.read( buffer, 0, buffer.length );
				if( n < 0 ) {
					break;
				}
			} catch(final IOException e) {
				return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_READ_ERROR;
				//return false;
			}
			if( n > 0 ) {
				try {
					tempfile.write( buffer, 0, n );
				} catch(final IOException e) {
					return /* *status = */ FLAC__METADATA_SIMPLE_ITERATOR_STATUS_WRITE_ERROR;
					//return false;
				}
			}
		}

		return FLAC__METADATA_SIMPLE_ITERATOR_STATUS_OK;// true;
	}
}

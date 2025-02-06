package org.xiph.flac;

/** \defgroup flac_format FLAC/format.h: format components
 *  \ingroup flac
 *
 *  \brief
 *  This module contains structure definitions for the representation
 *  of FLAC format components in memory.  These are the basic
 *  structures used by the rest of the interfaces.
 *
 *  First, you should be familiar with the
 *  <A HREF="../format.html">FLAC format</A>.  Many of the values here
 *  follow directly from the specification.  As a user of libFLAC, the
 *  interesting parts really are the structures that describe the frame
 *  header and metadata blocks.
 *
 *  The format structures here are very primitive, designed to store
 *  information in an efficient way.  Reading information from the
 *  structures is easy but creating or modifying them directly is
 *  more complex.  For the most part, as a user of a library, editing
 *  is not necessary; however, for metadata blocks it is, so there are
 *  convenience functions provided in the \link flac_metadata metadata
 *  module \endlink to simplify the manipulation of metadata blocks.
 *
 * \note
 * It's not the best convention, but symbols ending in _LEN are in bits
 * and _LENGTH are in bytes.  _LENGTH symbols are \#defines instead of
 * global variables because they are usually used when declaring byte
 * arrays and some compilers require compile-time knowledge of array
 * sizes when declared on the stack.
 *
 * \{
 */
/*
Most of the values described in this file are defined by the FLAC
format specification.  There is nothing to tune here.
*/
public class Format {
	public static final boolean FLAC__HAS_OGG = true;

	public static final int SIZE_MAX = 10000;// java: added to use for checking size

	/** The largest legal metadata type code. */
	public static final int FLAC__MAX_METADATA_TYPE_CODE = (126);

	/** The minimum block size, in samples, permitted by the format. */
	public static final int FLAC__MIN_BLOCK_SIZE = (16);

	/** The maximum block size, in samples, permitted by the format. */
	public static final int FLAC__MAX_BLOCK_SIZE = (65535);

	/** The maximum block size, in samples, permitted by the FLAC subset for
	 *  sample rates up to 48kHz. */
	public static final int FLAC__SUBSET_MAX_BLOCK_SIZE_48000HZ = (4608);

	/** The maximum number of channels permitted by the format. */
	public static final int FLAC__MAX_CHANNELS = (8);

	/** The minimum sample resolution permitted by the format. */
	public static final int FLAC__MIN_BITS_PER_SAMPLE = (4);

	/** The maximum sample resolution permitted by the format. */
	public static final int FLAC__MAX_BITS_PER_SAMPLE = (32);

	/** The maximum sample resolution permitted by libFLAC.
	 *
	 * \warning
	 * FLAC__MAX_BITS_PER_SAMPLE is the limit of the FLAC format.  However,
	 * the reference encoder/decoder is currently limited to 24 bits because
	 * of prevalent 32-bit math, so make sure and use this value when
	 * appropriate.
	 */
	public static final int FLAC__REFERENCE_CODEC_MAX_BITS_PER_SAMPLE = (24);

	/** The maximum sample rate permitted by the format.  The value is
	 *  ((2 ^ 16) - 1) * 10; see <A HREF="../format.html">FLAC format</A>
	 *  as to why.
	 */
	public static final int FLAC__MAX_SAMPLE_RATE = (655350);

	/** The maximum LPC order permitted by the format. */
	public static final int FLAC__MAX_LPC_ORDER = (32);

	/** The maximum LPC order permitted by the FLAC subset for sample rates
	 *  up to 48kHz. */
	public static final int FLAC__SUBSET_MAX_LPC_ORDER_48000HZ = (12);

	/** The minimum quantized linear predictor coefficient precision
	 *  permitted by the format.
	 */
	public static final int FLAC__MIN_QLP_COEFF_PRECISION = (5);

	/** The maximum quantized linear predictor coefficient precision
	 *  permitted by the format.
	 */
	public static final int FLAC__MAX_QLP_COEFF_PRECISION = (15);

	/** The maximum order of the fixed predictors permitted by the format. */
	public static final int FLAC__MAX_FIXED_ORDER = (4);

	/** The maximum Rice partition order permitted by the format. */
	public static final int FLAC__MAX_RICE_PARTITION_ORDER = (15);

	/** The maximum Rice partition order permitted by the FLAC Subset. */
	public static final int FLAC__SUBSET_MAX_RICE_PARTITION_ORDER = (8);

	/** The version string of the release, stamped onto the libraries and binaries.
	 *
	 * \note
	 * This does not correspond to the shared library version number, which
	 * is used to determine binary compatibility.
	 */
	//public static final String FLAC__VERSION_STRING = "1.4.0";

	/** The vendor string inserted by the encoder into the VORBIS_COMMENT block.
	 *  This is a NUL-terminated ASCII string; when inserted into the
	 *  VORBIS_COMMENT the trailing null is stripped.
	 */
	//public static final String FLAC__VENDOR_STRING = "reference libFLAC " + FLAC__VERSION_STRING + " 20220909";
	public static final String FLAC__VENDOR_STRING = "Java FLAC Encoder v1.4.0";

	static final byte[] FLAC__VENDOR_STRING_BYTES;// java: added
	static {
		byte[] string = null;
		try { string = FLAC__VENDOR_STRING.getBytes("US-ASCII");
		} catch( final java.io.UnsupportedEncodingException e ) {
		}
		FLAC__VENDOR_STRING_BYTES = string;
	}

	/** The byte string representation of the beginning of a FLAC stream. */
	public static final byte FLAC__STREAM_SYNC_STRING[] = { 'f','L','a','C' };

	/** The 32-bit integer big-endian representation of the beginning of
	 *  a FLAC stream.
	 */
	public static final int FLAC__STREAM_SYNC = 0x664C6143;

	/** The length of the FLAC signature in bits. */
	public static final int FLAC__STREAM_SYNC_LEN = 32; /* bits */

	/** The length of the FLAC signature in bytes. */
	public static final int FLAC__STREAM_SYNC_LENGTH = 4;

	/** An enumeration of the available entropy coding methods. */
	//typedef enum {
		/** Residual is coded by partitioning into contexts, each with it's own
		 * 4-bit Rice parameter. */
		public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE = 0;

		/** Residual is coded by partitioning into contexts, each with it's own
		 * 5-bit Rice parameter. */
		public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2 = 1;
	//} FLAC__EntropyCodingMethodType;

	/** Maps a FLAC__EntropyCodingMethodType to a C string.
	 *
	 *  Using a FLAC__EntropyCodingMethodType as the index to this array will
	 *  give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__EntropyCodingMethodTypeString[] = {
		"PARTITIONED_RICE",
		"PARTITIONED_RICE2"
	};

	/** == 4 (bits) */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN = 4; /* bits */
	/** == 4 (bits) */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN = 4; /* bits */
	/** == 5 (bits) */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN = 5; /* bits */
	/** == 5 (bits) */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN = 5; /* bits */

	/** == (1<<FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN)-1 */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER = 15; /* == (1<<FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN)-1 */
	/** == (1<<FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN)-1 */
	public static final int FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER = 31; /* == (1<<FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN)-1 */

	/** == 2 (bits) */
	public static final int FLAC__ENTROPY_CODING_METHOD_TYPE_LEN = 2; /* bits */

	/*****************************************************************************/

	/** An enumeration of the available subframe types. */
	//typedef enum {
		/** constant signal */
		public static final int FLAC__SUBFRAME_TYPE_CONSTANT = 0;
		/** uncompressed signal */
		public static final int FLAC__SUBFRAME_TYPE_VERBATIM = 1;
		/** fixed polynomial prediction */
		public static final int FLAC__SUBFRAME_TYPE_FIXED = 2;
		/** linear prediction */
		public static final int FLAC__SUBFRAME_TYPE_LPC = 3;
	//} FLAC__SubframeType;

	/** Maps a FLAC__SubframeType to a C string.
	 *
	 *  Using a FLAC__SubframeType as the index to this array will
	 *  give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__SubframeTypeString[] = {
		"CONSTANT",
		"VERBATIM",
		"FIXED",
		"LPC"
	};

	/** An enumeration of the possible verbatim subframe data types. */
	//typedef enum {
		/**< verbatim subframe has 32-bit int */
		public static final int FLAC__VERBATIM_SUBFRAME_DATA_TYPE_INT32 = 0;
		/**< verbatim subframe has 64-bit int */
		public static final int FLAC__VERBATIM_SUBFRAME_DATA_TYPE_INT64 = 1;
	//} FLAC__VerbatimSubframeDataType;

	/** == 4 (bits) */
	static final int FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN = 4; /* bits */
	/** == 5 (bits) */
	static final int FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN = 5; /* bits */

	/** == 1 (bit)
	 *
	 * This used to be a zero-padding bit (hence the name
	 * FLAC__SUBFRAME_ZERO_PAD_LEN) but is now a reserved bit.  It still has a
	 * mandatory value of \c 0 but in the future may take on the value \c 0 or \c 1
	 * to mean something else.
	 */
	static final int FLAC__SUBFRAME_ZERO_PAD_LEN = 1; /* bits */
	/** == 6 (bits) */
	static final int FLAC__SUBFRAME_TYPE_LEN = 6; /* bits */
	/** == 1 (bit) */
	static final int FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN = 1; /* bits */

	/** = 0x00 */
	static final int FLAC__SUBFRAME_TYPE_CONSTANT_BYTE_ALIGNED_MASK = 0x00;
	/** = 0x02 */
	static final int FLAC__SUBFRAME_TYPE_VERBATIM_BYTE_ALIGNED_MASK = 0x02;
	/** = 0x10 */
	static final int FLAC__SUBFRAME_TYPE_FIXED_BYTE_ALIGNED_MASK = 0x10;
	/** = 0x40 */
	static final int FLAC__SUBFRAME_TYPE_LPC_BYTE_ALIGNED_MASK = 0x40;

	/** An enumeration of the available channel assignments. */
	//typedef enum {
		/** independent channels */
		public static final int FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT = 0;
		/** left+side stereo */
		public static final int FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE = 1;
		/** right+side stereo */
		public static final int FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE = 2;
		/** mid+side stereo */
		public static final int FLAC__CHANNEL_ASSIGNMENT_MID_SIDE = 3;
	//} FLAC__ChannelAssignment;

	/** Maps a FLAC__ChannelAssignment to a C string.
	 *
	 *  Using a FLAC__ChannelAssignment as the index to this array will
	 *  give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__ChannelAssignmentString[] = {
		"INDEPENDENT",
		"LEFT_SIDE",
		"RIGHT_SIDE",
		"MID_SIDE"
	};

	/** An enumeration of the possible frame numbering methods. */
	//typedef enum {
		/** number contains the frame number */
		public static final boolean FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER = false;
		/** number contains the sample number of first sample in frame */
		public static final boolean FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER = true;
	//} FLAC__FrameNumberType;

	/** Maps a FLAC__FrameNumberType to a C string.
	 *
	 *  Using a FLAC__FrameNumberType as the index to this array will
	 *  give the string equivalent.  The contents should not be modified.
	 */
	static final String FLAC__FrameNumberTypeString[] = {
		"FRAME_NUMBER_TYPE_FRAME_NUMBER",
		"FRAME_NUMBER_TYPE_SAMPLE_NUMBER"
	};

	/** == 0x3ffe; the frame header sync code */
	public static final int FLAC__FRAME_HEADER_SYNC = 0x3ffe;
	/** == 14 (bits) */
	public static final int FLAC__FRAME_HEADER_SYNC_LEN = 14; /* bits */
	/** == 1 (bits) */
	public static final int FLAC__FRAME_HEADER_RESERVED_LEN = 1; /* bits */
	/** == 1 (bits) */
	public static final int FLAC__FRAME_HEADER_BLOCKING_STRATEGY_LEN = 1; /* bits */
	/** == 4 (bits) */
	public static final int FLAC__FRAME_HEADER_BLOCK_SIZE_LEN = 4; /* bits */
	/** == 4 (bits) */
	public static final int FLAC__FRAME_HEADER_SAMPLE_RATE_LEN = 4; /* bits */
	/** == 4 (bits) */
	public static final int FLAC__FRAME_HEADER_CHANNEL_ASSIGNMENT_LEN = 4; /* bits */
	/** == 3 (bits) */
	public static final int FLAC__FRAME_HEADER_BITS_PER_SAMPLE_LEN = 3; /* bits */
	/** == 1 (bit) */
	public static final int FLAC__FRAME_HEADER_ZERO_PAD_LEN = 1; /* bits */
	/** == 8 (bits) */
	public static final int FLAC__FRAME_HEADER_CRC_LEN = 8; /* bits */

	/** == 16 (bits) */
	public static final int FLAC__FRAME_FOOTER_CRC_LEN = 16; /* bits */

	/*****************************************************************************
	 *
	 * Meta-data structures
	 *
	 *****************************************************************************/

	/** An enumeration of the available metadata block types. */
	//typedef enum {
		/** <A HREF="../format.html#metadata_block_streaminfo">STREAMINFO</A> block */
		public static final int FLAC__METADATA_TYPE_STREAMINFO = 0;

		/** <A HREF="../format.html#metadata_block_padding">PADDING</A> block */
		public static final int FLAC__METADATA_TYPE_PADDING = 1;

		/** <A HREF="../format.html#metadata_block_application">APPLICATION</A> block */
		public static final int FLAC__METADATA_TYPE_APPLICATION = 2;

		/** <A HREF="../format.html#metadata_block_seektable">SEEKTABLE</A> block */
		public static final int FLAC__METADATA_TYPE_SEEKTABLE = 3;

		/** <A HREF="../format.html#metadata_block_vorbis_comment">VORBISCOMMENT</A> block (a.k.a. FLAC tags) */
		public static final int FLAC__METADATA_TYPE_VORBIS_COMMENT = 4;

		/** <A HREF="../format.html#metadata_block_cuesheet">CUESHEET</A> block */
		public static final int FLAC__METADATA_TYPE_CUESHEET = 5;

		/** <A HREF="../format.html#metadata_block_picture">PICTURE</A> block */
		public static final int FLAC__METADATA_TYPE_PICTURE = 6;

		/** marker to denote beginning of undefined type range; this number will increase as new metadata types are added */
		public static final int FLAC__METADATA_TYPE_UNDEFINED = 7;

		/** No type will ever be greater than this. There is not enough room in the protocol block. */
		public static final int FLAC__MAX_METADATA_TYPE = FLAC__MAX_METADATA_TYPE_CODE;

	//} FLAC__MetadataType;

	/** Maps a FLAC__MetadataType to a C string.
	 *
	 *  Using a FLAC__MetadataType as the index to this array will
	 *  give the string equivalent.  The contents should not be modified.
	 */
	static final String FLAC__MetadataTypeString[] = {
		"STREAMINFO",
		"PADDING",
		"APPLICATION",
		"SEEKTABLE",
		"VORBIS_COMMENT",
		"CUESHEET",
		"PICTURE"
	};

	public static final int FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN = 16; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN = 16; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN = 24; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN = 24; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN = 20; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN = 3; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN = 5; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN = 36; /* bits */
	public static final int FLAC__STREAM_METADATA_STREAMINFO_MD5SUM_LEN = 128; /* bits */

	/** The total stream length of the STREAMINFO block in bytes. */
	static final int FLAC__STREAM_METADATA_STREAMINFO_LENGTH = 34;

	public static final int FLAC__STREAM_METADATA_APPLICATION_ID_LEN = 32; /* bits */

	public static final int FLAC__STREAM_METADATA_SEEKPOINT_SAMPLE_NUMBER_LEN = 64; /* bits */
	public static final int FLAC__STREAM_METADATA_SEEKPOINT_STREAM_OFFSET_LEN = 64; /* bits */
	public static final int FLAC__STREAM_METADATA_SEEKPOINT_FRAME_SAMPLES_LEN = 16; /* bits */

	/** The total stream length of a seek point in bytes. */
	public static final int FLAC__STREAM_METADATA_SEEKPOINT_LENGTH = (18);

	public static final long FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER = (0xffffffffffffffffL);

	public static final int FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN = 32; /* bits */

	public static final int FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN = 64; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN = 8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN = 3*8; /* bits */

	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN = 64; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN = 8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN = 12*8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN = 1; /* bit */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN = 1; /* bit */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN = 6+13*8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN = 8; /* bits */

	public static final int FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN = 128*8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN = 64; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN = 1; /* bit */
	public static final int FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN = 7+258*8; /* bits */
	public static final int FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN = 8; /* bits */

	/** An enumeration of the PICTURE types (see FLAC__StreamMetadataPicture and id3 v2.4 APIC tag). */
	//typedef enum {
		/** Other */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_OTHER = 0;
		/** 32x32 pixels 'file icon' (PNG only) */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON_STANDARD = 1;
		/** Other file icon */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON = 2;
		/** Cover (front) */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_FRONT_COVER = 3;
		/** Cover (back) */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_BACK_COVER = 4;
		/** Leaflet page */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_LEAFLET_PAGE = 5;
		/** Media (e.g. label side of CD) */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_MEDIA = 6;
		/** Lead artist/lead performer/soloist */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_LEAD_ARTIST = 7;
		/** Artist/performer */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_ARTIST = 8;
		/** Conductor */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_CONDUCTOR = 9;
		/** Band/Orchestra */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_BAND = 10;
		/** Composer */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_COMPOSER = 11;
		/** Lyricist/text writer */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_LYRICIST = 12;
		/** Recording Location */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_RECORDING_LOCATION = 13;
		/** During recording */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_DURING_RECORDING = 14;
		/** During performance */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_DURING_PERFORMANCE = 15;
		/** Movie/video screen capture */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_VIDEO_SCREEN_CAPTURE = 16;
		/** A bright coloured fish */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_FISH = 17;
		/** Illustration */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_ILLUSTRATION = 18;
		/** Band/artist logotype */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_BAND_LOGOTYPE = 19;
		/** Publisher/Studio logotype */
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_PUBLISHER_LOGOTYPE = 20;
		public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_UNDEFINED = 21;
	//} FLAC__StreamMetadata_Picture_Type;

	/** Maps a FLAC__StreamMetadata_Picture_Type to a C string.
	 *
	 *  Using a FLAC__StreamMetadata_Picture_Type as the index to this array
	 *  will give the string equivalent.  The contents should not be
	 *  modified.
	 */
	static final String FLAC__StreamMetadata_Picture_TypeString[] = {
		"Other",
		"32x32 pixels 'file icon' (PNG only)",
		"Other file icon",
		"Cover (front)",
		"Cover (back)",
		"Leaflet page",
		"Media (e.g. label side of CD)",
		"Lead artist/lead performer/soloist",
		"Artist/performer",
		"Conductor",
		"Band/Orchestra",
		"Composer",
		"Lyricist/text writer",
		"Recording Location",
		"During recording",
		"During performance",
		"Movie/video screen capture",
		"A bright coloured fish",
		"Illustration",
		"Band/artist logotype",
		"Publisher/Studio logotype"
	};

	public static final int FLAC__STREAM_METADATA_PICTURE_TYPE_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_COLORS_LEN = 32; /* bits */
	public static final int FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN = 32; /* bits */

	public static final int FLAC__STREAM_METADATA_IS_LAST_LEN = 1; /* bits */
	public static final int FLAC__STREAM_METADATA_TYPE_LEN = 7; /* bits */
	public static final int FLAC__STREAM_METADATA_LENGTH_LEN = 24; /* bits */

	/** The total stream length of a metadata block header in bytes. */
	public static final int FLAC__STREAM_METADATA_HEADER_LENGTH = 4;

	public static int memcmp(final byte[] dim1, final int offset1, final byte[] dim2, int offset2, int count) {
		if( dim1.length - offset1 < count ) {
			return -1;
		}
		if( dim2.length - offset2 < count ) {
			return 1;
		}
		count += offset1;
		for( int i = offset1; i < count; i++, offset2++ ) {
			if( dim1[i] != dim2[offset2] ) {
				return dim1[i] - dim2[offset2];
			}
		}
		return 0;
	}
	// java: added, http://svn.ruby-lang.org/repos/ruby/branches/ruby_1_8/missing/strtod.c
	/*
	 * strtod.c --
	 *
	 *	Source code for the "strtod" library procedure.
	 *
	 * Copyright (c) 1988-1993 The Regents of the University of California.
	 * Copyright (c) 1994 Sun Microsystems, Inc.
	 */
	/** Largest possible base 10 exponent.  Any
	 * exponent larger than this will already
	 * produce underflow or overflow, so there's
	 * no need to worry about additional digits.
	 */
	private static final int maxExponent = 511;
	/** Table giving binary powers of 10.  Entry */
	private static final double powersOf10[] = {
		10.,			// is 10^2^i.  Used to convert decimal
		100.,			// exponents into floating-point numbers.
		1.0e4,
		1.0e8,
		1.0e16,
		1.0e32,
		1.0e64,
		1.0e128,
		1.0e256
	};

	/**
	 *----------------------------------------------------------------------
	 *
	 * strtod --
	 *
	 *	This procedure converts a floating-point number from an ASCII
	 *	decimal representation to internal double-precision format.
	 *
	 * Results:
	 *	The return value is the double-precision floating-point
	 *	representation of the characters in string.  If endPtr isn't
	 *	NULL, then *endPtr is filled in with the address of the
	 *	next character after the last one that was part of the
	 *	floating-point number.
	 *
	 * Side effects:
	 *	None.
	 *
	 *----------------------------------------------------------------------
	 */
	static final double strtod(final String string)// java: not using Double.parseDouble, because c decodes dirty strings
	{
		boolean sign, expSign = false;
		int exp = 0;		// Exponent read from "EX" field.
		int fracExp = 0;		// Exponent that derives from the fractional
						// part.  Under normal circumstances, it is
						// the negative of the number of digits in F.
						// However, if I is very long, the last digits
						// of I get dropped (otherwise a long I with a
						// large negative exponent could cause an
						// unnecessary overflow on I alone).  In this
						// case, fracExp is incremented one for each
						// dropped digit.
		int mantSize;		// Number of digits in mantissa.
		int decPt;			// Number of mantissa digits BEFORE decimal point.
		int pExp;		// Temporarily holds location of exponent in string.

		// Strip off leading blanks and check for a sign.

		int p = 0;
		while( Character.isSpaceChar( string.charAt( p ) ) ) {// TODO may be isWhitespace?
			p++;
		}
		if( string.charAt( p ) == '-' ) {
			sign = true;
			p++;
		} else {
			if( string.charAt( p ) == '+' ) {
				p++;
			}
			sign = false;
		}

		// Count the number of digits in the mantissa (including the decimal
		// point), and also locate the decimal point.
		decPt = -1;
		for( mantSize = 0; ; mantSize++ ) {
			final int c = string.charAt( p );
			if( ! Character.isDigit( c ) ) {
				if( (c != '.') || (decPt >= 0) ) {
					break;
				}
				decPt = mantSize;
			}
			p++;
		}

		// Now suck up the digits in the mantissa.  Use two integers to
		// collect 9 digits each (this is faster than using floating-point).
		// If the mantissa has more than 18 digits, ignore the extras, since
		// they can't affect the value anyway.

		pExp  = p;
		p -= mantSize;
		if( decPt < 0 ) {
			decPt = mantSize;
		} else {
			mantSize--;			// One of the digits was the point.
		}
		if( mantSize > 18 ) {
			fracExp = decPt - 18;
			mantSize = 18;
		} else {
			fracExp = decPt - mantSize;
		}
		double fraction;
		if( mantSize == 0 ) {
			fraction = 0.0;
			if( sign ) {
				return -fraction;
			}
			return fraction;
		} else {
			int frac1, frac2;
			frac1 = 0;
			for( ; mantSize > 9; mantSize-- ) {
				int c = string.charAt( p++ );
				if( c == '.' ) {
					c = string.charAt( p++ );
				}
				frac1 = 10 * frac1 + (c - '0');
			}
			frac2 = 0;
			for( ; mantSize > 0; mantSize-- ) {
				int c = string.charAt( p++ );
				if( c == '.' ) {
					c = string.charAt( p++ );
				}
				frac2 = 10 * frac2 + (c - '0');
			}
			fraction = (1.0e9 * frac1) + frac2;
		}

		// Skim off the exponent.
		p = pExp;
		if( (string.charAt( p ) == 'E') || (string.charAt( p ) == 'e') ) {
			p++;
			if( string.charAt( p ) == '-' ) {
				expSign = true;
				p++;
			} else {
				if( string.charAt( p ) == '+' ) {
					p++;
				}
				expSign = false;
			}
			while( Character.isDigit( string.charAt( p ) ) ) {
				exp = exp * 10 + (string.charAt( p++ ) - '0');
			}
		}
		if( expSign ) {
			exp = fracExp - exp;
		} else {
			exp = fracExp + exp;
		}

		// Generate a floating-point number that represents the exponent.
		// Do this by processing the exponent one bit at a time to combine
		// many powers of 2 of 10. Then combine the exponent with the
		// fraction.

		if( exp < 0 ) {
			expSign = true;
			exp = -exp;
		} else {
			expSign = false;
		}
		if( exp > maxExponent ) {
			exp = maxExponent;
			throw new NumberFormatException("errno = ERANGE");
		}
		double dblExp = 1.0;
		for( int d = 0; exp != 0; exp >>= 1, d++ ) {
			if( (exp & 01) != 0 ) {
				dblExp *= powersOf10[d];
			}
		}
		if( expSign ) {
			fraction /= dblExp;
		} else {
			fraction *= dblExp;
		}

		if( sign ) {
			return -fraction;
		}
		return fraction;
	}

	// bitmath.c
	/** An example of what bitmath_ilog2() computes:
	 *
	 * ilog2( 0) = assertion failure
	 * ilog2( 1) = 0
	 * ilog2( 2) = 1
	 * ilog2( 3) = 1
	 * ilog2( 4) = 2
	 * ilog2( 5) = 2
	 * ilog2( 6) = 2
	 * ilog2( 7) = 2
	 * ilog2( 8) = 3
	 * ilog2( 9) = 3
	 * ilog2(10) = 3
	 * ilog2(11) = 3
	 * ilog2(12) = 3
	 * ilog2(13) = 3
	 * ilog2(14) = 3
	 * ilog2(15) = 3
	 * ilog2(16) = 4
	 * ilog2(17) = 4
	 * ilog2(18) = 4
	 */
	static int bitmath_ilog2(int v)
	{
		int l = 0;
		//FLAC__ASSERT(v > 0);
		while( (v >>>= 1) != 0 ) {
			l++;
		}
		return l;
	}

	/* FIXME never used
	static int bitmath_ilog2_wide(long v)
	{
		int l = 0;
		//FLAC__ASSERT(v > 0);
		while( (v >>>= 1) != 0 )
			l++;
		return l;
	}
	*/
	/**  Brain-damaged compilers will use the fastest possible way that is,
	de Bruijn sequences (http://supertech.csail.mit.edu/papers/debruijn.pdf)
	(C) Timothy B. Terriberry (tterribe@xiph.org) 2001-2009 CC0 (Public domain).
	 */
	private static final byte DEBRUIJN_IDX64[] = {// [64]={
			0, 1, 2, 7, 3,13, 8,19, 4,25,14,28, 9,34,20,40,
			5,17,26,38,15,46,29,48,10,31,35,54,21,50,41,57,
			63, 6,12,18,24,27,33,39,16,37,45,47,30,53,49,56,
			62,11,23,32,36,44,52,55,61,22,43,51,60,42,59,58
		};
	static int bitmath_ilog2_wide(long v)
	{

		v|= v >>> 1;
		v|= v >>> 2;
		v|= v >>> 4;
		v|= v >>> 8;
		v|= v >>> 16;
		v|= v >>> 32;
		v= (v >>> 1) + 1;
		return DEBRUIJN_IDX64[(int)(v * (0x218A392CD3D5DBFL) >> 58 & 0x3F)];
	}

	/** An example of what bitmath_silog2() computes:
	 *
	 * silog2(-10) = 5
	 * silog2(- 9) = 5
	 * silog2(- 8) = 4
	 * silog2(- 7) = 4
	 * silog2(- 6) = 4
	 * silog2(- 5) = 4
	 * silog2(- 4) = 3
	 * silog2(- 3) = 3
	 * silog2(- 2) = 2
	 * silog2(- 1) = 2
	 * silog2(  0) = 0
	 * silog2(  1) = 2
	 * silog2(  2) = 3
	 * silog2(  3) = 3
	 * silog2(  4) = 4
	 * silog2(  5) = 4
	 * silog2(  6) = 4
	 * silog2(  7) = 4
	 * silog2(  8) = 5
	 * silog2(  9) = 5
	 * silog2( 10) = 5
	 */
	static int bitmath_silog2(long v) {
		if( v == 0 ) {
			return 0;
		}
		if( v == -1 ) {
			return 2;
		}
		v = (v < 0) ? (-(v + 1)) : v;
		return bitmath_ilog2_wide( v ) + 2;
	}

	// end bitmath.c

	/** Tests that a sample rate is valid for FLAC.
	 *
	 * \param sample_rate  The sample rate to test for compliance.
	 * \retval FLAC__bool
	 *    \c true if the given sample rate conforms to the specification, else
	 *    \c false.
	 */
	public static boolean sample_rate_is_valid(final int sample_rate)
	{
		if( sample_rate == 0 || sample_rate > FLAC__MAX_SAMPLE_RATE ) {
			return false;
		}
		//else
			return true;
	}

	/** Tests that a blocksize at the given sample rate is valid for the FLAC
	 *  subset.
	 *
	 * \param blocksize    The blocksize to test for compliance.
	 * \param sample_rate  The sample rate is needed, since the valid subset
	 *                     blocksize depends on the sample rate.
	 * \retval FLAC__bool
	 *    \c true if the given blocksize conforms to the specification for the
	 *    subset at the given sample rate, else \c false.
	 */
	public static boolean blocksize_is_subset(final int blocksize, final int sample_rate)
	{
		if( blocksize > 16384 ) {
			return false;
		} else if( sample_rate <= 48000 && blocksize > 4608 ) {
			return false;
		} else {
			return true;
		}
	}

	/** Tests that a sample rate is valid for the FLAC subset.  The subset rules
	 *  for valid sample rates are slightly more complex since the rate has to
	 *  be expressible completely in the frame header.
	 *
	 * \param sample_rate  The sample rate to test for compliance.
	 * \retval FLAC__bool
	 *    \c true if the given sample rate conforms to the specification for the
	 *    subset, else \c false.
	 */
	public static boolean sample_rate_is_subset(final int sample_rate)
	{
		if( // sample rate is not subset if
			! sample_rate_is_valid( sample_rate ) || // sample rate is invalid or
			sample_rate >= ((1 << 16) * 10) || // sample rate is larger then or equal to 655360 or
			(sample_rate >= (1 << 16) && sample_rate % 10 != 0) //sample rate is >= 65536 and not divisible by 10
			) {
				return false;
			}
			//else
				return true;
	}

	/** used as the sort predicate for qsort() */
	/*private static int seekpoint_compare_(final SeekPoint l, final SeekPoint r)
	{// moved to SeekPoint as Comparable interface
		// we don't just 'return l->sample_number - r->sample_number' since the result (FLAC__int64) might overflow an 'int'
		if( l.sample_number == r.sample_number )
			return 0;
		else if( l.sample_number < r.sample_number )
			return -1;
		else
			return 1;
	}*/

	/**
	 * also disallows non-shortest-form encodings, c.f.
	 *   http://www.unicode.org/versions/corrigendum1.html
	 * and a more clear explanation at the end of this section:
	 *   http://www.cl.cam.ac.uk/~mgk25/unicode.html#utf-8
	 */
	/* java: uses String, so the function do not needed
	private static int utf8len_(final byte[] utf8, int offset)
	{
		//FLAC__ASSERT(0 != utf8);
		if( (utf8[offset + 0] & 0x80) == 0 ) {
			return 1;
		}
		else if( (utf8[offset + 0] & 0xE0) == 0xC0 && (utf8[offset + 1] & 0xC0) == 0x80 ) {
			if ((utf8[offset + 0] & 0xFE) == 0xC0) // overlong sequence check
				return 0;
			return 2;
		}
		else if( (utf8[offset + 0] & 0xF0) == 0xE0 && (utf8[offset + 1] & 0xC0) == 0x80 && (utf8[offset + 2] & 0xC0) == 0x80 ) {
			if( utf8[offset + 0] == 0xE0 && (utf8[offset + 1] & 0xE0) == 0x80 ) // overlong sequence check
				return 0;
			// illegal surrogates check (U+D800...U+DFFF and U+FFFE...U+FFFF)
			if( utf8[offset + 0] == 0xED && (utf8[offset + 1] & 0xE0) == 0xA0 ) // D800-DFFF
				return 0;
			if( utf8[offset + 0] == 0xEF && utf8[offset + 1] == 0xBF && (utf8[offset + 2] & 0xFE) == 0xBE ) // FFFE-FFFF
				return 0;
			return 3;
		}
		else if( (utf8[offset + 0] & 0xF8) == 0xF0 && (utf8[offset + 1] & 0xC0) == 0x80 && (utf8[offset + 2] & 0xC0) == 0x80 && (utf8[offset + 3] & 0xC0) == 0x80 ) {
			if( utf8[offset + 0] == 0xF0 && (utf8[offset + 1] & 0xF0) == 0x80 ) // overlong sequence check
				return 0;
			return 4;
		}
		else if( (utf8[offset + 0] & 0xFC) == 0xF8 && (utf8[offset + 1] & 0xC0) == 0x80 && (utf8[offset + 2] & 0xC0) == 0x80 && (utf8[offset + 3] & 0xC0) == 0x80 && (utf8[offset + 4] & 0xC0) == 0x80 ) {
			if( utf8[offset + 0] == 0xF8 && (utf8[offset + 1] & 0xF8) == 0x80 ) // overlong sequence check
				return 0;
			return 5;
		}
		else if( (utf8[offset + 0] & 0xFE) == 0xFC && (utf8[offset + 1] & 0xC0) == 0x80 && (utf8[offset + 2] & 0xC0) == 0x80 && (utf8[offset + 3] & 0xC0) == 0x80 && (utf8[offset + 4] & 0xC0) == 0x80 && (utf8[offset + 5] & 0xC0) == 0x80 ) {
			if( utf8[offset + 0] == 0xFC && (utf8[offset + 1] & 0xFC) == 0x80 ) // overlong sequence check
				return 0;
			return 6;
		}
		else {
			return 0;
		}
	}

	public static final boolean vorbiscomment_entry_name_is_legal(final byte[] name)
	{
		char c;
		for( c = *name; c; c = *(++name) )
			if(c < 0x20 || c == 0x3d || c > 0x7d)
				return false;
		return true;
	}

	public static final boolean vorbiscomment_entry_value_is_legal(final FLAC__byte *value, unsigned length)
	{
		if( length == (unsigned)(-1) ) {
			while( *value ) {
				unsigned n = utf8len_( value );
				if(n == 0)
					return false;
				value += n;
			}
		}
		else {
			final FLAC__byte *end = value + length;
			while( value < end ) {
				unsigned n = utf8len_( value );
				if( n == 0 )
					return false;
				value += n;
			}
			if( value != end )
				return false;
		}
		return true;
	}

	public static final boolean vorbiscomment_entry_is_legal(final FLAC__byte *entry, unsigned length)
	{
		final FLAC__byte *s, *end;

		for( s = entry, end = s + length; s < end && *s != '='; s++ ) {
			if( *s < 0x20 || *s > 0x7D )
				return false;
		}
		if( s == end )
			return false;

		s++; // skip '='

		while( s < end ) {
			unsigned n = utf8len_( s );
			if( n == 0 )
				return false;
			s += n;
		}
		if( s != end )
			return false;

		return true;
	}
	*/

}

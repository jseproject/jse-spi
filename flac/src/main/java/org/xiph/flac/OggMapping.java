package org.xiph.flac;

class OggMapping {
	/** The length of the packet type field in bytes. */
	static final int FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH = 1;

	static final int FLAC__OGG_MAPPING_PACKET_TYPE_LEN = 8;/* bits */

	static final byte FLAC__OGG_MAPPING_FIRST_HEADER_PACKET_TYPE = 0x7f;

	/** The length of the 'FLAC' magic in bytes. */
	static final int FLAC__OGG_MAPPING_MAGIC_LENGTH = 4;

	static final byte FLAC__OGG_MAPPING_MAGIC[] = { 'F','L','A','C' }; /* = "FLAC" */

	static final int FLAC__OGG_MAPPING_VERSION_MAJOR_LEN = 8; /* bits */
	static final int FLAC__OGG_MAPPING_VERSION_MINOR_LEN = 8; /* bits */

	/** The length of the Ogg FLAC mapping major version number in bytes. */
	static final int FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH = 1;

	/** The length of the Ogg FLAC mapping minor version number in bytes. */
	static final int FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH = 1;

	static final int FLAC__OGG_MAPPING_NUM_HEADERS_LEN = 16; /* bits */

	/** The length of the #-of-header-packets number bytes. */
	static final int FLAC__OGG_MAPPING_NUM_HEADERS_LENGTH = 2;
}

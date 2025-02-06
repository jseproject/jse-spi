package org.xiph.flac;

/**
 * Helper class to read utf-8 header
 */
class RawHeaderHelper {
	final byte[] raw_header = new byte[16]; /* MAGIC NUMBER based on the maximum frame header size, including CRC */
	int raw_header_len = 0;
}

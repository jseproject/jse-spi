package org.xiph.flac;

/**
 * Changes from C to Java:
 * <pre>
 * byte data = iov_packet[iov_base]
 * int data = ((int)iov_packet[iov_base]) & 0xff
 * </pre>
 */
class IOV_EC {
	int iov_base;
	byte[] iov_packet;
	int iov_len;
}

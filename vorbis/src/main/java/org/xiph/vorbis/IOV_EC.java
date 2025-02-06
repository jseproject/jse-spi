package org.xiph.vorbis;

/**
 * Changes from C to Java:<p>
 * <code>byte data = iov_packet[iov_base]</code><br>
 * <code>int data = ((int)iov_packet[iov_base]) & 0xff</code>
 */
class IOV_EC {
    int iov_base;
    byte[] iov_packet;
    int iov_len;
}

/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2019 Alexey Kuznetsov
 * Copyright (c) 2002-2018 Xiph.Org Foundation
 * Copyright (c) 1994-1996 James Gosling,
 *                         Kevin A. Smith, Sun Microsystems, Inc.
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
 * - Neither the name of the Xiph.org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.vorbis;

import java.util.Arrays;

// XXX what is oggpackB_ functions? may be, it can be removed?
public class Buffer {
    int endbyte = 0;
    int endbit = 0;
    public byte[] buffer = null;
    int ptr = 0;
    public int storage = 0;

    public void clear() {
        this.endbyte = 0;
        this.endbit = 0;
        this.buffer = null;
        this.ptr = 0;
        this.storage = 0;
    }

    // bitwise.c
    private static final int BUFFER_INCREMENT = 256;

    private static final int mask[] = {
            0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000f,
            0x0000001f, 0x0000003f, 0x0000007f, 0x000000ff, 0x000001ff,
            0x000003ff, 0x000007ff, 0x00000fff, 0x00001fff, 0x00003fff,
            0x00007fff, 0x0000ffff, 0x0001ffff, 0x0003ffff, 0x0007ffff,
            0x000fffff, 0x001fffff, 0x003fffff, 0x007fffff, 0x00ffffff,
            0x01ffffff, 0x03ffffff, 0x07ffffff, 0x0fffffff, 0x1fffffff,
            0x3fffffff, 0x7fffffff, 0xffffffff};

    private static final int mask8B[] = {
            0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff};

    public final void pack_writeinit() {
        this.endbit = 0;
        this.endbyte = 0;
        this.ptr = 0;
        this.buffer = new byte[BUFFER_INCREMENT];
        // buffer[0] = '\0';// java: already zeroed
        this.storage = BUFFER_INCREMENT;
    }

    public final int pack_writecheck() {
        if (this.ptr < 0 || this.storage == 0) {
            return -1;
        }
        return 0;
    }

    public final int packB_writecheck() {
        return pack_writecheck();
    }

    public final void pack_writetrunc(int bits) {
        final int bytes = bits >> 3;
        if (this.ptr >= 0) {
            bits -= bytes << 3;
            this.ptr = bytes;
            this.endbit = bits;
            this.endbyte = bytes;
            this.buffer[this.ptr] &= mask[bits];
        }
    }

    public final void packB_writetrunc(int bits) {
        final int bytes = bits >> 3;
        if (this.ptr >= 0) {
            bits -= bytes << 3;
            this.ptr = bytes;
            this.endbit = bits;
            this.endbyte = bytes;
            this.buffer[this.ptr] &= mask8B[bits];
        }
    }

    public final void packB_writeinit() {
        pack_writeinit();
    }

    public final void pack_writeclear() {
        this.buffer = null;
        this.endbit = 0;
        this.endbyte = 0;
        this.ptr = -1;
        this.storage = 0;
    }

    public final void packB_writeclear() {
        pack_writeclear();
    }

    /**
     * Takes only up to 32 bits.
     */
    public final void pack_write(int value, int bits) {
        if (bits < 0 || bits > 32) {
            pack_writeclear();
            return;
        }
        if (this.endbyte >= this.storage - 4) {
            if (this.ptr < 0) {
                return;
            }
            if (this.storage > Integer.MAX_VALUE - BUFFER_INCREMENT) {
                pack_writeclear();
                return;
            }
            this.storage += BUFFER_INCREMENT;
            this.buffer = Arrays.copyOf(this.buffer, this.storage);
            this.ptr = this.endbyte;
        }

        value &= mask[bits];
        final int end = this.endbit;// java
        bits += end;

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        b[off] |= value << end;

        if (bits >= 8) {
            b[++off] = (byte) (value >>> (8 - end));
            if (bits >= 16) {
                b[++off] = (byte) (value >>> (16 - end));
                if (bits >= 24) {
                    b[++off] = (byte) (value >>> (24 - end));
                    if (bits >= 32) {
                        if (end != 0) {
                            b[++off] = (byte) (value >>> (32 - end));
                        } else {
                            b[++off] = 0;
                        }
                    }
                }
            }
        }

        this.endbit = bits & 7;
        bits >>>= 3;
        this.endbyte += bits;
        this.ptr += bits;
        return;
    }

    /**
     * Takes only up to 32 bits.
     */
    private final void packB_write(int value, int bits) {
        if (bits < 0 || bits > 32) {
            pack_writeclear();
            return;
        }
        if (this.endbyte >= this.storage - 4) {
            if (this.ptr < 0) {
                return;
            }
            if (this.storage > Integer.MAX_VALUE - BUFFER_INCREMENT) {
                pack_writeclear();
                return;
            }
            this.storage += BUFFER_INCREMENT;
            this.buffer = Arrays.copyOf(this.buffer, this.storage);
            this.ptr = this.endbyte;
        }

        value = (value & mask[bits]) << (32 - bits);
        final int end = this.endbit;// java
        bits += end;

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        b[off] |= value >>> (24 + end);

        if (bits >= 8) {
            b[++off] = (byte) (value >>> (16 + end));
            if (bits >= 16) {
                b[++off] = (byte) (value >>> (8 + end));
                if (bits >= 24) {
                    b[++off] = (byte) (value >>> (end));
                    if (bits >= 32) {
                        if (end != 0) {
                            b[++off] = (byte) (value << (8 - end));
                        } else {
                            b[++off] = 0;
                        }
                    }
                }
            }
        }

        this.endbit = bits & 7;
        bits >>>= 3;
        this.endbyte += bits;
        this.ptr += bits;

        return;
    }

    public final void pack_writealign() {
        final int bits = 8 - this.endbit;
        if (bits < 8) {
            pack_write(0, bits);
        }
    }

    public final void packB_writealign() {
        final int bits = 8 - this.endbit;
        if (bits < 8) {
            packB_write(0, bits);
        }
    }

    private final void pack_writecopy_helper(
            final byte[] source,
            int bits,
            // void (*w)(oggpack_buffer *, unsigned long, int),// java: use if( msb ) oggpackB_write : oggpack_write;
            final boolean msb) {

        final int bytes = bits >>> 3;
        final int pbytes = (this.endbit + bits) >>> 3;
        bits -= bytes << 3;

        /* expand storage up-front */
        if (this.endbyte + pbytes >= this.storage) {
            if (this.ptr < 0) {
                pack_writeclear();
                return;
            }
            if (this.storage > this.endbyte + pbytes + BUFFER_INCREMENT) {
                pack_writeclear();
                return;
            }
            this.storage = this.endbyte + pbytes + BUFFER_INCREMENT;
            this.buffer = Arrays.copyOf(this.buffer, this.storage);
            this.ptr = this.endbyte;
        }

        /* copy whole octets */
        if (this.endbit != 0) {
            /* unaligned copy.  Do it the hard way. */
            if (msb) {
                for (int i = 0; i < bytes; i++) {
                    packB_write((int) source[i], 8);
                }
            } else {
                for (int i = 0; i < bytes; i++) {
                    pack_write((int) source[i], 8);
                }
            }
        } else {
            /* aligned block copy */
            System.arraycopy(source, 0, this.buffer, this.ptr, bytes);
            this.ptr += bytes;
            this.endbyte += bytes;
            this.buffer[this.ptr] = 0;
        }

        /* copy trailing bits */
        if (bits != 0) {
            if (msb) {
                packB_write((int) (source[bytes] >>> (8 - bits)), bits);
            } else {
                pack_write((int) source[bytes], bits);
            }
        }
    }

    public final void pack_writecopy(final byte[] source, final int bits) {
        pack_writecopy_helper(source, bits, false);
    }

    public final void packB_writecopy(final byte[] source, final int bits) {
        pack_writecopy_helper(source, bits, true);
    }

    public final void pack_reset() {
        if (this.ptr < 0) {
            return;
        }
        this.ptr = 0;
        this.buffer[0] = 0;
        this.endbit = this.endbyte = 0;
    }

    public final void packB_reset() {
        pack_reset();
    }

    private final void pack_readinit(final byte[] buf, final int bytes) {
        this.endbit = 0;
        this.endbyte = 0;
        this.buffer = buf;
        this.ptr = 0;
        this.storage = bytes;
    }

    public final void pack_readinit(final byte[] buf, final int offset, final int bytes) {// added
        this.endbit = 0;
        this.endbyte = 0;
        this.buffer = buf;
        this.ptr = offset;
        this.storage = bytes;
    }

    public final void packB_readinit(final byte[] buf, final int bytes) {
        pack_readinit(buf, bytes);
    }

    /**
     * Read in bits without advancing the bitptr; bits <= 32
     */
    public final int pack_look(int bits) {
        if (bits < 0 || bits > 32) {
            return -1;
        }
        final int m = mask[bits];// unsigned long
        final int end = this.endbit;// java
        bits += end;

        if (this.endbyte >= this.storage - 4) {
            /* not the main path */
            if (this.endbyte > this.storage - ((bits + 7) >> 3)) {
                return -1;
            } else if (bits == 0) {
                return 0;
            }
        }

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        int ret = (((int) b[off]) & 0xff) >>> end;// unsigned long
        if (bits > 8) {
            ret |= (((int) b[++off]) & 0xff) << (8 - end);
            if (bits > 16) {
                ret |= (((int) b[++off]) & 0xff) << (16 - end);
                if (bits > 24) {
                    ret |= (((int) b[++off]) & 0xff) << (24 - end);
                    if (bits > 32 && end != 0) {
                        ret |= (((int) b[++off]) & 0xff) << (32 - end);
                    }
                }
            }
        }
        return (m & ret);
    }

    /**
     * Read in bits without advancing the bitptr; bits <= 32
     */
    public final int packB_look(int bits) {
        int ret;// unsigned long
        final int m = 32 - bits;

        if (m < 0 || m > 32) {
            return -1;
        }
        final int end = this.endbit;// java
        bits += end;

        if (this.endbyte >= this.storage - 4) {
            /* not the main path */
            if (this.endbyte > this.storage - ((bits + 7) >> 3)) {
                return -1;
            } else if (bits == 0) {
                return 0;
            }
        }

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        ret = (((int) b[off]) & 0xff) << (24 + end);
        if (bits > 8) {
            ret |= (((int) b[++off]) & 0xff) << (16 + end);
            if (bits > 16) {
                ret |= (((int) b[++off]) & 0xff) << (8 + end);
                if (bits > 24) {
                    ret |= (((int) b[++off]) & 0xff) << (end);
                    if (bits > 32 && end != 0) {
                        ret |= (((int) b[++off]) & 0xff) >>> (8 - end);
                    }
                }
            }
        }
        return (ret >>> (m >> 1)) >>> ((m + 1) >> 1);
    }

    public final int pack_look1() {
        if (this.endbyte >= this.storage) {
            return (-1);
        }
        return ((this.buffer[this.ptr] >>> this.endbit) & 1);
    }

    public final int packB_look1() {
        if (this.endbyte >= this.storage) {
            return (-1);
        }
        return ((this.buffer[this.ptr] >>> (7 - this.endbit)) & 1);
    }

    public final void pack_adv(int bits) {
        bits += this.endbit;

        if (this.endbyte > this.storage - ((bits + 7) >> 3)) {// overflow
            this.ptr = -1;
            this.endbyte = this.storage;
            this.endbit = 1;
            return;
        }

        this.endbit = bits & 7;
        bits >>>= 3;
        this.ptr += bits;
        this.endbyte += bits;
        return;
    }

    public final void packB_adv(final int bits) {
        pack_adv(bits);
    }

    public final void pack_adv1() {
        if (++(this.endbit) > 7) {
            this.endbit = 0;
            this.ptr++;
            this.endbyte++;
        }
    }

    public final void packB_adv1() {
        pack_adv1();
    }

    /**
     * bits <= 32
     */
    public final int pack_read(int bits) {
        if (bits < 0 || bits > 32) {//goto err;
            this.ptr = -1;
            this.endbyte = this.storage;
            this.endbit = 1;
            return -1;
        }
        final int m = mask[bits];
        final int end = this.endbit;// java
        bits += end;

        if (this.endbyte >= this.storage - 4) {
            /* not the main path */
            if (this.endbyte > this.storage - ((bits + 7) >> 3)) {//goto overflow;
                this.ptr = -1;
                this.endbyte = this.storage;
                this.endbit = 1;
                return -1;
            }
			/* special case to avoid reading b->ptr[0], which might be past the end of
			   the buffer; also skips some useless accounting */
            else if (bits == 0) {
                return 0;
            }
        }

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        int ret = (((int) b[off]) & 0xff) >>> end;
        if (bits > 8) {
            ret |= (((int) b[++off]) & 0xff) << (8 - end);
            if (bits > 16) {
                ret |= (((int) b[++off]) & 0xff) << (16 - end);
                if (bits > 24) {
                    ret |= (((int) b[++off]) & 0xff) << (24 - end);
                    if (bits > 32 && end != 0) {
                        ret |= (((int) b[++off]) & 0xff) << (32 - end);
                    }
                }
            }
        }
        ret &= m;
        this.endbit = bits & 7;
        bits >>>= 3;
        this.ptr += bits;
        this.endbyte += bits;
        return ret;
    }

    /**
     * bits <= 32
     */
    public final int packB_read(int bits) {
        final int m = 32 - bits;

        if (m < 0 || m > 32) {//goto err;
            this.ptr = -1;
            this.endbyte = this.storage;
            this.endbit = 1;
            return -1;
        }
        final int end = this.endbit;// java
        bits += end;

        if (this.endbyte + 4 >= this.storage) {
            /* not the main path */
            if (this.endbyte > this.storage - ((bits + 7) >> 3)) {//goto overflow;
                this.ptr = -1;
                this.endbyte = this.storage;
                this.endbit = 1;
                return -1;
            }
			/* special case to avoid reading b->ptr[0], which might be past the end of
			   the buffer; also skips some useless accounting */
            else if (bits == 0) {
                return 0;
            }
        }

        final byte[] b = this.buffer;// java
        int off = this.ptr;// java
        int ret = (((int) b[off]) & 0xff) << (24 + end);
        if (bits > 8) {
            ret |= (((int) b[++off]) & 0xff) << (16 + end);
            if (bits > 16) {
                ret |= (((int) b[++off]) & 0xff) << (8 + end);
                if (bits > 24) {
                    ret |= (((int) b[++off]) & 0xff) << (end);
                    if (bits > 32 && end != 0) {
                        ret |= (((int) b[++off]) & 0xff) >>> (8 - end);
                    }
                }
            }
        }
        ret = (ret >>> (m >> 1)) >>> ((m + 1) >> 1);

        this.endbit = bits & 7;
        bits >>>= 3;
        this.ptr += bits;
        this.endbyte += bits;
        return ret;
    }

    public final int pack_read1() {
        if (this.endbyte >= this.storage) {//goto overflow;
            this.ptr = -1;
            this.endbyte = this.storage;
            this.endbit = 1;
            return -1;
        }
        final int ret = (this.buffer[this.ptr] >>> this.endbit) & 1;

        this.endbit++;
        if (this.endbit > 7) {
            this.endbit = 0;
            this.ptr++;
            this.endbyte++;
        }
        return ret;
    }

    public final int packB_read1() {
        if (this.endbyte >= this.storage) {//goto overflow;
            this.ptr = -1;
            this.endbyte = this.storage;
            this.endbit = 1;
            return -1;
        }
        final int ret = (this.buffer[this.ptr] >>> (7 - this.endbit)) & 1;

        this.endbit++;
        if (this.endbit > 7) {
            this.endbit = 0;
            this.ptr++;
            this.endbyte++;
        }
        return ret;
    }

    public final int pack_bytes() {
        return (this.endbyte + ((this.endbit + 7) >>> 3));
    }

    public final int pack_bits() {
        return ((this.endbyte << 3) + this.endbit);
    }

    public final int packB_bytes() {
        return pack_bytes();
    }

    public final int packB_bits() {
        return pack_bits();
    }

    public final byte[] pack_get_buffer() {
        return this.buffer;
    }

    public final byte[] packB_get_buffer() {
        return pack_get_buffer();
    }

/* XXX Self test of the bitwise routines; everything else is based on
//them, so they damned well better be solid.

//#ifdef _V_SELFTEST

	private static int ilog(int v) {
		int ret = 0;
		while( v != 0 ) {
			ret++;
			v >>>= 1;
		}
		return ret;
	}

	private static Buffer o = new Buffer();
	private static Buffer r = new Buffer();

	private static void report(final String in){
		System.err.println( in );
		System.exit( 1 );
	}

	@SuppressWarnings("boxing")
	private static void cliptest(final int[] b, final int vals, final int bits, final int[] comp, final int compsize) {
		int bytes, i;
		byte[] buffer;

		o.pack_reset();
		for( i = 0; i < vals; i++ ) {
			o.pack_write( b[i], (bits != 0) ? bits : ilog( b[i] ) );
		}
		buffer = o.pack_get_buffer();
		bytes = o.pack_bytes();
		if( bytes != compsize ) {
			report("wrong number of bytes!\n");
		}
		for( i = 0; i < bytes; i++ ) {
			if( (((int)buffer[i]) & 0xff) != comp[i] ) {
				for( i = 0; i < bytes; i++ ) {
					System.err.printf("%x %x\n", (int)buffer[i], comp[i] );
				}
				report("wrote incorrect value!\n");
			}
		}
		r.pack_readinit( buffer, bytes );
		for( i = 0; i < vals; i++ ) {
			final int tbit = (bits != 0) ? bits : ilog( b[i] );
			if( r.pack_look( tbit ) == -1 ) {
				report("out of data!\n");
			}
			if( r.pack_look( tbit ) != (b[i] & mask[tbit]) ) {
				report("looked at incorrect value!\n");
			}
			if( tbit == 1) {
				if( r.pack_look1() != (b[i] & mask[tbit]) ) {
					report("looked at single bit incorrect value!\n");
				}
			}
			if( tbit == 1 ) {
				if( r.pack_read1() != (b[i] & mask[tbit]) ) {
					report("read incorrect single bit value!\n");
				}
			} else {
				if( r.pack_read( tbit ) != (b[i] & mask[tbit]) ) {
					report("read incorrect value!\n");
				}
			}
		}
		if( r.pack_bytes() != bytes ) {
			report("leftover bytes after read!\n");
		}
	}

	@SuppressWarnings("boxing")
	private static void cliptestB(final int[] b, final int vals, final int bits, final int[] comp, final int compsize) {
		int bytes,i;
		byte[] buffer;

		o.packB_reset();
		for( i = 0; i < vals; i++ ) {
			o.packB_write( b[i], (bits != 0) ? bits : ilog( b[i] ) );
		}
		buffer = o.packB_get_buffer();
		bytes = o.packB_bytes();
		if( bytes != compsize ) {
			report("wrong number of bytes!\n");
		}
		for( i = 0; i < bytes; i++ ) {
			if( (((int)buffer[i]) & 0xff) != comp[i] ) {
				for( i = 0; i < bytes; i++ ) {
					System.err.printf("%x %x\n", (int)buffer[i], comp[i] );
				}
				report("wrote incorrect value!\n");
			}
		}
		r.packB_readinit( buffer, bytes );
		for( i = 0; i < vals; i++ ) {
			final int tbit = (bits != 0) ? bits : ilog( b[i] );
			if( r.packB_look( tbit ) == -1 ) {
				report("out of data!\n");
			}
			if( r.packB_look( tbit ) != (b[i] & mask[tbit]) ) {
				report("looked at incorrect value!\n");
			}
			if( tbit == 1 ) {
				if( r.packB_look1() != (b[i] & mask[tbit]) ) {
					report("looked at single bit incorrect value!\n");
				}
			}
			if( tbit == 1 ) {
				if( r.packB_read1() != (b[i] & mask[tbit]) ) {
					report("read incorrect single bit value!\n");
				}
			} else {
				if( r.packB_read( tbit ) != (b[i] & mask[tbit]) ) {
					report("read incorrect value!\n");
				}
			}
		}
		if( r.packB_bytes() != bytes ) {
			report("leftover bytes after read!\n");
		}
	}

	private static final void copytest(final int prefill, final int copy){
		final Buffer source_write = new Buffer();
		final Buffer dest_write = new Buffer();
		final Buffer source_read = new Buffer();
		final Buffer dest_read = new Buffer();
		byte[] source;
		byte[] dest;
		int source_bytes, dest_bytes;
		int i;

		source_write.pack_writeinit();
		dest_write.pack_writeinit();

		for( i = 0; i < (prefill + copy + 7) / 8; i++ ) {
			source_write.pack_write( (i ^ 0x5a) & 0xff, 8 );
		}
		source = source_write.pack_get_buffer();
		source_bytes = source_write.pack_bytes();

		// prefill
		dest_write.pack_writecopy( source, prefill );

		// check buffers; verify end byte masking
		dest = dest_write.pack_get_buffer();
		dest_bytes = dest_write.pack_bytes();
		if( dest_bytes != (prefill + 7) / 8 ) {
			System.err.printf("wrong number of bytes after prefill! %ld!=%d\n", dest_bytes, (prefill + 7) / 8 );
			System.exit( 1 );
		}
		source_read.pack_readinit( source, source_bytes );
		dest_read.pack_readinit( dest, dest_bytes );

		for( i = 0; i < prefill; i += 8 ) {
			final int s = source_read.pack_read( prefill - i < 8 ? prefill - i : 8 );
			final int d = dest_read.pack_read( prefill - i < 8 ? prefill - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d mismatch! byte %d, %x!=%x\n", prefill, i / 8, s, d );
				System.exit( 1 );
			}
		}
		if( prefill < dest_bytes ) {
			if( dest_read.pack_read( dest_bytes - prefill ) != 0 ) {
				System.err.printf("prefill=%d mismatch! trailing bits not zero\n", prefill );
				System.exit( 1 );
			}
		}

		// second copy
		dest_write.pack_writecopy( source, copy );

		// check buffers; verify end byte masking
		dest = dest_write.pack_get_buffer();
		dest_bytes = dest_write.pack_bytes();
		if( dest_bytes != (copy + prefill + 7) / 8 ) {
			System.err.printf("wrong number of bytes after prefill+copy! %ld!=%d\n", dest_bytes, (copy + prefill + 7) / 8 );
			System.exit( 1 );
		}
		source_read.pack_readinit( source, source_bytes );
		dest_read.pack_readinit( dest, dest_bytes );

		for( i = 0; i < prefill; i += 8 ) {
			final int s = source_read.pack_read( prefill - i < 8 ? prefill - i : 8 );
			final int d = dest_read.pack_read( prefill - i < 8 ? prefill - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d mismatch! byte %d, %x!=%x\n", prefill, i / 8, s, d );
				System.exit( 1 );
			}
		}

		source_read.pack_readinit( source, source_bytes );
		for( i = 0; i < copy; i += 8 ) {
			final int s = source_read.pack_read( copy - i < 8 ? copy - i : 8 );
			final int d = dest_read.pack_read( copy - i < 8 ? copy - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d copy=%d mismatch! byte %d, %x!=%x\n", prefill, copy, i / 8, s, d );
				System.exit( 1 );
			}
		}

		if( copy + prefill < dest_bytes ) {
			if( dest_read.pack_read( dest_bytes - copy - prefill ) != 0 ) {
				System.err.printf("prefill=%d copy=%d mismatch! trailing bits not zero\n", prefill, copy );
				System.exit( 1 );
			}
		}

		source_write.pack_writeclear();
		dest_write.pack_writeclear();
	}

	private static final void copytestB(final int prefill, final int copy){
		final Buffer source_write = new Buffer();
		final Buffer dest_write = new Buffer();
		final Buffer source_read = new Buffer();
		final Buffer dest_read = new Buffer();
		byte[] source;
		byte[] dest;
		int source_bytes, dest_bytes;
		int i;

		source_write.packB_writeinit();
		dest_write.packB_writeinit();

		for( i = 0; i < (prefill + copy + 7) / 8; i++ ) {
			source_write.packB_write( (i^0x5a)&0xff, 8 );
		}
		source = source_write.packB_get_buffer();
		source_bytes = source_write.packB_bytes();

		// prefill
		dest_write.packB_writecopy( source, prefill );

		// check buffers; verify end byte masking
		dest = dest_write.packB_get_buffer();
		dest_bytes = dest_write.packB_bytes();
		if( dest_bytes != (prefill + 7) / 8 ) {
			System.err.printf("wrong number of bytes after prefill! %ld!=%d\n", dest_bytes, (prefill + 7) / 8 );
			System.exit( 1 );
		}
		source_read.packB_readinit( source, source_bytes );
		dest_read.packB_readinit( dest, dest_bytes );

		for( i = 0; i < prefill; i += 8 ) {
			final int s = source_read.packB_read( prefill - i < 8 ? prefill - i : 8 );
			final int d = dest_read.packB_read( prefill - i < 8 ? prefill - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d mismatch! byte %d, %x!=%x\n", prefill, i / 8, s, d );
				System.exit( 1 );
			}
		}
		if( prefill < dest_bytes ) {
			if( dest_read.packB_read( dest_bytes - prefill ) != 0 ) {
				System.err.printf("prefill=%d mismatch! trailing bits not zero\n", prefill );
				System.exit( 1 );
			}
		}

		// second copy
		dest_write.packB_writecopy( source, copy );

		// check buffers; verify end byte masking
		dest = dest_write.packB_get_buffer();
		dest_bytes = dest_write.packB_bytes();
		if( dest_bytes != (copy + prefill + 7) / 8 ) {
			System.err.printf("wrong number of bytes after prefill+copy! %ld!=%d\n", dest_bytes, (copy + prefill + 7) / 8 );
			System.exit( 1 );
		}
		source_read.packB_readinit( source, source_bytes );
		dest_read.packB_readinit( dest, dest_bytes );

		for( i = 0; i < prefill; i += 8 ) {
			final int s = source_read.packB_read( prefill - i < 8 ? prefill - i : 8 );
			final int d = dest_read.packB_read( prefill - i < 8 ? prefill - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d mismatch! byte %d, %x!=%x\n", prefill, i / 8, s, d );
				System.exit( 1 );
			}
		}

		source_read.packB_readinit( source, source_bytes );
		for( i = 0; i < copy; i += 8 ) {
			final int s = source_read.packB_read( copy - i < 8 ? copy - i : 8 );
			final int d = dest_read.packB_read( copy - i < 8 ? copy - i : 8 );
			if( s != d ) {
				System.err.printf("prefill=%d copy=%d mismatch! byte %d, %x!=%x\n", prefill, copy, i / 8, s, d );
				System.exit( 1 );
			}
		}

		if( copy + prefill < dest_bytes ) {
			if( dest_read.packB_read( dest_bytes - copy - prefill ) != 0 ) {
				System.err.printf("prefill=%d copy=%d mismatch! trailing bits not zero\n", prefill, copy );
				System.exit( 1 );
			}
		}

		source_write.packB_writeclear();
		dest_write.packB_writeclear();
	}

	@SuppressWarnings("boxing")
	public static void main(final String args[]) {
		byte[] buffer;
		int bytes, i, j;

		final int[] testbuffer1 = {
			18,12,103948,4325,543,76,432,52,3,65,4,56,32,42,34,21,1,23,32,546,456,7,
			567,56,8,8,55,3,52,342,341,4,265,7,67,86,2199,21,7,1,5,1,4 };
		final int test1size = 43;

		final int[] testbuffer2 = {
			216531625,1237861823,56732452,131,3212421,12325343,34547562,12313212,
			1233432,534,5,346435231,14436467,7869299,76326614,167548585,
			85525151,0,12321,1,349528352 };
		final int test2size = 21;

		final int[] testbuffer3 = {
			1,0,14,0,1,0,12,0,1,0,0,0,1,1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1,1,1,1,1,0,0,1,
			0,1,30,1,1,1,0,0,1,0,0,0,12,0,11,0,1,0,0,1 };
		final int test3size = 56;

		final int[] large = {
			2136531625,2137861823,56732452,131,3212421,12325343,34547562,12313212,
			1233432,534,5,2146435231,14436467,7869299,76326614,167548585,
			85525151,0,12321,1,2146528352 };

		final int onesize = 33;
		final int[] one = { 146,25,44,151,195,15,153,176,233,131,196,65,85,172,47,40,
			34,242,223,136,35,222,211,86,171,50,225,135,214,75,172,
			223,4 };
		final int[] oneB = { 150,101,131,33,203,15,204,216,105,193,156,65,84,85,222,
			8,139,145,227,126,34,55,244,171,85,100,39,195,173,18,
			245,251,128 };

		final int twosize = 6;
		final int[] two = { 61,255,255,251,231,29 };
		final int[] twoB = { 247,63,255,253,249,120 };

		final int threesize = 54;
		final int[] three = { 169,2,232,252,91,132,156,36,89,13,123,176,144,32,254,
			142,224,85,59,121,144,79,124,23,67,90,90,216,79,23,83,
			58,135,196,61,55,129,183,54,101,100,170,37,127,126,10,
			100,52,4,14,18,86,77,1 };
		final int[] threeB = { 206,128,42,153,57,8,183,251,13,89,36,30,32,144,183,
			130,59,240,121,59,85,223,19,228,180,134,33,107,74,98,
			233,253,196,135,63,2,110,114,50,155,90,127,37,170,104,
			200,20,254,4,58,106,176,144,0 };

		final int foursize = 38;
		final int[] four = { 18,6,163,252,97,194,104,131,32,1,7,82,137,42,129,11,72,
			132,60,220,112,8,196,109,64,179,86,9,137,195,208,122,169,
			28,2,133,0,1 };
		final int[] fourB = { 36,48,102,83,243,24,52,7,4,35,132,10,145,21,2,93,2,41,
			1,219,184,16,33,184,54,149,170,132,18,30,29,98,229,67,
			129,10,4,32 };

		final int fivesize = 45;
		final int[] five = { 169,2,126,139,144,172,30,4,80,72,240,59,130,218,73,62,
			241,24,210,44,4,20,0,248,116,49,135,100,110,130,181,169,
			84,75,159,2,1,0,132,192,8,0,0,18,22 };
		final int[] fiveB = { 1,84,145,111,245,100,128,8,56,36,40,71,126,78,213,226,
			124,105,12,0,133,128,0,162,233,242,67,152,77,205,77,
			172,150,169,129,79,128,0,6,4,32,0,27,9,0 };

		final int sixsize = 7;
		final int[] six = { 17,177,170,242,169,19,148 };
		final int[] sixB = { 136,141,85,79,149,200,41 };

		// Test read/write together
		// Later we test against pregenerated bitstreams
		o.pack_writeinit();

		System.err.print("\nSmall preclipped packing (LSb): ");
		cliptest( testbuffer1, test1size, 0, one, onesize );
		System.err.print("ok.");

		System.err.print("\nNull bit call (LSb): ");
		cliptest( testbuffer3, test3size, 0, two, twosize );
		System.err.print("ok.");

		System.err.print("\nLarge preclipped packing (LSb): ");
		cliptest( testbuffer2, test2size, 0, three, threesize );
		System.err.print("ok.");

		System.err.print("\n32 bit preclipped packing (LSb): ");
		o.pack_reset();
		for( i = 0; i < test2size; i++ ) {
			o.pack_write( large[i], 32 );
		}
		buffer = o.pack_get_buffer();
		bytes = o.pack_bytes();
		r.pack_readinit( buffer, bytes );
		for( i = 0; i < test2size; i++ ) {
			if( r.pack_look( 32 ) == -1 ) {
				report("out of data. failed!");
			}
			if( r.pack_look( 32 ) != large[i] ) {;
				System.err.printf("%d != %d (%x!=%x):", r.pack_look( 32 ), large[i],
						r.pack_look( 32 ), large[i] );
				report("read incorrect value!\n");
			}
			r.pack_adv( 32 );
		}
		if( r.pack_bytes() != bytes ) {
			report("leftover bytes after read!\n");
		}
		System.err.print("ok.");

		System.err.print("\nSmall unclipped packing (LSb): ");
		cliptest(testbuffer1,test1size,7,four,foursize);
		System.err.print("ok.");

		System.err.print("\nLarge unclipped packing (LSb): ");
		cliptest(testbuffer2,test2size,17,five,fivesize);
		System.err.print("ok.");

		System.err.print("\nSingle bit unclipped packing (LSb): ");
		cliptest(testbuffer3,test3size,1,six,sixsize);
		System.err.print("ok.");

		System.err.print("\nTesting read past end (LSb): ");
		r.pack_readinit( new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 8 );
		for( i = 0; i < 64; i++ ) {
			if( r.pack_read( 1 ) != 0 ) {
				System.err.print("failed; got -1 prematurely.\n");
				System.exit( 1 );
			}
		}
		if( r.pack_look( 1 ) != -1 ||
				r.pack_read( 1) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		r.pack_readinit( new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 8 );
		if( r.pack_read( 30 ) != 0 || r.pack_read( 16 ) != 0 ) {
			System.err.print("failed 2; got -1 prematurely.\n");
			System.exit( 1 );
		}

		if( r.pack_look( 18 ) != 0 ||
				r.pack_look( 18 ) != 0) {
			System.err.print("failed 3; got -1 prematurely.\n");
			System.exit( 1 );
		}
		if( r.pack_look( 19 ) != -1 ||
				r.pack_look( 19 ) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		if( r.pack_look( 32 ) != -1 ||
				r.pack_look( 32 ) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		o.pack_writeclear();
		System.err.print("ok.");


		// this is partly glassbox; we're mostly concerned about the allocation boundaries

		System.err.print("\nTesting aligned writecopies (LSb): ");
		for( i = 0; i < 71; i++ ) {
			for( j = 0; j < 5; j++ ) {
				copytest( j * 8, i );
			}
		}
		for( i = BUFFER_INCREMENT * 8 - 71; i <BUFFER_INCREMENT * 8 + 71; i++ ) {
			for( j = 0; j < 5; j++ ) {
				copytest( j * 8, i );
			}
		}
		System.err.print("ok.      ");

		System.err.print("\nTesting unaligned writecopies (LSb): ");
		for( i = 0; i < 71; i++ ) {
			for( j = 1; j < 40; j++ ) {
				if( (j & 0x7) != 0 ) {
					copytest( j, i );
				}
			}
		}
		for( i = BUFFER_INCREMENT * 8 - 71; i < BUFFER_INCREMENT * 8 + 71; i++ ) {
			for( j = 1; j < 40; j++ ) {
				if( (j & 0x7) != 0) {
					copytest( j, i );
				}
			}
		}

		System.err.print("ok.      \n");

		//********** lazy, cut-n-paste retest with MSb packing ***********

		// Test read/write together
		// Later we test against pregenerated bitstreams
		o.packB_writeinit();

		System.err.print("\nSmall preclipped packing (MSb): ");
		cliptestB( testbuffer1, test1size, 0, oneB, onesize );
		System.err.print("ok.");

		System.err.print("\nNull bit call (MSb): ");
		cliptestB( testbuffer3, test3size, 0, twoB, twosize );
		System.err.print("ok.");

		System.err.print("\nLarge preclipped packing (MSb): ");
		cliptestB( testbuffer2, test2size, 0, threeB, threesize );
		System.err.print("ok.");

		System.err.print("\n32 bit preclipped packing (MSb): ");
		o.packB_reset();
		for( i = 0; i < test2size; i++ ) {
			o.packB_write( large[i], 32 );
		}
		buffer = o.packB_get_buffer();
		bytes = o.packB_bytes();
		r.packB_readinit( buffer, bytes );
		for( i = 0; i < test2size; i++ ) {
			if( r.packB_look( 32 ) == -1 ) {
				report("out of data. failed!");
			}
			if( r.packB_look( 32 ) != large[i] ) {
				System.err.printf("%d != %d (%x!=%x):", r.packB_look( 32 ), large[i],
						r.packB_look( 32 ), large[i] );
				report("read incorrect value!\n");
			}
			r.packB_adv( 32 );
		}
		if( r.packB_bytes() != bytes) {
			report("leftover bytes after read!\n");
		}
		System.err.print("ok.");

		System.err.print("\nSmall unclipped packing (MSb): ");
		cliptestB( testbuffer1, test1size, 7, fourB, foursize );
		System.err.print("ok.");

		System.err.print("\nLarge unclipped packing (MSb): ");
		cliptestB( testbuffer2, test2size, 17, fiveB, fivesize );
		System.err.print("ok.");

		System.err.print("\nSingle bit unclipped packing (MSb): ");
		cliptestB( testbuffer3, test3size, 1, sixB, sixsize );
		System.err.print("ok.");

		System.err.print("\nTesting read past end (MSb): ");
		r.packB_readinit( new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 8);
		for( i = 0; i < 64; i++ ) {
			if( r.packB_read( 1 ) != 0 ) {
				System.err.print("failed; got -1 prematurely.\n");
				System.exit( 1 );
			}
		}
		if( r.packB_look( 1 ) != -1 ||
				r.packB_read( 1 ) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		r.packB_readinit( new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 8 );
		if( r.packB_read( 30 ) != 0 || r.packB_read( 16 ) != 0 ){
			System.err.print("failed 2; got -1 prematurely.\n");
			System.exit( 1 );
		}

		if( r.packB_look( 18 ) != 0 ||
				r.packB_look( 18 ) != 0 ) {
			System.err.print("failed 3; got -1 prematurely.\n");
			System.exit( 1 );
		}
		if( r.packB_look( 19 ) != -1 ||
				r.packB_look( 19 ) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		if( r.packB_look( 32 ) != -1 ||
				r.packB_look( 32 ) != -1 ) {
			System.err.print("failed; read past end without -1.\n");
			System.exit( 1 );
		}
		System.err.print("ok.");
		o.packB_writeclear();

		// this is partly glassbox; we're mostly concerned about the allocation boundaries

		System.err.print("\nTesting aligned writecopies (MSb): ");
		for( i = 0; i < 71; i++ ) {
			for( j = 0; j < 5; j++ ) {
				copytestB( j * 8, i );
			}
		}
		for( i = BUFFER_INCREMENT * 8 - 71; i < BUFFER_INCREMENT * 8 + 71; i++ ) {
			for( j = 0; j < 5; j++ ) {
				copytestB( j * 8, i );
			}
		}
		System.err.print("ok.      ");

		System.err.print("\nTesting unaligned writecopies (MSb): ");
		for( i = 0; i < 71; i++ ) {
			for( j = 1; j < 40; j++ ) {
				if( (j & 0x7) != 0 ) {
					copytestB( j, i );
				}
			}
		}
		for( i = BUFFER_INCREMENT * 8 - 71; i < BUFFER_INCREMENT * 8 + 71; i++ ) {
			for( j = 1; j < 40; j++ ) {
				if( (j & 0x7) != 0 ) {
					copytestB( j, i );
				}
			}
		}

		System.err.print("ok.      \n\n");

		System.exit( 0 );
	}
	/*#endif   _V_SELFTEST */
}

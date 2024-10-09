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

/** ogg_stream_state contains the current encode/decode state of a logical Ogg bitstream */
public class OggStreamState {
	/** bytes from packet bodies */
	byte[] body_data;
	/** storage elements allocated */
	int body_storage;
	/** elements stored; fill mark */
	int body_fill;
	/** elements of fill returned */
	int body_returned;
	/** The values that will go to the segment table */
	int[] lacing_vals;
	/** granulepos values for headers. Not compact
	 * this way, but it is simple coupled to the
	 * lacing fifo */
	long[] granule_vals;
	int lacing_storage;
	int lacing_fill;
	int lacing_packet;
	int lacing_returned;
	/** working space for header encode */
	final byte[] header = new byte[282];
	int header_fill;
	/** set when we have buffered the last packet in the
	 * logical bitstream */
	boolean e_o_s;
	/** set after we've written the initial page
	 * of a logical bitstream */
	boolean b_o_s;
	public int serialno;
	int pageno;
	/** sequence number for decode; the framing
	 * knows where there's a hole in the data,
	 * but we need coupling so that the codec
	 * (which is in a separate abstraction
	 * layer) also knows about the gap */
	long packetno;
	long granulepos;
	//
	private final void m_clear() {
		body_data = null;
		body_storage = 0;
		body_fill = 0;
		body_returned = 0;
		lacing_vals = null;
		granule_vals = null;
		lacing_storage = 0;
		lacing_fill = 0;
		lacing_packet = 0;
		lacing_returned = 0;
		header_fill = 0;
		e_o_s = false;
		b_o_s = false;
		serialno = 0;
		pageno = 0;
		packetno = 0;
		granulepos = 0;
	}
	// framing.c
	/** _clear does not free os, only the non-flat storage within */
	public final int clear() {
		//if( os != null ) {
			m_clear();
		//}
		return 0;
	}

	/** init the encode/decode logical stream state */
	public final int init(final int serialNo) {
		//if( os != null ) {
			m_clear();

			this.body_storage = 16 * 1024;
			this.lacing_storage = 1024;
			//try {
				this.body_data = new byte[this.body_storage];
				this.lacing_vals = new int[this.lacing_storage];
				this.granule_vals = new long[this.lacing_storage];
			//} catch( final OutOfMemoryError e ) {
			//	ogg_stream_clear();
			//	return -1;
			//}

			this.serialno = serialNo;

			return 0;
		//}
		//return -1;
	}

	/** async/delayed error detection for the ogg_stream_state */
	public final int check() {
		if( /*os == null || */this.body_data == null ) {
			return -1;
		}
		return 0;
	}

	/** don't use. make <code>os = null</code> */
	/*public static int destroy(OggStreamState os) {
		if( os != null ) {
			clear( os );
			os = null;
		}
		return 0;
	}*/

	/** Helpers for ogg_stream_encode; this keeps the structure and
	what's happening fairly clear */
	private final int _body_expand(final int needed) {
		if( this.body_storage - needed <= this.body_fill ) {
			if( this.body_storage > Integer.MAX_VALUE - needed ) {
				clear();
				return -1;
			}
			int storage = this.body_storage + needed;
			if( storage < Integer.MAX_VALUE - 1024 ) {
				storage += 1024;
			}
			//try {
				this.body_data = Arrays.copyOf( this.body_data, storage );
			//} catch( final OutOfMemoryError e ) {
			//	clear();
			//	return -1;
			//}
			this.body_storage = storage;
		}
		return 0;
	}

	private final int _lacing_expand(final int needed) {
		if( this.lacing_storage - needed <= this.lacing_fill ) {
			if( this.lacing_storage > Integer.MAX_VALUE - needed ) {
				clear();
				return -1;
			}
			int storage = this.lacing_storage + needed;
			if( storage < Integer.MAX_VALUE - 32 ) {
				storage += 32;
			}
			//try {
			this.lacing_vals = Arrays.copyOf( this.lacing_vals, storage );
			//} catch( final OutOfMemoryError e ) {
			//	clear();
			//	return -1;
			//}
			//try {
			this.granule_vals = Arrays.copyOf( this.granule_vals, storage );
			//} catch( final OutOfMemoryError e ) {
			//	clear();
			//	return -1;
			//}
			this.lacing_storage = storage;
		}
		return 0;
	}

	/** submit data to the internal buffer of the framing engine */
	private final int iovecin(final IOV_EC[] iov, final int count,
							  final boolean eos, final long granulePos) {

		int bytes = 0, i;

		if( check() != 0 ) {
			return -1;
		}
		if( iov == null ) {
			return 0;
		}

		for( i = 0; i < count; ++i ) {
			final int len = iov[i].iov_len;
			if( len > Integer.MAX_VALUE ) {
				return -1;
			}
			if( bytes > Integer.MAX_VALUE - len ) {
				return -1;
			}
			bytes += len;
		}
		final int lacing_vals_count = bytes / 255 + 1;

		if( this.body_returned != 0 ) {
			/* advance packet data according to the body_returned pointer. We
			had to keep it around to return a pointer into the buffer last
			call */

			this.body_fill -= this.body_returned;
			if( this.body_fill != 0 ) {
				System.arraycopy( this.body_data, this.body_returned, this.body_data, 0, this.body_fill );
			}
			this.body_returned = 0;
		}

		/* make sure we have the buffer storage */
		if( _body_expand( bytes ) != 0 || _lacing_expand( lacing_vals_count ) != 0 ) {
			return -1;
		}

		/* Copy in the submitted packet. Yes, the copy is a waste; this is
		the liability of overly clean abstraction for the time being. It
		will actually be fairly easy to eliminate the extra copy in the
		future */

		for( i = 0; i < count; ++i ) {
			System.arraycopy( iov[i].iov_packet, iov[i].iov_base,
					this.body_data, this.body_fill, iov[i].iov_len );
			this.body_fill += (int)iov[i].iov_len;
		}

		/* Store lacing vals for this packet */
		final int[] lvals = this.lacing_vals;// java
		final long[] gvals = this.granule_vals;// java
		i = this.lacing_fill;
		for( final int n = i + lacing_vals_count - 1; i < n; i++ ) {
			lvals[i] = 255;
			gvals[i] = this.granulepos;
		}
		// i += this.lacing_fill;
		lvals[i] = bytes % 255;
		this.granulepos = gvals[i] = granulePos;

		/* flag the first segment as the beginning of the packet */
		lvals[this.lacing_fill] |= 0x100;

		this.lacing_fill += lacing_vals_count;

		/* for the sake of completeness */
		this.packetno++;

		if( eos ) {
			this.e_o_s = true;
		}

		return 0;
	}

	public final int packetin(final OggPacket op) {
		final IOV_EC v = new IOV_EC();
		v.iov_packet = op.packet_base;
		v.iov_base = op.packet;
		v.iov_len = op.bytes;
		final IOV_EC[] iov = { v };
		return iovecin( iov, 1, op.e_o_s, op.granulepos );
	}

	/** Conditionally flush a page; force==0 will only flush nominal-size
	pages, force==1 forces us to flush a page regardless of page size
	so long as there's any data available at all. */
	private final int flush_i(final OggPage og, boolean force, final int nfill) {
		if( check() != 0 ) {
			return 0;
		}
		final int maxvals = (this.lacing_fill > 255 ? 255 : this.lacing_fill);
		if( maxvals == 0 ) {
			return 0;
		}

		/* construct a page */
		/* decide how many segments to include */

		/* If this is the initial header case, the first page must only include
		the initial header packet */
		int vals;
		long granule_pos = -1;
		final int[] lvals = this.lacing_vals;// java
		if( ! this.b_o_s ) {/* 'initial header page' case */
			granule_pos = 0;
			for( vals = 0; vals < maxvals; vals++ ) {
				if( (lvals[vals] & 0xff) < 255) {
					vals++;
					break;
				}
			}
		} else {
			/* The extra packets_done, packet_just_done logic here attempts to do two things:
			1) Don't unnecessarily span pages.
			2) Unless necessary, don't flush pages if there are less than four packets on
			them; this expands page size to reduce unnecessary overhead if incoming packets
			are large.
			These are not necessary behaviors, just 'always better than naive flushing'
			without requiring an application to explicitly request a specific optimized
			behavior. We'll want an explicit behavior setup pathway eventually as well. */

			int packets_done = 0;
			int packet_just_done = 0;
			int acc = 0;
			for( vals = 0; vals < maxvals; vals++ ) {
				if( acc > nfill && packet_just_done >= 4 ) {
					force = true;
					break;
				}
				acc += lvals[vals] & 0xff;
				if( (lvals[vals] & 0xff) < 255 ) {
					granule_pos = this.granule_vals[vals];
					packet_just_done = ++packets_done;
				} else {
					packet_just_done = 0;
				}
			}
			if( vals == 255 ) {
				force = true;
			}
		}

		if( ! force ) {
			return 0;
		}

		/* construct the header in temp storage */
		final byte[] hdr = this.header;// java
		hdr[0] = 'O'; hdr[1] = 'g'; hdr[2] = 'g'; hdr[3] = 'S';

		/* stream structure version */
		hdr[4] = 0x00;

		/* continued packet flag? */
		hdr[5] = 0x00;
		if( (lvals[0] & 0x100) == 0 ) {
			hdr[5] |= 0x01;
		}
		/* first page flag? */
		if( ! this.b_o_s ) {
			hdr[5] |= 0x02;
		}
		/* last page flag? */
		if( this.e_o_s && this.lacing_fill == vals ) {
			hdr[5] |= 0x04;
		}
		this.b_o_s = true;

		/* 64 bits of PCM position */
		for( int i = 6; i < 14; i++ ) {
			hdr[i] = (byte)granule_pos;
			granule_pos >>= 8;
		}

		/* 32 bits of stream serial number */
		{
			int serial_no = this.serialno;
			for( int i = 14; i < 18; i++ ) {
				hdr[i] = (byte)serial_no;
				serial_no >>= 8;
			}
		}

		/* 32 bits of page counter (we have both counter and page header
		because this val can roll over) */
		if( this.pageno == -1 ) {
			this.pageno = 0; /* because someone called
											stream_reset; this would be a
											strange thing to do in an
											encode stream, but it has
											plausible uses */
		}
		{
			int page_no = this.pageno++;
			for( int i = 18; i < 22; i++ ) {
				hdr[i] = (byte)page_no;
				page_no >>= 8;
			}
		}

		/* zero for computation; filled in later */
		hdr[22] = 0;
		hdr[23] = 0;
		hdr[24] = 0;
		hdr[25] = 0;

		/* segment table */
		int bytes = 0;
		hdr[26] = (byte)vals;
		for( int i = 0, j = 27; i < vals; i++, j++ ) {
			final int val = (lvals[i] & 0xff);
			bytes += val;
			hdr[j] = (byte)val;
		}

		/* set pointers in the ogg_page struct */
		og.header_base = hdr;
		og.header = 0;
		og.header_len = this.header_fill = vals + 27;
		og.body_base = this.body_data;
		og.body = this.body_returned;
		og.body_len = bytes;

		/* advance the lacing data and set the body_returned pointer */

		this.lacing_fill -= vals;
		System.arraycopy( lvals, vals, lvals, 0, this.lacing_fill );
		System.arraycopy( this.granule_vals, vals, this.granule_vals, 0, this.lacing_fill );
		this.body_returned += bytes;

		/* calculate the checksum */

		og.checksum_set();

		/* done */
		return 1;
	}

	/** This will flush remaining packets into a page (returning nonzero),
	even if there is not enough data to trigger a flush normally
	(undersized page). If there are no packets or partial packets to
	flush, ogg_stream_flush returns 0. Note that ogg_stream_flush will
	try to flush a normal sized page like ogg_stream_pageout; a call to
	ogg_stream_flush does not guarantee that all packets have flushed.
	Only a return value of 0 from ogg_stream_flush indicates all packet
	data is flushed into pages.

	since ogg_stream_flush will flush the last page in a stream even if
	it's undersized, you almost certainly want to use ogg_stream_pageout
	(and *not* ogg_stream_flush) unless you specifically need to flush
	a page regardless of size in the middle of a stream. */
	public final int flush(final OggPage og) {
		return flush_i( og, true, 4096 );
	}

	/** Like the above, but an argument is provided to adjust the nominal
	page size for applications which are smart enough to provide their
	own delay based flushing */
	public final int flush_fill(final OggPage og, final int nfill) {
		return flush_i( og, true, nfill );
	}

	/** This constructs pages from buffered packet segments. The pointers
	returned are to static buffers; do not free. The returned buffers are
	good only until the next call (using the same ogg_stream_state) */
	public final int pageout(final OggPage og) {
		boolean force = false;
		if( check() != 0 ) {
			return 0;
		}

		if( ( this.e_o_s && this.lacing_fill != 0 ) ||/* 'were done, now flush' case */
			 (this.lacing_fill != 0 && ! this.b_o_s ) ) {
			force = true;
		}

		return flush_i( og, force, 4096 );
	}

	/** add the incoming page to the stream state; we decompose the page
	  into packet segments here as well. */
	public final int pagein(final OggPage og) {
		if( check() != 0 ) {
			return -1;
		}

		/* clean up 'returned data' */
		final int[] lvals = this.lacing_vals;// java
		final long[] gvals = this.granule_vals;// java
		{
			final int lr = this.lacing_returned;
			final int br = this.body_returned;

			/* body data */
			if( br != 0 ) {
				this.body_fill -= br;
				if( this.body_fill != 0 ) {
					System.arraycopy( this.body_data, br, this.body_data, 0, this.body_fill );
				}
				this.body_returned = 0;
			}

			if( lr != 0 ) {
				/* segment table */
				this.lacing_fill -= lr;
				if( this.lacing_fill != 0 ) {
					System.arraycopy( lvals, lr, lvals, 0, this.lacing_fill );
					System.arraycopy( gvals, lr, gvals, 0, this.lacing_fill );
				}
				this.lacing_packet -= lr;
				this.lacing_returned = 0;
			}
		}

		/* check the serial number */
		if( og.serialno() != this.serialno ) {
			return (-1);
		}
		if( og.version() > 0 ) {
			return (-1);
		}

		final byte[] header_base = og.header_base;
		final int header27 = og.header + 27;
		int segments = ((int)header_base[header27 - 1]) & 0xff;// [26]
		if( _lacing_expand( segments + 1 ) != 0 ) {
			return -1;
		}

		/* are we in sequence? */
		final int page_no = og.pageno();
		if( page_no != this.pageno ) {
			/* unroll previous partial packet (if any) */
			for( int i = this.lacing_packet; i < this.lacing_fill; i++ ) {
				this.body_fill -= lvals[i] & 0xff;
			}
			this.lacing_fill = this.lacing_packet;

			/* make a note of dropped data in segment table */
			if( this.pageno != -1 ) {
				lvals[this.lacing_fill++] = 0x400;
				this.lacing_packet++;
			}
		}

		segments += header27;
		/* are we a 'continued packet' page? If so, we may need to skip
		some segments */
		int segptr = header27;
		int body = og.body;
		int bodysize = og.body_len;
		boolean bos = og.bos();
		if( og.continued() ) {
			if( this.lacing_fill < 1 ||
				(lvals[this.lacing_fill - 1] & 0xff) < 255 ||
				lvals[this.lacing_fill - 1] == 0x400 ) {
				bos = false;
				for( ; segptr < segments; segptr++ ) {
					final int val = ((int)header_base[segptr]) & 0xff;
					body += val;
					bodysize -= val;
					if( val < 255 ) {
						segptr++;
						break;
					}
				}
			}
		}

		if( bodysize != 0 ) {
			if( _body_expand( bodysize ) != 0 ) {
				return -1;
			}
			System.arraycopy( og.body_base, body, this.body_data, this.body_fill, bodysize );
			this.body_fill += bodysize;
		}

		{
			int saved = -1;
			while( segptr < segments ) {
				final int val = ((int)header_base[segptr]) & 0xff;
				lvals[this.lacing_fill] = val;
				gvals[this.lacing_fill] = -1;

				if( bos ) {
					lvals[this.lacing_fill] |= 0x100;
					bos = false;
				}

				if( val < 255 ) {
					saved = this.lacing_fill;
				}

				this.lacing_fill++;
				segptr++;

				if( val < 255 ) {
					this.lacing_packet = this.lacing_fill;
				}
			}

			/* set the granulepos on the last granuleval of the last full packet */
			if( saved != -1 ) {
				gvals[saved] = og.granulepos();
			}

		}

		if( og.eos() ) {
			this.e_o_s = true;
			if( this.lacing_fill > 0 ) {
				lvals[this.lacing_fill - 1] |= 0x200;
			}
		}

		this.pageno = page_no + 1;

		return (0);
	}

	/** Like the above, but an argument is provided to adjust the nominal
	page size for applications which are smart enough to provide their
	own delay based flushing */
	public final int pageout_fill(final OggPage og, final int nfill) {
		boolean force = false;
		if( check() != 0 ) {
			return 0;
		}

		if( ( this.e_o_s && this.lacing_fill != 0 ) ||/* 'were done, now flush' case */
			( this.lacing_fill != 0 && ! this.b_o_s )) {
			force = true;
		}

		return flush_i( og, force, nfill );
	}

	public final boolean eos() {
		if( check() != 0 ) {
			return true;
		}
		return this.e_o_s;
	}

	public final int reset() {
		if( check() != 0 ) {
			return -1;
		}

		body_fill = 0;
		body_returned = 0;

		lacing_fill = 0;
		lacing_packet = 0;
		lacing_returned = 0;

		header_fill = 0;

		e_o_s = false;
		b_o_s = false;
		pageno = -1;
		packetno = 0;
		granulepos = 0;

		return 0;
	}

	public final int reset_serialno(final int i_serialno) {
		if( check() != 0 ) {
			return -1;
		}
		reset();
		this.serialno = i_serialno;
		return (0);
	}

	private final int _packetout(final OggPacket op, final int adv) {

		/* The last part of decode. We have the stream broken into packet
		segments. Now we need to group them into packets (or return the
		out of sync markers) */

		int ptr = this.lacing_returned;

		if( this.lacing_packet <= ptr ) {
			return (0);
		}

		final int[] lvals = this.lacing_vals;// java
		if( (lvals[ptr] & 0x400) != 0 ) {
			/* we need to tell the codec there's a gap; it might need to
			handle previous packet dependencies. */
			this.lacing_returned++;
			this.packetno++;
			return (-1);
		}

		if( op == null && adv == 0 ) {
			return (1); /* just using peek as an inexpensive way
									to ask if there's a whole packet
									waiting */
		}

		/* Gather the whole packet. We'll have no holes or a partial packet */
		{
			int size = lvals[ptr] & 0xff;
			int bytes = size;
			boolean eos = (lvals[ptr] & 0x200) != 0; /* last packet of the stream? */
			final boolean bos = (lvals[ptr] & 0x100) != 0; /* first packet of the stream? */

			while( size == 255 ) {
				final int val = lvals[++ptr];
				size = val & 0xff;
				if( (val & 0x200) != 0 )
				 {
					eos = true;// eos = 0x200;
				}
				bytes += size;
			}

			if( op != null ) {
				op.e_o_s = eos;
				op.b_o_s = bos;
				op.packet_base = this.body_data;
				op.packet = this.body_returned;
				op.packetno = this.packetno;
				op.granulepos = this.granule_vals[ptr];
				op.bytes = bytes;
			}

			if( adv != 0 ) {
				this.body_returned += bytes;
				this.lacing_returned = ptr + 1;
				this.packetno++;
			}
		}
		return 1;
	}

	public final int packetout(final OggPacket op) {
		if( check() != 0 ) {
			return 0;
		}
		return _packetout( op, 1 );
	}

	public final int packetpeek(final OggPacket op) {
		if( check() != 0 ) {
			return 0;
		}
		return _packetout( op, 0 );
	}

/* XXX #ifdef _V_SELFTEST.

		private static OggStreamState os_en = new OggStreamState();
		private static OggStreamState os_de = new OggStreamState();
		private static OggSyncState oy = new OggSyncState();

		private static int sequence = 0;
		private static int lastno = 0;

		@SuppressWarnings("boxing")
		private static void checkpacket(final OggPacket op, final long len, final int no, final long pos) {
			int j;

			if( op.bytes != len ) {
				System.err.printf("incorrect packet length (%d != %d)!\n", op.bytes, len );
				System.exit( 1 );
			}
			if( op.granulepos != pos ) {
				System.err.printf("incorrect packet granpos (%ld != %ld)!\n", op.granulepos, pos );
				System.exit( 1 );
			}

			// packet number just follows sequence/gap; adjust the input number for that
			if( no == 0 ) {
				sequence = 0;
			} else {
				sequence++;
				if( no > lastno + 1 ) {
					sequence++;
				}
			}
			lastno = no;
			if( op.packetno != sequence ) {
				System.err.printf("incorrect packet sequence %d != %d\n", op.packetno, sequence );
				System.exit( 1 );
			}

			// Test data
			for( j = 0; j < op.bytes; j++ ) {
				if( (((int)op.packet_base[op.packet + j]) & 0xff) != ((j + no) & 0xff) ) {
					System.err.printf("body data mismatch (1) at pos %d: %x!=%x!\n\n",
							j, op.packet_base[op.packet + j], (j + no) & 0xff);
					System.err.println();
					System.exit( 1 );
				}
			}
		}

		@SuppressWarnings("boxing")
		private static void check_page(final byte[] data, final int offset, final int[] header, final OggPage og) {
			int j;
			// Test data
			for( j = 0; j < og.body_len; j++ ) {
				if( og.body_base[og.body + j] != data[j + offset] ) {
					System.err.printf("body data mismatch (2) at pos %d: %x!=%x!\n\n",
							j, data[j + offset], og.body_base[og.body + j] );
					System.exit( 1 );
				}
			}

			// Test header
			for( j = 0; j < og.header_len; j++ ) {
				if( (((int)og.header_base[og.header + j]) & 0xff) != header[j] ) {
					System.err.printf("header content mismatch at pos %d:\n", j);
					for( j = 0; j < header[26] + 27;j++) {
						System.err.printf(" (%d)%02x:%02x", j, header[j], og.header_base[og.header + j] );
					}
					System.err.print("\n");
					System.exit( 1 );
				}
			}
			if( og.header_len != header[26] + 27 ) {
				System.err.printf("header length incorrect! (%d!=%d)\n",
						og.header_len, (((int)header[26]) & 0xff) + 27 );
				System.exit( 1 );
			}
		}

		@SuppressWarnings("boxing")
		private static void print_header(final OggPage og) {
			int j;
			System.err.printf("\nHEADER:\n");
			System.err.printf("		capture: %c %c %c %c		version: %d		flags: %x\n",
					og.header_base[og.header + 0], og.header_base[og.header + 1],
					og.header_base[og.header + 2], og.header_base[og.header + 3],
					(int)og.header_base[og.header + 4], (int)og.header_base[og.header + 5]);

			System.err.printf("		granulepos: %d		serialno: %d		pageno: %d\n",
					(og.header_base[og.header + 9] << 24) |
					((((int)og.header_base[og.header + 8]) & 0xff) << 16) |
					((((int)og.header_base[og.header + 7]) & 0xff) << 8) |
					(((int)og.header_base[og.header + 6]) & 0xff),
					(og.header_base[og.header + 17] << 24) |
					((((int)og.header_base[og.header + 16]) & 0xff) << 16) |
					((((int)og.header_base[og.header + 15]) & 0xff) << 8) |
					(((int)og.header_base[og.header + 14]) & 0xff),
					((int)(og.header_base[og.header + 21]) << 24) |
					((((int)og.header_base[og.header + 20]) & 0xff) << 16) |
					((((int)og.header_base[og.header + 19]) & 0xff) << 8) |
					(((int)og.header_base[og.header + 18]) & 0xff));

			System.err.printf("		checksum: %02x:%02x:%02x:%02x\n		segments: %d (",
					(int)og.header_base[og.header + 22], (int)og.header_base[og.header + 23],
					(int)og.header_base[og.header + 24], (int)og.header_base[og.header + 25],
					(int)og.header_base[og.header + 26]);

			for( j = 27; j < og.header_len; j++ ) {
				System.err.printf("%d ", (int)og.header_base[og.header + j] );
			}
			System.err.printf(")\n\n");
		}

		private static void copy_page(final OggPage og) {
			byte[] temp = new byte[og.header_len];
			System.arraycopy( og.header_base, og.header, temp, 0, og.header_len );
			og.header_base = temp;
			og.header = 0;

			temp = new byte[og.body_len];
			System.arraycopy( og.body_base, og.body, temp, 0, og.body_len );
			og.body_base = temp;
			og.body = 0;
		}

		private static void free_page(final OggPage og) {
			og.header_base = null;
			og.body_base = null;
			//og.header = -1;
			//og.body = -1;
		}

		private static void error() {
			System.err.print("error!\n");
			System.exit( 1 );
		}

		// 17 only
		private static final int head1_0[] = { 0x4f,0x67,0x67,0x53,0,0x06,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0x15,0xed,0xec,0x91,
				1,
				17 };

		// 17, 254, 255, 256, 500, 510, 600 byte, pad
		private static final int head1_1[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0x59,0x10,0x6c,0x2c,
				1,
				17 };
		private static final int head2_1[] = { 0x4f,0x67,0x67,0x53,0,0x04,
				0x07,0x18,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0x89,0x33,0x85,0xce,
				13,
				254,255,0,255,1,255,245,255,255,0,
				255,255,90 };

		// nil packets; beginning,middle,end
		private static final int head1_2[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0xff,0x7b,0x23,0x17,
				1,
				0 };
		private static final int head2_2[] = { 0x4f,0x67,0x67,0x53,0,0x04,
				0x07,0x28,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0x5c,0x3f,0x66,0xcb,
				17,
				17,254,255,0,0,255,1,0,255,245,255,255,0,
				255,255,90,0 };

		// large initial packet
		private static final int head1_3[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0x01,0x27,0x31,0xaa,
				18,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,255,10 };

		private static final int head2_3[] = { 0x4f,0x67,0x67,0x53,0,0x04,
				0x07,0x08,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0x7f,0x4e,0x8a,0xd2,
				4,
				255,4,255,0 };


		// continuing packet test
		private static final int head1_4[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0xff,0x7b,0x23,0x17,
				1,
				0 };

		private static final int head2_4[] = { 0x4f,0x67,0x67,0x53,0,0x00,
				0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0xf8,0x3c,0x19,0x79,
				255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255 };

		private static final int head3_4[] = { 0x4f,0x67,0x67,0x53,0,0x05,
				0x07,0x0c,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,2,0,0,0,
				0x38,0xe6,0xb6,0x28,
				6,
				255,220,255,4,255,0 };


		// spill expansion test
		private static final int head1_4b[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				 0x01,0x02,0x03,0x04,0,0,0,0,
				 0xff,0x7b,0x23,0x17,
				 1,
				 0 };

		private static final int head2_4b[] = { 0x4f,0x67,0x67,0x53,0,0x00,
				 0x07,0x10,0x00,0x00,0x00,0x00,0x00,0x00,
				 0x01,0x02,0x03,0x04,1,0,0,0,
				 0xce,0x8f,0x17,0x1a,
				 23,
				 255,255,255,255,255,255,255,255,
				 255,255,255,255,255,255,255,255,255,10,255,4,255,0,0 };


		private static final int head3_4b[] = { 0x4f,0x67,0x67,0x53,0,0x04,
				 0x07,0x14,0x00,0x00,0x00,0x00,0x00,0x00,
				 0x01,0x02,0x03,0x04,2,0,0,0,
				 0x9b,0xb2,0x50,0xa1,
				 1,
				 0 };

		// page with the 255 segment limit
		private static final int head1_5[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0xff,0x7b,0x23,0x17,
				1,
				0 };

		private static final int head2_5[] = { 0x4f,0x67,0x67,0x53,0,0x00,
				0x07,0xfc,0x03,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0xed,0x2a,0x2e,0xa7,
				255,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10,10,
				10,10,10,10,10,10,10 };

		private static final int head3_5[] = { 0x4f,0x67,0x67,0x53,0,0x04,
				0x07,0x00,0x04,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,2,0,0,0,
				0x6c,0x3b,0x82,0x3d,
				1,
				50 };

		// packet that overspans over an entire page
		private static final int head1_6[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0xff,0x7b,0x23,0x17,
				1,
				0 };

		private static final int head2_6[] = { 0x4f,0x67,0x67,0x53,0,0x00,
				0x07,0x04,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0x68,0x22,0x7c,0x3d,
				255,
				100,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255 };

		private static final int head3_6[] = { 0x4f,0x67,0x67,0x53,0,0x01,
				0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
				0x01,0x02,0x03,0x04,2,0,0,0,
				0xf4,0x87,0xba,0xf3,
				255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255 };

		private static final int head4_6[] = { 0x4f,0x67,0x67,0x53,0,0x05,
				0x07,0x10,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,3,0,0,0,
				0xf7,0x2f,0x6c,0x60,
				5,
				254,255,4,255,0 };

		// packet that overspans over an entire page
		private static final int head1_7[] = { 0x4f,0x67,0x67,0x53,0,0x02,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,0,0,0,0,
				0xff,0x7b,0x23,0x17,
				1,
				0 };

		private static final int head2_7[] = { 0x4f,0x67,0x67,0x53,0,0x00,
				0x07,0x04,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,1,0,0,0,
				0x68,0x22,0x7c,0x3d,
				255,
				100,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255,255,255,
				255,255,255,255,255,255 };

		private static final int head3_7[] = { 0x4f,0x67,0x67,0x53,0,0x05,
				0x07,0x08,0x00,0x00,0x00,0x00,0x00,0x00,
				0x01,0x02,0x03,0x04,2,0,0,0,
				0xd4,0xe0,0x60,0xe5,
				1,
				0 };

		private static boolean compare_packet(final OggPacket op1, final OggPacket op2) {
			if( op1.packet != op2.packet ) {
				System.err.printf("op1->packet != op2->packet\n");
				return true;
			}
			if( op1.bytes != op2.bytes ) {
				System.err.printf("op1->bytes != op2->bytes\n");
				return true;
			}
			if( op1.b_o_s != op2.b_o_s ) {
				System.err.printf("op1->b_o_s != op2->b_o_s\n");
				return true;
			}
			if( op1.e_o_s != op2.e_o_s ) {
				System.err.printf("op1->e_o_s != op2->e_o_s\n");
				return true;
			}
			if( op1.granulepos != op2.granulepos ) {
				System.err.printf("op1->granulepos != op2->granulepos\n");
				return true;
			}
			if( op1.packetno != op2.packetno ) {
				System.err.printf("op1->packetno != op2->packetno\n");
				return true;
			}
			return false;
		}

		private static int memcmp(final byte[] dim1, final int offset1, final byte[] dim2, final int offset2, final int count) {
			if( dim1.length - offset1 < count ) {
				return -1;
			}
			if( dim2.length - offset2 < count ) {
				return 1;
			}
			for( int i = 0; i < count; i++ ) {
				final int d1 = dim1[offset1 + i];
				final int d2 = dim2[offset2 + i];
				final int res = d1 - d2;
				if( res != 0 ) {
					return res;
				}
			}
			return 0;
		}
		private static boolean memcmp(final OggPacket p1, final OggPacket p2) {
			return (p1.packet_base == p2.packet_base) &
					(p1.packet == p2.packet) &
					(p1.bytes == p2.bytes) &
					(p1.b_o_s == p2.b_o_s) &
					(p1.e_o_s == p2.e_o_s) &
					(p1.granulepos == p2.granulepos) &
					(p1.packetno == p2.packetno);
		}
		@SuppressWarnings("boxing")
		private static void test_pack(final int[] pl, final int[][] headers, final int byteskip,
						int pageskip, final int packetskip) {
			byte[] data = new byte[1024 * 1024];// for scripted test cases only
			int inptr = 0;
			int outptr = 0;
			int deptr = 0;
			int depacket = 0;
			int granule_pos = 7, pageno = 0;
			int i, j, packets, pageout = pageskip;
			boolean eosflag = false;
			boolean bosflag = false;

			int byteskipcount = 0;

			os_en.reset();
			os_de.reset();
			oy.reset();

			for( packets = 0; packets < packetskip; packets++ ) {
				depacket += pl[packets];
			}

			for( packets = 0; ; packets++ ) {
				if( pl[packets] == -1 ) {
					break;
				}
			}

			for( i = 0; i < packets; i++ ) {
				// construct a test packet
				final OggPacket op = new OggPacket();
				final int len = pl[i];

				op.packet_base = data;
				op.packet = inptr;
				op.bytes = len;
				op.e_o_s = pl[i + 1] < 0;// (pl[i + 1] < 0 ? 1 : 0);
				op.granulepos = granule_pos;

				granule_pos += 1024;

				for( j = 0; j < len; j++ ) {
					data[inptr++] = (byte)(i + j);
				}

				// submit the test packet
				os_en.packetin( op );

				// retrieve any finished pages
				{
					final OggPage og = new OggPage();

					while( os_en.pageout( og ) != 0 ) {
						// We have a page. Check it carefully

						System.err.printf("%d, ", pageno );

						if( headers[pageno] == null ) {
							System.err.print("coded too many pages!\n");
							System.exit( 1 );
						}

						check_page( data, outptr, headers[pageno], og );

						outptr += og.body_len;
						pageno++;
						if( pageskip != 0 ) {
							bosflag = true;
							pageskip--;
							deptr += og.body_len;
						}

						// have a complete page; submit it to sync/decode
						{
							final OggPage og_de = new OggPage();
							final OggPacket op_de = new OggPacket();
							final OggPacket op_de2 = new OggPacket();
							final int buf = oy.buffer( og.header_len + og.body_len );
							int next = buf;
							byteskipcount += og.header_len;
							if( byteskipcount > byteskip ) {
								System.arraycopy( og.header_base, og.header,
										oy.data, next, byteskipcount - byteskip );
								next += byteskipcount - byteskip;
								byteskipcount = byteskip;
							}

							byteskipcount += og.body_len;
							if( byteskipcount > byteskip ) {
								System.arraycopy( og.body_base, og.body,
										oy.data, next, byteskipcount - byteskip );
								next += byteskipcount - byteskip;
								byteskipcount = byteskip;
							}

							oy.wrote( next - buf );

							while( true ) {
								final int ret = oy.pageout( og_de );
								if( ret == 0 ) {
									break;
								}
								if( ret < 0 )
								 {
									continue;
									// got a page. Happy happy. Verify that it's good.
								}

								System.err.printf("(%d), ", pageout );

								check_page( data, deptr, headers[pageout], og_de );
								deptr += og_de.body_len;
								pageout++;

								// submit it to deconstitution
								os_de.pagein( og_de );

								// packets out?
								while( os_de.packetpeek( op_de2 ) > 0 ) {
									os_de.packetpeek( null );
									os_de.packetout( op_de );// just catching them all

									// verify peek and out match
									if( compare_packet( op_de, op_de2 ) ) {
										System.err.printf("packetout != packetpeek! pos=%d\n", depacket);
										System.exit( 1 );
									}

									// verify the packet!
									// check data
									if( memcmp( data, depacket, op_de.packet_base, op_de.packet, op_de.bytes )
											!= 0 ) {
										System.err.printf("packet data mismatch in decode! pos=%d\n", depacket);
										System.exit( 1 );
									}
									// check bos flag
									if( ! bosflag && ! op_de.b_o_s ) {
										System.err.print("b_o_s flag not set on packet!\n");
										System.exit( 1 );
									}
									if( bosflag && op_de.b_o_s ) {
										System.err.print("b_o_s flag incorrectly set on packet!\n");
										System.exit( 1 );
									}
									bosflag = true;
									depacket += op_de.bytes;

									// check eos flag
									if( eosflag ) {
										System.err.print("Multiple decoded packets with eos flag!\n");
										System.exit( 1 );
									}

									if( op_de.e_o_s ) {
										eosflag = true;
									}

									// check granulepos flag
									if( op_de.granulepos != -1 ) {
										System.err.printf(" granule:%d ", op_de.granulepos);
									}
								}
							}
						}
					}
				}
			}
			data = null;
			if( headers[pageno] != null ) {
				System.err.print("did not write last page!\n");
				System.exit( 1 );
			}
			if( headers[pageout] != null ) {
				System.err.print("did not decode last page!\n");
				System.exit( 1 );
			}
			if( inptr != outptr ) {
				System.err.print("encoded page data incomplete!\n");
				System.exit( 1 );
			}
			if( inptr != deptr ) {
				System.err.print("decoded page data incomplete!\n");
				System.exit( 1 );
			}
			if( inptr != depacket ) {
				System.err.print("decoded packet data incomplete!\n");
				System.exit( 1 );
			}
			if( ! eosflag ) {
				System.err.print("Never got a packet with EOS set!\n");
				System.exit( 1 );
			}
			System.err.print("ok.\n");
		}

		public static void main(final String args[]) {

			os_en.init( 0x04030201 );
			os_de.init( 0x04030201 );
			oy.init();

			// Exercise each code path in the framing code. Also verify that
			// the checksums are working.

			{
				// 17 only
				final int packets[] = { 17, -1 };
				final int headret[][] = { head1_0, null };

				System.err.print("testing single page encoding... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// 17, 254, 255, 256, 500, 510, 600 byte, pad
				final int packets[] = { 17, 254, 255, 256, 500, 510, 600, -1 };
				final int headret[][] = { head1_1, head2_1, null };

				System.err.print("testing basic page encoding... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// nil packets; beginning,middle,end
				final int packets[] = { 0,17, 254, 255, 0, 256, 0, 500, 510, 600, 0, -1 };
				final int headret[][] = { head1_2, head2_2, null};

				System.err.print("testing basic nil packets... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// large initial packet
				final int packets[] = { 4345,259,255,-1 };
				final int headret[][] = { head1_3, head2_3, null };

				System.err.print("testing initial-packet lacing > 4k... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// continuing packet test; with page spill expansion, we have to
		 		// overflow the lacing table.
				final int packets[] = { 0,65500,259,255,-1 };
				final int headret[][] = { head1_4, head2_4, head3_4, null };

				System.err.print("testing single packet page span... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// spill expand packet test
				final int packets[] = { 0,4345,259,255,0,0,-1 };
				final int headret[][] = { head1_4b, head2_4b, head3_4b, null };

				System.err.print("testing page spill expansion... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			// page with the 255 segment limit
			{

				final int packets[] = { 0,10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,10,
						 10,10,10,10,10,10,10,50,-1};
				final int headret[][] = { head1_5, head2_5, head3_5, null };

				System.err.print("testing max packet segments... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// packet that overspans over an entire page
				final int packets[] = { 0,100,130049,259,255,-1 };
				final int headret[][] = { head1_6, head2_6, head3_6, head4_6, null };

				System.err.print("testing very large packets... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// test for the libogg 1.1.1 resync in large continuation bug
		 		// found by Josh Coalson)
				final int packets[] = { 0,100,130049,259,255,-1 };
				final int headret[][] = { head1_6, head2_6, head3_6, head4_6, null };

				System.err.print("testing continuation resync in very large packets... ");
				test_pack( packets, headret, 100, 2, 3 );
			}

			{
				// term only page. why not?
				final int packets[] = { 0,100,64770,-1 };
				final int headret[][] = { head1_7,head2_7,head3_7,null };

				System.err.print("testing zero data page (1 nil packet)... ");
				test_pack( packets, headret, 0, 0, 0 );
			}

			{
				// build a bunch of pages for testing
				byte[] data = new byte[1024 * 1024];
				final int pl[] = {0, 1,1,98,4079, 1,1,2954,2057, 76,34,912,0,234,1000,1000, 1000,300,-1};
				int inptr = 0, i, j;
				final OggPage[] og = { new OggPage(), new OggPage(),
						new OggPage(), new OggPage(), new OggPage() };

				os_en.reset();

				for( i = 0; pl[i] != -1; i++ ) {
					final OggPacket op = new OggPacket();
					final int len = pl[i];

					op.packet_base = data;
					op.packet = inptr;
					op.bytes = len;
					op.e_o_s = pl[i + 1] < 0;// (pl[i + 1] < 0 ? 1 : 0);
					op.granulepos = (i + 1) * 1000;

					for( j = 0; j < len; j++ ) {
						data[inptr++] = (byte)(i + j);
					}
					os_en.packetin( op );
				}

				data = null;

				// retrieve finished pages
				for( i = 0; i < 5; i++ ) {
					if( os_en.pageout( og[i] ) == 0 ) {
						System.err.print("Too few pages output building sync tests!\n");
						System.exit( 1 );
					}
					copy_page( og[i] );
				}

				// Test lost pages on pagein/packetout: no rollback
				{
					final OggPage temp = new OggPage();
					final OggPacket test = new OggPacket();

					System.err.print("Testing loss of pages... ");

					oy.reset();
					os_de.reset();
					for( i = 0; i < 5; i++ ) {
						System.arraycopy( og[i].header_base, og[i].header,
							oy.data, oy.buffer( og[i].header_len ), og[i].header_len );
						oy.wrote( og[i].header_len );
						System.arraycopy( og[i].body_base, og[i].body,
							oy.data, oy.buffer( og[i].body_len ), og[i].body_len );
						oy.wrote( og[i].body_len );
					}

					oy.pageout( temp );
					os_de.pagein( temp );
					oy.pageout( temp );
					os_de.pagein( temp );
					oy.pageout( temp );
					// skip
					oy.pageout( temp );
					os_de.pagein( temp );

					// do we get the expected results/packets?

					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 0, 0, 0 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 1, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 2, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 98, 3, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 4079, 4, 5000 );
					if( os_de.packetout( test ) != -1 ) {
						System.err.print("Error: loss of page did not return error\n");
						System.exit( 1 );
					}
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 76, 9, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 34, 10, -1 );
					System.err.print("ok.\n");
				}

				// Test lost pages on pagein/packetout: rollback with continuation
				{
					final OggPage temp = new OggPage();
					final OggPacket test = new OggPacket();

					System.err.print("Testing loss of pages (rollback required)... ");

					oy.reset();
					os_de.reset();
					for( i = 0; i < 5; i++ ) {
						System.arraycopy( og[i].header_base, og[i].header,
							oy.data, oy.buffer( og[i].header_len ), og[i].header_len );
						oy.wrote( og[i].header_len );
						System.arraycopy( og[i].body_base, og[i].body,
							oy.data, oy.buffer( og[i].body_len ), og[i].body_len );
						oy.wrote( og[i].body_len );
					}

					oy.pageout( temp );
					os_de.pagein( temp );
					oy.pageout( temp );
					os_de.pagein( temp );
					oy.pageout( temp );
					os_de.pagein( temp );
					oy.pageout( temp );
					// skip
					oy.pageout( temp );
					os_de.pagein( temp );

					// do we get the expected results/packets?

					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 0, 0, 0 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 1, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 2, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 98, 3, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 4079, 4, 5000 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 5, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 1, 6, -1 );
					if( os_de.packetout( test ) != 1 ) {
						error();
					}
					checkpacket( test, 2954, 7, -1 );
					if( os_de.packetout( test ) != 1) {
						error();
					}
					checkpacket( test, 2057, 8, 9000 );
					if( os_de.packetout( test ) != -1 ){
						System.err.print("Error: loss of page did not return error\n");
						System.exit( 1 );
					}
					if( os_de.packetout( test) != 1 ) {
						error();
					}
					checkpacket( test, 300, 17, 18000 );
					System.err.print("ok.\n");
				}

				// the rest only test sync
				{
					final OggPage og_de = new OggPage();
					// Test fractional page inputs: incomplete capture
					System.err.print("Testing sync on partial inputs... ");
					oy.reset();
					System.arraycopy( og[1].header_base, og[1].header,
						oy.data, oy.buffer( og[1].header_len ), 3 );
					oy.wrote( 3 );
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					// Test fractional page inputs: incomplete fixed header
					System.arraycopy( og[1].header_base, og[1].header + 3,
						oy.data, oy.buffer( og[1].header_len ), 20 );
					oy.wrote( 20 );
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					// Test fractional page inputs: incomplete header
					System.arraycopy( og[1].header_base, og[1].header + 23,
						oy.data, oy.buffer( og[1].header_len ), 5 );
					oy.wrote( 5 );
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					// Test fractional page inputs: incomplete body

					System.arraycopy( og[1].header_base, og[1].header + 28,
						oy.data, oy.buffer( og[1].header_len ), og[1].header_len - 28 );
					oy.wrote( og[1].header_len - 28 );
					if( oy.pageout( og_de) > 0 ) {
						error();
					}

					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), 1000 );
					oy.wrote( 1000 );
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					System.arraycopy( og[1].body_base, og[1].body + 1000,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len - 1000 );
					oy.wrote( og[1].body_len - 1000 );
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}

					System.err.print("ok.\n");
				}

				// Test fractional page inputs: page + incomplete capture
				{
					final OggPage og_de = new OggPage();
					System.err.print("Testing sync on 1+partial inputs... ");
					oy.reset();

					System.arraycopy( og[1].header_base, og[1].header,
						oy.data, oy.buffer( og[1].header_len ), og[1].header_len );
					oy.wrote( og[1].header_len );

					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len );
					oy.wrote( og[1].body_len );

					System.arraycopy( og[1].header_base, og[1].header,
						oy.data, oy.buffer( og[1].header_len ), 20 );
					oy.wrote( 20 );
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					System.arraycopy( og[1].header_base, og[1].header + 20,
						oy.data, oy.buffer( og[1].header_len ), og[1].header_len - 20 );
					oy.wrote( og[1].header_len - 20 );
					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len );
					oy.wrote( og[1].body_len );
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}

					System.err.print("ok.\n");
				}

				// Test recapture: garbage + page
				{
					final OggPage og_de = new OggPage();
					System.err.print("Testing search for capture... ");
					oy.reset();

					// 'garbage'
					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len );
					oy.wrote( og[1].body_len );

					System.arraycopy( og[1].header_base, og[1].header,
						oy.data, oy.buffer( og[1].header_len ), og[1].header_len );
					oy.wrote( og[1].header_len );

					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len );
					oy.wrote( og[1].body_len );

					System.arraycopy( og[2].header_base, og[2].header,
						oy.data, oy.buffer( og[2].header_len), 20 );
					oy.wrote( 20 );
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}
					if( oy.pageout( og_de ) > 0 ) {
						error();
					}

					System.arraycopy( og[2].header_base, og[2].header + 20,
						oy.data, oy.buffer( og[2].header_len ), og[2].header_len - 20 );
					oy.wrote( og[2].header_len - 20 );
					System.arraycopy( og[2].body_base, og[2].body,
						oy.data, oy.buffer( og[2].body_len ), og[2].body_len );
					oy.wrote( og[2].body_len );
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}

					System.err.print("ok.\n");
				}

				// Test recapture: page + garbage + page
				{
					final OggPage og_de = new OggPage();
					System.err.print("Testing recapture... ");
					oy.reset();

					System.arraycopy( og[1].header_base, og[1].header,
						oy.data, oy.buffer( og[1].header_len), og[1].header_len );
					oy.wrote( og[1].header_len );

					System.arraycopy( og[1].body_base, og[1].body,
						oy.data, oy.buffer( og[1].body_len ), og[1].body_len );
					oy.wrote( og[1].body_len );

					System.arraycopy( og[2].header_base, og[2].header,
						oy.data, oy.buffer( og[2].header_len ), og[2].header_len );
					oy.wrote( og[2].header_len );

					System.arraycopy( og[2].header_base, og[2].header,
						oy.data, oy.buffer( og[2].header_len ), og[2].header_len );
					oy.wrote( og[2].header_len );

					if( oy.pageout( og_de ) <= 0) {
						error();
					}

					System.arraycopy( og[2].body_base, og[2].body,
						oy.data, oy.buffer( og[2].body_len ), og[2].body_len - 5 );
					oy.wrote( og[2].body_len - 5 );

					System.arraycopy( og[3].header_base, og[3].header,
						oy.data, oy.buffer( og[3].header_len ), og[3].header_len );
					oy.wrote( og[3].header_len );

					System.arraycopy( og[3].body_base, og[3].body,
						oy.data, oy.buffer( og[3].body_len ), og[3].body_len );
					oy.wrote( og[3].body_len );

					if( oy.pageout( og_de ) > 0 ) {
						error();
					}
					if( oy.pageout( og_de ) <= 0 ) {
						error();
					}

					System.err.print("ok.\n");
				}

				// Free page data that was previously copied
				{
					for( i = 0; i < 5; i++ ) {
						free_page( og[i] );
					}
				}
			}
			oy.clear();
			os_en.clear();
			os_de.clear();

			System.exit( 0 );
		}
		/* #endif _V_SELFTEST */
}

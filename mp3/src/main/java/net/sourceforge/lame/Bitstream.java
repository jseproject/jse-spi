package net.sourceforge.lame;

import net.sourceforge.lame.EncStateVar.Header;

// bitstream.c

class Bitstream {
	// struct Bit_stream
	/** bit stream buffer */
	byte[] buf;
	/** size of buffer (in number of bytes) */
	int    buf_size;
	/** bit counter of bit stream */
	int    totbit;
	/** pointer to top byte in buffer */
	int    buf_byte_idx;
	/** pointer to top bit of top byte in buffer */
	int    buf_bit_idx;
	/* format of file in rd mode (BINARY/ASCII) */
	// end struct Bit_stream

	private static final int CRC16_POLYNOMIAL = 0x8005;
	private static final int BUFFER_SIZE = LAME.LAME_MAXMP3BUFFER;

	/** unsigned int is at least this large:
	 * we work with ints, so when doing bit manipulation, we limit
	 * ourselves to MAX_LENGTH-2 just to be on the safe side */
	// private static final int MAX_LENGTH = 32;

	private static final int calcFrameLength(final SessionConfig cfg, final int kbps, final int pad) {
		return ((cfg.version + 1) * 72000 * kbps / cfg.samplerate + pad ) << 3;
	}

	/***********************************************************************
	 * compute bitsperframe and mean_bits for a layer III frame
	 **********************************************************************/
	static final int getframebits(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		int bit_rate;

		/* get bitrate in kbps [?] */
		if( eov.bitrate_index != 0 ) {
			bit_rate = Tables.bitrate_table[cfg.version][eov.bitrate_index];
		} else {
			bit_rate = cfg.avg_bitrate;
		}

		/* main encoding routine toggles padding on and off */
		/* one Layer3 Slot consists of 8 bits */
		return calcFrameLength( cfg, bit_rate, eov.padding ? 1 : 0 );
	}

	static final int get_max_frame_buffer_size_by_constraint(final SessionConfig cfg, final int constraint) {
		int maxmp3buf = 0;
		if( cfg.avg_bitrate > 320 ) {
			/* in freeformat the buffer is constant */
			if( constraint == LAME.MDB_STRICT_ISO ) {
				maxmp3buf = calcFrameLength( cfg, cfg.avg_bitrate, 0 );
			} else {
				/* maximum allowed bits per granule are 7680 */
				maxmp3buf = 7680 * (cfg.version + 1);
			}
		} else {
			int max_kbps;
			if( cfg.samplerate < 16000 ) {
				max_kbps = Tables.bitrate_table[cfg.version][8]; /* default: allow 64 kbps (MPEG-2.5) */
			} else {
				max_kbps = Tables.bitrate_table[cfg.version][14];
			}
			switch( constraint )
			{
			default:
			case LAME.MDB_DEFAULT:
				/* Bouvigne suggests this more lax interpretation of the ISO doc instead of using 8*960. */
				/* All mp3 decoders should have enough buffer to handle this value: size of a 320kbps 32kHz frame */
				maxmp3buf = 8 * 1440;
				break;
			case LAME.MDB_STRICT_ISO:
				maxmp3buf = calcFrameLength( cfg, max_kbps, 0 );
				break;
			case LAME.MDB_MAXIMUM:
				maxmp3buf = 7680 * (cfg.version + 1);
				break;
			}
		}
		return maxmp3buf;
	}

	private static final void putheader_bits(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		final Bitstream bs = gfc.bs;
		System.arraycopy( esv.header[esv.w_ptr].buf, 0, bs.buf, bs.buf_byte_idx, cfg.sideinfo_len );
		bs.buf_byte_idx += cfg.sideinfo_len;
		bs.totbit += cfg.sideinfo_len << 3;
		esv.w_ptr = (esv.w_ptr + 1) & (EncStateVar.MAX_HEADER_BUF - 1 );
	}

	/** write j bits into the bit stream */
	private static final void putbits2(final InternalFlags gfc, final int val, int j) {
		final EncStateVar esv = gfc.sv_enc;
		final Bitstream bs = gfc.bs;
		final byte[] buf = bs.buf;// java
		int buf_bit_idx = bs.buf_bit_idx;// java

		while( j > 0 ) {
			if( buf_bit_idx == 0 ) {
				buf_bit_idx = 8;
				bs.buf_byte_idx++;
				if( esv.header[esv.w_ptr].write_timing == bs.totbit ) {
					putheader_bits( gfc );
				}
				buf[bs.buf_byte_idx] = 0;
			}

			final int k = (j <= buf_bit_idx ? j : buf_bit_idx);
			j -= k;

			buf_bit_idx -= k;

			buf[bs.buf_byte_idx] |= ( (val >> j) << buf_bit_idx );
			bs.totbit += k;
		}
		bs.buf_bit_idx = buf_bit_idx;// java
	}

	/** write j bits into the bit stream, ignoring frame headers */
	private static final void putbits_noheaders(final InternalFlags gfc, final int val, int j) {
		final Bitstream bs = gfc.bs;
		final byte[] buf = bs.buf;// java
		int buf_bit_idx = bs.buf_bit_idx;// java
		int buf_byte_idx = bs.buf_byte_idx;// java

		while( j > 0 ) {
			if( buf_bit_idx == 0 ) {
				buf_bit_idx = 8;
				buf_byte_idx++;
				buf[buf_byte_idx] = 0;
			}

			final int k = (j <= buf_bit_idx ? j : buf_bit_idx);
			j -= k;

			buf_bit_idx -= k;

			buf[buf_byte_idx] |= ( (val >> j) << buf_bit_idx );
			bs.totbit += k;
		}
		bs.buf_bit_idx = buf_bit_idx;// java
		bs.buf_byte_idx = buf_byte_idx;// java
	}

	/*
	  Some combinations of bitrate, Fs, and stereo make it impossible to stuff
	  out a frame using just main_data, due to the limited number of bits to
	  indicate main_data_length. In these situations, we put stuffing bits into
	  the ancillary data...
	*/
	private static final void drain_into_ancillary(final InternalFlags gfc, int remainingBits) {
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;

		if( remainingBits >= 8 ) {
			putbits2( gfc, 0x4c, 8 );
			remainingBits -= 8;
		}
		if( remainingBits >= 8 ) {
			putbits2( gfc, 0x41, 8 );
			remainingBits -= 8;
		}
		if( remainingBits >= 8 ) {
			putbits2( gfc, 0x4d, 8 );
			remainingBits -= 8;
		}
		if( remainingBits >= 8 ) {
			putbits2( gfc, 0x45, 8 );
			remainingBits -= 8;
		}

		if( remainingBits >= 32 ) {
			final String version = Version.get_lame_short_version();
			if( remainingBits >= 32 ) {
				for( int i = 0, length = version.length(); i < length && remainingBits >= 8; ++i ) {
					remainingBits -= 8;
					putbits2( gfc, (int)version.charAt( i ), 8 );
				}
			}
		}

		for( ; remainingBits >= 1; remainingBits-- ) {
			putbits2( gfc, esv.ancillary_flag ? 1 : 0, 1 );
			esv.ancillary_flag ^= ! cfg.disable_reservoir;
		}
	}

	/**write N bits into the header */
	private static void writeheader(final InternalFlags gfc, final int val, int j) {
		final EncStateVar esv = gfc.sv_enc;
		final Header header = esv.header[esv.h_ptr];
		final byte[] buf = header.buf;// java
		int ptr = header.ptr;

		while( j > 0 ) {
			int k = 8 - (ptr & 7);
			k = ( j <= k ? j : k );
			j -= k;
			buf[ptr >> 3] |= ((val >> j)) << (8 - (ptr & 7) - k );
			ptr += k;
		}
		header.ptr = ptr;
	}

	private static final int CRC_update(int value, int crc) {
		value <<= 8;
		for( int i = 0; i < 8; i++ ) {
			value <<= 1;
			crc <<= 1;

			if( ((crc ^ value) & 0x10000) != 0 ) {
				crc ^= CRC16_POLYNOMIAL;
			}
		}
		return crc;
	}

	static final void CRC_writeheader(final InternalFlags gfc, final byte[] header) {
		final SessionConfig cfg = gfc.cfg;
		int crc = 0xffff;    /* (jo) init crc16 for error_protection */

		crc = CRC_update( (int)header[2] & 0xff, crc );
		crc = CRC_update( (int)header[3] & 0xff, crc );
		for( int i = 6, ie = cfg.sideinfo_len; i < ie; i++ ) {
			crc = CRC_update( (int)header[i] & 0xff, crc );
		}

		header[4] = (byte)(crc >> 8);
		header[5] = (byte)crc;
	}

	private static final void encodeSideInfo2(final InternalFlags gfc, final int bitsPerFrame) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;
		final EncStateVar esv = gfc.sv_enc;

		final III_SideInfo l3_side = gfc.l3_side;
		esv.header[esv.h_ptr].ptr = 0;
		final byte[] buf = esv.header[esv.h_ptr].buf;
		for( int i = 0, ie = cfg.sideinfo_len; i < ie; i++ ) {
			buf[i] = 0;
		}
		if( cfg.samplerate < 16000 ) {
			writeheader( gfc, 0xffe, 12 );
		} else {
			writeheader( gfc, 0xfff, 12 );
		}
		writeheader( gfc, (cfg.version), 1 );
		writeheader( gfc, 4 - 3, 2 );
		writeheader( gfc, (cfg.error_protection ? 0 : 1), 1 );
		writeheader( gfc, (eov.bitrate_index), 4 );
		writeheader( gfc, (cfg.samplerate_index), 2 );
		writeheader( gfc, (eov.padding ? 1 : 0), 1 );
		writeheader( gfc, (cfg.extension ? 1 : 0), 1 );
		writeheader( gfc, (cfg.mode), 2 );
		writeheader( gfc, (eov.mode_ext), 2 );
		writeheader( gfc, (cfg.copyright ? 1 : 0), 1 );
		writeheader( gfc, (cfg.original ? 1 : 0), 1 );
		writeheader( gfc, (cfg.emphasis), 2 );
		if( cfg.error_protection ) {
			writeheader( gfc, 0, 16 ); /* dummy */
		}

		final int channels_out = cfg.channels_out;// java
		if( cfg.version == 1 ) {
			/* MPEG1 */
			writeheader( gfc, l3_side.main_data_begin, 9 );

			writeheader( gfc, l3_side.private_bits, channels_out == 2 ? 3 : 5 );

			int ch = 0;
			final int[][] scfsi = l3_side.scfsi;// java
			do {
				final int[] scfsi_ch = scfsi[ch];// java
				writeheader( gfc, scfsi_ch[0], 1 );
				writeheader( gfc, scfsi_ch[1], 1 );
				writeheader( gfc, scfsi_ch[2], 1 );
				writeheader( gfc, scfsi_ch[3], 1 );
			} while( ++ch < channels_out );

			int gr = 0;
			do {
				ch = 0;
				do {
					final III_GrInfo gi = l3_side.tt[gr][ch];
					writeheader( gfc, gi.part2_3_length + gi.part2_length, 12 );
					writeheader( gfc, gi.big_values >> 1, 9 );
					writeheader( gfc, gi.global_gain, 8 );
					writeheader( gfc, gi.scalefac_compress, 4 );

					final int[] table_select = gi.table_select;// java
					if( gi.block_type != Encoder.NORM_TYPE ) {
						writeheader( gfc, 1, 1 ); /* window_switching_flag */
						writeheader( gfc, gi.block_type, 2 );
						writeheader( gfc, gi.mixed_block_flag ? 1 : 0, 1 );

						if( table_select[0] == 14 ) {
							table_select[0] = 16;
						}
						writeheader( gfc, table_select[0], 5 );
						if( table_select[1] == 14 ) {
							table_select[1] = 16;
						}
						writeheader( gfc, table_select[1], 5 );

						writeheader( gfc, gi.subblock_gain[0], 3 );
						writeheader( gfc, gi.subblock_gain[1], 3 );
						writeheader( gfc, gi.subblock_gain[2], 3 );
					} else {
						writeheader( gfc, 0, 1 ); /* window_switching_flag */
						if( table_select[0] == 14 ) {
							table_select[0] = 16;
						}
						writeheader( gfc, table_select[0], 5 );
						if( table_select[1] == 14 ) {
							table_select[1] = 16;
						}
						writeheader( gfc, table_select[1], 5 );
						if( table_select[2] == 14 ) {
							table_select[2] = 16;
						}
						writeheader( gfc, table_select[2], 5 );

						writeheader( gfc, gi.region0_count, 4 );
						writeheader( gfc, gi.region1_count, 3 );
					}
					writeheader( gfc, gi.preflag ? 1 : 0, 1 );
					writeheader( gfc, gi.scalefac_scale, 1 );
					writeheader( gfc, gi.count1table_select, 1 );
				} while( ++ch < channels_out );
			} while( ++gr < 2 );
		} else {
			/* MPEG2 */
			writeheader( gfc, (l3_side.main_data_begin), 8 );
			writeheader( gfc, l3_side.private_bits, channels_out );

			final III_GrInfo[] tt_gr = l3_side.tt[0];// java
			int ch = 0;
			do {
				final III_GrInfo gi = tt_gr[ch];
				writeheader( gfc, gi.part2_3_length + gi.part2_length, 12 );
				writeheader( gfc, gi.big_values >> 1, 9 );
				writeheader( gfc, gi.global_gain, 8 );
				writeheader( gfc, gi.scalefac_compress, 9 );

				if( gi.block_type != Encoder.NORM_TYPE ) {
					writeheader( gfc, 1, 1 ); /* window_switching_flag */
					writeheader( gfc, gi.block_type, 2 );
					writeheader( gfc, gi.mixed_block_flag ? 1 : 0, 1 );

					if( gi.table_select[0] == 14 ) {
						gi.table_select[0] = 16;
					}
					writeheader( gfc, gi.table_select[0], 5 );
					if( gi.table_select[1] == 14) {
						gi.table_select[1] = 16;
					}
					writeheader( gfc, gi.table_select[1], 5 );

					writeheader( gfc, gi.subblock_gain[0], 3 );
					writeheader( gfc, gi.subblock_gain[1], 3 );
					writeheader( gfc, gi.subblock_gain[2], 3 );
				} else {
					writeheader( gfc, 0, 1 ); /* window_switching_flag */
					if( gi.table_select[0] == 14 ) {
						gi.table_select[0] = 16;
					}
					writeheader( gfc, gi.table_select[0], 5 );
					if( gi.table_select[1] == 14 ) {
						gi.table_select[1] = 16;
					}
					writeheader( gfc, gi.table_select[1], 5 );
					if( gi.table_select[2] == 14 ) {
						gi.table_select[2] = 16;
					}
					writeheader( gfc, gi.table_select[2], 5 );

					writeheader( gfc, gi.region0_count, 4 );
					writeheader( gfc, gi.region1_count, 3 );
				}

				writeheader( gfc, gi.scalefac_scale, 1 );
				writeheader( gfc, gi.count1table_select, 1 );
			} while( ++ch < channels_out );
		}

		if( cfg.error_protection ) {
			/* (jo) error_protection: add crc16 information to header */
			CRC_writeheader( gfc, esv.header[esv.h_ptr].buf );
		}

		{
			final int old = esv.h_ptr;

			esv.h_ptr = (old + 1) & (EncStateVar.MAX_HEADER_BUF - 1 );
			esv.header[esv.h_ptr].write_timing = esv.header[old].write_timing + bitsPerFrame;

			if( esv.h_ptr == esv.w_ptr ) {
				/* yikes! we are out of header buffer space */
				//System.err.print("Error: MAX_HEADER_BUF too small in bitstream.c \n");
			}
		}
	}

	private static final int huffman_coder_count1(final InternalFlags gfc, final III_GrInfo gi) {
		/* Write count1 area */
		final HuffCodeTab h = Tables.ht[gi.count1table_select + 32];
		int bits = 0;

		final int[] l3_enc = gi.l3_enc;// java
		int ix = gi.big_values;// l3_enc[ix]
		final float[] gi_xr = gi.xr;// java
		int xr = gi.big_values;// gi_xr[xr]

		final char[] table = h.table;// java
		final byte[] hlen = h.hlen;// java
		for( int i = (gi.count1 - gi.big_values) >> 2; i > 0; --i ) {
			int huffbits = 0;
			int p = 0;

			int v = l3_enc[ix++];
			if( v != 0 ) {
				p += 8;
				if( gi_xr[xr] < 0.0f ) {
					huffbits++;
				}
			}
			xr++;

			v = l3_enc[ix++];
			if( v != 0 ) {
				p += 4;
				huffbits <<= 1;
				if( gi_xr[xr] < 0.0f ) {
					huffbits++;
				}
			}
			xr++;

			v = l3_enc[ix++];
			if( v != 0 ) {
				p += 2;
				huffbits <<= 1;
				if( gi_xr[xr] < 0.0f ) {
					huffbits++;
				}
			}
			xr++;

			v = l3_enc[ix++];
			if( v != 0 ) {
				p++;
				huffbits <<= 1;
				if( gi_xr[xr] < 0.0f ) {
					huffbits++;
				}
			}
			xr++;

			// ix += 4;
			// xr += 4;
			putbits2( gfc, huffbits + table[p], hlen[p] );
			bits += hlen[p];
		}
		return bits;
}

	/** Implements the pseudocode of page 98 of the IS */
	private static final int Huffmancode(final InternalFlags gfc, final int tableindex, final int start, final int end, final III_GrInfo gi)
	{
		if( 0 == tableindex ) {
			return 0;
		}

		final HuffCodeTab h = Tables.ht[tableindex];
		final int linbits = h.xlen;
		int bits = 0;

		final int[] l3_enc = gi.l3_enc;// java
		final float[] xr = gi.xr;// java
		final char[] table = h.table;// java
		final byte[] hlen = h.hlen;// java
		for( int i = start; i < end; i += 2 ) {
			short cbits = 0;
			char xbits = 0;
			int xlen = linbits;// h.xlen;
			int ext = 0;
			int x1 = l3_enc[i];
			int x2 = l3_enc[i + 1];

			if( x1 != 0 ) {
				if( xr[i] < 0.0f ) {
					ext++;
				}
				cbits--;
			}

			if( tableindex > 15 ) {
				/* use ESC-words */
				if( x1 >= 15 ) {
					final int linbits_x1 = x1 - 15;
					ext |= linbits_x1 << 1;
					xbits = (char)linbits;
					x1 = 15;
				}

				if( x2 >= 15 ) {
					final int linbits_x2 = x2 - 15;
					ext <<= linbits;
					ext |= linbits_x2;
					xbits += linbits;
					x2 = 15;
				}
				xlen = 16;
			}

			if( x2 != 0 ) {
				ext <<= 1;
				if( xr[i + 1] < 0.0f ) {
					ext++;
				}
				cbits--;
			}

			x1 = x1 * xlen + x2;
			xbits -= cbits;
			cbits += hlen[x1];


			putbits2( gfc, table[x1], cbits );
			putbits2( gfc, ext, xbits );
			bits += cbits + xbits;
		}
		return bits;
	}

	/*
	  Note the discussion of huffmancodebits() on pages 28
	  and 29 of the IS, as well as the definitions of the side
	  information on pages 26 and 27.
	  */
	private static final int ShortHuffmancodebits(final InternalFlags gfc, final III_GrInfo gi) {
		int region1Start = 3 * gfc.scalefac_band.s[3];
		if( region1Start > gi.big_values ) {
			region1Start = gi.big_values;
		}

		/* short blocks do not have a region2 */
		int bits = Huffmancode( gfc, gi.table_select[0], 0, region1Start, gi );
		bits += Huffmancode( gfc, gi.table_select[1], region1Start, gi.big_values, gi );
		return bits;
	}

	private static final int LongHuffmancodebits(final InternalFlags gfc, final III_GrInfo gi) {
		final int bigvalues = gi.big_values;

		int i = gi.region0_count + 1;

		final int[] sbl = gfc.scalefac_band.l;// java
		int region1Start = sbl[i];
		i += gi.region1_count + 1;

		int region2Start = sbl[i];

		if( region1Start > bigvalues ) {
			region1Start = bigvalues;
		}

		if( region2Start > bigvalues ) {
			region2Start = bigvalues;
		}

		final int[] table_select = gi.table_select;// java
		int bits = Huffmancode( gfc, table_select[0], 0, region1Start, gi );
		bits += Huffmancode( gfc, table_select[1], region1Start, region2Start, gi );
		bits += Huffmancode( gfc, table_select[2], region2Start, bigvalues, gi );
		return bits;
	}

	private static final int writeMainData(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final III_SideInfo l3_side = gfc.l3_side;
		int tot_bits = 0;
		final int channels_out = cfg.channels_out;// java

		if( cfg.version == 1 ) {
			/* MPEG 1 */
			final III_GrInfo[][] tt = l3_side.tt;// java
			int gr = 0;
			do {
				final III_GrInfo[] tt_gr = tt[gr];// java
				int ch = 0;
				do {
					final III_GrInfo gi = tt_gr[ch];
					final int[] scalefac = gi.scalefac;// java
					final int slen1 = Takehiro.slen1_tab[gi.scalefac_compress];
					final int slen2 = Takehiro.slen2_tab[gi.scalefac_compress];
					int data_bits = 0;
					int sfb = 0;
					for( final int sfbdivide = gi.sfbdivide; sfb < sfbdivide; sfb++ ) {
						final int sf = scalefac[sfb];// java
						if( sf == -1 ) {
							continue;
						} /* scfsi is used */
						putbits2( gfc, sf, slen1 );
						data_bits += slen1;
					}
					for( final int sfbmax = gi.sfbmax; sfb < sfbmax; sfb++ ) {
						final int sf = scalefac[sfb];// java
						if( sf == -1 ) {
							continue;
						} /* scfsi is used */
						putbits2( gfc, sf, slen2 );
						data_bits += slen2;
					}

					if( gi.block_type == Encoder.SHORT_TYPE ) {
						data_bits += ShortHuffmancodebits( gfc, gi );
					} else {
						data_bits += LongHuffmancodebits( gfc, gi );
					}
					data_bits += huffman_coder_count1( gfc, gi );
					/* does bitcount in quantize.c agree with actual bit count? */
					tot_bits += data_bits;
				} while( ++ch < channels_out );   /* for ch */
			} while( ++gr < 2 );    /* for gr */
		} else {
			/* MPEG 2 */
			final III_GrInfo[] tt_gr = l3_side.tt[0];// java
			int ch = 0;
			do {
				final III_GrInfo gi = tt_gr[ch];
				final int[] scalefac = gi.scalefac;// java
				int scale_bits = 0;
				int data_bits = 0;
				int sfb = 0;
				int sfb_partition = 0;
				final int[] sfb_partition_table = gi.sfb_partition_table;// java
				final int[] slen = gi.slen;// java

				if( gi.block_type == Encoder.SHORT_TYPE ) {
					do {
						final int sfbs = sfb_partition_table[sfb_partition];
						final int len = slen[sfb_partition];
						while( sfb < sfbs ) {
							putbits2( gfc, Math.max( scalefac[sfb++], 0 ), len );
							putbits2( gfc, Math.max( scalefac[sfb++], 0 ), len );
							putbits2( gfc, Math.max( scalefac[sfb++], 0 ), len );
							scale_bits += 3 * len;
						}
					} while( ++sfb_partition < 4 );
					data_bits += ShortHuffmancodebits( gfc, gi );
				} else {
					do {
						final int sfbs = sfb_partition_table[sfb_partition];
						final int len = slen[sfb_partition];
						while( sfb < sfbs ) {
							putbits2( gfc, Math.max( scalefac[sfb++], 0 ), len );
							scale_bits += len;
						}
					} while( ++sfb_partition < 4 );
					data_bits += LongHuffmancodebits( gfc, gi );
				}
				data_bits += huffman_coder_count1( gfc, gi );
				/* does bitcount in quantize.c agree with actual bit count? */
				tot_bits += scale_bits + data_bits;
			} while( ++ch < channels_out );  /* for ch */
		}                   /* for gf */
		return tot_bits;
	}                       /* main_data */

	/** compute the number of bits required to flush all mp3 frames
	   currently in the buffer.  This should be the same as the
	   reservoir size.  Only call this routine between frames - i.e.
	   only after all headers and data have been added to the buffer
	   by format_bitstream().

	   Also compute total_bits_output =
	       size of mp3 buffer (including frame headers which may not
	       have yet been send to the mp3 buffer) +
	       number of bits needed to flush all mp3 frames.

	   total_bytes_output is the size of the mp3 output buffer if
	   lame_encode_flush_nogap() was called right now.

	 * @return (flushbits) | (total_bytes_output << 32)
	 */
	static final long compute_flushbits(final InternalFlags gfc/*, final int[] total_bytes_output*/) {
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		final int first_ptr = esv.w_ptr; /* first header to add to bitstream */
		int last_ptr = esv.h_ptr - 1; /* last header to add to bitstream */
		if( last_ptr == -1 ) {
			last_ptr = EncStateVar.MAX_HEADER_BUF - 1;
		}

		/* add this many bits to bitstream so we can flush all headers */
		int flushbits = esv.header[last_ptr].write_timing - gfc.bs.totbit;
		int total_bytes_output = flushbits;

		if( flushbits >= 0 ) {
			/* if flushbits >= 0, some headers have not yet been written */
			/* reduce flushbits by the size of the headers */
			int remaining_headers = 1 + last_ptr - first_ptr;
			if( last_ptr < first_ptr ) {
				remaining_headers += EncStateVar.MAX_HEADER_BUF;
			}
			flushbits -= (remaining_headers * cfg.sideinfo_len) << 3;
		}


		/* finally, add some bits so that the last frame is complete
		 * these bits are not necessary to decode the last frame, but
		 * some decoders will ignore last frame if these bits are missing
		 */
		final int bitsPerFrame = getframebits( gfc );
		flushbits += bitsPerFrame;
		total_bytes_output += bitsPerFrame;
		/* round up:   */
		if( (total_bytes_output & 7) != 0 ) {
			total_bytes_output = 1 + (total_bytes_output >> 3);
		} else {
			total_bytes_output >>= 3;
		}
		total_bytes_output += gfc.bs.buf_byte_idx + 1;


		if( flushbits < 0 ) {
			//System.err.print("strange error flushing buffer ... \n");
		}
		return ((long)flushbits & 0xffffffffL) | ((long)total_bytes_output << 32);
	}

	static final void flush_bitstream(final InternalFlags gfc) {
		final EncStateVar esv = gfc.sv_enc;
		int last_ptr = esv.h_ptr - 1; /* last header to add to bitstream */
		if( last_ptr == -1 ) {
			last_ptr = EncStateVar.MAX_HEADER_BUF - 1;
		}

		final long tmp = compute_flushbits( gfc/*, &nbytes */ );
		final int flushbits = (int)tmp;
		if( flushbits < 0 ) {
			return;
		}
		drain_into_ancillary( gfc, flushbits );

		/* we have padded out all frames with ancillary data, which is the
		   same as filling the bitreservoir with ancillary data, so : */
		esv.ResvSize = 0;
		gfc.l3_side.main_data_begin = 0;
	}

	static final void add_dummy_byte(final InternalFlags gfc, final byte val, int n) {
		final EncStateVar esv = gfc.sv_enc;
		final int ival = (int)val & 0xff;// java
		final Header[] header = esv.header;// java

		while( n-- > 0 ) {
			putbits_noheaders( gfc, ival, 8 );

			int i = 0;
			do {
				header[i].write_timing += 8;
			} while( ++i < EncStateVar.MAX_HEADER_BUF );
		}
	}


	/**
	  format_bitstream()

	  This is called after a frame of audio has been quantized and coded.
	  It will write the encoded audio to the bitstream. Note that
	  from a layer3 encoder's perspective the bit stream is primarily
	  a series of main_data() blocks, with header and side information
	  inserted at the proper locations to maintain framing. (See Figure A.7
	  in the IS).
	  */
	@SuppressWarnings("boxing")
	static final int format_bitstream(final InternalFlags gfc) {
		final SessionConfig cfg = gfc.cfg;
		final EncStateVar esv = gfc.sv_enc;
		final III_SideInfo l3_side = gfc.l3_side;

		final int bitsPerFrame = getframebits( gfc );
		drain_into_ancillary( gfc, l3_side.resvDrain_pre );

		encodeSideInfo2( gfc, bitsPerFrame );
		int bits = cfg.sideinfo_len << 3;
		bits += writeMainData( gfc );
		drain_into_ancillary( gfc, l3_side.resvDrain_post );
		bits += l3_side.resvDrain_post;

		l3_side.main_data_begin += (bitsPerFrame - bits) >> 3;

		/* compare number of bits needed to clear all buffered mp3 frames
		 * with what we think the resvsize is: */
		final long tmp = compute_flushbits( gfc/* , &nbytes */ );
		if( (int)tmp != esv.ResvSize ) {
			//System.err.print("Internal buffer inconsistency. flushbits <> ResvSize");
		}

		/* compare main_data_begin for the next frame with what we
		 * think the resvsize is: */
		if( (l3_side.main_data_begin << 3) != esv.ResvSize ) {
			/*
			System.err.printf("bit reservoir error: \n" +
				"l3_side.main_data_begin:  %d \n" +
				"Resvoir size:             %d \n" +
				"resv drain (post)         %d \n" +
				"resv drain (pre)          %d \n" +
				"header and sideinfo:      %d \n" +
				"data bits:                %d \n" +
				"total bits:               %d (remainder: %d) \n" +
				"bitsperframe:             %d \n",
				8 * l3_side.main_data_begin,
				esv.ResvSize,
				l3_side.resvDrain_post,
				l3_side.resvDrain_pre,
				8 * cfg.sideinfo_len,
				bits - l3_side.resvDrain_post - 8 * cfg.sideinfo_len,
				bits, bits % 8, bitsPerFrame );

			System.err.print( "This is a fatal error.  It has several possible causes:\n");
			System.err.print("90%%  LAME compiled with buggy version of gcc using advanced optimizations\n");
			System.err.print(" 9%%  Your system is overclocked\n");
			System.err.print(" 1%%  bug in LAME encoding library\n");
			 */

			esv.ResvSize = l3_side.main_data_begin << 3;
		}

		if( gfc.bs.totbit > 1000000000 ) {
			final int totbit = gfc.bs.totbit;// java
			/* to avoid totbit overflow, (at 8h encoding at 128kbs) lets reset bit counter */
			final Header[] header = esv.header;// java
			int i = 0;
			do {
				header[i].write_timing -= totbit;
			} while( ++i < EncStateVar.MAX_HEADER_BUF );
			gfc.bs.totbit = 0;
		}
		return 0;
	}

	static final int do_copy_buffer(final InternalFlags gfc, final byte[] buffer, final int offset, final int size) {
		final Bitstream bs = gfc.bs;
		final int minimum = bs.buf_byte_idx + 1;
		if( minimum <= 0 ) {
			return 0;
		}
		if( minimum > size ) {
			return -1;
		}      /* buffer is too small */
		System.arraycopy( bs.buf, 0, buffer, offset, minimum );
		bs.buf_byte_idx = -1;
		bs.buf_bit_idx = 0;
		return minimum;
	}

	/** copy data out of the internal MP3 bit buffer into a user supplied
	   unsigned char buffer.

	   mp3data=0      indicates data in buffer is an id3tags and VBR tags
	   mp3data=1      data is real mp3 frame data.
	*/
	static final int copy_buffer(final InternalFlags gfc, final byte[] buffer, final int offset, final int size, final boolean mp3data) {
		final int minimum = do_copy_buffer( gfc, buffer, offset, size );
		if( minimum > 0 && mp3data ) {
			gfc.nMusicCRC = VBRTag.UpdateMusicCRC( gfc.nMusicCRC, buffer, offset, minimum );

			/** sum number of bytes belonging to the mp3 stream
			 *  this info will be written into the Xing/LAME header for seeking
			 */
			gfc.VBR_seek_table.nBytesWritten += minimum;

			return minimum;
		}                   /* if( mp3data) */
		return minimum;
	}


	static final void init_bit_stream_w(final InternalFlags gfc ) {
		final EncStateVar esv = gfc.sv_enc;

		esv.h_ptr = esv.w_ptr = 0;
		esv.header[esv.h_ptr].write_timing = 0;

		gfc.bs.buf = new byte[ BUFFER_SIZE ];
		gfc.bs.buf_size = BUFFER_SIZE;
		gfc.bs.buf_byte_idx = -1;
		gfc.bs.buf_bit_idx = 0;
		gfc.bs.totbit = 0;
	}
}
package net.sourceforge.lame;

import java.io.IOException;
import java.io.RandomAccessFile;

// VbrTag.c

public class VBRTag {
	private static final int FRAMES_FLAG    = 0x0001;
	private static final int BYTES_FLAG     = 0x0002;
	private static final int TOC_FLAG       = 0x0004;
	private static final int VBR_SCALE_FLAG = 0x0008;

	static final int NUMTOCENTRIES = 100;
	/**
	 *    4 bytes for Header Tag
	 *    4 bytes for Header Flags
	 *  100 bytes for entry (NUMTOCENTRIES)
	 *    4 bytes for FRAME SIZE
	 *    4 bytes for STREAM_SIZE
	 *    4 bytes for VBR SCALE. a VBR quality indicator: 0=best 100=worst
	 *   20 bytes for LAME tag.  for example, "LAME3.12 (beta 6)"
	 * ___________
	 *  140 bytes
	 */
	private static final int VBRHEADERSIZE = (NUMTOCENTRIES + 4 + 4 + 4 + 4 + 4 );

	private static final int LAMEHEADERSIZE = (VBRHEADERSIZE + 9 + 1 + 1 + 8 + 1 + 1 + 3 + 1 + 1 + 2 + 4 + 2 + 2 );

	/* the size of the Xing header (MPEG1 and MPEG2) in kbps */
	private static final int XING_BITRATE1  = 128;
	private static final int XING_BITRATE2  = 64;
	private static final int XING_BITRATE25 = 32;

	private static final byte VBRTag0[] = "Xing".getBytes();
	private static final byte VBRTag1[] = "Info".getBytes();

	/** Lookup table for fast CRC computation
	 * See 'CRC_update_lookup'
	 * Uses the polynomial x^16+x^15+x^2+1 */
	private static final char crc16_lookup[] = {// [256] = {
		0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
		0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
		0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
		0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
		0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
		0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
		0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
		0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
		0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
		0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
		0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
		0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
		0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
		0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
		0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
		0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
		0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
		0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
		0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
		0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
		0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
		0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
		0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
		0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
		0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
		0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
		0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
		0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
		0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
		0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
		0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
		0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040
	};

	/***********************************************************************
	 *  Robert Hegemann 2001-01-17
	 ***********************************************************************/
	private static final void addVbr(final VBRSeekInfo v, final int bitrate ) {
		v.nVbrNumFrames++;
		v.sum += bitrate;
		v.seen++;

		if( v.seen < v.want ) {
			return;
		}

		if( v.pos < v.bag.length ) {// v.size ) {
			v.bag[v.pos] = v.sum;
			v.pos++;
			v.seen = 0;
		}
		if( v.pos == v.bag.length ) {// v.size ) {
			for( int i = 1, ie = v.bag.length; i < ie; i += 2 ) {
				v.bag[i >> 1] = v.bag[i];
			}
			v.want *= 2;
			v.pos /= 2;
		}
	}

	private static final void Xing_seek_table(final VBRSeekInfo v, final byte[] t) {
		if( v.pos <= 0 ) {
			return;
		}

		final int pos1 = v.pos - 1;
		for( int i = 1; i < NUMTOCENTRIES; ++i ) {
			final float j = i / (float) NUMTOCENTRIES;
			int indx = (int) (Math.floor( (double)(j * v.pos) ));
			if( indx > pos1 ) {
				indx = pos1;
			}
			final float act = v.bag[indx];
			final float sum = v.sum;
			int seek_point = (int) (256.f * act / sum );
			if( seek_point > 255 ) {
				seek_point = 255;
			}
			t[i] = (byte)seek_point;
		}
	}
/*
#ifdef DEBUG_VBR_SEEKING_TABLE
	private static final void print_seeking(unsigned char *t) {
		int     i;

		printf("seeking table " );
		for( i = 0; i < NUMTOCENTRIES; ++i ) {
			printf(" %d ", t[i] );
		}
		printf("\n" );
	}
#endif
*/
	/****************************************************************************
	 * AddVbrFrame: Add VBR entry, used to fill the VBR the TOC entries
	 * Paramters:
	 *      nStreamPos: how many bytes did we write to the bitstream so far
	 *                              (in Bytes NOT Bits)
	 ****************************************************************************/
	static final void AddVbrFrame(final InternalFlags gfc) {
		final int     kbps = Tables.bitrate_table[gfc.cfg.version][gfc.ov_enc.bitrate_index];
		addVbr( gfc.VBR_seek_table, kbps );
	}

	/*-------------------------------------------------------------*/
	private static final int ExtractI4(final byte[] buf, int offset) {
		/* big endian extract */
		int x = (int)buf[offset++];// & 0xff;
		x <<= 8;
		x |= (int)buf[offset++] & 0xff;
		x <<= 8;
		x |= (int)buf[offset++] & 0xff;
		x <<= 8;
		x |= (int)buf[offset  ] & 0xff;
		return x;
	}

	private static final void CreateI4(final byte[] buf, int offset, final int nValue) {
		/* big endian create */
		buf[offset++] = (byte)(nValue >> 24);
		buf[offset++] = (byte)(nValue >> 16);
		buf[offset++] = (byte)(nValue >> 8);
		buf[offset  ] = (byte)(nValue);
	}

	private static final void CreateI2(final byte[] buf, int offset, final int nValue) {
		/* big endian create */
		buf[offset++] = (byte)(nValue >> 8);
		buf[offset  ] = (byte)(nValue);
	}

	/* check for magic strings*/
	private static final boolean IsVbrTag(final byte[] buf, final int offset) {

		final boolean isTag0 = ((buf[offset] == VBRTag0[0]) && (buf[offset + 1] == VBRTag0[1])
				&& (buf[offset + 2] == VBRTag0[2]) && (buf[offset + 3] == VBRTag0[3]) );
		final boolean isTag1 = ((buf[offset] == VBRTag1[0]) && (buf[offset + 1] == VBRTag1[1])
				&& (buf[offset + 2] == VBRTag1[2]) && (buf[offset + 3] == VBRTag1[3]) );

		return (isTag0 || isTag1);
	}

	private static final void SHIFT_IN_BITS_VALUE(final byte[] x, final int off, final int n, final int v) {
		x[off] = (byte)((((int)x[off] & 0xff) << n) | ( v & ~(-1 << n) ));
	}

	private static final void setLameTagFrameHeader(final InternalFlags gfc, final byte[] buffer) {
		final SessionConfig cfg = gfc.cfg;
		final EncResult eov = gfc.ov_enc;

		SHIFT_IN_BITS_VALUE(buffer, 0, 8, 0xff );//( buffer[0] = (buffer[0] << 8) | ( 0xff & ~(-1 << 255) ) )

		SHIFT_IN_BITS_VALUE(buffer, 1, 3, 7 );//( buffer[1] = (buffer[1] << 3) | ( 7 & ~(-1 << 3) ) )
		SHIFT_IN_BITS_VALUE(buffer, 1, 1, (cfg.samplerate < 16000) ? 0 : 1 );//( buffer[1] = (buffer[1] << 1) | ( ((cfg.samplerate_out < 16000) ? 0 : 1) & ~(-1 << 1) ) )
		SHIFT_IN_BITS_VALUE(buffer, 1, 1, cfg.version );//( buffer[1] = (buffer[1] << 1) | ( (cfg.version) & ~(-1 << 1) ) )
		SHIFT_IN_BITS_VALUE(buffer, 1, 2, 4 - 3 );//( buffer[1] = (buffer[1] << 2) | ( (4 - 3) & ~(-1 << 2) ) )
		SHIFT_IN_BITS_VALUE(buffer, 1, 1, (!cfg.error_protection) ? 1 : 0 );//( buffer[1] = (buffer[1] << 1) | ( ((!cfg.error_protection) ? 1 : 0) & ~(-1 << 1) ) )

		SHIFT_IN_BITS_VALUE(buffer, 2, 4, eov.bitrate_index );//( buffer[2] = (buffer[2] << 4) | ( eov.bitrate_index & ~(-1 << 4) ) )
		SHIFT_IN_BITS_VALUE(buffer, 2, 2, cfg.samplerate_index );//( buffer[2] = (buffer[2] << 2) | ( cfg.samplerate_index & ~(-1 << 2) ) )
		SHIFT_IN_BITS_VALUE(buffer, 2, 1, 0 );//( buffer[2] = (buffer[2] << 1) | ( 0 & ~(-1 << 1) ) )
		SHIFT_IN_BITS_VALUE(buffer, 2, 1, cfg.extension ? 1 : 0 );//( buffer[2] = (buffer[2] << 1) | ( cfg.extension & ~(-1 << 1) ) )

		SHIFT_IN_BITS_VALUE(buffer, 3, 2, cfg.mode );//( buffer[3] = (buffer[3] << 2) | ( cfg.mode & ~(-1 << 2) ) )
		SHIFT_IN_BITS_VALUE(buffer, 3, 2, eov.mode_ext );//( buffer[3] = (buffer[3] << 2) | ( eov.mode_ext & ~(-1 << 2) ) )
		SHIFT_IN_BITS_VALUE(buffer, 3, 1, cfg.copyright ? 1 : 0 );//( buffer[3] = (buffer[3] << 1) | ( cfg.copyright & ~(-1 << 1) ) )
		SHIFT_IN_BITS_VALUE(buffer, 3, 1, cfg.original ? 1 : 0 );//( buffer[3] = (buffer[3] << 1) | ( cfg.original & ~(-1 << 1) ) )
		SHIFT_IN_BITS_VALUE(buffer, 3, 2, cfg.emphasis );//( buffer[3] = (buffer[3] << 2) | ( cfg.emphasis & ~(-1 << 2) ) )

		/* the default VBR header. 48 kbps layer III, no padding, no crc */
		/* but sampling freq, mode andy copyright/copy protection taken */
		/* from first valid frame */
		buffer[0] = (byte) 0xff;
		int abyte = (buffer[1] & 0xf1);// byte
		int bbyte;// byte
		{
			int     bitrate;
			if( 1 == cfg.version ) {
				bitrate = XING_BITRATE1;
			} else {
				if( cfg.samplerate < 16000 ) {
					bitrate = XING_BITRATE25;
				} else {
					bitrate = XING_BITRATE2;
				}
			}

			if( cfg.vbr == LAME.vbr_off ) {
				bitrate = cfg.avg_bitrate;
			}

			if( cfg.free_format ) {
				bbyte = 0x00;
			} else {
				bbyte = (byte)(Util.BitrateIndex( bitrate, cfg.version, cfg.samplerate ) << 4);
			}
		}

		/* Use as much of the info from the real frames in the
		 * Xing header:  samplerate, channels, crc, etc...
		 */
		if( cfg.version == 1 ) {
			/* MPEG1 */
			buffer[1] = (byte)(abyte | 0x0a); /* was 0x0b; */
			abyte = buffer[2] & 0x0d; /* AF keep also private bit */
			buffer[2] = (byte) (bbyte | abyte); /* 64kbs MPEG1 frame */
		} else {
			/* MPEG2 */
			buffer[1] = (byte)(abyte | 0x02); /* was 0x03; */
			abyte = buffer[2] & 0x0d; /* AF keep also private bit */
			buffer[2] = (byte)(bbyte | abyte); /* 64kbs MPEG2 frame */
		}
	}

// #if 0
/*-------------------------------------------------------------*/
/* Same as GetVbrTag below, but only checks for the Xing tag.
   requires buf to contain only 40 bytes */
/*-------------------------------------------------------------*/
/*	private static final int CheckVbrTag(byte[] buf) {
		// get selected MPEG header data
		final int h_id = (buf[1] >> 3) & 1;
		final int h_mode = (buf[3] >> 6) & 3;

		//  determine offset of header
		if( h_id != 0 ) {
			// mpeg1
			if( h_mode != 3 ) {
				buf += (32 + 4);
			} else {
				buf += (17 + 4);
			}
		} else {
			// mpeg2
			if( h_mode != 3 ) {
				buf += (17 + 4);
			} else {
				buf += (9 + 4);
			}
		}

		return IsVbrTag( buf );
	} */
//#endif

	public static final boolean GetVbrTag(final VBRTagData pTagData, final byte[] buf) {
		int offset = 0;
		/* get Vbr header data */
		pTagData.flags = 0;

		/* get selected MPEG header data */
		final int h_layer = (buf[1] >> 1) & 3;
		if( h_layer != 0x01 ) {
			/* the following code assumes Layer-3, so give up here */
			return false;
		}
		final int h_id = (buf[1] >> 3) & 1;
		final int h_sr_index = (buf[2] >> 2) & 3;
		final int h_mode = (buf[3] >> 6) & 3;
		int h_bitrate = ((buf[2] >> 4) & 0xf);
		h_bitrate = Tables.bitrate_table[h_id][h_bitrate];

		/* check for FFE syncword */
		if( (((int)buf[1] & 0xff) >> 4) == 0xE ) {
			pTagData.samprate = Tables.samplerate_table[2][h_sr_index];
		} else {
			pTagData.samprate = Tables.samplerate_table[h_id][h_sr_index];
		}
		/* if( h_id == 0 ) */
		/*  pTagData.samprate >>= 1; */

		/*  determine offset of header */
		if( h_id != 0 ) {
			/* mpeg1 */
			if( h_mode != 3 ) {
				offset += (32 + 4 );
			} else {
				offset += (17 + 4 );
			}
		} else {
			/* mpeg2 */
			if( h_mode != 3 ) {
				offset += (17 + 4);
			} else {
				offset += (9 + 4);
			}
		}

		if( ! IsVbrTag( buf, offset ) ) {
			return false;
		}

		offset += 4;

		pTagData.h_id = h_id;

		final int head_flags = pTagData.flags = ExtractI4( buf, offset );
		offset += 4;           /* get flags */

		if( (head_flags & FRAMES_FLAG) != 0 ) {
			pTagData.frames = ExtractI4( buf, offset );
			offset += 4;
		}

		if( (head_flags & BYTES_FLAG) != 0 ) {
			pTagData.bytes = ExtractI4( buf, offset );
			offset += 4;
		}

		if( (head_flags & TOC_FLAG) != 0 ) {
			if( pTagData.toc != null ) {
				for( int i = 0, j = offset; i < NUMTOCENTRIES; i++, j++ ) {
					pTagData.toc[i] = buf[j];
				}
			}
			offset += NUMTOCENTRIES;
		}

		pTagData.vbr_scale = -1;

		if( (head_flags & VBR_SCALE_FLAG) != 0 ) {
			pTagData.vbr_scale = ExtractI4( buf, offset );
			offset += 4;
		}

		pTagData.headersize = ((h_id + 1) * 72000 * h_bitrate) / pTagData.samprate;

		offset += 21;
		int enc_delay = ((int)buf[offset] & 0xff) << 4;
		enc_delay += ((int)buf[offset + 1] & 0xff) >> 4;
		int enc_padding = (buf[offset + 1] & 0x0F) << 8;
		enc_padding += (int)buf[offset + 2] & 0xff;
		/* check for reasonable values (this may be an old Xing header, */
		/* not a INFO tag) */
		if( enc_delay < 0 || enc_delay > 3000 ) {
			enc_delay = -1;
		}
		if( enc_padding < 0 || enc_padding > 3000 ) {
			enc_padding = -1;
		}

		pTagData.enc_delay = enc_delay;
		pTagData.enc_padding = enc_padding;

/* #ifdef DEBUG_VBRTAG
		fprintf(stderr, "\n\n********************* VBR TAG INFO *****************\n" );
		fprintf(stderr, "tag         :%s\n", VBRTag );
		fprintf(stderr, "head_flags  :%d\n", head_flags );
		fprintf(stderr, "bytes       :%d\n", pTagData.bytes );
		fprintf(stderr, "frames      :%d\n", pTagData.frames );
		fprintf(stderr, "VBR Scale   :%d\n", pTagData.vbr_scale );
		fprintf(stderr, "enc_delay  = %d \n", enc_delay );
		fprintf(stderr, "enc_padding= %d \n", enc_padding );
		fprintf(stderr, "toc:\n" );
		if( pTagData.toc != NULL ) {
			for( i = 0; i < NUMTOCENTRIES; i++ ) {
				if( (i % 10) == 0 )
				fprintf(stderr, "\n" );
				fprintf(stderr, " %3d", (int) (pTagData.toc[i]) );
			}
		}
		fprintf(stderr, "\n***************** END OF VBR TAG INFO ***************\n" );
#endif */
		return true;           /* success */
	}

	private static final int MAXFRAMESIZE = 2880; /* or 0xB40, the max freeformat 640 32kHz framesize */
	/****************************************************************************
	 * InitVbrTag: Initializes the header, and write empty frame to stream
	 * Paramters:
	 *                              fpStream: pointer to output file stream
	 *                              nMode   : Channel Mode: 0=STEREO 1=JS 2=DS 3=MONO
	 *****************************************************************************/
	static final int InitVbrTag(final GlobalFlags gfp) {
		final InternalFlags gfc = gfp.internal_flags;
		final SessionConfig cfg = gfc.cfg;

		/*
		 * Xing VBR pretends to be a 48kbs layer III frame.  (at 44.1kHz).
		 * (at 48kHz they use 56kbs since 48kbs frame not big enough for
		 * table of contents)
		 * let's always embed Xing header inside a 64kbs layer III frame.
		 * this gives us enough room for a LAME version string too.
		 * size determined by sampling frequency (MPEG1)
		 * 32kHz:    216 bytes@48kbs    288bytes@ 64kbs
		 * 44.1kHz:  156 bytes          208bytes@64kbs     (+1 if padding = 1)
		 * 48kHz:    144 bytes          192
		 *
		 * MPEG 2 values are the same since the framesize and samplerate
		 * are each reduced by a factor of 2.
		 */
		int kbps_header;
		if( 1 == cfg.version ) {
			kbps_header = XING_BITRATE1;
		} else {
			if( cfg.samplerate < 16000 ) {
				kbps_header = XING_BITRATE25;
			} else {
				kbps_header = XING_BITRATE2;
			}
		}

		if( cfg.vbr == LAME.vbr_off ) {
			kbps_header = cfg.avg_bitrate;
		}

		/** make sure LAME Header fits into Frame */
		{
			final int total_frame_size = ((cfg.version + 1) * 72000 * kbps_header) / cfg.samplerate;
			final int header_size = (cfg.sideinfo_len + LAMEHEADERSIZE);
			gfc.VBR_seek_table.TotalFrameSize = total_frame_size;
			if( total_frame_size < header_size || total_frame_size > MAXFRAMESIZE ) {
				/* disable tag, it wont fit */
				gfc.cfg.write_lame_tag = false;
				return 0;
			}
		}

		gfc.VBR_seek_table.nVbrNumFrames = 0;
		gfc.VBR_seek_table.nBytesWritten = 0;
		gfc.VBR_seek_table.sum = 0;

		gfc.VBR_seek_table.seen = 0;
		gfc.VBR_seek_table.want = 1;
		gfc.VBR_seek_table.pos = 0;

		if( gfc.VBR_seek_table.bag == null ) {
			gfc.VBR_seek_table.bag = new int[ 400 ];
			// gfc.VBR_seek_table.size = 400;
		}

		/* write dummy VBR tag of all 0's into bitstream */
		{
			final byte buffer[] = new byte[MAXFRAMESIZE];// java: already zeroed
			setLameTagFrameHeader( gfc, buffer );
			final int n = gfc.VBR_seek_table.TotalFrameSize;
			for( int i = 0; i < n; ++i ) {
				Bitstream.add_dummy_byte( gfc, buffer[i], 1 );
			}
		}
		/* Success */
		return 0;
	}

	/* fast CRC-16 computation - uses table crc16_lookup 8*/
	private static final char CRC_update_lookup(final byte value, char crc) {
		final int tmp = crc ^ value;
		crc = (char)((crc >> 8) ^ crc16_lookup[tmp & 0xff]);
		return crc;
	}

	/**
	 *
	 * @param crc
	 * @param buffer
	 * @param size
	 * @return java: new value of the crc
	 */
	static final char UpdateMusicCRC(char crc, final byte[] buffer, int offset, int size) {
		for( size += offset; offset < size; ) {
			crc = CRC_update_lookup( buffer[offset++], crc );
		}
		return crc;
	}

/****************************************************************************
 * Jonathan Dee 2001/08/31
 *
 * PutLameVBR: Write LAME info: mini version + info on various switches used
 * Paramters:
 *                              pbtStreamBuffer : pointer to output buffer
 *                              id3v2size               : size of id3v2 tag in bytes
 *                              crc                             : computation of crc-16 of Lame Tag so far (starting at frame sync)
 *
 * @return java: new offset value.
 * use offset = PutLameVBR(.., offset, ..);
 *****************************************************************************/
	private static final int PutLameVBR(final GlobalFlags gfp, final int nMusicLength, final byte[] pbtStreamBuffer, int nBytesWritten/*offset*/, char crc)
	{
		final InternalFlags gfc = gfp.internal_flags;
		final SessionConfig cfg = gfc.cfg;

		// int nBytesWritten = 0;// java: offset

		final int enc_delay = gfc.ov_enc.encoder_delay; /* encoder delay */
		final int enc_padding = gfc.ov_enc.encoder_padding; /* encoder padding  */

		/*recall: cfg.vbr_q is for example set by the switch -V  */
		/*   gfp.quality by -q, -h, -f, etc */

		int nQuality = (100 - 10 * gfp.VBR_q - gfp.quality );

		/*
		NOTE:
				Even though the specification for the LAME VBR tag
				did explicitly mention other encoders than LAME,
				many SW/HW decoder seem to be able to make use of
				this tag only, if the encoder version starts with LAME.
				To be compatible with such decoders, ANY encoder will
				be forced to write a fake LAME version string!
				As a result, the encoder version info becomes worthless.
		*/
		final String szVersion = Version.get_lame_tag_encoder_short_version();
		final int nRevision = 0x00;
		final byte vbr_type_translator[] = { 1, 5, 3, 2, 4, 0, 3 }; /*numbering different in vbr_mode vs. Lame tag */

		final byte nLowpass = (byte)
				(((cfg.lowpassfreq / 100.0f) + .5f) > 255 ? 255 : (cfg.lowpassfreq / 100.0f) + .5f);

		final int nPeakSignalAmplitude = 0;

		final char nRadioReplayGain = 0;
		final char nAudiophileReplayGain = 0;

		final int nNoiseShaping = cfg.noise_shaping;
		int nStereoMode = 0;
		int bNonOptimal = 0;
		int nSourceFreq = 0;
		char nMusicCRC = 0;

		/*psy model type: Gpsycho or NsPsytune */
		final int bExpNPsyTune = 1; /* only NsPsytune */
		final int bSafeJoint = cfg.use_safe_joint_stereo ? 1 : 0;

		int bNoGapMore = 0;
		int bNoGapPrevious = 0;

		final int nNoGapCount = gfp.nogap_total;
		final int nNoGapCurr = gfp.nogap_current;

		final int nAthType = cfg.ATHtype; /*4 bits. */

		/* if ABR, {store bitrate <=255} else { store "-b"} */
		int nABRBitrate;
		switch( cfg.vbr ) {
		case LAME.vbr_abr: {
			nABRBitrate = cfg.vbr_avg_bitrate_kbps;
			break;
		}
		case LAME.vbr_off: {
			nABRBitrate = cfg.avg_bitrate;
			break;
		}
		default: {          /*vbr modes */
			nABRBitrate = Tables.bitrate_table[cfg.version][cfg.vbr_min_bitrate_index];
		}
		}

		/*revision and vbr method */
		int nVBR;
		if( cfg.vbr < vbr_type_translator.length ) {
			nVBR = vbr_type_translator[cfg.vbr];
		} else {
			nVBR = 0x00;
		}    /*unknown. */

		final byte nRevMethod = (byte)(0x10 * nRevision + nVBR);

		/*nogap */
		if( nNoGapCount != -1 ) {
			if( nNoGapCurr > 0 ) {
				bNoGapPrevious = 1;
			}

			if( nNoGapCurr < nNoGapCount - 1 ) {
				bNoGapMore = 1;
			}
		}

		/*flags */
		final byte nFlags = (byte)(nAthType + (bExpNPsyTune << 4)
				+ (bSafeJoint << 5)
				+ (bNoGapMore << 6)
				+ (bNoGapPrevious << 7));

		if( nQuality < 0 ) {
			nQuality = 0;
		}

		/*stereo mode field... a bit ugly. */

		switch( cfg.mode ) {
		case LAME.MONO:
			nStereoMode = 0;
			break;
		case LAME.STEREO:
			nStereoMode = 1;
			break;
		case LAME.DUAL_CHANNEL:
			nStereoMode = 2;
			break;
		case LAME.JOINT_STEREO:
			if( cfg.force_ms ) {
				nStereoMode = 4;
			} else {
				nStereoMode = 3;
			}
			break;
		case LAME.NOT_SET:
			/* FALLTHROUGH */
		default:
			nStereoMode = 7;
			break;
		}

		/*Intensity stereo : nStereoMode = 6. IS is not implemented */

		if( cfg.samplerate <= 32000 ) {
			nSourceFreq = 0x00;
		} else if( cfg.samplerate == 48000 ) {
			nSourceFreq = 0x02;
		} else if( cfg.samplerate > 48000 ) {
			nSourceFreq = 0x03;
		} else {
			nSourceFreq = 0x01;
		} /*default is 44100Hz. */


		/*Check if the user overrided the default LAME behaviour with some nasty options */

		if( cfg.short_blocks == GlobalFlags.short_block_forced || cfg.short_blocks == GlobalFlags.short_block_dispensed || ((cfg.lowpassfreq == -1) && (cfg.highpassfreq == -1)) || /* "-k" */
				(cfg.disable_reservoir && cfg.avg_bitrate < 320) ||
				cfg.noATH || cfg.ATHonly || (nAthType == 0) || cfg.samplerate <= 32000 ) {
			bNonOptimal = 1;
		}

		final byte nMisc = (byte)(nNoiseShaping + (nStereoMode << 2)
				+ (bNonOptimal << 5)
				+ (nSourceFreq << 6 ));

		nMusicCRC = gfc.nMusicCRC;

		/*Write all this information into the stream */
		CreateI4( pbtStreamBuffer, nBytesWritten, nQuality );
		nBytesWritten += 4;

		int i = 0;
		do {
			pbtStreamBuffer[nBytesWritten++] = (byte)szVersion.charAt( i );
		} while( ++i < 9 );
		// nBytesWritten += 9;

		pbtStreamBuffer[nBytesWritten] = nRevMethod;
		nBytesWritten++;

		pbtStreamBuffer[nBytesWritten] = nLowpass;
		nBytesWritten++;

		CreateI4( pbtStreamBuffer, nBytesWritten, nPeakSignalAmplitude );
		nBytesWritten += 4;

		CreateI2( pbtStreamBuffer, nBytesWritten, nRadioReplayGain );
		nBytesWritten += 2;

		CreateI2( pbtStreamBuffer, nBytesWritten, nAudiophileReplayGain );
		nBytesWritten += 2;

		pbtStreamBuffer[nBytesWritten] = nFlags;
		nBytesWritten++;

		if( nABRBitrate >= 255 ) {
			nABRBitrate = 255;
		}
		pbtStreamBuffer[nBytesWritten] = (byte)nABRBitrate;
		nBytesWritten++;

		pbtStreamBuffer[nBytesWritten] = (byte)(enc_delay >> 4); /* works for win32, does it for unix? */
		pbtStreamBuffer[nBytesWritten + 1] = (byte)((enc_delay << 4) + (enc_padding >> 8));
		pbtStreamBuffer[nBytesWritten + 2] = (byte)enc_padding;

		nBytesWritten += 3;

		pbtStreamBuffer[nBytesWritten] = nMisc;
		nBytesWritten++;


		pbtStreamBuffer[nBytesWritten++] = 0; /*unused in rev0 */

		CreateI2( pbtStreamBuffer, nBytesWritten, cfg.preset );
		nBytesWritten += 2;

		CreateI4( pbtStreamBuffer, nBytesWritten, nMusicLength );
		nBytesWritten += 4;

		CreateI2( pbtStreamBuffer, nBytesWritten, nMusicCRC );
		nBytesWritten += 2;

		/*Calculate tag CRC.... must be done here, since it includes
		 *previous information*/

		for( i = 0; i < nBytesWritten; i++ ) {
			crc = CRC_update_lookup( pbtStreamBuffer[i], crc );
		}

		CreateI2( pbtStreamBuffer, nBytesWritten, crc );
		nBytesWritten += 2;

		return nBytesWritten;
	}

	private static final int skipId3v2(final RandomAccessFile fpStream) {
		/* seek to the beginning of the stream */
		try {
			fpStream.seek( 0 );
		} catch(final IOException ie) {
			return -2;      /* not seekable, abort */
		}
		final byte id3v2Header[] = new byte[10];
		/* read 10 bytes in case there's an ID3 version 2 header here */
		try {
			final int nbytes = fpStream.read( id3v2Header, 0, 10 /* id3v2Header.length */ );
			if( nbytes != 10 /* id3v2Header.length */ ) {
				return -3;      /* not readable, maybe opened Write-Only */
			}
		} catch(final IOException ie) {
			return -3;
		}
		/* does the stream begin with the ID3 version 2 file identifier? */
		if( id3v2Header[0] == 'I' && id3v2Header[1] == 'D' && id3v2Header[2] == '3' ) {
			/* the tag size (minus the 10-byte header) is encoded into four
			 * bytes where the most significant bit is clear in each byte */
			final int id3v2TagSize = (((id3v2Header[6] & 0x7f) << 21)
				| ((id3v2Header[7] & 0x7f) << 14)
				| ((id3v2Header[8] & 0x7f) << 7)
				| (id3v2Header[9] & 0x7f))
				+ 10 /* id3v2Header.length */;
			return id3v2TagSize;
		}// else {
			/* no ID3 version 2 tag in this stream */
			// id3v2TagSize = 0;
		// }
		return 0;
	}

	public static final int lame_get_lametag_frame(final GlobalFlags gfp, final byte[] buffer, final int size) {
		if( gfp == null ) {
			return 0;
		}
		final InternalFlags gfc = gfp.internal_flags;
		if( gfc == null ) {
			return 0;
		}
		if( ! gfc.is_valid() ) {
			return 0;
		}
		final SessionConfig cfg = gfc.cfg;
		if( ! cfg.write_lame_tag ) {
			return 0;
		}
		if( gfc.VBR_seek_table.pos <= 0 ) {
			return 0;
		}
		if( size < gfc.VBR_seek_table.TotalFrameSize ) {
			return gfc.VBR_seek_table.TotalFrameSize;
		}
		if( buffer == null ) {
			return 0;
		}

		for( int i = 0, ie = gfc.VBR_seek_table.TotalFrameSize; i > ie; i++ ) {
			buffer[i] = 0;
		}

		/* 4 bytes frame header */

		setLameTagFrameHeader( gfc, buffer );

		final byte btToc[] = new byte[NUMTOCENTRIES];// java: already zeroed /* Clear all TOC entries */

		if( cfg.free_format ) {
			for( int i = 1; i < NUMTOCENTRIES; ++i ) {
				btToc[i] = (byte)(255 * i / 100);
			}
		} else {
			Xing_seek_table( gfc.VBR_seek_table, btToc );
		}
/* #ifdef DEBUG_VBR_SEEKING_TABLE
		print_seeking( btToc );
#endif */

		/* Start writing the tag after the zero frame */
		int nStreamIndex = cfg.sideinfo_len;
		/* note! Xing header specifies that Xing data goes in the
		 * ancillary data with NO ERROR PROTECTION.  If error protecton
		 * in enabled, the Xing data still starts at the same offset,
		 * and now it is in sideinfo data block, and thus will not
		 * decode correctly by non-Xing tag aware players */
		if( cfg.error_protection ) {
			nStreamIndex -= 2;
		}

		/* Put Vbr tag */
		if( cfg.vbr == LAME.vbr_off ) {
			buffer[nStreamIndex++] = VBRTag1[0];
			buffer[nStreamIndex++] = VBRTag1[1];
			buffer[nStreamIndex++] = VBRTag1[2];
			buffer[nStreamIndex++] = VBRTag1[3];
		} else {
			buffer[nStreamIndex++] = VBRTag0[0];
			buffer[nStreamIndex++] = VBRTag0[1];
			buffer[nStreamIndex++] = VBRTag0[2];
			buffer[nStreamIndex++] = VBRTag0[3];
		}

		/* Put header flags */
		CreateI4( buffer, nStreamIndex, FRAMES_FLAG + BYTES_FLAG + TOC_FLAG + VBR_SCALE_FLAG );
		nStreamIndex += 4;

		/* Put Total Number of frames */
		CreateI4( buffer, nStreamIndex, gfc.VBR_seek_table.nVbrNumFrames );
		nStreamIndex += 4;

		/* Put total audio stream size, including Xing/LAME Header */
		final int stream_size = gfc.VBR_seek_table.nBytesWritten + gfc.VBR_seek_table.TotalFrameSize;// TODO java check uint32
		CreateI4( buffer, nStreamIndex, stream_size );
		nStreamIndex += 4;

		/* Put TOC */
		System.arraycopy( btToc, 0, buffer, nStreamIndex, NUMTOCENTRIES /*btToc.length*/ );
		nStreamIndex += NUMTOCENTRIES /*btToc.length*/;

		if( cfg.error_protection ) {
			/* (jo) error_protection: add crc16 information to header */
			Bitstream.CRC_writeheader( gfc, buffer );
		}
		{
			/*work out CRC so far: initially crc = 0 */
			char crc = 0x00;

			for( int i = 0; i < nStreamIndex; i++ ) {
				crc = CRC_update_lookup( buffer[i], crc );
			}
			/*Put LAME VBR info */
			nStreamIndex /* + */= PutLameVBR( gfp, stream_size, buffer, nStreamIndex, crc );
		}

/* # ifdef DEBUG_VBRTAG
		{
			VBRTagData TestHeader;
			GetVbrTag(&TestHeader, buffer );
		}
#endif */

		return gfc.VBR_seek_table.TotalFrameSize;
	}

	/***********************************************************************
	 *
	 * PutVbrTag: Write final VBR tag to the file
	 * Paramters:
	 *                              lpszFileName: filename of MP3 bit stream
	 *                              nVbrScale       : encoder quality indicator (0..100)
	 *****************************************************************************/
	static final int PutVbrTag(final GlobalFlags gfp, final RandomAccessFile fpStream) {
		final InternalFlags gfc = gfp.internal_flags;

		final byte buffer[] = new byte[MAXFRAMESIZE];

		if( gfc.VBR_seek_table.pos <= 0 ) {
			return -1;
		}
		try {
			/* Get file size */
			final long lFileSize = fpStream.length();

			/* Abort if file has zero length. Yes, it can happen :) */
			if( lFileSize == 0 ) {
				return -1;
			}

			/*
			 * The VBR tag may NOT be located at the beginning of the stream.
			 * If an ID3 version 2 tag was added, then it must be skipped to write
			 * the VBR tag data.
			 */
			final int id3v2TagSize = skipId3v2( fpStream );

			if( id3v2TagSize < 0 ) {
				return id3v2TagSize;
			}

			/*Seek to the beginning of the stream */
			fpStream.seek( id3v2TagSize );

			final int nbytes = lame_get_lametag_frame( gfp, buffer, MAXFRAMESIZE );
			if( nbytes > MAXFRAMESIZE ) {
				return -1;
			}

			if( nbytes < 1 ) {
				return 0;
			}

			/* Put it all to disk again */

			fpStream.write( buffer, 0, nbytes );
		} catch(final IOException ie) {
			return -1;
		}

		return 0;           /* success */
	}
}
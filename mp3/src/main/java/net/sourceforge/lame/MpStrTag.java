package net.sourceforge.lame;

import java.util.Arrays;

/** java: wordbuf[ wordpointer ] */
public class MpStrTag {
	private static final class Buf {
		private byte[] pnt;
		private int size;
		private int pos;
		private Buf next;
	}
	//
	private static final Synth sDecoder = new Decode();// short[] version;
	private static final Synth sDecoderUnclipped = new DecodeUnclipped();// float[] version;
	//
	public static final int tabsel_123[][][] = {// [2] [3] [16] = {
			{ {0,32,64,96,128,160,192,224,256,288,320,352,384,416,448,},
			{0,32,48,56, 64, 80, 96,112,128,160,192,224,256,320,384,},
			{0,32,40,48, 56, 64, 80, 96,112,128,160,192,224,256,320,} },

			{ {0,32,48,56,64,80,96,112,128,144,160,176,192,224,256,},
			{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,},
			{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,} }
		};

	public static final int freqs[] = {// [9] = {
		44100, 48000, 32000,
		22050, 24000, 16000,
		11025, 12000,  8000 };

	static final float muls[][] = new float[27][64];
	//
	/** buffer linked list pointers, tail points to oldest buffer */
	Buf head, tail;
	/** 1 if valid Xing vbr header detected */
	boolean vbr_header;
	/** set if vbr header present */
	public int     num_frames;
	/** set if vbr header present */
	public int     enc_delay;
	/** set if vbr header present */
	public int     enc_padding;
	/* header_parsed, side_parsed and data_parsed must be all set 1
	   before the full frame has been parsed */
	/** 1 = header of current frame has been parsed */
	public boolean header_parsed;
	/** 1 = header of sideinfo of current frame has been parsed */
	boolean side_parsed;
	boolean data_parsed;
	/** 1 = free format frame */
	boolean free_format;
	/** 1 = last frame was free format */
	boolean old_free_format;
	int     bsize;
	public int framesize;
	/* number of bytes used for side information, including 2 bytes for CRC-16 if present */
	int     ssize;
	int     dsize;
	/** size of previous frame, -1 for first */
	public int     fsizeold;
	int     fsizeold_nopadding;
	/** holds the parameters decoded from the header */
	public final Frame fr = new Frame();
	final MpgSideInfo sideinfo = new MpgSideInfo();
	/** bit stream space used ???? */ /* MAXFRAMESIZE */
	final byte bsspace[][] = new byte[2][Mpg123.MAXFRAMESIZE + 1024];
	final float hybrid_block[][][] = new float[2][2][Mpg123.SBLIMIT * Mpg123.SSLIMIT];
	final int hybrid_blc[] = new int[2];
	int header;
	int  bsnum;
	final float synth_buffs[][][] = new float[2][2][0x110];
	int     synth_bo;
	/** 1 = bitstream is yet to be synchronized */
	boolean sync_bitstream;

	int     bitindex;
	/** java: wordbuf[ wordpointer ] */
	byte[] wordbuf;
	/** java: wordbuf[ wordpointer ] */
	int wordpointer;

	/*
	public PrintStream report_msg;
	public PrintStream report_dbg;
	public PrintStream report_err;
	*/
	//
	public final void InitMP3() {
		Layer1.hip_init_tables_layer1();
		Layer2.hip_init_tables_layer2();
		Layer3.hip_init_tables_layer3();

		// if( mp != null ) {
		// java: operations to replace memset 0
		this.fsizeold_nopadding = 0;
		this.fr.clear();
		this.sideinfo.clear();
		Arrays.fill( this.bsspace[0], (byte)0 );
		Arrays.fill( this.bsspace[1], (byte)0 );
		Arrays.fill( this.hybrid_block[0][0], 0 );
		Arrays.fill( this.hybrid_block[0][1], 0 );
		Arrays.fill( this.hybrid_block[1][0], 0 );
		Arrays.fill( this.hybrid_block[1][1], 0 );
		this.hybrid_blc[0] = 0;
		this.hybrid_blc[1] = 0;
		this.header = 0;
		Arrays.fill( this.synth_buffs[0][0], 0 );
		Arrays.fill( this.synth_buffs[0][1], 0 );
		Arrays.fill( this.synth_buffs[1][0], 0 );
		Arrays.fill( this.synth_buffs[1][1], 0 );
		//
		this.framesize = 0;
		this.num_frames = 0;
		this.enc_delay = -1;
		this.enc_padding = -1;
		this.vbr_header = false;
		this.header_parsed = false;
		this.side_parsed = false;
		this.data_parsed = false;
		this.free_format = false;
		this.old_free_format = false;
		this.ssize = 0;
		this.dsize = 0;
		this.fsizeold = -1;
		this.bsize = 0;
		this.head = this.tail = null;
		this.fr.single = -1;
		this.bsnum = 0;
		this.wordbuf = this.bsspace[this.bsnum];
		this.wordpointer = 512;
		this.bitindex = 0;
		this.synth_bo = 1;
		this.sync_bitstream = true;

		// }
		DCT64.make_decode_tables( 32767 );
	}

	public final void ExitMP3() {
		// if( mp != null ) {
		Buf b = this.tail;
		while( b != null  ) {
			b.pnt = null;
			final Buf bn = b.next;
			b = bn;
		}
		// }
	}

	private final Buf addbuf(final byte[] buf, final int size) {
		final Buf nbuf = new Buf();
		nbuf.pnt = new byte[ size ];
		nbuf.size = size;
		System.arraycopy( buf, 0, nbuf.pnt, 0, size );
		nbuf.next = null;
		nbuf.pos = 0;

		if( null == this.tail ) {
			this.tail = nbuf;
		} else {
			this.head.next = nbuf;
		}

		this.head = nbuf;
		this.bsize += size;

		return nbuf;
	}

	/** added remove_buf to support mpglib seeking */
	public final void remove_buf() {
		final Buf buf = this.tail;

		this.tail = buf.next;
		if( this.tail == null ) {
			this.head = null;
		}

		buf.pnt = null;
	}

	private final int read_buf_byte() {
		int pos = this.tail.pos;
		while( pos >= this.tail.size ) {
			remove_buf();
			if( null == this.tail ) {
				//System.err.print("hip: Fatal error! tried to read past mp buffer\n");
				//System.exit( 1 );
				//return 0;
				throw new IllegalStateException("tried to read past mp buffer");
			}
			pos = this.tail.pos;
		}

		final int b = (int)this.tail.pnt[pos] & 0xff;
		this.bsize--;
		this.tail.pos++;

		return b;
	}

	private final void read_head() {
		int val = read_buf_byte();
		val <<= 8;
		val |= read_buf_byte();
		val <<= 8;
		val |= read_buf_byte();
		val <<= 8;
		val |= read_buf_byte();

		this.header = val;
	}

	private final void copy_mp(final int size, final byte[] ptr, final int poffset) {
		int len = 0;

		while( len < size && this.tail != null ) {
			int nlen;
			final int blen = this.tail.size - this.tail.pos;
			if( (size - len) <= blen ) {
				nlen = size - len;
			} else {
				nlen = blen;
			}
			System.arraycopy( this.tail.pnt, this.tail.pos, ptr, poffset + len, nlen );
			len += nlen;
			this.tail.pos += nlen;
			this.bsize -= nlen;
			if( this.tail.pos == this.tail.size ) {
				remove_buf();
			}
		}
	}

	/** number of bytes needed by GetVbrTag to parse header */
	private static final int XING_HEADER_SIZE = 194;

	/*
	traverse mp data structure without changing it
	(just like sync_buffer)
	pull out Xing bytes
	call vbr header check code from LAME
	if we find a header, parse it and also compute the VBR header size
	if no header, do nothing.

	bytes = number of bytes before MPEG header.  skip this many bytes
	before starting to read
	return value: number of bytes in VBR header, including syncword
	*/
	private final int check_vbr_header(final int bytes) {
		Buf buf = this.tail;
		final byte xing[] = new byte[XING_HEADER_SIZE];
		final VBRTagData pTagData = new VBRTagData();

		int pos = buf.pos;
		/* skip to valid header */
		for( int i = 0; i < bytes; ++i ) {
			while( pos >= buf.size ) {
				buf = buf.next;
				if( null == buf ) {
					return -1;
				} /* fatal error */
				pos = buf.pos;
			}
			++pos;
		}
		/* now read header */
		for( int i = 0; i < XING_HEADER_SIZE; ++i ) {
			while( pos >= buf.size ) {
				buf = buf.next;
				if( null == buf ) {
					return -1;
				} /* fatal error */
				pos = buf.pos;
			}
			xing[i] = buf.pnt[pos];
			++pos;
		}

		/* check first bytes for Xing header */
		this.vbr_header = VBRTag.GetVbrTag( pTagData, xing );
		if( this.vbr_header ) {
			this.num_frames = pTagData.frames;
			this.enc_delay = pTagData.enc_delay;
			this.enc_padding = pTagData.enc_padding;

			/* Util.lame_report_fnc(this.report_msg,"hip: delays: %d %d \n",this.enc_delay,this.enc_padding); */
			/* Util.lame_report_fnc(this.report_msg,"hip: Xing VBR header dectected.  MP3 file has %d frames\n", pTagData.frames); */
			if( pTagData.headersize < 1 ) {
				return 1;
			}
			return pTagData.headersize;
		}
		return 0;
	}

	private static final boolean head_check(final int head, final int check_layer) {
		/*
		   look for a valid header.
		   if check_layer > 0, then require that
		   nLayer = check_layer.
		 */

		/* bits 13-14 = layer 3 */
		final int nLayer = 4 - ((head >>> 17) & 3);

		if( (head & 0xffe00000) != 0xffe00000 ) {
			/* syncword */
			return false;
		}

		if( nLayer == 4 ) {
			return false;
		}

		if( check_layer > 0 && nLayer != check_layer ) {
			return false;
		}

		if( ((head >>> 12) & 0xf) == 0xf ) {
			/* bits 16,17,18,19 = 1111  invalid bitrate */
			return false;
		}
		if( ((head >>> 10) & 0x3) == 0x3 ) {
			/* bits 20,21 = 11  invalid sampling freq */
			return false;
		}
		if( (head & 0x3) == 0x2 ) {
			/* invalid emphasis */
			return false;
		}
		return true;
	}

	@SuppressWarnings("unused")
	private final int sync_buffer(final boolean free_match) {
		/* traverse mp structure without modifying pointers, looking
		 * for a frame valid header.
		 * if free_format, valid header must also have the same
		 * samplerate.
		 * return number of bytes in mp, before the header
		 * return -1 if header is not found
		 */
		final int b[] = { 0, 0, 0, 0 };
		Buf buf = this.tail;
		if( null == buf ) {
			return -1;
		}

		int pos = buf.pos;
		for( int i = 0; i < this.bsize; i++ ) {
			/* get 4 bytes */

			b[0] = b[1];
			b[1] = b[2];
			b[2] = b[3];
			while( pos >= buf.size ) {
				buf = buf.next;
				if( null == buf ) {// java: incorrect eclipse warining: "Dead code"
					return -1;
					/* not enough data to read 4 bytes */
				}
				pos = buf.pos;
			}
			b[3] = (int)buf.pnt[pos] & 0xff;
			++pos;

			if( i >= 3 ) {
				final Frame frame = this.fr;

				int val = b[0];
				val <<= 8;
				val |= b[1];
				val <<= 8;
				val |= b[2];
				val <<= 8;
				val |= b[3];
				boolean h = head_check( val, frame.lay );

				if( h && free_match ) {
					boolean mpeg25;
					int lsf;
					if( (val & (1 << 20)) != 0 ) {
						lsf = (val & (1 << 19)) != 0 ? 0x0 : 0x1;
						mpeg25 = false;
					} else {
						lsf = 1;
						mpeg25 = true;
					}

					final int mode = ((val >> 6) & 0x3);
					final int stereo = (mode == Mpg123.MPG_MD_MONO) ? 1 : 2;
					/* just to be even more thorough, match the sample rate */
					int sampling_frequency;
					if( mpeg25 ) {
						sampling_frequency = 6 + ((val >> 10) & 0x3);
					} else {
						sampling_frequency = ((val >> 10) & 0x3) + (lsf * 3);
					}
					h = ((stereo == frame.stereo) && (lsf == frame.lsf) && (mpeg25 == frame.mpeg25) &&
							(sampling_frequency == frame.sampling_frequency));
				}

				if( h ) {
					return i - 3;
				}
			}
		}
		return -1;
	}

	/** Resets decoding. Aids seeking. */
	public final void decode_reset() {
/*#if 0
		remove_buf();
		// start looking for next frame
		// this.fsizeold = this.framesize;
		this.fsizeold = -1;
		this.old_free_format = this.free_format;
		this.framesize = 0;
		this.header_parsed = 0;
		this.side_parsed = 0;
		this.data_parsed = 0;
		this.sync_bitstream = 1; // TODO check if this is right
#else */
		InitMP3();        /* Less error prone to just to reinitialise. */
// #endif
	}

	/** added audiodata_precedesframes to return the number of bitstream frames the audio data will precede the
	 * current frame by for Layer 3 data. Aids seeking.
	 */
	public final int audiodata_precedesframes() {
		if( this.fr.lay == 3 ) {
			return Layer3.layer3_audiodata_precedesframes( this );
		}// else {
			return 0;       /* For Layer 1 & 2 the audio data starts at the frame that describes it, so no audio data precedes. */
		//}
	}

	final int getbits(final int number_of_bits) {
		final byte[] buf = this.wordbuf;// java
		if( number_of_bits <= 0 || null == buf ) {// ! this.wordpointer ) {
			return 0;
		}

		int p = this.wordpointer;
		int rval = buf[ p++ ];
		rval <<= 8;
		rval |= (int)buf[ p++ ] & 0xff;
		rval <<= 8;
		rval |= (int)buf[ p ] & 0xff;
		rval <<= this.bitindex;
		rval &= 0xffffff;

		this.bitindex += number_of_bits;

		rval >>= (24 - number_of_bits);

		this.wordpointer += (this.bitindex >> 3);
		this.bitindex &= 7;
		return rval;
	}

	@SuppressWarnings("boxing")
	private final int decodeMP3_clipchoice(final byte[] in, final int isize, final Object out, final int[] done,
		final Synth synth)
	{
		if( in != null && isize != 0 && addbuf( in, isize ) == null ) {
			return Mpg123.MP3_ERR;
		}

		/* First decode header */
		if( ! this.header_parsed ) {
			int bytes;
			if( this.fsizeold == -1 || this.sync_bitstream ) {
				this.sync_bitstream = false;

				/* This is the very first call.   sync with anything */
				/* bytes= number of bytes before header */
				bytes = sync_buffer( false );

				/* now look for Xing VBR header */
				if( this.bsize < bytes + XING_HEADER_SIZE ) {
					/* not enough data to look for Xing header */
					return Mpg123.MP3_NEED_MORE;
				}
				/* vbrbytes = number of bytes in entire vbr header */
				final int vbrbytes = check_vbr_header( bytes ) + bytes;

				if( this.vbr_header ) {
					/* do we have enough data to parse entire Xing header? */
					if( vbrbytes > this.bsize ) {
						/* Util.lame_report_fnc(this.report_err,"hip: not enough data to parse entire Xing header\n"); */
						return Mpg123.MP3_NEED_MORE;
					}

					/* read in Xing header.  Buffer data in case it
					 * is used by a non zero main_data_begin for the next
					 * frame, but otherwise dont decode Xing header */
					for( int i = 0; i < vbrbytes; ++i ) {
						read_buf_byte();
					}
					/* now we need to find another syncword */
					/* just return and make user send in more data */

					return Mpg123.MP3_NEED_MORE;
				}
			} else {
				/* match channels, samplerate, etc, when syncing */
				bytes = sync_buffer( true );
			}

			/* buffer now synchronized */
			if( bytes < 0 ) {
				/* Util.lame_report_fnc(this.report_err,"hip: need more bytes %d\n", bytes); */
				return Mpg123.MP3_NEED_MORE;
			}
			if( bytes > 0 ) {
				/* there were some extra bytes in front of header.
				 * bitstream problem, but we are now resynced
				 * should try to buffer previous data in case new
				 * frame has nonzero main_data_begin, but we need
				 * to make sure we do not overflow buffer
				 */
				if( this.fsizeold != -1 ) {
					//System.err.printf("hip: bitstream problem, resyncing skipping %d bytes...\n", bytes );
				}
				this.old_free_format = false;
// #if 1
				/* FIXME: correct ??? */
				this.sync_bitstream = true;
// #endif
				/* skip some bytes, buffer the rest */
				int size = this.wordpointer - 512;

				if( size > Mpg123.MAXFRAMESIZE ) {
					/* wordpointer buffer is trashed.  probably cant recover, but try anyway */
					//System.err.printf("hip: wordpointer trashed.  size=%d (%d)  bytes=%d \n",
					//					size, Mpg123.MAXFRAMESIZE, bytes );
					size = 0;
					this.wordbuf = this.bsspace[ this.bsnum ];
					this.wordpointer = 512;
				}

				/* buffer contains 'size' data right now
				we want to add 'bytes' worth of data, but do not
				exceed MAXFRAMESIZE, so we through away 'i' bytes */
				int i = (size + bytes) - Mpg123.MAXFRAMESIZE;
				for( ; i > 0; --i ) {
					--bytes;
					read_buf_byte();
				}

				copy_mp( bytes, this.wordbuf, this.wordpointer );
				this.fsizeold += bytes;
			}

			read_head();
			if( ! this.fr.decode_header( this, this.header ) ) {
				return Mpg123.MP3_ERR;
			}
			this.header_parsed = true;
			this.framesize = this.fr.framesize;
			this.free_format = (this.framesize == 0);

			if( this.fr.lsf != 0 ) {
				this.ssize = (this.fr.stereo == 1) ? 9 : 17;
			} else {
				this.ssize = (this.fr.stereo == 1) ? 17 : 32;
			}
			if( this.fr.error_protection ) {
				this.ssize += 2;
			}

			this.bsnum = 1 - this.bsnum; /* toggle buffer */
			this.wordbuf = this.bsspace[this.bsnum];
			this.wordpointer = 512;
			this.bitindex = 0;

			/* for very first header, never parse rest of data */
			if( this.fsizeold == -1 ) {
				return Mpg123.MP3_NEED_MORE;
			}
		}                   /* end of header parsing block */

		/* now decode side information */
		if( ! this.side_parsed ) {

			/* Layer 3 only */
			if( this.fr.lay == 3 ) {
				if( this.bsize < this.ssize ) {
					return Mpg123.MP3_NEED_MORE;
				}

				copy_mp( this.ssize, this.wordbuf, this.wordpointer );

				if( this.fr.error_protection ) {
					getbits( 16 );
				}
				int bits = Layer3.decode_layer3_sideinfo( this );
				/* bits = actual number of bits needed to parse this frame */
				/* can be negative, if all bits needed are in the reservoir */
				if( bits < 0 ) {
					bits = 0;
				}

				/* read just as many bytes as necessary before decoding */
				this.dsize = (bits + 7) >> 3;

				if( ! this.free_format ) {
					/* do not read more than framsize data */
					final int frame_size = this.fr.framesize - this.ssize;
					if( this.dsize > frame_size ) {
						//System.err.printf(
						//		"hip: error audio data exceeds framesize by %d bytes\n",
						//		this.dsize - frame_size );
						this.dsize = frame_size;
					}
				}
				/* this will force mpglib to read entire frame before decoding */
				/* this.dsize = this.framesize - this.ssize; */

			} else {
				/* Layers 1 and 2 */

				/* check if there is enough input data */
				if( this.fr.framesize > this.bsize ) {
					return Mpg123.MP3_NEED_MORE;
				}

				/* takes care that the right amount of data is copied into wordpointer */
				this.dsize = this.fr.framesize;
				this.ssize = 0;
			}

			this.side_parsed = true;
		}

		/* now decode main data */
		int iret = Mpg123.MP3_NEED_MORE;
		if( ! this.data_parsed ) {
			if( this.dsize > this.bsize ) {
				return Mpg123.MP3_NEED_MORE;
			}

			copy_mp( this.dsize, this.wordbuf, this.wordpointer );

			done[0] = 0;

			/*do_layer3(&mp.fr,(unsigned char *) out,done); */
			switch( this.fr.lay ) {
			case 1:
				if( this.fr.error_protection ) {
					getbits( 16 );
				}

				//Layer1.decode_layer1_frame( this, out, done );// FIXME incorrect calling
				if( Layer1.decode_layer1_frame( this, out, done, synth ) < 0 ) {
					return Mpg123.MP3_ERR;
				}
				break;

			case 2:
				if( this.fr.error_protection ) {
					getbits( 16 );
				}

				//Layer2.decode_layer2_frame( this, out, done );// FIXME incorrect calling
				Layer2.decode_layer2_frame( this, out, done, synth );
				break;

			case 3:
				Layer3.decode_layer3_frame( this, out, done, synth );
				break;
			default:
				//System.err.printf("hip: invalid layer %d\n", this.fr.lay );
			}

			this.wordbuf = this.bsspace[ this.bsnum ];
			this.wordpointer = 512 + this.ssize + this.dsize;

			this.data_parsed = true;
			iret = Mpg123.MP3_OK;
		}


		/* remaining bits are ancillary data, or reservoir for next frame
		 * If free format, scan stream looking for next frame to determine
		 * this.framesize */
		if( this.free_format ) {
			if( this.old_free_format ) {
				/* free format.  bitrate must not vary */
				this.framesize = this.fsizeold_nopadding + (this.fr.padding);
			} else {
				final int bytes = sync_buffer( true );
				if( bytes < 0 ) {
					return iret;
				}
				this.framesize = bytes + this.ssize + this.dsize;
				this.fsizeold_nopadding = this.framesize - this.fr.padding;
/* #if 0
				Util.lame_report_fnc(this.report_dbg,"hip: freeformat bitstream:  estimated bitrate=%dkbs  \n",
						8*(4+this.framesize)*freqs[this.fr.sampling_frequency]/
						(1000*576*(2-this.fr.lsf)));
#endif */
			}
		}

		/* buffer the ancillary data and reservoir for next frame */
		int bytes = this.framesize - (this.ssize + this.dsize);
		if( bytes > this.bsize ) {
			return iret;
		}

		if( bytes > 0 ) {
// #if 1
			/* FIXME: while loop OK ??? */
			while( bytes > 512 ) {
				read_buf_byte();
				bytes--;
				this.framesize--;
			}
// #endif
			copy_mp( bytes, this.wordbuf, this.wordpointer );
			this.wordpointer += bytes;

			final int size = this.wordpointer - 512;
			if( size > Mpg123.MAXFRAMESIZE ) {
				//System.err.print("hip: fatal error.  MAXFRAMESIZE not large enough.\n");
			}
		}

		/* the above frame is completely parsed.  start looking for next frame */
		this.fsizeold = this.framesize;
		this.old_free_format = this.free_format;
		this.framesize = 0;
		this.header_parsed = false;
		this.side_parsed = false;
		this.data_parsed = false;

		return iret;
	}

	@SuppressWarnings("boxing")
	public final int decodeMP3(final byte[] in, final int isize, final short[] out, final int osize, final int[] done) {
		if( osize < (1152 * 2) ) {
			//System.err.printf("hip: Insufficient memory for decoding buffer %d\n", osize );
			return Mpg123.MP3_ERR;
		}

		// passing pointers to the functions which clip the samples
		return decodeMP3_clipchoice( in, isize, out, done, sDecoder );// synth_1to1_mono, synth_1to1 );
	}

	/** added decodeMP3_unclipped to support returning raw floating-point values of samples. The representation
	 * of the floating-point numbers is defined in mpg123.h as #define real. It is 64-bit double by default.
	 * No more than 1152 samples per channel are allowed. */
	public final int decodeMP3_unclipped(final byte[] in, final int isize, final float[] out, final int osize, final int[] done) {
		// we forbid input with more than 1152 samples per channel for output in unclipped mode
		if( osize < (1152 * 2) ) {
			//System.err.print("hip: out space too small for unclipped mode\n");
			return Mpg123.MP3_ERR;
		}

		// passing pointers to the functions which don't clip the samples
		return decodeMP3_clipchoice( in, isize, out, done, sDecoderUnclipped );// synth_1to1_mono_unclipped, synth_1to1_unclipped );
	}

	final int getbits_fast(final int number_of_bits) {
		final byte[] buf = this.wordbuf;// java
		int p = this.wordpointer;// java
		int rval = (int)buf[ p++ ];
		rval <<= 8;
		rval |= (int)buf[ p ] & 0xff;
		rval <<= this.bitindex;
		rval &= 0xffff;
		this.bitindex += number_of_bits;

		rval >>= (16 - number_of_bits);

		this.wordpointer += (this.bitindex >> 3);
		this.bitindex &= 7;
		return rval;
	}

	final byte get_leq_8_bits(final int number_of_bits) {
		return (byte) getbits_fast( number_of_bits );
	}

	final char get_leq_16_bits(final int number_of_bits) {
		return (char) getbits_fast( number_of_bits );
	}

	@SuppressWarnings("boxing")
	final int set_pointer(final int backstep) {
		if( this.fsizeold < 0 && backstep > 0 ) {
			//System.err.printf("hip: Can't step back %d bytes!\n", backstep );
			return Mpg123.MP3_ERR;
		}
		this.wordpointer -= backstep;
		if( backstep != 0 ) {
			final byte[] bsbufold = this.bsspace[1 - this.bsnum];// + 512;
			System.arraycopy( bsbufold, 512 + this.fsizeold - backstep, this.wordbuf, this.wordpointer, backstep );
		}
		this.bitindex = 0;
		return Mpg123.MP3_OK;
	}
}

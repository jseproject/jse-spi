package org.xiph.flac;

/** FLAC frame header structure.  (c.f. <A HREF="../format.html#frame_header">format specification</A>)
 */
public class FrameHeader {
	/** The number of samples per subframe. */
	public int blocksize = 0;

	/** The sample rate in Hz. */
	public int sample_rate = 0;

	/** The number of channels (== number of subframes). */
	public int channels = 0;

	/** The channel assignment for the frame. */
	public int channel_assignment = 0;

	/** The sample resolution. */
	public int bits_per_sample = 0;

	/** The numbering scheme used for the frame.  As a convenience, the
	 * decoder will always convert a frame number to a sample number because
	 * the rules are complex. */
	boolean number_type = false;// /** An enumeration of the possible frame numbering methods. */
	/*typedef enum {
		FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER, /**< number contains the frame number */
	/*	FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER /**< number contains the sample number of first sample in frame */
	/*} FLAC__FrameNumberType;*/

	/** The frame number or sample number of first sample in frame;
	 * use the \a number_type value to determine which to use. */
	/*union {
		FLAC__uint32 frame_number;
		FLAC__uint64 sample_number;
	} number;*/
	int frame_number = 0;
	public long sample_number = 0;// FIXME why long? in examples uses as integer and as index for pcm array!

	/** CRC-8 (polynomial = x^8 + x^2 + x^1 + x^0, initialized with 0)
	 * of the raw frame header bytes, meaning everything before the CRC byte
	 * including the sync code.
	 */
	byte crc = 0;

	// stream_encoder_framing.c
	final boolean add_header(final BitWriter bw)
	{
		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(bw));

		if( ! bw.write_raw_uint32( Format.FLAC__FRAME_HEADER_SYNC, Format.FLAC__FRAME_HEADER_SYNC_LEN ) ) {
			return false;
		}

		if( ! bw.write_raw_uint32( 0, Format.FLAC__FRAME_HEADER_RESERVED_LEN ) ) {
			return false;
		}

		if( ! bw.write_raw_uint32( (this.number_type == Format.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER) ? 0 : 1, Format.FLAC__FRAME_HEADER_BLOCKING_STRATEGY_LEN ) ) {
			return false;
		}

		//FLAC__ASSERT(header->blocksize > 0 && header->blocksize <= FLAC__MAX_BLOCK_SIZE);
		/* when this assertion holds true, any legal blocksize can be expressed in the frame header */
		//FLAC__ASSERT(FLAC__MAX_BLOCK_SIZE <= 65535u);
		int blocksize_hint = 0;
		int u;
		switch( this.blocksize ) {
			case   192: u = 1; break;
			case   576: u = 2; break;
			case  1152: u = 3; break;
			case  2304: u = 4; break;
			case  4608: u = 5; break;
			case   256: u = 8; break;
			case   512: u = 9; break;
			case  1024: u = 10; break;
			case  2048: u = 11; break;
			case  4096: u = 12; break;
			case  8192: u = 13; break;
			case 16384: u = 14; break;
			case 32768: u = 15; break;
			default:
				if( this.blocksize <= 0x100 ) {
					blocksize_hint = u = 6;
				} else {
					blocksize_hint = u = 7;
				}
				break;
		}
		if( ! bw.write_raw_uint32( u, Format.FLAC__FRAME_HEADER_BLOCK_SIZE_LEN ) ) {
			return false;
		}

		//FLAC__ASSERT(FLAC__format_sample_rate_is_valid(header->sample_rate));
		int sample_rate_hint = 0;
		switch( this.sample_rate ) {
			case  88200: u = 1; break;
			case 176400: u = 2; break;
			case 192000: u = 3; break;
			case   8000: u = 4; break;
			case  16000: u = 5; break;
			case  22050: u = 6; break;
			case  24000: u = 7; break;
			case  32000: u = 8; break;
			case  44100: u = 9; break;
			case  48000: u = 10; break;
			case  96000: u = 11; break;
			default:
				if( this.sample_rate <= 255000 && this.sample_rate % 1000 == 0 ) {
					sample_rate_hint = u = 12;
				} else if( this.sample_rate <= 655350 && this.sample_rate % 10 == 0 ) {
					sample_rate_hint = u = 14;
				} else if( this.sample_rate <= 0xffff ) {
					sample_rate_hint = u = 13;
				} else {
					u = 0;
				}
				break;
		}
		if( ! bw.write_raw_uint32( u, Format.FLAC__FRAME_HEADER_SAMPLE_RATE_LEN ) ) {
			return false;
		}

		//FLAC__ASSERT(header->channels > 0 && header->channels <= (1u << Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN) && header->channels <= Format.FLAC__MAX_CHANNELS);
		switch( this.channel_assignment ) {
			case Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
				u = this.channels - 1;
				break;
			case Format.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
				//FLAC__ASSERT(header->channels == 2);
				u = 8;
				break;
			case Format.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
				//FLAC__ASSERT(header->channels == 2);
				u = 9;
				break;
			case Format.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
				//FLAC__ASSERT(header->channels == 2);
				u = 10;
				break;
			default:
				//FLAC__ASSERT(0);
		}
		if( ! bw.write_raw_uint32( u, Format.FLAC__FRAME_HEADER_CHANNEL_ASSIGNMENT_LEN ) ) {
			return false;
		}

		//FLAC__ASSERT(header->bits_per_sample > 0 && header->bits_per_sample <= (1u << FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN));
		switch( this.bits_per_sample ) {
			case 8 : u = 1; break;
			case 12: u = 2; break;
			case 16: u = 4; break;
			case 20: u = 5; break;
			case 24: u = 6; break;
			case 32: u = 7; break;
			default: u = 0; break;
		}
		if( ! bw.write_raw_uint32( u, Format.FLAC__FRAME_HEADER_BITS_PER_SAMPLE_LEN ) ) {
			return false;
		}

		if( ! bw.write_raw_uint32( 0, Format.FLAC__FRAME_HEADER_ZERO_PAD_LEN ) ) {
			return false;
		}

		if( this.number_type == Format.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER ) {
			if( ! bw.write_utf8_uint32( this./*number.*/frame_number ) ) {
				return false;
			}
		}
		else {
			if( ! bw.write_utf8_uint64( this./*number.*/sample_number ) ) {
				return false;
			}
		}

		if( blocksize_hint != 0 ) {
			if( ! bw.write_raw_uint32( this.blocksize - 1, (blocksize_hint == 6) ? 8 : 16 ) ) {
				return false;
			}
		}

		switch( sample_rate_hint ) {
			case 12:
				if( ! bw.write_raw_uint32( this.sample_rate / 1000, 8 ) ) {
					return false;
				}
				break;
			case 13:
				if( ! bw.write_raw_uint32( this.sample_rate, 16 ) ) {
					return false;
				}
				break;
			case 14:
				if( ! bw.write_raw_uint32( this.sample_rate / 10, 16 ) ) {
					return false;
				}
				break;
		}

		/* write the CRC */
		try {
			final int crc8 = bw.get_write_crc8();

			if( ! bw.write_raw_uint32( crc8, Format.FLAC__FRAME_HEADER_CRC_LEN ) ) {
				return false;
			}
		} catch(final OutOfMemoryError e) {
			return false;
		}

		return true;
	}
}

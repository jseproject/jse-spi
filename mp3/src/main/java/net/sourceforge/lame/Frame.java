package net.sourceforge.lame;

public class Frame {
	public int stereo;
	/** single channel (monophonic) */
	int     single;
	/** 0 = MPEG-1, 1 = MPEG-2/2.5 */
	public int lsf;
	/** 1 = MPEG-2.5, 0 = MPEG-1/2 */
	boolean mpeg25;
	int     header_change;
	/** Layer */
	public int     lay;
	/** 1 = CRC-16 code following header */
	boolean error_protection;
	public int     bitrate_index;
	/** sample rate of decompressed audio in Hz */
	public int     sampling_frequency;
	int    padding;
	boolean extension;
	public int mode;
	public int mode_ext;
	boolean copyright;
	boolean original;
	int     emphasis;
	/** computed framesize */
	int     framesize;

	/* AF: ADDED FOR LAYER1/LAYER2 */
	int     II_sblimit;
	ALTable2[] alloc;
	int     down_sample_sblimit;
	int     down_sample;
	//
	final void clear() {
		stereo = 0;
		single = 0;
		lsf = 0;
		mpeg25 = false;
		header_change = 0;
		lay = 0;
		error_protection = false;
		bitrate_index = 0;
		sampling_frequency = 0;
		padding = 0;
		extension = false;
		mode = 0;
		mode_ext = 0;
		copyright = false;
		original = false;
		emphasis = 0;
		framesize = 0;

		II_sblimit = 0;
		alloc = null;
		down_sample_sblimit = 0;
		down_sample = 0;
	}
	private static final int MAX_INPUT_FRAMESIZE = 4096;
	/**
	 * decode a header and write the information
	 * into the frame structure
	 */
	@SuppressWarnings("boxing")
	final boolean decode_header(final MpStrTag mp, final int newhead) {

		if( (newhead & (1 << 20)) != 0 ) {
			this.lsf = (newhead & (1 << 19)) != 0 ? 0x0 : 0x1;
			this.mpeg25 = false;
		} else {
			this.lsf = 1;
			this.mpeg25 = true;
		}

		this.lay = 4 - ((newhead >>> 17) & 3);

		if( this.lay != 3 && this.mpeg25 ) {
			//System.err.printf("MPEG-2.5 is supported by Layer3 only\n");
			return false;
		}
		if( ((newhead >>> 10) & 0x3) == 0x3 ) {
			//System.err.printf("Stream error\n");
			return false;
		}
		if( this.mpeg25 ) {
			this.sampling_frequency = 6 + ((newhead >>> 10) & 0x3);
		} else {
			this.sampling_frequency = ((newhead >>> 10) & 0x3) + (this.lsf * 3);
		}

		this.error_protection = ((newhead >>> 16) & 0x1) == 0;//(((newhead >>> 16) & 0x1) ^ 0x1) != 0;

		if( this.mpeg25 ) {
			this.bitrate_index = ((newhead >>> 12) & 0xf);
		}

		this.bitrate_index = ((newhead >>> 12) & 0xf);
		this.padding = ((newhead >>> 9) & 0x1);
		this.extension = ((newhead >>> 8) & 0x1) != 0;
		this.mode = ((newhead >>> 6) & 0x3);
		this.mode_ext = ((newhead >>> 4) & 0x3);
		this.copyright = ((newhead >>> 3) & 0x1) != 0;
		this.original = ((newhead >>> 2) & 0x1) != 0;
		this.emphasis = newhead & 0x3;

		this.stereo = (this.mode == Mpg123.MPG_MD_MONO) ? 1 : 2;

		switch( this.lay ) {
		case 1:
			this.framesize = MpStrTag.tabsel_123[this.lsf][0][this.bitrate_index] * 12000;
			this.framesize /= MpStrTag.freqs[this.sampling_frequency];
			this.framesize = ((this.framesize + this.padding) << 2) - 4;
			this.down_sample = 0;
			this.down_sample_sblimit = Mpg123.SBLIMIT >> (this.down_sample);
			break;

		case 2:
			this.framesize = MpStrTag.tabsel_123[this.lsf][1][this.bitrate_index] * 144000;
			this.framesize /= MpStrTag.freqs[this.sampling_frequency];
			this.framesize += this.padding - 4;
			this.down_sample = 0;
			this.down_sample_sblimit = Mpg123.SBLIMIT >> (this.down_sample);
			break;

		case 3:
/* #if 0
			this.do_layer = do_layer3;
			if( this.lsf ) {
				ssize = (this.stereo == 1) ? 9 : 17;
			} else {
				ssize = (this.stereo == 1) ? 17 : 32;
			}
#endif */

/* #if 0
			if( this.error_protection ) {
				ssize += 2;
			}
#endif */
			if( this.framesize > MAX_INPUT_FRAMESIZE ) {
				//System.err.printf("Frame size too big.\n");
				this.framesize = MAX_INPUT_FRAMESIZE;
				return false;
			}

			if( this.bitrate_index == 0 ) {
				this.framesize = 0;
			} else {
				this.framesize = MpStrTag.tabsel_123[this.lsf][2][this.bitrate_index] * 144000;
				this.framesize /= MpStrTag.freqs[this.sampling_frequency] << (this.lsf);
				this.framesize = this.framesize + this.padding - 4;
			}
			break;
		default:
			//System.err.printf("Sorry, layer %d not supported\n", this.lay );
			return false;
		}
		/*    print_header(mp, fr); */
		return true;
	}
}

package net.sourceforge.lame;

/***********************************************************************
*
*  Control Parameters set by User.  These parameters are here for
*  backwards compatibility with the old, non-shared lib API.
*  Please use the lame_set_variablename() functions below
*
*
***********************************************************************/
public class GlobalFlags {// struct lame_global_struct
	//
	// typedef enum short_block_e {
	/** allow LAME to decide */
	static final int short_block_not_set = -1;
	/** LAME may use them, even different block types for L/R */
	static final int short_block_allowed = 0;
	/** LAME may use them, but always same block types in L/R */
	static final int short_block_coupled = 1;
	/** LAME will not use short blocks, long blocks only */
	static final int short_block_dispensed = 2;
	/** LAME will not use long blocks, short blocks only */
	static final int short_block_forced = 3;
	// } short_block_t;

	int class_id;

	/** input description */
	/** number of samples. default=2^32-1 */
	long    num_samples;
	/** input number of channels. default=2 */
	int     num_channels;
	/** samp_rate in Hz. default=44.1 kHz */
	int     samplerate;
	/** scale input by this amount before encoding at least not used for MP3 decoding */
	float   scale;

	/* general control params */
	/** add Xing VBR tag? */
	boolean write_lame_tag;
	/** use lame/mpglib to convert mp3 to wav */
	boolean decode_only;
	/** quality setting 0=best,  9=worst  default=5 */
	int     quality;
	/** see enum in JMPEG_mode. default = LAME picks best value */
	int /* MPEG_mode */ mode;
	/** force M/S mode.  requires mode=1 */
	boolean force_ms;
	/** use free format? default=0 */
	boolean free_format;
	/** 1 (default) writes ID3 tags, 0 not */
	boolean write_id3tag_automatic;

	int     nogap_total;
	int     nogap_current;

	int     substep_shaping;
	int     noise_shaping;
	/**  0 = no, 1 = yes */
	int     subblock_gain;// -1 don't set
	/** 0 = no.  1=outside loop  2=inside loop(slow) */
	int     use_best_huffman;

	/*
	 * set either brate>0  or compression_ratio>0, LAME will compute
	 * the value of the variable not set.
	 * Default is compression_ratio = 11.025
	 */
	/** bitrate */
	int     brate;
	/** sizeof(wav file)/sizeof(mp3 file) */
	float   compression_ratio;

	/* frame params */
	/** mark as copyright. default=0 */
	boolean copyright;
	/** mark as original. default=1 */
	boolean original;
	/** the MP3 'private extension' bit. Meaningless */
	boolean extension;
	/** Input PCM is emphased PCM (for instance from one of the rarely emphased CDs),
	 * it is STRONGLY not recommended to use this,
	 * because psycho does not take it into account,
	 * and last but not least many decoders don't care about these bits */
	int emphasis;
	/** use 2 bytes per frame for a CRC checksum. default=0 */
	boolean error_protection;
	/** enforce ISO spec as much as possible */
	int     strict_ISO;

	/** use bit reservoir? */
	boolean disable_reservoir;

	/* quantization/noise shaping */
	int     quant_comp;
	int     quant_comp_short;
	boolean experimentalY;
	boolean experimentalZ;
	int     exp_nspsytune;

	int     preset;

	/* VBR control */
	int /* vbr_mode */ VBR;
	float   VBR_q_frac;      /* Range [0,...,1[ */
	int     VBR_q;           /* Range [0,...,9] */
	int     VBR_mean_bitrate_kbps;
	int     VBR_min_bitrate_kbps;
	int     VBR_max_bitrate_kbps;
	/** strictly enforce VBR_min_bitrate normaly,
	 * it will be violated for analog silence */
	boolean VBR_hard_min;

	/* resampling and filtering */
	/** freq in Hz. 0=lame choses. -1=no filter */
	int     lowpassfreq;
	/** freq in Hz. 0=lame choses. -1=no filter */
	int     highpassfreq;
	/** freq width of filter, in Hz (default=15%) */
	int     lowpasswidth;
	/** freq width of filter, in Hz (default=15%) */
	int     highpasswidth;

	/*
	 * psycho acoustics and other arguments which you should not change
	 * unless you know what you are doing
	 */
	float   maskingadjust;
	float   maskingadjust_short;
	/** only use ATH                         */
	boolean ATHonly;
	/** only use ATH for short blocks        */
	boolean ATHshort;
	/** disable ATH                          */
	boolean noATH;
	/** select ATH formula                   */
	int     ATHtype;
	/** change ATH formula 4 shape           */
	float   ATHcurve;
	/** lower ATH by this many db            */
	float   ATH_lower_db;
	/** select ATH auto-adjust scheme        */
	int     athaa_type;
	/** dB, tune active region of auto-level */
	float   athaa_sensitivity;
	int     short_blocks;
	/** use temporal masking effect          */
	boolean useTemporal;
	float   interChRatio;
	/** Naoki's adjustment of Mid/Side maskings */
	float   msfix;// experimental

	/** attack threshold for L/R/M channel */
	float   attackthre;
	/** attack threshold for S channel */
	float   attackthre_s;

	/************************************************************************/
	/* internal variables, do not set...                                    */
	/* provided because they may be of use to calling application           */
	/************************************************************************/
	/** is this struct owned by calling program or lame? */
	boolean lame_allocated_gfp;

	/**************************************************************************/
	/* more internal variables are stored in this structure:                  */
	/**************************************************************************/
	InternalFlags internal_flags;

	boolean is_valid() {
		/*if( gfp == null ) {
			return false;
		}*/
		return this.class_id == InternalFlags.LAME_ID;
	}
	/** number of samples
	 * it's unlikely for this function to return an error */
	public final int set_num_samples(final long n_samples) {
		if( is_valid() ) {
			/* default = 2^32-1 */
			this.num_samples = n_samples;
			return 0;
		}
		return -1;
	}

	public final long get_num_samples() {
		if( is_valid() ) {
			return this.num_samples;
		}
		return 0;
	}

	/** number of channels in input stream */
	public final boolean set_num_channels(final int n_channels) {
		if( is_valid() ) {
			/* default = 2 */
			if( 2 < n_channels || 0 >= n_channels ) {
				return true;  /* we don't support more than 2 channels */
			}
			this.num_channels = n_channels;
			return false;
		}
		return true;
	}

	public final int get_num_channels() {
		if( is_valid() ) {
			return this.num_channels;
		}
		return 0;
	}

	/** scale the input by this amount before encoding (not used for decoding) */
	public final int set_scale(final float fscale) {
		if( is_valid() ) {
			/* default = 1 */
			this.scale = fscale;
			return 0;
		}
		return -1;
	}

	public final float get_scale() {
		if( is_valid() ) {
			return this.scale;
		}
		return 0;
	}

	/** sample rate in Hz */
	public final int set_out_samplerate(final int sample_rate) {
		if( is_valid() ) {
			/*
			 * default = 0: LAME picks best value based on the amount
			 *              of compression
			 * MPEG only allows:
			 *  MPEG1    32, 44.1,   48khz
			 *  MPEG2    16, 22.05,  24
			 *  MPEG2.5   8, 11.025, 12
			 *
			 * (not used by decoding routines)
			 */
			if( sample_rate != 0 ) {
				if( Util.SmpFrqIndex( sample_rate ) < 0 ) {
					return -1;
				}
			}
			this.samplerate = sample_rate;
			return 0;
		}
		return -1;
	}

	public final int get_out_samplerate() {
		if( is_valid() ) {
			return this.samplerate;
		}
		return 0;
	}

	/*
	 * general control parameters
	 */

	/** write a Xing VBR header frame */
	public final int set_bWriteVbrTag(final boolean bWriteVbrTag) {
		if( is_valid() ) {
			/* default = 1 (on) for VBR/ABR modes, 0 (off) for CBR mode */
			this.write_lame_tag = bWriteVbrTag;
			return 0;
		}
		return -1;
	}

	public final boolean get_bWriteVbrTag() {
		if( is_valid() ) {
			return this.write_lame_tag;
		}
		return false;
	}

	/** decode only, use lame/mpglib to convert mp3 to wav */
	public final int set_decode_only(final boolean is_decode_only) {
		if( is_valid() ) {
			/* default = 0 (disabled) */

			this.decode_only = is_decode_only;
			return 0;
		}
		return -1;
	}

	public final boolean get_decode_only() {
		if( is_valid() ) {
			return this.decode_only;
		}
		return false;
	}


	/*
	 * Internal algorithm selection.
	 * True quality is determined by the bitrate but this variable will effect
	 * quality by selecting expensive or cheap algorithms.
	 * quality=0..9.  0=best (very slow).  9=worst.
	 * recommended:  3     near-best quality, not too slow
	 *               5     good quality, fast
	 *               7     ok quality, really fast
	 */
	public final int set_quality(final int q) {
		if( is_valid() ) {
			if( q < 0 ) {
				this.quality = 0;
			} else if( q > 9 ) {
				this.quality = 9;
			} else {
				this.quality = q;
			}
			return 0;
		}
		return -1;
	}

	public final int get_quality() {
		if( is_valid() ) {
			return this.quality;
		}
		return 0;
	}

	/** mode = STEREO, JOINT_STEREO, DUAL_CHANNEL (not supported), MONO */
	public final int set_mode(final int /* MPEG_mode */ mpeg_mode) {
		if( is_valid() ) {
			final int mpg_mode = mpeg_mode;
			/* default: lame chooses based on compression ratio and input channels */
			if( mpg_mode < 0 || LAME.MAX_INDICATOR <= mpg_mode ) {
				return -1;  /* Unknown MPEG mode! */
			}
			this.mode = mpeg_mode;
			return 0;
		}
		return -1;
	}

	public final int /* MPEG_mode */ get_mode() {
	    if( is_valid() ) {
	        return this.mode;
	    }
	    return LAME.NOT_SET;
	}

	/**
	 * Force M/S for all frames.  For testing only.
	 * Requires mode = 1.
	 */
	public final int set_force_ms(final boolean is_force_ms) {
		if( is_valid() ) {
			/* default = 0 (disabled) */

			this.force_ms = is_force_ms;
			return 0;
		}
		return -1;
	}

	public final boolean get_force_ms() {
		if( is_valid() ) {
			return this.force_ms;
		}
		return false;
	}

	/** Use free_format. */
	public final int set_free_format(final boolean is_free_format) {
		if( is_valid() ) {
			/* default = 0 (disabled) */

			this.free_format = is_free_format;
			return 0;
		}
		return -1;
	}

	public final boolean get_free_format() {
		if( is_valid() ) {
			return this.free_format;
		}
		return false;
	}

	/* set and get some gapless encoding flags */

	public final int set_nogap_total(final int the_nogap_total) {
		if( is_valid() ) {
			this.nogap_total = the_nogap_total;
			return 0;
		}
		return -1;
	}

	public final int get_nogap_total() {
		if( is_valid() ) {
			return this.nogap_total;
		}
		return 0;
	}

	public final int set_nogap_currentindex(final int the_nogap_index) {
		if( is_valid() ) {
			this.nogap_current = the_nogap_index;
			return 0;
		}
		return -1;
	}

	public final int get_nogap_currentindex() {
		if( is_valid() ) {
			return this.nogap_current;
		}
		return 0;
	}

	/**
	 * Set one of
	 *  - brate
	 *  - compression ratio.
	 *
	 * Default is compression ratio of 11.
	 */
	public final int set_brate(final int bitrate) {
		if( is_valid() ) {
			this.brate = bitrate;
			if( bitrate > 320 ) {
				this.disable_reservoir = true;
			}
			return 0;
		}
		return -1;
	}

	public final int get_brate() {
		if( is_valid() ) {
			return this.brate;
		}
		return 0;
	}

	public final int set_compression_ratio(final float ratio) {
		if( is_valid() ) {
			this.compression_ratio = ratio;
			return 0;
		}
		return -1;
	}

	public final float get_compression_ratio() {
		if( is_valid() ) {
			return this.compression_ratio;
		}
		return 0;
	}

	/*
	 * frame parameters
	 */

	/* Mark as copyright protected. */
	public final int set_copyright(final boolean is_copyright) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			this.copyright = is_copyright;
			return 0;
		}
		return -1;
	}

	public final boolean get_copyright() {
		if( is_valid() ) {
			return this.copyright;
		}
		return false;
	}

	/** Mark as original. */
	public final int set_original(final boolean is_original) {
		if( is_valid() ) {
			/* default = 1 (enabled) */
			this.original = is_original;
			return 0;
		}
		return -1;
	}

	public final boolean get_original() {
		if( is_valid() ) {
			return this.original;
		}
		return false;
	}

	/*
	 * error_protection.
	 * Use 2 bytes from each frame for CRC checksum.
	 */
	public final int set_error_protection(final boolean is_error_protection) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			this.error_protection = is_error_protection;
			return 0;
		}
		return -1;
	}

	public final boolean get_error_protection() {
		if( is_valid() ) {
			return this.error_protection;
		}
		return false;
	}


	/** MP3 'private extension' bit. Meaningless. */
	public final int set_extension(final boolean is_extension) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			this.extension = is_extension;
			return 0;
		}
		return -1;
	}

	public final boolean get_extension() {
		if( is_valid() ) {
			return this.extension;
		}
		return false;
	}

	/** Enforce strict ISO compliance. */
	public final int set_strict_ISO(final int val) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			/* enforce disable/enable meaning, if we need more than two values
			   we need to switch to an enum to have an apropriate representation
			   of the possible meanings of the value */
			if( val < LAME.MDB_DEFAULT || LAME.MDB_MAXIMUM < val ) {
				return -1;
			}
			this.strict_ISO = val;
			return 0;
		}
		return -1;
	}

	public final int get_strict_ISO() {
		if( is_valid() ) {
			return this.strict_ISO;
		}
		return 0;
	}

	/********************************************************************
	 * quantization/noise shaping
	 ***********************************************************************/

	/** Disable the bit reservoir. For testing only. */
	public final int set_disable_reservoir(final boolean is_disable_reservoir) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			this.disable_reservoir = is_disable_reservoir;
			return 0;
		}
		return -1;
	}

	public final boolean get_disable_reservoir() {
		if( is_valid() ) {
			return this.disable_reservoir;
		}
		return false;
	}

	public final int set_experimentalX(final int experimentalX) {
		if( is_valid() ) {
			set_quant_comp( experimentalX );
			set_quant_comp_short( experimentalX );
			return 0;
		}
		return -1;
	}

	public final int get_experimentalX() {
		return get_quant_comp();
	}

	/** Select a different "best quantization" function. default = 0 */
	public final int set_quant_comp(final int quant_type) {
		if( is_valid() ) {
			this.quant_comp = quant_type;
			return 0;
		}
		return -1;
	}

	public final int get_quant_comp() {
		if( is_valid() ) {
			return this.quant_comp;
		}
		return 0;
	}

	/** Select a different "best quantization" function. default = 0 */
	public final int set_quant_comp_short(final int quant_type) {
		if( is_valid() ) {
			this.quant_comp_short = quant_type;
			return 0;
		}
		return -1;
	}

	public final int get_quant_comp_short() {
		if( is_valid() ) {
			return this.quant_comp_short;
		}
		return 0;
	}


	/** Another experimental option. For testing only. */
	public final int set_experimentalY(final boolean is_experimentalY) {
		if( is_valid() ) {
			this.experimentalY = is_experimentalY;
			return 0;
		}
		return -1;
	}

	public final boolean get_experimentalY() {
		if( is_valid() ) {
			return this.experimentalY;
		}
		return false;
	}

	public final int set_experimentalZ(final boolean is_experimentalZ) {
		if( is_valid() ) {
			this.experimentalZ = is_experimentalZ;
			return 0;
		}
		return -1;
	}

	public final boolean get_experimentalZ() {
		if( is_valid() ) {
			return this.experimentalZ;
		}
		return false;
	}

	/** Naoki's psycho acoustic model. */
	public final int set_exp_nspsytune(final int nspsytune) {
		if( is_valid() ) {
			// default = 0 (disabled)
			this.exp_nspsytune = nspsytune;
			return 0;
		}
		return -1;
	}

	public final int get_exp_nspsytune() {
		if( is_valid() ) {
			return this.exp_nspsytune;
		}
		return 0;
	}

	/********************************************************************
	 * VBR control
	 ***********************************************************************/

	/* Types of VBR.  default = vbr_off = CBR */
	public final int set_VBR(final int /* vbr_mode */ vbr_mode) {
		if( is_valid() ) {
			final int vbr_q = vbr_mode;
			if( 0 > vbr_q || LAME.vbr_max_indicator <= vbr_q ) {
				return -1;  /* Unknown VBR mode! */
			}
			this.VBR = vbr_mode;
			return 0;
		}
		return -1;
	}

	public final int /* vbr_mode */ get_VBR() {
		if( is_valid() ) {
			return this.VBR;
		}
		return LAME.vbr_off;
	}

	/**
	 * VBR quality level.
	 *  0 = highest
	 *  9 = lowest
	 */
	public final int set_VBR_q(int vbr_quality) {
		if( is_valid() ) {
			int ret = 0;

			if( 0 > vbr_quality ) {
				ret = -1;   /* Unknown VBR quality level! */
				vbr_quality = 0;
			}
			if( 9 < vbr_quality ) {
				ret = -1;
				vbr_quality = 9;
			}
			this.VBR_q = vbr_quality;
			this.VBR_q_frac = 0;
			return ret;
		}
		return -1;
	}

	public final int get_VBR_q() {
		if( is_valid() ) {
			return this.VBR_q;
		}
		return 0;
	}

	public final int set_VBR_quality(float vbr_quality) {
		if( is_valid() ) {
			int ret = 0;

			if( 0 > vbr_quality ) {
				ret = -1;   /* Unknown VBR quality level! */
				vbr_quality = 0;
			}
			if( 9.999 < vbr_quality ) {
				ret = -1;
				vbr_quality = 9.999f;
			}

			this.VBR_q = (int) vbr_quality;
			this.VBR_q_frac = vbr_quality - this.VBR_q;

			return ret;
		}
		return -1;
	}

	public final float get_VBR_quality() {
		if( is_valid() ) {
			return this.VBR_q + this.VBR_q_frac;
		}
		return 0;
	}

	/** Ignored except for VBR = vbr_abr (ABR mode) */
	public final int set_VBR_mean_bitrate_kbps(final int vbr_mean_bitrate_kbps) {
		if( is_valid() ) {
			this.VBR_mean_bitrate_kbps = vbr_mean_bitrate_kbps;
		return 0;
		}
		return -1;
	}

	public final int get_VBR_mean_bitrate_kbps() {
		if( is_valid() ) {
			return this.VBR_mean_bitrate_kbps;
		}
		return 0;
	}

	public final int set_VBR_min_bitrate_kbps(final int vbr_min_bitrate_kbps) {
		if( is_valid() ) {
			this.VBR_min_bitrate_kbps = vbr_min_bitrate_kbps;
			return 0;
		}
		return -1;
	}

	public final int get_VBR_min_bitrate_kbps() {
		if( is_valid() ) {
			return this.VBR_min_bitrate_kbps;
		}
		return 0;
	}

	public final int set_VBR_max_bitrate_kbps(final int vbr_max_bitrate_kbps) {
		if( is_valid() ) {
			this.VBR_max_bitrate_kbps = vbr_max_bitrate_kbps;
			return 0;
		}
		return -1;
	}

	public final int get_VBR_max_bitrate_kbps() {
		if( is_valid() ) {
			return this.VBR_max_bitrate_kbps;
		}
		return 0;
	}

	/**
	 * Strictly enforce VBR_min_bitrate.
	 * Normally it will be violated for analog silence.
	 */
	public final int set_VBR_hard_min(final boolean vbr_hard_min) {
		if( is_valid() ) {
			/* default = 0 (disabled) */
			this.VBR_hard_min = vbr_hard_min;
			return 0;
		}
		return -1;
	}

	public final boolean get_VBR_hard_min() {
		if( is_valid() ) {
			return this.VBR_hard_min;
		}
		return false;
	}


	/********************************************************************
	 * Filtering control
	 ***********************************************************************/

	/**
	 * Freqency in Hz to apply lowpass.
	 *   0 = default = lame chooses
	 *  -1 = disabled
	 */
	public final int set_lowpassfreq(final int lowpass_freq) {
		if( is_valid() ) {
			this.lowpassfreq = lowpass_freq;
			return 0;
		}
		return -1;
	}

	public final int get_lowpassfreq() {
		if( is_valid() ) {
			return this.lowpassfreq;
		}
		return 0;
	}

	/**
	 * Width of transition band (in Hz).
	 *  default = one polyphase filter band
	 */
	public final int set_lowpasswidth(final int lowpass_width) {
		if( is_valid() ) {
			this.lowpasswidth = lowpass_width;
			return 0;
		}
		return -1;
	}

	public final int get_lowpasswidth() {
		if( is_valid() ) {
			return this.lowpasswidth;
		}
		return 0;
	}

	/**
	 * Frequency in Hz to apply highpass.
	 *   0 = default = lame chooses
	 *  -1 = disabled
	 */
	public final int set_highpassfreq(final int highpass_freq) {
		if( is_valid() ) {
			this.highpassfreq = highpass_freq;
			return 0;
		}
		return -1;
	}

	public final int get_highpassfreq() {
		if( is_valid() ) {
			return this.highpassfreq;
		}
		return 0;
	}

	/**
	 * Width of transition band (in Hz).
	 *  default = one polyphase filter band
	 */
	public final int set_highpasswidth(final int highpass_width) {
		if( is_valid() ) {
			this.highpasswidth = highpass_width;
			return 0;
		}
		return -1;
	}

	public final int get_highpasswidth() {
		if( is_valid() ) {
			return this.highpasswidth;
		}
		return 0;
	}

	/*
	 * psycho acoustics and other arguments which you should not change
	 * unless you know what you are doing
	 */
	/** Adjust masking values. */
	public final int set_maskingadjust(final float adjust) {
		if( is_valid() ) {
			this.maskingadjust = adjust;
			return 0;
		}
		return -1;
	}

	public final float get_maskingadjust() {
		if( is_valid() ) {
			return this.maskingadjust;
		}
		return 0;
	}

	public final int set_maskingadjust_short(final float adjust) {
		if( is_valid() ) {
			this.maskingadjust_short = adjust;
			return 0;
		}
		return -1;
	}

	public final float get_maskingadjust_short() {
		if( is_valid() ) {
			return this.maskingadjust_short;
		}
		return 0;
	}

	/** Only use ATH for masking. */
	public final int set_ATHonly(final boolean is_ATHonly) {
		if( is_valid() ) {
			this.ATHonly = is_ATHonly;
			return 0;
		}
		return -1;
	}

	public final boolean get_ATHonly() {
		if( is_valid() ) {
			return this.ATHonly;
		}
		return false;
	}

	/** Only use ATH for short blocks. */
	public final int set_ATHshort(final boolean is_ATHshort) {
		if( is_valid() ) {
			this.ATHshort = is_ATHshort;
			return 0;
		}
		return -1;
	}

	public final boolean get_ATHshort() {
		if( is_valid() ) {
			return this.ATHshort;
		}
		return false;
	}

	/** Disable ATH. */
	public final int set_noATH(final boolean is_noATH) {
		if( is_valid() ) {
			this.noATH = is_noATH;
			return 0;
		}
		return -1;
	}

	public final boolean get_noATH() {
		if( is_valid() ) {
			return this.noATH;
		}
		return false;
	}

	/** Select ATH formula. */
	public final int set_ATHtype(final int is_ATHtype) {
		if( is_valid() ) {
			/* XXX: ATHtype should be converted to an enum. */
			this.ATHtype = is_ATHtype;
			return 0;
		}
		return -1;
	}

	public final int get_ATHtype() {
		if( is_valid() ) {
			return this.ATHtype;
		}
		return 0;
	}

	/** Select ATH formula 4 shape. */
	public final int set_ATHcurve(final float is_ATHcurve) {
		if( is_valid() ) {
			this.ATHcurve = is_ATHcurve;
			return 0;
		}
		return -1;
	}

	public final float get_ATHcurve() {
		if( is_valid() ) {
			return this.ATHcurve;
		}
		return 0;
	}

	/** Lower ATH by this many db. */
	public final int set_ATHlower(final float ATHlower) {
		if( is_valid() ) {
			this.ATH_lower_db = ATHlower;
			return 0;
		}
		return -1;
	}

	public final float get_ATHlower() {
		if( is_valid() ) {
			return this.ATH_lower_db;
		}
		return 0;
	}

	/** Select ATH adaptive adjustment scheme. */
	public final int set_athaa_type(final int ATHaa_type) {
		if( is_valid() ) {
			this.athaa_type = ATHaa_type;
			return 0;
		}
		return -1;
	}

	public final int get_athaa_type() {
		if( is_valid() ) {
			return this.athaa_type;
		}
		return 0;
	}

	/** Adjust (in dB) the point below which adaptive ATH level adjustment occurs. */
	public final int set_athaa_sensitivity(final float ATHaa_sensitivity) {
		if( is_valid() ) {
			this.athaa_sensitivity = ATHaa_sensitivity;
			return 0;
		}
		return -1;
	}

	public final float get_athaa_sensitivity() {
		if( is_valid() ) {
			return this.athaa_sensitivity;
		}
		return 0;
	}

	/** Predictability limit (ISO tonality formula) */
	/*public final int set_cwlimit(final int cwlimit) {
		return 0;
	}

	public final int get_cwlimit() {
		return 0;
	}*/

	/**
	 * Allow blocktypes to differ between channels.
	 * default:
	 *  0 for jstereo => block types coupled
	 *  1 for stereo  => block types may differ
	 */
	public final int set_allow_diff_short(final boolean allow_diff_short) {
		if( is_valid() ) {
			this.short_blocks = allow_diff_short ? GlobalFlags.short_block_allowed : GlobalFlags.short_block_coupled;
			return 0;
		}
		return -1;
	}

	public final int get_allow_diff_short() {
		if( is_valid() ) {
			if( this.short_blocks == GlobalFlags.short_block_allowed ) {
				return 1;   /* short blocks allowed to differ */
			} else {
				return 0;   /* not set, dispensed, forced or coupled */
			}
		}
		return 0;
	}


	/** Use temporal masking effect */
	public final int set_useTemporal(final boolean is_useTemporal) {
		if( is_valid() ) {
			/* default = 1 (enabled) */
			this.useTemporal = is_useTemporal;
			return 0;
		}
		return -1;
	}

	public final boolean get_useTemporal() {
		if( is_valid() ) {
			return this.useTemporal;
		}
		return false;
	}

	/** Use inter-channel masking effect */
	public final int set_interChRatio(final float ratio) {
		if( is_valid() ) {
			/* default = 0.0 (no inter-channel maskin) */
			if( 0 <= ratio && ratio <= 1.0 ) {
				this.interChRatio = ratio;
				return 0;
			}
		}
		return -1;
	}

	public final float get_interChRatio() {
		if( is_valid() ) {
			return this.interChRatio;
		}
		return 0;
	}

	/** Use pseudo substep shaping method */
	public final int set_substep(final int method) {
		if( is_valid() ) {
			/* default = 0.0 (no substep noise shaping) */
			if( 0 <= method && method <= 7 ) {
				this.substep_shaping = method;
				return 0;
			}
		}
		return -1;
	}

	public final int get_substep() {
		if( is_valid() ) {
			return this.substep_shaping;
		}
		return 0;
	}

	/** scalefactors scale */
	public final int set_sfscale(final boolean val) {
		if( is_valid() ) {
			this.noise_shaping = val ? 2 : 1;
			return 0;
		}
		return -1;
	}

	public final boolean get_sfscale() {
		if( is_valid() ) {
			return (this.noise_shaping == 2);
		}
		return false;
	}

	/** subblock gain */
	public final int set_subblock_gain(final int sbgain) {
		if( is_valid() ) {
			this.subblock_gain = sbgain;
			return 0;
		}
		return -1;
	}

	public final int get_subblock_gain() {
		if( is_valid() ) {
			return this.subblock_gain;
		}
		return 0;
	}

	/** Disable short blocks. */
	public final int set_no_short_blocks(final boolean no_short_blocks) {
		if( is_valid() ) {
			this.short_blocks = no_short_blocks ? GlobalFlags.short_block_dispensed : GlobalFlags.short_block_allowed;
			return 0;
		}
		return -1;
	}

	public final int get_no_short_blocks() {
		if( is_valid() ) {
			switch( this.short_blocks ) {
				default:
			case GlobalFlags.short_block_not_set:
				return -1;
			case GlobalFlags.short_block_dispensed:
				return 1;
			case GlobalFlags.short_block_allowed:
			case GlobalFlags.short_block_coupled:
			case GlobalFlags.short_block_forced:
				return 0;
			}
		}
		return -1;
	}

	/** Force short blocks. */
	public int set_force_short_blocks(final boolean is_short_blocks)
	{
		if( is_valid() ) {
			/* enforce disable/enable meaning, if we need more than two values
			   we need to switch to an enum to have an apropriate representation
			   of the possible meanings of the value */

			if( is_short_blocks ) {
				this.short_blocks = GlobalFlags.short_block_forced;
			} else if( this.short_blocks == GlobalFlags.short_block_forced ) {
				this.short_blocks = GlobalFlags.short_block_allowed;
			}

			return 0;
		}
		return -1;
	}

	public final int get_force_short_blocks() {
		if( is_valid() ) {
			switch( this.short_blocks ) {
			default:
			case GlobalFlags.short_block_not_set:
				return -1;
			case GlobalFlags.short_block_dispensed:
			case GlobalFlags.short_block_allowed:
			case GlobalFlags.short_block_coupled:
				return 0;
			case GlobalFlags.short_block_forced:
				return 1;
			}
		}
		return -1;
	}

	public final int set_short_threshold_lrm(final float lrm) {
		if( is_valid() ) {
			this.attackthre = lrm;
			return 0;
		}
		return -1;
	}

	public final float get_short_threshold_lrm() {
		if( is_valid() ) {
			return this.attackthre;
		}
		return 0;
	}

	public final int set_short_threshold_s(final float s) {
		if( is_valid() ) {
			this.attackthre_s = s;
			return 0;
		}
		return -1;
	}

	public final float get_short_threshold_s() {
		if( is_valid() ) {
			return this.attackthre_s;
		}
		return 0;
	}

	public final int set_short_threshold(final float lrm, final float s) {
		if( is_valid() ) {
			set_short_threshold_lrm( lrm );
			set_short_threshold_s( s );
			return 0;
		}
		return -1;
	}

	/**
	 * Input PCM is emphased PCM
	 * (for instance from one of the rarely emphased CDs).
	 *
	 * It is STRONGLY not recommended to use this, because psycho does not
	 * take it into account, and last but not least many decoders
	 * ignore these bits
	 */
	public final int set_emphasis(final int emphasis_val) {
		if( is_valid() ) {
			/* XXX: emphasis should be converted to an enum */
			if( 0 <= emphasis_val && emphasis_val < 4 ) {
				this.emphasis = emphasis_val;
				return 0;
			}
		}
		return -1;
	}

	public final int get_emphasis() {
		if( is_valid() ) {
			return this.emphasis;
		}
		return 0;
	}

	/***************************************************************/
	/* internal variables, cannot be set...                        */
	/* provided because they may be of use to calling application  */
	/***************************************************************/

	/** MPEG version.
	 *  0 = MPEG-2
	 *  1 = MPEG-1
	 * (2 = MPEG-2.5)
	 */
	public final int get_version() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				return gfc.cfg.version;
			}
		}
		return 0;
	}

	/** Encoder delay. */
	public final int get_encoder_delay() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				return gfc.ov_enc.encoder_delay;
			}
		}
		return 0;
	}

	/** padding added to the end of the input */
	/**
	  padding appended to the input to make sure decoder can fully decode
	  all input.  Note that this value can only be calculated during the
	  call to lame_encoder_flush().  Before lame_encoder_flush() has
	  been called, the value of encoder_padding = 0.
	*/
	public final int get_encoder_padding() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				return gfc.ov_enc.encoder_padding;
			}
		}
		return 0;
	}

	/** Size of MPEG frame. */
	public final int get_framesize() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				final SessionConfig cfg = gfc.cfg;
				return 576 * cfg.mode_gr;
			}
		}
		return 0;
	}

	/** Number of frames encoded so far. */
	public final int get_frameNum() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				return gfc.ov_enc.frame_number;
			}
		}
		return 0;
	}

	/** number of PCM samples buffered, but not yet encoded to mp3 data. */
	public final int get_mf_samples_to_encode() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				return gfc.sv_enc.mf_samples_to_encode;
			}
		}
		return 0;
	}

	/**
	  size (bytes) of mp3 data buffered, but not yet encoded.
	  this is the number of bytes which would be output by a call to
	  lame_encode_flush_nogap.  NOTE: lame_encode_flush() will return
	  more bytes than this because it will encode the reamining buffered
	  PCM samples before flushing the mp3 buffers.
	*/
	public final int get_size_mp3buffer() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				final int size = (int)(Bitstream.compute_flushbits( gfc/*, &size */ ) >> 32);
				return size;
			}
		}
		return 0;
	}

	/**
	 * LAME's estimate of the total number of frames to be encoded.
	 * Only valid if calling program set num_samples.
	 */
	public final int get_totalframes() {
		if( is_valid() ) {
			final InternalFlags gfc = this.internal_flags;
			if( gfc.is_valid() ) {
				final SessionConfig cfg = gfc.cfg;
				final long pcm_samples_per_frame = 576 * cfg.mode_gr;
				long pcm_samples_to_encode = this.num_samples;
				long end_padding = 0;

				/* estimate based on user set num_samples: */
				if( pcm_samples_to_encode == ((0L - 1L) & 0xffffffff) ) {
					return 0;
				}
				pcm_samples_to_encode += 576;
				end_padding = pcm_samples_per_frame - (pcm_samples_to_encode % pcm_samples_per_frame);
				if( end_padding < 576 ) {
					end_padding += pcm_samples_per_frame;
				}
				pcm_samples_to_encode += end_padding;
				/* check to see if we underestimated totalframes */
				/*    if( totalframes < this.frameNum) */
				/*        totalframes = this.frameNum; */
				return (int)(pcm_samples_to_encode / pcm_samples_per_frame);
			}
		}
		return 0;
	}

	public final int set_preset(final int preset_val) {
		if( is_valid() ) {
			this.preset = preset_val;
			return Presets.apply_preset( this, preset_val, true );
		}
		return -1;
	}

	public final void set_write_id3tag_automatic(final boolean v) {
		if( is_valid() ) {
			this.write_id3tag_automatic = v;
		}
	}

	public final boolean get_write_id3tag_automatic() {
		if( is_valid() ) {
			return this.write_id3tag_automatic;
		}
		return true;
	}

	/*
	UNDOCUMENTED, experimental settings.  These routines are not prototyped
	in lame.h.  You should not use them, they are experimental and may
	change.
	*/

	/* Custom msfix hack */
	final void set_msfix(final float msfix_val) {
		if( is_valid() ) {
			/* default = 0 */
			this.msfix = msfix_val;
		}
	}

	final float get_msfix() {
		if( is_valid() ) {
			return this.msfix;
		}
		return 0;
	}
/*
	final int set_preset_notune(final int preset_notune) {
		return 0;
	}
*/
}

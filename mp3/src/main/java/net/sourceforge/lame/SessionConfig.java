package net.sourceforge.lame;

class SessionConfig {
	/** 0=MPEG-2/2.5  1=MPEG-1 */
	int     version;
	int     samplerate_index;
	int     sideinfo_len;
	/** 0 = none
	 * 1 = ISO AAC model
	 * 2 = allow scalefac_select=1
	 */
	int     noise_shaping;
	/**  0 = no, 1 = yes */
	int     subblock_gain;// -1 don't set
	/** 0 = no.  1=outside loop  2=inside loop(slow) */
	int     use_best_huffman;
	/** 0 = ISO model: amplify all distorted bands
	 * 1 = amplify within 50% of max (on db scale)
	 * 2 = amplify only most distorted band
	 * 3 = method 1 and refine with method 2
	 */
	int     noise_shaping_amp;
	/** 0 = stop at over=0, all scalefacs amplified or
	 * a scalefac has reached max value
	 * 1 = stop when all scalefacs amplified or
	 * a scalefac has reached max value
	 * 2 = stop when all scalefacs amplified
	 */
	int     noise_shaping_stop;
	/* 0 = stop early after 0 distortion found. 1 = full search */
	int full_outer_loop;// FIXME uses -1, 0, +1 values

	int     lowpassfreq;
	int     highpassfreq;
	/** samp_rate in Hz. default=44.1 kHz     */
	int     samplerate;
	/** number of channels in the input data stream (PCM or decoded PCM) */
	int     channels_in;
	/** number of channels in the output data stream (not used for decoding) */
	int     channels_out;
	/** granules per frame */
	int     mode_gr;
	/** force M/S mode.  requires mode=1 */
	boolean     force_ms;

	int     quant_comp;
	int     quant_comp_short;

	boolean use_temporal_masking_effect;
	boolean use_safe_joint_stereo;

	int     preset;

	int /* vbr_mode */ vbr;
	int     vbr_avg_bitrate_kbps;
	/** min bitrate index */
	int     vbr_min_bitrate_index;
	/** max bitrate index */
	int     vbr_max_bitrate_index;
	int     avg_bitrate;
	/** strictly enforce VBR_min_bitrate normaly, it will be violated for analog silence */
	boolean enforce_min_bitrate;
	boolean disable_reservoir;
	/** enforce ISO spec as much as possible */
	int     buffer_constraint;
	boolean free_format;
	/** add Xing VBR tag? */
	boolean write_lame_tag;

	/** use 2 bytes per frame for a CRC checksum. default=0 */
	boolean error_protection;
	/** mark as copyright. default=0 */
	boolean copyright;
	/** mark as original. default=1 */
	boolean original;
	/** the MP3 'private extension' bit. Meaningless */
	boolean extension;
	/** Input PCM is emphased PCM (for instance from one of the rarely emphased CDs),
	 * it is STRONGLY not recommended to use this, because psycho does not take it into account,
	 * and last but not least many decoders don't care about these bits */
	int     emphasis;

	int /* MPEG_mode */ mode;
	int /* short_block_t */ short_blocks;

	float   interChRatio;
	/** Naoki's adjustment of Mid/Side maskings */
	float   msfix;// experimental
	/** add to ATH this many db */
	float   ATH_offset_db;
	/** change ATH by this factor, derived from ATH_offset_db */
	float   ATH_offset_factor;
	/** change ATH formula 4 shape */
	float   ATHcurve;
	int     ATHtype;
	/** only use ATH */
	boolean ATHonly;
	/** only use ATH for short blocks */
	boolean ATHshort;
	/** disable ATH */
	boolean noATH;

	float   ATHfixpoint;

	float   adjust_alto_db;
	float   adjust_bass_db;
	float   adjust_treble_db;
	float   adjust_sfb21_db;
	/** sizeof(wav file)/sizeof(mp3 file) */
	float   compression_ratio;

	/* lowpass and highpass filter control */
	/** normalized frequency bounds of passband */
	float   lowpass1, lowpass2;
	/** normalized frequency bounds of passband */
	float   highpass1, highpass2;

	/** scale input by this amount before encoding at least not used for MP3 decoding */
	final float pcm_transform[][] = new float[2][2];

	float   minval;
}

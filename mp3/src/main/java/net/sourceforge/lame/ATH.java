package net.sourceforge.lame;

/**
 *  ATH related stuff, if something new ATH related has to be added,
 *  please plugg it here into the ATH_t struct
 */
class ATH {
	/** method for the auto adjustment  */
	int   use_adjust;
	/** factor for tuning the (sample power)
    point below which adaptive threshold
    of hearing adjustment occurs */
	float aa_sensitivity_p;
	/** lowering based on peak volume, 1 = no lowering */
	float adjust_factor;
	/** limit for dynamic ATH adjust */
	float adjust_limit;
	/** determined to lower x dB each second */
	float decay;
	/** lowest ATH value */
	float floor;
	/** ATH for sfbs in long blocks */
	final float l[] = new float[Encoder.SBMAX_l];
	/** ATH for sfbs in short blocks */
	final float s[] = new float[Encoder.SBMAX_s];
	/** ATH for partitionned sfb21 in long blocks */
	final float psfb21[] = new float[Encoder.PSFB21];
	/** ATH for partitionned sfb12 in short blocks */
	final float psfb12[] = new float[Encoder.PSFB12];
	/** ATH for long block convolution bands */
	final float cb_l[] = new float[Encoder.CBANDS];
	/** ATH for short block convolution bands */
	final float cb_s[] = new float[Encoder.CBANDS];
	/** equal loudness weights (based on ATH) */
	final float eql_w[] = new float[Encoder.BLKSIZE / 2];
}

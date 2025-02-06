package net.sourceforge.lame;

class PsyResult {
	/** loudness calculation (for adaptive threshold of hearing) */
	final float loudness_sq[][] = new float[2][2]; /* loudness^2 approx. per granule and channel */
}

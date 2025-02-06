package net.sourceforge.lame;

/**
 * allows re-use of previously
 * computed noise values
 */
class CalcNoiseData {
	int global_gain;
	int sfb_count1;
	final int   step[] = new int[39];
	final float noise[] = new float[39];
	final float noise_log[] = new float[39];
}

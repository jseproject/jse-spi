package net.sourceforge.lame;

interface Synth {
	/** java: pnt is sample counter, not byte counter */
	int synth_1to1_mono(MpStrTag mp, float[] bandPtr, int boffset, Object out, int[] pnt);
	/** java: pnt is sample counter, not byte counter */
	int synth_1to1(MpStrTag mp, float[] bandPtr, int boffset, int channel, Object out, int[] pnt);
}

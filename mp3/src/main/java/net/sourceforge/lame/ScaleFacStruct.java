package net.sourceforge.lame;

/** Layer III side information. */
class ScaleFacStruct {
	final int l[];// = new int[1 + Encoder.SBMAX_l];
	final int s[];// = new int[1 + Encoder.SBMAX_s];
	final int psfb21[];// = new int[1 + Encoder.PSFB21];
	final int psfb12[];// = new int[1 + Encoder.PSFB12];
	//
	ScaleFacStruct() {
		this.l = new int[1 + Encoder.SBMAX_l];
		this.s = new int[1 + Encoder.SBMAX_s];
		this.psfb21 = new int[1 + Encoder.PSFB21];
		this.psfb12 = new int[1 + Encoder.PSFB12];
	}
	ScaleFacStruct(final int[] il, final int[] is, final int[] ipsfb21, final int[] ipsfb12) {
		this.l = il;
		this.s = is;
		this.psfb21 = ipsfb21;
		this.psfb12 = ipsfb12;
	}
}

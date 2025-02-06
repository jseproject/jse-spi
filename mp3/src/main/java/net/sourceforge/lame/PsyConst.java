package net.sourceforge.lame;

class PsyConst {
	final float window[] = new float[Encoder.BLKSIZE];
	final float window_s[] = new float[Encoder.BLKSIZE_s / 2];
	final PsyConst_CB2SB l = new PsyConst_CB2SB();
	final PsyConst_CB2SB s = new PsyConst_CB2SB();
	final PsyConst_CB2SB l_to_s = new PsyConst_CB2SB();
	final float attack_threshold[] = new float[4];
	float   decay;
	boolean force_short_block_calc;
}

package net.sourceforge.lame;

class PsyStateVar {
	final float nb_l1[][] = new float[4][Encoder.CBANDS];
	final float nb_l2[][] = new float[4][Encoder.CBANDS];
	final float nb_s1[][] = new float[4][Encoder.CBANDS];
	final float nb_s2[][] = new float[4][Encoder.CBANDS];

	final III_PsyXmin thm[] = new III_PsyXmin[4];
	final III_PsyXmin en[] = new III_PsyXmin[4];

	/* loudness calculation (for adaptive threshold of hearing) */
	final float loudness_sq_save[] = new float[2]; /* account for granule delay of L3psycho_anal */

	final float tot_ener[] = new float[4];

	final float last_en_subshort[][] = new float[4][9];
	final int last_attacks[] = new int[4];

	final int blocktype_old[] = new int[2];
	//
	PsyStateVar() {
		thm[0] = new III_PsyXmin();
		thm[1] = new III_PsyXmin();
		thm[2] = new III_PsyXmin();
		thm[3] = new III_PsyXmin();
		en[0] = new III_PsyXmin();
		en[1] = new III_PsyXmin();
		en[2] = new III_PsyXmin();
		en[3] = new III_PsyXmin();
	}
}

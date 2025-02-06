package net.sourceforge.lame;

@SuppressWarnings("unused")
class PsyConst_CB2SB {
	final float masking_lower[] = new float[Encoder.CBANDS];
	final float minval[] = new float[Encoder.CBANDS];
	final float rnumlines[] = new float[Encoder.CBANDS];
	final float mld_cb[] = new float[Encoder.CBANDS];

	final float mld[] = new float[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];
	final float bo_weight[] = new float[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)]; /* band weight long scalefactor bands, at transition */
	float attack_threshold; /* short block tuning */
	int     s3ind[][] = new int[Encoder.CBANDS][2];
	int     numlines[] = new int[Encoder.CBANDS];
	int     bm[] = new int[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];// FIXME never uses bm
	int     bo[] = new int[(Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s)];
	int     npart;
	int     n_sb; /* SBMAX_l or SBMAX_s */
	float[] s3;
	//
	final void copyFrom(final PsyConst_CB2SB p) {
		System.arraycopy( p.masking_lower, 0, this.masking_lower, 0, Encoder.CBANDS );
		System.arraycopy( p.minval, 0, this.minval, 0, Encoder.CBANDS );
		System.arraycopy( p.rnumlines, 0, this.rnumlines, 0, Encoder.CBANDS );
		System.arraycopy( p.mld_cb, 0, this.mld_cb, 0, Encoder.CBANDS );
		System.arraycopy( p.mld, 0, this.mld, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		System.arraycopy( p.bo_weight, 0, this.bo_weight, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		this.attack_threshold = p.attack_threshold;
		int i = Encoder.CBANDS;
		final int[][] buf = this.s3ind;
		final int[][] ibuf = p.s3ind;
		do {
			final int[] ib = ibuf[--i];
			final int[] b = buf[i];
			b[0] = ib[0];
			b[1] = ib[1];
		} while( i > 0 );
		System.arraycopy( p.numlines, 0, this.numlines, 0, Encoder.CBANDS );
		System.arraycopy( p.bm, 0, this.bm, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		System.arraycopy( p.bo, 0, this.bo, 0, (Encoder.SBMAX_l >= Encoder.SBMAX_s ? Encoder.SBMAX_l : Encoder.SBMAX_s) );
		this.npart = p.npart;
		this.n_sb = p.n_sb;
		this.s3 = p.s3;
	}
}

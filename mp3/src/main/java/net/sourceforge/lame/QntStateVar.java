package net.sourceforge.lame;

/** variables used by quantize.c */
class QntStateVar {
	/* variables for nspsytune */
	final float longfact[] = new float[ Encoder.SBMAX_l ];
	final float shortfact[] = new float[ Encoder.SBMAX_s ];
	float masking_lower;
	float mask_adjust; /* the dbQ stuff */
	float mask_adjust_short; /* the dbQ stuff */
	final int OldValue[] = new int[2];
	final int CurrentStep[] = new int[2];
	final boolean pseudohalf[] = new boolean[ Encoder.SFBMAX ];
	boolean sfb21_extra; /* will be set in lame_init_params */
	/** 0 = no substep
	 * 1 = use substep shaping at last step(VBR only) (not implemented yet)
	 * 2 = use substep inside loop
	 * 3 = use substep inside loop and last step
	 */
	int substep_shaping;

	final byte bv_scf[] = new byte[576];
}

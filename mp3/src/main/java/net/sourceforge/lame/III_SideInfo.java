package net.sourceforge.lame;

class III_SideInfo {
	final III_GrInfo tt[][] = new III_GrInfo[2][2];
	int     main_data_begin;
	int     private_bits;
	int     resvDrain_pre;
	int     resvDrain_post;
	final int scfsi[][] = new int[2][4];
	//
	III_SideInfo() {
		tt[0][0] = new III_GrInfo();
		tt[0][1] = new III_GrInfo();
		tt[1][0] = new III_GrInfo();
		tt[1][1] = new III_GrInfo();
	}

	/** convert from L/R <. Mid/Side */
	final void ms_convert(final int gr) {
		final III_GrInfo[] t = this.tt[gr];// java
		final float[] xr0 = t[0].xr;// java
		final float[] xr1 = t[1].xr;// java
		for( int i = 0; i < 576; ++i ) {
			final float l = xr0[i];
			final float r = xr1[i];
			xr0[i] = (l + r) * (Util.SQRT2 * 0.5f);
			xr1[i] = (l - r) * (Util.SQRT2 * 0.5f);
		}
	}
}

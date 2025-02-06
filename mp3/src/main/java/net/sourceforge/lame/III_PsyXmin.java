package net.sourceforge.lame;

class III_PsyXmin {
	final float l[] = new float[Encoder.SBMAX_l];
	final float s[][] = new float[Encoder.SBMAX_s][3];
	//
	III_PsyXmin() {
	}
	III_PsyXmin(final III_PsyXmin p) {
		copyFrom( p );
	}
	final void copyFrom(final III_PsyXmin p) {
		System.arraycopy( p.l, 0, this.l, 0, Encoder.SBMAX_l );
		int i = Encoder.SBMAX_s;
		final float ibuf[][] = p.s;
		final float buf[][] = this.s;
		do {
			final float[] ib = ibuf[--i];
			final float[] b = buf[i];
			b[0] = ib[0];
			b[1] = ib[1];
			b[2] = ib[2];
		} while( i > 0 );
	}
}

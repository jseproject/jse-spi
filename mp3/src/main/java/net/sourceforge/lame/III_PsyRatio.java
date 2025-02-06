package net.sourceforge.lame;

class III_PsyRatio {
	final III_PsyXmin thm = new III_PsyXmin();
	final III_PsyXmin en = new III_PsyXmin();
	//
	final void copyFrom(final III_PsyRatio r) {
		this.thm.copyFrom( r.thm );
		this.en.copyFrom( r.en );
	}
}

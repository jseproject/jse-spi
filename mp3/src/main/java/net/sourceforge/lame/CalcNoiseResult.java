package net.sourceforge.lame;

class CalcNoiseResult {
	/** sum of quantization noise > masking */
	float   over_noise;
	/** sum of all quantization noise */
	float   tot_noise;
	/** max quantization noise */
	float   max_noise;
	/** number of quantization noise > masking */
	int     over_count;
	/** SSD-like cost of distorted bands */
	int     over_SSD;
	int     bits;
	//
	final void copyFrom(final CalcNoiseResult r) {
		this.over_noise = r.over_noise;
		this.tot_noise = r.tot_noise;
		this.max_noise = r.max_noise;
		this.over_count = r.over_count;
		this.over_SSD = r.over_SSD;
		this.bits = r.bits;
	}
}

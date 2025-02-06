package net.sourceforge.lame;

/** java using:
 * <pre>
 * pow2gain_base[ pow2gain ]
 * full_gain[ full_gain_pos ]
 * </pre>
 */
class MpgGrInfo {
	int scfsi;
	int part2_3_length;
	int big_values;
	int scalefac_compress;
	int block_type;
	int mixed_block_flag;
	final int table_select[] = new int[3];
	final int subblock_gain[] = new int[3];
	final int maxband[] = new int[3];
	int maxbandl;
	int maxb;
	int region1start;
	int region2start;
	boolean preflag;
	int scalefac_scale;
	int count1table_select;
	/** full_gain[ full_gain_pos ] */
	final float full_gain[][] = new float[3][];
	/** full_gain[ full_gain_pos ] */
	final int full_gain_pos[] = new int[3];
	/** pow2gain_base[ pow2gain ] */
	int pow2gain;
	/** pow2gain_base[ pow2gain ] */
	float[] pow2gain_base;
	//
	final void clear() {
		scfsi = 0;
		part2_3_length = 0;
		big_values = 0;
		scalefac_compress = 0;
		block_type = 0;
		mixed_block_flag = 0;
		table_select[0] = 0; table_select[1] = 0; table_select[2] = 0;
		subblock_gain[0] = 0; subblock_gain[1] = 0; subblock_gain[2] = 0;
		maxband[0] = 0; maxband[1] = 0; maxband[2] = 0;
		maxbandl = 0;
		maxb = 0;
		region1start = 0;
		region2start = 0;
		preflag = false;
		scalefac_scale = 0;
		count1table_select = 0;
		full_gain[0] = null; full_gain[1] = null; full_gain[2] = null;
		full_gain_pos[0] = 0; full_gain_pos[1] = 0; full_gain_pos[2] = 0;
		pow2gain = 0;
		pow2gain_base = null;
	}
}

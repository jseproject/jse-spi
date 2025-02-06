package net.sourceforge.lame;

/** simple statistics */
class EncResult {
	final int bitrate_channelmode_hist[][] = new int[16][4 + 1];
	/** norm/start/short/stop/mixed(short)/sum */
	final int bitrate_blocktype_hist[][] = new int[16][4 + 1 + 1];

	int     bitrate_index;
	/** number of frames encoded */
	int     frame_number;
	/** padding for the current frame? */
	boolean padding;
	int     mode_ext;
	int     encoder_delay;
	/** number of samples of padding appended to input */
	int     encoder_padding;
}

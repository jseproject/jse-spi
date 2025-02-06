package net.sourceforge.lame;

class FrameDataNode {
	FrameDataNode nxt;
	/** Frame Identifier */
	int fid;
	/** 3-character language descriptor */
	// final byte[] lng = new byte[3];
	String lng;
	//
	//static final class Ptr {
	//	union {
	//	final char   *l;       // ptr to Latin-1 chars
	//	final unsigned short *u; // ptr to UCS-2 text
	//	final unsigned char *b; // ptr to raw bytes
		//byte[] b;
		/** 0:Latin-1, 1:UCS-2, 2:RAW */
		//boolean enc;
	//};
	//	size_t  dim;
	//} dsc, txt;
	//final Ptr dsc = new Ptr();
	//final Ptr txt = new Ptr();
	//String dsc;
	//String txt;
	boolean enc;
	byte[] dsc;
	byte[] txt;
}

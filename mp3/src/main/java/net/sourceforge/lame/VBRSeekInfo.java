package net.sourceforge.lame;

class VBRSeekInfo {
	/** what we have seen so far */
	int     sum;
	/** how many frames we have seen in this chunk */
	int     seen;
	/** how many frames we want to collect into one chunk */
	int     want;
	/** actual position in our bag */
	int     pos;
	/** size of our bag */
	// int     size;
	/** pointer to our bag */
	int[]   bag;// java: size = bag.length
	int nVbrNumFrames;// uint32?
	int nBytesWritten;// uint32?
	/** VBR tag data */
	int TotalFrameSize;// uint32?
}

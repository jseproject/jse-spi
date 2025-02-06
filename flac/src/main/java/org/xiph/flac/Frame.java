package org.xiph.flac;

/** FLAC frame structure.  (c.f. <A HREF="../format.html#frame">format specification</A>)
 */
public class Frame {
	public final FrameHeader header;
	public final Subframe[] subframes = new Subframe[Format.FLAC__MAX_CHANNELS];
	public final FrameFooter footer = new FrameFooter();

	Frame() {
		header = new FrameHeader();
		createSubframeHolders();
	}

	Frame(final FrameHeader frameHeader, final char footerCRC) {
		header = frameHeader;
		footer.crc = footerCRC;
		createSubframeHolders();
	}

	private void createSubframeHolders() {
		for(int i = 0; i < Format.FLAC__MAX_CHANNELS; i++ ) {
			subframes[i] = new Subframe();
		}
	}
}

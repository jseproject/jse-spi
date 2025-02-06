package net.sourceforge.lame;

/** structure to receive extracted header
 * toc may be NULL */
public class VBRTagData {
	/** from MPEG header, 0=MPEG2, 1=MPEG1 */
	int     h_id;
	/** determined from MPEG header */
	int     samprate;
	/** from Vbr header data */
	int     flags;
	/** total bit stream frames from Vbr header data */
	public int     frames;
	/** total bit stream bytes from Vbr header data */
	int     bytes;
	/** encoded vbr scale from Vbr header data */
	int     vbr_scale;
	/** may be NULL if toc not desired */
	final byte toc[] = new byte[VBRTag.NUMTOCENTRIES];
	/** size of VBR header, in bytes */
	public int     headersize;
	/** encoder delay */
	public int     enc_delay;
	/** encoder paddign added at end of stream */
	public int     enc_padding;
}

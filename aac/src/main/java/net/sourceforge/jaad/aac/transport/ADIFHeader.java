package net.sourceforge.jaad.aac.transport;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.IBitStream;
import net.sourceforge.jaad.aac.syntax.PCE;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * @author in-somnia
 */
public final class ADIFHeader {

	private static final long ADIF_ID = 0x41444946; //'ADIF'
	private long id;
	private boolean copyrightIDPresent;
	private byte[] copyrightID;
	private boolean originalCopy, home, bitstreamType;
	private int bitrate;
	private int pceCount;
	private int[] adifBufferFullness;
	private PCE[] pces;

	public static boolean isPresent(IBitStream _in) throws AACException {
		return _in.peekBits(32)==ADIF_ID;
	}

	private ADIFHeader() {
		copyrightID = new byte[9];
	}

	public static ADIFHeader readHeader(IBitStream _in) throws AACException {
		final ADIFHeader h = new ADIFHeader();
		h.decode(_in);
		return h;
	}

	private void decode(IBitStream _in) throws AACException {
		int i;
		id = _in.readBits(32); //'ADIF'
		copyrightIDPresent = _in.readBool();
		if(copyrightIDPresent) {
			for(i = 0; i<9; i++) {
				copyrightID[i] = (byte) _in.readBits(8);
			}
		}
		originalCopy = _in.readBool();
		home = _in.readBool();
		bitstreamType = _in.readBool();
		bitrate = _in.readBits(23);
		pceCount = _in.readBits(4)+1;
		pces = new PCE[pceCount];
		adifBufferFullness = new int[pceCount];
		for(i = 0; i<pceCount; i++) {
			if(bitstreamType) adifBufferFullness[i] = -1;
			else adifBufferFullness[i] = _in.readBits(20);
			pces[i] = new PCE();
			pces[i].decode(_in);
		}
	}

	public PCE getFirstPCE() {
		return pces[0];
	}
}

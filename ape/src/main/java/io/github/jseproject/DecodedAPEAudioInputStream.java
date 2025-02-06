/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package io.github.jseproject;

import davaguine.jmac.decoder.IAPEDecompress;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class DecodedAPEAudioInputStream extends TAsynchronousFilteredAudioInputStream {

    private final static int BLOCKS_PER_DECODE = 9216;

    private APEAudioInputStream audioInputStream;
    private IAPEDecompress decoder;
    private byte[] buffer;
    private int blocksLeft;
    private final int blockAlign;

    public DecodedAPEAudioInputStream(AudioFormat format, APEAudioInputStream inputStream) {
        super(format, AudioSystem.NOT_SPECIFIED);
        audioInputStream = inputStream;
        this.decoder = inputStream.decoder;
        blocksLeft = decoder.getApeInfoDecompressTotalBlocks();
        blockAlign = decoder.getApeInfoBlockAlign();
        // allocate space for decompression
        buffer = new byte[blockAlign * BLOCKS_PER_DECODE];
        buffer = new byte[blockAlign * BLOCKS_PER_DECODE];
    }

    @Override
    public void execute() {
        if (TDebug.TraceAudioInputStream) TDebug.out("execute(): begin");
        try {
            if (blocksLeft > 0) {
                int nBlocksDecoded = decoder.GetData(buffer, BLOCKS_PER_DECODE);
                blocksLeft -= nBlocksDecoded;
                getCircularBuffer().write(buffer, 0, nBlocksDecoded * blockAlign);
            }
            else getCircularBuffer().close();
        }
        catch (IOException e) {
            if (TDebug.TraceAudioConverter) TDebug.out(e);
            getCircularBuffer().close();
        }
        if (TDebug.TraceAudioInputStream) TDebug.out("execute(): end");
    }

    @Override
    public void close() throws IOException {
        super.close();
        audioInputStream.close();
        audioInputStream = null;
        decoder = null;
        buffer = null;
    }

}

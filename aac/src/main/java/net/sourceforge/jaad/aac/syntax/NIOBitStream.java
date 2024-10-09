/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.jaad.aac.syntax;

import java.nio.ByteBuffer;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.BitReader;

/**
 * This class is part of JAAD ( jaadec.sourceforge.net ) that is distributed
 * under the Public Domain license. Code changes provided by the JCodec project
 * are distributed under FreeBSD license.
 * 
 * @author in-somnia
 */
public class NIOBitStream implements IBitStream {

    private BitReader br;

    public NIOBitStream(BitReader br) {
        this.br = br;
    }

    @Override
    public void destroy() {
        reset();
        br = null;
    }

    @Override
    public void setData(byte[] data) {
        br = BitReader.createBitReader(ByteBuffer.wrap(data));
    }

    @Override
    public void byteAlign() throws AACException {
        br.align();
    }

    @Override
    public void reset() {
        throw new RuntimeException("todo");
    }

    @Override
    public int getPosition() {
        return br.position();
    }

    @Override
    public int getBitsLeft() {
        return br.remaining();
    }

    @Override
    public int readBits(int n) throws AACException {
        if (br.remaining() >= n) {
            return br.readNBit(n);
        }
        throw AACException.endOfStream();
    }

    @Override
    public int readBit() throws AACException {
        if (br.remaining() >= 1) {
            return br.read1Bit();
        }
        throw AACException.endOfStream();
    }

    @Override
    public boolean readBool() throws AACException {
        int read1Bit = readBit();
        return read1Bit != 0;
    }

    @Override
    public int peekBits(int n) throws AACException {
        int checkNBit = br.checkNBit(n);
        return checkNBit;
    }

    @Override
    public int peekBit() throws AACException {
        int curBit = br.curBit();
        return curBit;
    }

    @Override
    public void skipBits(int n) throws AACException {
        br.skip(n);
    }

    @Override
    public void skipBit() throws AACException {
        skipBits(1);
    }

    @Override
    public int maskBits(int n) {
        int i;
        if (n == 32)
            i = -1;
        else
            i = (1 << n) - 1;
        return i;
    }

}

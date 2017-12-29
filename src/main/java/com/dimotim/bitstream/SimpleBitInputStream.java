package com.dimotim.bitstream;

import com.dimotim.huffman.BitInputStream;
import com.dimotim.huffman.Constants;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


public final class SimpleBitInputStream implements BitInputStream {
    private final byte[] masks=Constants.getMasks();
    private int ind = 8;
    private byte buf;
    private final InputStream is;

    public SimpleBitInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public Constants.Bit readBit() throws IOException {
        if (ind > 7) {
            ind = 0;
            int next = is.read();
            if (next == -1) throw new EOFException();
            buf = (byte) next;
        }
        return (masks[ind++] & buf) == 0 ? Constants.Bit.ZERO : Constants.Bit.ONE;
    }

    @Override
    public byte readByte() throws IOException {
        byte res = 0;
        for (int i = 0; i < 8; i++) {
            res <<= 1;
            res |= readBit().ordinal();
        }
        return res;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}

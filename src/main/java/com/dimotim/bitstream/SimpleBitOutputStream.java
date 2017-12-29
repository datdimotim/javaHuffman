package com.dimotim.bitstream;

import com.dimotim.huffman.BitOutputStream;
import com.dimotim.huffman.Constants;

import java.io.IOException;
import java.io.OutputStream;

public final class SimpleBitOutputStream implements BitOutputStream {
    private final byte[] masks=Constants.getMasks();
    private int ind = 0;
    private byte buf = 0;
    private final OutputStream os;

    public SimpleBitOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void writeBit(Constants.Bit bit) throws IOException {
        if (ind > 7) {
            ind = 0;
            os.write(buf);
            buf = 0;
        }
        buf |= (bit.ordinal() << (ind++));
    }

    @Override
    public void writeByte(byte b) throws IOException {
        for (int i = 7; i >= 0; i--) {
            Constants.Bit bit = ((b & masks[i]) == 0) ? Constants.Bit.ZERO : Constants.Bit.ONE;
            writeBit(bit);
        }
    }

    @Override
    public void close() throws IOException {
        if (ind != 0) os.write(buf);
        os.close();
    }
}

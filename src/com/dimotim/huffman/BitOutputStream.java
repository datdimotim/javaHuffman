package com.dimotim.huffman;

import java.io.IOException;

public interface BitOutputStream extends AutoCloseable {
    void writeBit(Constants.Bit bit) throws IOException;
    void writeByte(byte b) throws IOException;
}


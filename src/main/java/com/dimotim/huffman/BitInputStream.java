package com.dimotim.huffman;

import java.io.IOException;

public interface BitInputStream extends AutoCloseable{
    Constants.Bit readBit() throws IOException;
    byte readByte() throws IOException;
}

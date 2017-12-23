package com.dimotim.huffman;

public final class Constants {
    public static byte[] getMasks(){return new byte[] {1, 2, 4, 8, 16, 32, 64, (byte) 128};}
    public enum Bit {
        ZERO, ONE;
        @Override
        public String toString() {
            return ""+this.ordinal();
        }
    }
}

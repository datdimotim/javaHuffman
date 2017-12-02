package com.dimotim.huffman;

import java.io.*;

import static com.dimotim.huffman.Constants.*;
class Constants{
    static final byte[] masks={1,2,4,8,16,32,64,(byte) 128};
    enum Bit{ZERO,ONE}
}

class BitInputStream implements AutoCloseable{
    private int ind=8;
    private byte buf;
    private final InputStream is;
    BitInputStream(InputStream is){
        this.is=is;
    }
    Bit readBit() throws IOException {
        if(ind>7){
            ind=0;
            int next=is.read();
            if(next==-1)throw new EOFException();
            buf= (byte) next;
        }
        return (masks[ind++]&buf)==0?Bit.ZERO:Bit.ONE;
    }
    byte readByte() throws IOException {
        byte res=0;
        for(int i=0;i<8;i++){
            res<<=1;
            res|=readBit().ordinal();
        }
        return res;
    }
    @Override
    public void close() throws IOException {
        is.close();
    }
}

class BitOutputStream implements AutoCloseable{
    private int ind=0;
    private byte buf=0;
    private final OutputStream os;
    BitOutputStream(OutputStream os) {
        this.os = os;
    }
    void writeBit(Bit bit) throws IOException {
        if(ind>7){
            ind=0;
            os.write(buf);
            buf=0;
        }
        buf|=(bit.ordinal()<<(ind++));
    }
    void writeByte(byte b) throws IOException {
        for(int i=7;i>=0;i--){
            Bit bit=((b&masks[i])==0)?Bit.ZERO:Bit.ONE;
            writeBit(bit);
        }
    }
    @Override
    public void close() throws IOException {
        if(ind!=0)os.write(buf);
        os.close();
    }
}


public class Main {

    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        BitOutputStream bitOutputStream=new BitOutputStream(bos);
        bitOutputStream.writeBit(Bit.ZERO);
        bitOutputStream.writeBit(Bit.ZERO);
        bitOutputStream.writeBit(Bit.ZERO);
        bitOutputStream.writeBit(Bit.ZERO);
        bitOutputStream.writeBit(Bit.ZERO);
        bitOutputStream.writeBit(Bit.ONE);
        bitOutputStream.writeBit(Bit.ONE);
        bitOutputStream.writeBit(Bit.ONE);
        bitOutputStream.writeBit(Bit.ONE);
        bitOutputStream.writeBit(Bit.ONE);
        bitOutputStream.close();


        ByteArrayInputStream bis=new ByteArrayInputStream(bos.toByteArray());
        BitInputStream bitInputStream=new BitInputStream(bis);

        for(int i=0;i<16;i++)System.out.println(bitInputStream.readBit());
    }
}

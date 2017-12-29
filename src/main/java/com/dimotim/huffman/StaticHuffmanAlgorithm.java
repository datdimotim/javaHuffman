package com.dimotim.huffman;

import com.dimotim.java8Compability.InputStreamReadAllBytes;
import org.jetbrains.annotations.NotNull;
import com.dimotim.bitstream.SimpleBitInputStream;
import com.dimotim.bitstream.SimpleBitOutputStream;

import java.io.*;
import java.util.Arrays;

import static com.dimotim.huffman.Constants.Bit;

public class StaticHuffmanAlgorithm {
    public static void decode(InputStream inStd, OutputStream code) throws IOException {
        if(inStd.available()==0)return;
        int[] stat=readStat(new DataInputStream(inStd));
        Node tree=getTree(stat);
        BitInputStream bis=new SimpleBitInputStream(inStd);
        int count=numberOfSymbols(stat);

        if(tree.left==null){
            for (int i=0;i<count;i++)code.write(tree.symbol);
        }
        else for(int i=0;i<count;i++)code.write(symbolByBitStream(bis,tree));
        code.close();
    }
    public static void encode(InputStream in, OutputStream codeStd) throws Exception {
        if(in.available()==0)return;
        in=new ByteArrayInputStream((InputStreamReadAllBytes.readAllBytes(in)));

        int[] stat=getStat(in);
        writeStat(new DataOutputStream(codeStd),stat);
        Node tree=getTree(stat);
        BitCode[] codes=getCodes(tree);

        in.reset();
        BitOutputStream codeStream = new SimpleBitOutputStream(codeStd);

        // сообщения из 1 различного символа не записываются
        if(tree.left!=null) while (0!=in.available())writeCode(codeStream,codes[in.read()]);
        codeStream.close();
    }

    private static byte symbolByBitStream(BitInputStream bis, Node root) throws IOException {
        if(bis.readBit()==Bit.ZERO){
            if(root.left.left==null)return root.left.symbol;
            else return symbolByBitStream(bis, root.left);
        }
        else {
            if(root.right.left==null)return root.right.symbol;
            else return symbolByBitStream(bis,root.right);
        }
    }

    private static int numberOfSymbols(int[] stat){
        int r=0;
        for(int c:stat)r+=c;
        return r;
    }

    private static void writeCode(BitOutputStream bs, BitCode code) throws IOException {
        for(Bit b:code.code)bs.writeBit(b);
    }

    private static void getCodesHelper(Node t, Bit[] code, int deep, BitCode[] codes){
        if(t.left!=null){
            code[deep]=Bit.ZERO;
            getCodesHelper(t.left,code,deep+1,codes);
        }
        else {
            Bit[] tmp=new Bit[deep];
            System.arraycopy(code,0,tmp,0,deep);
            codes[Byte.toUnsignedInt(t.symbol)]=new BitCode(tmp);
            return;
        }
        code[deep]=Bit.ONE;
        getCodesHelper(t.right,code,deep+1,codes);
    }

    private static BitCode[] getCodes(Node tree){
        BitCode[] codes=new BitCode[256];
        getCodesHelper(tree,new Bit[256],0,codes);
        return codes;
    }

    private static void writeStat(DataOutputStream dos, int[] stat) throws IOException {
        int count=0;
        for(int v:stat)if(v!=0)count++;
        dos.writeInt(count);
        for(int i=0;i<stat.length;i++){
            if(stat[i]==0)continue;
            dos.writeByte(i);
            dos.writeInt(stat[i]);
        }
    }
    private static int[] readStat(DataInputStream dis) throws IOException {
        int[] stat=new int[256];
        int count=dis.readInt();
        for(int i=0;i<count;i++){
            byte ind=dis.readByte();
            stat[Byte.toUnsignedInt(ind)]=dis.readInt();
        }
        return stat;
    }
    private static int[] getStat(InputStream in) throws IOException {
        int[] stat=new int[256];
        while (in.available()!=0)stat[+in.read()]++;
        return stat;
    }

    private static Node getTree(int[] stat){
        PriorityQueue<Node> pq=new PriorityQueue<>(256,Node[]::new);
        for(int i=0;i<stat.length;i++)if(stat[i]>0) pq.push(new Node((byte) i,stat[i]));
        while (pq.size()!=1)pq.push(new Node(pq.pop(),pq.pop()));
        return pq.pop();
    }

    private static final class BitCode{
        Bit[] code;
        BitCode(Bit[] code){
            this.code=code;
        }

        @Override
        public String toString() {
            return Arrays.toString(code);
        }
    }
    private static final class Node implements Comparable<Node>{
        final byte symbol;
        final int priority;
        Node left;
        Node right;

        private Node(byte symbol, int priority) {
            this.symbol = symbol;
            this.priority=priority;
        }
        private Node(Node left,Node right){
            symbol=0;
            this.left=left;
            this.right=right;
            priority=left.priority+right.priority;
        }

        @Override
        public int compareTo(@NotNull Node o) {
            return Integer.compare(priority,o.priority);
        }
    }
}


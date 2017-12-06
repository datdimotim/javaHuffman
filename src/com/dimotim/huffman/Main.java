package com.dimotim.huffman;

import java.io.*;
import java.util.Arrays;


import static com.dimotim.huffman.Constants.*;

class Constants {
    static final byte[] masks = {1, 2, 4, 8, 16, 32, 64, (byte) 128};
    enum Bit {ZERO, ONE}
}

class BitInputStream implements AutoCloseable {
    private int ind = 8;
    private byte buf;
    private final InputStream is;

    BitInputStream(InputStream is) {
        this.is = is;
    }

    Bit readBit() throws IOException {
        if (ind > 7) {
            ind = 0;
            int next = is.read();
            if (next == -1) throw new EOFException();
            buf = (byte) next;
        }
        return (masks[ind++] & buf) == 0 ? Bit.ZERO : Bit.ONE;
    }

    byte readByte() throws IOException {
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

class BitOutputStream implements AutoCloseable {
    private int ind = 0;
    private byte buf = 0;
    private final OutputStream os;

    BitOutputStream(OutputStream os) {
        this.os = os;
    }

    void writeBit(Bit bit) throws IOException {
        if (ind > 7) {
            ind = 0;
            os.write(buf);
            buf = 0;
        }
        buf |= (bit.ordinal() << (ind++));
    }

    void writeByte(byte b) throws IOException {
        for (int i = 7; i >= 0; i--) {
            Bit bit = ((b & masks[i]) == 0) ? Bit.ZERO : Bit.ONE;
            writeBit(bit);
        }
    }

    @Override
    public void close() throws IOException {
        if (ind != 0) os.write(buf);
        os.close();
    }
}

class Node {
    private final byte symbol;
    private long weight;
    private Node parent;
    private Node left;
    private Node right;
    private Node next;
    private Node back;

    private Node(byte symbol, long weight, Node parent, Node left, Node right) {
        this.symbol = symbol;
        this.weight = weight;
        this.parent = parent;
        this.left = left;
        this.right = right;
    }

    private static boolean isLeft(Node node) {
        return node == node.parent.left;
    }

    private static void resolve(Node problem) {
        Node next = problem.next;
        Node target = next;
        while ((next = next.next).weight < problem.weight)
            if (next != problem.parent) target = next;

        Node parentTarget = target.parent;
        Node parentProblem = problem.parent;
        if (isLeft(target)) parentTarget.left = problem;
        else parentTarget.right = problem;
        if (isLeft(problem)) parentProblem.left = target;
        else parentProblem.right = target;
        problem.parent = parentTarget;
        target.parent = parentProblem;

        Node tn = target.next;
        target.next = problem.next;
        target.next.back = target;
        problem.next = tn;
        problem.next.back = problem;

        Node tb = target.back;
        target.back = problem.back;
        target.back.next = target;
        problem.back = tb;
        problem.back.next = problem;
    }

    private static void incrementWeight(Node n) {
        n.weight++;
        if (n.parent == null) return;
        Node next = n.next;
        if (next == n.parent) next = next.next;
        if (next != null && next.weight < n.weight) resolve(n);
        incrementWeight(n.parent);
    }

    private static Node splitESCSymbol(Node esc, byte symbol) {
        Node root = new Node((byte) 0,
                esc.weight,
                esc.parent,
                esc,
                new Node(symbol, 1, null, null, null)
        );
        if (esc.parent != null) {
            if (isLeft(esc)) root.parent.left = root;
            else root.parent.right = root;
        }
        esc.parent = root;
        root.right.parent = root;

        root.next = esc.next;
        if (root.next != null) root.next.back = root;
        root.back = root.right;
        root.back.next = root;
        esc.next = root.back;
        esc.next.back = esc;

        incrementWeight(root);
        return root.right;
    }

    private static void symbolToCodeStream(Node s,BitOutputStream os) throws IOException {
        if(s.parent==null)return;
        symbolToCodeStream(s.parent,os);
        if(isLeft(s))os.writeBit(Bit.ZERO);
        else os.writeBit(Bit.ONE);
    }

    private static Node initESC() {
        return new Node((byte) 0, 0, null, null, null);
    }

    public static void encode(InputStream in, OutputStream codeStd) throws IOException {
        if (in.available() == 0) return;
        BitOutputStream code = new BitOutputStream(codeStd);
        Node[] symbols = new Node[256];
        Node esc = initESC();

        final byte firstbyte = (byte) in.read();
        code.writeByte(firstbyte);
        symbols[128+firstbyte] = splitESCSymbol(esc, firstbyte);

        while (in.available() != 0) {
            byte current = (byte) in.read();
            if (symbols[128+current] == null) {
                symbolToCodeStream(esc, code);
                symbols[128+current] = splitESCSymbol(esc, current);
                code.writeByte(current);
            } else {
                symbolToCodeStream(symbols[128+current],code);
                incrementWeight(symbols[128+current]);
            }
        }

        symbolToCodeStream(esc,code);
        code.writeByte(firstbyte);

        code.close();
        codeStd.flush();
    }

    private static Node symbolByCodeStream(Node root, BitInputStream code) throws IOException {
        while (true) {
            if (root.left == null) return root;
            byte c = (byte) code.readBit().ordinal();
            if (c == 0) root = root.left;
            else root = root.right;
        }
    }

    public static void decode(InputStream inStd, OutputStream code) throws IOException {
        if(inStd.available()==0)return;
        BitInputStream in = new BitInputStream(inStd);
        boolean[] symbols = new boolean[256];
        Node esc = initESC();

        final byte firstSymbol = in.readByte();
        splitESCSymbol(esc, firstSymbol);
        Node root = esc.parent;
        code.write(firstSymbol);

        while (true) {
            Node symbol = symbolByCodeStream(root, in);
            if (symbol != esc) {
                code.write(symbol.symbol);
                incrementWeight(symbol);
            } else {
                byte newSymbol = in.readByte();
                if (newSymbol == firstSymbol) break;
                if (symbols[128+newSymbol]) throw new RuntimeException();
                symbols[128+ newSymbol] = true;
                code.write(newSymbol);
                splitESCSymbol(esc, newSymbol);
            }
        }
        code.flush();
    }
}

public class Main {

    public static void main(String[] args) throws Exception {
        test(new FileInputStream("/home/dimotim/Рабочий стол/vim 30M"));
    }

    public static void test(InputStream file) throws IOException {
        byte[] fileBytes=file.readAllBytes();
        ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        ByteArrayInputStream inputFile=new ByteArrayInputStream(fileBytes);
        ByteArrayOutputStream decoded=new ByteArrayOutputStream();

        while (true) {
            encoded.reset();
            inputFile.reset();
            decoded.reset();
            long encStart=System.currentTimeMillis();
            Node.encode(inputFile, encoded);
            System.out.println("endode time="+(System.currentTimeMillis()-encStart)+"ms");
            ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
            long decStart=System.currentTimeMillis();
            Node.decode(inputCode, decoded);
            System.out.println("decode time="+(System.currentTimeMillis()-decStart)+"ms");
            if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("unmatch!");
        }
    }
}

package com.dimotim.huffman;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import stream.SimpleBitInputStream;
import stream.SimpleBitOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AdaptiveHuffmanAlgorithm {
    @Contract(pure = true)
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

    private static void symbolToCodeStream(Node s, BitOutputStream os) throws IOException {
        if(s.parent==null)return;
        symbolToCodeStream(s.parent,os);
        if(isLeft(s))os.writeBit(Constants.Bit.ZERO);
        else os.writeBit(Constants.Bit.ONE);
    }

    @NotNull
    private static Node initESC() {
        return new Node((byte) 0, 0, null, null, null);
    }

    public static void encode(InputStream in, OutputStream codeStd) throws Exception {
        if (in.available() == 0) return;
        BitOutputStream code = new SimpleBitOutputStream(codeStd);
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
        BitInputStream in = new SimpleBitInputStream(inStd);
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

    private static final class Node {
        final byte symbol;
        long weight;
        Node parent;
        Node left;
        Node right;
        Node next;
        Node back;

        Node(byte symbol, long weight, Node parent, Node left, Node right) {
            this.symbol = symbol;
            this.weight = weight;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
    }
}


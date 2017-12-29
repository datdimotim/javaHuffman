package com.dimotim.huffman_test;

import com.dimotim.huffman.AdaptiveHuffmanAlgorithm;
import com.dimotim.huffman.StaticHuffmanAlgorithm;
import com.dimotim.java8Compability.InputStreamReadAllBytes;

import java.io.*;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println("TESTS");
        applyOnSet(new File("resources"), file -> {
            System.out.print("\tstatic: ");
            test(StaticHuffmanAlgorithm::encode,StaticHuffmanAlgorithm::decode,file);
            System.out.print("\tadaptive: ");
            test(AdaptiveHuffmanAlgorithm::encode,AdaptiveHuffmanAlgorithm::decode,file);
        });

        System.out.println("\nCOMPARE LENGTH");
        applyOnSet(new File("resources"), Test::compareLength);

        System.out.println("\nBENCHMARKS");
        System.out.println("static");
        benchmark("resources/vim 30M",StaticHuffmanAlgorithm::encode,StaticHuffmanAlgorithm::decode,10);
        System.out.println("\nadaptive");
        benchmark("resources/vim 30M",AdaptiveHuffmanAlgorithm::encode,AdaptiveHuffmanAlgorithm::decode,10);
    }

    public static void compareLength(String name) throws Exception {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        byte[] input= InputStreamReadAllBytes.readAllBytes(new FileInputStream(name));
        StaticHuffmanAlgorithm.encode(new ByteArrayInputStream(input),baos);
        System.out.println("\traw file "+input.length);
        System.out.println("\tstatic "+baos.toByteArray().length);//18262224

        baos.reset();
        AdaptiveHuffmanAlgorithm.encode(new ByteArrayInputStream(input),baos);
        System.out.println("\tadaptive "+baos.toByteArray().length);
    }

    public static void test(Encoder encoder, Decoder decoder, String fileName) throws Exception {
        FileInputStream file=new FileInputStream(fileName);
        byte[] fileBytes = InputStreamReadAllBytes.readAllBytes(file);
        file.close();

        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        ByteArrayInputStream inputFile = new ByteArrayInputStream(fileBytes);
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();

        encoder.encode(inputFile, encoded);
        ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
        decoder.decode(inputCode, decoded);
        if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("unmatch!");
        else System.out.println("ok");
    }

    public static void applyOnSet(File folder, Tester tester) throws Exception {
        for (File f:folder.listFiles()){
            System.out.println("file: "+f.getName());
            tester.process(folder+"/"+f.getName());
            System.out.println();
        }
    }
    public interface Tester{
        void process(String file) throws Exception;
    }
    public interface Encoder{
        void encode(InputStream in, OutputStream out)throws Exception;
    }
    public interface Decoder{
        void decode(InputStream in, OutputStream out)throws IOException;
    }
    public static void benchmark(String file, Encoder encoder, Decoder decoder, int count) throws Exception {
        InputStream file_f=new FileInputStream(file);
        byte[] fileBytes= InputStreamReadAllBytes.readAllBytes(file_f);
        file_f.close();
        ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        ByteArrayInputStream inputFile=new ByteArrayInputStream(fileBytes);
        ByteArrayOutputStream decoded=new ByteArrayOutputStream();

        for(int i=0;i<count;i++) {
            encoded.reset();
            inputFile.reset();
            decoded.reset();
            long encStart=System.currentTimeMillis();
            encoder.encode(inputFile, encoded);
            System.out.println("encode time="+(System.currentTimeMillis()-encStart)+"ms");
            ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
            long decStart=System.currentTimeMillis();
            decoder.decode(inputCode, decoded);
            System.out.println("decode time="+(System.currentTimeMillis()-decStart)+"ms");
            if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("unmatch!");
        }
    }

}
package test;

import com.dimotim.huffman.AdaptiveHuffmanAlgorithm;
import com.dimotim.huffman.PriorityQueue;
import com.dimotim.huffman.StaticHuffmanAlgorithm;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws Exception {
        test();
        //benchmarkStatic();
        benchmarkAdaptive();
    }
    public static void test() throws Exception {
        System.out.println("static");
        testSet(StaticHuffmanAlgorithm::encode,StaticHuffmanAlgorithm::decode,new File("tests").listFiles());
        System.out.println("\nadaptive:");
        testSet(AdaptiveHuffmanAlgorithm::encode,AdaptiveHuffmanAlgorithm::decode,new File("tests").listFiles());
        System.out.println();
    }
    public static void benchmarkStatic() throws Exception {
        benchmark(new FileInputStream("tests/vim 30M"),StaticHuffmanAlgorithm::encode,StaticHuffmanAlgorithm::decode);
    }
    public static void benchmarkAdaptive() throws Exception {
        benchmark(new FileInputStream("tests/vim 30M"),AdaptiveHuffmanAlgorithm::encode,AdaptiveHuffmanAlgorithm::decode);
    }

    static void testSet(Encoder encoder, Decoder decoder, File... files) throws Exception {
        for(File fileName:files) {
            FileInputStream file=new FileInputStream(fileName);
            byte[] fileBytes = file.readAllBytes();
            file.close();

            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            ByteArrayInputStream inputFile = new ByteArrayInputStream(fileBytes);
            ByteArrayOutputStream decoded = new ByteArrayOutputStream();

            encoder.encode(inputFile, encoded);
            ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
            decoder.decode(inputCode, decoded);
            if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("file: "+fileName+" unmatch!");
            else System.out.println("file: "+fileName+" ok");
        }
    }
    public static void testPriorityQueue(){
        Random r=new Random();
        int[] m=new int[10];
        for(int i=0;i<m.length;i++)m[i]=r.nextInt(4);
        PriorityQueue<Integer> pq=new PriorityQueue<>(m.length,Integer[]::new);
        for(int i=0;i<m.length;i++)pq.push(m[i]);

        Integer prev=pq.pop();
        System.out.println("prior="+prev);
        while (0!=pq.size()){
            Integer cur=pq.pop();
            System.out.println("prior="+cur);
        }
    }

    public interface Encoder{
        void encode(InputStream in, OutputStream out)throws Exception;
    }
    public interface Decoder{
        void decode(InputStream in, OutputStream out)throws IOException;
    }
    public static void benchmark(InputStream file, Encoder encoder, Decoder decoder) throws Exception {
        byte[] fileBytes=file.readAllBytes();
        ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        ByteArrayInputStream inputFile=new ByteArrayInputStream(fileBytes);
        ByteArrayOutputStream decoded=new ByteArrayOutputStream();

        while (true) {
            encoded.reset();
            inputFile.reset();
            decoded.reset();
            long encStart=System.currentTimeMillis();
            encoder.encode(inputFile, encoded);
            System.out.println("endode time="+(System.currentTimeMillis()-encStart)+"ms");
            ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
            long decStart=System.currentTimeMillis();
            decoder.decode(inputCode, decoded);
            System.out.println("decode time="+(System.currentTimeMillis()-decStart)+"ms");
            if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("unmatch!");
        }
    }
}
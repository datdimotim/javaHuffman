package test;

import com.dimotim.huffman.AdaptiveHuffmanAlgorithm;
import com.dimotim.huffman.PriorityQueue;
import com.dimotim.huffman.StaticHuffmanAlgorithm;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Test {



    public static void main(String[] args) throws Exception {
        //testPriorityQueue();
        testStatic();

        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        AdaptiveHuffmanAlgorithm.encode(new FileInputStream("vim 30M"),bos);
        System.out.println("dynamic code lenght= "+bos.toByteArray().length);
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
            //if(cur.priority==prev.priority&&cur.elem>prev.elem)throw new RuntimeException();
            prev=cur;
        }
    }

    public static void testStatic() throws Exception {
        byte[] save=new FileInputStream("vim 30M").readAllBytes();
        ByteArrayInputStream inputMesg=new ByteArrayInputStream(save);
        ByteArrayOutputStream outputCode=new ByteArrayOutputStream();
        StaticHuffmanAlgorithm.encode(inputMesg,outputCode);

        ByteArrayInputStream inputCode=new ByteArrayInputStream(outputCode.toByteArray());
        ByteArrayOutputStream outputMesg=new ByteArrayOutputStream();
        StaticHuffmanAlgorithm.decode(inputCode,outputMesg);

        //System.out.println(Arrays.toString(save));
        //System.out.println(Arrays.toString(outputMesg.toByteArray()));
        System.out.println(Arrays.equals(save,outputMesg.toByteArray()));
        System.out.println("input lenght= "+save.length);
        System.out.println("encoded lenght= "+outputCode.toByteArray().length);
    }

    public static void testAdaptive(InputStream file) throws Exception {
        byte[] fileBytes=file.readAllBytes();
        ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        ByteArrayInputStream inputFile=new ByteArrayInputStream(fileBytes);
        ByteArrayOutputStream decoded=new ByteArrayOutputStream();

        while (true) {
            encoded.reset();
            inputFile.reset();
            decoded.reset();
            long encStart=System.currentTimeMillis();
            AdaptiveHuffmanAlgorithm.encode(inputFile, encoded);
            System.out.println("endode time="+(System.currentTimeMillis()-encStart)+"ms");
            ByteArrayInputStream inputCode = new ByteArrayInputStream(encoded.toByteArray());
            long decStart=System.currentTimeMillis();
            AdaptiveHuffmanAlgorithm.decode(inputCode, decoded);
            System.out.println("decode time="+(System.currentTimeMillis()-decStart)+"ms");
            if (!Arrays.equals(fileBytes, decoded.toByteArray())) throw new RuntimeException("unmatch!");
        }
    }
}
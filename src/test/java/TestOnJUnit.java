import com.dimotim.huffman.AdaptiveHuffmanAlgorithm;
import com.dimotim.huffman.StaticHuffmanAlgorithm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TestOnJUnit extends Assert{
    private File testFolder;
    @Before
    public void init(){
        testFolder=new File(getClass().getResource("tests").getPath());
        assertTrue(testFolder.exists());
    }
    @Test
    public void testStatic() throws Exception {
        com.dimotim.huffman_test.Test.applyOnSet(testFolder,file -> {
            com.dimotim.huffman_test.Test.test(StaticHuffmanAlgorithm::encode,StaticHuffmanAlgorithm::decode,file);
        });
    }
    @Test
    public void testAdaptive() throws Exception {
        com.dimotim.huffman_test.Test.applyOnSet(testFolder,file -> {
            com.dimotim.huffman_test.Test.test(AdaptiveHuffmanAlgorithm::encode,AdaptiveHuffmanAlgorithm::decode,file);
        });
    }
}

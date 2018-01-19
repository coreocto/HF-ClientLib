package org.coreocto.dev.hf.clientlib.sse.mces;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class McesClientTest {
    @Before
    public void setUp() throws Exception {

    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
//        GeneralizedSuffixTree suffixTree = new GeneralizedSuffixTree();
//        String word = "cocoon";
//        suffixTree.put(word, 0);
//        Collection<Integer> srhResult = suffixTree.search("coc");
//        System.out.println(srhResult);
//        System.out.println(suffixTree);
        McesClient client = new McesClient();
        client.Gen(16);
        client.Enc("cocoon", null, null, null);
    }
}

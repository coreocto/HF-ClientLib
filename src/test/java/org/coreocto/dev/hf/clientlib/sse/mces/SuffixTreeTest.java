package org.coreocto.dev.hf.clientlib.sse.mces;

import org.coreocto.dev.hf.commonlib.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SuffixTreeTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        String word = "cocoon";
        SuffixTree suffixTree = new SuffixTree();
        suffixTree.addString(word);

        List<String> allSubstr = Util.getSubstrings(word, false);
        for (int i = 0; i < allSubstr.size(); i++) {
            System.out.println("contain " + allSubstr.get(i) + "? " + suffixTree.contains(allSubstr.get(i)));
        }

        String word2 = "apple";

        suffixTree.addString(word2);

        List<String> allSubstr2 = Util.getSubstrings(word2, false);
        for (int i = 0; i < allSubstr2.size(); i++) {
            System.out.println("contain " + allSubstr2.get(i) + "? " + suffixTree.contains(allSubstr2.get(i)));
        }

        String word3 = "cow";

        List<String> allSubstr3 = Util.getSubstrings(word3, false);
        for (int i = 0; i < allSubstr3.size(); i++) {
            System.out.println("contain " + allSubstr3.get(i) + "? " + suffixTree.contains(allSubstr3.get(i)));
        }

        suffixTree.dfs(suffixTree.getRoot());
    }
}

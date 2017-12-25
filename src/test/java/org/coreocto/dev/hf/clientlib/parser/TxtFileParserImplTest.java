package org.coreocto.dev.hf.clientlib.parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringBufferInputStream;
import java.util.List;

public class TxtFileParserImplTest {

    private IFileParser fileParser = null;

    @Before
    public void setUp() throws Exception {
        fileParser = new TxtFileParserImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("i am to school by bus\nfat boy eat all day");
        List<String> words = fileParser.getText(in);
        int listSize = words.size();
        for (int i = 0; i < listSize; i++) {
            System.out.println(words.get(i));
        }
        in.close();
    }
}

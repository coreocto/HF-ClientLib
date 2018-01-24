package org.coreocto.dev.hf.clientlib.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShuffleTest {

    private List<Integer> list;

    @Before
    public void setUp() throws Exception {
        list = new ArrayList<>();

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {

        for (int i=0;i<10;i++){
            list.add(i);
        }

        Random rnd = new Random(0);

        Collections.shuffle(list, rnd);
        System.out.println(list);

        list.clear();
        for (int i=0;i<10;i++){
            list.add(i);
        }

        Collections.shuffle(list, rnd);
        System.out.println(list);
    }
}

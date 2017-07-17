package com.example.asadkhan.kurrencyapp;

import com.example.asadkhan.kurrencyapp.domain.GenericClassExample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testGenerix() throws Exception{

        GenericClassExample aExample = new GenericClassExample();
        aExample.BoxTest();
        aExample.PairTest();
    }

    @Test
    public void testReact() throws Exception{

        GenericClassExample aExample = new GenericClassExample();
        aExample.ReactTest();
    }
}
package com.bbva.mfid.fidcven.batch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for simple App.
 * Delete this class, is only for initial commit purposes
 * 
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */


    @Test
    public void testAppMessage()
    {
        App app = new App();
        assertEquals("Hello World!", app.getMessage());
    }
}
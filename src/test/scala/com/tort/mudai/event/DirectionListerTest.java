package com.tort.mudai.event;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DirectionListerTest {
    @Test
    public void testListDirections() throws Exception {
        final String directions = new DirectionLister().listDirections();

        assertEquals(directions, "запад|восток|север|юг|вверх|вниз");
    }
}

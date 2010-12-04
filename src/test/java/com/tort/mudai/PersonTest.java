package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test
public class PersonTest {
    Injector injector = Guice.createInjector(new MudaiModule());
    Person person = injector.getInstance(Person.class);

    @BeforeTest
    public void setUp() {
    }

    @AfterTest
    public void tearDown() {
    }

    public void create(){
        assertNotNull(person);
    }
}

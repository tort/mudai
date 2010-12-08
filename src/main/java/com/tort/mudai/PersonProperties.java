package com.tort.mudai;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PersonProperties {
    private String _login;
    private String _password;
    private static final String PROPERTIES_FILENAME = "mudai.properties";

    public PersonProperties(){
        try {
            final FileInputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
            final Properties properties = new Properties();
            properties.load(inputStream);

            _login = properties.getProperty("login");
            if(_login == null)
                throw new RuntimeException("login property is absent");

            _password = properties.getProperty("password");
            if(_password == null)
                throw new RuntimeException("password property is absent");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("couldn't find " + PROPERTIES_FILENAME);
        } catch (IOException e) {
            throw new RuntimeException("couldn't load " + PROPERTIES_FILENAME);
        }
    }

    public String getLogin() {
        return _login;
    }

    public String getPassword() {
        return _password;
    }
}

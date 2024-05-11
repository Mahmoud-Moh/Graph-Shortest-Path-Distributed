package org.example.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropValues{
    public static Properties getPropValues() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = GetPropValues.class.getClassLoader().getResourceAsStream("system.properties");
        if(inputStream != null){
            prop.load(inputStream);
        }else{
            throw new FileNotFoundException("property file \'system.properties\' not found in the classpath");
        }
        return prop;
    }

    public static String getRemoteObjectReference() throws IOException {
        Properties props = GetPropValues.getPropValues();
        String server = props.getProperty("GSP.server");
        String port = props.getProperty("GSP.rmiregistry.port");
        return "rmi://" + server + ":" + port + "/gsp";
    }

}

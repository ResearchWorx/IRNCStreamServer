package edu.uky.irnc.streamserver.core;

import java.io.File;
import java.io.IOException;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class Config {
    private Wini ini;

    public Config(String configFile) {
        try {
            ini = new Wini(new File(configFile));
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfig(String group, String setting) {
        return ini.get(group, setting);
    }
}
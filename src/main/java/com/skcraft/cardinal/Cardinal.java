package com.skcraft.cardinal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Cardinal {

    private static Cardinal instance;
    private final Properties properties;
    @Getter
    private final Injector injector;

    public static Cardinal load() throws CardinalFatalException {
        Cardinal local = Cardinal.instance;
        if (local != null) {
            return local;
        }
        synchronized (Cardinal.class) {
            local = Cardinal.instance;
            if (local != null) {
                return local;
            }
            Cardinal instance = new Cardinal();
            Cardinal.instance = instance;
            return instance;
        }
    }

    private Cardinal() throws CardinalFatalException {
        try {
            properties = readConfig();
        } catch (IOException e) {
            throw new CardinalFatalException("Could not read configuration file for Cardinal", e);
        }

        this.injector = Guice.createInjector(new CardinalModule(properties));
    }

    private Properties readConfig() throws IOException {
        File file = new File(System.getProperty("cardinal.configFile", "cardinal.properties"));
        Properties properties = new Properties();
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            properties.load(br);
        }
        return properties;
    }

    public <T> T getInstance(Key<T> key) {
        return injector.getInstance(key);
    }

    public <T> T getInstance(Class<T> type) {
        return injector.getInstance(type);
    }
}

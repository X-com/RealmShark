package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties class saving options to file and loads options when restarting.
 */
public class PropertiesManager {

    private static Properties properties;

    /*
     * Load the properties as the class loads.
     */
    static {
        properties = new Properties();
        try {
            FileReader reader = new FileReader("realmShark.properties");
            properties.load(reader);
        } catch (IOException ignored) {
        }
    }

    /**
     * Sets a preset needed when reloading the program.
     *
     * @param name  Name of the property.
     * @param value Value of the property.
     */
    public static void setProperties(String name, String value) {
        properties.setProperty(name, value);
        try {
            properties.store(new FileWriter("realmShark.properties"), "Realm shark properties");
        } catch (IOException ignored) {
        }
    }

    /**
     * Gets the property value by the name of the property.
     *
     * @param name Name of the property
     * @return Value of the property.
     */
    public static String getProperty(String name) {
        if (properties == null) return null;
        return properties.getProperty(name);
    }
}

package tomato.gui.character;

import org.xml.sax.SAXException;
import util.StringXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pet class
 */
public class Pet {

    private static final String PETS_XML_PATH = "assets/xml/pets.xml";
    private static HashSet<Integer> petIds;

    public static boolean isPet(int id) {
        return petIds.contains(id);
    }

    public static void load() {
        petIds = new HashSet<>();
        try {
            FileInputStream file = new FileInputStream(PETS_XML_PATH);
            String result = new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.joining("\n"));
            StringXML base = StringXML.getParsedXML(result);

            for (StringXML xml : base) {
                if (Objects.equals(xml.name, "Object")) {
                    StringXML type = xml.children.get(1);
                    int val = Integer.parseInt(type.value.substring(2), 16);
                    petIds.add(val);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}

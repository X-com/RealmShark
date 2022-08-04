package bugfixingtools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import util.Pair;
import util.Util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class XmlIdParser {
    static final String ROOTDIR = "D:\\Programmering\\himextractor\\exalt-extractor\\output";
    static final String name = "src\\main\\resources";
    static final ArrayList<Pair<String, String>> pairs = new ArrayList<>();
    static final ArrayList<String> fullList = new ArrayList<>();
    static final HashMap<Integer, String> hashList = new HashMap<>();

    public static void main(String[] args) {
        new XmlIdParser().run();
    }

    private void run() {
        System.out.println("clearconsole");
        Util.setSaveLogs(true);
        try {
            Files.walk(Paths.get(ROOTDIR)).filter(Files::isRegularFile).filter(p -> p.toString().endsWith("xml")).forEach(XmlIdParser::xml);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Collections.sort(pairs, Comparator.comparing(Pair::left));
//        for (Pair pair : pairs) {
//            System.out.printf("%s:%s\n", pair.left(), pair.right());
//        }
//        System.out.println(hashList.size());
    }

    private static void xml(Path path) {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc;
            try {
                doc = db.parse(new File(path.toAbsolutePath().toString()));
            } catch (SAXParseException e) {
                return;
            }
            // optional, but recommended
            doc.getDocumentElement().normalize();

            // get <staff>
            NodeList list = doc.getElementsByTagName("Object");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    String idID = element.getAttribute("id");
                    String typeID = element.getAttribute("type");
                    int id = Integer.decode(typeID);

                    NodeList displayID = element.getElementsByTagName("DisplayId");
                    NodeList clazzID = element.getElementsByTagName("Class");
                    NodeList groupID = element.getElementsByTagName("Group");
                    NodeList projectile = element.getElementsByTagName("Projectile");

                    String display = "";
                    String clazz = "";
                    String group = "";
                    String minDmg = "";
                    String maxDmg = "";
                    if (displayID.getLength() > 0) {
                        display = displayID.item(0).getTextContent();
                    }
                    if (clazzID.getLength() > 0) {
                        clazz = clazzID.item(0).getTextContent();
                    }
                    if (groupID.getLength() > 0) {
                        group = groupID.item(0).getTextContent();
                    }
                    StringBuilder pTemp = new StringBuilder();
                    boolean projectilesFound = false;
                    for (int i = 0; i < projectile.getLength(); i++) {
                        Node n = projectile.item(i);
                        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
                            Element e = (Element) n;
                            NodeList min = e.getElementsByTagName("MinDamage");
                            NodeList max = e.getElementsByTagName("MaxDamage");
                            NodeList armorPiercing = e.getElementsByTagName("ArmorPiercing");
                            if (min.getLength() > 0) {
                                minDmg = min.item(0).getTextContent();
                                projectilesFound = true;
                            }
                            if (max.getLength() > 0) {
                                maxDmg = max.item(0).getTextContent();
                                projectilesFound = true;
                            }
                            pTemp.append(minDmg).append(",").append(maxDmg).append(",").append(armorPiercing.item(0) != null ? "1," : "0,");
                        }
                    }
                    String projectileString = "";
                    if (projectilesFound) {
                        projectileString = pTemp.substring(0, pTemp.length() - 1);
                    }
//                    if (hashList.containsKey(id)) {
//                    }
                    String s = String.format("%d:%s:%s:%s:%s:%s", id, display, clazz, group, projectileString, idID);
                    Util.print(name, s);
//                    System.out.println(s);
//                    hashList.put(id, idID);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}

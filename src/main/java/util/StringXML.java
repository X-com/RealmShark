package util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple class to extract XML data from realm into a String tree to be read.
 */
public class StringXML implements Iterable<StringXML> {

    public String name;
    public String value;
    public short type;
    public StringXML parrent;
    public ArrayList<StringXML> children = new ArrayList<>();

    /**
     * A Parsing class for returning the base node created by parsing a XML string.
     *
     * @param rawXMLString XML string to be parsed.
     * @return Returns base tree node of the XML parsed tree.
     * @throws Exception Thrown if string can't be parsed or other issues.
     */
    public static StringXML getParsedXML(String rawXMLString) throws ParserConfigurationException, IOException, SAXException {
        Document doc = loadXMLFromString(rawXMLString);
        return next(doc.getDocumentElement(), null);
    }

    /**
     * Creates document builder from a string to parse the XML
     *
     * @param xml XML string to be parsed.
     * @return A ducment to be used to parse the XML string.
     * @throws Exception Thrown if the string is invalid
     */
    private static Document loadXMLFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    /**
     * Method to extract XML data into a tree with children contained in the arraylist.
     * All other data is represented as strings for both name and value.
     *
     * @param node        Current XML node traversing the XML data.
     * @param parrentNode Parrent XMLString object stored in the child.
     * @return Return the XMLString object to be added to the parrent.
     */
    private static StringXML next(Node node, StringXML parrentNode) {
        StringXML thisXMLString = new StringXML();

        thisXMLString.parrent = parrentNode;
        thisXMLString.name = node.getNodeName();
        thisXMLString.value = node.getNodeValue();
        thisXMLString.type = node.getNodeType();

        if (node.hasAttributes()) {
            NamedNodeMap attribNode = node.getAttributes();
            for (int i = 0; i < attribNode.getLength(); i++) {
                Node item = attribNode.item(i);

                StringXML newXMLString = new StringXML();

                newXMLString.parrent = thisXMLString;
                newXMLString.name = item.getNodeName();
                newXMLString.value = item.getNodeValue();
                newXMLString.type = item.getNodeType();

                thisXMLString.children.add(newXMLString);
            }
        }

        Node c = node.getFirstChild();
        if (c != null) thisXMLString.children.add(next(c, thisXMLString));

        Node s = node.getNextSibling();
        if (s != null) parrentNode.children.add(next(s, parrentNode));

        return thisXMLString;
    }

    @Override
    public Iterator iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return name + (value == null ? "" : " " + value);
    }
}

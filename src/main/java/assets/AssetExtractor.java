package assets;

import assets.resextractor.UnityExtractor;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import util.PropertiesManager;
import util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main loader for assets. If assets are missing or are outdated,
 * extracts the assets from realm resources files.
 */
public class AssetExtractor {

    public static final String ASSETS_OBJECT_FILE_DIR_PATH = "assets/ObjectID.list";
    public static final String ASSETS_TILE_FILE_DIR_PATH = "assets/TileID.list";
    private static final String XML_DIR_PATH = "assets/xml";
    private static final File[] ASSET_FOLDERS = {new File("assets/json/"), new File("assets/sprites/"), new File("assets/xml/")};
    private static final String REALM_RES_PATH = "/RealmOfTheMadGod/Production/RotMG Exalt_Data/resources.assets";

    /**
     * Main loader for realm assets.
     *
     * @param buildVersion
     */
    public static void checkForExtraction(String buildVersion) {
        if (checkUpdateAssets(buildVersion) != 0) {
            assetExtractionWindow(buildVersion);
        }
    }

    /**
     * GUI dialog options for extracting the assets.
     *
     * @param buildVersion Build version of realm.
     */
    private static void assetExtractionWindow(String buildVersion) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));

        JPanel lablePanel = new JPanel();
        lablePanel.setLayout(new BoxLayout(lablePanel, BoxLayout.X_AXIS));
        lablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(lablePanel);

        JLabel l1 = new JLabel("New update available");
        JLabel l2 = new JLabel("Assets are needed for some features");
        JLabel l3 = new JLabel("Extracting assets");
        l1.setBorder(BorderFactory.createEmptyBorder(5, 5, 1, 5));
        l2.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));
        l3.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));

        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        lablePanel.add(labels);
        labels.add(l1);
        labels.add(l2);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton extract = new JButton("Extract");
        JButton close = new JButton("Ignore");
        extract.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        close.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(extract);
        buttons.add(close);
        buttons.add(Box.createHorizontalGlue());

        mainPanel.add(buttons);

        JDialog dialog = new JDialog();
        close.addActionListener(e -> {
            dialog.dispose();
        });
        extract.addActionListener(e -> {
            extract.setEnabled(false);
            File f = assetFile();

            if (!f.exists()) {
                int i = JOptionPane.showOptionDialog(dialog, "Please select realm folder", "Realm folder not found", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{"Realm Folder", "Cancel"}, null);
                if (i == 1) {
                    dialog.dispose();
                } else if (i == 0) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    File path = FileSystemView.getFileSystemView().getDefaultDirectory();
                    while (true) {
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory(path);
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int returnVal = fc.showDialog(null, "Realm Folder");
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            path = fc.getSelectedFile();

                            try (Stream<Path> pathStream = Files.find(path.toPath(), 5, (p, basicFileAttributes) -> p.getFileName().toString().equalsIgnoreCase("resources.assets"))) {
                                List<Path> list = pathStream.collect(Collectors.toList());
                                if (list.size() == 1) {
                                    Path p = list.get(0);
                                    f = p.toFile();
                                    PropertiesManager.setProperties("realmResPath", f.getPath());
                                    break;
                                }
                            } catch (Exception ex) {
                                continue;
                            }
                        } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                            dialog.dispose();
                            return;
                        }
                    }
                }
            }

            labels.removeAll();
            buttons.removeAll();

            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            labels.add(l3);
            buttons.add(Box.createHorizontalGlue());
            close.setEnabled(false);
            close.setText("Done");
            buttons.add(close);
            buttons.add(Box.createHorizontalGlue());

            labels.revalidate();
            buttons.revalidate();
            dialog.repaint();

            File finalF = f;
            SwingUtilities.invokeLater(() -> {
                try {
                    extractAssets(finalF, buildVersion);
                    extractAssetsFromXML();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                labels.removeAll();
                labels.add(new JLabel("Done extracting assets."));
                labels.add(new JLabel("Restart app if assets are missing."));
                labels.revalidate();
                dialog.repaint();
                close.setEnabled(true);
            });
        });

        JOptionPane optionPane = new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        dialog.setTitle("Asset Extraction");
        dialog.setContentPane(optionPane);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Uses custom path to resources.assets file. If no custom path is found
     * finds Windows path to realm resources.assets file.
     *
     * @return Absolute path to resources.assets file.
     */
    public static File assetFile() {
        String p = PropertiesManager.getProperty("realmResPath");
        if (p != null) {
            return new File(p);
        }
        String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        return new File(path + REALM_RES_PATH);
    }

    /**
     * Extracts assets using the UnityExtractor from realms resrouces.assets file into assets folder.
     *
     * @param file         File path to resrouces.assets.
     * @param buildVersion Build version of current realm client.
     */
    private static void extractAssets(File file, String buildVersion) throws IOException {
        new UnityExtractor().extract(file, ASSET_FOLDERS);
        PropertiesManager.setProperties("buildVersion", buildVersion);
    }

    /**
     * Checks if asset folders exist and current build version is matching.
     *
     * @param buildVersion Build version of current realm client.
     * @return True if assets are missing.
     */
    private static int checkUpdateAssets(String buildVersion) {
        if (!new File(ASSETS_OBJECT_FILE_DIR_PATH).exists()) return 1;
        if (!new File(ASSETS_TILE_FILE_DIR_PATH).exists()) return 2;
        if (!Objects.equals(PropertiesManager.getProperty("buildVersion"), buildVersion)) return 3;
        return 0;
    }

    /**
     * Extracts assets from XML files.
     */
    private static void extractAssetsFromXML() {
        ArrayList<AssetObject> objectAssets = new ArrayList<>();
        ArrayList<AssetTile> tileAssets = new ArrayList<>();

        try {
            Files.walk(Paths.get(XML_DIR_PATH)).filter(Files::isRegularFile).filter(p -> p.toString().endsWith("xml")).forEach(path -> {
                try {
                    parseXML(path, objectAssets, tileAssets);
                } catch (ParserConfigurationException | IOException | SAXException e) {
//                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        objectAssets.sort(Comparator.comparing(a -> a.id));
        objectAssets.forEach(e -> Util.print(ASSETS_OBJECT_FILE_DIR_PATH + "-", e.toString()));

        tileAssets.sort(Comparator.comparing(a -> a.id));
        tileAssets.forEach(e -> Util.print(ASSETS_TILE_FILE_DIR_PATH + "-", e.toString()));

        reloadAssetsOnRunningApp();
    }

    /**
     * Reloads assets to reset assets in running app.
     */
    private static void reloadAssetsOnRunningApp() {
        ImageBuffer.clear();
        IdToAsset.reloadAssets();
        SpriteJson.jsonFileReader();
    }

    /**
     * Creates a Document object from XML files given by path.
     *
     * @param path Path to a XML file.
     * @return Returns Document object.
     */
    private static Document getDocumentElement(Path path) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(path.toAbsolutePath().toString()));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Root method for parsing XML files.
     *
     * @param path         Path to an XML file.
     * @param objectAssets Object assets array to store parsed XML objects into.
     * @param tileAssets   Tile asset array to st store parsed XML tiles into.
     */
    private static void parseXML(Path path, ArrayList<AssetObject> objectAssets, ArrayList<AssetTile> tileAssets) throws ParserConfigurationException, IOException, SAXException {
        Document doc = getDocumentElement(path);

        NodeList listObjects = doc.getElementsByTagName("Object");
        for (int i = 0; i < listObjects.getLength(); i++) {
            Node node = listObjects.item(i);
            AssetObject ao = new AssetObject();

            if (node.hasAttributes()) {
                addAttributes(node, ao);
            }

            parseChildObjects(node, ao);
            objectAssets.add(ao);
        }

        NodeList listTiles = doc.getElementsByTagName("Ground");
        for (int i = 0; i < listTiles.getLength(); i++) {
            Node node = listTiles.item(i);
            AssetTile at = new AssetTile();

            if (node.hasAttributes()) {
                addAttributes(node, at);
            }

            parseChildTiles(node, at);
            tileAssets.add(at);
        }
    }

    /**
     * Parses attributes from XML objects
     *
     * @param node Node of parse attributes
     * @param ao   Asset objects to store the parsed data into.
     */
    private static void addAttributes(Node node, Asset ao) {
        NamedNodeMap attribNode = node.getAttributes();
        for (int j = 0; j < attribNode.getLength(); j++) {
            Node item = attribNode.item(j);
            String name = item.getNodeName();
            String value = item.getNodeValue();

            if (name.equals("id")) {
                ao.idName = value;
            } else if (name.equals("type")) {
                ao.id = Integer.decode(value);
            }
        }
    }

    /**
     * Node parsing method to iterate over the sibling nodes and parse using lambdas.
     *
     * @param node The node to be parsed.
     * @param al   Asset lambda object for pasing the node data.
     */
    public static void addNode(Node node, AssetLambda al) {
        Node n = node.getFirstChild();
        while (n != null) {
            String name = n.getNodeName();
            String value = "";
            if (n.hasChildNodes()) value = n.getFirstChild().getNodeValue();

            al.parse(name, value, n);

            n = n.getNextSibling();
        }
    }

    /**
     * Child nodes of object data to parse.
     *
     * @param node The node to be parsed.
     * @param ao   Asset objects to store the parsed data into.
     */
    private static void parseChildObjects(Node node, AssetObject ao) {
        addNode(node, (name, value, n) -> {
            switch (name) {
                case "DisplayId":
                    ao.display = value;
                    break;
                case "Class":
                    ao.clazz = value;
                    break;
                case "Group":
                    ao.group = value;
                    break;
                case "Labels":
                    ao.labels = value;
                    break;
                case "Tier":
                    ao.tier = value;
                    break;
                case "Projectile":
                    addProjectile(n, ao);
                    break;
                case "Texture":
                case "AnimatedTexture":
                    addTexture(n, ao);
                    break;
            }
        });
    }

    /**
     * Child nodes of tile data to parse.
     *
     * @param node The node to be parsed.
     * @param at   Asset objects to store the parsed data into.
     */
    private static void parseChildTiles(Node node, AssetTile at) {
        addNode(node, (name, value, n) -> {
            switch (name) {
                case "Texture":
                    addTexture(n, at);
                    break;
                case "RandomTexture":
                    if (n.hasChildNodes()) {
                        parseChildTiles(n, at);
                    }
                    break;
            }
        });
    }

    /**
     * Projectile nodes of the main object data to be parsed.
     *
     * @param node The node to be parsed.
     * @param ao   Asset objects to store the parsed data into.
     */
    private static void addProjectile(Node node, AssetObject ao) {
        AssetProjectile projectile = new AssetProjectile();
        addNode(node, (name, value, n) -> {
            switch (name) {
                case "MinDamage":
                    projectile.min = value;
                    break;
                case "MaxDamage":
                    projectile.max = value;
                    break;
                case "ArmorPiercing":
                    projectile.peirce = true;
                    break;
            }
        });
        if (projectile.min != null && projectile.max != null) {
            if (ao.projectiles == null) ao.projectiles = new ArrayList<>();
            ao.projectiles.add(projectile);
        }
    }

    /**
     * Texture nodes of the main object data to be parsed.
     *
     * @param node The node to be parsed.
     * @param ao   Asset objects to store the parsed data into.
     */
    private static void addTexture(Node node, Asset ao) {
        AssetTexture texture = new AssetTexture();
        addNode(node, (name, value, n) -> {
            switch (name) {
                case "Index":
                    texture.index = value.startsWith("0x") ? Integer.decode(value) : Integer.parseInt(value);
                    break;
                case "File":
                    texture.file = value;
                    break;
            }
        });
        if (texture.file != null && texture.index != -1) {
            if (ao.textures == null) ao.textures = new ArrayList<>();
            ao.textures.add(texture);
        }
    }

    /**
     * Lambda function used to parse the XML data.
     */
    private interface AssetLambda {
        void parse(String name, String a, Node n);
    }

    /**
     * Superclass to store XML parsed object and tile data into.
     */
    private static class Asset {
        int id;
        String idName = "";

        ArrayList<AssetTexture> textures;
    }

    /**
     * Class to store XML parsed object data into.
     */
    private static class AssetObject extends Asset {

        String display = "";
        String clazz = "";
        String group = "";
        String labels = "";
        String tier = "";

        ArrayList<AssetProjectile> projectiles;

        @Override
        public String toString() {
            if (clazz != null && clazz.equals("Equipment") && labels.contains("UT")) {
                idName = "UT " + idName;
            } else if (clazz != null && clazz.equals("Equipment") && !tier.equals("")) {
                idName = "T" + tier + " " + idName;
            }

            StringBuilder projectileString = new StringBuilder();
            if (projectiles != null) {
                for (AssetProjectile p : projectiles) {
                    projectileString.append(p);
                }
                projectileString.deleteCharAt(projectileString.length() - 1);
            }

            StringBuilder textureString = new StringBuilder();
            if (textures != null) {
                for (AssetTexture p : textures) {
                    textureString.append(p);
                }
                textureString.deleteCharAt(textureString.length() - 1);
            }

            return String.format("%s;%s;%s;%s;%s;%s;%s", id, display, clazz, group, projectileString, textureString, idName);
        }
    }

    /**
     * Class to store XML parsed tile data into.
     */
    private static class AssetTile extends Asset {

        @Override
        public String toString() {
            StringBuilder textureString = new StringBuilder();
            if (textures != null) {
                for (AssetTexture p : textures) {
                    textureString.append(p);
                }
                textureString.deleteCharAt(textureString.length() - 1);
            }
            return String.format("%d;%s;%s", id, textureString, idName);
        }
    }

    /**
     * Class to store XML parsed projectile data into.
     */
    private static class AssetProjectile {
        String min;
        String max;
        boolean peirce = false;

        @Override
        public String toString() {
            return min + "," + max + "," + (peirce ? "1," : "0,");
        }
    }

    /**
     * Class to store XML parsed texture/sprite data into.
     */
    private static class AssetTexture {
        int index = -1;
        String file;

        @Override
        public String toString() {
            return index + "," + file + ",";
        }
    }
}
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
import java.nio.file.attribute.BasicFileAttributes;
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
     */
    public static void checkForExtraction() {
        String lastModifiedTime = lastEdited();
        if (checkUpdateAssets(lastModifiedTime) != 0) {
            assetExtractionWindow(lastModifiedTime);
        }
    }

    public static String lastEdited() {
        File file = assetFile();
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attr.lastModifiedTime().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * GUI dialog options for extracting the assets.
     *
     * @param lastModifiedTime Last modified time of the assets file.
     */
    private static void assetExtractionWindow(String lastModifiedTime) {
        JFrame frame = new JFrame("Realm Shark Asset Extractor");
        frame.setVisible(true);
        Object[] options = {"Extract",
                "Ignore"};
        int n = JOptionPane.showOptionDialog(frame,
                "New update available\n"
                        + "Assets are needed for some features?",
                "Asset Extractor",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,//do not use a custom Icon
                options,//the titles of buttons
                options[1]);//default button title
        if (n == 0) {
            File assetsFile = getAssetsFile();
            if (assetsFile != null) {
                waitWhileExtracting(assetsFile, lastModifiedTime);
            }
        }
        frame.dispose();
    }

    /**
     * Gets the asset file and if file is not found in default folder
     * opens a file dialog window for user to give location of assets file.
     *
     * @return The resources.assets file used for extraction
     */
    private static File getAssetsFile() {
        File f = assetFile();

        if (!f.exists()) {
            int i = JOptionPane.showOptionDialog(null, "Please select realm folder", "Realm folder not found", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{"Realm Folder", "Cancel"}, null);
            if (i == 0) {
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                        return null;
                    }
                }
            }
        }
        return f;
    }

    /**
     * Starts the extraction on the resources.assets file while
     * creates dialog window for user to wait.
     *
     * @param assetsFile       The resources.assets file to be extracted.
     * @param lastModifiedTime Last modified time used to keep track of updates on the assets file.
     */
    private static void waitWhileExtracting(File assetsFile, String lastModifiedTime) {
        JPanel panel = new JPanel(new BorderLayout());
        JButton ok = new JButton("OK");
        ok.setEnabled(false);
        JLabel text = new JLabel("Please wait while extracting.");
        panel.add(text, BorderLayout.CENTER);
        ok.addActionListener(e -> {
            Component component = (Component) e.getSource();
            if (component == null) {
                return;
            }
            Window win = SwingUtilities.getWindowAncestor(component);
            if (win == null) {
                return;
            }
            win.dispose();
        });
        JOptionPane pane = new JOptionPane("Extracting. Please wait.", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{ok}, ok);
        JDialog dialog = pane.createDialog(panel, "Extracting");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        new Thread(() -> {
            try {
                extractAssets(assetsFile, lastModifiedTime);
                extractAssetsFromXML();
                pane.setMessage("Finished extraction.");
                ok.setEnabled(true);
                System.out.println("done extracting.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

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
     * @param file             File path to resrouces.assets.
     * @param lastModifiedTime Last modified time of the assets file.
     */
    private static void extractAssets(File file, String lastModifiedTime) throws IOException {
        new UnityExtractor().extract(file, ASSET_FOLDERS);
        PropertiesManager.setProperties("lastModifiedTime", lastModifiedTime);
    }

    /**
     * Checks if asset folders exist and current build version is matching.
     *
     * @param lastModifiedTime Last modified time of the assets file.
     * @return True if assets are missing.
     */
    private static int checkUpdateAssets(String lastModifiedTime) {
        if (!new File(ASSETS_OBJECT_FILE_DIR_PATH).exists()) return 1;
        if (!new File(ASSETS_TILE_FILE_DIR_PATH).exists()) return 2;
        if (!Objects.equals(PropertiesManager.getProperty("lastModifiedTime"), lastModifiedTime)) return 3;
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
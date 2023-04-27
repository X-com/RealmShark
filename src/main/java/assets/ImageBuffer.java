package assets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Buffered image class used to crop images from the sprite atlas.
 */
public class ImageBuffer {

    private static HashMap<Integer, BufferedImage> images = new HashMap<>();
    private static HashMap<Integer, Integer> colors = new HashMap<>();
    private static final SpriteJson spriteJson = new SpriteJson();
    private static BufferedImage emptyImg;

    private static String[] spriteSheets = {"assets/sprites/groundTiles.png", "assets/sprites/characters.png", "assets/sprites/characters_masks.png", "assets/sprites/mapObjects.png"};
    private static BufferedImage[] bigImages = new BufferedImage[4];

    private static BufferedImage emptyImage() {
        if (emptyImg != null) return emptyImg;
        emptyImg = createTransparentBufferedImage(8, 8);
        return emptyImg;
    }

    /**
     * Image method used to get image from object id.
     *
     * @param id Type ID of the object.
     * @return Sprite of based of the object type ID.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    public static BufferedImage getImage(int id) throws IOException, AssetMissingException {
        if (id <= 0) return null;
//        if (images.containsKey(id)) return images.get(id);
        String name = IdToAsset.getObjectTextureName(id, 0);
        if (name == null) return emptyImage();
        int index = IdToAsset.getObjectTextureIndex(id, 0);
        int[] spriteData = spriteJson.getSpriteData(name, index);
        if (spriteData == null) return emptyImage();
        BufferedImage sprite = getSprite(spriteData);
        images.put(id, sprite);
        return sprite;
    }

    /**
     * Color method used to get most common color from tile id.
     *
     * @param id Type ID of the tile.
     * @return Most common color of the tile.
     */
    public static int getColor(int id) throws AssetMissingException {
        if (id <= 0) return 0;
        if (colors.containsKey(id)) return colors.get(id);
        if (!IdToAsset.tileIdExists(id)) return 0;
        String name = IdToAsset.getTileTextureName(id, 0);
        if (name == null) return 0;
        int index = IdToAsset.getTileTextureIndex(id, 0);
        int color = spriteJson.getSpriteColor(name, index);
        colors.put(id, color);
        return color;
    }

    /**
     * Retrieves the sprite from the sprite atlas based on image location on the atlas.
     *
     * @param data The coordinate, size and altas ID of the sprite being requested.
     * @return Buffered image of the sprite cropped from a sprite atlas.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    private static BufferedImage getSprite(int[] data) throws IOException { // data:{x, y, w, h, aId}
        if (data == null) return null;
        if (bigImages[data[4] - 1] == null) bigImages[data[4] - 1] = ImageIO.read(new File(spriteSheets[data[4] - 1]));

        return bigImages[data[4] - 1].getSubimage(data[0], data[1], data[2], data[3]);
    }

    /**
     * Resets the assets after loading new ones.
     */
    public static void clear() {
        images.clear();
        colors.clear();
        bigImages = new BufferedImage[4];
    }

    /**
     * Creates a transparent buffered image.
     *
     * @param width  Width of the transparent image.
     * @param height Height of the transparent image.
     * @return Transparent buffered image.
     */
    public static BufferedImage createTransparentBufferedImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(new Color(0, true));
        graphics.clearRect(0, 0, width, height);
        graphics.dispose();

        return bufferedImage;
    }

    /**
     * Getter for transparent 8x8 image
     */
    public static BufferedImage getEmptyImg() {
        return emptyImage();
    }
}

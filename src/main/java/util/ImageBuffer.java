package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Buffered image class used to crop images from the sprite atlas.
 */
public class ImageBuffer {

    private static HashMap<Integer, BufferedImage> images = new HashMap<>();
    private static final SpriteJson spriteJson = new SpriteJson();

    private static String[] spriteSheets = {"assets/tiles.png", "assets/characters.png", "assets/characters_masks.png", "assets/objects.png"};
    private static BufferedImage[] bigImages = new BufferedImage[4];

    /**
     * Image method used to get image from object id.
     *
     * @param id Type ID of the object.
     * @return Sprite of based of the object type ID.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    public static BufferedImage getImage(int id) throws IOException {
        if (id <= 0) return null;
        if (images.containsKey(id)) return images.get(id);
        String name = IdToName.getTextureName(id, 0);
        int index = IdToName.getTextureIndex(id, 0);
        int[] spriteData = spriteJson.getSprite(name, index);
        BufferedImage sprite = getSprite(spriteData);
        images.put(id, sprite);
        return sprite;
    }

    /**
     * Retrieves the sprite from the sprite atlas based on image location on the atlas.
     *
     * @param data The coordinate, size and altas ID of the sprite being requested.
     * @return Buffered image of the sprite cropped from a sprite atlas.
     * @throws IOException Thrown if the sprite atlas file is missing.
     */
    private static BufferedImage getSprite(int[] data) throws IOException { // data:{x, y, w, h, aId}
        if (bigImages[data[4] - 1] == null) bigImages[data[4] - 1] = ImageIO.read(new File(spriteSheets[data[4] - 1]));

        return bigImages[data[4] - 1].getSubimage(data[0], data[1], data[2], data[3]);
    }
}

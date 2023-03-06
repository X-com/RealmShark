package experimental.asset;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class UnityExtractor {

    public static void main(String[] args) {
        try {
            new UnityExtractor().extract();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File[] assetFiles() throws IOException {
        File[] f = new File[1];
        String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        f[0] = new File(path + "\\RealmOfTheMadGod\\Production\\t\\resources.assets");
//        f[1] = new File(path + "\\RealmOfTheMadGod\\Production\\t\\resources.assets.resS");
        if (!f[0].exists()) {
            throw new IOException("File resources.assets not found");
        }
//        if (!f[1].exists()) {
//            throw new IOException("File resources.assets.resS not found");
//        }
        return f;
    }

    public void extract() throws IOException {
        File[] files = assetFiles();
        Resources res = new Resources(files);

        File file = new File("assets/new/");
        if (file.mkdirs()) {
            System.out.println("Assets folder made");
        }

        extractXml(res);
        extractSpritesheetJson(res);
        extractSprites(res);
    }

    private void extractXml(Resources res) {
        for (TextAsset t : res.assetTextAsset) {
            if (!Arrays.asList(TextAsset.NON_XML_FILES).contains(t.name)) {
                File outputFile = new File("assets/new/" + t.name + ".xml");
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    outputStream.write(t.m_Script);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void extractSpritesheetJson(Resources res) {
        if (res.spritesheet != null) {
            File outputFile = new File("assets/new/spritesheet.json");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(res.spritesheet.m_Script);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractSprites(Resources res) {
        for (Texture2D t : res.assetTexture2D) {
            if (Arrays.asList(Texture2D.SPRITESHEET_NAMES).contains(t.name)) {
                File outputFile = new File("assets/new/" + t.name + ".png");
                int width = t.m_Width;
                int height = t.m_Height;
                byte[] data = t.image_data;

                DataBuffer buffer = new DataBufferByte(data, data.length);

                WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 4 * width, 4, new int[]{0, 1, 2, 3}, (Point) null);
                ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
                BufferedImage image = new BufferedImage(cm, raster, true, null);

                AffineTransform at = new AffineTransform();
                at.concatenate(AffineTransform.getScaleInstance(1, -1));
                at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
                BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = newImage.createGraphics();
                g.transform(at);
                g.drawImage(image, 0, 0, null);
                g.dispose();

                try {
                    ImageIO.write(newImage, "png", outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

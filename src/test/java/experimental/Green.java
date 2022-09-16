package experimental;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Green
{
    public static void main(String args[]) throws IOException
    {
        /* Read the image */
        BufferedImage bi= ImageIO.read(new File("image.jpg"));

        /* Loop through all the pixels */
        for (int x=0; x < bi.getWidth(); x++)
        {
            for (int y = 0; y < bi.getHeight(); y++)
            {
                /* Apply the green mask */
                bi.setRGB(x, y, bi.getRGB(x, y) & 0xff00ff00);
            }
        }

        /* Save the image */
        ImageIO.write(bi, "JPG", new File("green_mask.jpg"));
    }
}
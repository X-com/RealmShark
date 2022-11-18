package opengl;

import potato.model.Bootloader;
import potato.model.HeroLocations;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GLHeroes {
    private Texture texture;

    public GLHeroes() {
        BufferedImage[] heroes = Bootloader.loadHeroIcons();
        BufferedImage heroImages = new BufferedImage(72 * 13, 72, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < heroes.length; i++) {
            BufferedImage image = heroes[i];
            heroImages.getGraphics().drawImage(image, i * 72, 0, null);
        }
        texture = new Texture(heroImages, true);
    }

    public void drawHeros(GLRenderer renderer, ArrayList<HeroLocations> vaHero) {
        int height = 72;
        int halfH = height / 2;

        texture.bind();
        renderer.begin();
        for(HeroLocations hero : vaHero) {
            int width = 72;
            int halfW = width / 2;

            float drawX = hero.getX() - 1024;
            float drawY = 1024 - hero.getY();
//            GLColor c = GLColor.GREEN; // hero.getColor()
            GLColor c = new GLColor(hero.getColor());

            renderer.drawTextureRegion(texture, drawX - halfW, drawY - halfH, 72 * hero.getHeroTypeId(), 0, width, height, c);
        }
        renderer.end();
    }
}

package potato.view.opengl;

import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import potato.model.Bootloader;
import potato.model.HeroLocations;
import potato.view.OpenGLPotato;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static potato.view.OpenGLPotato.renderText;

public class GLHeroes {
    private Vector2f zerozero = new Vector2f(0, 0);

    public void drawHeros(ArrayList<HeroLocations> vaHero) {
        for (HeroLocations hero : vaHero) {
            float drawX = hero.getX() - 1024;
            float drawY = 1024 - hero.getY();
            Color c = hero.getPossibleSpawnColorLeft();
            Color d = hero.getPossibleSpawnColorRight();
            Vector4f colorText = new Vector4f(c.getRed(), c.getGreen(), c.getBlue(), 0.8f);
            Vector4f colorShapesL = new Vector4f(c.getRed(), c.getGreen(), c.getBlue(), 0.6f);

            Matrix4f pose = new Matrix4f().translate(drawX, drawY, 0);
            renderText.drawTextAligned(hero.getIndexString(), pose.scale(32), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorFont, colorText);

            if (c != d) {
                Vector4f colorShapesR = new Vector4f(d.getRed(), d.getGreen(), d.getBlue(), 0.6f);
                renderText.drawTextAligned(hero.shapeCharR(), pose.scale(4), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, colorShapesR);
                renderText.drawTextAligned(hero.shapeCharL(), pose.scale(1.5f), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, colorShapesL);
            } else {
                renderText.drawTextAligned(hero.shapeCharL(), pose.scale(4), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, colorShapesL);
            }
        }
    }
}

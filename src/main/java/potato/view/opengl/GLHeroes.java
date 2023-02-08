package potato.view.opengl;

import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import potato.model.Config;
import potato.model.HeroLocations;

import java.util.ArrayList;

import static potato.view.opengl.OpenGLPotato.renderText;

public class GLHeroes {
    private Vector2f zerozero = new Vector2f(0, 0);

    public void drawHeros(ArrayList<HeroLocations> vaHero) {
        for (HeroLocations hero : vaHero) {
            float drawX = hero.getX() - 1024;
            float drawY = 1024 - hero.getY();
            Vector4f c = hero.getPossibleSpawnColorMain();
            Vector4f colorText = new Vector4f(c.x(), c.y(), c.z(), Config.instance.textTransparency / 255f);

            Matrix4f pose = new Matrix4f().translate(drawX, drawY, 0);
            renderText.drawTextAligned(hero.getIndexString(), pose.scale(Config.instance.textSize), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorFont, colorText);

            Matrix4f poseShape = new Matrix4f().translate(drawX, drawY, 0);
            if (hero.multipleShapes()) {
                renderText.drawTextAligned(hero.shapeCharS(), poseShape.scale(Config.instance.shapeSize * 4), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, hero.getPossibleSpawnColorSecondary());
                renderText.drawTextAligned(hero.shapeCharM(), poseShape.scale(1.5f), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, hero.getPossibleSpawnColorMain());
            } else {
                renderText.drawTextAligned(hero.shapeCharM(), poseShape.scale(Config.instance.shapeSize * 4), zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, OpenGLPotato.vectorShapes, hero.getPossibleSpawnColorMain());
            }
        }
    }
}

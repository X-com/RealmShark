package potato.view.opengl;

import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import potato.model.Config;
import potato.model.HeroLocations;

import java.util.ArrayList;

import static potato.view.opengl.OpenGLPotato.renderShape;
import static potato.view.opengl.OpenGLPotato.renderText;

public class GLHeroes {
    private Vector2f zerozero = new Vector2f(0, 0);

    public void drawHeros(ArrayList<HeroLocations> vaHero, Matrix4f mvp) {
        for (HeroLocations hero : vaHero) {
            float drawX = hero.getX() - 1024;
            float drawY = 1024 - hero.getY();
            Vector4f c = hero.getPossibleSpawnColorMain();
            Vector4f colorText = new Vector4f(c.x(), c.y(), c.z(), Config.instance.textTransparency / 255f);
//String text, Matrix4f pose, Vector2f align, TextBoundType alignType, Vector4f color
            Matrix4f pose = new Matrix4f().translate(drawX, drawY, 0);
            renderText.drawText(hero.getIndexString(), pose.scale(Config.instance.textSize), mvp, zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, colorText);

            Matrix4f poseShape = new Matrix4f().translate(drawX, drawY, 0);
            if (hero.multipleShapes()) {
                renderShape.drawText(hero.shapeCharS(), poseShape.scale(Config.instance.shapeSize * 4), mvp, zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, hero.getPossibleSpawnColorSecondary());
                renderShape.drawText(hero.shapeCharM(), poseShape.scale(1.5f), mvp, zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, hero.getPossibleSpawnColorMain());
            } else {
                renderShape.drawText(hero.shapeCharM(), poseShape.scale(Config.instance.shapeSize * 4), mvp, zerozero, TextRenderer.TextBoundType.BOUNDING_BOX, hero.getPossibleSpawnColorMain());
            }
        }
    }
}

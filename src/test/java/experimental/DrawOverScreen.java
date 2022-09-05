package experimental;

//https://stackoverflow.com/questions/21604762/drawing-over-screen-in-java
public class DrawOverScreen {

}

/*
Window w=new Window(null)
{
  @Override
  public void paint(Graphics g)
  {
    final Font font = getFont().deriveFont(48f);
    g.setFont(font);
    g.setColor(Color.RED);
    final String message = "Hello";
    FontMetrics metrics = g.getFontMetrics();
    g.drawString(message,
      (getWidth()-metrics.stringWidth(message))/2,
      (getHeight()-metrics.getHeight())/2);
  }
  @Override
  public void update(Graphics g)
  {
    paint(g);
  }
};
w.setAlwaysOnTop(true);
w.setBounds(w.getGraphicsConfiguration().getBounds());
w.setBackground(new Color(0, true));
w.setVisible(true);
 */

/*
Window w=new Window(null)
{
  Shape shape;
  @Override
  public void paint(Graphics g)
  {
    Graphics2D g2d = ((Graphics2D)g);
    if(shape==null)
    {
      Font f=getFont().deriveFont(48f);
      FontMetrics metrics = g.getFontMetrics(f);
      final String message = "Hello";
      shape=f.createGlyphVector(g2d.getFontRenderContext(), message)
        .getOutline(
            (getWidth()-metrics.stringWidth(message))/2,
            (getHeight()-metrics.getHeight())/2);
      // Java6: com.sun.awt.AWTUtilities.setWindowShape(this, shape);
      setShape(shape);
    }
    g.setColor(Color.RED);
    g2d.fill(shape.getBounds());
  }
  @Override
  public void update(Graphics g)
  {
    paint(g);
  }
};
w.setAlwaysOnTop(true);
w.setBounds(w.getGraphicsConfiguration().getBounds());
w.setVisible(true);
 */
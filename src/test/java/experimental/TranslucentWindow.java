package experimental;

import potato.Potato;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class TranslucentWindow {

    public Point[] points;
    TranslucentPane translucentPane = new TranslucentPane();

    public static void main(String[] args) {
        new TranslucentWindow();
    }

    public TranslucentWindow() {
        points = new Point[100];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                }

                JWindow frame = new JWindow();
                frame.setAlwaysOnTop(true);
                frame.setSize(500, 400);
                frame.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            SwingUtilities.getWindowAncestor(e.getComponent()).dispose();
                        }
                    }
                });
                frame.setBackground(new Color(0, 0, 0, 0));
                URL input = Objects.requireNonNull(getClass().getResource("/potatoIcon.png"));
                frame.setContentPane(translucentPane);
                try {
                    BufferedImage io = ImageIO.read(input);
//                    frame.add(new JLabel(new ImageIcon(io)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                System.out.println("started");
            }
        });

        while(true) {

            for (int i = 0; i < points.length; i++) {
                points[i] = new Point((int) (Math.random() * 100), (int) (Math.random() * 100));
            }
            translucentPane.repaint();
        }
    }

    public class TranslucentPane extends JPanel {

        public TranslucentPane() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
//            g2d.setComposite(AlphaComposite.SrcOver.derive(0.85f));
            g2d.setColor(getBackground());
//            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.green);
//            g2d.setFont(new Font("Arial Black", Font.PLAIN, 100));
//            g2d.drawString("HAÖSKHJKAJHDGK", 0, 100);
            for (Point p : points) {
                g2d.drawOval(p.x, p.y, 10, 10);
            }

        }

    }

}
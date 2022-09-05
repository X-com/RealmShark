package experimental;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Test {

    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TestPane pane = new TestPane();
                JFrame frame = new JFrame("Test");
                frame.setLayout(new GridBagLayout());
                frame.setSize(100, 100);
                frame.setResizable(true);
                frame.setUndecorated(true);
                frame.add(pane);
                frame.setBackground(new Color(255, 0, 0, 128));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.toFront();

                Timer timer = new Timer(40, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        pane.render();
                    }
                });
                timer.start();
            }
        });
    }

    public class TestPane extends Canvas {

        public void render() {
            BufferStrategy bs = this.getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(3);
                return;
            }
            Graphics g = bs.getDrawGraphics();
            //////////////////////////////////////////////////
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 25, 25);
            //////////////////////////////////////////////////
            g.dispose();
            bs.show();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(50, 50);
        }

    }

}
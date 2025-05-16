package view.login;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoginPanel extends JPanel {
    private BufferedImage background;

    public LoginPanel() {
        setOpaque(true);
        setVisible(true);
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/loginbackground.jpg");
            if (inputStream != null) {
                background = ImageIO.read(inputStream);
                System.out.println("a" +
                        background.getWidth() + "x" + background.getHeight());
                repaint();
            } else {
                System.err.println("b");
            }
        } catch (IOException e) {
            System.err.println("c" + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
import model.MapModel;
import view.game.GameFrame;
import view.login.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create login frame
            LoginFrame loginFrame = new LoginFrame(280, 320);
            
            // Create game model and frame (initially hidden)
            MapModel mapModel = new MapModel();
            GameFrame gameFrame = new GameFrame(800, 600, mapModel);
            
            // Connect controller with game frame (for timer control)
            gameFrame.getController().setGameFrame(gameFrame);
            
            // Set game frame in login frame and show login
            gameFrame.setVisible(false);
            loginFrame.setGameFrame(gameFrame);
            loginFrame.setVisible(true);
        });
    }
}

package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.Border;
public class GamePanel extends ListenerPanel {
    private List<BoxComponent> boxes;
    private MapModel model;
    private GameController controller;
    private JLabel stepLabel;
    private int steps;
    private int GRID_SIZE;
    public BoxComponent selectedBox;
    private int horizontalPadding = 150;
    private int verticalPadding = 100;

    private boolean obstacleSelectionMode = false;
    private Image backgroundImage;

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.requestFocusInWindow();

        this.backgroundImage = new ImageIcon(getClass().getResource("/Chessboard.jpg")).getImage();

        int maxDimension = Math.max(model.getWidth(), model.getHeight());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int availableWidth = (int)(screenSize.width * 0.8);
        int availableHeight = (int)(screenSize.height * 0.8);

        int maxWidthGridSize = (availableWidth - 300) / model.getWidth();
        int maxHeightGridSize = (availableHeight - 200) / model.getHeight();

        int adaptiveGridSize = Math.min(maxWidthGridSize, maxHeightGridSize);
        adaptiveGridSize = Math.max(40, Math.min(70, adaptiveGridSize));
        GRID_SIZE = adaptiveGridSize;

        int boardWidth = model.getWidth() * GRID_SIZE;
        int boardHeight = model.getHeight() * GRID_SIZE;
        int exitSpace = GRID_SIZE + 20;
        int width = boardWidth + horizontalPadding * 2;
        int height = boardHeight + verticalPadding * 2 + exitSpace;
        Dimension panelSize = new Dimension(width + 20, height + 20);
        this.setPreferredSize(panelSize);
        this.setMinimumSize(panelSize);
        this.setSize(panelSize);
        this.model = model;
        this.selectedBox = null;
        this.validate();
        this.setBackground(new Color(240, 240, 255));

        try {
            initialGame();
        } catch (Exception e) {
            System.err.println("Error during initialGame():");
            e.printStackTrace();
            throw e;
        }
    }

    public void initialGame() {
        this.steps = 0;
        int[][] map = new int[model.getHeight()][model.getWidth()];
        for (int i = 0; i < model.getHeight(); i++) {
            for (int j = 0; j < model.getWidth(); j++) {
                map[i][j] = model.getId(i, j);
            }
        }
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 0) continue;

                BoxComponent box = null;
                int blockType = map[i][j];

                switch(blockType) {
                    case MapModel.CAO_CAO:
                        if (i < map.length - 1 && j < map[0].length - 1) {
                            box = new BoxComponent(Color.RED, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                        }
                        break;
                    case MapModel.GUAN_YU:
                        if (j < map[0].length - 1 && map[i][j] == MapModel.GUAN_YU && map[i][j+1] == MapModel.GUAN_YU) {
                            box = new BoxComponent(Color.ORANGE, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE);
                            map[i][j+1] = 0;
                        }
                        break;
                    case MapModel.GENERAL:
                        if (i < map.length - 1 && map[i][j] == MapModel.GENERAL && map[i+1][j] == MapModel.GENERAL) {
                            box = new BoxComponent(Color.BLUE, i, j);
                            box.setSize(GRID_SIZE, GRID_SIZE * 2);
                            map[i+1][j] = 0;
                        }
                        break;
                    case MapModel.SOLDIER:
                        box = new BoxComponent(Color.GREEN, i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        break;
                    case MapModel.ZHOU_YU:
                        if (j < map[0].length - 2) {
                            box = new BoxComponent(Color.MAGENTA, i, j);
                            box.setSize(GRID_SIZE * 3, GRID_SIZE);
                        }
                        break;
                    case MapModel.BLOCKED:
                        box = new BoxComponent(Color.DARK_GRAY, i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        box.setMovable(false);
                        break;
                    case -MapModel.BLOCKED:
                        box = new BoxComponent(new Color(200, 200, 200), i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        box.setMovable(true);
                        break;
                    case MapModel.MILITARY_CAMP:  // 仅修改此case
                        box = new BoxComponent(new Color(139, 69, 19), i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        box.setMovable(false);
                        // 加载并缩放军营图片
                        ImageIcon campIcon = new ImageIcon(getClass().getResource("/MILITARY_CAMP.jpg"));
                        Image scaledImage = campIcon.getImage().getScaledInstance(
                                GRID_SIZE, GRID_SIZE, Image.SCALE_SMOOTH);  // 适配格子大小
                        JLabel campLabel = new JLabel(new ImageIcon(scaledImage));
                        campLabel.setBounds(0, 0, GRID_SIZE, GRID_SIZE);  // 填满格子
                        box.setLayout(new BorderLayout());
                        box.add(campLabel, BorderLayout.CENTER);  // 用图片标签替代文字
                        box.setBackground(new Color(0, 0, 0, 0));  // 透明背景避免遮挡图片
                        break;
                }

                if (box != null) {
                    int boardWidth = model.getWidth() * GRID_SIZE;
                    int boardHeight = model.getHeight() * GRID_SIZE;
                    int xOffset = horizontalPadding;
                    int yOffset = verticalPadding;
                    if (xOffset < 100) xOffset = 100;
                    if (yOffset < 80) yOffset = 80;
                    int x = xOffset + j * GRID_SIZE;
                    int y = yOffset + i * GRID_SIZE;

                    switch (blockType) {
                        case MapModel.GUAN_YU:
                            if (box.getWidth() != GRID_SIZE * 2) {
                                box.setSize(GRID_SIZE * 2, GRID_SIZE);
                            }
                            break;
                        case MapModel.GENERAL:
                            if (box.getHeight() != GRID_SIZE * 2) {
                                box.setSize(GRID_SIZE, GRID_SIZE * 2);
                            }
                            break;
                        case MapModel.CAO_CAO:
                            if (box.getWidth() != GRID_SIZE * 2 || box.getHeight() != GRID_SIZE * 2) {
                                box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                            }
                            break;
                        case MapModel.ZHOU_YU:
                            if (box.getWidth() != GRID_SIZE * 3) {
                                box.setSize(GRID_SIZE * 3, GRID_SIZE);
                            }
                            break;
                    }

                    box.setLocation(x, y);
                    boxes.add(box);
                    this.add(box);
                    for (int r = i; r < i + box.getHeight()/GRID_SIZE; r++) {
                        for (int c = j; c < j + box.getWidth()/GRID_SIZE; c++) {
                            if (r < map.length && c < map[0].length) {
                                map[r][c] = 0;
                            }
                        }
                    }
                }
            }
        }
        this.repaint();
    }

    @Override
    public void doMouseClick(Point point) {
        Component component = this.getComponentAt(point);
        if (component instanceof BoxComponent clickedComponent) {
            view.game.GameFrame gameFrame = null;
            if (this.getParent() != null && this.getParent().getParent() instanceof view.game.GameFrame) {
                gameFrame = (view.game.GameFrame) this.getParent().getParent();
            }

            if (obstacleSelectionMode) {
                handleObstacleSelection(clickedComponent);
                return;
            }
            if (controller != null && gameFrame != null && gameFrame.propPanel != null) {
                boolean obstacleRemoverActive = gameFrame.propPanel.isObstacleRemoverActive();
                if (obstacleRemoverActive) {
                    handleObstacleRemover(clickedComponent, gameFrame);
                    return;
                }
            }

            handleNormalPieceSelection(clickedComponent);
        }
    }

    private void handleObstacleSelection(BoxComponent clickedComponent) {
        int row = clickedComponent.getRow();
        int col = clickedComponent.getCol();
        int pieceType = controller.getModel().getId(row, col);

        if (pieceType == MapModel.BLOCKED) {
            boolean success = controller.useObstacleRemoverProp(row, col);
            if (success) {
                setObstacleSelectionMode(false);
                resetBoard(controller.getModel().getMatrix());
                JOptionPane.showMessageDialog(this, "Obstacle successfully removed!", "Obstacle Removed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setObstacleSelectionMode(false);
            }
        } else {
            JOptionPane.showMessageDialog(this, "The obstacle remover can only be used on obstacles (gray blocks).", "Obstacle Remover", JOptionPane.INFORMATION_MESSAGE);
            setObstacleSelectionMode(false);
        }
    }

    private void handleObstacleRemover(BoxComponent clickedComponent, view.game.GameFrame gameFrame) {
        int row = clickedComponent.getRow();
        int col = clickedComponent.getCol();
        int pieceType = controller.getModel().getId(row, col);

        if (pieceType == MapModel.BLOCKED) {
            boolean success = controller.useObstacleRemoverProp(row, col);
            if (success) {
                gameFrame.propPanel.resetObstacleRemoverMode();
                resetBoard(controller.getModel().getMatrix());
                this.revalidate();
                this.repaint();
                JOptionPane.showMessageDialog(this, "Obstacle successfully removed!", "Obstacle Removed", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "The obstacle remover can only be used on obstacles (gray blocks).", "Obstacle Remover", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleNormalPieceSelection(BoxComponent clickedComponent) {
        if (selectedBox == null) {
            selectedBox = clickedComponent;
            selectedBox.setSelected(true);
        } else if (selectedBox != clickedComponent) {
            selectedBox.setSelected(false);
            clickedComponent.setSelected(true);
            selectedBox = clickedComponent;
        } else {
            clickedComponent.setSelected(false);
            selectedBox = null;
        }
    }

    private boolean isAnyBoxAnimating() {
        for (BoxComponent box : boxes) {
            if (box.isAnimating()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doMoveRight() {
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveLeft() {
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveUp() {
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveDown() {
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                afterMove();
            }
        }
    }

    public void afterMove() {
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", controller.getMoveCount()));
        }
    }

    public void setStepLabel(JLabel stepLabel) {
        this.stepLabel = stepLabel;
    }

    public void setController(GameController controller) {
        this.controller = controller;
        this.requestFocusInWindow();
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                System.out.println("GamePanel gained focus");
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                System.out.println("GamePanel lost focus");
            }
        });
    }

    public BoxComponent getSelectedBox() {
        return selectedBox;
    }

    public int getGRID_SIZE() {
        return GRID_SIZE;
    }

    public List<BoxComponent> getBoxes() {
        return boxes;
    }

    public void resetBoard(int[][] newMatrix) {
        for (BoxComponent box : boxes) {
            this.remove(box);
        }
        boxes.clear();
        this.model = new MapModel(newMatrix);
        initialGame();
    }

    private boolean exitHighlighted = false;
    private boolean caoHighlighted = false;

    public void highlightExit(boolean highlight) {
        this.exitHighlighted = highlight;
        this.repaint();
    }

    public void highlightCaoCao(boolean highlight) {
        this.caoHighlighted = highlight;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            try {
                int width = getWidth();
                int height = getHeight();

                boolean imageDrawn = g.drawImage(backgroundImage, 0, 0, width, height, this);

                if (!imageDrawn) {
                    System.err.println("背景图片未能成功绘制");
                }
            } catch (Exception e) {
                System.err.println("绘制背景图片时出错: " + e.getMessage());
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        Graphics2D g2d = (Graphics2D)g;
        int boardWidth = model.getWidth() * GRID_SIZE;
        int boardHeight = model.getHeight() * GRID_SIZE;
        int xOffset = horizontalPadding;
        int yOffset = verticalPadding;

        if (xOffset < 100) xOffset = 100;
        if (yOffset < 80) yOffset = 80;

        g2d.setColor(new Color(245, 222, 179, 0)); // 完全透明
        if (model.getWidth() == 5 && model.getHeight() == 4) {
            int topHeight = yOffset - 4;
            int bottomHeight = this.getHeight() - (yOffset + boardHeight + 4);
            g2d.fillRect(0, 0, this.getWidth(), topHeight);
            g2d.fillRect(0, topHeight, xOffset - 4, boardHeight + 8);
            g2d.fillRect(xOffset + boardWidth + 4, topHeight, this.getWidth() - (xOffset + boardWidth + 4), boardHeight + 8);
            g2d.fillRect(0, yOffset + boardHeight + 4, this.getWidth(), bottomHeight);
        } else {
            g2d.fillRect(0, 0, this.getWidth(), yOffset - 2);
            g2d.fillRect(0, yOffset - 2, xOffset - 2, boardHeight + 4);
            g2d.fillRect(xOffset + boardWidth + 2, yOffset - 2, this.getWidth() - (xOffset + boardWidth + 2), boardHeight + 4);
            g2d.fillRect(0, yOffset + boardHeight + 2, this.getWidth(), this.getHeight() - (yOffset + boardHeight + 2));
        }
        g2d.setColor(new Color(245, 222, 179));
        g2d.fillRect(xOffset - 2, yOffset - 2, boardWidth + 4, boardHeight + 4);
        g2d.setColor(new Color(255, 248, 220));
        g2d.fillRect(xOffset, yOffset, boardWidth, boardHeight);

        int exitWidth = 2 * GRID_SIZE;
        int exitX = xOffset + (boardWidth - exitWidth) / 2;
        int exitY = yOffset + boardHeight - GRID_SIZE;

        if (exitHighlighted) {
            g.setColor(new Color(255, 100, 100));
        } else {
            g.setColor(new Color(255, 200, 200));
        }
        g.fillRect(exitX, exitY, exitWidth, GRID_SIZE);
        g.setColor(Color.RED);
        g.drawRect(exitX, exitY, exitWidth, GRID_SIZE);
        g.setColor(Color.BLACK);
        Font exitFont = new Font(Font.SANS_SERIF, Font.BOLD, GRID_SIZE/2);
        g.setFont(exitFont);
        FontMetrics fm = g.getFontMetrics();
        String exitText = "EXIT";
        int textX = exitX + (exitWidth - fm.stringWidth(exitText))/2;
        int textY = exitY + GRID_SIZE/2 + fm.getAscent()/2;
        g.drawString(exitText, textX, textY);

        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);
        this.setBorder(border);
    }

    public void updateMoveCount(int count) {
        this.steps = count;
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", this.steps));
        }
    }

    public void setObstacleSelectionMode(boolean active) {
        this.obstacleSelectionMode = active;
        this.setCursor(active ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
    }
}

package tankgame2p;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TwoPlayerTankGame extends JFrame {

    private static final int TANK_SIZE = 30;
    private static final int MAZE_SIZE = 400;
    private static final int TANK_SPEED = 3;
    private static final int BULLET_SPEED = 8;
    private static final int BULLET_DAMAGE = 10;
    private static final int MARK_SIZE = 5;

    private int player1X = 50;
    private int player1Y = 50;
    private int player1HP = 100;
    private boolean player1Shoot = false;
    private int player1BulletX = -1;
    private int player1BulletY = -1;
    private double player1TankAngle = 0; // Angle in radians

    private int player2X = 300;
    private int player2Y = 300;
    private int player2HP = 100;
    private boolean player2Shoot = false;
    private int player2BulletX = -1;
    private int player2BulletY = -1;
    private double player2TankAngle = 0; // Angle in radians

    private boolean player1Up = false;
    private boolean player1Down = false;
    private boolean player1Left = false;
    private boolean player1Right = false;

    private boolean player2Up = false;
    private boolean player2Down = false;
    private boolean player2Left = false;
    private boolean player2Right = false;

    public TwoPlayerTankGame() {
        setTitle("2D Tank Game");
        setSize(MAZE_SIZE, MAZE_SIZE + 40); // Increased height for displaying HP
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used in this example
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });

        setFocusable(true);
        requestFocus();

        // Use a Timer for the game loop
        Timer timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                repaint();
            }
        });
        timer.start();

        // Create threads for tank movements
        Thread player1Thread = new Thread(new TankMovementRunnable(this, 1));
        Thread player2Thread = new Thread(new TankMovementRunnable(this, 2));

        player1Thread.start();
        player2Thread.start();
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Player 1 controls
            case KeyEvent.VK_W:
                player1Up = true;
                break;
            case KeyEvent.VK_S:
                player1Down = true;
                break;
            case KeyEvent.VK_A:
                player1Left = true;
                break;
            case KeyEvent.VK_D:
                player1Right = true;
                break;
            case KeyEvent.VK_R:
                if (!player1Shoot) {
                    player1Shoot = true;
                    player1BulletX = player1X + TANK_SIZE / 2;
                    player1BulletY = player1Y + TANK_SIZE / 2;
                }
                break;

            // Player 2 controls
            case KeyEvent.VK_UP:
                player2Up = true;
                break;
            case KeyEvent.VK_DOWN:
                player2Down = true;
                break;
            case KeyEvent.VK_LEFT:
                player2Left = true;
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = true;
                break;
            case KeyEvent.VK_ENTER:
                if (!player2Shoot) {
                    player2Shoot = true;
                    player2BulletX = player2X + TANK_SIZE / 2;
                    player2BulletY = player2Y + TANK_SIZE / 2;
                }
                break;
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Player 1 controls
            case KeyEvent.VK_W:
                player1Up = false;
                break;
            case KeyEvent.VK_S:
                player1Down = false;
                break;
            case KeyEvent.VK_A:
                player1Left = false;
                break;
            case KeyEvent.VK_D:
                player1Right = false;
                break;
            case KeyEvent.VK_R:
                player1Shoot = false;
                break;

            // Player 2 controls
            case KeyEvent.VK_UP:
                player2Up = false;
                break;
            case KeyEvent.VK_DOWN:
                player2Down = false;
                break;
            case KeyEvent.VK_LEFT:
                player2Left = false;
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = false;
                break;
            case KeyEvent.VK_ENTER:
                player2Shoot = false;
                break;
        }
    }

    private void updateGame() {
        // Handle shooting
        updateBulletPosition(player1Shoot, player1BulletX, player1BulletY, BULLET_SPEED, player1TankAngle);
        updateBulletPosition(player2Shoot, player2BulletX, player2BulletY, BULLET_SPEED, player2TankAngle);

        // Update tank positions
        updateTankPosition(player1Up, player1Down, player1Left, player1Right, 1);
        updateTankPosition(player2Up, player2Down, player2Left, player2Right, 2);

        // Check for collisions or other game logic
        handleCollisions(player1BulletX, player1BulletY, player2X, player2Y, BULLET_DAMAGE);
        handleCollisions(player2BulletX, player2BulletY, player1X, player1Y, BULLET_DAMAGE);
    }

    private void updateBulletPosition(boolean shoot, int bulletX, int bulletY, int speed, double angle) {
        if (shoot && bulletY == -1) {
            // Shoot only if a bullet is not already on the screen
            bulletX = player1X + TANK_SIZE / 2;
            bulletY = player1Y + TANK_SIZE / 2;
        }

        if (bulletY != -1) {
            // Move the bullet in the direction specified by the angle
            bulletX += (int) (speed * Math.cos(angle));
            bulletY += (int) (speed * Math.sin(angle));

            // Check if the bullet goes out of bounds
            if (bulletX < 0 || bulletX > MAZE_SIZE || bulletY < 0 || bulletY > MAZE_SIZE) {
                // Reset bullet position when it goes off-screen
                bulletX = -1;
                bulletY = -1;
            }
        }

        // Update bullet position
        if (Thread.currentThread().getName().equals("Player1Thread")) {
            player1BulletX = bulletX;
            player1BulletY = bulletY;
        } else {
            player2BulletX = bulletX;
            player2BulletY = bulletY;
        }
    }

    private void updateTankPosition(boolean up, boolean down, boolean left, boolean right, int player) {
        double tankAngle;
        int tankX, tankY;

        if (player == 1) {
            tankAngle = player1TankAngle;
            tankX = player1X;
            tankY = player1Y;
        } else {
            tankAngle = player2TankAngle;
            tankX = player2X;
            tankY = player2Y;
        }

        if (left) {
            // Rotate left
            tankAngle -= Math.toRadians(2);
        }

        if (right) {
            // Rotate right
            tankAngle += Math.toRadians(2);
        }

        if (up) {
            // Move forward
            tankX += (int) (TANK_SPEED * Math.cos(tankAngle));
            tankY += (int) (TANK_SPEED * Math.sin(tankAngle));
        }

        if (down) {
            // Move backward
            tankX -= (int) (TANK_SPEED * Math.cos(tankAngle));
            tankY -= (int) (TANK_SPEED * Math.sin(tankAngle));
        }

        if (player == 1) {
            player1TankAngle = tankAngle;
            player1X = tankX;
            player1Y = tankY;
        } else {
            player2TankAngle = tankAngle;
            player2X = tankX;
            player2Y = tankY;
        }
    }

    private class TankMovementRunnable implements Runnable {

        private final TwoPlayerTankGame game;
        private final int player;

        public TankMovementRunnable(TwoPlayerTankGame game, int player) {
            this.game = game;
            this.player = player;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                game.repaint();
            }
        }
    }

    private void handleCollisions(int bulletX, int bulletY, int targetX, int targetY, int damage) {
        // Check if the bullet hits the target
        if (bulletX >= targetX && bulletX <= targetX + TANK_SIZE &&
                bulletY >= targetY && bulletY <= targetY + TANK_SIZE) {
            // Reduce target's HP
            // For simplicity, deduct a fixed amount (BULLET_DAMAGE)
            // You might want to consider more sophisticated damage calculations
            if (Thread.currentThread().getName().equals("Player1Thread")) {
                player1HP -= damage;
            } else {
                player2HP -= damage;
            }
            // Reset bullet position
            if (Thread.currentThread().getName().equals("Player1Thread")) {
                player1BulletX = -1;
                player1BulletY = -1;
            } else {
                player2BulletX = -1;
                player2BulletY = -1;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw the maze
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MAZE_SIZE, MAZE_SIZE);

        // Draw Player 1 tank
        drawTank(g, player1X, player1Y, player1TankAngle, Color.BLUE);

        // Draw Player 2 tank
        drawTank(g, player2X, player2Y, player2TankAngle, Color.RED);

        // Draw front marks
        drawFrontMark(g, player1X, player1Y, player1TankAngle);
        drawFrontMark(g, player2X, player2Y, player2TankAngle);

        // Draw bullets
        drawBullet(g, player1BulletX, player1BulletY);
        drawBullet(g, player2BulletX, player2BulletY);

        // Draw player HP in the upper left corner
        g.drawString("Player 1 HP: " + player1HP, 10, 20);
        g.drawString("Player 2 HP: " + player2HP, 10, 40);
    }

    private void drawTank(Graphics g, int x, int y, double angle, Color color) {
        // Draw tank body
        g.setColor(color);
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(angle, x + TANK_SIZE / 2, y + TANK_SIZE / 2);
        g2d.fillRect(x, y, TANK_SIZE, TANK_SIZE);
        g2d.rotate(-angle, x + TANK_SIZE / 2, y + TANK_SIZE / 2);

        // Draw tank turret
        int turretSize = TANK_SIZE / 2;
        g.setColor(color.darker());
        g2d.rotate(angle, x + TANK_SIZE / 2, y + TANK_SIZE / 2);
        g2d.fillRect(x + TANK_SIZE / 4, y + TANK_SIZE / 4, turretSize, turretSize / 2);
        g2d.rotate(-angle, x + TANK_SIZE / 2, y + TANK_SIZE / 2);
    }

    private void drawFrontMark(Graphics g, int x, int y, double angle) {
        // Draw a mark at the front of the tank to indicate direction
        g.setColor(Color.WHITE);
        int frontX = (int) (x + (TANK_SIZE / 2) + (TANK_SIZE / 2) * Math.cos(angle));
        int frontY = (int) (y + (TANK_SIZE / 2) + (TANK_SIZE / 2) * Math.sin(angle));
        g.fillRect(frontX - MARK_SIZE / 2, frontY - MARK_SIZE / 2, MARK_SIZE, MARK_SIZE);
    }

    private void drawBullet(Graphics g, int x, int y) {
        if (x != -1) {
            g.setColor(Color.RED);
            g.fillOval(x, y, 5, 5);
        }
    }

    public static void main(String[] args) {
        TwoPlayerTankGame game = new TwoPlayerTankGame();
        game.setVisible(true);
    }
}


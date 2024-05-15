package com.zetcode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

    private transient Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;

    private boolean inGame = false;
    private boolean dying = false;

    private static final int BLOCK_SIZE = 24;
    private static final int N_BLOCKS = 15;
    private static final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private static final int PAC_ANIM_DELAY = 2;
    private static final int PACMAN_ANIM_COUNT = 4;
    private static final int MAX_GHOSTS = 12;
    private static final int PACMAN_SPEED = 6;

    private int pacAnimCount = PAC_ANIM_DELAY;
    private int pacAnimDir = 1;
    private int pacmanAnimPos = 0;
    private int nGhosts = 6;
    private int pacsLeft;
    private int score;
    private int[] dx;
    private int[] dy;
    private int[] ghostX;
    private int[] ghostY;
    private int[] ghostDX;
    private int[] ghostDY;
    private int[] ghostSpeed;

    private transient Image ghost;
    private transient Image pacman1;
    private transient Image pacman2up;
    private transient Image pacman2left;
    private transient Image pacman2right;
    private transient Image pacman2down;
    private transient Image pacman3up;
    private transient Image pacman3down;
    private transient Image pacman3left;
    private transient Image pacman3right;
    private transient Image pacman4up;
    private transient Image pacman4down;
    private transient Image pacman4left;
    private transient Image pacman4right;

    private int pacmanX;
    private int pacmanY;
    private int pacmanDX;
    private int pacmanDY;
    private int reqDX;
    private int reqDY;
    private int viewDX;
    private int viewDY;

    private final short[] levelData = {
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };

    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};
    private static final int MAX_SPEED = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board() {

        loadImages();
        initVariables();
        initBoard();
    }

    private void initBoard() {

        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.black);
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        ghostX = new int[MAX_GHOSTS];
        ghostDX = new int[MAX_GHOSTS];
        ghostY = new int[MAX_GHOSTS];
        ghostDY = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    private void doAnim() {

        pacAnimCount--;

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            pacmanAnimPos = pacmanAnimPos + pacAnimDir;

            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir;
            }
        }
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2);
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(pacman3left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (nGhosts < MAX_GHOSTS) {
                nGhosts++;
            }

            if (currentSpeed < MAX_SPEED) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        pacsLeft--;

        if (pacsLeft == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhostsHelper(short i) {
        if (ghostX[i] % BLOCK_SIZE == 0 && ghostY[i] % BLOCK_SIZE == 0) {
            int pos = ghostX[i] / BLOCK_SIZE + N_BLOCKS * (ghostY[i] / BLOCK_SIZE);

            int count = 0;

            if ((screenData[pos] & 1) == 0 && ghostDX[i] != 1) {
                dx[count] = -1;
                dy[count] = 0;
                count++;
            }

            if ((screenData[pos] & 2) == 0 && ghostDY[i] != 1) {
                dx[count] = 0;
                dy[count] = -1;
                count++;
            }

            if ((screenData[pos] & 4) == 0 && ghostDX[i] != -1) {
                dx[count] = 1;
                dy[count] = 0;
                count++;
            }

            if ((screenData[pos] & 8) == 0 && ghostDY[i] != -1) {
                dx[count] = 0;
                dy[count] = 1;
                count++;
            }

            if (count == 0) {

                if ((screenData[pos] & 15) == 15) {
                    ghostDX[i] = 0;
                    ghostDY[i] = 0;
                }
                else {
                    ghostDX[i] = -ghostDX[i];
                    ghostDY[i] = -ghostDY[i];
                }

            }
            else {

                count = (int) (Math.random() * count);

                if (count > 3) {
                    count = 3;
                }

                ghostDX[i] = dx[count];
                ghostDY[i] = dy[count];
            }

        }
    }

    private void moveGhosts(Graphics2D g2d) {

        short i;

        for (i = 0; i < nGhosts; i++) {
            moveGhostsHelper(i);

            ghostX[i] = ghostX[i] + (ghostDX[i] * ghostSpeed[i]);
            ghostY[i] = ghostY[i] + (ghostDY[i] * ghostSpeed[i]);
            drawGhost(g2d, ghostX[i] + 1, ghostY[i] + 1);

            if (pacmanX > (ghostX[i] - 12) && pacmanX < (ghostX[i] + 12)
                    && pacmanY > (ghostY[i] - 12) && pacmanY < (ghostY[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {

        g2d.drawImage(ghost, x, y, this);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (reqDX == -pacmanDX && reqDY == -pacmanDY) {
            pacmanDX = reqDX;
            pacmanDY = reqDY;
            viewDX = pacmanDX;
            viewDY = pacmanDY;
        }

        if (pacmanX % BLOCK_SIZE == 0 && pacmanY % BLOCK_SIZE == 0) {
            pos = pacmanX / BLOCK_SIZE + N_BLOCKS * (pacmanY / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if ((reqDX != 0 || reqDY != 0) && (!((reqDX == -1 && reqDY == 0 && (ch & 1) != 0)
                    || (reqDX == 1 && reqDY == 0 && (ch & 4) != 0)
                    || (reqDX == 0 && reqDY == -1 && (ch & 2) != 0)
                    || (reqDX == 0 && reqDY == 1 && (ch & 8) != 0)))) {

                pacmanDX = reqDX;
                pacmanDY = reqDY;
                viewDX = pacmanDX;
                viewDY = pacmanDY;
            }

            // Check for standstill
            if ((pacmanDX == -1 && pacmanDY == 0 && (ch & 1) != 0)
                    || (pacmanDX == 1 && pacmanDY == 0 && (ch & 4) != 0)
                    || (pacmanDX == 0 && pacmanDY == -1 && (ch & 2) != 0)
                    || (pacmanDX == 0 && pacmanDY == 1 && (ch & 8) != 0)) {
                pacmanDX = 0;
                pacmanDY = 0;
            }
        }
        pacmanX = pacmanX + PACMAN_SPEED * pacmanDX;
        pacmanY = pacmanY + PACMAN_SPEED * pacmanDY;
    }

    private void drawPacman(Graphics2D g2d) {

        if (viewDX == -1) {
            drawPacnanLeft(g2d);
        } else if (viewDX == 1) {
            drawPacmanRight(g2d);
        } else if (viewDY == -1) {
            drawPacmanUp(g2d);
        } else {
            drawPacmanDown(g2d);
        }
    }

    private void drawPacmanUp(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2up, pacmanX + 1, pacmanY + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3up, pacmanX + 1, pacmanY + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4up, pacmanX + 1, pacmanY + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
                break;
        }
    }

    private void drawPacmanDown(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2down, pacmanX + 1, pacmanY + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3down, pacmanX + 1, pacmanY + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4down, pacmanX + 1, pacmanY + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
                break;
        }
    }

    private void drawPacnanLeft(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacmanX + 1, pacmanY + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3left, pacmanX + 1, pacmanY + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4left, pacmanX + 1, pacmanY + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
                break;
        }
    }

    private void drawPacmanRight(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2right, pacmanX + 1, pacmanY + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3right, pacmanX + 1, pacmanY + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4right, pacmanX + 1, pacmanY + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x;
        int y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    private void initGame() {

        pacsLeft = 3;
        score = 0;
        initLevel();
        nGhosts = 6;
        currentSpeed = 3;
    }

    private void initLevel() {
        screenData = Arrays.copyOf(levelData, N_BLOCKS * N_BLOCKS);

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int ddx = 1;
        int random;
        Random r = new Random();

        for (i = 0; i < nGhosts; i++) {

            ghostY[i] = 4 * BLOCK_SIZE;
            ghostX[i] = 4 * BLOCK_SIZE;
            ghostDY[i] = 0;
            ghostDX[i] = ddx;
            ddx = -ddx;

            random = r.nextInt(6) * (currentSpeed + 1);

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacmanX = 7 * BLOCK_SIZE;
        pacmanY = 11 * BLOCK_SIZE;
        pacmanDX = 0;
        pacmanDY = 0;
        reqDX = 0;
        reqDY = 0;
        viewDX = -1;
        viewDY = 0;
        dying = false;
    }

    private void loadImages() {

        ghost = new ImageIcon("src/resources/images/ghost.png").getImage();
        pacman1 = new ImageIcon("src/resources/images/pacman.png").getImage();
        pacman2up = new ImageIcon("src/resources/images/up1.png").getImage();
        pacman3up = new ImageIcon("src/resources/images/up2.png").getImage();
        pacman4up = new ImageIcon("src/resources/images/up3.png").getImage();
        pacman2down = new ImageIcon("src/resources/images/down1.png").getImage();
        pacman3down = new ImageIcon("src/resources/images/down2.png").getImage();
        pacman4down = new ImageIcon("src/resources/images/down3.png").getImage();
        pacman2left = new ImageIcon("src/resources/images/left1.png").getImage();
        pacman3left = new ImageIcon("src/resources/images/left2.png").getImage();
        pacman4left = new ImageIcon("src/resources/images/left3.png").getImage();
        pacman2right = new ImageIcon("src/resources/images/right1.png").getImage();
        pacman3right = new ImageIcon("src/resources/images/right2.png").getImage();
        pacman4right = new ImageIcon("src/resources/images/right3.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    reqDX = -1;
                    reqDY = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    reqDX = 1;
                    reqDY = 0;
                } else if (key == KeyEvent.VK_UP) {
                    reqDX = 0;
                    reqDY = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    reqDX = 0;
                    reqDY = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    inGame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT
                    || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                reqDX = 0;
                reqDY = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}

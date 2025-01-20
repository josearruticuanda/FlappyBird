import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Variables
    int boardWidth = 360;
    int boardHeight = 640;
    int fps = 60;

    int floorHeight = 90;

    // Images
    Image bgImg;
    Image birdImg;
    Image pipeTopImg;
    Image pipeBotImg;
    Image floorImg;

    // Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    int pipeGap = boardHeight/6;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    // Game Logic
    Bird bird;

    int velocityY = -11; // Bird initial velocity
    int velocityX = -4; // Pipe velocity
    int gravity = 1;
    int pipeSpawnTime = 2000; // ms

    boolean gameOver = false;

    double score = 0;

    Timer gameLoop;
    Timer pipesTimer;


    // Classes
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Constructor
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        
        // Make sure that this panel is taking the KeyListener
        setFocusable(true);
        addKeyListener(this);

        // Load Images
        bgImg = new ImageIcon(getClass().getResource("./images/bg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./images/flappybird.png")).getImage();
        pipeTopImg = new ImageIcon(getClass().getResource("./images/pipe_top.png")).getImage();
        pipeBotImg = new ImageIcon(getClass().getResource("./images/pipe_bottom.png")).getImage();
        floorImg = new ImageIcon(getClass().getResource("./images/floor.png")).getImage();

        // Bird
        bird = new Bird(birdImg);
        // Pipes
        pipes = new ArrayList<Pipe>();

        // Place pipes timer
        pipesTimer = new Timer(pipeSpawnTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        pipesTimer.start();

        // Game Timer
        gameLoop = new Timer(1000/fps, this);
        gameLoop.start();
    }


    // Functions
    public void placePipes() {
        int randomPipeY = (int)(pipeY - pipeHeight/4 - Math.random()*pipeHeight/2);
        
        // Top Pipe
        Pipe topPipe = new Pipe(pipeTopImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        // Bottom Pipe
        Pipe bottomPipe = new Pipe(pipeBotImg);
        bottomPipe.y = topPipe.y + pipeHeight + pipeGap;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(bgImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (int i=0; i<pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Floor
        g.drawImage(floorImg, 0, 0, boardWidth, boardHeight, null);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 42));
        if (gameOver) {
            g.drawString("Game Over!", boardWidth/6, boardHeight/2);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("Score: " + (int)score, boardWidth/3, (int)(boardHeight/1.75));
        }
        else {
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("Score: " + (int)score, 10, 35);
        }
    }

    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);
        bird.y = Math.min(bird.y, boardHeight-floorHeight);

        // Pipes
        for (int i=0; i<pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // Check for pipes passed
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // Because there are two pipes it passes at the same time
            }

            // Collision with pipes
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Collision with floor
        // -1 is because the bird can only go down to (boardHeight-floorHeight)
        if (bird.y > boardHeight-floorHeight-1) {
            gameOver = true;
        }
    }


    // Formula for collision bird with pipe
    public boolean collision(Bird b, Pipe p) {
        return  b.x < p.x + p.width &&  // bird's top left corner doesn't reach pipe's top right corner
                b.x + b.width > p.x &&  // bird's top right corner passes pipe's top left corner
                b.y < p.y + p.height && // bird's top left corner doesn't reach pipe's bottom left corner
                b.y + b.height > p.y;   // bird's bottom left corner passes pipe's top left corner
    }

    // Loops every frame
    @Override
    public void actionPerformed(ActionEvent e) {
        // Update position of bird
        move();
        // Draw frame
        repaint();

        if (gameOver) {
            pipesTimer.stop();
            gameLoop.stop();
            // System.out.println("Game Over!");
        }
    }


    // What to do when SPACE is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -11;

            // Restart the game with SPACE key by reseting the conditions
            if (gameOver) {
                bird.y = boardHeight/2;
                velocityY = -11;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                pipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FoodGame;

import antcolony.SimulationObjects.Food;
import antcolony.configuration.Configuration;
import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author Ben
 */
public class Renderer {

	private JFrame app;
	private Canvas canvas;
	private BufferStrategy buffer;
	private GraphicsEnvironment ge;
	private GraphicsDevice gd;
	private GraphicsConfiguration gc;
	private BufferedImage bi;
	private Graphics graphics = null;
	private Graphics2D g2d = null;
	private Color background = Configuration.currentConfig.backColor;
	private int resX;
	private int resY;
	private int frameDelay;
	private final SimulationState state;
	private boolean graphicsSetup = false;
	private boolean running = true;
	private int currentState = 0;
	private boolean paused = false;
	
	//inputs
	private KeyboardInput keyboard = new KeyboardInput(); // Keyboard polling
	private MouseInput mouse = new MouseInput();
	private Food currentFood = null;
	private BufferedImage backgroundImg;
	private Image bkg1;
	
	private BufferedImage bklunch;
	private Image bkl;
	
	public Renderer(SimulationState s) {
		this.state = s;
	}

	private class DrawingThread extends Thread {

		public DrawingThread() {
		}

		@Override
		public void run() {
			while (running) {
				try {
					repaint();

					sleep(frameDelay);
				} catch (InterruptedException ex) {
					Logger.getLogger(Renderer.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					// release resources

					if (graphics != null) {
						graphics.dispose();
					}
					if (g2d != null) {
						g2d.dispose();
					}
				}
			}



		}
	}

	public void play() {
		if (!graphicsSetup) {
			setUpWindow();
			graphicsSetup = true;
		}






		repaint();

		//wait for a spacebar press before starting
		while (true) {
			keyboard.poll();
			if (keyboard.keyDown(KeyEvent.VK_SPACE)) {
				break;
			}
		}



		DrawingThread thread = new DrawingThread();
		thread.start();
		Point mousePos = new Point(0, 0);
		while (running) {


			// Poll the keyboard
			keyboard.poll();
			mouse.poll();
			if (keyboard.keyDownOnce(KeyEvent.VK_SPACE)) {
				paused = !paused;
			}

			if (keyboard.keyDownOnce(KeyEvent.VK_ESCAPE)) {
				running = false;
			}
			Point newMousePos = mouse.getPosition();
			if (mouse.buttonDown(1)) {


				Food clickFood = state.clickFood(mousePos.x, mousePos.y);
				if (clickFood != null) {
					System.out.println(clickFood.foodName);
					clickFood.pos.x += newMousePos.x - mousePos.x;
					clickFood.pos.y += newMousePos.y - mousePos.y;
				}
				currentFood = clickFood;

			}

			mousePos = newMousePos;
		}

		try {
			if (thread != null) {
				thread.join(2000);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(Renderer.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (graphics != null) {
			graphics.dispose();
		}
		if (g2d != null) {
			g2d.dispose();
		}

		app.dispose();





	}

	//pre: ants have been instantiated, as have the graphics things
	//post: ants are drawn on the screen.
	private void drawFood(java.util.List<Food> food) {
		for (Food f : food) {
			g2d.drawImage(f.image, f.pos.x, f.pos.y, null); //these numbers are probably wildly wrong
		}

	}

	private void drawGrid() {
	}

	private void drawFoodStats(Food f) {
		if (f != null) {
			g2d.setColor(Color.BLACK);
			g2d.drawString("Food Quality: " + f.foodQuality, 500, 100);
		}
	}

	private void drawBackground() {
		//TODO: Draw Background. Refridgerator and stuff.
		backgroundImg.getGraphics().drawImage(bkg1, 0, 0, null);
		g2d.drawImage(backgroundImg, 0, 70, null);
		
		bklunch.getGraphics().drawImage(bkl, 0, 0, null);
		g2d.drawImage(bklunch, 551, 400, null);
		
		Graphics2D graphics2D=(Graphics2D) g2d;
		GraphicsEnvironment.getLocalGraphicsEnvironment();
	    graphics2D.setFont(new Font("LucidaSans", Font.PLAIN, 20));
	    graphics2D.setColor(Color.BLACK);
	    graphics2D.drawString("Calories", 420, 60);
		graphics2D.drawString("Proteins", 420, 90);
		graphics2D.drawString("Fruits", 420, 120);
		graphics2D.drawString("Vegetables", 420, 150);
		graphics2D.drawString("Grains", 420, 180);
		graphics2D.drawString("Dairy", 420, 210);
		graphics2D.drawString("Fats", 420, 240);
		graphics2D.drawString("Sugars", 420, 270);
		graphics2D.setFont(new Font("LucidaSans", Font.PLAIN, 25));
		graphics2D.drawString("Current", 550, 30);
		graphics2D.drawString("Goals", 680, 30);
		
	}

	//pre: graphics are set up, things that need to be drawn are instantiated.
	//post: all the parts of the simulation are drawn to the screen
	private void repaint() {
		g2d = bi.createGraphics();
		g2d.setColor(background);
		g2d.fillRect(0, 0, resX, resY);

		drawBackground();

		drawFood(state.getFoodList());
		drawFoodStats(currentFood);
//		g2d.drawString("Frame " + currentState, 10, 10);


		// Blit image and flip...
		graphics = buffer.getDrawGraphics();
		graphics.drawImage(bi, 0, 0, null);
		if (!buffer.contentsLost()) {
			buffer.show();
		}

		// Let the OS have a little time...
		//Thread.yield();

	}

	//pre: none
	//post: instantiates all the various variables necessary for core functions.
	private void setUpWindow() {
		// Create simulation window...
		resX = Configuration.currentConfig.resolution.x;
		resY = Configuration.currentConfig.resolution.y;
		app = new JFrame();
		app.setIgnoreRepaint(true);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create canvas for painting...
		canvas = new Canvas();
		canvas.setIgnoreRepaint(true);

		canvas.setSize(resX, resY);

		// Add canvas to simulation window...
		app.add(canvas);
		app.pack();
		app.setVisible(true);

		// Create BackBuffer...
		canvas.createBufferStrategy(2);
		buffer = canvas.getBufferStrategy();

		// Get graphics configuration...
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();

		// Create off-screen drawing surface
		bi = gc.createCompatibleImage(resX, resY);

		// Hookup keyboard polling
		app.addKeyListener(keyboard);
		canvas.addKeyListener(keyboard);
		app.addMouseListener(mouse);
		app.addMouseMotionListener(mouse);

		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);




		frameDelay = Math.round(1000 / Configuration.currentConfig.FPS);

		try {
			BufferedImage bkg= ImageIO.read(new File("images/fridge.png"));
			 bkg1 =  bkg.getScaledInstance(550, 520, Image.SCALE_SMOOTH);
			backgroundImg=new BufferedImage(bkg1.getWidth(null), bkg1.getHeight(null), BufferedImage.TYPE_INT_ARGB );
			
			BufferedImage lk= ImageIO.read(new File("images/lunchbag.png"));
			bkl =  lk.getScaledInstance(245, 200, Image.SCALE_SMOOTH);
			bklunch=new BufferedImage(bkl.getWidth(null), bkl.getHeight(null), BufferedImage.TYPE_INT_ARGB );
			
			
			
		} catch (IOException e) {
		}

	}
}

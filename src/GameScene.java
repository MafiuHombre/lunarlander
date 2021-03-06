import javafx.animation.Animation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.*;
import javafx.scene.text.Font;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.event.EventHandler;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.css.Rect;
import javafx.scene.transform.Rotate;

import javax.swing.Timer;
/**
 *Class responsbile for making scene wherein the game takes place
 */

public class GameScene extends Scene {

	private float DEFAULT_WIDTH;
	private float DEFAULT_HEIGHT;
	private Coin[] coins;
	private Ellipse[] coinsCircle;
	private int numberOfMountains;
	private int coinsQuantity;
	private FuelBar fuelBar;
	private Rectangle fuelBarRectangle  = new Rectangle();
	private Rectangle fuelBarCountor = new Rectangle();
	private Rocket rocket;
	private Text timeText;
	private Config cfg;
	private Timeline rocketAnimation;
	private Rectangle rect;
	private Ellipse circle;
	private Line line;
	private CubicCurve[] mountains;
	private Group root;
	private Fuel fuel;
	private Rectangle fuelRectangle = new Rectangle();
	private Text velXText;
	private Text velYText;
	private float maxVelX;
	private float maxVelY;
	private Stage stage;
	private Scene nextScene;
	private DoubleProperty centerX;
	private DoubleProperty centerY;
	private LevelTimer timer;
	private double score = Player.getPlayerScore();
	private Text scoreIndicator = new Text();
	private Frame frame;
	private Text lifesText = new Text();
	private Text levelNumber = new Text();

	public GameScene(Region root, Stage stage, Scene nextScene, Frame frame) {
		super(root);
		this.frame = frame;
		DEFAULT_WIDTH = Constants.getDefaultWidth();
		DEFAULT_HEIGHT = Constants.getDefaultHeight();
		this.stage = stage;
		stage.widthProperty().addListener((obs, oldVal, newVal) -> {
			WidthScalability(DEFAULT_WIDTH, (float)stage.getWidth());
			DEFAULT_WIDTH = (float)stage.getWidth();
		});
		stage.heightProperty().addListener((obs, oldVal, newVal) -> {
			HeightScalability(DEFAULT_HEIGHT, (float)stage.getHeight());
			DEFAULT_HEIGHT = (float)stage.getHeight();

		});
		this.nextScene = nextScene;
		this.cfg = cfg;
	}
	
	public Scene initiateGame(Enum difficulty) {
//		try {
//			//setup the client
//			Client client = new Client();
//			//get level from the server
//			client.getLevel(Player.getActualLevel());
//			Thread.sleep(60000);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		cfg = new Config(Player.getActualLevel());

		//setting line properties
		line = setLineProperties();

		// maximum values of velocity by which rocket can successfully land
		maxVelX = Utils.floatFromConfig(cfg, "maxVelX");
		maxVelY = Utils.floatFromConfig(cfg, "maxVelY");

		//Depending on difficulty argument we set fallVelocity
		rocket = new Rocket(difficulty, cfg);
	//	rocket.setFallVelocity(getFallVelocity(difficulty));
		root = new Group();
		Scene scene = new Scene(root, Constants.GAME_SCENE_DEFAULT_WIDTH, Constants.GAME_SCENE_DEFAULT_HEIGHT);
		numberOfMountains = Utils.intFromConfig(cfg, "mountainsCount");
		coinsQuantity = Utils.intFromConfig(cfg, "coinsQuantity");
		coins = new Coin[coinsQuantity];
		coinsCircle = new Ellipse[coinsQuantity];
		for (Integer i = 1; i <= coinsQuantity; i++) {
			coins[i - 1] = new Coin(Utils.intFromConfig(cfg, "coin" + i + "X"), Utils.intFromConfig(cfg, "coin" + i + "Y"), cfg);
			coinsCircle[i - 1] = coins[i - 1].paint();
		}
		fuel = new Fuel(Utils.intFromConfig(cfg, "fuelX"), Utils.intFromConfig(cfg, "fuelY"), Utils.intFromConfig(cfg, "fuelSize"));
		fuelRectangle = fuel.paint();
		fuelBar = new FuelBar(Utils.doubleFromConfig(cfg, "fuelBarX"),
				Utils.doubleFromConfig(cfg, "fuelBarY"), cfg);

		//set level number
		Text levelNumber = setLevelNumber();

		//setup score indicator
		setupScoreIndicator();

		velXText = setStartVelXText();
		velYText = setStartVelYText();
		setLifesText();
		//timeText initialization, setting starting value and text properties
		timer = new LevelTimer(cfg);
		timeText = setTimeText((stage.getWidth() - timer.getEdgeDistance()));

//		line = new Line();
//		line = setLineProperties();
		mountains = new CubicCurve[numberOfMountains];
		for (int i = 0; i < numberOfMountains; i++) {
			mountains[i] = new CubicCurve();
			mountains[i] = setMountainProperties(i + 1);
		}

		//       DoubleProperty mountainsProp = new SimpleDoubleProperty();
		centerX = new SimpleDoubleProperty();
		centerY = new SimpleDoubleProperty();
		circle = new Ellipse(rocket.getX(), rocket.getY(), rocket.getRadius(), rocket.getRadius());
		circle = rocket.paint();
		fuelBarCountor = fuelBar.paint();

		for (int i = 0; i < mountains.length; i++)
			root.getChildren().add(mountains[i]);
		//adding all the coins to root
		for (Integer i = 0; i < coinsQuantity; i++) {
			root.getChildren().addAll(coinsCircle[i]);
		}
		fuelBarRectangle = fuelBar.fillFuel(rocket.getFuel());
		root.getChildren().addAll(levelNumber, scoreIndicator,
				fuelRectangle, fuelBarCountor, fuelBarRectangle,
				velXText, velYText, timeText, circle, line, lifesText);
		root.setFocusTraversable(true);


		animate(centerX, centerY);

		circle.centerXProperty().bind(centerX);
		circle.centerYProperty().bind(centerY);
		centerX.setValue(rocket.getX());
		centerY.setValue(rocket.getY());
		root.requestFocus();
		return scene;
	}
	/*
	 * Method returning number of actual level (Text)
	 */
	private Text setLevelNumber() {
		levelNumber.setText(cfg.getProperty("levelText"));
		levelNumber.setX(10);
		levelNumber.setY(330);
		Font font = new Font(20);
		levelNumber.setFont(font);
		
		return levelNumber;
	}
	
	private Text setStartVelXText() {
		Text velX = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("VelocityX:");
		builder.append(Double.toString(rocket.getInsRightVelocity() + rocket.getInsLeftVelocity()));
		velX.setText(builder.toString());
		velX.setX(3);
		velX.setY(10);
		velX.setFill(Color.GREEN);
		Font font = new Font(14);
		velX.setFont(font);
		
		return velX;
			
	}

	private Text setStartVelYText() {
		Text velY = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("VelocityY:");
		builder.append(Double.toString(rocket.getFallVelocity()));
		velY.setText(builder.toString());
		velY.setX(3);
		velY.setY(25);
		velY.setFill(Color.GREEN);
		Font font = new Font(14);
		velY.setFont(font);
		
		return velY;
				
	}

	private Text setTimeText(double x) {
		Text timeText = new Text();
		StringBuilder builder = new StringBuilder();
		Date date = timer.getDate();
		builder.append("Czas: ");
		builder.append(date.getMinutes());
		builder.append(":");
		builder.append(date.getSeconds());
		timeText.setText(builder.toString());
		timeText.setX(x);
		timeText.setY(10);
		Font font = new Font(14);
		timeText.setFont(font);

		return timeText;
	}
	/*
	Method sets lifesNumber text
	 */
	public void setLifesText() {
		lifesText.setX(3);
		lifesText.setY(40);
		StringBuilder builder = new StringBuilder();
		builder.append("Lifes: ");
		builder.append(Player.getLifesNumber());
		Font font = new Font(14);
		lifesText.setFont(font);
		lifesText.setText(builder.toString());
	}
	/*
	Method checks if rocket has a collision with mountain, if so animation stops
	 */
	public void checkForMountainCollision() {
	    for(int i = 0; i < mountains.length; i++)
            if (mountains[i].contains(circle.getCenterX(), circle.getCenterY())) {
//                System.out.println("Kolizja");
                rocketAnimation.stop();
                circle.setVisible(false);
                Player.levelFailed();
//				stage.setScene(nextScene);
				if(checkPlayersLifes()) {
					frame.setScoreScene();
				}
				else {
					frame.setLeaderboardScene();
				}
            }
	}
	/*
	Method checks if rocket has a collision with fuel tank,
	if so fuel tank dissapears and rocket fuel increases
	 */

	public void checkForFuelCollision() {
		if(fuelRectangle.contains(circle.getCenterX(), circle.getCenterY())
				&& !fuel.getWasUsed()) {
//			System.out.println("Paliwo");
//			fuelRectangle.setVisible(false);
			root.getChildren().remove(fuelRectangle);
			rocket.addFuel();
			fuel.setWasUsed();
		}
		else if(fuelRectangle.contains((circle.getCenterX() + rocket.getRadius()),
				(circle.getCenterY() + rocket.getRadius()))
				&& !fuel.getWasUsed()) {
//			System.out.println("Paliwo");
//			fuelRectangle.setVisible(false);
			root.getChildren().remove(fuelRectangle);
			rocket.addFuel();
			fuel.setWasUsed();
		}
		else if(fuelRectangle.contains((circle.getCenterX() - rocket.getRadius()),
				(circle.getCenterY() - rocket.getRadius()))
				&& !fuel.getWasUsed()) {
//			System.out.println("Paliwo");
//			fuelRectangle.setVisible(false);
			root.getChildren().remove(fuelRectangle);
			rocket.addFuel();
			fuel.setWasUsed();
		}
		else if(fuelRectangle.contains((circle.getCenterX() - rocket.getRadius()),
				(circle.getCenterY() + rocket.getRadius()))
				&& !fuel.getWasUsed()) {
//			System.out.println("Paliwo");
//			fuelRectangle.setVisible(false);
			root.getChildren().remove(fuelRectangle);
			rocket.addFuel();
			fuel.setWasUsed();
		}
		else if(fuelRectangle.contains((circle.getCenterX() + rocket.getRadius()),
				(circle.getCenterY() - rocket.getRadius()))
				&& !fuel.getWasUsed()) {
//			System.out.println("Paliwo");
//			fuelRectangle.setVisible(false);
			root.getChildren().remove(fuelRectangle);
			rocket.addFuel();
			fuel.setWasUsed();
		}
	}
	/*
	Method checks if rocket has a collision with landingZone,
	if velocities of rocket weren`t to fast rocket successfully lands,
	else rocket crashes
	 */
	public void checkForLandingZoneCollision() {
		if (line.contains(circle.getCenterX(), circle.getCenterY())) {
			if((rocket.getInsRightVelocity() + rocket.getInsLeftVelocity()) < maxVelX
			&& (rocket.getInsFallVelocity() < maxVelY)) {
//				System.out.println("Ladowanie");
				circle.setVisible(true);
				rocketAnimation.stop();
				countFinalScore(timer.getSeconds(), rocket);
				if(Player.getActualLevel() == Constants.LEVEL_NUMBERS+1) {
					Player.getBestScores().addResult((float) Player.getPlayerScore(), Player.getName());
					frame.setLeaderboardScene();
				}
				else
					frame.setScoreScene();
			}
			else {
//				System.out.println("Kolizja");
				circle.setVisible(false);
				rocketAnimation.stop();
				Player.levelFailed();
				if(checkPlayersLifes()) {
					frame.setScoreScene();
				}
				else {
					frame.setLeaderboardScene();
				}
			}

//			stage.setScene(nextScene);

		}

	}
	/*
	Method checks if rocket had move out of bounds
	 */
	public void checkOutOfBoundsCollision() {
		if((circle.getCenterX() < stage.getMinWidth()) || (circle.getCenterX() > (stage.getWidth()))
		|| (circle.getCenterY() < stage.getMinHeight()) || (circle.getCenterY() > (stage.getHeight()))) {
			rocketAnimation.stop();
			Player.levelFailed();
//			stage.setScene(nextScene);
			if(checkPlayersLifes()) {
				frame.setScoreScene();
			}
			else {
				frame.setLeaderboardScene();
			}
		}
	}
	/*
	Method checks if rocket had a collision with coin, if so points value increases
	 */
	private void checkCoinCollision() {
		for(int i = 0; i < coinsCircle.length; i++) {
			if ((coinsCircle[i].contains(circle.getCenterX(), circle.getCenterY())
					&& !coins[i].getWasUsed())) {
//				System.out.println("You've got a coin!");
				eatCoin();
				root.getChildren().remove(coinsCircle[i]);
				coins[i].setWasUsed();
			}
			else if ((coinsCircle[i].contains((circle.getCenterX() + rocket.getRadius())
					, (circle.getCenterY() + rocket.getRadius()))
					&& !coins[i].getWasUsed())) {
//				System.out.println("You've got a coin!");
				eatCoin();
				root.getChildren().remove(coinsCircle[i]);
				coins[i].setWasUsed();
			}
			else if ((coinsCircle[i].contains((circle.getCenterX() - rocket.getRadius())
					, (circle.getCenterY() - rocket.getRadius()))
					&& !coins[i].getWasUsed())) {
//				System.out.println("You've got a coin!");
				eatCoin();
				root.getChildren().remove(coinsCircle[i]);
				coins[i].setWasUsed();
			}
			else if ((coinsCircle[i].contains((circle.getCenterX() + rocket.getRadius())
					, (circle.getCenterY() - rocket.getRadius()))
					&& !coins[i].getWasUsed())) {
//				System.out.println("You've got a coin!");
				eatCoin();
				root.getChildren().remove(coinsCircle[i]);
				coins[i].setWasUsed();
			}
			else if ((coinsCircle[i].contains((circle.getCenterX() - rocket.getRadius())
					, (circle.getCenterY() + rocket.getRadius()))
					&& !coins[i].getWasUsed())) {
//				System.out.println("You've got a coin!");
				eatCoin();
				root.getChildren().remove(coinsCircle[i]);
				coins[i].setWasUsed();
			}
		}


	}
	/*
	Method calls methods responsible for checking for collisions
	 */
	private void checkForCollisions() {
		checkForLandingZoneCollision();
		checkForMountainCollision();
		checkForFuelCollision();
		checkOutOfBoundsCollision();
		checkCoinCollision();
	}
	/*
	Method checks if player lost all lifes, if so next scene is leaderboard scene
	if game is over returns false, else true
	 */
	private boolean checkPlayersLifes() {
		if(Player.getGameStatus()) {
			//adding players result to best results
			Player.getBestScores().addResult((float) Player.getPlayerScore(), Player.getName());
			Player.reset();
			return false;
		}
		return true;
	}

	/*
	Method returning fall velocity of the Rocket (Integer) gets difficulty (Integer) as argument
	 */
	private float getFallVelocity(int difficulty) {
		float fallVelocity = 0;
		switch(difficulty){
			case 1:
				fallVelocity = 0.5f;
			break;

			case 2:
				fallVelocity = 0.55f;
			break;

			case 3:
				fallVelocity = 0.6f;
			break;

		}
		return fallVelocity;
	}
	/*
	Method sets parameters of line
	 */
	private Line setLineProperties() {
		Line line = new Line();
		line.setStartX(Utils.floatFromConfig(cfg, "landingZoneStartX"));
		line.setStartY(Utils.floatFromConfig(cfg, "landingZoneY"));
		line.setEndX(Utils.floatFromConfig(cfg, "landingZoneEndX"));
		line.setEndY(Utils.floatFromConfig(cfg, "landingZoneY"));
		line.setStrokeWidth(Utils.floatFromConfig(cfg, "landingZoneWidth"));
		return line;
	}
	/*
	Method sets parameters of mountain, get index of current mountain (Int)
	 */
	private CubicCurve setMountainProperties(int index) {
		CubicCurve mountain = new CubicCurve();
		StringBuilder builder = new StringBuilder();
		builder.append("mountain");
		builder.append(index);
		String template = builder.toString();
		mountain.setStartX(Utils.floatFromConfig(cfg, template + "StartX"));
		mountain.setStartY(Utils.floatFromConfig(cfg, template + "StartY"));
		mountain.setEndX(Utils.floatFromConfig(cfg, template + "EndX"));
		mountain.setEndY(Utils.floatFromConfig(cfg, template + "EndY"));
		mountain.setControlX1(Utils.floatFromConfig(cfg, template + "ControlX1")) ;
		mountain.setControlX2(Utils.floatFromConfig(cfg, template + "ControlX2"));
		mountain.setControlY1(Utils.floatFromConfig(cfg, template + "ControlY1"));
		mountain.setControlY2(Utils.floatFromConfig(cfg, template + "ControlY2"));
		return mountain;

	}
	/*
	Method actualizes timeText value every second
	 */
	private void setTimer() {
		DateFormat timeFormat = new SimpleDateFormat("mm:ss");
		timeText.setText(timeFormat.format(timer.getDate()));
	}
	/*
	Method makes animation of game
	 */
	private void animate(DoubleProperty centerX, DoubleProperty centerY) {
		rocketAnimation = new Timeline(
				new KeyFrame(new Duration(10.0), t ->  {
					//every second timeText is actualized
					setTimer();
					//if collision happen animation stops
					checkForCollisions();
					// set velocity with which rocket falls down
					rocket.increaseInsFallVelocity();
					centerY.setValue(centerY.getValue() + rocket.getInsFallVelocity());
					root.getChildren().remove(velXText);
					setVelX();
					root.getChildren().add(velXText);
					setVelY();
					//make rocket burn some amount of it's fuel per frame
					rocket.burnFuel();
					//update fuel bar width
					actualizeFuelBar(root, fuelBar, rocket);
					updateScoreIndicator();
					//controls
					root.setOnKeyPressed(k -> {
						actualizeVelTexts(k.getCode());
						if(rocket.getFuel() > 0) {
							if (rocketAnimation.getStatus() != Animation.Status.STOPPED
								&&	k.getCode() == KeyCode.UP) {
								rocket.accUpVelocity();
								centerY.setValue(centerY.getValue() + rocket.getInsUpVelocity());
								rocket.restartInsFallVelocity();
							}
						/*	else if (k.getCode() == KeyCode.DOWN)
								centerY.setValue(centerY.getValue() + 6); */
							else if (rocketAnimation.getStatus() != Animation.Status.STOPPED
									&& k.getCode() == KeyCode.LEFT) {
								rocket.accLeftVelocity();
								centerX.setValue(centerX.getValue() + rocket.getInsLeftVelocity());
								rocket.restartInsFallVelocity();
							}
							else if (rocketAnimation.getStatus() != Animation.Status.STOPPED
									&& k.getCode() == KeyCode.RIGHT) {
								rocket.accRightVelocity();
								centerX.setValue(centerX.getValue() + rocket.getInsRightVelocity());
								rocket.restartInsFallVelocity();
							}
							else if(k.getCode() == KeyCode.SPACE) {
								if(rocketAnimation.getStatus() == Animation.Status.STOPPED) {
									rocketAnimation.playFromStart();
									timer.runTimer();
									rocket.runFuelBurn();
								}
								else {
									rocketAnimation.stop();
									timer.pauseTimer();
									rocket.pauseFuelBurn();
								}
							}
							}
					});
					/*
					When user realease key, velocity returns to starting value
					 */
					root.setOnKeyReleased(k -> {
						if(k.getCode() == KeyCode.UP) {
							actualizeVelTexts(k.getCode());
							rocket.resetUpVelocity();
						}
						else if(k.getCode() == KeyCode.LEFT) {
							actualizeVelTexts(k.getCode());
							rocket.resetLeftVelocity();
						}
						else if(k.getCode() == KeyCode.RIGHT) {
							actualizeVelTexts(k.getCode());
							rocket.resetRightVelocity();
						}
					});
				})
		);
		rocketAnimation.setCycleCount(Timeline.INDEFINITE);
		rocketAnimation.playFromStart();

	}
	/*
	Method actualizes filling of FuelBar
	We must to remove old fill to paint new one
	 */
	private void actualizeFuelBar(Group root, FuelBar fuelBar, Rocket rocket) {
		root.getChildren().remove(fuelBarRectangle);
		fuelBarRectangle = fuelBar.updateFuelLevel(rocket);
		root.getChildren().add(fuelBarRectangle);
	}
	/*
	Method sets display of velocity by x axis
	if rocket can land display is green, else is red
	 */
	private void setVelX() {
		// instant value of rocket by x axis
		float xValue = rocket.getInsRightVelocity() + rocket.getInsLeftVelocity();
		velXText = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("VelocityX:");
		builder.append(Float.toString(xValue));
		velXText.setText(builder.toString());
		velXText.setX(3);
		velXText.setY(10);
		if(Math.abs(xValue) < maxVelX)
			velXText.setFill(Color.GREEN);
		else
			velXText.setFill(Color.RED);
		Font font = new Font(14);
		velXText.setFont(font);
	}
	/*
	Method actualizes velYText when any button is pressed, or released,
	if rocket can land display is green, else is red
	 */
	private void setVelX(KeyCode k) {
		// instant value of rocket by x axis
		float xValue = rocket.getInsRightVelocity() + rocket.getInsLeftVelocity();
		velXText = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("PredkoscX:");
		builder.append(Float.toString(xValue));
		velXText.setText(builder.toString());
		velXText.setX(3);
		velXText.setY(10);
		if(Math.abs(xValue) < maxVelX)
			velXText.setFill(Color.GREEN);
		else
			velXText.setFill(Color.RED);
		Font font = new Font(14);
		velXText.setFont(font);
	}

	/*
	Method actualizes velYText when any button is pressed, or released
	if rocket can land display is green, else is red
	 */
	private void setVelY(KeyCode k) {
		// instant value of rocket by y axis
		float yValue = 0;
		velYText = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("VelocityY:");
		if(k == KeyCode.UP) {
			yValue = rocket.getInsFallVelocity() + rocket.getInsUpVelocity();
			builder.append(Double.toString(Utils.round(yValue)));
		}
		else {
			yValue = rocket.getInsFallVelocity();
			builder.append(Double.toString(yValue));
		}
		velYText.setText(builder.toString());
		velYText.setX(3);
		velYText.setY(25);
		if(yValue < maxVelY)
			velYText.setFill(Color.GREEN);
		else
			velYText.setFill(Color.RED);
		Font font = new Font(14);
		velYText.setFont(font);
	}
	/*
	Method actualizes velYText when any button isn`t pressed,
	if rocket can land display is green, else is red
	 */
	private void setVelY() {
		// instant value of rocket by y axis
		float yValue = rocket.getInsFallVelocity();
		root.getChildren().remove(velYText);
		velYText = new Text();
		StringBuilder builder = new StringBuilder();
		builder.append("VelocityY:");
		builder.append(Utils.round(yValue));
		velYText.setText(builder.toString());
		velYText.setX(3);
		velYText.setY(25);
		if(yValue < maxVelY)
			velYText.setFill(Color.GREEN);
		else
			velYText.setFill(Color.RED);
		Font font = new Font(14);
		velYText.setFont(font);
		root.getChildren().add(velYText);
	}
	/*
	Method actualizes velocity Texts, removes old texts, to add new ones
	 */
	private void actualizeVelTexts(KeyCode k) {
		root.getChildren().removeAll(velXText, velYText);
		setVelX();
		setVelY(k);
		setVelY(k);
		root.getChildren().addAll(velXText, velYText);
	}
	/*
	Method paints all elements on screen every change of stage width
	 */
	private void WidthScalability(float oldValue, float newValue) {
		if(root == null) return;
		float factor = newValue/oldValue;
			if(mountains != null)
				for(int i = 0; i < mountains.length; i++)
					repaintMountainWidth(i, factor);
			repaintRocketWidth(factor);
			repaintLandingZoneWidth(factor);
			repaintFuelRectangleWidth(factor);
			repaintFuelBarWidth(factor);
			repaintTimerWidth();
			if((coins != null) && (coinsCircle != null))
				for(int i = 0; i < coinsCircle.length; i++)
					repaintCoinWidth(i, factor);
			actualizeMovingPropertiesWidth(factor);
			actualizeScoreIndicatorWidth(factor);
	}
	/*
	Method paints all elements on screen every change of stage height
	 */
	private void HeightScalability(float oldValue, float newValue) {
		if(root == null) return;
		float factor = newValue/oldValue;
		if(mountains != null)
			for(int i = 0; i < mountains.length; i++)
				repaintMountainHeight(i, factor);
		repaintRocketHeight(factor);
		repaintLandingZoneHeight(factor);
		repaintFuelRectangleHeight(factor);
		if((coins != null) && (coinsCircle != null))
			for(int i = 0; i < coinsCircle.length; i++)
				repaintCoinHeight(i, factor);
		actualizeMovingPropertiesHeight(factor);
		actualizeScoreIndicatorHeight(factor);
		actualizeLevelTextHeight(factor);
	}
	private void repaintRocketWidth(float factor) {
		if(circle != null) {
			double x = circle.getCenterX() * factor;
			double y = circle.getCenterY();
			double radiusX = circle.getRadiusX() * factor;
			double radiusY = circle.getRadiusY();
			root.getChildren().remove(circle);
			circle = new Ellipse(x, y, radiusX, radiusY);
			centerX.setValue(x);
			centerY.setValue(y);
			circle.centerXProperty().bind(centerX);
			circle.centerYProperty().bind(centerY);
			root.getChildren().add(circle);
		}
	}
	private void repaintRocketHeight(float factor) {
		if(circle != null) {
			double x = circle.getCenterX();
			double y = circle.getCenterY()* factor;
			double radiusX = circle.getRadiusX();
			double radiusY = circle.getRadiusY()* factor;
			root.getChildren().remove(circle);
			circle = new Ellipse(x, y, radiusX, radiusY);
			centerX.setValue(x);
			centerY.setValue(y);
			circle.centerXProperty().bind(centerX);
			circle.centerYProperty().bind(centerY);
			root.getChildren().add(circle);
		}
	}
	private void repaintMountainWidth(int mountainNumber, float factor) {
		if (mountains[mountainNumber] != null) {
			float startX, endX;
			if (mountainNumber == 0)
				startX = (float) stage.getMinWidth();
			else
				startX = (float) mountains[mountainNumber].getStartX() * factor;
			if (mountainNumber == mountains.length)
				endX = (float) stage.getMaxWidth();
			else
				endX = (float) mountains[mountainNumber].getEndX() * factor;
			root.getChildren().remove(mountains[mountainNumber]);
			mountains[mountainNumber].setStartX(startX);
			mountains[mountainNumber].setEndX(endX);
			mountains[mountainNumber].setControlX1((float) mountains[mountainNumber].getControlX1() * factor);
			mountains[mountainNumber].setControlX2((float) mountains[mountainNumber].getControlX2() * factor);
			root.getChildren().add(mountains[mountainNumber]);
		}
	}
	private void repaintMountainHeight(int mountainNumber, float factor) {
		if (mountains[mountainNumber] != null) {
			root.getChildren().remove(mountains[mountainNumber]);
			mountains[mountainNumber].setStartY((float) mountains[mountainNumber].getStartY() * factor);
			mountains[mountainNumber].setEndY((float) mountains[mountainNumber].getEndY() * factor);
			mountains[mountainNumber].setControlY1((float) mountains[mountainNumber].getControlY1() * factor);
			mountains[mountainNumber].setControlY2((float) mountains[mountainNumber].getControlY2() * factor);
			root.getChildren().add(mountains[mountainNumber]);
		}
	}
	/*
	*	Method counting the final score of the level
	* @ int endTime, Rocket rocket
	 */
	private void countFinalScore(int endTime, Rocket rocket) {
		//higher difficulties has bonus
		score = (Player.getChosenDifficulty().ordinal()) * 100 +
				score - (endTime * 5) + rocket.getFuel() * 200;
		if(score < 0) score = 0;
		score = Math.round(score);
		Player.incrementScore(score);
		Player.levelCompleted();
	}

	private void repaintLandingZoneWidth(float factor) {
		if(line != null) {
			root.getChildren().remove(line);
			line.setStartX((float) line.getStartX() * factor);
			line.setEndX((float) line.getEndX() * factor);
			root.getChildren().add(line);
		}
	}
	private void repaintLandingZoneHeight(float factor) {
		if(line != null) {
			root.getChildren().remove(line);
			line.setStartY((float) line.getStartY() * factor);
			line.setEndY((float) line.getEndY() * factor);
			line.setStrokeWidth((float) line.getStrokeWidth() * factor);
			root.getChildren().add(line);
		}
	}
	private void eatCoin() {
		score += 100;
	}
	private void repaintFuelRectangleWidth(float factor) {
		if(fuelRectangle != null) {
			root.getChildren().remove(fuelRectangle);
			fuel.setX((float)fuelRectangle.getX() * factor);
			fuel.setWidth((float)fuelRectangle.getWidth() * factor);
			fuelRectangle = fuel.paint();
			root.getChildren().add(fuelRectangle);
		}
	}
	private void repaintFuelRectangleHeight(float factor) {
		if(fuelRectangle != null) {
			root.getChildren().remove(fuelRectangle);
			fuel.setY((float)fuelRectangle.getY() * factor);
			fuel.setHeight((float)fuelRectangle.getHeight() * factor);
			fuelRectangle = fuel.paint();
			root.getChildren().add(fuelRectangle);
		}
	}
	private void repaintFuelBarWidth(float factor) {
		if(fuelBarRectangle == null || fuelBar == null || rocket == null) return;
		root.getChildren().removeAll(fuelBarRectangle, fuelBarCountor);
		fuelBar.setX(stage.getWidth() - fuelBar.getEdgeDistance());
		fuelBar.setWidth(factor);
		fuelBarRectangle = fuelBar.updateFuelLevel(rocket);
		fuelBarCountor = fuelBar.paint();
		root.getChildren().addAll(fuelBarRectangle, fuelBarCountor);
	}
	private void repaintTimerWidth() {
		if(timer == null) return;
		root.getChildren().remove(timeText);
		timeText = setTimeText((stage.getWidth() - timer.getEdgeDistance()));
		root.getChildren().add(timeText);
	}
	private void repaintCoinWidth(int coinNumber, float factor) {
		if(coinsCircle[coinNumber] == null) return;
		root.getChildren().remove(coinsCircle[coinNumber]);
		coinsCircle[coinNumber].setCenterX(coinsCircle[coinNumber].getCenterX() * factor);
		coinsCircle[coinNumber].setRadiusX(coinsCircle[coinNumber].getRadiusX() * factor);
		root.getChildren().add(coinsCircle[coinNumber]);
	}
	private void repaintCoinHeight(int coinNumber, float factor) {
		if(coinsCircle[coinNumber] == null) return;
		root.getChildren().remove(coinsCircle[coinNumber]);
		coinsCircle[coinNumber].setCenterY(coinsCircle[coinNumber].getCenterY() * factor);
		coinsCircle[coinNumber].setRadiusY(coinsCircle[coinNumber].getRadiusY() * factor);
		root.getChildren().add(coinsCircle[coinNumber]);
	}
	/*
	Method actualizes velocities and acceleration relative to OX axis
	 */
	private void actualizeMovingPropertiesWidth(float factor) {
		rocket.setAcceleration(rocket.getAcceleration() * factor);
		rocket.setRightVelocity(rocket.getRightVelocity() * factor);
		rocket.setLeftVelocity(rocket.getLeftVelocity() * factor);
		maxVelX *= factor;
	}
	/*
	Method actualizes velocities and acceleration relative to OY axis
	 */
	private void actualizeMovingPropertiesHeight(float factor) {
		rocket.setFallVelocity(rocket.getFallVelocity() * factor);
		rocket.setInsFallVelocity(rocket.getInsFallVelocity() * factor);
		rocket.setFallAcceleration(rocket.getFallAcceleration() * factor);
		maxVelY *= factor;
	}
	private void actualizeScoreIndicatorHeight(float factor) {
		scoreIndicator.setY(scoreIndicator.getY() * factor);
	}
	private void actualizeScoreIndicatorWidth(float factor) {
		scoreIndicator.setX(scoreIndicator.getX() * factor);
	}
	private void actualizeLevelTextHeight(float factor) {
		levelNumber.setY(scoreIndicator.getY() * factor);
	}

	/*
		Method configuring the score indicator on game start
	 */
	private void setupScoreIndicator() {
		StringBuilder builder = new StringBuilder();
		builder.append("Score: ");
		builder.append(Double.toString(score));
		scoreIndicator.setText(builder.toString());
		scoreIndicator.setX(300);
		scoreIndicator.setY(330);
		Font font = new Font(20);
		scoreIndicator.setFont(font);
	}

	/*
	*	Method updating the score displayed on the screen
	 */
	private void updateScoreIndicator() {
		StringBuilder builder = new StringBuilder();
		builder.append("Score: ");
		builder.append(Double.toString(score));
		scoreIndicator.setText(builder.toString());
	}
}

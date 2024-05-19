/**
 * java
 * This class represents a visual screen displaying the main game board.
 * Displayed after choosing a game piece or loading an existing save.
 */

package com.candyland.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

public class GameScreen extends ScreenAdapter
{
	// buttons and images
	private Texture pausedLogo;
	private Texture drawCard;
	private Texture yourCard;
	private Rectangle drawButton;
	private Texture exitTexture;
	private Texture saveTexture;
	private Texture savedTexture;
	private Rectangle exitButton;
	private Rectangle saveButton;
	private Texture peppermintTexture;
	private Texture peanutTexture;
	private Texture gumdropTexture;
	private Texture lollipopTexture;
	private Texture rainbowShortcutTexture;
	private Texture gumdropShortcutTexture;
	private Rectangle playButton;
	private Texture playTexture;
	private Texture pauseTexture;
	private Rectangle pauseButton;

	private CandyLandMain game;
	private TiledMap gameSpaces;
	private OrthographicCamera camera = new OrthographicCamera();
	private OrthogonalTiledMapRenderer renderer;

	// variables used to store game board information
	private Sprite[] playerSprites = new Sprite[GAME_PIECES];
	private Sprite[] playerSpritesDisplayed = new Sprite[GAME_PIECES];
	private Texture[] playerTextures = new Texture[GAME_PIECES];
	public CandyLandSpaces[] gamePositions = new CandyLandSpaces[137];
	public boolean pausedState = false;

	public int[] playerTokens = new int[GAME_PIECES];
	public float positions[][] = new float[GAME_PIECES][2];
	private boolean userSaved = false;
	
	// final variables that do not change
	public final float RENDER_SCALE = 1/1.2f;
	private final int SQUARE_SIZE = 32;
	public final int MAX_POSITIONS = gamePositions.length - 1;
	
	private final String[] COLORS = {"Purple", "Yellow", "Blue", "Green", "Orange", "Red"};
	private final int SINGLE_CARDS = 8;
	private final int DOUBLE_CARDS = 2;

	// Local Variables
	private int playerIndex;
	public boolean drawCardPressed = false;

	// Public Variables
	public final static int GAME_PIECES = 4;
	public boolean loadedGame = false;
	public boolean[] skipNextTurn = new boolean[GAME_PIECES];
	public int[] playerPositions = new int[GAME_PIECES];
	public boolean[] skipCurrentTurn = new boolean[GAME_PIECES];
	public boolean validPress = false;
	public boolean activeGame = false;
	public boolean firstPress = true;
	public boolean[] shortcutTaken = new boolean[GAME_PIECES];
	public String[][] drawnCards = new String[GAME_PIECES][2];
	public int gameWinner = -1;
	public boolean validMousePress = false;
	public List<String> cards;
	public CandyLandLoad LoadSaveManager = new CandyLandLoad();
	
	// necessary for white-box testing to remove GUI/LibGDX calls
	public boolean includesScreenLogic = true;
	public boolean enableLicoriceForTest = false;

	public GameScreen(final CandyLandMain game)
	{
		this.game = game;
	}
	
	public void initializeGame()
	{
		// load preferences set by settings screen
		Preferences preferences = Gdx.app.getPreferences("candyland-prefs");
		boolean playMusicDuringGame = preferences.getBoolean("music");
		if (!playMusicDuringGame)
		{
			MainMenuScreen.mainMenuMusic.stop();
		}
		createButtons();
		createBoardSpaces();
		handleInputs();
		createSprites();

		if (!loadedGame)
		{
			// game not loaded, place sprites at the default position
			setDefaultPiecePositions();
		}
		else
		{
			// game loaded, place sprites at their saved position
			for (int i = 0; i < GAME_PIECES; i++)
			{
				setPiecePosition();
				incrementIndex();
			}
			// disable save button after loading a game as there is nothing to save
			userSaved = true;
		}
		
		// Initialize cards if game was not loaded
		// Otherwise, these arrays are filled by the load method
		if (!loadedGame)
		{
			initializeCards();
			shuffleCards();
		}

		initializeLicorice();
		resetShortcutTaken();

		if (CandyLandMain.DEBUG)
		{
			System.out.println("Welcome to Candy Land!\n");
			System.out.println("Your goal is to reach the end before the other players do!\n");
			System.out.println("Press RIGHT ARROW KEY to draw a card!\n");
		}

		// Ensure user can interact with the GameScreen upon creation
		playerIndex = 0;
		validPress = true;
		drawCardPressed = false;
	}

	/**
	 * Create all GameScreen images and their respective rectangles indiciating
	 * their physical interactable location if applicable
	 */
	private void createButtons()
	{
		drawCard = new Texture(Gdx.files.internal("drawCardButton.png"));
		yourCard = new Texture(Gdx.files.internal("yourCard.png"));
		pausedLogo = new Texture(Gdx.files.internal("Paused.png"));
		exitTexture = new Texture(Gdx.files.internal("textExitButton.png"));
		saveTexture = new Texture(Gdx.files.internal("saveButton.png"));
		savedTexture = new Texture(Gdx.files.internal("savedButton.png"));
		peppermintTexture = new Texture(Gdx.files.internal("peppermint-56.png"));
		peanutTexture = new Texture(Gdx.files.internal("peanut.png"));
		gumdropTexture = new Texture(Gdx.files.internal("gumdrop.png"));
		lollipopTexture = new Texture(Gdx.files.internal("lollipop.png"));
		rainbowShortcutTexture = new Texture(Gdx.files.internal("arrow.png"));
		gumdropShortcutTexture = new Texture(Gdx.files.internal("arrow.png"));
		playTexture = new Texture(Gdx.files.internal("testPlayButtonOne.png"));
		pauseTexture = new Texture(Gdx.files.internal("pause.jpg"));

		drawButton = new Rectangle();
		drawButton.x = (Gdx.graphics.getWidth() - drawCard.getWidth()) / 2 + 75;
		drawButton.y = (Gdx.graphics.getHeight() - drawCard.getWidth() - 25);
		drawButton.width = drawCard.getWidth();
		drawButton.height = drawCard.getHeight();

		exitButton = new Rectangle();
		exitButton.x = (Gdx.graphics.getWidth() - exitTexture.getWidth()) / 3f;
		exitButton.y = 50;
		exitButton.width = exitTexture.getWidth();
		exitButton.height = exitTexture.getHeight() - 20;

		saveButton = new Rectangle();
		saveButton.x = (Gdx.graphics.getWidth() - saveTexture.getWidth()) / 1.65f;
		saveButton.y = 50;
		saveButton.width = saveTexture.getWidth();
		saveButton.height = saveTexture.getHeight() - 20;

		playButton = new Rectangle();
		playButton.x = ((Gdx.graphics.getWidth() - playTexture.getWidth()) / 2.10f);
		playButton.y = ((Gdx.graphics.getHeight()) / 3.2f) + 30;
		playButton.width = playTexture.getWidth();
		playButton.height = playTexture.getHeight();
		
		pauseButton = new Rectangle();
		pauseButton.x = 1150;
		pauseButton.y = 875;
		pauseButton.width = pauseTexture.getWidth();
		pauseButton.height = pauseTexture.getHeight();
	}

	/**
	 * Handle user's ability to interact with the GameScreen
	 */
	private void handleInputs()
	{
		Gdx.input.setInputProcessor(new InputAdapter()
		{
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button)
			{
				float touchX = screenX * camera.viewportWidth / Gdx.graphics.getWidth();
				float touchY = (Gdx.graphics.getHeight() - screenY) * camera.viewportHeight / Gdx.graphics.getHeight();

				if (drawButton.contains(touchX, touchY) && validPress && !pausedState)
				{
					// user clicked draw card
					// user has not saved and this is a valid press
					userSaved = false;
					validMousePress = true;
					return true;
				}
				if (exitButton.contains(touchX, touchY) && pausedState)
				{
					// user clicked exit button on pause screen
					// return to main menu
					pausedState = false;
					dispose();
					resetValues();
					game.setScreen(new MainMenuScreen(game));
					return true;
				}
				if (saveButton.contains(touchX, touchY) && pausedState && !userSaved)
				{
					// user saved the game on pause screen
					LoadSaveManager.write(1, getPlayerTokens(), playerPositions, cards, skipNextTurn);
					userSaved = true;
					return true;
				}
				if (playButton.contains(touchX, touchY) && pausedState)
				{
					// user pressed play on pause screen, resuming gameplay
					pausedState = false;
					return true;
				}
				if (pauseButton.contains(touchX, touchY) && !pausedState)
				{
					// user paused the game
					pausedState = true;
					return true;
				}
				return false;
			}
			@Override
			public boolean keyDown(int keycode)
			{
				// player pressed escape and was already in pause menu
				// close pause menu, return to game
				if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) && pausedState)
				{
					pausedState = false;
					return true;
				}
				// player pressed escape and was not on pause menu
				// open pause menu
				if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) && !pausedState)
				{
					pausedState = true;
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Create each player's sprite on initialization of GameScreen
	 */
	private void createSprites()
	{
		for (int i = 0; i < GAME_PIECES; i++)
		{
			createSprites(i);
		}
	}

	/**
	 * Create game board space for every colored location on the board, up to MAX_POSITIONS.
	 * Exact positioning follows location of each individual tile on the TiledMap
	 * (Requirement 2.1.0)
	 */
	public void createBoardSpaces()
	{
		int x = 32;
		int y = 32;
		int lr = 1;
		for (int i = 0; i <= MAX_POSITIONS; i++)
		{
			if ((x == 1248) || (x == 32 && y != 32))
			{
				createSpace(i, x, y);
				i++;
				y += 64;
				createSpace(i, x, y);
				i++;
				y += 64;
				lr = (x == 1248) ? 0 : 1;
			}

			if (lr == 1)
			{
				createSpace(i, x, y);
				x += 64;
			}

			if (lr == 0)
			{
				createSpace(i, x, y);
				x -= 64;
			}
		}
	}

	/**
	 * Scale game board spaces and create them from the x and y, at location i.
	 * 
	 * @param i - index of game board position
	 * @param x - x value of the position on the board
	 * @param y - y value of the position on the board
	 */
	private void createSpace(int i, int x, int y)
	{
		x = (int) (RENDER_SCALE * x);
		y = (int) (RENDER_SCALE * y);
		gamePositions[i] = new CandyLandSpaces(x, y);
	}

	/**
	 * Set every game piece to the four corners of the first square
	 * on the game board space.
	 */
	private void setDefaultPiecePositions()
	{
		for (int i = 0; i < GAME_PIECES; i++)
		{
			Sprite playerPiece = getPlayerSprite(i);
			int currentPosition = getCurrentPositionIndex(i);

			float floatX = gamePositions[currentPosition].returnX() + (SQUARE_SIZE / 2) - (playerPiece.getWidth());
			float floatY = gamePositions[currentPosition].returnY() + (SQUARE_SIZE / 2) - (playerPiece.getHeight());
			
			// logic to adjust position to each corner of the first square
			// determined by player index
			float offsetX = (i % 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE : -SQUARE_SIZE / 2 * RENDER_SCALE;
			float offsetY = (i / 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2)) : -SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2));

			// save adjusted X and Y values into positions array,
			// used by the animation calls in setPiecePosition().
			positions[i][0] = floatX + offsetX;
			positions[i][1] = floatY + offsetY;
			playerPiece.setPosition(positions[i][0], positions[i][1]);
		}
	}

	/**
	 * Render each frame
	 */
	@Override
	public void render(float delta)
	{
		ScreenUtils.clear(255, 255, 255, 255);

		renderer.setView(camera);
		camera.update();
		renderer.render();

		// Accept right arrow key input method from candyLandGame
		handleInput();

		game.batch.begin();
		game.batch.draw(peppermintTexture, 1012, 52);
		game.batch.draw(peanutTexture, 532, 319);
		game.batch.draw(gumdropTexture, 212, 107);
		game.batch.draw(lollipopTexture, 800, 427);
		game.batch.draw(rainbowShortcutTexture, 716, 160);
		game.batch.draw(gumdropShortcutTexture, 395, 267);

		/**
	 	* Pause screen that allows user to save, return to menu, or return to game
		* (Requirement 2.1.1)
	 	*/
		if (pausedState)
		{
			// game is paused, display the pause screen
			game.batch.draw(pausedLogo, Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() / 2 + 100);
			game.batch.draw(exitTexture, exitButton.x, exitButton.y);
			if (!userSaved && validPress && activeGame)
			{
				// player has not saved this round
				game.batch.draw(saveTexture, saveButton.x, saveButton.y);
			}
			else if (validPress)
			{
				// player has already saved this round
				game.batch.draw(savedTexture, saveButton.x, saveButton.y);
			}
			game.batch.draw(playTexture, playButton.x, playButton.y);

			game.font.setColor(Color.PINK);
			GlyphLayout exitToMainMenuText = new GlyphLayout(game.font, "Exit to Main Menu?\n\nProgress will NOT be automatically saved\n\nPress Exit to Confirm");
			game.font.draw(game.batch, exitToMainMenuText, 50, 900);
		}
		else
		{
			// game is not paused, display the normal board
			game.batch.draw(pauseTexture, pauseButton.x, pauseButton.y);

			if (firstPress)
			{
				// Display text instructions on how to play the game
				// only if a user has not drawn a card
				game.font.setColor(Color.PINK);
				game.font.draw(game.batch, "Welcome to Candy Land!\n\n\nYour goal is to reach the finish before your opponents!\n\n\nEach card drawn will progress your game piece!\n\n\nPress the RIGHT ARROW key or click the draw button to draw your first card!\n\n\nPress the ESCAPE key or click the pause button to pause the game!", 60, 945);
			}
			else
			{
				// display player's game pieces and their names in
				// top left corner
				game.font.setColor(Color.RED);
				game.font.draw(game.batch, "You", 50, 875);
				game.font.draw(game.batch, "Computer 1", 140, 875);
				game.font.draw(game.batch, "Computer 2", 290, 875);
				game.font.draw(game.batch, "Computer 3", 440, 875);
			}

			/**
	 		* Display button user can click to draw a card
			* (Requirement 2.1.2)
	 		*/
			game.batch.draw(drawCard, drawButton.x, drawButton.y);
			for (int i = 0; i < GAME_PIECES; i++)
			{
				// display each player's game piece
				Sprite playerSprite = playerSprites[i];
				playerSprite.draw(game.batch);

				if (!firstPress)
				{
					// display player's game pieces and their names in
					// top left corner
					Sprite playerSpriteDisplayed = playerSpritesDisplayed[i];
					playerSpriteDisplayed.draw(game.batch);
				}

				if (i == 0)
				{
					/**
					 * Set "You" text over user's game piece on game board
					 * (Requirement 4.3.1)
					 */
					game.font.setColor(Color.SKY);
					GlyphLayout youText = new GlyphLayout(game.font, "You");
					float youTextX = playerSprite.getX() + playerSprite.getWidth() / 2 - youText.width / 2 - 2;
					float youTextY = playerSprite.getY() + playerSprite.getHeight() + youText.height - 1;
					game.font.draw(game.batch, youText, youTextX, youTextY);
				}

				if (drawnCards[i][0] != null)
				{
					// display drawn card if it has been drawn this round
					String[] drawnCard = drawnCards[i][0].split(" ");
					float cardX = i == 0 ? 50 : i * 152;
					float cardY = 825;
					// format card text to wrap a line below for cards with spaces
					for (String card : drawnCard)
					{
						GlyphLayout drawnCardText = new GlyphLayout(game.font, card);
						determineCardColor(i);
						game.font.draw(game.batch, card, cardX, cardY);
						cardY -= drawnCardText.height * 2;
					}
				}

				if (drawnCards[i][1] != null)
				{
					// display second card if it has been drawn this round
					// only applicable on Extreme difficulty
					String[] drawnSecondCard = drawnCards[i][1].split(" ");
					float secondCardX = i == 0 ? 50 : i * 152;
					float secondCardY = 775;
					// format card text to wrap a line below for cards with spaces
					for (String card : drawnSecondCard)
					{
						GlyphLayout secondCardText = new GlyphLayout(game.font, card);
						determineCardColor(i);
						game.font.draw(game.batch, card, secondCardX, secondCardY);
						secondCardY -= secondCardText.height * 2;
					}
				}
				else if (shortcutTaken[i])
				{
					// display shortcut taken through visual text below card drawn
					float shortcutTextX = i == 0 ? 50 : i * 146;
					float shortcutTextY = 775;
					String shortcutDestination = getCurrentColor(playerPositions[i]);
					determineCardColor(i);
					shortcutDestination = "-> " + shortcutDestination;
					game.font.draw(game.batch, shortcutDestination, shortcutTextX, shortcutTextY);
				}
				else if (skipCurrentTurn[i] || skipNextTurn[i])
				{
					// display licorice text if player's turn will be skipped next round
					// needs current or next boolean to correctly display when intended
					float licoriceTextX = i == 0 ? 50 : i * 145;
					float licoriceTextY = 775;
					game.font.setColor(Color.BLACK);
					game.font.draw(game.batch, "X Licorice X", licoriceTextX, licoriceTextY);
				}
			}
			if (firstPress)
			{
				// display instructions in bottom left corner
				// when card hasn't been drawn
				game.font.setColor(Color.MAROON);
				GlyphLayout instructionsText = new GlyphLayout(game.font, "Press Right Arrow Key or Click the Draw Button to Begin!");
				game.font.draw(game.batch, instructionsText, 10, 85);
			}
			else if (validPress)
			{
				// indicate it is the user's turn
				game.font.setColor(Color.PINK);
				GlyphLayout yourTurnText = new GlyphLayout(game.font, "It is now your turn!");
				game.font.draw(game.batch, yourTurnText, 195, 750);
			}
			
			// show licorice spaces at the specified game board positions
			// these numbers line up with handleLicorice()
			drawLicorice(12);
			drawLicorice(44);
			drawLicorice(82);
		}
		game.batch.end();
	}

	/**
	 * Display licorice spaces as a black X on the game board
	 * at the specified game board positions.
	 * 
	 * @param index - game board position to display the licorice
	 */
	private void drawLicorice(int index)
	{
		// center the text
		float targetX = gamePositions[index].returnX() - 5;
		float targetY = gamePositions[index].returnY() + 5;
		game.font.setColor(Color.BLACK);
		GlyphLayout licoriceText = new GlyphLayout(game.font, "X");
		game.font.draw(game.batch, licoriceText, targetX, targetY);
	}

	/**
	 * Determine color of text to display on drawn card
	 * by finding color associated with the given board position
	 * 
	 * @param index - a player position on the game board
	 */
	private void determineCardColor(int index)
	{
		// obtain color at current player's positions
		// this gives color to location cards like 
		String color = getCurrentColor(playerPositions[index]);
		Color textColor;

		// default should never be called as all positions have a valid color
		// within the switch statement
		switch (color)
		{
			case "Purple":
				textColor = Color.PURPLE;
				break;
			case "Yellow":
				textColor = Color.YELLOW;
				break;
			case "Blue":
				textColor = Color.BLUE;
				break;
			case "Green":
				textColor = Color.GREEN;
				break;
			case "Orange":
				textColor = Color.ORANGE;
				break;
			case "Red":
				textColor = Color.RED;
				break;
			default:
				textColor = game.font.getColor();
				break;
		}

		// sets text color to determined color
		game.font.setColor(textColor);
	}

	/**
	 * Handle all player piece movement including their animations.
	 * Position determined by offsets when encountering overlapping pieces.
	 * Otherwise is centered on game board.
	 * Position stored in positions[][].
	 * (Requirement 4.0.0)
	 */
	public void movePosition()
	{
		// obtain current player's position and player piece
		int playerType = getCurrentPlayerType();
		int currentPosition = getCurrentPositionIndex(playerType);

		int tempPosition = 0;
		int numberOfPieces = 1;
		int tempIndex = 0;


		// end position for game piece, used for animation
		float targetX = gamePositions[currentPosition].returnX() + (SQUARE_SIZE / 2);
		float targetY = gamePositions[currentPosition].returnY() + (SQUARE_SIZE / 2);
		positions[playerType][0] = targetX;
		positions[playerType][1] = targetY;

		/**
		 * Handle player piece overlap by determining number of pieces overlap on a given position
		 * Position differs based on how many players land on same position.
		 * 2 players = side by side positioning
		 * 3+ players = set pieces in each corner depending on the player's index
		 * (Requirement 4.1.0)
		 */
		for (int i = 0; i < GAME_PIECES; i++)
		{
			tempPosition = getCurrentPositionIndex(i);

			// determine number of pieces that are overlapping
			if (i < playerType && tempPosition == currentPosition)
			{
				numberOfPieces++;
				// tempIndex represents a player piece that overlaps
				// we use this to handle the case of only 2 pieces overlapping
				tempIndex = i;
			}
		}

		if (numberOfPieces == 2)
		{
			// two game pieces are overlapping on same position
			positions[playerType][0] = targetX + (SQUARE_SIZE / 2 - 2);
			positions[playerType][1] = targetY - (SQUARE_SIZE / (SQUARE_SIZE / 2)) + 1;
			// use index of player position determined to be overlapping,
			// determined by previous for loop
			positions[tempIndex][0] = targetX - (SQUARE_SIZE / 2 - 3);
			positions[tempIndex][1] = targetY - (SQUARE_SIZE / (SQUARE_SIZE / 2));
		}
		else if (numberOfPieces > 2)
		{
			// more than two pieces are overlapping on the same position
			for (int i = 0; i < GAME_PIECES; i++)
			{
				tempPosition = getCurrentPositionIndex(i);
				if (currentPosition == tempPosition)
				{
					// set player offset based on their index, like we do
					// when handling default player positioning,
					// assigning pieces to corners
					float offsetX = (i % 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE : -SQUARE_SIZE / 2 * RENDER_SCALE;
					float offsetY = (i / 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2)) : -SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2));
					positions[i][0] = targetX + offsetX;
					positions[i][1] = targetY + offsetY;
				}
			}
		}
		// end of overlap code
	}
	
	public void setPiecePosition()
	{
		// obtain current player's position and player piece
		int playerType = getCurrentPlayerType();
		Sprite playerPiece = getPlayerSprite(playerType);
		int currentPosition = getCurrentPositionIndex(playerType);

		int tempPosition = 0;
		int numberOfPieces = 1;
		int tempIndex = 0;

		// start position for game piece, used for animation
		float startX = playerPiece.getX();
		float startY = playerPiece.getY();

		// end position for game piece, used for animation
		float targetX = gamePositions[currentPosition].returnX() + (SQUARE_SIZE / 2) - (playerPiece.getWidth());
		float targetY = gamePositions[currentPosition].returnY() + (SQUARE_SIZE / 2) - (playerPiece.getHeight());
		positions[playerType][0] = targetX;
		positions[playerType][1] = targetY;

		// distance the animation needs to travel
		float distanceX = targetX - startX;
		float distanceY = targetY - startY;

		// animation specifics
		float animationDuration = 0.5f;
		int steps = 60;

		// maintain player piece X and Y values at each frame of the animation
		float stepX = distanceX / steps;
		float stepY = distanceY / steps;

		/**
		 * Handle player piece overlap by determining number of pieces overlap on a given position
		 * Position differs based on how many players land on same position.
		 * 2 players = side by side positioning
		 * 3+ players = set pieces in each corner depending on the player's index
		 * (Requirement 4.1.0)
		 */
		for (int i = 0; i < GAME_PIECES; i++)
		{
			tempPosition = getCurrentPositionIndex(i);

			// determine number of pieces that are overlapping
			if (i < playerType && tempPosition == currentPosition)
			{
				numberOfPieces++;
				// tempIndex represents a player piece that overlaps
				// we use this to handle the case of only 2 pieces overlapping
				tempIndex = i;
			}
		}

		if (numberOfPieces == 2)
		{
			// two game pieces are overlapping on same position
			positions[playerType][0] = targetX + (SQUARE_SIZE / 2 - 2);
			positions[playerType][1] = targetY - (SQUARE_SIZE / (SQUARE_SIZE / 2)) + 1;
			// use index of player position determined to be overlapping,
			// determined by previous for loop
			positions[tempIndex][0] = targetX - (SQUARE_SIZE / 2 - 3);
			positions[tempIndex][1] = targetY - (SQUARE_SIZE / (SQUARE_SIZE / 2));
		}
		else if (numberOfPieces > 2)
		{
			// more than two pieces are overlapping on the same position
			for (int i = 0; i < GAME_PIECES; i++)
			{
				tempPosition = getCurrentPositionIndex(i);
				if (currentPosition == tempPosition)
				{
					// set player offset based on their index, like we do
					// when handling default player positioning,
					// assigning pieces to corners
					float offsetX = (i % 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE : -SQUARE_SIZE / 2 * RENDER_SCALE;
					float offsetY = (i / 2 == 0) ? SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2)) : -SQUARE_SIZE / 2 * RENDER_SCALE - (SQUARE_SIZE / (SQUARE_SIZE / 2));
					positions[i][0] = targetX + offsetX;
					positions[i][1] = targetY + offsetY;
				}
			}
		}
		// end of overlap code

		for (int i = 0; i < GAME_PIECES; i++)
		{
			// adjust game piece to:
			// correct position if their turn was skipped
			// account for new overlap
			// when game is loaded from save state
			if (skipCurrentTurn[i] || numberOfPieces > 1 || loadedGame)
			{
				final int playerIndex = i;
				final Sprite piece = getPlayerSprite(i);
				Timer.schedule(new Timer.Task()
				{
					@Override
					public void run()
					{
						piece.setPosition(positions[playerIndex][0], positions[playerIndex][1]);
					}
					// determine animation time based on if game was loaded or not
				}, loadedGame ? 0.0f : 0.5f);
			}
		}

		if (playerType == GAME_PIECES - 1 && activeGame && !skipNextTurn[0])
		{
			// indicate it is the user's turn, which occurs at .6 seconds
			// where this is after final computer has their animation finished.
			Timer.schedule(new Timer.Task()
			{
				@Override
				public void run()
				{
					userSaved = false;
					validPress = true;
					if (CandyLandMain.DEBUG)
					{
						System.out.println("|---------------------------------|\n");
						System.out.println("It is your turn! Press RIGHT ARROW KEY to draw a card!\n");
					}
				}
			}, loadedGame ? 0.0f : 0.6f);
		}

		/**
		 * Game piece locations animated through updating their position until
		 * reaching target destination
		 * (Requirement 4.2.0)
		 */
		if (!skipCurrentTurn[playerType] && !loadedGame)
		{
			// handle normal game piece animations if player is not on licorice
			// and game was not loaded
			for (int i = 0; i <= steps; i++)
			{
				// set and display current player piece position
				// depending on the step of the animation
				// progressively reaching the destination
				final float currentX = startX + stepX * i;
				final float currentY = startY + stepY * i;
				final int stepIndex = i;
				final Sprite piece = playerPiece;
				Timer.schedule(new Timer.Task()
				{
					@Override
					public void run()
					{
						piece.setPosition(currentX, currentY);
						if (stepIndex == steps)
						{
							// adjust final piece position to account for overlap
							piece.setPosition(positions[playerType][0], positions[playerType][1]);
						}
					}
				}, i * (animationDuration / steps));
			}
		}
		else
		{
			// current turn is over, set skip current turn to false
			// this only triggers if this was true and it is a normal round
			// that was not just loaded
			skipCurrentTurn[playerType] = false;
		}
	}

	/**
	 * Player has won the game, switch them to winner screen.
	 * 
	 * @param p - index of player that won the game, used to indicate
	 * which winner screen is displayed
	 */
	public void switchToWinnerScreen(int p)
	{
		resetValues();
		pausedState = false;
		game.setScreen(new WinnerScreen(game, p));
		return;
	}

	/**
	 * Obtain a Sprite entity from a given player index
	 * 
	 * @param index - index of a player
	 * @return playerSprites array element at the given index
	 */
	private Sprite getPlayerSprite(int index)
	{
		return playerSprites[index];
	}

	/**
	 * Create game piece sprites at the specified index.
	 * Called for each player index.
	 * 
	 * @param index - player index
	 */
	private void createSprites(int index)
	{
		// determine which texture to use for game piece
		if (index == 0)
		{
			// user's texture for their game piece determined
			// by their choice of game piece on PieceSelectionScreen
			playerTextures[index] = new Texture(Gdx.files.internal(setTextureString(PieceSelectionScreen.pieceSelection)));
			playerTokens[index] = PieceSelectionScreen.pieceSelection;
		}
		else if (PieceSelectionScreen.pieceSelection == index)
		{
			// player index equal to the piece selection index
			// we need to swap their player piece with the user's index
			// as user "took" their player piece.
			playerTextures[index] = new Texture(setTextureString(0));
			playerTokens[index] = 0;
		}
		else
		{
			// set player piece based on player index
			playerTextures[index] = new Texture(setTextureString(index));
			playerTokens[index] = index;
		}
		// create new sprite based on determined piece texture
		// this will display on the normal game board
		playerSprites[index] = new Sprite(playerTextures[index]);
		playerSprites[index].setScale(RENDER_SCALE);

		// create new sprite based on determined piece texture
		// this will display in the upper left corner
		playerSpritesDisplayed[index] = new Sprite(playerTextures[index]);
		playerSpritesDisplayed[index].setScale(1f);
		
		// determine position of displayed sprites in top left corner
		// depending on player index
		if (index == 0)
		{
			playerSpritesDisplayed[index].setPosition(50, 885);
		}
		else
		{
			playerSpritesDisplayed[index].setPosition(index * 155, 885);
		}
	}

	/**
	 * Sets the texture to use for the game piece at the specified player's index
	 * Called by createSprites()
	 * 
	 * @param index - game piece index
	 * @return string of player piece texture, associated to a file within the assets
	 */
	public String setTextureString(int index)
	{
		switch (index)
		{
			case 0:
				return "cookiePiece.png";
			case 1:
				return "sucker.png";
			case 2:
				return "candyCane.png";
			case 3:
				return "pinkCandy.png";
			default:
				return "cookiePiece.png";
		}
	}
	
	/**
	 * Reset all CandyLandGame public variables to their initial state.
	 * Calling a new GameScreen will otherwise persist these values.
	 */
	public void resetValues()
	{
		loadedGame = false;
		skipNextTurn = new boolean[GAME_PIECES];
		playerPositions = new int[GAME_PIECES];
		skipCurrentTurn = new boolean[GAME_PIECES];
		validPress = false;
		activeGame = false;
		firstPress = true;
		shortcutTaken = new boolean[GAME_PIECES];
		drawnCards = new String[GAME_PIECES][2];
		gameWinner = -1;
		validMousePress = false;
		drawCardPressed = false;
		playerIndex = 0;
		cards = new ArrayList<>();
	}

	/**
	 * Handle input of user drawing a card during an active game
	 */
	public void handleInput()
	{
		// Checks if actively viewing a valid game and it is user's turn
		if (playerIndex == 0 && validPress && gameWinner == -1 && !getPausedState())
		{
			// Accepts draw card input through RIGHT ARROW key and mouse click on the draw card button
			if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || validMousePress)
			{
				// Ensures players cannot draw multiple times by rapidly drawing
				if (!drawCardPressed)
				{
					drawCardPressed = true;
					validPress = false;
					validMousePress = false;
					activeGame = true;
					firstPress = false;
					resetDrawnCards();
					resetShortcutTaken();
					drawCard();
				}
			}
			else
			{
				drawCardPressed = false;
			}
		}
	}

	/**
	 * Reset the current rounds' drawn cards.
	 * Array is used to display card text visually on 
	 */
	public void resetDrawnCards()
	{
		for (int i = 0; i < GAME_PIECES; i++)
		{
			drawnCards[i][0] = null;
		}
	}

	/**
	 * Create the 64 card deck
	 * (Requirement 1.0.0)
	 */
	private void initializeCards()
	{
		cards = new ArrayList<>();

		/**
		* Create 48 single color cards, 8 of each color
		* (Requirement 1.0.1)
		*/
		for (String color : COLORS)
		{
			for (int i = 0; i < SINGLE_CARDS; i++)
			{
				cards.add(color);
			}
		}

		/**
		* Create 12 double color cards, 2 of each color
		* (Requirement 1.0.2)
		*/
		for (int i = 0; i < DOUBLE_CARDS; i++)
		{
			for (String color : COLORS)
			{
				cards.add("Double " + color);
			}
		}

		/**
		* Create 4 special character cards
		* (Requirement 1.0.3)
		*/
		cards.add("Peppermint Forest");
		cards.add("Gumdrop Mountains");
		cards.add("Peanut Acres");
		cards.add("Lollipop Woods");
	}

	/**
	 * Initialize licorice logic.
	 * skipCurrentTurn indicates licorice text on screen for 
	 * skipNextTurn determines skipping turns.
	 * Called only on initilization.
	 */
	private void initializeLicorice()
	{
		for (int i = 0; i < GAME_PIECES; i++)
		{
			skipCurrentTurn[i] = false;
			if (!loadedGame)
			{
				skipNextTurn[i] = false;
			}
		}
	}

	/**
	 * Reset shortcuts taken boolean.
	 * Called after user input of drawing card but before card is drawn
	 */
	public void resetShortcutTaken()
	{
		for (int i = 0; i < GAME_PIECES; i++)
		{
			shortcutTaken[i] = false;
		}
	}

	/**
	 * Shuffle card deck.
	 * Called when checkEmptyDeck() returns true
	 */
	private void shuffleCards()
	{
		if (!cards.isEmpty())
		{
			Collections.shuffle(cards);
		}
	}

	/**
	 * User draws card from deck.
	 * (Requirement 1.1.0)
	 */
	public void drawCard()
	{
		if (includesScreenLogic)
		{
			// Check valid card in deck and draw next card
			checkEmptyDeck();
			String card = nextCard();

			if (CandyLandMain.DEBUG)
			{
				System.out.println("You (" + getPiece(playerIndex) + ") drew: " + card);
			}
			drawnCards[playerIndex][0] = card;

			// Move player based on number of steps determined from calculateSteps()
			movePlayer(card, calculateSteps(card));

			removeCard();
			checkWinner();
			incrementIndex();
		}
		else
		{
			if (!enableLicoriceForTest || (!skipNextTurn[playerIndex] && enableLicoriceForTest))
			{
				checkEmptyDeck();
				String card = nextCard();

				if (CandyLandMain.DEBUG)
				{
					System.out.println("You (" + getPiece(playerIndex) + ") drew: " + card);
				}
				drawnCards[playerIndex][0] = card;

				// Move player based on number of steps determined from calculateSteps()
				movePlayer(card, calculateSteps(card));

				removeCard();
				checkWinner();
				incrementIndex();
			}
			else
			{
				skipCurrentTurn[playerIndex] = true;
				skipNextTurn[playerIndex] = false;
				incrementIndex();
			}
		}

		if (includesScreenLogic)
		{
			// 0.4 second timer to allow for space between user's draw and computers drawing cards
			Timer.schedule(new Timer.Task()
			{
				@Override
				public void run()
				{
					handleComputerTurn();
				}
			}, 0.4f);
		}
		else
		{
			handleComputerTurn();
		}
	}

	/**
	 * Computer draws card from deck
	 * (Requirement 1.1.1)
	 */
	private void handleComputerTurn()
	{
		if (includesScreenLogic)
		{
			// 0.4 second timer to allow for space between computer turns
			Timer.schedule(new Timer.Task()
			{
				@Override
				public void run()
				{
					if (playerIndex != 0 && activeGame)
					{
						// Computer not on Licorice location
						if (!skipNextTurn[playerIndex])
						{
							checkEmptyDeck();
							String card1 = nextCard();
							drawnCards[playerIndex][0] = card1;
							int steps1 = calculateSteps(card1);
							removeCard();
							String finalCard = card1;
							int finalSteps = steps1;

							// Load difficulty preference
							Preferences preferences = Gdx.app.getPreferences("candyland-prefs");
							boolean extremeDifficulty = preferences.getBoolean("difficulty");
							if (extremeDifficulty)
							{
								// Computer draws second card if Extreme difficulty selected
								checkEmptyDeck();
								final String card2 = nextCard();
								drawnCards[playerIndex][1] = card2;
								int steps2 = calculateSteps(card2);
								removeCard();

								// Determines which card drawn will progress the player further
								if (steps1 <= steps2)
								{
									finalCard = card2;
									finalSteps = steps2;
								}
								final String chosenCard = finalCard;
								final int index = playerIndex;

								// 1 second timer to visually remove the second drawn card text on GameScreen
								Timer.schedule(new Timer.Task()
								{
									@Override
									public void run()
									{
										drawnCards[index][0] = chosenCard;
										drawnCards[index][1] = null;
									}
								}, 1.0f);
								if (CandyLandMain.DEBUG)
								{
									System.out.println("Computer " + index + " (" + getPiece(index) + ") drew: " + card1 + " and " + card2);
								}
							}
							if (CandyLandMain.DEBUG)
							{
								String phrasing = extremeDifficulty ? "chose" : "drew";
								System.out.println("Computer " + playerIndex + " (" + getPiece(playerIndex) + ") " + phrasing + ": " + finalCard);
							}
							movePlayer(finalCard, finalSteps);
							checkWinner();
							incrementIndex();
							validPress = false;
						}
						else
						{
							// Computer was on Licorice location
							if (CandyLandMain.DEBUG)
							{
								System.out.println("Computer " + playerIndex + " (" + getPiece(playerIndex) + ") is on a Licorice space and had their turn skipped!");
								System.out.println("Stayed at position " + getBoardPosition(playerPositions[playerIndex]) + "/" + MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]) + "\n");
							}
							skipCurrentTurn[playerIndex] = true;
							if (includesScreenLogic)
							{
								setPiecePosition();
							}
							skipNextTurn[playerIndex] = false;
							incrementIndex();
						}

					}

					// Handle next computer's turn
					if (playerIndex != 0)
					{
						handleComputerTurn();
					}
					// Player landed on licorice space
					// Skip their turn and draw the next round of computer cards
					else if (playerIndex == 0 && skipNextTurn[playerIndex])
					{
						// 1.1 second timer to add space between rounds
						// .1 seconds longer than the visually remove second drawn card text
						// from GameScreen timer, to prevent cards being incorrectly removed from GameScreen
						Timer.schedule(new Timer.Task()
						{
							@Override
							public void run()
							{
								if (CandyLandMain.DEBUG)
								{
									System.out.println("You are on a Licorice space so your turn was skipped!");
									System.out.println("Stayed at position " + getBoardPosition(playerPositions[playerIndex]) + "/" + MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]) + "\n");
								}
								skipCurrentTurn[playerIndex] = true;
								if (includesScreenLogic)
								{
									setPiecePosition();
								}
								skipNextTurn[playerIndex] = false;
								resetDrawnCards();
								resetShortcutTaken();
								incrementIndex();
								handleComputerTurn();
							}
						}, 1.1f);
					}
				}
			}, 0.4f);
		}
		else
		{
			while (playerIndex != 0 && activeGame)
			{
				checkEmptyDeck();
				String card = nextCard();

				// Move player based on number of steps determined from calculateSteps()
				movePlayer(card, calculateSteps(card));

				removeCard();
				checkWinner();
				incrementIndex();
			}
		}
	}

	/**
	 * Returns an integer representing the location of the specified player's index
	 * 
	 * @param	index - the index of a player within playerIndex
	 * @return	integer of the specified player's position on the game board
	 */
	public int getCurrentPositionIndex(int index)
	{
		return playerPositions[index];
	}

	/**
	 * Determine if a player has won the game
	 * (Requirement 1.3.0)
	 */
	public void checkWinner()
	{
		// Prevent multiple players from winning in the same round
		if (gameWinner == -1)
		{
			for (int i = 0; i < GAME_PIECES; i++)
			{
				if (playerPositions[i] >= MAX_POSITIONS)
				{
					activeGame = false;
					validPress = false;
					if (CandyLandMain.DEBUG)
					{
						if (i != 0)
						{
							System.out.println("Computer " + i + " has won the game!");
						}
						else
						{
							System.out.println("Congratulations! You have won the game!");
						}
					}
					gameWinner = i;
					int winnerIndex = gameWinner;

					if (includesScreenLogic)
					{
						// 1.2 second timer to allow for user to see a piece has moved
						// into the FINISH location on GameScreen
						Timer.schedule(new Timer.Task()
						{
							@Override
							public void run()
							{
								switchToWinnerScreen(winnerIndex);
							}
						}, 1.2f);
						break;
					}
				}
			}
		}
	}

	/**
	 * Create a new deck of cards and shuffle if the deck is empty
	 * (Requirement 1.1.2)
	 */
	public void checkEmptyDeck()
	{
		if (cards.isEmpty())
		{
			initializeCards();
			shuffleCards();
		}
	}

	/**
	 * Obtain the game piece for a specified player index.
	 * Only used to print in console
	 * 
	 * @param index - player index
	 * @return string of player's game piece, with the file type removed
	 */
	private String getPiece(int index)
	{
		String texture = getTextureString(index);
		return texture.substring(0, texture.lastIndexOf('.'));
	}
	
	/**
	 * Returns an integer representing the number of steps required
	 * to reach the drawn card from the current position
	 * (Requirement 1.2.0)
	 * 
	 * @param	nextCard - the string of the card drawn
	 * @return	the steps needed to reach the position from the drawn card
	 */
	public int calculateSteps(String nextCard)
	{
		int steps = 0;
		/**
		 * Double color cards move to second nearest color position
		 * (Requirement 1.2.2)
		 */
		if (nextCard.contains("Double"))
		{
			String drawnColor = nextCard.substring("Double ".length());
			steps = getStepsFromColor(drawnColor);
			// Add 6 to steps needed as there are 6 colors,
			// which handles the double color draws
			steps += 6;
		}

		/**
		 * Special cards cards move to pre-determined board position
		 * (Requirement 1.2.3)
		 */
		else if (nextCard.contains("Peppermint Forest"))
		{
			steps = 20;
		}
		else if (nextCard.contains("Gumdrop Mountains"))
		{
			steps = 36;
		}
		else if (nextCard.contains("Peanut Acres"))
		{
			steps = 72;
		}
		else if (nextCard.contains("Lollipop Woods"))
		{
			steps = 99;
		}
		else
		{
			/**
			 * Single color cards move to nearest color position
			 * (Requirement 1.2.1)
			 */
			steps = getStepsFromColor(nextCard);
		}
		return steps;
	}

	/**
	 * Move the player index and piece position based on card drawn
	 * 
	 * @param	nextCard - the string of the card drawn
	 * @param 	steps - number of steps the player piece will move
	 */
	public void movePlayer(String nextCard, int steps)
	{
		// Check for 4 special cards
		if (nextCard.contains("Peppermint Forest") || nextCard.contains("Gumdrop Mountains") || nextCard.contains("Peanut Acres") || nextCard.contains("Lollipop Woods"))
		{
			//Special card drawn so set player position to value of special card
			playerPositions[playerIndex] = steps;
		}
		else
		{
			//Normal movement card so add steps to current player position
			int newPosition = playerPositions[playerIndex] + steps;
			playerPositions[playerIndex] = newPosition;
		}

		if (CandyLandMain.DEBUG)
		{
			System.out.println("Moved to position " + getBoardPosition(playerPositions[playerIndex]) + "/" + MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]));
		}

		// Handle logic of shortcuts and licorice before setting piece position
		// Ideally we should handle shortcuts differently with a timer after
		// person reaches their drawn card, to then display the shortcut movement
		handleShortcuts();
		handleLicorice();

		if (CandyLandMain.DEBUG)
		{
			System.out.println("");
		}

		// Prevent player position array from overflowing if there is a winner
		if (playerPositions[playerIndex] >= MAX_POSITIONS)
		{
			playerPositions[playerIndex] = MAX_POSITIONS;
		}
		// Move game piece on GameScreen to new position
		if (includesScreenLogic)
		{
			setPiecePosition();
		}
	}

	/**
	 * Determine if player landed on a shortcut location
	 * (Requirement 4.0.1)
	 */
	private void handleShortcuts()
	{
		if (playerPositions[playerIndex] == 27)
		{
			// Set player position to shortcut destination
			playerPositions[playerIndex] = 56;
			if (CandyLandMain.DEBUG)
			{
				System.out.println("Landed on the Rainbow Trail shortcut!");
				System.out.println("New position is " + getBoardPosition(playerPositions[playerIndex]) + "/" + MAX_POSITIONS + ": "  + getCurrentColor(playerPositions[playerIndex]));
			}
			shortcutTaken[playerIndex] = true;
		}
		else if (playerPositions[playerIndex] == 49)
		{
			// Set player position to shortcut destination
			playerPositions[playerIndex] = 74;
			if (CandyLandMain.DEBUG)
			{
				System.out.println("Landed on the Gumdrop Pass shortcut!");
				System.out.println("New position is " + getBoardPosition(playerPositions[playerIndex]) + "/" + MAX_POSITIONS + ": "  + getCurrentColor(playerPositions[playerIndex]));
			}
			shortcutTaken[playerIndex] = true;
		}
	}

	/**
	 * Determine if player landed on a licorice location
	 * (Requirement 4.0.2)
	 */
	private void handleLicorice()
	{
		if (playerPositions[playerIndex] == 12 || playerPositions[playerIndex] == 44 || playerPositions[playerIndex] == 82)
		{
			if (CandyLandMain.DEBUG)
			{
				System.out.println("Landed on Licorice so the next turn will be skipped!");
			}
			skipNextTurn[playerIndex] = true;
		}
	}

	/**
	 * Return integer of the visual location of a player piece on game board
	 * 
	 * @param	currentPlayerPosition - the location of a player position
	 * @return	the location of the input + 1
	 */
	private int getBoardPosition(int currentPlayerPosition)
	{
		return currentPlayerPosition + 1;
	}

	/**
	 * Determine the number of steps a given color card will move a player
	 * 
	 * @param	color - a card color
	 * @return	the number of steps the current player will be moved
	 */
	public int getStepsFromColor(String color)
	{
		// Obtain index of input color within COLORS array
		// We add 1 because we want to know the card number (1 : 6)
		int drawnCardValue = Arrays.asList(COLORS).indexOf(color) + 1;
		int steps = 0;
		
		// Obtain card value of current player's position
		int currentCardValue = getCardValue(playerPositions[playerIndex]);

		if (currentCardValue < 0)
		{
			currentCardValue = 0;
		}

		// Current position color's value is greater than drawn card's value
		if (currentCardValue > drawnCardValue)
		{
			// Subtract current card's value from color array length
			// and add the drawn card's value to this number to obtain steps
			int temp = COLORS.length - currentCardValue;
			temp += drawnCardValue;
			steps = temp;
		}
		// Current position's color is the same as the drawn color
		else if (currentCardValue == drawnCardValue)
		{
			// length of array will move to same color
			steps = COLORS.length;
		}
		// Current position color value is less than drawn card color value
		else
		{
			// Subtract drawn card's value from current position value
			steps = drawnCardValue - currentCardValue;
			if (steps < 0)
			{
				steps = Math.abs(steps);
			}
		}
		return steps;
	}

	/**
	 * Remove card from deck.
	 * Only called after checkEmptyDeck()
	 */
	private void removeCard()
	{
		cards.remove(0);
	}

	/**
	 * Determine the color of a playerPositions position
	 * 
	 * @param	position - a location within the playerPositions array (0 : X)
	 * @return	the color of the specified position
	 */
	public String getCurrentColor(int position)
	{
		// Start position
		if (position == 0)
		{
			// Last element in array is first color on the board
			return COLORS[COLORS.length - 1];
		}
		// Not the start position
		else
		{
			// Remainder indicates game board position
			int color = position % COLORS.length;

			// No remainder, last position in the array, first position on board
			if (color == 0)
			{
				// String of last element in array, first position on board
				return COLORS[COLORS.length - 1];
			}
			// Not the last position in the array
			else
			{
				// Subtract one to obtain COLORS array position
				// color indicates game board position (COLORS + 1)
				// 0 : 4
				return COLORS[color - 1];
			}
		}
	}

	/**
	 * Determine the value of a card
	 * 
	 * @param	position - a location within the playerPositions array (0 : X)
	 * @return	the game board value of the specified position
	 */
	public int getCardValue(int position)
	{
		// Start position
		if (position == 0)
		{
			// value of starting location
			return 0;
		}
		else
		{
			// Remainder indicates game board position
			int color = position % COLORS.length;

			// No remainder, last position in the array, first position on board (0 : 5)
			if (color == 0)
			{
				return COLORS.length;
			}
			else
			{
				// game board position
				// 1 : 5
				return color;
			}
		}
	}

	/**
	 * Obtain the next card to be drawn
	 * 
	 * @return	the string of the next card in the deck
	 */
	private String nextCard()
	{
		return cards.get(0);
	}

	/**
	 * Increment current player index
	 * Starts over at 0 once it would reach GAME_PIECES value.
	 */
	public void incrementIndex()
	{
		playerIndex = (playerIndex + 1) % GAME_PIECES;
	}

	/**
	 * Obtain the index of the current player.
	 * 0 = User.
	 * 1 : 3 = Computer.
	 * 
	 * @return	the index of the current player
	 */
	public int getCurrentPlayerType()
	{
		return playerIndex;
	}

	/**
	 * Check if there is a winner
	 * 
	 * @return	integer indicating which player index won the game. Returns -1 if no winner.
	 */
	public int getWinner()
	{
		return gameWinner;
	}

	/**
	 * Load a saved game state through LoadStateManager
	 * 
	 * @param	slotNumber - slot of the desired game save
	 * @return	boolean value indicating if save was loaded
	 */
	public boolean load(int slotNumber)
	{
		// check if save file exists
		boolean loadCorrect = LoadSaveManager.read(slotNumber);

		// file exists
		if (loadCorrect)
		{
			// import values from the game save
			loadedGame = true;
			cards = LoadSaveManager.getCards();
			skipNextTurn = LoadSaveManager.getLicoriceStatus();
			playerPositions = LoadSaveManager.getPlayerPositions();
			PieceSelectionScreen.pieceSelection = LoadSaveManager.getPlayerTokens()[0];

			// disable save button after loading a game as there is nothing to save
			userSaved = true;
			
			return true;
		}
		//file does not exist
		return false;
	}

	/**
	 * Returns integer array indicating the index of each player's game piece.
	 * Used for saving/loading
	 * 
	 * @return integer array with index of each player's game piece, attributed to a specific texture in setTextureString()
	 */
	public int[] getPlayerTokens()
	{
		return playerTokens;
	}

	/**
	 * Determines names of a sprite's texture, used for printing in console
	 * 
	 * @param index - player index
	 * @return - string of game piece texture at a player's index. Displays the full file, including .png
	 */
	public String getTextureString(int index)
	{
		return playerTextures[index].toString();
	}
	
	public boolean getPausedState()
	{
		return pausedState;
	}

	public void setPausedState(boolean pausedState)
	{
		this.pausedState = pausedState;
	}

	@Override
	public void resize(int width, int height)
	{
		camera.setToOrtho(false, 1280, 960);
		camera.update();
	}

	@Override
	public void show()
	{
		gameSpaces = new TmxMapLoader().load("board-finish-multicolor.tmx");
		renderer = new OrthogonalTiledMapRenderer(gameSpaces, RENDER_SCALE);
	}

	@Override
	public void dispose()
	{
		drawCard.dispose();
		yourCard.dispose();
		pausedLogo.dispose();
		exitTexture.dispose();
		saveTexture.dispose();
		savedTexture.dispose();
		playTexture.dispose();
		pauseTexture.dispose();
		peppermintTexture.dispose();
		peanutTexture.dispose();
		gumdropTexture.dispose();
		lollipopTexture.dispose();
		rainbowShortcutTexture.dispose();
		gumdropShortcutTexture.dispose();
	}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}
}

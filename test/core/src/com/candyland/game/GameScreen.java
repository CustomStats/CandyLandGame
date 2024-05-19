/**
 * GameScreen.java
 * This class represents a visual screen displaying the main game board.
 * Displayed after choosing a game piece or loading an existing save.
 */

package com.candyland.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
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

public class GameScreen implements Screen
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

	private static CandyLandMain game;
	private TiledMap gameSpaces;
	private OrthographicCamera camera = new OrthographicCamera();
	private OrthogonalTiledMapRenderer renderer;

	// variables used to store game board information
	private static Sprite[] playerSprites = new Sprite[CandyLandGame.GAME_PIECES];
	private static Sprite[] playerSpritesDisplayed = new Sprite[CandyLandGame.GAME_PIECES];
	private static Texture[] playerTextures = new Texture[CandyLandGame.GAME_PIECES];
	public static CandyLandSpaces[] gamePositions = new CandyLandSpaces[137];
	public static boolean pausedState = false;
	public static int[] playerTokens = new int[CandyLandGame.GAME_PIECES];
	public static float positions[][] = new float[CandyLandGame.GAME_PIECES][2];
	private static boolean userSaved = false;
	
	// final variables that do not change
	public final static float RENDER_SCALE = 1/1.2f;
	private final static int SQUARE_SIZE = 32;
	public final static int MAX_POSITIONS = GameScreen.gamePositions.length - 1;

	public GameScreen(final CandyLandMain game)
	{
		GameScreen.game = game;

		// create instance of CandyLandGame.java
		new CandyLandGame(GameScreen.game);

		if (CandyLandGame.includesScreenLogic)
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
			
			if (!CandyLandGame.loadedGame)
			{
				// game not loaded, place sprites at the default position
				setDefaultPiecePositions();
			}
			else
			{
				// game loaded, place sprites at their saved position
				for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
				{
					setPiecePosition();
					CandyLandGame.incrementIndex();
				}
				// disable save button after loading a game as there is nothing to save
				userSaved = true;
			}
		}
		else
		{
			createBoardSpaces();
			playerTokens[0] = PieceSelectionScreen.pieceSelection;
		}
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

				if (drawButton.contains(touchX, touchY) && CandyLandGame.validPress && !pausedState)
				{
					// user clicked draw card
					// user has not saved and this is a valid press
					userSaved = false;
					CandyLandGame.validMousePress = true;
					return true;
				}
				if (exitButton.contains(touchX, touchY) && pausedState)
				{
					// user clicked exit button on pause screen
					// return to main menu
					pausedState = false;
					dispose();
					CandyLandGame.resetValues();
					game.setScreen(new MainMenuScreen(game));
					return true;
				}
				if (saveButton.contains(touchX, touchY) && pausedState && !userSaved)
				{
					// user saved the game on pause screen
					CandyLandGame.LoadSaveManager.write(1, getPlayerTokens(), CandyLandGame.playerPositions, CandyLandGame.cards, CandyLandGame.skipNextTurn);
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
		for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
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
		for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
		{
			Sprite playerPiece = getPlayerSprite(i);
			int currentPosition = CandyLandGame.getCurrentPositionIndex(i);

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

		// Accept right arrow key input method from CandyLandGame
		CandyLandGame.handleInput();

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
			if (!userSaved && CandyLandGame.validPress && CandyLandGame.activeGame)
			{
				// player has not saved this round
				game.batch.draw(saveTexture, saveButton.x, saveButton.y);
			}
			else if (CandyLandGame.validPress)
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

			if (CandyLandGame.firstPress)
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
			for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
			{
				// display each player's game piece
				Sprite playerSprite = playerSprites[i];
				playerSprite.draw(game.batch);

				if (!CandyLandGame.firstPress)
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

				if (CandyLandGame.drawnCards[i][0] != null)
				{
					// display drawn card if it has been drawn this round
					String[] drawnCard = CandyLandGame.drawnCards[i][0].split(" ");
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

				if (CandyLandGame.drawnCards[i][1] != null)
				{
					// display second card if it has been drawn this round
					// only applicable on Extreme difficulty
					String[] drawnSecondCard = CandyLandGame.drawnCards[i][1].split(" ");
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
				else if (CandyLandGame.shortcutTaken[i])
				{
					// display shortcut taken through visual text below card drawn
					float shortcutTextX = i == 0 ? 50 : i * 146;
					float shortcutTextY = 775;
					String shortcutDestination = CandyLandGame.getCurrentColor(CandyLandGame.playerPositions[i]);
					determineCardColor(i);
					shortcutDestination = "-> " + shortcutDestination;
					game.font.draw(game.batch, shortcutDestination, shortcutTextX, shortcutTextY);
				}
				else if (CandyLandGame.skipCurrentTurn[i] || CandyLandGame.skipNextTurn[i])
				{
					// display licorice text if player's turn will be skipped next round
					// needs current or next boolean to correctly display when intended
					float licoriceTextX = i == 0 ? 50 : i * 145;
					float licoriceTextY = 775;
					game.font.setColor(Color.BLACK);
					game.font.draw(game.batch, "X Licorice X", licoriceTextX, licoriceTextY);
				}
			}
			if (CandyLandGame.firstPress)
			{
				// display instructions in bottom left corner
				// when card hasn't been drawn
				game.font.setColor(Color.MAROON);
				GlyphLayout instructionsText = new GlyphLayout(game.font, "Press Right Arrow Key or Click the Draw Button to Begin!");
				game.font.draw(game.batch, instructionsText, 10, 85);
			}
			else if (CandyLandGame.validPress)
			{
				// indicate it is the user's turn
				game.font.setColor(Color.PINK);
				GlyphLayout yourTurnText = new GlyphLayout(game.font, "It is now your turn!");
				game.font.draw(game.batch, yourTurnText, 195, 750);
			}
			
			// show licorice spaces at the specified game board positions
			// these numbers line up with CandyLandGame.handleLicorice()
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
		String color = CandyLandGame.getCurrentColor(CandyLandGame.playerPositions[index]);
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
	public static void movePosition()
	{
		// obtain current player's position and player piece
		int playerType = CandyLandGame.getCurrentPlayerType();
		int currentPosition = CandyLandGame.getCurrentPositionIndex(playerType);

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
		for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
		{
			tempPosition = CandyLandGame.getCurrentPositionIndex(i);

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
			for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
			{
				tempPosition = CandyLandGame.getCurrentPositionIndex(i);
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
	
	public static void setPiecePosition()
	{
		// obtain current player's position and player piece
		int playerType = CandyLandGame.getCurrentPlayerType();
		Sprite playerPiece = getPlayerSprite(playerType);
		int currentPosition = CandyLandGame.getCurrentPositionIndex(playerType);

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
		for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
		{
			tempPosition = CandyLandGame.getCurrentPositionIndex(i);

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
			for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
			{
				tempPosition = CandyLandGame.getCurrentPositionIndex(i);
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

		for (int i = 0; i < CandyLandGame.GAME_PIECES; i++)
		{
			// adjust game piece to:
			// correct position if their turn was skipped
			// account for new overlap
			// when game is loaded from save state
			if (CandyLandGame.skipCurrentTurn[i] || numberOfPieces > 1 || CandyLandGame.loadedGame)
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
				}, CandyLandGame.loadedGame ? 0.0f : 0.5f);
			}
		}

		if (playerType == CandyLandGame.GAME_PIECES - 1 && CandyLandGame.activeGame && !CandyLandGame.skipNextTurn[0])
		{
			// indicate it is the user's turn, which occurs at .6 seconds
			// where this is after final computer has their animation finished.
			Timer.schedule(new Timer.Task()
			{
				@Override
				public void run()
				{
					userSaved = false;
					CandyLandGame.validPress = true;
					if (CandyLandMain.DEBUG)
					{
						System.out.println("|---------------------------------|\n");
						System.out.println("It is your turn! Press RIGHT ARROW KEY to draw a card!\n");
					}
				}
			}, CandyLandGame.loadedGame ? 0.0f : 0.6f);
		}

		/**
		 * Game piece locations animated through updating their position until
		 * reaching target destination
		 * (Requirement 4.2.0)
		 */
		if (!CandyLandGame.skipCurrentTurn[playerType] && !CandyLandGame.loadedGame)
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
			CandyLandGame.skipCurrentTurn[playerType] = false;
		}
	}

	/**
	 * Player has won the game, switch them to winner screen.
	 * 
	 * @param p - index of player that won the game, used to indicate
	 * which winner screen is displayed
	 */
	public static void switchToWinnerScreen(int p)
	{
		CandyLandGame.resetValues();
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
	private static Sprite getPlayerSprite(int index)
	{
		return playerSprites[index];
	}

	/**
	 * Create game piece sprites at the specified index.
	 * Called for each player index.
	 * 
	 * @param index - player index
	 */
	private static void createSprites(int index)
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
	public static String setTextureString(int index)
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
	 * Returns integer array indicating the index of each player's game piece.
	 * Used for saving/loading
	 * 
	 * @return integer array with index of each player's game piece, attributed to a specific texture in setTextureString()
	 */
	public static int[] getPlayerTokens()
	{
		return playerTokens;
	}

	/**
	 * Determines names of a sprite's texture, used for printing in console
	 * 
	 * @param index - player index
	 * @return - string of game piece texture at a player's index. Displays the full file, including .png
	 */
	public static String getTextureString(int index)
	{
		return playerTextures[index].toString();
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

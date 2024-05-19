/**
 * MainMenuScreen.java
 * This class represents a visual landing screen displaying the main menu.
 * Displayed upon launching the game.
 * (Requirement 2.0.0)
 */
package com.candyland.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuScreen extends ScreenAdapter
{
	// buttons and images
	private Texture mainMenuBackgroundTexture;
	private Texture titleCard;
	private Texture newGameTexture;
	private Texture exitTexture;
	private Texture settingsTexture;
	private Texture loadTexture;
	private Rectangle newGameButton;
	private Rectangle exitButton;
	private Rectangle settingsButton;
	private Rectangle loadButton;

	private FitViewport viewport;
	private OrthographicCamera camera;
	private Stage stage;

	private final CandyLandMain game;

	// global music
	public static Music mainMenuMusic;

	public MainMenuScreen(final CandyLandMain game)
	{
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 960);
		this.viewport = new FitViewport(0, 0, camera);

		// load saved music volume, otherwise use 0.1 as default
		Preferences preferences = Gdx.app.getPreferences("candyland-prefs");
		float savedVolume = preferences.getFloat("volume", 0.1f);

		if (mainMenuMusic == null)
		{
			/**
	 		* Play background music from the specified music file and set volume based on saved preference for volume.
			* (Requirement 3.2.0)
	 		*/
			mainMenuMusic = Gdx.audio.newMusic(Gdx.files.internal("candy-club-174360.mp3"));
			mainMenuMusic.setLooping(true);
			mainMenuMusic.setVolume(savedVolume);
			mainMenuMusic.play();
		}
		else if (!mainMenuMusic.isPlaying())
		{
			// play music if paused
			// occurs when allow music during gameplay is set to Disabled
			mainMenuMusic.play();
		}

		stage = new Stage();
		this.viewport = new FitViewport(1280, 960, camera);
		stage.setViewport(viewport);

		handleInputs();
		createButtons();
	}

	/**
	 * Handle user's ability to interact with the MainMenuScreen
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

				if (newGameButton.contains(touchX, touchY))
				{
					// user selected new game button
					dispose();
					game.setScreen(new PieceSelectionScreen(game));
					return true;
				}
				if (exitButton.contains(touchX, touchY))
				{
					// user selected exit
					dispose();
					Gdx.app.exit();
					System.exit(-1);
					return true;
				}
				if (settingsButton.contains(touchX, touchY))
				{
					// user selected settings
					dispose();
					game.setScreen(new SettingsScreen(game));
					return true;
				}
				if (loadButton.contains(touchX, touchY))
				{
					GameScreen gameScreen = new GameScreen(game);
					// user selected continue game
					if (gameScreen.load(1))
					{
						// valid save loaded, switch display to game screen
						dispose();
						gameScreen.initializeGame();
						game.setScreen(gameScreen);
						gameScreen.loadedGame = false;
					}
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Create all MainMenuScreen images and their respective rectangles indiciating
	 * their physical interactable location if applicable
	 */
	private void createButtons()
	{
		mainMenuBackgroundTexture = new Texture(Gdx.files.internal("5939.jpg"));
		newGameTexture = new Texture(Gdx.files.internal("newGame.png"));
		exitTexture = new Texture(Gdx.files.internal("textExitButton.png"));
		titleCard = new Texture(Gdx.files.internal("candyland_title.png"));
		settingsTexture = new Texture(Gdx.files.internal("SettingsButton.png"));
		loadTexture = new Texture(Gdx.files.internal("continue.png"));

		exitButton = new Rectangle();
		exitButton.x = (Gdx.graphics.getWidth() - exitTexture.getWidth()) / 2;
		exitButton.y = 200;
		exitButton.width = exitTexture.getWidth();
		exitButton.height = exitTexture.getHeight() - 20;

		newGameButton = new Rectangle();
		newGameButton.x = ((Gdx.graphics.getWidth() - newGameTexture.getWidth()) / 2);
		newGameButton.y = (Gdx.graphics.getHeight()) / 1.6f;
		newGameButton.width = newGameTexture.getWidth();
		newGameButton.height = newGameTexture.getHeight();

		loadButton = new Rectangle();
		loadButton.x = ((Gdx.graphics.getWidth() - loadTexture.getWidth()) / 2);
		loadButton.y = 515;
		loadButton.width = loadTexture.getWidth();
		loadButton.height = loadTexture.getHeight() - 20;

		settingsButton = new Rectangle();
		settingsButton.x = ((Gdx.graphics.getWidth() - settingsTexture.getWidth()) / 2);
		settingsButton.y = 415;
		settingsButton.width = settingsTexture.getWidth();
		settingsButton.height = settingsTexture.getHeight();
	}

	/**
	 * Render each frame
	 */
	@Override
	public void render(float delta)
	{
		ScreenUtils.clear(255, 255, 255, 255);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
		game.batch.draw(mainMenuBackgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		game.batch.draw(titleCard, ((Gdx.graphics.getWidth() - titleCard.getWidth()) / 2), ((Gdx.graphics.getHeight() - titleCard.getHeight())));
		game.batch.draw(newGameTexture, newGameButton.x, newGameButton.y);
		game.batch.draw(loadTexture, loadButton.x, loadButton.y);
		game.batch.draw(settingsTexture, settingsButton.x, settingsButton.y);
		game.batch.draw(exitTexture, exitButton.x, exitButton.y);
		game.batch.end();
	}

	@Override
	public void resize(int width, int height)
	{
		camera.setToOrtho(false, width, height);
		viewport.setWorldSize(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose()
	{
		stage.dispose();
		mainMenuBackgroundTexture.dispose();
		titleCard.dispose();
		newGameTexture.dispose();
		exitTexture.dispose();
		settingsTexture.dispose();
		loadTexture.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void show() {}
}

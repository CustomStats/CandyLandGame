/**
 * PieceSelectionScreen.java
 * This class represents a visual screen displaying the game pieces a player can choose from.
 * Displayed after pressing New Game on the main menu.
 * (Requirement 4.3.0)
 */
package com.candyland.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;


public class PieceSelectionScreen implements Screen
{
	// buttons and images
	private Texture backgroundTexture;
	private Texture titleCard;
	private Texture playTexture;
	private Texture backTexture;
	private Texture pieceOneTexture;
	private Texture pieceTwoTexture;
	private Texture pieceThreeTexture;
	private Texture pieceFourTexture;
	private Rectangle playButton;
	private Rectangle backButton;

	// visual rectangle around selected game piece
	private Rectangle[] pieceRectangles = new Rectangle[CandyLandGame.GAME_PIECES];
	private ShapeRenderer shapeRenderer = new ShapeRenderer();

	private FitViewport viewport;
	private OrthographicCamera camera;
	private Stage stage;

	private CandyLandMain game;

	// game piece chosen
	public static int pieceSelection = 0;

	public PieceSelectionScreen(final CandyLandMain game)
	{
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 960);
		this.viewport = new FitViewport(0, 0, camera);
		stage = new Stage();
		this.viewport = new FitViewport(1280, 960, camera);
		stage.setViewport(viewport);

		createButtons();
		handleInputs();
	}

	/**
	 * Handle user's ability to interact with the PieceSelectionScreen
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

				if (playButton.contains(touchX, touchY))
				{
					// user selected play, start a new game on the board
					dispose();
					game.setScreen(new GameScreen(game));
					return true;
				}
				if (backButton.contains(touchX, touchY))
				{
					// user selected back, return to main menu
					dispose();
					game.setScreen(new MainMenuScreen(game));
					return true;
				}

				for (int i = 0; i < pieceRectangles.length; i++)
				{
					if (pieceRectangles[i].contains(touchX, touchY))
					{
						// set index of player's game piece choice
						// based on the game piece selected
						pieceSelection = i;
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Create all PieceSelectionScreen images and their respective rectangles indiciating
	 * their physical interactable location if applicable
	 */
	private void createButtons()
	{
		backgroundTexture = new Texture(Gdx.files.internal("5939.jpg"));
		playTexture = new Texture(Gdx.files.internal("testPlayButtonOne.png"));
		backTexture = new Texture(Gdx.files.internal("backButton.png"));
		titleCard = new Texture(Gdx.files.internal("candyland_title.png"));

		// scale game pieces to be much larger than their normal size
		scaleGamePieces();

		backButton = new Rectangle();
		backButton.x = (Gdx.graphics.getWidth() - backTexture.getWidth()) / 2;
		backButton.y = 200;
		backButton.width = backTexture.getWidth();
		backButton.height = backTexture.getHeight() - 20;

		playButton = new Rectangle();
		playButton.x = ((Gdx.graphics.getWidth() - playTexture.getWidth()) / 2);
		playButton.y = (Gdx.graphics.getHeight()) / 1.6f;
		playButton.width = playTexture.getWidth();
		playButton.height = playTexture.getHeight();

		float totalWidth = pieceOneTexture.getWidth() + pieceTwoTexture.getWidth() + pieceThreeTexture.getWidth() + pieceFourTexture.getWidth();
		float spacing = (Gdx.graphics.getWidth() - totalWidth) / 5;

		float x = spacing;
		for (int i = 0; i < pieceRectangles.length; i++)
		{
			// create rectangle for each player piece, according to the spacing between them
			pieceRectangles[i] = new Rectangle();
			pieceRectangles[i].x = x;
			pieceRectangles[i].y = 442;
			pieceRectangles[i].width = pieceOneTexture.getWidth();
			pieceRectangles[i].height = pieceOneTexture.getHeight();
			x += pieceRectangles[i].width + spacing;
		}
	}

	/**
	 * Method that calls to scale all player pieces
	 */
	private void scaleGamePieces()
	{
		int SCALE = 75;
		pieceOneTexture = createScaledTexture("cookiePiece.png", SCALE);
		pieceTwoTexture = createScaledTexture("sucker.png", SCALE);
		pieceThreeTexture = createScaledTexture("candyCane.png", SCALE);
		pieceFourTexture = createScaledTexture("pinkCandy.png", SCALE);
	}

	/**
	 * Scale the input texture based on the scale size
	 * 
	 * @param texture - string of the texture to be scaled
	 * @param scale - size to scale texture to
	 * @return texture that has been scaled
	 */
	private Texture createScaledTexture(String texture, int scale)
	{
		// create normal texture as pixmap
		// create new pixmap with desired scale
		// drawPixmap to scale and stretch normal texture
		// to fit the desired rectangle
		Pixmap pixmapDefault = new Pixmap(Gdx.files.internal(texture));
		Pixmap pixmapScaled = new Pixmap(scale, scale, pixmapDefault.getFormat());
		pixmapScaled.drawPixmap(pixmapDefault, 0, 0, pixmapDefault.getWidth(), pixmapDefault.getHeight(), 0, 0, pixmapScaled.getWidth(), pixmapScaled.getHeight());

		// create new texture based off of the pixMap that has been scaled
		Texture newTexture = new Texture(pixmapScaled);
		pixmapDefault.dispose();
		pixmapScaled.dispose();

		// return scaled texture
		return newTexture;
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
		game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		game.batch.draw(titleCard, ((Gdx.graphics.getWidth() - titleCard.getWidth()) / 2), ((Gdx.graphics.getHeight() - titleCard.getHeight())));
		game.batch.draw(playTexture, playButton.x, playButton.y);
		game.batch.draw(pieceOneTexture, pieceRectangles[0].x, pieceRectangles[0].y);
		game.batch.draw(pieceTwoTexture, pieceRectangles[1].x, pieceRectangles[1].y);
		game.batch.draw(pieceThreeTexture, pieceRectangles[2].x, pieceRectangles[2].y);
		game.batch.draw(pieceFourTexture, pieceRectangles[3].x, pieceRectangles[3].y);
		game.batch.draw(backTexture, backButton.x, backButton.y);

		// selected text over player's current piece choice
		game.font.setColor(Color.GREEN);
		GlyphLayout selectedText = new GlyphLayout(game.font, "SELECTED");
		game.font.draw(game.batch, selectedText, pieceRectangles[pieceSelection].x, pieceRectangles[pieceSelection].y + pieceRectangles[pieceSelection].height + 20);
		game.batch.end();

		// initialization of green shape
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);

		switch (pieceSelection)
		{
			// green rectangle around the player's current piece choice
			case 0:
				shapeRenderer.rect(pieceRectangles[0].x, pieceRectangles[0].y, pieceRectangles[0].width, pieceRectangles[0].height);
				break;
			case 1:
				shapeRenderer.rect(pieceRectangles[1].x, pieceRectangles[1].y, pieceRectangles[1].width, pieceRectangles[1].height);
				break;
			case 2:
				shapeRenderer.rect(pieceRectangles[2].x, pieceRectangles[2].y, pieceRectangles[2].width, pieceRectangles[2].height);
				break;
			case 3:
				shapeRenderer.rect(pieceRectangles[3].x, pieceRectangles[3].y, pieceRectangles[3].width, pieceRectangles[3].height);
				break;
			default:
				break;
		}

		shapeRenderer.end();

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
		{
			// return user to main menu if they press escape
			// while on this screen
			dispose();
			game.setScreen(new MainMenuScreen(game));
		}
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
		titleCard.dispose();
		playTexture.dispose();
		backTexture.dispose();
		pieceOneTexture.dispose();
		pieceTwoTexture.dispose();
		pieceThreeTexture.dispose();
		pieceFourTexture.dispose();
		shapeRenderer.dispose();
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide(){}

	@Override
	public void show() {}
}

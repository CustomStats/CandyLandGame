/**
 * WinnerScreen.java
 * This class represents a visual screen displaying the winner of the game.
 * Displayed once GameScreen has a winner.
 * (Requirement 2.2.0)
 */
package com.candyland.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class WinnerScreen implements Screen
{
	// buttons and images
	private Texture winnerTexture;
	private Texture gameOverBackgroundTexture;

	private OrthographicCamera camera;
	private CandyLandMain game;
	private Stage stage;
	private FitViewport viewport;

	// index of winner
	private int winner = -1;

	public WinnerScreen(final CandyLandMain game, int y)
	{
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 960);
		this.viewport = new FitViewport(0, 0, camera);
		winner = y;
		stage = new Stage();
		this.viewport = new FitViewport(1280, 960, camera);
		stage.setViewport(viewport);

		handleInputs();
		createButtons();
	}

	/**
	 * Handle user's ability to interact with the WinnerScreen
	 */
	private void handleInputs()
	{
		Gdx.input.setInputProcessor(new InputAdapter()
		{
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button)
			{
				if (Gdx.input.isTouched())
				{
					/**
	 				* Send player to main menu with any mouse click down interaction
	 				* (Requirement 1.3.1)
	 				*/
					dispose();
					game.setScreen(new MainMenuScreen(game));
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Create dynamic background texture based on user winning or losing
	 * (Requirement 2.2.1)
	 */
	private void createButtons()
	{
		gameOverBackgroundTexture = new Texture(Gdx.files.internal("5939.jpg"));
		if (winner <= 0)
		{
			// user won the game
			winnerTexture = new Texture(Gdx.files.internal("playerWinner.png"));
		}
		else
		{
			// computer won the game
			winnerTexture = new Texture(Gdx.files.internal("GameOver.png"));
		}
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
		game.batch.draw(gameOverBackgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		game.batch.draw(winnerTexture, ((Gdx.graphics.getWidth() - winnerTexture.getWidth()) / 2), (531 - winnerTexture.getHeight()));
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
		winnerTexture.dispose();
		gameOverBackgroundTexture.dispose();
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void show() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}
}

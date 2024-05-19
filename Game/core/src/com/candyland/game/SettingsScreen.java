/**
 * SettingsScreen.java
 * This class represents a visual screen displaying the game settings.
 * Displayed after clicking Settings on the main menu.
 * (Requirement 2.3.0)
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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class SettingsScreen implements Screen
{
	// buttons and images
	private Texture backgroundTexture;
	private Texture titleCard;
	private Texture backTexture;
	private Rectangle backButton;

	private OrthographicCamera camera;
	private CandyLandMain game;
	private Stage stage;
	private FitViewport viewport;

	// sliders
	private Slider volumeSlider;
	private Slider musicSlider;
	private Slider difficultySlider;

	public SettingsScreen(final CandyLandMain game)
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
	 * Handle user's ability to interact with the SettingsScreen
	 */
	private void handleInputs()
	{
		Gdx.input.setInputProcessor(new InputAdapter()
		{
			// boolean values to track which slider user is using
			// otherwise moves all sliders when interacting with a single slider
			private boolean isDraggingVolumeSlider = false;
			private boolean isDraggingMusicSlider = false;
			private boolean isDraggingDifficultySlider = false;

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button)
			{
				float touchX = screenX * camera.viewportWidth / Gdx.graphics.getWidth();
				float touchY = (Gdx.graphics.getHeight() - screenY) * camera.viewportHeight / Gdx.graphics.getHeight();

				if (backButton.contains(touchX, touchY))
				{
					// user pressed back, return to main menu
					dispose();
					game.setScreen(new MainMenuScreen(game));
					return true;
				}
				if (touchX >= volumeSlider.getX() && touchX <= volumeSlider.getX() + volumeSlider.getWidth() && touchY >= volumeSlider.getY() && touchY <= volumeSlider.getY() + volumeSlider.getHeight())
				{
					// user clicked on the volume slider
					// allow this to set value, not just by dragging
					isDraggingVolumeSlider = true;
					float value = calculateSliderValue(volumeSlider, screenX);
					volumeSlider.setValue(value);
					return true;
				}
				if (touchX >= musicSlider.getX() && touchX <= musicSlider.getX() + musicSlider.getWidth() && touchY >= musicSlider.getY() && touchY <= musicSlider.getY() + musicSlider.getHeight())
				{
					// user clicked on the music slider
					// allow this to set value, not just by dragging
					isDraggingMusicSlider = true;
					float value = calculateSliderValue(musicSlider, screenX);
					musicSlider.setValue(value);
					return true;
				}
				if (touchX >= difficultySlider.getX() && touchX <= difficultySlider.getX() + difficultySlider.getWidth() && touchY >= difficultySlider.getY() && touchY <= difficultySlider.getY() + difficultySlider.getHeight())
				{
					// user clicked on the difficulty slider
					// allow this to set value, not just by dragging
					isDraggingDifficultySlider = true;
					float value = calculateSliderValue(difficultySlider, screenX);
					difficultySlider.setValue(value);
					return true;
				}
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer)
			{
				if (isDraggingVolumeSlider)
				{
					// user dragging volume slider
					// set slider location to where player is dragging
					float value = calculateSliderValue(volumeSlider, screenX);
					volumeSlider.setValue(value);
					return true;
				}
				if (isDraggingMusicSlider)
				{
					// user dragging music slider
					// set slider location to where player is dragging
					float value = calculateSliderValue(musicSlider, screenX);
					musicSlider.setValue(value);
					return true;
				}
				if (isDraggingDifficultySlider)
				{
					// user dragging difficulty slider
					// set slider location to where player is dragging
					float value = calculateSliderValue(difficultySlider, screenX);
					difficultySlider.setValue(value);
					return true;
				}
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button)
			{
				// user released mouse button, set all dragging to false
				if (isDraggingVolumeSlider)
				{
					isDraggingVolumeSlider = false;
					return true;
				}
				if (isDraggingMusicSlider)
				{
					isDraggingMusicSlider = false;
					return true;
				}
				if (isDraggingDifficultySlider)
				{
					isDraggingDifficultySlider = false;
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Determine value of where the slider should be position
	 * based on where the user moved the slider to
	 * @param slider - corresponding slider
	 * @param screenX - X coordinate where user interacted
	 * @return value of slider position
	 */
	private float calculateSliderValue(Slider slider, int screenX)
	{
		// Based off ProgressBar logic from libgdx documentation.
		// Obtains where the knob is positioned, in order to determine the value
		// that should correspond.
		float sliderWidth = slider.getWidth();
		float knobWidth = slider.getStyle().knob.getMinWidth();
		float knobPosition = screenX - slider.getX() - knobWidth / 2;
		float value = knobPosition / (sliderWidth - knobWidth) * (slider.getMaxValue() - slider.getMinValue()) + slider.getMinValue();
		return MathUtils.clamp(value, slider.getMinValue(), slider.getMaxValue());
	}

	/**
	 * Create all SettingsScreen images and their respective rectangles indiciating
	 * their physical interactable location if applicable
	 * Additionally creates three interactable sliders that change a libgdx Preference.
	 */
	private void createButtons()
	{
		backgroundTexture = new Texture(Gdx.files.internal("5939.jpg"));
		titleCard = new Texture(Gdx.files.internal("candyland_title.png"));
		backTexture = new Texture(Gdx.files.internal("backButton.png"));

		backButton = new Rectangle();
		backButton.x = (Gdx.graphics.getWidth() - backTexture.getWidth()) / 2;
		backButton.y = 200;
		backButton.width = backTexture.getWidth();
		backButton.height = backTexture.getHeight() - 20;

		// load preferences
		Preferences preferences = Gdx.app.getPreferences("candyland-prefs");

		// create slider's skin
		Skin skin = new Skin();
		Texture sliderBackground = new Texture(Gdx.files.internal("PT_WHITE.png"));
		Texture sliderKnob = new Texture(Gdx.files.internal("PT_BLUE.png"));
		skin.add("slider-background", sliderBackground);
		skin.add("slider-knob", sliderKnob);

		// set skin filter to be high quality for the bar and knob
		// this can result in a performance hit
		sliderBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		sliderBackground.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		sliderBackground.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		sliderKnob.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		sliderKnob.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		sliderKnob.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		// visual display for slider
		TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(sliderBackground, 400, 30));
		TextureRegionDrawable knobDrawable = new TextureRegionDrawable(new TextureRegion(sliderKnob, 30, 30));

		// create slider
		Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
		sliderStyle.background = backgroundDrawable;
		sliderStyle.knob = knobDrawable;
		skin.add("default-horizontal", sliderStyle);

		/**
	 	* Music volume slider which allows users to determine the volume of the music playing in the background
		* (Requirement 3.1.0)
	 	*/
		volumeSlider = new Slider(0.0f, 0.5f, 0.005f, false, skin);
		volumeSlider.setStyle(sliderStyle);
		volumeSlider.setSize(325, 50);
		volumeSlider.setPosition(Gdx.graphics.getWidth() / 2 - volumeSlider.getWidth() / 2, 565);
		float savedVolume = preferences.getFloat("volume", 0.1f);
		volumeSlider.setValue(savedVolume);

		/**
	 	* Music slider which allows users to determine whether the music should be played during gameplay
	 	*/
		musicSlider = new Slider(0.0f, 1.0f, 1.0f, false, skin);
		musicSlider.setStyle(sliderStyle);
		musicSlider.setSize(325, 50);
		musicSlider.setPosition(Gdx.graphics.getWidth() / 2 - musicSlider.getWidth() / 2, 665);
		boolean savedMusicState = preferences.getBoolean("music", true);

		// set knob position depending on saved state
		// default is true, which is enabled
		if (savedMusicState)
		{
			// true, enable music during gameplay
			musicSlider.setValue(1.0f);
		}
		else
		{
			// false, disable music during gameplay
			musicSlider.setValue(0.0f);
		}

		/**
	 	* Difficulty slider which allows users to choose between normal mode and an "adult version" of the game
		* (Requirement 3.1.0)
	 	*/
		difficultySlider = new Slider(0.0f, 1.0f, 1.0f, false, skin);
		difficultySlider.setStyle(sliderStyle);
		difficultySlider.setSize(325, 50);
		difficultySlider.setPosition(Gdx.graphics.getWidth() / 2 - difficultySlider.getWidth() / 2, 465);
		boolean savedDifficulty = preferences.getBoolean("difficulty", false);

		// set knob position depending on saved state
		// default is false, which is normal difficulty
		if (savedDifficulty)
		{
			// true, extreme difficulty
			difficultySlider.setValue(1.0f);
		}
		else
		{
			// false, normal difficulty
			difficultySlider.setValue(0.0f);
		}

		// listen for change to volume slider
		volumeSlider.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				// set music volume and save the preference
				float value = volumeSlider.getValue();
				MainMenuScreen.mainMenuMusic.setVolume(value);
				preferences.putFloat("volume", value);
				preferences.flush();
			}
		});

		// listen for change to music slider
		musicSlider.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				// set music enabled or disabled and save the preference
				float value = musicSlider.getValue();
				boolean musicState = false;
				if (value == 1.0)
				{
					musicState = true;
				}
				else
				{
					musicState = false;
				}
				preferences.putBoolean("music", musicState);
				preferences.flush();
			}
		});

		// listen for change to difficulty slider
		difficultySlider.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				// set difficulty normal or extreme and save the preference
				float value = difficultySlider.getValue();
				boolean difficultyState = false;
				if (value == 1.0)
				{
					difficultyState = true;
				}
				else
				{
					difficultyState = false;
				}
				preferences.putBoolean("difficulty", difficultyState);
				preferences.flush();
			}
		});

		// display the sliders on screen
		stage.addActor(volumeSlider);
		stage.addActor(musicSlider);
		stage.addActor(difficultySlider);
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
		game.batch.draw(titleCard, ((Gdx.graphics.getWidth() - titleCard.getWidth()) / 2), ((Gdx.graphics.getHeight() - titleCard.getHeight())));
		
		// Volume Slider
		game.font.setColor(Color.RED);
		GlyphLayout volumePercentText = new GlyphLayout(game.font, Integer.toString(Math.round(volumeSlider.getPercent() * 100)));
		float volumePercentTextX = volumeSlider.getX() + volumeSlider.getWidth() / 2 - volumePercentText.width / 2 - 2;
		float volumePercentTextY = volumeSlider.getY() + volumeSlider.getHeight() + volumePercentText.height - 1;
		game.font.draw(game.batch, volumePercentText, volumePercentTextX, volumePercentTextY);
		game.font.setColor(Color.BLACK);
		game.font.draw(game.batch, "Music Volume", musicSlider.getX() - 225, volumePercentTextX - 28);
		game.font.setColor(Color.GREEN);
		game.font.draw(game.batch, "0", volumeSlider.getX() - 15, volumePercentTextY - 28);
		game.font.draw(game.batch, "100", ((Gdx.graphics.getWidth() - volumeSlider.getWidth()) / 1.2f + 12), volumePercentTextY - 28);

		// Music During Gameplay Slider
		game.font.setColor(Color.RED);
		String musicState = musicSlider.getValue() > 0.0 ? "Enabled" : "Disabled";
		GlyphLayout musicStateText = new GlyphLayout(game.font, musicState);
		float musicStateTextX = musicSlider.getX() + musicSlider.getWidth() / 2 - musicStateText.width / 2 - 2;
		float musicStateTextY = musicSlider.getY() + musicSlider.getHeight() + musicStateText.height - 1;
		game.font.draw(game.batch, musicStateText, musicStateTextX, musicStateTextY);
		game.font.setColor(Color.BLACK);
		game.font.draw(game.batch, "Music During Gameplay", musicSlider.getX() - 250, musicStateTextY - 28);
		game.font.setColor(Color.GREEN);
		game.font.draw(game.batch, "Disabled", musicSlider.getX() - 63, musicStateTextY - 28);
		game.font.draw(game.batch, "Enabled", ((Gdx.graphics.getWidth() - musicSlider.getWidth()) / 1.2f + 12), musicStateTextY - 28);

		// Difficulty Slider
		game.font.setColor(Color.RED);
		String difficultyState = difficultySlider.getValue() > 0.0 ? "Computer draws two random cards and chooses the best card" : "Computer draws a random card";
		GlyphLayout currentDifficultyText = new GlyphLayout(game.font, difficultyState);
		float currentDifficultyTextX = difficultySlider.getX() + difficultySlider.getWidth() / 2 - currentDifficultyText.width / 2 - 2;
		float currentDifficultyTextY = difficultySlider.getY() + difficultySlider.getHeight() + currentDifficultyText.height - 1;
		game.font.draw(game.batch, currentDifficultyText, currentDifficultyTextX, currentDifficultyTextY);
		game.font.setColor(Color.BLACK);
		game.font.draw(game.batch, "Difficulty", difficultySlider.getX() - 215, currentDifficultyTextY - 28);
		game.font.setColor(Color.GREEN);
		game.font.draw(game.batch, "Normal", difficultySlider.getX() - 52, currentDifficultyTextY - 28);
		game.font.draw(game.batch, "Extreme", ((Gdx.graphics.getWidth() - difficultySlider.getWidth()) / 1.2f + 12), currentDifficultyTextY - 28);

		game.batch.draw(backTexture, backButton.x, backButton.y);
		game.batch.end();
		stage.draw();

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
		viewport.update(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose()
	{
		stage.dispose();
		backTexture.dispose();
		backgroundTexture.dispose();
		titleCard.dispose();
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

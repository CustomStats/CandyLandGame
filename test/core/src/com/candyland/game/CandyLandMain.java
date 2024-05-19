/**
 * CandyLandMain.java
 * This class represents an Application listener,
 * handling the creation and display of multiple screens
 */

package com.candyland.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CandyLandMain extends Game
{
	// variables used to display text and sprites.
	// creating globally prevents unnecessary use of resources.
	// can run into issues of needing to store information locally,
	// but this is out of the scope for this game.
	public SpriteBatch batch;
	public BitmapFont font;
	
	// show console prints
	public static boolean DEBUG = false;

	@Override
	public void create()
	{
		batch = new SpriteBatch();
		font = new BitmapFont();
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		batch.dispose();
		font.dispose();
	}
}

package com.candyland.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher
{
	public static void main (String[] arg)
	{
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
	    config.setWindowedMode(1280, 960);
	    config.useVsync(true);
	    config.setResizable(false);
		config.setTitle("Candy Land");
		config.setWindowIcon("candyCane.png");
		new Lwjgl3Application(new CandyLandMain(), config);
	}
}

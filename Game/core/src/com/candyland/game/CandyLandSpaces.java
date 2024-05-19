/**
 * CandyLandSpaces.java
 * This class represents the game board spaces (positions) for Candy Land
 */
package com.candyland.game;

public class CandyLandSpaces
{
	public int x;
	public int y;

	CandyLandSpaces(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	int returnX()
	{
		return x;
	}

	int returnY()
	{
		return y;
	}
}

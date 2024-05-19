/**
 * CandyLandGameTest.java
 * This class represents the white box testing for Candy Land components
 */

package com.candyland.game;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class CandyLandGameTest
{
	private CandyLandMain game;

	public CandyLandGameTest()
	{
		CandyLandGame.includesScreenLogic = false;
	}

	/**
	 * TODO: v1.0
	 * --------------------------------------------------------------------
	 * This block of testing handles v1.0 white box testing where applicable
	 * 
	 * These test cases handle the following requirements:
	 * 1.1.0 - Users should be able to draw from the deck of cards.
	 * 1.1.1 - Three computers should automatically draw cards after the user’s turn.
	 * 1.2.0 - The game should determine a player’s position by moving them based on the drawn card.
	 * 1.2.1 - Single color cards should move players to the closest respective color position on the board.
	 * 1.2.2 - Double color cards should move players to the second closest respective color position on the board.
	 * 1.2.3 - Special cards should set a player to a pre-determined position associated with the card.
	 * 1.3.0 - The game should end once a player reaches the end position.
	 *---------------------------------------------------------------------
	 */

	/* 
	 * Testing:
	 * Default player positions at 0
	 * User can draw card and progress player position
	 * Cards automatically drawn for computer players
	 * Player can win the game
	 */
	@Test
	public void drawCardWithWinner()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// default position at 0
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(0));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(1));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(2));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(3));

		// 1.1.0 - Users should be able to draw from the deck of cards.
		// 1.1.1 - Three computers should automatically draw cards after the user’s turn.
		// call handleInput logic as if we pressed the draw card button
		// Requirement (1.1.0 & 1.1.1)
		if (!CandyLandGame.drawCardPressed)
		{
			CandyLandGame.drawCardPressed = true;
			CandyLandGame.validPress = false;
			CandyLandGame.validMousePress = false;
			CandyLandGame.activeGame = true;
			CandyLandGame.firstPress = false;
			CandyLandGame.resetDrawnCards();
			CandyLandGame.resetShortcutTaken();
			CandyLandGame.drawCard();
			CandyLandGame.drawCardPressed = false;
		}

		// ensure player moved from 0
		assertNotEquals(0, CandyLandGame.getCurrentPositionIndex(0));

		// ensure computers moved from 0
		assertNotEquals(0, CandyLandGame.getCurrentPositionIndex(1));
		assertNotEquals(0, CandyLandGame.getCurrentPositionIndex(2));
		assertNotEquals(0, CandyLandGame.getCurrentPositionIndex(3));

		// player type should be back to 0 after round is over
		assertEquals(0, CandyLandGame.getCurrentPlayerType());

		/**
		 * The game should end once a player reaches the end of the game positions array
		 * (Requirement 1.3.0)
		 */

		// there should not be a winner
		assertEquals(-1, CandyLandGame.gameWinner);
 
		// set player to winning position
		CandyLandGame.movePlayer("Red", 9999);

		// ensure position was corrected to max index
		assertEquals(136, CandyLandGame.getCurrentPositionIndex(0));

		CandyLandGame.checkWinner();

		// 1.3.0 there should be a winner
		assertEquals(0, CandyLandGame.gameWinner);
	}
	
	/* 
	 * Testing:
	 * Computer can win the game
	 */
	@Test
	public void computerWinner()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// ensure positions are 0
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(0));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(1));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(2));
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(3));

		// increment index to computer 1
		CandyLandGame.incrementIndex();

		// set computer 1 to winning position
		CandyLandGame.movePlayer("Red", 999);

		CandyLandGame.checkWinner();

		assertEquals(1, CandyLandGame.gameWinner);
	}
	
	/* 
	 * Testing:
	 * Deck generates 64 cards
	 * Empty deck
	 * Automatic deck shuffle
	 * Deck generates 64 cards 
	 */
	@Test
	public void deckAttributes()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// card deck should not be empty
		assertFalse(CandyLandGame.cards.isEmpty());

		// deck should be 64
		assertEquals(64, CandyLandGame.cards.size());

		// color at 0 is red
		assertEquals("Red", CandyLandGame.getCurrentColor(0));

		// draw card
		CandyLandGame.drawCard();

		// deck should be 60 after drawing cards
		assertEquals(60, CandyLandGame.cards.size());

		// set winner to index so it doesn't check for winner while removing cards
		// remove all cards from deck by simulating 15 rounds
		CandyLandGame.gameWinner = 999;
		for (int i = 0; i < 15; i++)
		{
			CandyLandGame.drawCard();
		}

		// deck should be 0
		assertEquals(0, CandyLandGame.cards.size());

		// call checkEmptyDeck() which drawCard calls before drawing a new card
		CandyLandGame.checkEmptyDeck();

		//deck should be re-populated
		assertEquals(64, CandyLandGame.cards.size());
	}
	
	/* 
	 * Testing:
	 * Obtaining current color from a position on the board
	 * Determine number of steps players needs to move to reach drawn card
	 * (Requirement 1.2.0 & 1.2.1 & 1.2.2 & 1.2.3)
	 */
	@Test
	public void stepsFromCardDraw()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// set user to position 56
		CandyLandGame.movePlayer("Red", 56);

		// Position 56 is yellow
		// 56 % 6 = 2
		// 2 = yellow
		// 0 = red
		// {"Purple", "Yellow", "Blue", "Green", "Orange", "Red"};
		assertEquals("Yellow", CandyLandGame.getCurrentColor(56));

		// example card drawn is purple
		String currentCard = "Purple";

		// determine how many steps to move player, and move them
		CandyLandGame.movePlayer(currentCard, CandyLandGame.calculateSteps(currentCard));

		/*
		 * Single color cards should move players to the closest respective color position on the board.
		 * (Requirement 1.2.1)
		 */
		// landing location should be 61.
		// Yellow is index 1, Purple is index 0.
		// Reaching 0 takes 5 steps.
		assertEquals(61, CandyLandGame.getCurrentPositionIndex(0));

		/*
		 * Double color cards should move players to the second closest respective color position on the board.
		 * (Requirement 1.2.2)
		 */
		// user is at 61 which is purple. drawing double red should send them 11 steps forward
		CandyLandGame.cards.set(0, "Double Red");
		CandyLandGame.drawCard();
		assertEquals(72, CandyLandGame.getCurrentPositionIndex(0));

		/*
		 * Special cards should set a player to a pre-determined position associated with the card.
		 * (Requirement 1.2.3)
		 */
		// set next card to special card and draw from deck
		CandyLandGame.cards.set(0, "Peppermint Forest");
		CandyLandGame.drawCard();

		// user should be at peppermint forest pre-defined position
		assertEquals(20, CandyLandGame.getCurrentPositionIndex(0));
	}


	/**
	 * TODO: v2.0
	 * --------------------------------------------------------------------
	 * This block of testing handles v2.0 white box testing where applicable
	 * 
	 * These test cases handle the following requirements:
	 * 4.0.1 - The game should support shortcuts that teleport a player to a new position if they land on a specific location.
	 * 4.0.2 - The game should support licorice spaces that skip a players' turn if they land on a specific location.
	 * 4.1.0 - The game pieces should account for overlap of player positions on the same board location.
	 * --------------------------------------------------------------------
	 */
	
	/**
	 * Handle game piece overlap
	 * (Requirement 4.1.0)
	 */
	@Test
	public void pieceOverlap()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		GameScreen gameScreen = new GameScreen(game);

		int[] tempX = new int[137];
		int[] tempY = new int[137];

		// Ensure gamePositions[] is filling with same information each creation
		//
		// We are working with floats to scale the game positions X and Y values,
		// But since we are not comparing any float values,
		// their floating point values do not have to be considered
		// and we just need to confirm that the X and Y values are constant.
		for (int i = 0; i < GameScreen.gamePositions.length; i++)
		{
			tempX[i] = GameScreen.gamePositions[i].returnX();
			tempY[i] = GameScreen.gamePositions[i].returnY();
		}
		int temp66X = GameScreen.gamePositions[66].returnX();
		int temp66Y = GameScreen.gamePositions[66].returnY();
		gameScreen.createBoardSpaces();
		for (int i = 0; i < GameScreen.gamePositions.length; i++)
		{
			assertEquals(tempX[i], GameScreen.gamePositions[i].returnX());
			assertEquals(tempY[i], GameScreen.gamePositions[i].returnY());
			if (i == 66)
			{
				assertEquals(temp66X, GameScreen.gamePositions[i].returnX());
				assertEquals(temp66Y, GameScreen.gamePositions[i].returnY());
			}
		}

		// game positions at 0
		assertEquals(CandyLandGame.playerPositions[0], CandyLandGame.playerPositions[1]);
		assertEquals(0, CandyLandGame.playerPositions[0]);

		// pieces on the screen at same X and Y locations
		// positions[X][] = player index
		// positions[][X] = X (0) or Y (1) value

		assertEquals(GameScreen.positions[0][0], GameScreen.positions[1][0], 0);
		assertEquals(GameScreen.positions[0][1], GameScreen.positions[1][1], 0);

		/**
		 * Handle case of two pieces overlapping
		 */
		// move user to yellow space
		CandyLandGame.movePlayer("Yellow", CandyLandGame.calculateSteps("Yellow"));

		// check that user was moved to correct location
		// and computer 1 has not moved
		assertEquals(2, CandyLandGame.playerPositions[0]);
		assertEquals(0, CandyLandGame.playerPositions[1]);

		// set position of user game piece to new position
		GameScreen.movePosition();

		// save user information to compare if correctly re-positioned
		float savedUserPositionsX = GameScreen.positions[0][0];
		float centerOfCurrentPosition = GameScreen.positions[0][0];

		// computer 1
		CandyLandGame.incrementIndex();

		// move computer 1 to yellow
		CandyLandGame.movePlayer("Yellow", CandyLandGame.calculateSteps("Yellow"));

		// check that user and computer 1 landed on same position
		assertEquals(CandyLandGame.playerPositions[0], CandyLandGame.playerPositions[1]);

		// set position of computer 1 game piece to new position
		GameScreen.movePosition();

		// if these are not equal, it means positions were automatically adjusted
		// to account for overlap
		assertNotEquals(savedUserPositionsX, GameScreen.positions[0][0], 0);
		assertNotEquals(GameScreen.positions[0][0], GameScreen.positions[1][0], 0);
		assertNotEquals(GameScreen.positions[0][1], GameScreen.positions[1][1], 0);

		/**
		 * Handle case of 3 pieces overlapping
		 */
		// update saved user's X position
		savedUserPositionsX = GameScreen.positions[0][0];

		// computer 2
		CandyLandGame.incrementIndex();

		// move computer 2 to yellow
		CandyLandGame.movePlayer("Yellow", CandyLandGame.calculateSteps("Yellow"));

		// check that user and computer 2 landed on same position
		assertEquals(CandyLandGame.playerPositions[0], CandyLandGame.playerPositions[2]);

		// set position of computer 2 game piece to new position
		GameScreen.movePosition();

		// if these are not equal, it means positions were automatically adjusted
		// to account for overlap
		assertNotEquals(savedUserPositionsX, GameScreen.positions[0][0], 0);

		// X values should be different
		assertNotEquals(GameScreen.positions[1][0], GameScreen.positions[2][0], 0);

		// these are not equal as the Y values should not be lined up,
		// since the piece for computer 2 is position on the right edge
		assertNotEquals(GameScreen.positions[1][1], GameScreen.positions[2][1], 0);

		// We have to compare Y values as now the X values would be the same,
		// as two player pieces would be lined up on the same X but different Y's
		// on the left side of the game board location
		assertNotEquals(GameScreen.positions[0][1], GameScreen.positions[2][1], 0);

		// check that computer 1 doesnt equal computer 2
		assertNotEquals(GameScreen.positions[1][0], GameScreen.positions[2][0], 0);
		assertNotEquals(GameScreen.positions[1][1], GameScreen.positions[2][1], 0);

		/**
		 * Handle case of 4 pieces overlapping
		 */
		// computer 3
		CandyLandGame.incrementIndex();

		// move computer 3 to yellow
		CandyLandGame.movePlayer("Yellow", CandyLandGame.calculateSteps("Yellow"));

		// check that user and computer 3 landed on same position
		assertEquals(CandyLandGame.playerPositions[0], CandyLandGame.playerPositions[3]);

		// set position of computer 3 game piece to new position
		GameScreen.movePosition();

		// these are equal as the Y values should be lined up, since both pieces
		// are positioned side by side next to each other.
		assertEquals(GameScreen.positions[2][1], GameScreen.positions[3][1], 0);
		assertNotEquals(GameScreen.positions[2][0], GameScreen.positions[3][0], 0);

		// check that user doesnt equal computer 2
		// We can both X and Y and these pieces are in opposite corners
		assertNotEquals(GameScreen.positions[0][1], GameScreen.positions[3][1], 0);
		assertNotEquals(GameScreen.positions[0][0], GameScreen.positions[3][0], 0);

		// check that computer 1 doesnt equal computer 2
		// Y values are the same as theyre both lined up on the right
		// side of a game board position
		assertNotEquals(GameScreen.positions[2][0], GameScreen.positions[3][0], 0);
		assertEquals(GameScreen.positions[2][1], GameScreen.positions[3][1], 0);

		/**
		 * Check that pieces get automatically set back to their correct position
		 * within a specific game board location if a game piece
		 * is no longer overlapping
		 * A piece should be set back to the center if there is no longer overlap,
		 * even if the player does not move (licorice)
		 */

		// these should not be equal yet, as the piece has been moved from the center
		assertNotEquals(centerOfCurrentPosition, GameScreen.positions[0][0]);

		// change to index 1, we want to keep user on current position
		CandyLandGame.incrementIndex();
		CandyLandGame.incrementIndex();

		// move all computers to different position
		for (int i = 0; i < 3; i++)
		{
			CandyLandGame.movePlayer("Red", CandyLandGame.calculateSteps("Red"));
			GameScreen.movePosition();
			CandyLandGame.incrementIndex();
		}

		// move to user adjusted position
		// they did not move, but we have accounted for them no longer overlapping
		// so their position has technically changed
		GameScreen.movePosition();

		// position should be back to center of current position
		assertEquals(centerOfCurrentPosition, GameScreen.positions[0][0], 0);
	}

	/*
	 * Handle player landing on a shortcut location
	 * Requirement (4.0.1)
	 */
	@Test
	public void shortcuts()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;
		
		// ensure player is at 0
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(0));

		// move user to shorcut position
		CandyLandGame.movePlayer("Red", 49);

		// user should be at 74 if shortcut was taken
		assertNotEquals(0, CandyLandGame.getCurrentPositionIndex(0));
		assertNotEquals(49, CandyLandGame.getCurrentPositionIndex(0));
		assertEquals(74, CandyLandGame.getCurrentPositionIndex(0));
	}
	
	/*
	 * Handle player landing on a licorice location
	 * Requirement (4.0.2)
	 */
	@Test
	public void licorice()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// computer 1 should be on 0
		assertEquals(0, CandyLandGame.getCurrentPositionIndex(1));

		// set card to land on licorice
		CandyLandGame.cards.set(0, "Double Red");
		CandyLandGame.cards.set(1, "Yellow");
		CandyLandGame.drawCard();

		// computer 1 should now be on 2 which is yellow
		assertEquals(2, CandyLandGame.getCurrentPositionIndex(1));

		// user should be on 12, which is licorice
		assertEquals(12, CandyLandGame.getCurrentPositionIndex(0));

		// set next card in deck to yellow, which is what computer 1 will draw
		CandyLandGame.cards.set(0, "Yellow");

		// enable licorice logic as we don't want it enable for previous test cases
		// messes up amount of cards drawn from deck, so forcing shuffle test will
		// break if there are turns being skipped
		CandyLandGame.enableLicoriceForTest = true;

		// draw card
		CandyLandGame.drawCard();

		// user's turn should be skipped, and they'll still be on licorice
		assertEquals(12, CandyLandGame.getCurrentPositionIndex(0));

		// computer 1 should have moved to 8 since, they drew the next card in deck instead of user
		assertEquals(8, CandyLandGame.getCurrentPositionIndex(1));

		// new round of drawing cards
		CandyLandGame.drawCard();

		//user should move from licorice space as they already did their skipped turn
		assertNotEquals(12, CandyLandGame.getCurrentPositionIndex(0));

		CandyLandGame.enableLicoriceForTest = false;
	}

	/**
	 * TODO: v3.0
	 * --------------------------------------------------------------------
	 * The block of testing handles v3.0 white box testing where applicable
	 * 
	 * These test cases handle the following requirements:
	 * 4.3.0 - Users should be able to choose their game piece before starting the game.
	 * --------------------------------------------------------------------
	 */
	
	/*
	 * Choose game piece screen
	 * (Requirement 4.3.0)
	 */
	@Test
	public void chooseGamePiece()
	{
		new GameScreen(game);
		CandyLandGame.activeGame = true;

		// set new game piece selection, as you would on game piece screen
		PieceSelectionScreen.pieceSelection = 3;

		// after pressing play, a new game screen is called
		new GameScreen(game);

		// confirm getPlayerTokens() is set back to the appropriate values
		assertEquals(3, GameScreen.getPlayerTokens()[0]);
		
		/*
		 * Loading and Saving Player Piece Choice
		 */
		// save game with updated piece selection
		CandyLandGame.LoadSaveManager.write(1, GameScreen.getPlayerTokens(), CandyLandGame.playerPositions, CandyLandGame.cards, CandyLandGame.skipNextTurn);

		// set piece selection to default
		PieceSelectionScreen.pieceSelection = 0;
		
		// new game screen to set tokens array back to default
		new GameScreen(game);
		assertEquals(0, GameScreen.getPlayerTokens()[0]);

		// confirm a load
		assertTrue(CandyLandGame.load(1));

		// confirm selection gets set back when loading
		assertEquals(3, PieceSelectionScreen.pieceSelection);

		// after load button is pressed on main menu, gamescreen is called
		new GameScreen(game);
		
		// confirm getPlayerTokens() is set back to the appropriate values
		assertEquals(3, GameScreen.getPlayerTokens()[0]);
	}

	/**
	 * TODO: v4.0
	 * --------------------------------------------------------------------
	 * The block of testing handles v4.0 white box testing where applicable
	 * 
	 * These test cases handle the following requirements:
	 * 3.0.0 - The game should support the ability for game state to be saved and loaded.
	 * --------------------------------------------------------------------
	 */

	/*
	 * Game save and load
	 * (Requirement 3.0.0)
	 */
	@Test
	public void saveAndLoadGame()
	{
		new CandyLandGame(game);
		CandyLandGame.activeGame = true;

		// simulate 3 card draws
		for (int i = 0; i < 3; i++)
		{
			CandyLandGame.drawCard();
		}

		// set to something other than default, which is 0
		GameScreen.getPlayerTokens()[0] = 9;

		// confirm array was updated
		assertEquals(9, GameScreen.getPlayerTokens()[0]);

		// obtain all values we are going to save
		int[] beforeToken = GameScreen.getPlayerTokens();
		int[] beforePlayerPositions = CandyLandGame.playerPositions;
		List<String> beforeCards = CandyLandGame.cards;
		boolean[] beforeLicorice = CandyLandGame.skipNextTurn;

		// save number of cards in deck to compare
		int deckLength = beforeCards.size();

		/**
		 * Write save in slot 1
		 */
		CandyLandGame.LoadSaveManager.write(1, GameScreen.getPlayerTokens(), CandyLandGame.playerPositions, CandyLandGame.cards, CandyLandGame.skipNextTurn);

		// reset all values
		CandyLandGame.resetValues();
		new GameScreen(game);
		CandyLandGame.activeGame = true;

		// ensure values were reset
		assertNotEquals(deckLength, CandyLandGame.cards.size());
		assertEquals(0, CandyLandGame.playerPositions[0]);
		assertEquals(0, GameScreen.getPlayerTokens()[0]);
		assertNotEquals(beforePlayerPositions[0], CandyLandGame.playerPositions[0]);

		/**
		 * Read (load) save from slot 1
		 */
		assertTrue(CandyLandGame.load(1));

		// ensure all values are loaded back properly
		assertEquals(beforeToken[0], GameScreen.getPlayerTokens()[0]);
		assertEquals(beforeCards, CandyLandGame.cards);
		assertEquals(beforePlayerPositions[2], CandyLandGame.playerPositions[2]);
		assertEquals(beforeLicorice[1], CandyLandGame.skipNextTurn[1]);

		// should not be able to load slot 2 as we do not have anything written here
		assertFalse(CandyLandGame.load(2));
	}
}


/**
 * CandyLandGame.java
 * This class represents the card deck and main game logic for Candy Land
 */

package com.candyland.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Timer;

public class CandyLandGame
{
	// Constants
	private static final String[] COLORS = {"Purple", "Yellow", "Blue", "Green", "Orange", "Red"};
	private static final int SINGLE_CARDS = 8;
	private static final int DOUBLE_CARDS = 2;

	// Local Variables
	private static int playerIndex;
	public static boolean drawCardPressed = false;

	// Public Variables
	public static final int GAME_PIECES = 4;
	public static boolean loadedGame = false;
	public static boolean[] skipNextTurn = new boolean[GAME_PIECES];
	public static int[] playerPositions = new int[GAME_PIECES];
	public static boolean[] skipCurrentTurn = new boolean[GAME_PIECES];
	public static boolean validPress = false;
	public static boolean activeGame = false;
	public static boolean firstPress = true;
	public static boolean[] shortcutTaken = new boolean[GAME_PIECES];
	public static String[][] drawnCards = new String[GAME_PIECES][2];
	public static int gameWinner = -1;
	public static boolean validMousePress = false;
	public static List<String> cards;
	public static CandyLandLoad LoadSaveManager = new CandyLandLoad();
	
	// necessary for white-box testing to remove GUI/LibGDX calls
	public static boolean includesScreenLogic = true;
	public static boolean enableLicoriceForTest = false;

	public CandyLandGame(CandyLandMain game)
	{
		// reset values on new game call if not using libgdx calls
		if (!includesScreenLogic)
		{
			resetValues();
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
	 * Reset all CandyLandGame public variables to their initial state.
	 * Calling a new GameScreen will otherwise persist these values.
	 */
	public static void resetValues()
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
	public static void handleInput()
	{
		// Checks if actively viewing a valid game and it is user's turn
		if (playerIndex == 0 && validPress && gameWinner == -1 && !GameScreen.pausedState)
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
	 * Array is used to display card text visually on GameScreen.
	 */
	public static void resetDrawnCards()
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
	private static void initializeCards()
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
	 * skipCurrentTurn indicates licorice text on screen for GameScreen.
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
	public static void resetShortcutTaken()
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
	private static void shuffleCards()
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
	public static void drawCard()
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
	private static void handleComputerTurn()
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
								System.out.println("Stayed at position " + getBoardPosition(playerPositions[playerIndex]) + "/" + GameScreen.MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]) + "\n");
							}
							skipCurrentTurn[playerIndex] = true;
							if (includesScreenLogic)
							{
								GameScreen.setPiecePosition();
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
									System.out.println("Stayed at position " + getBoardPosition(playerPositions[playerIndex]) + "/" + GameScreen.MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]) + "\n");
								}
								skipCurrentTurn[playerIndex] = true;
								if (includesScreenLogic)
								{
									GameScreen.setPiecePosition();
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
	public static int getCurrentPositionIndex(int index)
	{
		return playerPositions[index];
	}

	/**
	 * Determine if a player has won the game
	 * (Requirement 1.3.0)
	 */
	public static void checkWinner()
	{
		// Prevent multiple players from winning in the same round
		if (gameWinner == -1)
		{
			for (int i = 0; i < GAME_PIECES; i++)
			{
				if (playerPositions[i] >= GameScreen.MAX_POSITIONS)
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
								GameScreen.switchToWinnerScreen(winnerIndex);
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
	public static void checkEmptyDeck()
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
	private static String getPiece(int index)
	{
		String texture = GameScreen.getTextureString(index);
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
	public static int calculateSteps(String nextCard)
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
	public static void movePlayer(String nextCard, int steps)
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
			System.out.println("Moved to position " + getBoardPosition(playerPositions[playerIndex]) + "/" + GameScreen.MAX_POSITIONS + ": " + getCurrentColor(playerPositions[playerIndex]));
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
		if (playerPositions[playerIndex] >= GameScreen.MAX_POSITIONS)
		{
			playerPositions[playerIndex] = GameScreen.MAX_POSITIONS;
		}
		// Move game piece on GameScreen to new position
		if (includesScreenLogic)
		{
			GameScreen.setPiecePosition();
		}
	}

	/**
	 * Determine if player landed on a shortcut location
	 * (Requirement 4.0.1)
	 */
	private static void handleShortcuts()
	{
		if (playerPositions[playerIndex] == 27)
		{
			// Set player position to shortcut destination
			playerPositions[playerIndex] = 56;
			if (CandyLandMain.DEBUG)
			{
				System.out.println("Landed on the Rainbow Trail shortcut!");
				System.out.println("New position is " + getBoardPosition(playerPositions[playerIndex]) + "/" + GameScreen.MAX_POSITIONS + ": "  + getCurrentColor(playerPositions[playerIndex]));
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
				System.out.println("New position is " + getBoardPosition(playerPositions[playerIndex]) + "/" + GameScreen.MAX_POSITIONS + ": "  + getCurrentColor(playerPositions[playerIndex]));
			}
			shortcutTaken[playerIndex] = true;
		}
	}

	/**
	 * Determine if player landed on a licorice location
	 * (Requirement 4.0.2)
	 */
	private static void handleLicorice()
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
	private static int getBoardPosition(int currentPlayerPosition)
	{
		return currentPlayerPosition + 1;
	}

	/**
	 * Determine the number of steps a given color card will move a player
	 * 
	 * @param	color - a card color
	 * @return	the number of steps the current player will be moved
	 */
	public static int getStepsFromColor(String color)
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
	private static void removeCard()
	{
		cards.remove(0);
	}

	/**
	 * Determine the color of a playerPositions position
	 * 
	 * @param	position - a location within the playerPositions array (0 : X)
	 * @return	the color of the specified position
	 */
	public static String getCurrentColor(int position)
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
	public static int getCardValue(int position)
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
	private static String nextCard()
	{
		return cards.get(0);
	}

	/**
	 * Increment current player index
	 * Starts over at 0 once it would reach GAME_PIECES value.
	 */
	public static void incrementIndex()
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
	public static int getCurrentPlayerType()
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
	public static boolean load(int slotNumber)
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
			return true;
		}
		//file does not exist
		return false;
	}
}

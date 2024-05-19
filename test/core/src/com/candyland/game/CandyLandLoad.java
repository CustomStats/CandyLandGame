/**
 * CandyLandLoad.java
 * This class represents the saving and loading logic for Candy Land
 */

package com.candyland.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CandyLandLoad
{
	// Variables to store the save and load information
	private List<String> cards;
	private int[] playerPositions;
	private int[] playerTokens;
	private boolean[] licoriceStatus;

	public CandyLandLoad()
	{
		cards = new ArrayList<>();
	}

	/**
	 * Check if a saved_game_data_X file exists for the given slot number.
	 * If it exists, read the file and assign the information to the respective variables and return true.
	 * If it does not exist, return false.
	 * (Requirement 3.0.0)
	 * 
	 * @param slotNumber - integer indicating which save game slot to read from (1 : 3)
	 * @return boolean indicating if reading from specific slot was successful
	 */
	public boolean read(int slotNumber)
	{
		// three game save slots
		// currently only utilizes slot 1 when pressing save
		if (slotNumber < 1 || slotNumber > 3)
		{
			if (CandyLandMain.DEBUG)
			{
				System.err.println("Invalid slot number.");
			}
			return false;
		}
		// see if file exists and can be read
		try (BufferedReader reader = new BufferedReader(new FileReader("saved_game_data_" + slotNumber + ".txt")))
		{
			// obtain entire string of saved characters
			String line = reader.readLine();

			if (line == null || line.equals(""))
			{
				// empty string, no saved game
				if (CandyLandMain.DEBUG)
				{
					System.out.println("Slot " + slotNumber + " is empty.");
				}
				return false;
			}
			else
			{
				// string exists
				int firstLetter = 0;
				int playerCounter = -1;

				// obtain number of players
				while (line.charAt(firstLetter) < 'A' || line.charAt(firstLetter) > 'Z')
				{
					if (line.charAt(firstLetter) == '.')
					{
						playerCounter++;
					}
					firstLetter++;
				}

				// create variables to be filled by the saved information
				playerPositions = new int[playerCounter];
				playerTokens = new int[playerCounter];
				licoriceStatus = new boolean[playerCounter];
				boolean[] licoriceStatusValueChecked = new boolean[playerCounter];

				// initialize variables otherwise gives null error
				for (int i = 0; i < playerCounter; i++)
				{
					playerPositions[i] = 0;
					playerTokens[i] = -1;
					licoriceStatus[i] = false;
					licoriceStatusValueChecked[i] = false;
				}

				// clears card deck as cards variable is not re-initialized
				cards.clear();

				int currentPlayer = 0;

				// start at position 2 in the character string
				//as first two locations are used for difficulty check
				for (int i = 2; i < line.length(); i++)
				{
					// integer input that isn't a card
					if (line.charAt(i) >= '0' && line.charAt(i) <= '9' )
					{
						// valid player piece index
						if (playerTokens[currentPlayer] < 0)
						{
							playerTokens[currentPlayer] = line.charAt(i) - '0';
						}
						// add licorice status if the specific player index
						// has not had their licorice status checked yet
						else if (!licoriceStatusValueChecked[currentPlayer])
						{
							if (line.charAt(i) == '1')
							{
								licoriceStatus[currentPlayer] = true;
							}
							licoriceStatusValueChecked[currentPlayer] = true;
						}
						// player position
						else
						{
							playerPositions[currentPlayer] *= 10;
							playerPositions[currentPlayer] += line.charAt(i) - '0';
						}
					}
					// increment player index
					else if (line.charAt(i) == '.')
					{
						currentPlayer++;
					}
					// character input is a card within the card deck
					else if (line.charAt(i) >= 'A' && line.charAt(i) <= 'Z' )
					{
						// valid card within the deck
						switch (line.charAt(i))
						{
						case 'A':
							cards.add("Red");
							break;
						case 'B':
							cards.add("Orange");
							break;
						case 'C':
							cards.add("Yellow");
							break;
						case 'D':
							cards.add("Green");
							break;
						case 'E':
							cards.add("Blue");
							break;
						case 'F':
							cards.add("Purple");
							break;
						case 'G':
							cards.add("Double Red");
							break;
						case 'H':
							cards.add("Double Orange");
							break;
						case 'I':
							cards.add("Double Yellow");
							break;
						case 'J':
							cards.add("Double Green");
							break;
						case 'K':
							cards.add("Double Blue");
							break;
						case 'L':
							cards.add("Double Purple");
							break;
						case 'M':
							cards.add("Peppermint Forest");
							break;
						case 'N':
							cards.add("Gumdrop Mountains");
							break;
						case 'O':
							cards.add("Peanut Acres");
							break;
						case 'P':
							cards.add("Lollipop Woods");
							break;
						}
					}
				}
				if (CandyLandMain.DEBUG)
				{
					System.out.println("Slot " + slotNumber + " loaded.");
				}
			}
		}
		catch (IOException e)
		{
			if (CandyLandMain.DEBUG)
			{
				System.err.println("Slot " + slotNumber + " is empty.");
			}
			return false;
		}
		return true;
	}

	/**
	 * Write to saved_game_data_X, where X is indicated by the input integer slotNumber.
	 * Values are passed as parameters and transitioned into a character output stream.
	 * This is necessary for writing arrays to a file, as libGDX's Preferences does
	 * not support array saving / loading.
	 * (Requirement 3.0.0)
	 * 
	 * @param slotNumber - integer indicating which save game slot to write to.
	 * @param playerTokens - integer array indicating index of each player's player piece. See GameScreen's setTextureString().
	 * @param playerPositions - integer array indiciating each player's position on the board.
	 * @param cards - list of strings representing the current card deck.
	 * @param playerLicoriceStatus - boolean array indicating if next player's turn should be skipped due to being on a licorice location.
	 * @return boolean indicating if writing to specific slot was successful
	 */
	public void write(int slotNumber, int[] playerTokens, int[] playerPositions, List<String> cards, boolean[] playerLicoriceStatus)
	{
		// create output stream to store each paramater as a character
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("saved_game_data_" + slotNumber + ".txt")))
		{
			// string to store all information from the characters generated
			String toString = "";

			// store difficulty
			// currently not implemented
			toString += "0.";

			// loop through each player index
			for (int i = 0; i < playerPositions.length; i++)
			{
				// add the index of each player's player piece
				toString += playerTokens[i];

				// add status of skipNextTurn[] boolean
				if (playerLicoriceStatus[i]) toString += '1';
				else toString += '0';

				// player's position on the board
				toString += playerPositions[i];

				toString += '.';
			}

			// each card in the current card deck saved as an associated character (letter)
			for (String card : cards)
			{
				switch (card)
				{
				case "Red":
					toString += 'A';
					break;
				case "Orange":
					toString += 'B';
					break;
				case "Yellow":
					toString += 'C';
					break;
				case "Green":
					toString += 'D';
					break;
				case "Blue":
					toString += 'E';
					break;
				case "Purple":
					toString += 'F';
					break;
				case "Double Red":
					toString += 'G';
					break;
				case "Double Orange":
					toString += 'H';
					break;
				case "Double Yellow":
					toString += 'I';
					break;
				case "Double Green":
					toString += 'J';
					break;
				case "Double Blue":
					toString += 'K';
					break;
				case "Double Purple":
					toString += 'L';
					break;
				case "Peppermint Forest":
					toString += 'M';
					break;
				case "Gumdrop Mountains":
					toString += 'N';
					break;
				case "Peanut Acres":
					toString += 'O';
					break;
				case "Lollipop Woods":
					toString += 'P';
					break;
				}
			}
			// write final string of characters to file
			writer.write(toString);
			if (CandyLandMain.DEBUG)
			{
				System.out.println("Game saved in Slot " + slotNumber + ".");
			}
		}
		catch (IOException e)
		{
			if (CandyLandMain.DEBUG)
			{
				System.err.println("Error writing to file: " + e.getMessage());
			}
		}
	}

	/**
	 * Obtain list of current deck of cards.
	 * Utilized for saving/loading.
	 * 
	 * @return	list of cards in the deck
	 */
	public List<String> getCards()
	{
		return cards;
	}

	/**
	 * Obtain array of each player's positions.
	 * Utilized for saving/loading.
	 * 
	 * @return	integer array with player positions
	 */
	public int[] getPlayerPositions()
	{
		return playerPositions;
	}

	/**
	 * Obtain each player's game piece.
	 * Utilized for saving/loading.
	 * 
	 * @return	integer array with index of each player's game piece token
	 */
	public int[] getPlayerTokens()
	{
		return playerTokens;
	}

	/**
	 * Obtain licorice status of each player indicating if their next turn will be skipped.
	 * Utilized for saving/loading.
	 * 
	 * @return	boolean array with status of needing to skip next turn
	 */
	public boolean[] getLicoriceStatus()
	{
		return licoriceStatus;
	}
}

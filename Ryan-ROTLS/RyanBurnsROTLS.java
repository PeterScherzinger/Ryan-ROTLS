/** The "Ryan Burns ROTLS" class.
  * @author Ryan Burns, Peter Scherzinger, Matan Feldberg
  * @version last updated January 2013
  */
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class RyanBurnsROTLS extends JFrame implements ActionListener
{
    private JMenuItem newOption, exitOption, rulesMenuItem, aboutMenuItem;
    private World gameArea;
    public RyanBurnsROTLS ()
    {

	super ("Ryan Burns and the Raiders of the Lost School");
	//setLocation (100, 100);

	Container contentPane = getContentPane ();
	gameArea = new World ();
	contentPane.add (gameArea, BorderLayout.CENTER);
	// Adds the menu and menu items to the frame (see below for code)
	// Set up the Game MenuItems
	newOption = new JMenuItem ("New");
	newOption.setAccelerator (
		KeyStroke.getKeyStroke (KeyEvent.VK_N, InputEvent.CTRL_MASK));
	newOption.addActionListener (this);

	exitOption = new JMenuItem ("Exit");
	exitOption.setAccelerator (
		KeyStroke.getKeyStroke (KeyEvent.VK_X, InputEvent.CTRL_MASK));
	exitOption.addActionListener (this);

	// Set up the Help Menu
	JMenu helpMenu = new JMenu ("Help");
	helpMenu.setMnemonic ('H');
	rulesMenuItem = new JMenuItem ("Rules...", 'R');
	rulesMenuItem.addActionListener (this);
	helpMenu.add (rulesMenuItem);
	aboutMenuItem = new JMenuItem ("About...", 'A');
	aboutMenuItem.addActionListener (this);
	helpMenu.add (aboutMenuItem);

	// Add each MenuItem to the Game Menu (with a separator)
	JMenu gameMenu = new JMenu ("Game");
	gameMenu.add (newOption);
	gameMenu.addSeparator ();
	gameMenu.add (exitOption);
	JMenuBar mainMenu = new JMenuBar ();
	mainMenu.add (gameMenu);
	mainMenu.add (helpMenu);
	// Set the menu bar for this frame to mainMenu
	setJMenuBar (mainMenu);

    } // Constructor


    /** Responds to a Menu Event.  This method is needed since our
      * Connect Four frame implements ActionListener
      * @param event the event that triggered this method
      */
    public void actionPerformed (ActionEvent event)
    {
	if (event.getSource () == newOption)   // Selected "New"
	{
	    gameArea.newGame ();
	    repaint ();
	}
	else if (event.getSource () == exitOption)  // Selected "Exit"
	{
	    hide ();
	    System.exit (0);
	}
	else if (event.getSource () == rulesMenuItem)  // Selected "Rules"
	{
	    JOptionPane.showMessageDialog (this,
		    "Reach the exit of the map without being knocked out." +
		    "\nPress the 'H' key to pick up nearby water." +
		    "\nPress the 'J' key to dump water on nearby lava." +
		    "\nPress the 'K' key to drop a stick of dynamite." +
		    "\nDynamite has a 1 square explosion radius.",
		    "Rules",
		    JOptionPane.INFORMATION_MESSAGE);
	}
	else if (event.getSource () == aboutMenuItem)  // Selected "About"
	{
	    JOptionPane.showMessageDialog (this,
		    "by\n Ryan Burns\n Peter Scherzinger\n and Matan Feldberg" +
		    "\n\u00a9 2013", "About ROTLS",
		    JOptionPane.INFORMATION_MESSAGE);
	}
    }


    private class World extends JPanel
    {
	private final int IMAGE_WIDTH;
	private final int IMAGE_HEIGHT;

	private final int UNMOVEABLEBOULDER = 7;
	private final int BOULDER = 6;
	private final int PIT = 5;
	private final int EXIT = 4;
	private final int WATER = 3;
	private final int LAVA = 2;
	private final int DYNAMITE = 1;
	private final int FLOOR = 0;
	private final int ACTIVE_DYNAMITE = 14;

	// Number association for inventory items
	private final int NO_OF_ITEMS = 2;
	private final int BUCKET = 0;
	private final int EXPLOSIVES = 1;


	private Image[] gridImages;
	private Image playerImage;
	private Image inventoryEmpty;
	private Image inventoryFull;

	private int dynamiteRow;
	private int dynamiteColumn;
	private int dynamiteTime;
	private Timer dynamiteTimer;
	private int[] [] grid;
	private int inventory[];
	private int currentRow;
	private int currentColumn;
	private int worldLevel;
	private int steps;
	private int score;
	private Font serifFont = new Font ("Serif", Font.BOLD, 24);

	public World ()
	{

	    gridImages = new Image [15];

	    gridImages [0] = new ImageIcon ("graphics\\Stone.png").getImage ();
	    gridImages [1] = new ImageIcon ("graphics\\Dynamite.png").getImage ();
	    gridImages [2] = new ImageIcon ("graphics\\Lava1.png").getImage ();
	    gridImages [3] = new ImageIcon ("graphics\\Water1.png").getImage ();
	    gridImages [4] = new ImageIcon ("graphics\\EXIT.png").getImage ();
	    gridImages [5] = new ImageIcon ("graphics\\Pit.png").getImage ();
	    gridImages [6] = new ImageIcon ("graphics\\Boulder1.png").getImage ();
	    gridImages [7] = new ImageIcon ("graphics\\UnmoveAble.png").getImage ();
	    gridImages [8] = new ImageIcon ("graphics\\Dynamite1.png").getImage ();
	    gridImages [9] = new ImageIcon ("graphics\\Dynamite2.png").getImage ();
	    gridImages [10] = new ImageIcon ("graphics\\Dynamite3.png").getImage ();
	    gridImages [11] = new ImageIcon ("graphics\\Dynamite4.png").getImage ();
	    gridImages [12] = new ImageIcon ("graphics\\Dynamite5.png").getImage ();
	    gridImages [13] = new ImageIcon ("graphics\\Dynamite6.png").getImage ();
	    gridImages [14] = new ImageIcon ("graphics\\Dynamite7.png").getImage ();
	    playerImage = new ImageIcon ("graphics\\Character.png").getImage ();
	    inventoryEmpty = new ImageIcon ("graphics\\InventoryNoWater.png").getImage ();
	    inventoryFull = new ImageIcon ("graphics\\InventoryWithWater.png").getImage ();


	    dynamiteTimer = new Timer (500, new TimerEventHandler ());
	    worldLevel = 1;
	    newGame ();

	    IMAGE_WIDTH = gridImages [0].getWidth (this);
	    IMAGE_HEIGHT = gridImages [0].getHeight (this);
	    Dimension size = new Dimension (grid [0].length * IMAGE_WIDTH + 250,
		    grid.length * IMAGE_HEIGHT);
	    this.setPreferredSize (size);

	    this.setFocusable (true);
	    this.addKeyListener (new KeyHandler ());
	    this.requestFocusInWindow ();


	}


	public void paintComponent (Graphics g)
	{
	    super.paintComponent (g);

	    // Redraw the grid with current images
	    for (int row = 0 ; row < grid.length ; row++)
		for (int column = 0 ; column < grid [0].length ; column++)
		{
		    // Put a path underneath everywhere
		    g.drawImage (gridImages [0],
			    column * IMAGE_WIDTH,
			    row * IMAGE_HEIGHT, this);
		    int imageNo = grid [row] [column];
		    g.drawImage (gridImages [imageNo],
			    column * IMAGE_WIDTH,
			    row * IMAGE_HEIGHT, this);
		}

	    // Draw the moving player on top of the grid
	    g.drawImage (playerImage,
		    currentColumn * IMAGE_WIDTH,
		    currentRow * IMAGE_HEIGHT, this);
	    // Draw empty inventory
	    if (inventory [BUCKET] == 0)
		g.drawImage (inventoryEmpty, 650, 0, this);
	    else
		g.drawImage (inventoryFull, 650, 0, this);
	    // Draw score and amount of dynamite
	    g.setColor (Color.RED);
	    g.setFont (serifFont);
	    g.drawString (String.valueOf (inventory [EXPLOSIVES]), 860, 124);
	    g.drawString (String.valueOf (score), 785, 373);
	    g.drawString (String.valueOf (steps), 785, 463);
	} // paint component method


	public void newGame ()
	{
	    // Initial position of the player
	    String worldFileName = "levels\\world" + worldLevel + ".txt";
	    dynamiteTime = -1;
	    currentRow = 1;
	    currentColumn = 1;
	    steps = 0;
	    score = 1000;

	    // Load up the file for the maze (try catch, is for file io errors)
	    try
	    {
		// Find the size of the file first to size the array
		// Standard Java file input (better than hsa.TextInputFile)
		BufferedReader worldFile =
		    new BufferedReader (new FileReader (worldFileName));

		// Assume file has at least 1 line
		int noOfRows = 1;
		String rowStr = worldFile.readLine ();
		int noOfColumns = rowStr.length ();

		// Read and count the rest of rows until the end of the file
		String line;
		while ((line = worldFile.readLine ()) != null)
		{
		    noOfRows++;
		}
		worldFile.close ();

		// Set up the array
		grid = new int [noOfRows] [noOfColumns];

		// Load in the file data into the grid (Need to re-open first)
		// In this example  the grid contains the characters '0' to '9'
		// So to translate each character into an integer we subtract '0'
		// If your maze has more than 10 types of squares you could use
		// letters in your file 'a' to 'z' and then subtract 'a' to
		// translate each letter into an integer
		worldFile =
		    new BufferedReader (new FileReader (worldFileName));
		for (int row = 0 ; row < grid.length ; row++)
		{
		    rowStr = worldFile.readLine ();
		    for (int column = 0 ; column < grid [0].length ; column++)
		    {
			grid [row] [column] = (int) (rowStr.charAt (column) - '0');
			if (grid [row] [column] == 9)
			{
			    currentColumn = column;
			    currentRow = row;
			    grid [row] [column] = 0;
			}
		    }
		}

		worldFile.close ();

		// Create the inventory
		inventory = new int [NO_OF_ITEMS];

		for (int item = 0 ; item < inventory.length ; item++)
		    inventory [item] = 0;
	    }
	    catch (IOException e)
	    {
		JOptionPane.showMessageDialog (this, worldFileName +
			" not a valid world file", "Message - Invalid World File",
			JOptionPane.WARNING_MESSAGE);
		System.exit (0);

	    }


	}




	// Inner class to handle key events
	private class KeyHandler extends KeyAdapter
	{
	    public boolean canMove (int checkRow, int checkColumn)
	    {
		if (grid [checkRow] [checkColumn] < BOULDER)
		    return true;
		return false;
	    }


	    public void keyPressed (KeyEvent event)
	    {

		// Change the currentRow and currentColumn of the player

		// based on the key pressed
		int pendingRow = currentRow;
		int pendingColumn = currentColumn;

		// If left arrow key is pressed, and the space to your left is not the end of the map
		if (event.getKeyCode () == KeyEvent.VK_LEFT && currentColumn > 0)
		{
		    // Change the pending column from the currently occupied column, to the one on the left
		    pendingColumn--;

		    // Use the canMove method to determine if the place you want to move is not already occupied
		    // If you can move, then move
		    if (this.canMove (pendingRow, pendingColumn))
		    {
			currentColumn--;
			steps++;
			score -= 2;
		    }

		    // Otherwise if the space you wish to enter has a moveable boulder in it, and the space
		    // after that is not outside the map and is empty, then push the boulder
		    else if (grid [pendingRow] [pendingColumn] == BOULDER && pendingColumn - 1 >= 0)
		    {
			// If you are pushing the boulder on to a spot of empty floor
			if (grid [pendingRow] [pendingColumn - 1] == FLOOR)
			{
			    grid [pendingRow] [pendingColumn - 1] = BOULDER;
			    currentColumn--;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
			// Otherwise if you are pushing the boulder into a pit, change the pit to floor and move forward
			else if (grid [pendingRow] [pendingColumn - 1] == PIT)
			{
			    grid [pendingRow] [pendingColumn - 1] = FLOOR;
			    currentColumn--;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
		    }
		}
		//if right arrow key is pressed and space to the right is not the end of the map
		else if (event.getKeyCode () == KeyEvent.VK_RIGHT && currentColumn + 1 < grid [0].length)
		{
		    // Change the pending column from the currently occupied column, to the one on the right
		    pendingColumn++;
		    // Use the canMove method to determine if the place you want to move is not already occupied
		    // If you can move, then move
		    if (this.canMove (pendingRow, pendingColumn))
		    {
			currentColumn++;
			steps++;
			score -= 2;
		    }
		    // Otherwise if the space you wish to enter has a moveable boulder in it, and the space
		    // after that is not outside the map and is empty, then push the boulder
		    else if (grid [pendingRow] [pendingColumn] == BOULDER && pendingColumn + 1 < grid [0].length)
		    {
			if (grid [pendingRow] [pendingColumn + 1] == FLOOR)
			{
			    grid [pendingRow] [pendingColumn + 1] = BOULDER;
			    currentColumn++;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
			else if (grid [pendingRow] [pendingColumn + 1] == PIT)
			{
			    grid [pendingRow] [pendingColumn + 1] = FLOOR;
			    currentColumn++;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}

		    }
		}

		//if up arrow key is pressed and space above is not the end of the map
		else if (event.getKeyCode () == KeyEvent.VK_UP && currentRow > 0)
		{
		    // Change the pending row from the currently occupied row, to the one on the above it
		    pendingRow--;
		    // Use the canMove method to determine if the place you want to move is not already occupied
		    // If you can move, then move
		    if (this.canMove (pendingRow, pendingColumn))
		    {
			currentRow--;
			steps++;
			score -= 2;
		    }
		    // Otherwise if the space you wish to enter has a moveable boulder in it, and the space
		    // after that is not outside the map and is empty, then push the boulder
		    else if (grid [pendingRow] [pendingColumn] == BOULDER && pendingRow - 1 >= 0)
		    {
			if (grid [pendingRow - 1] [pendingColumn] == FLOOR)
			{
			    grid [pendingRow - 1] [pendingColumn] = BOULDER;
			    currentRow--;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
			else if (grid [pendingRow - 1] [pendingColumn] == PIT)
			{
			    grid [pendingRow - 1] [pendingColumn] = FLOOR;
			    currentRow--;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}

		    }
		}
		//if down arrow key is pressed and space below is not the end of the map
		else if (event.getKeyCode () == KeyEvent.VK_DOWN && currentRow + 1 < grid.length)
		{
		    // Change the pending row from the currently occupied row, to the one on the below it
		    pendingRow++;

		    // Use the canMove method to determine if the place you want to move is not already occupied
		    // If you can move, then move
		    if (this.canMove (pendingRow, pendingColumn))
		    {
			currentRow++;
			steps++;
			score -= 2;
		    }
		    // Otherwise if the space you wish to enter has a moveable boulder in it, and the space
		    // after that is not outside the map and is empty, then push the boulder
		    else if (grid [pendingRow] [pendingColumn] == BOULDER && pendingRow + 1 < grid.length)
		    {
			if (grid [pendingRow + 1] [pendingColumn] == FLOOR)
			{
			    grid [pendingRow + 1] [pendingColumn] = BOULDER;
			    currentRow++;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
			else if (grid [pendingRow + 1] [pendingColumn] == PIT)
			{
			    grid [pendingRow + 1] [pendingColumn] = FLOOR;
			    currentRow++;
			    steps++;
			    score -= 2;
			    grid [currentRow] [currentColumn] = FLOOR;
			}
		    }
		}
		// If the 'P' key is pressed, pick up any water within 1 space (excluding diagonal)
		// An empty pit will be left in place of the water picked up
		// If the players bucket is empty:
		else if (event.getKeyCode () == KeyEvent.VK_H && inventory [BUCKET] == 0)
		{
		    // Check the space below the player for water
		    if (pendingRow + 1 <= 9 && grid [pendingRow + 1] [pendingColumn] == WATER)
		    {
			grid [pendingRow + 1] [pendingColumn] = PIT;
			inventory [BUCKET] = 1;
		    }
		    // Check the space above the player for water
		    else if (pendingRow - 1 >= 0 && grid [pendingRow - 1] [pendingColumn] == WATER)
		    {
			grid [pendingRow - 1] [pendingColumn] = PIT;
			inventory [BUCKET] = 1;
		    }
		    // Check the space to the right of the player for water
		    else if (pendingColumn + 1 <= 9 && grid [pendingRow] [pendingColumn + 1] == WATER)
		    {
			grid [pendingRow] [pendingColumn + 1] = PIT;
			inventory [BUCKET] = 1;
		    }
		    //Check the space to the left of the player for water
		    else if (pendingColumn - 1 >= 0 && grid [pendingRow] [pendingColumn - 1] == WATER)
		    {
			grid [pendingRow] [pendingColumn - 1] = PIT;
			inventory [BUCKET] = 1;
		    }
		}
		// If the 'L' key is pressed, dump your bucket of water on any lava within 1 space
		// The lava will be turned to stone
		// If the players bucket contains water:
		else if (event.getKeyCode () == KeyEvent.VK_J && inventory [BUCKET] == 1)
		{
		    // Check the space below the player for lava
		    if (grid [pendingRow + 1] [pendingColumn] == LAVA)
		    {
			grid [pendingRow + 1] [pendingColumn] = FLOOR;
			inventory [BUCKET] = 0;
		    }
		    // Check the space above the player for lava
		    else if (grid [pendingRow - 1] [pendingColumn] == LAVA)
		    {
			grid [pendingRow - 1] [pendingColumn] = FLOOR;
			inventory [BUCKET] = 0;
		    }
		    // Check the space to the right of the player for lava
		    else if (grid [pendingRow] [pendingColumn + 1] == LAVA)
		    {
			grid [pendingRow] [pendingColumn + 1] = FLOOR;
			inventory [BUCKET] = 0;
		    }
		    // Check the space to the left of the player for lava
		    else if (grid [pendingRow] [pendingColumn - 1] == LAVA)
		    {
			grid [pendingRow] [pendingColumn - 1] = FLOOR;
			inventory [BUCKET] = 0;
		    }
		}
		// If the 'O' key is pressed, use one stick of dynamite to blow up any moveable boulders
		// within one space. Dropped Dynamite will explode after 3 seconds. If caught in the
		// explosion radius, player dies.
		else if (event.getKeyCode () == KeyEvent.VK_K && inventory [EXPLOSIVES] >= 1 && dynamiteTime == -1)
		{
		    // If the player is standing on empty floor, drop a stick of dynamite
		    if (grid [currentRow] [currentColumn] == FLOOR)
		    {
			// Record the location of the placed dynamite
			dynamiteRow = currentRow;
			dynamiteColumn = currentColumn;
			grid [currentRow] [currentColumn] = DYNAMITE;
			paintImmediately (0, 0, getWidth (), getHeight ());
			dynamiteTime = 7;
			dynamiteTimer.start ();
		    }

		}

		// Game over when score runs out
		if (score == 0)
		    newGame();
		    
		// Pick up dynamite
		if (grid [currentRow] [currentColumn] == DYNAMITE && dynamiteTime == -1)
		{
		    grid [currentRow] [currentColumn] = FLOOR;
		    inventory [EXPLOSIVES]++;
		}

		// When the player enteres water or lava
		else if (grid [currentRow] [currentColumn] == WATER ||
			grid [currentRow] [currentColumn] == LAVA || grid [currentRow] [currentColumn] == PIT)
		{
		    newGame ();
		}

		else if (grid [currentRow] [currentColumn] == EXIT && dynamiteTime == -1)
		{
		    worldLevel++;
		    newGame ();
		}

		// Repaint the screen after the change
		repaint ();

	    }


	}

	/** An inner class to deal with the timer events
	    */
	private class TimerEventHandler implements ActionListener
	{

	    /** The following method is called each time a timer event is
	     * generated (every 500 milliseconds in this example)
	     * @param event the Timer event
	     */
	    public void actionPerformed (ActionEvent event)
	    {
		dynamiteTime--;
		grid [dynamiteRow] [dynamiteColumn] = ACTIVE_DYNAMITE - dynamiteTime;
		int explode = 0;
		if (dynamiteTime == -1)
		{
		    grid [dynamiteRow] [dynamiteColumn] = FLOOR; //This is the picture of the explosion
		    dynamiteTimer.stop ();
		    // Destroy boulders to the left and right (including diagonal)
		    for (int sides = -1 ; sides <= 1 ; sides++)
		    {
			if (currentRow == dynamiteRow + sides && currentColumn == dynamiteColumn - 1 ||
				currentRow == dynamiteRow + sides && currentColumn == dynamiteColumn + 1)
			{
			    newGame ();
			    explode++;
			}
			else
			{
			    if (dynamiteColumn > 0 && dynamiteRow + sides < 9 && dynamiteRow + sides > 0)
			    {
				if (grid [dynamiteRow + sides] [dynamiteColumn - 1] == BOULDER)
				    grid [dynamiteRow + sides] [dynamiteColumn - 1] = FLOOR;
				//Start the level over if the player is caught in the blast
			    }
			    if (dynamiteColumn < 9 && dynamiteRow + sides < 9 && dynamiteRow + sides > 0)
			    {
				if (grid [dynamiteRow + sides] [dynamiteColumn + 1] == BOULDER)
				    grid [dynamiteRow + sides] [dynamiteColumn + 1] = FLOOR;
				//Start the level over if the player is caught in the blast
			    }
			}
		    }

		    if (currentRow == dynamiteRow + 1 && currentColumn == dynamiteColumn)
			newGame ();
		    else if (currentRow == dynamiteRow - 1 && currentColumn == dynamiteColumn)
			newGame ();
		    else if (explode == 0)
		    {
			//Destroy a boulder below (if there is one there)
			if (dynamiteRow < 9)
			{
			    if (grid [dynamiteRow + 1] [dynamiteColumn] == BOULDER)
				grid [dynamiteRow + 1] [dynamiteColumn] = FLOOR;
			}
			// Destroy a boulder above (if there is one there)
			if (dynamiteRow > 1)
			{
			    if (grid [dynamiteRow - 1] [dynamiteColumn] == BOULDER)
				grid [dynamiteRow - 1] [dynamiteColumn] = FLOOR;
			}
		    }
		}

		// Repaint the screen

		repaint ();

	    }
	}
    }






    // Sets up the main frame for the Game
    public static void main (String[] args)
    {
	RyanBurnsROTLS frame = new RyanBurnsROTLS ();
	frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	frame.pack ();
	frame.setVisible (true);

    } // main method
} // MazeGame class



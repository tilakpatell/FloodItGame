import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;

//represents an interface ICell
interface ICell {
  public void floodAdd(ArrayList<Cell> nextFlooded);
}

//represents a EmptyCell that implements ICell
class EmptyCell implements ICell {

  // Adds the cell to the given list of cells to be flooded
  public void floodAdd(ArrayList<Cell> nextFlooded) {
    // returns nothing so it is blank
  }

}

//represents a Cell that implements ICell
class Cell implements ICell {
  int x;
  int y;
  Color color;
  boolean flooded;
  ICell left = new EmptyCell();
  ICell top = new EmptyCell();
  ICell right = new EmptyCell();
  ICell bottom = new EmptyCell();

  // Constructor
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  // draws a given cell by changing the scene variable and returning the new scene
  // with the rectangle
  public WorldScene drawCells(WorldScene scene, int size) {
    scene.placeImageXY(
        new RectangleImage((500 / size) + 1, (500 / size) + 1, OutlineMode.SOLID, color),
        x * 500 / size + 250 / size, y * 500 / size + 250 / size);
    return scene;
  }

  // Adds this cell to the nextFlooded ArrayList
  // Effect: Modifies the given nextFlooded ArrayList by adding this cell to it
  public void floodAdd(ArrayList<Cell> nextFlooded) {
    nextFlooded.add(this);

  }

  // Sets the adjacent cells (left, right, top, bottom) for this cell based on the
  // board size
  // Effect: Modifies the left, right, top, and bottom fields of this cell
  public void setAll(int size, ArrayList<Cell> board) {
    if (this.x != 0) {
      board.get(this.x + size * this.y).left = board.get((this.x - 1) + size * this.y);
    }
    if (this.x != size - 1) {
      board.get(this.x + size * this.y).right = board.get((this.x + 1) + size * this.y);
    }
    if (this.y != 0) {
      board.get(this.x + size * this.y).top = board.get(this.x + size * (this.y - 1));
    }
    if (this.y != size - 1) {
      board.get(this.x + size * this.y).bottom = board.get(this.x + size * (this.y + 1));
    }
  }
}

// represents a class utils
class Utils {

  Random rand = new Random();
  ArrayList<Color> colors = new ArrayList<>(Arrays.asList(Color.CYAN, Color.GREEN, Color.RED,
      Color.ORANGE, Color.YELLOW, Color.MAGENTA, Color.PINK, Color.DARK_GRAY));

  // seeds the random variable for testing purposes.
  // Effect: seeds a random value in rand variable.
  public void seedRandom(long seed) {
    this.rand = new Random(seed);
  }

  // get a specific color from a arrayList of colors and returns it.
  public Color getColor(int num) {
    if (num > colors.size()) {
      throw new IllegalArgumentException("Number of colors requested exceeds available colors.");
    }
    return colors.get(rand.nextInt(num));
  }
}

// represents a class FloodItWorld that extends world
class FloodItWorld extends World {
  ArrayList<Cell> board;
  ArrayList<Color> colorsal;
  ArrayList<Cell> nextFlooded = new ArrayList<Cell>();
  ArrayList<Cell> flood = new ArrayList<Cell>();
  ArrayList<Cell> checkCells = new ArrayList<Cell>();
  int size;
  int numOfColor;
  Utils u;
  Color currentColor;
  int steps;
  boolean running = false;
  int maxAmountSteps;
  String gameMessage = "";

  // Constructor
  FloodItWorld(int size, int numOfColor) {
    this.size = size;
    this.numOfColor = numOfColor;
    this.u = new Utils();
    initBoard(size);
  }

  // Initializes the game by drawing all the cells from the list on the board.
  // Effect: changes the scene so that all the cells are on it.
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(500, 500);
    if (size == 0) {
      return scene;
    }

    for (Cell cell : board) {
      cell.drawCells(scene, this.size);
    }

    if (running) {
      scene.placeImageXY(new TextImage(gameMessage, 32, FontStyle.BOLD, Color.RED), 250, 250);
    }
    else {
      scene.placeImageXY(new TextImage(this.steps + "/" + this.maxAmountSteps, 24, Color.BLACK),
          500 / this.size, 500 / this.size);
    }

    return scene;

  }

  // Links adjacent cells in the game.
  // Effect: Sets the board variable to an ArrayList full of cells that are then
  // drawn.
  void initBoard(int size) {
    this.board = new ArrayList<Cell>(size * size);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        board.add(new Cell(x, y, u.getColor(numOfColor)));
      }
    }

    for (Cell cell : board) {
      cell.setAll(size, board);
    }

    if (board.size() > 0) {
      this.board.get(0).flooded = true;
      this.maxAmountSteps = this.size + this.numOfColor + 5;
      this.steps = maxAmountSteps;
      this.currentColor = board.get(0).color;
      this.flood.add(board.get(0));
      this.running = false;
    }

  }

  // Processes the flooding of cells in the game
  // Effect: Modifies the flooded cells and updates the game state
  public void flooding() {
    for (int i = 0; i < flood.size(); i++) {
      if (!checkCells.contains(this.flood.get(i))) {
        checkCells.add(this.flood.get(i));

        if (this.flood.get(i).flooded || this.flood.get(i).color.equals(this.currentColor)) {
          this.flood.get(i).color = this.currentColor;
          this.flood.get(i).flooded = true;
          this.flood.get(i).top.floodAdd(this.nextFlooded);

          this.flood.get(i).bottom.floodAdd(this.nextFlooded);

          this.flood.get(i).left.floodAdd(this.nextFlooded);

          this.flood.get(i).right.floodAdd(this.nextFlooded);

        }

        else if (this.flood.get(i).color.equals(this.currentColor)) {
          this.flood.get(i).flooded = true;
          this.flood.get(i).top.floodAdd(this.nextFlooded);
          this.flood.get(i).bottom.floodAdd(this.nextFlooded);
          this.flood.get(i).left.floodAdd(this.nextFlooded);
          this.flood.get(i).right.floodAdd(this.nextFlooded);

        }
      }
    }

    this.flood = this.nextFlooded;
    this.nextFlooded.clear();

    if (this.flood.size() == 0) {
      this.checkCells.clear();
      this.running = true;
    }
  }

  // Handles tick events in the game world
  // Effect: Advances the game state by processing the flooding of cells
  public void onTick() {
    if (!running) {
      this.flooding(); // Continue game logic if the game is not marked as finished
    }

    // Check if all cells are flooded
    if (allCellsFlooded() && steps > 0) {
      gameMessage = "You Won!";
      running = true;
      endOfWorld(gameMessage);
    }
    else if (steps <= 0) {
      gameMessage = "You Lost!";
      running = true;
      endOfWorld(gameMessage);
    }
  }

  // Checks if all cells on the game board have been flooded with the same color
  // Effect: Returns true if all cells in the board have the same color as the
  // first cell,
  // false otherwise
  public boolean allCellsFlooded() {
    Color firstColor = board.get(0).color;
    for (Cell cell : this.board) {
      if (!cell.color.equals(firstColor)) {
        return false;
      }
    }
    return true;
  }

  // Handles mouse click events in the game world
  // Effect: Updates the current color and initiates flooding from the clicked
  // cell
  public void onMouseClicked(Posn posn) {
    // x and y of point clicked
    int x = posn.x / (500 / size);
    int y = posn.y / (500 / size);

    if (this.currentColor != board.get(x + y * size).color) {
      steps -= 1;
    }
    this.currentColor = board.get(x + y * size).color;
    this.flood.add(board.get(0));
    this.running = false;
  }

  // Handles key events in the game world
  // Effect: Resets the game board when 'r' key is pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board.clear();
      this.initBoard(size);
    }
  }

}

// Represents a ExamplesGame class
class ExamplesGame {
  Cell x;
  Cell y;
  Cell z;
  EmptyCell e;
  WorldScene scene;
  FloodItWorld world;
  Cell xLeft;
  FloodItWorld empty;

  // tests the bigBang function to make sure the game is working
  void testBigBang(Tester t) {
    FloodItWorld game = new FloodItWorld(14, 6);
    game.bigBang(500, 500, 0.1);
  }

  // sets the initialized data to a value
  void initData() {
    this.x = new Cell(15, 20, Color.BLUE);
    this.y = new Cell(15, 21, Color.GREEN);
    this.z = new Cell(0, 2, Color.WHITE);
    this.scene = new WorldScene(200, 200);
    this.e = new EmptyCell();
    this.world = new FloodItWorld(3, 3);
    this.xLeft = new Cell(12, 20, Color.YELLOW);
    this.empty = new FloodItWorld(0, 0);
  }

  // tests the getColor method
  void testGetColor(Tester t) {
    Utils utils = new Utils();

    utils.seedRandom(123);

    Color expectedColor = Color.RED;
    t.checkExpect(utils.getColor(3), expectedColor);

    expectedColor = Color.CYAN;
    t.checkExpect(utils.getColor(1), expectedColor);

    expectedColor = Color.DARK_GRAY;
    t.checkExpect(utils.getColor(8), expectedColor);

    t.checkException(
        new IllegalArgumentException("Number of colors requested exceeds available colors."), utils,
        "getColor", 9);
  }

  // tests the FoodAdd method
  void testFloodAdd(Tester t) {
    initData();
    ArrayList<Cell> isFlooded1 = new ArrayList<Cell>();
    x.floodAdd(isFlooded1);
    y.floodAdd(isFlooded1);

    ArrayList<Cell> result1 = new ArrayList<Cell>(Arrays.asList(this.x, this.y));

    t.checkExpect(isFlooded1, result1);

    ArrayList<Cell> isFlooded2 = new ArrayList<Cell>();
    e.floodAdd(isFlooded2);
    ArrayList<Cell> result2 = new ArrayList<Cell>();

    t.checkExpect(isFlooded2, result2);

    ArrayList<Cell> isFlooded3 = new ArrayList<Cell>();
    x.floodAdd(isFlooded3);
    y.floodAdd(isFlooded3);
    z.floodAdd(isFlooded3);

    ArrayList<Cell> result3 = new ArrayList<Cell>(Arrays.asList(this.x, this.y, this.z));
    t.checkExpect(isFlooded3, result3);
  }

  // test the drawcells method
  void testdrawCells(Tester t) {
    initData();
    x.drawCells(scene, 10);
    WorldScene scene1 = new WorldScene(200, 200);
    x.drawCells(scene1, 10);
    t.checkExpect(scene, scene1);

    initData();
    y.drawCells(scene, 30);
    WorldScene scene2 = new WorldScene(200, 200);
    y.drawCells(scene2, 30);

    t.checkExpect(scene, scene2);

    initData();
    z.drawCells(scene, 20);
    WorldScene scene3 = new WorldScene(200, 200);
    z.drawCells(scene3, 20);
    t.checkExpect(scene, scene3);
  }

  // tests the SetAll method
  void testSetAll(Tester t) {
    FloodItWorld world = new FloodItWorld(3, 3);
    world.initBoard(world.size);

    t.checkExpect(world.board.get(0).left, new EmptyCell());
    t.checkExpect(world.board.get(0).top, new EmptyCell());
    t.checkExpect(world.board.get(0).right, world.board.get(1));
    t.checkExpect(world.board.get(0).bottom, world.board.get(3));

    t.checkExpect(world.board.get(8).right, new EmptyCell());
    t.checkExpect(world.board.get(8).bottom, new EmptyCell());
    t.checkExpect(world.board.get(8).left, world.board.get(7));
    t.checkExpect(world.board.get(8).top, world.board.get(5));

    t.checkExpect(world.board.get(1).top, new EmptyCell());
    t.checkExpect(world.board.get(1).left, world.board.get(0));
    t.checkExpect(world.board.get(1).right, world.board.get(2));
    t.checkExpect(world.board.get(1).bottom, world.board.get(4));

    t.checkExpect(world.board.get(4).top, world.board.get(1));
    t.checkExpect(world.board.get(4).left, world.board.get(3));
    t.checkExpect(world.board.get(4).right, world.board.get(5));
    t.checkExpect(world.board.get(4).bottom, world.board.get(7));
  }

  // tests the makeScene method
  void testMakeScene(Tester t) {
    initData();
    WorldScene emptyWorld2 = new WorldScene(500, 500);
    t.checkExpect(this.empty.makeScene(), emptyWorld2);

    FloodItWorld world = new FloodItWorld(3, 3);
    world.initBoard(world.size);

    WorldScene expectedScene = new WorldScene(500, 500);
    world.board.get(0).drawCells(expectedScene, world.size);
    world.board.get(1).drawCells(expectedScene, world.size);
    world.board.get(2).drawCells(expectedScene, world.size);
    world.board.get(3).drawCells(expectedScene, world.size);
    world.board.get(4).drawCells(expectedScene, world.size);
    world.board.get(5).drawCells(expectedScene, world.size);
    world.board.get(6).drawCells(expectedScene, world.size);
    world.board.get(7).drawCells(expectedScene, world.size);
    world.board.get(8).drawCells(expectedScene, world.size);

    expectedScene.placeImageXY(
        new TextImage(world.steps + "/" + world.maxAmountSteps, 24, Color.BLACK), 500 / world.size,
        500 / world.size);

    t.checkExpect(world.makeScene(), expectedScene);

    world.running = true;
    world.gameMessage = "You Won!";
    WorldScene expectedEndScene = new WorldScene(500, 500);
    world.board.get(0).drawCells(expectedEndScene, world.size);
    world.board.get(1).drawCells(expectedEndScene, world.size);
    world.board.get(2).drawCells(expectedEndScene, world.size);
    world.board.get(3).drawCells(expectedEndScene, world.size);
    world.board.get(4).drawCells(expectedEndScene, world.size);
    world.board.get(5).drawCells(expectedEndScene, world.size);
    world.board.get(6).drawCells(expectedEndScene, world.size);
    world.board.get(7).drawCells(expectedEndScene, world.size);
    world.board.get(8).drawCells(expectedEndScene, world.size);

    expectedEndScene.placeImageXY(new TextImage("You Won!", 32, FontStyle.BOLD, Color.RED), 250,
        250);
    t.checkExpect(world.makeScene(), expectedEndScene);
  }

  // test the InitBoard method
  void testInitBoard(Tester t) {
    FloodItWorld world = new FloodItWorld(3, 3);
    world.initBoard(world.size);

    t.checkExpect(world.board.size(), 9);
    t.checkExpect(world.board.get(0).flooded, true);
    t.checkExpect(world.currentColor, world.board.get(0).color);
    t.checkExpect(world.board.get(0).right, world.board.get(1));
    t.checkExpect(world.board.get(0).bottom, world.board.get(3));
    t.checkExpect(world.board.get(0).left, new EmptyCell());
    t.checkExpect(world.board.get(0).top, new EmptyCell());

    t.checkExpect(world.board.get(4).top, world.board.get(1));
    t.checkExpect(world.board.get(4).left, world.board.get(3));
    t.checkExpect(world.board.get(4).right, world.board.get(5));
    t.checkExpect(world.board.get(4).bottom, world.board.get(7));

    Cell firstFloodedCell = world.flood.get(0);
    Cell firstBoardCell = world.board.get(0);
    t.checkExpect(firstFloodedCell.x, firstBoardCell.x);
    t.checkExpect(firstFloodedCell.y, firstBoardCell.y);
    t.checkExpect(firstFloodedCell.flooded, firstBoardCell.flooded);

    FloodItWorld emptyWorld = new FloodItWorld(0, 0);
    emptyWorld.initBoard(emptyWorld.size);

    t.checkExpect(emptyWorld.board.size(), 0);
    t.checkExpect(emptyWorld.flood.size(), 0);
    t.checkExpect(emptyWorld.currentColor, null);
  }

  // tests the flooding method
  void testFlooding(Tester t) {
    FloodItWorld world = new FloodItWorld(3, 3);
    world.initBoard(world.size);
    world.currentColor = world.board.get(0).color;
    world.flooding();

    // Manually check specific cells
    Cell cell0 = world.board.get(0);
    Cell cell1 = world.board.get(1);
    Cell cell2 = world.board.get(2);
    Cell cell3 = world.board.get(3);
    Cell cell4 = world.board.get(4);
    Cell cell5 = world.board.get(5);
    Cell cell6 = world.board.get(6);
    Cell cell7 = world.board.get(7);
    Cell cell8 = world.board.get(8);

    // Expected results
    boolean isCell0Flooded = cell0.color.equals(world.currentColor);
    boolean isCell2Flooded = cell2.color.equals(world.currentColor) && cell1.flooded;
    boolean isCell4Flooded = cell4.color.equals(world.currentColor)
        && (cell1.flooded || cell3.flooded);
    boolean isCell5Flooded = cell5.color.equals(world.currentColor) && cell4.flooded;
    boolean isCell6Flooded = cell6.color.equals(world.currentColor) && cell3.flooded;
    boolean isCell7Flooded = cell7.color.equals(world.currentColor)
        && (cell4.flooded || cell6.flooded);
    boolean isCell8Flooded = cell8.color.equals(world.currentColor) && cell7.flooded;

    // Check flooded state and nextFlooded list
    t.checkExpect(cell0.flooded, isCell0Flooded);
    t.checkExpect(cell2.flooded, isCell2Flooded);
    t.checkExpect(cell4.flooded, isCell4Flooded);
    t.checkExpect(cell5.flooded, isCell5Flooded);
    t.checkExpect(cell6.flooded, isCell6Flooded);
    t.checkExpect(cell7.flooded, isCell7Flooded);
    t.checkExpect(cell8.flooded, isCell8Flooded);
  }

  // tests the Reset Board Method
  void testResetBoard(Tester t) {
    initData();
    world.initBoard(3);
    world.onKeyEvent("r");

    t.checkExpect(world.board.size(), 9);
    t.checkExpect(world.flood.size(), 3);
    t.checkExpect(world.running, false);

    world.initBoard(3);
    world.onKeyEvent("a");

    t.checkExpect(world.board.size(), 9);
    t.checkExpect(world.flood.size(), 4);
    t.checkExpect(world.running, false);
  }

  // tests the OnMouseClick method
  void testOnMouseClicked(Tester t) {
    initData();
    FloodItWorld world = new FloodItWorld(5, 5);
    world.initBoard(5);
    int initialSteps = world.steps;
    world.currentColor = Color.BLUE;
    world.board.get(0).color = Color.BLUE;
    world.onMouseClicked(new Posn(0, 0));
    t.checkExpect(world.currentColor, world.board.get(0).color);
    t.checkExpect(world.steps, initialSteps);
    world.board.get(1 + 5 * 1).color = Color.RED;
    world.onMouseClicked(new Posn(100, 100));

    t.checkExpect(world.currentColor, world.board.get(1 + 5 * 1).color);
    t.checkExpect(world.steps, initialSteps - 1);
  }

}
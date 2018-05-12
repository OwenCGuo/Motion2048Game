package lab4_204_40.uwaterloo.ca.lab4_204_40;

    import android.app.Activity;
    import android.content.Context;
    import android.graphics.Color;
    import android.util.Log;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import java.util.Arrays;
    import java.util.LinkedList;
    import java.util.Random;
    import java.util.TimerTask;


public class GameLoopTask extends TimerTask {

    //initialize member fields here
    private Activity activity;
    private RelativeLayout rl;
    private Context context;

    LinkedList<GameBlock> myGBList = new LinkedList<GameBlock>();

    public int nextX;
    public int nextY;

    private int initialX;
    private int initialY;

    private static final int leftBound = -65;
    private static final int rightBound = 745;
    private static final int topBound = -65;
    private static final int bottomBound = 745;

    public static final int slotWidth = 270;

    private Random rand = new Random();

    public boolean EndGame = false;

    private int randomInt(int upperRange){
        //generate a random int with the given upper range
        //int randomNum = rand.nextInt((750 - 65 + 1)) - 65;
        int randomNum = rand.nextInt(upperRange);
        return randomNum;
    }

    //enumeration to correspond with FSM states
    public enum myDirection{UP, DOWN, LEFT, RIGHT, NO_MOVEMENT};

    public myDirection direction = myDirection.NO_MOVEMENT;     //initialize a direction to no movement
    private GameBlock newBlock;     //declare a GameBlock here (to be instantiated in constructor)

    //GameLoopTask constructor, take in activity (MainActivity), relative layout (of gameboard)
    //and application context
    GameLoopTask(Activity myActivity, RelativeLayout myRL, Context myContext){
        activity = myActivity;
        rl = myRL;
        context = myContext;

        createBlock();
    }

    private void createBlock(){
        //create 4x4 array for status of each grid
        boolean[][] gridStatus = new boolean[4][4];
        int numEmpty = 16;
        //int numOccumpied = 0;
        //checks whether is grid location is occupied or not
        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 4; y++){
                gridStatus[x][y] = isTargetOccupied(leftBound + x * slotWidth,
                                    topBound + y * slotWidth);
                if(gridStatus[x][y] == true){
                    //numOccumpied ++;
                    numEmpty--;
                }

            }
        }

        //generate a random int to iterate through the empty slots to determine where
        //to spawn a new block
        int generateInt;
        if(numEmpty > 0){
            generateInt = randomInt(numEmpty);
        }
        else{
            generateInt = 0;
        }
        int Xindex = 0;
        int Yindex = 0;
        //iteration through the status array
        //until randomly generated number is reached
        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 4; y++){
                if(gridStatus[x][y] == false){
                    if(generateInt == 0){
                        Xindex = x;
                        Yindex = y;
                    }
                    generateInt --;
                }

            }
        }

        //instantiate a gameblock
        initialX = leftBound + Xindex * slotWidth;
        initialY = topBound + Yindex * slotWidth;
        newBlock = new GameBlock(context, rl, this, initialX, initialY);
        myGBList.add(newBlock);
        //newBlock.setX(0);
        //newBlock.setX(0);
        //rl.addView(newBlock);   //add gameblock to gameboard's relative layout


    }

    public boolean isOccupied(int coordX, int coordY){
        //checks if a slot sis occupied
        int[] checkCoord = new int[2];

        for(GameBlock gb : myGBList){
            checkCoord = gb.getCoordinates();
            if(checkCoord[0] == coordX && checkCoord[1] == coordY){
                return true;
            }
        }

        return false;
    }

    public boolean checkNumber(int check){
        //checks if any of this number exist on the gameboard
        for(GameBlock gb: myGBList){
            if(gb.getBlockNum() == check){
                return true;
            }
        }
        return false;
    }


    public int getNumber(int coordX, int coordY){
        //gets the number of the gameblock at the specified coordinates
        int[] checkCoord = new int[2];

        for(GameBlock gb: myGBList){
            checkCoord = gb.getCoordinates();
            if(checkCoord[0] == coordX && checkCoord[1] == coordY){
                return gb.getBlockNum();
            }
        }
        return 0;
    }

    public boolean isTargetOccupied(int coordX, int coordY){
        //checks if target location has a gambelock in it
        int[] checkCoord = new int[2];

        for(GameBlock gb : myGBList){
            checkCoord = gb.getTargetCoordinates();
            if(checkCoord[0] == coordX && checkCoord[1] == coordY){
                return true;
            }
        }

        return false;
    }

    public void setDirection(myDirection inDirection){
        //take in input direction
        direction = inDirection;
        Log.d("FSM Direction ", "determined to be: " + direction.toString());

        //EndGame flag, if true do not execute any more commands
        if(!EndGame){
            //set GameBlock instance to inputted direction
            for(GameBlock gameBlock: myGBList){
                gameBlock.setBlockDirection(direction);
            }
            if (direction != myDirection.NO_MOVEMENT){
                createBlock();
            }
        }


    }
    public void run() {
        activity.runOnUiThread(
                new Runnable(){
                    public void run(){
                        //System.out.println("Hello");
                        //call move() method of GameBlock instance, running in Ui thread
                        //newBlock.move();
                        if(checkNumber(128)){   //once this number is reached, game is won, set message
                            EndGame = true;
                            TextView youWin = new TextView(context);
                            youWin.setX(leftBound + slotWidth - 30);
                            youWin.setY(topBound + slotWidth);
                            youWin.setText("You Win");
                            youWin.setTextSize(70.0f);
                            youWin.setTextColor(Color.GREEN);
                            rl.addView(youWin);
                            youWin.bringToFront();
                            Log.d("Game Finished", "you win");
                        }

                        //list to collect all the GameBlock references to be deleted
                        LinkedList<GameBlock> deletThis = new LinkedList<GameBlock>();

                        for(GameBlock gameBlock: myGBList){
                            gameBlock.move();

                        }

                        //once there are 16 blocks on the board, game over
                        if(myGBList.size() == 16){
                            EndGame = true;
                            TextView GameOver = new TextView(context);
                            GameOver.setX(leftBound + slotWidth); //set TextView coordinates to include proper offset
                            GameOver.setY(topBound + slotWidth);
                            GameOver.setText("Game\nOver");    //TextView displays the block number
                            GameOver.setTextSize(70.0f);    //visual stuff here
                            GameOver.setTextColor(Color.RED);
                            rl.addView(GameOver); //add textview to relative layout
                            GameOver.bringToFront();    //bring textview to front

                            Log.d("Game ended", "nominal conditions");

                        }

                        for(GameBlock gb1: myGBList){
                            //iterating through all the GameBlocks
                            //check the ones that are to be merged, i.e. destroyed
                            //add those to the deletThis list
                            //then go through array, once target coordinates reached, double the
                            //target block
                            int[] tempCoordinates = new int[2];
                            if(gb1.isToBeDestroyed() &&
                                    Arrays.equals(gb1.getCoordinates(), gb1.getTargetCoordinates())){
                                deletThis.add(gb1);
                                tempCoordinates = gb1.getTargetCoordinates();
                                for(GameBlock gb2: myGBList){
                                    if(Arrays.equals(gb2.getCoordinates(), tempCoordinates)){
                                        gb2.doubleBlockNum();
                                    }
                                }
                            }
                        }

                        //remove blocks that are to be merged
                        for(GameBlock gb: deletThis){
                            myGBList.remove(gb);
                            gb.destroyMe();
                        }

                    }
                }
        );
    }


}


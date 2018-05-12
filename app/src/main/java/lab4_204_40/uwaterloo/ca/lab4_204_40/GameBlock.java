package lab4_204_40.uwaterloo.ca.lab4_204_40;

    import android.content.Context;
    import android.graphics.Color;
    import android.util.Log;
    import android.widget.ImageView;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import java.util.Random;


public class GameBlock extends GameBlockTemplate {
    private final float IMAGE_SCALE = 0.7f;     //scale gameblock to fit board grid

    //initialize variables and constants here
    private int myCoordX;
    private int myCoordY;
    private int targetCoordX;
    private int targetCoordY;
    private int myVelocity;
    private final float GB_ACC = 10.0f;     //for animation purposes

    //enum of myDirection from GameLoopTask class, declared here
    private GameLoopTask.myDirection myDir;
    //coordinates of corner bounds
    private static final int leftX = -65;
    private static final int rightX = 745;
    private static final int topY = -65;
    private static final int bottomY = 745;

    private TextView myTV;      //TextView for displaying block number
    private int blockNum;
    private final int TV_OFFSET = 65;   //offset for layout purposes

    private GameLoopTask myGL;
    private RelativeLayout myRL;

    private Random myRandom;

    private boolean toBeDestroyed = false;

    //constructor for GameBlock, takes application context and coordinate values
    public GameBlock(Context myContext, RelativeLayout gbRL, GameLoopTask GL, int coordX, int coordY){
        super(myContext);   //complete ImageView constructor initialization from superclass
        this.setImageResource(R.drawable.gameblock);    //set image as gameblock.png
        this.setScaleX(IMAGE_SCALE);    //set scaling
        this.setScaleY(IMAGE_SCALE);
        myCoordX = coordX;  //set current and target coordinates to passed-in coordiantes
        myCoordY = coordY;
        targetCoordX = coordX;
        targetCoordY = coordY;
        //initial coordinates
        this.setX(myCoordX);   //set image coordinates
        this.setY(myCoordY);
        myVelocity = 0;     //for animation purposes, initialize to 0

        myRL = gbRL;
        myRL.addView(this); //add gameblock to relative layout
        this.bringToFront();    //bring image to front
        myGL = GL;  //initialize gameblock's GameLoopTask to passed in GameLoopTask

        myRandom = new Random();    //initialize Random class
        blockNum = (myRandom.nextInt(2) + 1) * 2;   //generate random number for new block

        myTV = new TextView(myContext); //initialize new TextView for the new GameBlock
        myTV.setX(myCoordX + TV_OFFSET);    //set TextView coordinates to include proper offset
        myTV.setY(myCoordY + TV_OFFSET);
        myTV.setText(String.format("%d", blockNum));    //TextView displays the block number
        myTV.setTextSize(50.0f);    //visual stuff here
        myTV.setTextColor(Color.BLACK);
        myRL.addView(myTV); //add textview to relative layout
        myTV.bringToFront();    //bring textview to front

        myDir = GameLoopTask.myDirection.NO_MOVEMENT;   //initialize myDir to no movement as per FSM

    }

    public int calculateMergeOffset(int[]slotNums, int numOccupants){
        //based on values in the slots and the number of occupoants, determine what
        //to offset the movement by in terms of indices
        switch(numOccupants){
            case 0:
                //no obstructions, no merging
                //targetCoordX = leftX + numOccupants * GameLoopTask.slotWidth;
                return 0;
                //break;

            case 1:
                //if there is one obstruction, merge if its blockNum is the same as yours
                if(slotNums[0] == getBlockNum()){
                    //targetCoordX = leftX + (numOccupants  - 1)* GameLoopTask.slotWidth;
                    toBeDestroyed = true;
                    return 1;

                }
                else{
                    return 0;
                    //targetCoordX = leftX + numOccupants * GameLoopTask.slotWidth;
                }

                //break;

            case 2:
                //if there are two blocks in front of the current block
                if(slotNums[0] == slotNums[1]){
                    //if the first two blocks equal, they merge, so increase offset by 1
                    return 1;
                    //targetCoordX = leftX + (numBlocks-1) * GameLoopTask.slotWidth;
                }
                else if(slotNums[1] == getBlockNum()){
                    //if second block equals current block, merge so increase offset by 1
                    toBeDestroyed = true;
                    return 1;
                    //targetCoordX = leftX + (numBlocks -1) * GameLoopTask.slotWidth;

                }else{
                    return 0;
                    //targetCoordX = leftX + numBlocks * GameLoopTask.slotWidth;
                }
                //break;

            case 3:
                //if there are three blocks in front of current block
                if(slotNums[0] == slotNums [1]){
                    if(slotNums[2] == getBlockNum()){
                        //if first two same and third and you are same, merge and
                        // 2 offsets needed
                        toBeDestroyed = true;
                        return 2;
                        //targetCoordX = leftX + (numBlocks - 2) * GameLoopTask.slotWidth;
                    }
                    else{
                        //if only front two same, offset of 1
                        return 1;
                        //targetCoordX = leftX + (numBlocks - 1) * GameLoopTask.slotWidth;
                    }
                }
                else if(slotNums[1] == slotNums[2]){
                    //for if second and third have same number, increase offset by 1
                    return 1;
                    //targetCoordX = leftX + (numBlocks - 1) * GameLoopTask.slotWidth;
                }else if(slotNums[2] == getBlockNum()){
                    //if just third and current blocks are same, offset by 1
                    toBeDestroyed = true;
                    return 1;
                }
                else{
                    return 0;
                    //targetCoordX = leftX + numBlocks * GameLoopTask.slotWidth;
                }
                //break;

            default:
                break;
        }
        return 0;
    }

    public boolean isToBeDestroyed(){
        return toBeDestroyed;
    }

    public void destroyMe(){
        //when called, removes the GameBlock's number(TextView) and image (this)
        myRL.removeView(myTV);
        myRL.removeView(this);
    }

    public void setBlockDirection(GameLoopTask.myDirection newDir){
        myDir = newDir;     //take in a new direction, set block direction state to that
        Log.d("Direction ", "is :" + myDir);

        int testCoord;
        int numBlocks;

        int count;
        //int emptySlots = 0;

        int mergeOffset = 0;

        int[] slotNums = new int[4];

        switch(myDir){
            //check for each possible direction
            //using slotNums, record all the block numbers in nonvacant slots
            //then analyze data with calculateMergeOffset to determine how much more movement
            //the block needs
            //set target coordinates for the block
            case LEFT:
                testCoord = leftX;
                numBlocks = 0;
                //count = 0;

                while (testCoord != myCoordX){
                    if(myGL.isOccupied(testCoord, myCoordY)){

                        slotNums[numBlocks] = myGL.getNumber(testCoord, myCoordY);
                        numBlocks++;
                    }
                    //slotNums[count] = myGL.getNumber(testCoord, myCoordY);
                    //count++;
                    testCoord += GameLoopTask.slotWidth;
                }
                targetCoordX = leftX +
                        (numBlocks - calculateMergeOffset(slotNums, numBlocks))* GameLoopTask.slotWidth;


                /*int isMerge = 0;
                int otherMerge = 0;
                int maxIndex = count;
                int maxIn = count;
                if (count == 0){
                    targetCoordX = leftX + numBlocks * GameLoopTask.slotWidth;
                }else {
                    while (count > 0) {
                        if (slotNums[count - 1] == getBlockNum()) {
                            isMerge = 1;
                            //targetCoordX = leftX + (numBlocks - 1) * GameLoopTask.slotWidth;
                            break;
                        } else if (count == 1 || slotNums[count - 1] != 0) {
                            //targetCoordX = leftX + numBlocks * GameLoopTask.slotWidth;
                            break;
                        }

                        count--;
                    }
                    while(maxIndex > 1){
                        for(int i = 1; i < maxIndex; i++){
                            if(slotNums[maxIndex - 1] == slotNums[maxIndex - i]
                                    && slotNums[maxIndex - 1] != 0){
                                otherMerge = 1;
                                if(isMerge == 1){
                                    if(slotNums[maxIndex - i] == getBlockNum() && maxIn != 3){
                                        isMerge = 0;
                                    }
                                }
                            }
                        }
                        maxIndex--;
                    }
                    targetCoordX = leftX + (numBlocks - isMerge - otherMerge) * GameLoopTask.slotWidth;

                }
                */
                break;

            case RIGHT:
                testCoord = rightX;
                numBlocks = 0;
                count = 0;
                while (testCoord != myCoordX){
                    if(myGL.isOccupied(testCoord, myCoordY)){
                        numBlocks++;
                    }
                    slotNums[count] = myGL.getNumber(testCoord, myCoordY);
                    count++;
                    testCoord -= GameLoopTask.slotWidth;
                }
                targetCoordX = rightX -
                        (numBlocks - calculateMergeOffset(slotNums, numBlocks)) * GameLoopTask.slotWidth;

                /*
                if (count == 0){
                    targetCoordX = rightX - numBlocks * GameLoopTask.slotWidth;
                }else{
                    while(count > 0){
                        if(slotNums[count - 1] == getBlockNum()){
                            targetCoordX = rightX - (numBlocks - 1) * GameLoopTask.slotWidth;
                            break;
                        }else if(count == 1 || slotNums[count - 1] != 0){
                            targetCoordX = rightX - numBlocks * GameLoopTask.slotWidth;
                            break;
                        }
                        count--;
                    }
                }*/

                break;

            case UP:
                testCoord = topY;
                numBlocks = 0;
                count = 0;
                while (testCoord != myCoordY){
                    if(myGL.isOccupied(myCoordX, testCoord)){
                        numBlocks++;
                    }
                    slotNums[count] = myGL.getNumber(myCoordX, testCoord);
                    count++;
                    testCoord += GameLoopTask.slotWidth;
                }
                targetCoordY = topY +
                        (numBlocks - calculateMergeOffset(slotNums, numBlocks)) * GameLoopTask.slotWidth;

                /*if (count == 0){
                    targetCoordY = topY + numBlocks * GameLoopTask.slotWidth;
                }else{
                    while(count > 0){
                        if(slotNums[count - 1] == getBlockNum()){
                            targetCoordY = topY + (numBlocks - 1) * GameLoopTask.slotWidth;
                            break;
                        }else if(count == 1 || slotNums[count - 1] != 0){
                            targetCoordY = topY + numBlocks * GameLoopTask.slotWidth;
                            break;
                        }
                        count--;
                    }
                }*/


                break;

            case DOWN:
                testCoord = bottomY;
                numBlocks = 0;
                count = 0;
                while (testCoord != myCoordY){
                    if(myGL.isOccupied(myCoordX, testCoord)){
                        numBlocks++;
                    }
                    slotNums[count] = myGL.getNumber(myCoordX, testCoord);
                    count++;
                    testCoord -= GameLoopTask.slotWidth;
                }
                targetCoordY = bottomY -
                        (numBlocks - calculateMergeOffset(slotNums, numBlocks)) * GameLoopTask.slotWidth;

                /*if (count == 0){
                    targetCoordY = bottomY - numBlocks * GameLoopTask.slotWidth;
                }else{
                    while(count > 0){
                        if(slotNums[count - 1] == getBlockNum()){
                            targetCoordY = bottomY - (numBlocks - 1) * GameLoopTask.slotWidth;
                            break;
                        }else if(count == 1 || slotNums[count - 1] != 0){
                            targetCoordY = bottomY - numBlocks * GameLoopTask.slotWidth;
                            break;
                        }
                        count--;
                    }
                }*/


                break;

            default:
                break;

        }
    }

    public int getBlockNum(){
        return blockNum;
    }

    public void doubleBlockNum(){

        //double the block number when merging
        //also need to updated the TextView
        blockNum *= 2;
        myTV.setText(String.format("%d", blockNum));
        this.bringToFront();
        myTV.bringToFront();
    }

    //for blocks to report its own location
    public int[] getCoordinates(){
        int[] thisCoord = new int[2];
        thisCoord[0] = myCoordX;
        thisCoord[1] = myCoordY;

        //thisCoord[0] = targetCoordX;
        //thisCoord[1] = targetCoordY;
        return thisCoord;
    }

    public int[] getTargetCoordinates(){
        int[] targetCoord = new int[2];

        targetCoord[0] = targetCoordX;
        targetCoord[1] = targetCoordY;
        return targetCoord;
    }


    public void move(){
        //when function called, take in myDir direction for cases
        switch(myDir){
            //for each direction, absolute value of myVelocity increasing by rate of GB_ACC per
            //tick squared
            //at end of movement, myVelocity set to zero
            //constantly check to make sure block does not exceed set coordinate bounds

            //when direction is left, move until block reaches leftX bound
            case LEFT:
                //targetCoordX = leftX;

                if(myCoordX > targetCoordX){
                    if((myCoordX - myVelocity) <= targetCoordX){
                        myCoordX = targetCoordX;
                        myVelocity = 0;
                    }
                    else {
                        myCoordX -= myVelocity;
                        myVelocity += GB_ACC;
                    }
                }
                break;

            //when direction is right, move until block reaches rightX bound
            case RIGHT:
                //targetCoordX = rightX;

                if(myCoordX < targetCoordX){
                    if((myCoordX + myVelocity) >= targetCoordX){
                        myCoordX = targetCoordX;
                        myVelocity = 0;
                    }
                    else {
                        myCoordX += myVelocity;
                        myVelocity += GB_ACC;
                    }
                }

                break;

            //when direction is up, move until block reaches topY bound
            case UP:
                //targetCoordY = topY;

                if(myCoordY > targetCoordY){
                    if((myCoordY - myVelocity) <= targetCoordY){
                        myCoordY = targetCoordY;
                        myVelocity = 0;
                    }
                    else {
                        myCoordY -= myVelocity;
                        myVelocity += GB_ACC;
                    }
                }

                break;

            //when direction is down, move until block reaches bottomY bound
            case DOWN:
                //targetCoordY = bottomY;

                if(myCoordY < targetCoordY){
                    if((myCoordY + myVelocity) >= targetCoordY){
                        myCoordY = targetCoordY;
                        myVelocity = 0;
                    }
                    else {
                        myCoordY += myVelocity;
                        myVelocity += GB_ACC;
                    }
                }
                break;

            default:
                //default no movement
                break;
        }

        if(!myGL.EndGame){
            //at end of each cycle, update coordinates of gameblock.png image
            this.setX(myCoordX);
            this.setY(myCoordY);

            myTV.setX(myCoordX + TV_OFFSET);
            myTV.setY(myCoordY + TV_OFFSET);
            myTV.bringToFront();
        }


        if(myVelocity == 0){
            //at end of movement cycle, when velocity becomes zero, set myDir direction
            //to no movement
            myDir = GameLoopTask.myDirection.NO_MOVEMENT;
        }

    }
}

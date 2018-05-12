package lab4_204_40.uwaterloo.ca.lab4_204_40;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by owenc on 2017-03-23.
 */

public abstract class GameBlockTemplate extends ImageView {
    public GameBlockTemplate(Context context){
        super(context);
    }

    public abstract void setBlockDirection(GameLoopTask.myDirection newDir);

    public abstract void move();
}

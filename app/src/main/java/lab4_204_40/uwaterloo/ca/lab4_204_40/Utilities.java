package lab4_204_40.uwaterloo.ca.lab4_204_40;

    import android.util.Log;

    import java.io.File;
    import java.io.IOException;
    import java.io.PrintWriter;

public final class Utilities {
    //Writes a x by 3 float array to a .csv file
    public static void writeXBy3FloatArrayToFile(String fileName, File fileLocation, int x, float data[][]){

        //initialize file and printWriter object
        File file = null;
        PrintWriter printWriter = null;

        try{

            //The default External File Directory is
            // Android/data/ANDROID_PACKAGE_NAME/Lecture 4 Demo
            // If the directory does not exist it will be created for you
            file = new File(fileLocation, fileName);
            printWriter = new PrintWriter(file);

            for(int i = 0; i < x; i++){
                printWriter.println(String.format("%f,%f,%f", data[i][0], data[i][1], data[i][2]));
            }
        }
        catch(IOException e){

            //Print error message in Log
            Log.d(fileName, "File Write Fail: " + e.toString());
        }
        finally{

            //Added the null pointer check to prevent further NPE.
            if(printWriter != null){
                printWriter.flush();
                printWriter.close();
            }

            //Write to log so we know when file write is finished
            Log.d(fileName, "File Write Ended.");
        }
    }
}



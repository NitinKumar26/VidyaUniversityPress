package in.edu.vidya.vup.helper;

import android.os.Environment;

public class CheckForSDCard  {

    // Method to check if the SD card is mounted or not
    public static boolean isSDCardPresent(){
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED
        );
    }
}

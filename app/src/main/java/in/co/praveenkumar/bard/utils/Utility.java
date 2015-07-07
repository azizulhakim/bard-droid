package in.co.praveenkumar.bard.utils;

/**
 * Created by mhaki005 on 7/6/15.
 */
public class Utility {
    public static void sleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

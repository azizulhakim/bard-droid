package in.co.praveenkumar.bard.utils;

/**
 * Created by mhaki005 on 5/13/15.
 */
public class RLE {
    public static byte[] decode(byte[] data, int pos){
        byte[] ret = new byte[Globals.DATA_SIZE];

        int j = 0;
        for (int i=pos; i<data.length; i+=3){
            int count = data[i+2];
            while(count > 0){
                ret[j++] = data[i];
                ret[j++] = data[i+1];
                count--;
            }
        }

        return ret;
    }
}

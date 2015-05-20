package in.co.praveenkumar.bard.utils;

/**
 * Created by Azizul Hakim on 5/13/15.
 */
public class RLE {
    public static int encode(byte in[], int pos, int len, byte out[]){
        int outlen = 0;
        int i = 0, j = 0;
        int outi = 0;
        int count = 0;

        if (len % 2 != 0){
            System.out.println("Length should be multiple of 2\n");
            return 0;
        }

        i = pos;
        while(i < len){
            j = i;
            count = 0;
            while (j + 1 < len && in[i] == in[j] && in[i+1] == in[j+1]){
                count++;
                j+=2;
            }

            while (count > 255){
                out[outi++] = in[i];
                out[outi++] = in[i+1];
                out[outi++] = (byte) 255;
                count -= 255;
                i += 2*255;
            }
            out[outi++] = in[i];
            out[outi++] = in[i+1];
            out[outi++] = (byte) count;
            i += 2*count;
        }
        //printk("i = %d, outi = %d, j = %d\n", i, outi, j);
        outlen = outi;

        return outlen;
    }


    public static byte[] decode(byte[] data, int pos, int end){
        byte[] ret = new byte[Globals.DATA_SIZE];

        int j = 0;
        for (int i=pos; i<end; i+=3){
            int count = (int)data[i+2] & 0xff;
            while(count > 0){
                ret[j++] = data[i];
                ret[j++] = data[i+1];
                count--;
            }
        }

        return ret;
    }
}

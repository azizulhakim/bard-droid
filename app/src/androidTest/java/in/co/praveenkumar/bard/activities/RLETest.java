package in.co.praveenkumar.bard.activities;

import android.test.InstrumentationTestCase;

import in.co.praveenkumar.bard.utils.RLE;

/**
 * Created by Azizul Hakim on 5/13/15.
 */
public class RLETest extends InstrumentationTestCase {

    public void testEncode() throws Exception{
        byte[] in = new byte[4096];
        byte[] rleBuffer = new byte[27];

        for (int i=0; i<4096; i+=2){
            in[i] = (byte) 0xE6;
            in[i+1] = 0x37;
        }

        for (int i=0; i<27; i+=3){
            rleBuffer[i] = (byte) 0xE6;
            rleBuffer[i+1] = 0x37;
            rleBuffer[i+2] = (byte) 255;
        }
        rleBuffer[26] = 8;

        byte[] out = new byte[4096*3];

        int rleLen = RLE.encode(in, 0, 4096, out);
        assertEquals(27, rleLen);

        for (int i=0; i<27; i+=3){
            assertEquals(rleBuffer[i], out[i]);
            assertEquals(rleBuffer[i+1], out[i+1]);
            assertEquals(rleBuffer[i+2], out[i+2]);
        }
    }

    public void testDecode() throws Exception {
        byte[] in = new byte[4096];
        byte[] rleBuffer = new byte[27];

        for (int i=0; i<4096; i+=2){
            in[i] = (byte) 0xE6;
            in[i+1] = 0x37;
        }

        for (int i=0; i<27; i+=3){
            rleBuffer[i] = (byte) 0xE6;
            rleBuffer[i+1] = 0x37;
            rleBuffer[i+2] = (byte) 255;
        }
        rleBuffer[26] = 8;

        byte[] originalData = RLE.decode(rleBuffer, 0, rleBuffer.length);
        for (int i=0; i<4096; i++)
            assertEquals(originalData[i], in[i]);

        /*byte[] rle = {0,0,2};
        byte[] original = {0,0,0,0};

        byte[] decodedRle = RLE.decode(rle, 0, rle.length);

        //assertEquals(original.length, decodedRle.length);
        for (int i=0; i <original.length; i++)
            assertEquals(original[i], decodedRle[i]);

        byte[] original1 = {0,0,0,0,1,0};
        byte[] rle1 = {0,0,2,1,0,1};

        decodedRle = RLE.decode(rle1, 0, rle.length);

        //assertEquals(original1.length, decodedRle.length);
        for (int i=0; i <original1.length; i++)
            assertEquals(original1[i], decodedRle[i]);

        byte[] rleWithHeader = {1,0,0,0,0,0,2,1,1,1};
        byte[] original2 = {0,0,0,0,1,1};

        decodedRle = RLE.decode(rleWithHeader, 4, rle.length);

        //assertEquals(original2.length, decodedRle.length);
        for (int i=0; i <original2.length; i++)
            assertEquals(original2[i], decodedRle[i]);

        //final int expected = 5;
        //final int reality = 5;
        //assertEquals(expected, reality);
        */
    }
}

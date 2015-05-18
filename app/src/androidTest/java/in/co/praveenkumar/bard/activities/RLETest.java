package in.co.praveenkumar.bard.activities;

import android.test.InstrumentationTestCase;

import in.co.praveenkumar.bard.utils.RLE;

/**
 * Created by mhaki005 on 5/13/15.
 */
public class RLETest extends InstrumentationTestCase {

    public void testDecode() throws Exception {
        byte[] rle = {0,0,2};
        byte[] original = {0,0,0,0};

        byte[] decodedRle = RLE.decode(rle, 0);

        //assertEquals(original.length, decodedRle.length);
        for (int i=0; i <original.length; i++)
            assertEquals(original[i], decodedRle[i]);

        byte[] original1 = {0,0,0,0,1,0};
        byte[] rle1 = {0,0,2,1,0,1};

        decodedRle = RLE.decode(rle1, 0);

        //assertEquals(original1.length, decodedRle.length);
        for (int i=0; i <original1.length; i++)
            assertEquals(original1[i], decodedRle[i]);

        byte[] rleWithHeader = {1,0,0,0,0,0,2,1,1,1};
        byte[] original2 = {0,0,0,0,1,1};

        decodedRle = RLE.decode(rleWithHeader, 4);

        //assertEquals(original2.length, decodedRle.length);
        for (int i=0; i <original2.length; i++)
            assertEquals(original2[i], decodedRle[i]);

        //final int expected = 5;
        //final int reality = 5;
        //assertEquals(expected, reality);
    }
}

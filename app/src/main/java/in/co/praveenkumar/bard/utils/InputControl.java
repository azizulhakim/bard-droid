package in.co.praveenkumar.bard.utils;

/**
 * Created by Azizul Hakim on 5/30/15.
 * azizulfahim2002@gmail.com
 */
public class InputControl {
    public static final int EVENT_TYPE_INDEX = 0;
    public static final int MOUSE_BTN_INDEX = 1;
    public static final int REL_X_INDEX = 2;
    public static final int REL_Y_INDEX = 3;
    public static final int REL_WHEEL_INDEX = 4;

    public static final int METAKEY_INDEX = 5;
    public static final int KEY_INDEX = 7;

    public static final int BTN_LEFT = 1;
    public static final int BTN_RIGHT = 2;

    public static final int SHIFT = 1;
    public static final int ALT = 2;
    public static final int CTRL = 4;
    public static final int DEL = 8;
    public static final int TAB = 16;

    public static final int [][]ShiftKeyMappedKey = {
            {'!','1'},
            {'@', '2'},
            {'#', '3'},
            {'$', '4'},
            {'%', '5'},
            {'^', '6'},
            {'&', '7'},
            {'*', '8'},
            {'(', '9'},
            {')', '0'},
            {'_', '-'},
            {'+', '='},
            {'~','`'},
            {'{', '['},
            {'}', ']'},
            {':', ';'},
            {'"', '\''},
            {'|', '\\'},
            {'<', ','},
            {'>', '.'},
            {'?', '/'}
    };

    public static boolean isShiftKeyMappedKey(int unicodeChar){
        for (int i=0; i<ShiftKeyMappedKey.length; i++){
            if (ShiftKeyMappedKey[i][0] == unicodeChar)
                return true;
        }

        return false;
    }

    public static int getShiftKeyMappedKey(int unicodeChar){
        for (int i=0; i<ShiftKeyMappedKey.length; i++){
            if (ShiftKeyMappedKey[i][0] == unicodeChar)
                return ShiftKeyMappedKey[i][1];
        }

        return 0;
    }
}

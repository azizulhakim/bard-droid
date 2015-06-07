package in.co.praveenkumar.bard.io;

import java.io.IOException;

import in.co.praveenkumar.bard.graphics.FrameSettings;
import in.co.praveenkumar.bard.utils.Globals;

/**
 * Created by Azizul Hakim on 5/20/15.
 */
public class DummyInputStream implements IUsbInputStream {
    private byte[] data = null;
    private int pos = 0;
    private enum type {HEADER, DATA};

    private type flag = type.HEADER;
    private int framePos = 0;
    private int animPos = 0;

    public DummyInputStream(){
        data = new byte[4096];

        data[0] = (byte)Globals.DATA_VIDEO;
        data[1] = (byte)1;

        for (int i=0; i<4096; i+=2){
            data[i] = (byte)0xe6;
            data[i+1] = 0x37;
        }
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (Globals.RLE){
            return 0;

        }
        else{
            for (int i=0; i<buffer.length; i+=2){
                buffer[i] = (byte)0xe6;
                buffer[i+1] = (byte)0x37;
            }
            buffer[0] = (byte)Globals.DATA_VIDEO;
            buffer[1] = (byte)framePos;
            buffer[2] = (byte)(framePos >> 8);


            if (framePos >= FrameSettings.HEIGHT / 4 - 10 && framePos <= FrameSettings.HEIGHT/4 + 10){
                for (int i=0; i<40; i++){
                    buffer[animPos + i] = 0;
                    buffer[FrameSettings.WIDTH / 2 + animPos + i] = 0;
                    buffer[FrameSettings.WIDTH + animPos + i] = 0;
                }

                animPos = (animPos + 1) % (FrameSettings.WIDTH);
            }

            framePos = (framePos + 1) % (FrameSettings.HEIGHT / 2);

            try {
                Thread.sleep(0, 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return buffer.length;
        }
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (flag == type.HEADER){
            buffer[0] = (byte)Globals.DATA_VIDEO;
            buffer[1] = (byte)1;
            buffer[2] = (byte)framePos;
            buffer[3] = (byte)(framePos >> 8);
            buffer[4] = (byte)(27);
            buffer[5] = (byte)(27 >> 8);

            for (int i=0; i<27; i+=3){
                buffer[i+6] = (byte)0xe6;
                buffer[i+7] = (byte)0x37;
                buffer[i+8] = (byte)0xff;
            }
            buffer[6+26] = (byte)0x08;

            framePos++;
            framePos %= 393;

            //flag = type.DATA;
        }

        return 33;
    }
}

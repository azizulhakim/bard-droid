package in.co.praveenkumar.bard.io;

import java.io.IOException;

/**
 * Created by Azizul Hakim on 5/20/15.
 * azizulfahim2002@gmail.com
 */
public interface IUsbInputStream {
    public int read() throws IOException;
    public int read(byte[] buffer) throws IOException;
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException;
}

package in.co.praveenkumar.bard.io;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Azizul Hakim on 5/20/15.
 * azizulfahim2002@gmail.com
 */
public class UsbInputStream implements IUsbInputStream{
    private FileInputStream inputStream;

    public UsbInputStream(FileDescriptor fd){
        inputStream = new FileInputStream(fd);
    }

    @Override
    public int read() throws IOException {
        int i = inputStream.read();

        return i;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        int i = inputStream.read(buffer);

        return i;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int i = inputStream.read(buffer, byteOffset, byteCount);

        return i;
    }
}

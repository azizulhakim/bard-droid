package in.co.praveenkumar.bard.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Azizul Hakim on 5/19/15.
 */
public class DebugDump {
    private File actualBufferFile = null;
    private File actualPayloadFile = null;
    private File rleDecodedPayloadFile = null;

    private FileOutputStream actualBufferFOut = null;
    private OutputStreamWriter actualBufferOutWriter = null;

    private FileOutputStream actualPayloadFOut = null;
    private OutputStreamWriter actualPayloadOutWriter = null;

    private FileOutputStream rleDecodedPayloadFOut = null;
    private OutputStreamWriter rleDecodedPayloadOutWriter = null;

    public void init(){
        try {
            actualBufferFile = new File("/sdcard/capturedBuffer.txt");
            actualPayloadFile = new File("/sdcard/actualPayload.txt");
            rleDecodedPayloadFile = new File("/sdcard/rleDecodedPayload.txt");

            if (!actualBufferFile.exists())
                actualBufferFile.createNewFile();

            if (!actualPayloadFile.exists())
                actualPayloadFile.createNewFile();

            if (!rleDecodedPayloadFile.exists())
                rleDecodedPayloadFile.createNewFile();

            actualBufferFOut = new FileOutputStream(actualBufferFile, true);
            actualBufferOutWriter = new OutputStreamWriter(actualBufferFOut);

            actualPayloadFOut = new FileOutputStream(actualPayloadFile, true);
            actualPayloadOutWriter = new OutputStreamWriter(actualPayloadFOut);

            rleDecodedPayloadFOut = new FileOutputStream(rleDecodedPayloadFile, true);
            rleDecodedPayloadOutWriter = new OutputStreamWriter(rleDecodedPayloadFOut);
        }
        catch (Exception ex){

        }
    }

    public void dumpActualBuffer(byte[] actualBuffer){
        try {
            for (int i = 0; i < actualBuffer.length; i++) {
                actualBufferOutWriter.write("" + actualBuffer[i]);
                actualBufferOutWriter.write("   ");
            }
            actualBufferOutWriter.write("\n");
        }
        catch (Exception ex){

        }
    }

    public void dumpActualPayload(byte[] actualPayload){
        try {
            for (int i = 0; i < actualPayload.length; i++) {
                actualPayloadOutWriter.write("" + actualPayload[i]);
                actualPayloadOutWriter.write("  ");
            }
            actualPayloadOutWriter.write("\n");
        }
        catch (Exception ex){

        }
    }

    public void dumpRleDecodedPayload(byte[] rleDecodedPayload){
        try {
            for (int i = 0; i < rleDecodedPayload.length; i++) {
                rleDecodedPayloadOutWriter.write("" + rleDecodedPayload[i]);
                rleDecodedPayloadOutWriter.write("  ");
            }
            rleDecodedPayloadOutWriter.write("\n");
        }
        catch (Exception ex){

        }
    }

    public void close(){
        try{
            actualBufferOutWriter.close();
            actualBufferFOut.close();

            actualPayloadOutWriter.close();
            actualPayloadFOut.close();

            rleDecodedPayloadOutWriter.close();
            rleDecodedPayloadFOut.close();
        }
        catch (Exception ex){

        }
    }
}

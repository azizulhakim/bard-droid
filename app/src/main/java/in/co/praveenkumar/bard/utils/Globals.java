package in.co.praveenkumar.bard.utils;

public class Globals {
	public static int DATA_SIZE = 4096;
	public static int AUDIO_BUFFER_SIZE = 4096 * 4;
	public static int DATA_HEADER_SIZE = 4;
	public static int DATA_PACKET_SIZE = 4100;
	public static int DATA_AUDIO = 1;
	public static int DATA_VIDEO = 2;

	public static final boolean RLE = false;

	public static final byte CONTROL_HEADER = 0x03;
	public static final byte DROP_FRAME = 0x02;

	public static final boolean DEBUG = false;
}

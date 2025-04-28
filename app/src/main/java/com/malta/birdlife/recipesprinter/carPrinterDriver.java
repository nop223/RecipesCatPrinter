package com.malta.birdlife.recipesprinter;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class carPrinterDriver {
    public int RetractPaper = 0xA0;     // Data: Number of steps to go back
    public int FeedPaper = 0xA1;        // Data: Number of steps to go forward
    public int DrawBitmap = 0xA2;       // Data: Line to draw. 0 bit -> don't draw pixel, 1 bit -> draw pixel
    public int DrawingMode = 0xBE;      // Data: 1 for Text, 0 for Images
    public int SetEnergy = 0xAF;        // Data: 1 - 0xFFFF
    public int SetQuality = 0xA4;       // Data: 1 - 5

    String PrinterAddress = "93:2A:BB:C4:95:8D";
    String PrinterCharacteristic = "0000AE01-0000-1000-8000-00805F9B34FB";

    public static int[] concat(int[] a, int[] b) {
        int lenA = a.length;
        int lenB = b.length;
        int[] c = Arrays.copyOf(a, lenA + lenB);
        System.arraycopy(b, 0, c, lenA, lenB);
        return c;
    }
    public byte[] formatMessage(int command, int[] data){
        catPrinterCRC crcCalc = new catPrinterCRC();
        int[] dataOut = concat(new int[]{0x51, 0x78, command, 0x00, (int) (data.length & 0xFF), 0x00}, data );
        dataOut = concat(new int[]{crcCalc.crc8(data), 0xFF},dataOut);
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataOut.length*4 );
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(dataOut);
        return byteBuffer.array();
    }

}

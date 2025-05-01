package com.malta.birdlife.recipesprinter;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.nio.ByteBuffer;
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
        int[] dataOut = concat(new int[]{0x51, 0x78, (byte)(command&0xff), 0x00, (int) (data.length & 0xFF), 0x00}, data );
        dataOut = concat(dataOut,new int[]{crcCalc.crc8(data), 0xFF});
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataOut.length );
        for(int inData : dataOut){
            byteBuffer.put((byte)inData);
        }
        return byteBuffer.array();
    }

    public byte[] printText(String msg){
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAntiAlias(true);
        paint.setTextSize(40);
        paint.setColor(Color.BLACK);

        canvas.drawText(msg, 0, 90, paint) ;
        canvas.save();
        //canvas.restore();

        ByteBuffer byteBuffer =  ByteBuffer.allocate(bitmap.getHeight()*(bitmap.getWidth() / 8));
        int[] pix = new int[bitmap.getWidth()*bitmap.getHeight()];
        for (int y=0;y<bitmap.getHeight();y++){
            for(int x=0;x<bitmap.getWidth();x=x+8){
                byte pixelData = 0;
                for(int index=0;index<8;index++){
                    int pixel = bitmap.getPixel(x+index,y);
                    pix[y*bitmap.getWidth()+(x+index)] = bitmap.getPixel(x+index,y);
                    if(pixel!=-1){
                        pixelData = (byte) (pixelData + (1 << index));
                    }
                }
                byteBuffer.put(pixelData);
            }
        }
        return byteBuffer.array();
    }

}

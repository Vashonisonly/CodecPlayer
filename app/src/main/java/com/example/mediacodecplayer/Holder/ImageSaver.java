package com.example.mediacodecplayer.Holder;

import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;

public class ImageSaver implements Runnable {
    private final String TAG = "ImageReader";
    private final String  fileName = "tempImage.jpg";
    private File file;
    private Image image;

    public ImageSaver(Image image, File file){
        this.image = image;
        this.file = file;
    }
    @Override
    public void run(){
        savePicture();
    }
    private void savePicture() {
        // 创建File对象，用于存储拍照后的图片
        File outputImageFile = new File(file, String.valueOf(System.currentTimeMillis())+".jpg");
        try {
            if (outputImageFile.exists()) {
                outputImageFile.delete();
            }
            outputImageFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG,"savePicture: create file fail: "+e.getMessage());
        }
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(outputImageFile);
            fos.write(data, 0 ,data.length);
        }catch (IOException e){
            Log.d(TAG,"write image fail: "+ e.getMessage());
        }finally {
            outputImageFile = null;
            if(fos != null){
                try{
                    fos.close();
                    fos = null;
                }catch (IOException e){
                    Log.d(TAG,"close file fail: "+ e.getMessage());
                }
            }
        }
    }
}

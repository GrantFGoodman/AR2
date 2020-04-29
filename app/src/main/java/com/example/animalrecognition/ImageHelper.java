package com.example.animalrecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;

//this function helps decrease image size for better performance
public class ImageHelper {

    public static int calculateInsampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height =options.outHeight;
        final int width =options.outWidth;
        int inSampleSize =1;

        if(height>reqHeight || width>reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            while((halfHeight/inSampleSize)>= reqHeight &&(halfWidth/inSampleSize)>=reqWidth){
                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String pathName, int reqWidth , int reqHeight){
        final BitmapFactory.Options options =new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(pathName,options);

        options.inSampleSize =calculateInsampleSize(options,reqWidth,reqHeight);

        options.inJustDecodeBounds =false;
        return BitmapFactory.decodeFile(pathName,options);
    }


}

package com.example.moodcolouring2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

//https://developer.android.com/topic/performance/graphics/load-bitmap
public class ImageProcessingService extends Service {

    public static final String CHANNEL_ID = "DownloadChannel";
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "COMP3018";
    private boolean isImageProcessing = false;
    private final IBinder binder = new LocalBinder();
    private DownloadCallback callback;

    @Override
    public void onCreate(){
        Log.d(TAG, "On Create Service");
        super.onCreate();
        createNotificationChannel();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.d(TAG, "onStartCommand Service");

        int[] responseCounts = intent.getIntArrayExtra("responseCounts");//The response for each emotion
        int totalResponses = intent.getIntExtra("totalResponses", 0);//The total responses
        int inputImage = intent.getIntExtra("inputImage", 0);//Input image

        if(isImageProcessing){
            Log.d(TAG, "Already Processing Image");
            return START_NOT_STICKY;
        }
        Notification notification = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setContentTitle("Image Processing Service")
                .setContentText("Image Currently Being Processed")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        Log.d(TAG, "Start Foreground Service");
        startForeground(NOTIFICATION_ID, notification);

        new Thread(()->{
            isImageProcessing = true;
            Log.d(TAG, "Image Processing");
            Bitmap processedImage = createImage(responseCounts, totalResponses, inputImage);
            isImageProcessing = false;
            stopForeground(true);
            Log.d(TAG, "Image has ben processed");
            if (callback != null) {
                callback.onImageProcessed(processedImage); // Invoke the callback with the processed image
            }
            stopSelf();
        }).start();


        return START_STICKY;

    }





    private void createNotificationChannel(){
        //Simple Notification Channel creation
        Log.d(TAG, "CreateNotificationChannel Called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Image Processing Service";
            String description = "Used for Image Processing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name
                    , importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(
                    NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "CreateNotificationChannel Complete");
        }
    }

    public interface DownloadCallback{
        void onImageProcessed(Bitmap bitmap);

    }

    public void setCallback(DownloadCallback callback){
        this.callback = callback;
    }


    public class LocalBinder extends Binder {
        ImageProcessingService getService(){
            return ImageProcessingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    //Need to rework this more for efficiency
    private Bitmap createImage(int[] responses, int totalResponses, int image) {

        //Scales the bitmap down to be within the size and height
        Bitmap originalBitmap = getScaledBitmap(image, 500, 500);

        //get the new width and height
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Create a mutable copy of the bitmap to modify
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        //Array list for all the coordinate pairs
        List<int[]> bluePixelPositions = new ArrayList<>();

        //Find all the pixels where all the colours need to change where they are above the threshold
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = mutableBitmap.getPixel(x, y);
                int blue = Color.blue(pixel);
                if (blue > 30) {
                    bluePixelPositions.add(new int[]{x, y});
                }
            }
        }

        //array used which cumulatively calculates how much should be used
        float[] pixelNumber = new float[responses.length];
        int cumulativePixelCount = 0;

        //Go through each of the emotions, calculate the proportion that emotion should have of the
        //total number of pixels.
        for (int i = 0; i < responses.length; i++) {
            cumulativePixelCount += (responses[i] * bluePixelPositions.size()) / totalResponses;
            pixelNumber[i] = cumulativePixelCount;
        }



        int[] colours = {Color.YELLOW,Color.RED, Color.GRAY, Color.GREEN, Color.BLUE};
        int currentBluePixelCount = 0;
        int colourIndex =0;

        /*
        Go through each coordinate pair, First check if we need to change so a new colour for the
        pixel, if the current number of blue pixels is greater or equal to the current emotion
        then we move the next emotion as long as we are not out of bounds
        Then set the pixel to the current colour being used
        Then add one to the pixel value to keep track of where we are.

         */
        for (int[] position : bluePixelPositions) {
            if (currentBluePixelCount >= pixelNumber[colourIndex] && colourIndex < 4) {
                colourIndex++;
            }
            mutableBitmap.setPixel(position[0], position[1], colours[colourIndex]);
            currentBluePixelCount++;
        }

        return mutableBitmap;
    }


    //Need to scale the bit map size down for memory issues, Does this scaling down the image until
    //It is within the required size and height

    private Bitmap getScaledBitmap(int resId, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();//This initialises a new instance of bitmap Factory Options
        options.inJustDecodeBounds = true;//Only load image dimensions without loading them into memory
        BitmapFactory.decodeResource(getResources(), resId, options);//Decodes resource into bitmap object
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//Set the inSampleSize needed, using calculate InSampleSize
        options.inJustDecodeBounds = false;// Now load actual image into memory
        return BitmapFactory.decodeResource(getResources(), resId, options);//Returns the bitmap object needed
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;//Find the original heigt
        int width = options.outWidth;//Find the original width of the

        int inSampleSize = 1;//Sample size starts at one

        if (height > reqHeight || width > reqWidth) {//If the height or width is higher than the required

            int halfHeight = height / 2;
            int halfWidth = width / 2;


            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize = inSampleSize* 2;
            }
            /*
            Works out how many times the image size needs to be cut in half to be within the required
            range
             */

        }
        return inSampleSize;
    }



}
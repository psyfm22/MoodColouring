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

public class ImageProcessingService extends Service {
    public static final String CHANNEL_ID = "DownloadChannel";
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "COMP3018";
    private boolean isImageProcessing = false;
    private boolean cancelDownload = false;
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

        int[] responseCounts = intent.getIntArrayExtra("responseCounts");
        int totalResponses = intent.getIntExtra("totalResponses", 0);
        int inputImage = intent.getIntExtra("inputImage", 0);

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
//            Bitmap processedImage = createImage(responseCounts, totalResponses, R.drawable.cocacola);
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


//need to rework a lot of this for efficiency and reworking

    private Bitmap createImage(int[] responses, int totalResponses, int image) {
        Bitmap originalBitmap = getScaledBitmap(image, 640, 480); // Use appropriate target width and height
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Create a mutable copy of the bitmap to modify
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);


        List<int[]> bluePixelPositions = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = mutableBitmap.getPixel(x, y);
                int blue = Color.blue(pixel);
                if (blue > 30) {
                    bluePixelPositions.add(new int[]{x, y});
                }
            }
        }

        float[] pixelNumber = new float[responses.length];
        int cumulativePixelCount = 0;
        for (int i = 0; i < responses.length; i++) {
            cumulativePixelCount += (responses[i] * bluePixelPositions.size()) / totalResponses;
            pixelNumber[i] = cumulativePixelCount;
        }



        int[] colours = {Color.YELLOW,Color.RED, Color.GRAY, Color.GREEN, Color.BLUE};
        int currentBluePixelCount = 0;
        int colourIndex =0;

        for (int[] position : bluePixelPositions) {
            if (currentBluePixelCount >= pixelNumber[colourIndex] && colourIndex < colours.length - 1) {
                colourIndex++;
            }
            mutableBitmap.setPixel(position[0], position[1], colours[colourIndex]);
            currentBluePixelCount++;
        }

        return mutableBitmap;
    }


    //Need to scale the bit map size down

    private Bitmap getScaledBitmap(int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//Only load image dimensions without loading them into memoy
        BitmapFactory.decodeResource(getResources(), resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;// Now load actual image into memory
        return BitmapFactory.decodeResource(getResources(), resId, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;//Find the original heigt
        final int width = options.outWidth;//Find the original width of the

        int inSampleSize = 1;//Sample size starts at one

        if (height > reqHeight || width > reqWidth) {//If the height or width is higher than the required

            final int halfHeight = height / 2;//Divide it by 2
            final int halfWidth = width / 2;//Divide it by 2


            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }



}
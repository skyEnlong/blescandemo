package glide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;

import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.facebook.animated.webp.WebPFrame;
import com.facebook.animated.webp.WebPImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by enlong on 2016/12/15.
 */

public class WebpGifDrawable extends GlideDrawable{
    static {
        System.loadLibrary("gifimage");
    }

    private InputStream inputStream;
    private  Rect destRect;
    private  Paint paint;
    private WebPImage webPImage;
    private boolean isRunning;
    private boolean isStarted;
    private boolean isRecycled;
    private boolean isVisible;
    private int loopCount;
    private int maxLoopCount;
    private boolean applyGravity;
    private String id;

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 1024)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    public WebpGifDrawable(InputStream inputStream){
        id = "WebpGifDrawable....";
        Log.i("enlong", "id :" + id);
        destRect  = new Rect();
        paint = new Paint();
        try {
            webPImage = WebPImage.create(input2byte(inputStream));
            Log.i("enlong", "webp creat success");
            int height = webPImage.getHeight();
            int duration = webPImage.getDuration();
            int width = webPImage.getWidth();
            maxLoopCount =  webPImage.getLoopCount();

            Log.i("enlong", String.format("webp creat success height: %d, duration: %d width: %d, maxLoopCount %d", height, duration, width, maxLoopCount));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebpGifDrawable(ImageVideoWrapper source) {
        id = source.getFileDescriptor().toString();
        Log.i("enlong", "id :" + id);
        inputStream = source.getStream();
        destRect  = new Rect();
        paint = new Paint();
        try {
             webPImage = WebPImage.create(input2byte(inputStream));
            Log.i("enlong", "webp creat success");
            int height = webPImage.getHeight();
            int duration = webPImage.getDuration();
            int width = webPImage.getWidth();
            maxLoopCount =  webPImage.getLoopCount();

            Log.i("enlong", String.format("webp creat success height: %d, duration: %d width: %d, maxLoopCount %d", height, duration, width, maxLoopCount));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void setLoopCount(int loopCount) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        if(!this.isRecycled) {
            if(this.applyGravity) {
                Gravity.apply(119, this.getIntrinsicWidth(), this.getIntrinsicHeight(), this.getBounds(), this.destRect);
                this.applyGravity = false;
            }

            Bitmap currentFrame = Bitmap.createBitmap(this.getIntrinsicWidth(), this.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            WebPFrame frame = webPImage.getFrame(0);
            frame.renderFrame(currentFrame.getWidth(), currentFrame.getHeight(), currentFrame);

            canvas.drawBitmap(currentFrame, (Rect)null, this.destRect, this.paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public int[] getData() {
        return new int[0];
    }

    public Bitmap getFirstFrame() {
        return null;
    }

    public void recycle() {
        this.isRecycled = true;
    }


    public String getId() {
        return null;
    }
}

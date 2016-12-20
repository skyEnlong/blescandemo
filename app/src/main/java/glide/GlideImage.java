package glide;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.enlong.blescandemo.R;

import java.io.File;

/**
 * Created by chenqiang on 2016/11/25.
 */

public class GlideImage<T> {

    private final RequestManager requestManager;
    /**
     * Allows the implementor to apply some options to the given request.
     *
     * @param imageView
     * @param <T> The type of the model.
     */

    /**
     * 从资源文件中加载：
     * int resourceId = R.mipmap.ic_launcher;
     * 从文件中加载图片：
     * File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Running.jpg");
     * 从URI中加载图片：
     * Uri uri = resourceIdToUri(context, R.mipmap.future_studio_launcher);*/

    static final String FitCenter = "fitCenter";
    static final String CenterCrop = "centerCrop";
    private final Context mContext;
//    private GlideCircleTransform avertTransform;
//    private GlideCircleTransform circleTransform;
    public GlideImage(Context context) {
        mContext = context;
        requestManager = Glide.with(context);
//        circleTransform =  new GlideCircleTransform(mContext, 0,
//                Color.WHITE);
//        avertTransform = new GlideCircleTransform(mContext, (int)(3 * context.getResources().getDisplayMetrics().density),
//                Color.WHITE);

    }
    public void displayImage(T source, ImageView imageView) {
        this.displayImage(source, imageView, 0, 0);
    }


    public void displayImage(T source, ImageView imageView, int placeImg) {
        this.displayImage(source, imageView, placeImg, 0);
    }
    /**
     *
     * @param placeImg  默认占位图
     * @param errImg 错误图
     */
    public void displayImage(T source, ImageView imageView, int placeImg, int errImg) {
        requestManager
                .load(source)
                .placeholder(placeImg)
                .error(errImg)
                .dontAnimate()
//                .thumbnail(0.1f)
                .into(imageView);
    }

    /**
     *
     * @param placeImg  默认占位图
     * @param errImg 错误图
     */
    public void displayImage(T source, ImageView imageView, int placeImg, int errImg, BitmapTransformation transformation) {
        requestManager
                .load(source)
                .placeholder(placeImg)
                .skipMemoryCache(false)
                .error(errImg)
                .transform(transformation)
                .into(imageView);
    }

    /**
     *
     * @param placeImg  默认占位图
     * @param errImg 错误图
     */
    public void displayAnimationImage(T source, ImageView imageView, int placeImg, int errImg) {
        requestManager
                .load(source)
//                .decoder(new WebpGifDecorder())
                .into(imageView);

        try{
            WebpGifDrawable dra = new WebpGifDrawable(mContext.getResources().openRawResource(R.raw.webp_gif));
            imageView.setImageDrawable(dra);
        }catch (Exception e){

        }

    }



    /**
     *Glide 显示gif
     */
    public void displayGIFImage(T source, ImageView imageView, int placeImg, int errImg) {
        requestManager
                .load(source)
                .asGif()
                .placeholder(placeImg)
                .error(errImg)
                .fitCenter()
                .thumbnail(0.1f)
                .into(imageView);
    }



    /**
     *Glide 回调bitmap
     */
    public void displayImageListen(T source, ImageView imageView, int placeImg, int errImg) {
        Glide.with( mContext.getApplicationContext() ) // safer!
                .load(source)
                .listener(new RequestListener<T, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, T model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, T model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }


    /**
     *Glide 恢复加载
     */
    public void resume(){
        requestManager.resumeRequests();
    }


    /**
     *Glide 暂停加载
     */
    public void pause(){
        requestManager.pauseRequests();
    }


    /**
     *Glide 取消请求
     */
    public void clear(View view){
        Glide.clear(view);
    }

    public void displayImageNoCache(T source, ImageView imageView, int placeImg) {
        requestManager
                .load(source)
                .placeholder(placeImg)
                .thumbnail(0.1f)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void storeImageFromService(T source){
        requestManager
                .load(source)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {

                    }
                });
    }

}

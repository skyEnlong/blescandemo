package glide;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.bumptech.glide.load.resource.gif.GifDrawable;

/**
 * Created by enlong on 2016/12/15.
 */

public class WebpGifBitmapWrapper   {

    private String id;
    private Resource<WebpGifDrawable> webpGifResource;
    public  WebpGifBitmapWrapper(Resource<Bitmap> bitmapResource, Resource<GifDrawable> gifResource,
                                 Resource<WebpGifDrawable> webpGifResource ) {

        this.webpGifResource =webpGifResource;
    }

    public WebpGifBitmapWrapper(Resource<Bitmap> bitmapResource, Resource<GifDrawable> gifResource) {

    }

    public Resource<WebpGifDrawable> getWebpGifResource(){
        return webpGifResource;
    }

    public WebpGifBitmapWrapper(ImageVideoWrapper source){
        WebpGifDrawable drawable = new WebpGifDrawable(source);
        webpGifResource = new WebpGifDrawableResource(drawable);
        id = drawable.getId();

    }

    public String getId() {
        return TextUtils.isEmpty(id) ? "WebpGifDecorder" : id;
    }
}

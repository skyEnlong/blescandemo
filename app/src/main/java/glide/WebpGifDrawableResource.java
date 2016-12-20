package glide;

import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.util.Util;

/**
 * Created by enlong on 2016/12/15.
 */

public class WebpGifDrawableResource extends DrawableResource<WebpGifDrawable> {
    public WebpGifDrawableResource(WebpGifDrawable drawable) {
        super(drawable);
    }

    @Override
    public int getSize() {
        return drawable.getData().length + Util.getBitmapByteSize(drawable.getFirstFrame());
    }

    @Override
    public void recycle() {
        drawable.stop();
        drawable.recycle();
    }
}
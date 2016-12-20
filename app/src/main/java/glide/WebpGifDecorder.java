package glide;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ImageVideoWrapper;

import java.io.IOException;

/**
 * Created by enlong on 2016/12/15.
 */

public class WebpGifDecorder<ModelType> implements ResourceDecoder<ImageVideoWrapper, WebpGifBitmapWrapper> {
    private String id;
    @Override
    public Resource<WebpGifBitmapWrapper> decode(ImageVideoWrapper source, int width, int height) throws IOException {

       final  WebpGifBitmapWrapper wrapper = new WebpGifBitmapWrapper(source);
        id = wrapper.getId();
        Resource<WebpGifBitmapWrapper> resource = new Resource<WebpGifBitmapWrapper>() {
            @Override
            public WebpGifBitmapWrapper get() {
                return wrapper;
            }

            @Override
            public int getSize() {
                return wrapper.getWebpGifResource().getSize();
            }

            @Override
            public void recycle() {
                 wrapper.getWebpGifResource().recycle();
            }
        };
        return resource;
    }

    @Override
    public String getId() {

        return id;
    }
}

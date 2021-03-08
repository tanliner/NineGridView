package com.lzy.ninegrid.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.lzy.ninegrid.R;
import com.tw.moments.common.ItemClickListener;

import java.util.List;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/3/21
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePreviewAdapter extends PagerAdapter {

    private List<ImageInfo> imageInfo;
    private Context context;
    private View currentView;
    private OnPhotoTapListener photoTapListener;
    private OnOutsidePhotoTapListener photoTapOutsideListener;
    private ItemClickListener longClickListener;
    private NineGridView.ImageLoader imageLoader;

    public ImagePreviewAdapter(Context context, @NonNull List<ImageInfo> imageInfo) {
        super();
        this.imageInfo = imageInfo;
        this.context = context;
        imageLoader = NineGridView.getImageLoader();
    }

    /**
     * @param tapListener tap listener of image
     */
    public void setTapListener(OnPhotoTapListener tapListener) {
        photoTapListener = tapListener;
    }

    public void setTapOutSideListener(OnOutsidePhotoTapListener tapListener) {
        photoTapOutsideListener = tapListener;
    }

    public void setItemLongClickListener(ItemClickListener listener) {
        this.longClickListener = listener;
    }

    public void setImageLoader(NineGridView.ImageLoader loader) {
        imageLoader = loader;
    }

    @Override
    public int getCount() {
        return imageInfo.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentView = (View) object;
    }

    public View getPrimaryItem() {
        return currentView;
    }

    public ImageView getPrimaryImageView() {
        return (ImageView) currentView.findViewById(R.id.pv);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photoview, container, false);
        final ProgressBar pb = (ProgressBar) view.findViewById(R.id.pb);
        final PhotoView imageView = (PhotoView) view.findViewById(R.id.pv);
        imageView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemClick(position, imageView, 0);
                return true;
            } else {
                return false;
            }
        });
        ImageInfo info = this.imageInfo.get(position);
        imageView.setMaximumScale(4.5F);
        imageView.setOnPhotoTapListener(photoTapListener);
        imageView.setOnOutsidePhotoTapListener(photoTapOutsideListener);
        showExcessPic(info, imageView);

        //如果需要加载的loading,需要自己改写,不能使用这个方法
        imageLoader.onDisplayImage(view.getContext(), imageView, info.bigImageUrl);

        container.addView(view);
        return view;
    }

    /** 展示过度图片 */
    private void showExcessPic(ImageInfo imageInfo, PhotoView imageView) {
        //先获取大图的缓存图片
        Bitmap cacheImage = imageLoader.getCacheImage(imageInfo.bigImageUrl);
        //如果大图的缓存不存在,在获取小图的缓存
        if (cacheImage == null) {
            cacheImage = imageLoader.getCacheImage(imageInfo.thumbnailUrl);
        }
        //如果没有任何缓存,使用默认图片,否者使用缓存
        if (cacheImage == null) {
            imageView.setImageResource(R.drawable.ic_default_color);
        } else {
            imageView.setImageBitmap(cacheImage);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
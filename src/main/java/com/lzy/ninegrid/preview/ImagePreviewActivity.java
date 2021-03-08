package com.lzy.ninegrid.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.ninegrid.DeviceUtil;
import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.lzy.ninegrid.R;

import java.util.ArrayList;
import java.util.Locale;

public class ImagePreviewActivity extends AppCompatActivity implements ViewTreeObserver.OnPreDrawListener {

    private static final String TAG = "ImagePreviewActivity";
    public static final String IMAGE_INFO = "image_info";
    public static final String IMAGE_INFO_POS = "image_info_pos";
    public static final String CURRENT_ITEM = "CURRENT_ITEM";
    public static final String SKIP_CACHE = "arg_skip_cache";
    public static final int REQ_PREVIEW_BACK = 0xF100;
    public static final int ANIMATE_DURATION = 200;

    private RelativeLayout rootView;
    private ViewPager previewPager;

    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<ImageInfo> imageInfos;
    private int currentItem;
    private boolean skipCache;
    private int imageHeight;
    private int imageWidth;
    private int screenWidth;
    private int screenHeight;

    public static void startPreview(AppCompatActivity activity, int index, ArrayList<ImageInfo> sourceInfos) {
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IMAGE_INFO, sourceInfos);
        bundle.putInt(CURRENT_ITEM, index);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, REQ_PREVIEW_BACK);
        activity.overridePendingTransition(0, 0);
    }

    public static void startPreview(Fragment fragment, int index, ArrayList<ImageInfo> sourceInfos) {
        startPreview(fragment, index, sourceInfos, false);
    }

    public static void startPreview(Fragment fragment, int index, ArrayList<ImageInfo> sourceInfos, boolean skipCache) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IMAGE_INFO, sourceInfos);
        bundle.putInt(CURRENT_ITEM, index);
        bundle.putBoolean(SKIP_CACHE, skipCache);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, REQ_PREVIEW_BACK);
        activity.overridePendingTransition(0, 0);
    }

    public static ArrayList<ImageInfo> getLocation(RecyclerView rv, ArrayList<?> items, int blankSize) {
        if (rv.getLayoutManager() == null) {
            return new ArrayList<>();
        }
        ArrayList<ImageInfo> sourceInfos = new ArrayList<>();
        for (int i = blankSize; i < items.size(); i++) {
            Object obj = items.get(i);
            // if (!(obj instanceof PreviewImageInfo)) {
            //     continue;
            // }
            // PreviewImageInfo imageInfo = (PreviewImageInfo) obj;
            // ImageInfo imageInfo = (PreviewImageInfo) obj;
            ImageInfo info = new ImageInfo();
            // info.thumbnailUrl = imageInfo.getPath();
            // info.bigImageUrl = imageInfo.getPath();
            info.bigImageUrl = "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRJm8UXZ0mYtjv1a48RKkFkdyd4kOWLJB0o_l7GuTS8-q8VF64w";
            View imageView = rv.getLayoutManager().getChildAt(i);

            info.imageViewWidth = imageView.getWidth();
            info.imageViewHeight = imageView.getHeight();
            int[] points = new int[2];
            imageView.getLocationInWindow(points);
            info.imageViewX = points[0];
            info.imageViewY = points[1] - 60;
            sourceInfos.add(info);
        }
        return sourceInfos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int f = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            f |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(f);

        //设置导航栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // getWindow().setNavigationBarColor(ColorUtils.blendARGB(0x000000, 0xFFFFFF, 0));
            getWindow().setNavigationBarColor(0x00000000);
        }

        processArgs();
        initView();
        getScreenSize();
        setViewPagerAdapter();
    }

    private void processArgs() {
        Intent intent = getIntent();
        imageInfos = intent.getParcelableArrayListExtra(IMAGE_INFO);
        currentItem = intent.getIntExtra(CURRENT_ITEM, 0);
        skipCache = intent.getBooleanExtra(SKIP_CACHE, false);
        if (imageInfos == null || imageInfos.size() == 0) {
            Log.e(TAG, "must has a preview list");
            finish();
        }
    }

    private void initView() {
        previewPager = findViewById(R.id.vp_preview);
        rootView = findViewById(R.id.root);
        previewPager = findViewById(R.id.vp_preview);
    }

    private void setViewPagerAdapter() {
        imagePreviewAdapter = new ImagePreviewAdapter(this, imageInfos);
        if (skipCache) {
            imagePreviewAdapter.setImageLoader(new NineGridView.ImageLoader() {
                @Override
                public void onDisplayImage(Context context, ImageView imageView, String url) {
                    Glide.with(context)
                            .load(url)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_default_color)
                            .error(R.drawable.ic_default_color)
                            .into(imageView);
                }

                @Override
                public Bitmap getCacheImage(String url) {
                    return null;
                }
            });
        }
        imagePreviewAdapter.setItemLongClickListener((pos, view, type) -> {
            // TODO, more action
        });
        previewPager.setAdapter(imagePreviewAdapter);
        previewPager.setCurrentItem(currentItem);
        previewPager.getViewTreeObserver().addOnPreDrawListener(this);
        previewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentItem = position;
                updateIndicator(position, imageInfos.size());
            }
        });
        imagePreviewAdapter.setTapListener((view, x, y) -> {
            finishWithData();
        });
        imagePreviewAdapter.setTapOutSideListener(view -> {
            finishWithData();
        });

        updateIndicator(currentItem, imageInfos.size());
    }

    private void getScreenSize() {
        Point size = new Point();
        DeviceUtil.getDeviceSize(this, size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    private void updateIndicator(int curIndex, int total) {

    }

    public String format(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }

    private void finishWithData() {
        Intent dataIntent = new Intent();
        dataIntent.putParcelableArrayListExtra(IMAGE_INFO, imageInfos);
        setResult(RESULT_OK, dataIntent);
        finishActivityAnim();
    }

    @Override
    public void onBackPressed() {
        finishWithData();
    }

    /**
     * 绘制前开始动画
     */
    @Override
    public boolean onPreDraw() {
        rootView.getViewTreeObserver().removeOnPreDrawListener(this);
        final View view = imagePreviewAdapter.getPrimaryItem();
        final ImageView imageView = imagePreviewAdapter.getPrimaryImageView();
        computeImageWidthAndHeight(imageView);

        final ImageInfo imageData = imageInfos.get(currentItem);
        final float vx = getInitScaleX(imageData);
        final float vy = getInitScaleY(imageData);
        ValueAnimator valueAnimator = generateAnimator(animation -> {
            enterUpdater(animation, imageData, view, imageView, vx, vy);
        });
        // 进场动画过程监听
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                rootView.setBackgroundColor(0x0);
            }
        });
        valueAnimator.start();
        return true;
    }

    /**
     * activity的退场动画
     */
    public void finishActivityAnim() {
        final View view = imagePreviewAdapter.getPrimaryItem();
        final ImageView imageView = imagePreviewAdapter.getPrimaryImageView();
        computeImageWidthAndHeight(imageView);

        final ImageInfo imageData;
        if (imageInfos == null || imageInfos.size() == 0) {
            imageData = new ImageInfo();
        } else {
            imageData = imageInfos.get(currentItem);
        }
        final float vx = getInitScaleX(imageData);
        final float vy = getInitScaleY(imageData);

        final ValueAnimator valueAnimator = generateAnimator(animation ->
                exitUpdater(animation, imageData, view, imageView, vx, vy));
        // 退场动画过程监听
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                rootView.setBackgroundColor(0x0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        valueAnimator.start();
    }

    private ValueAnimator generateAnimator(ValueAnimator.AnimatorUpdateListener updater) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1.0f);
        valueAnimator.addUpdateListener(updater);
        valueAnimator.setDuration(ANIMATE_DURATION);
        return valueAnimator;
    }

    private void exitUpdater(ValueAnimator animation, ImageInfo imageInfo, View root, View anchor, final float vx, final float vy) {
        long duration = animation.getDuration();
        long playTime = animation.getCurrentPlayTime();
        float fraction = duration > 0 ? (float) playTime / duration : 1f;
        if (fraction > 1) {
            fraction = 1;
        }
        root.setTranslationX(evaluateInt(fraction, 0, getInitTransX(imageInfo, anchor)));
        root.setTranslationY(evaluateInt(fraction, 0, getInitTransY(imageInfo, anchor)));
        root.setScaleX(evaluateFloat(fraction, 1, vx));
        root.setScaleY(evaluateFloat(fraction, 1, vy));
        root.setAlpha(1 - fraction);

        rootView.setBackgroundColor(evaluateArgb(fraction, Color.BLACK, Color.TRANSPARENT));
    }

    private void enterUpdater(ValueAnimator animation, ImageInfo imageInfo, View root, View anchor, final float vx, final float vy) {
        long duration = animation.getDuration();
        long playTime = animation.getCurrentPlayTime();
        float fraction = duration > 0 ? (float) playTime / duration : 1f;
        if (fraction > 1) {
            fraction = 1;
        }
        root.setTranslationX(evaluateInt(fraction, getInitTransX(imageInfo, anchor), 0));
        root.setTranslationY(evaluateInt(fraction, getInitTransY(imageInfo, anchor), 0));
        root.setScaleX(evaluateFloat(fraction, vx, 1));
        root.setScaleY(evaluateFloat(fraction, vy, 1));
        root.setAlpha(fraction);

        rootView.setBackgroundColor(evaluateArgb(fraction, Color.TRANSPARENT, Color.BLACK));
    }

    /**
     * 计算图片的宽高
     */
    private void computeImageWidthAndHeight(ImageView imageView) {

        // 获取真实大小
        Drawable drawable = imageView.getDrawable();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        // 计算出与屏幕的比例，用于比较以宽的比例为准还是高的比例为准，因为很多时候不是高度没充满，就是宽度没充满
        float h = screenHeight * 1.0f / intrinsicHeight;
        float w = screenWidth * 1.0f / intrinsicWidth;
        if (h > w) {
            h = w;
        } else {
            w = h;
        }

        // 得出当宽高至少有一个充满的时候图片对应的宽高
        imageHeight = (int) (intrinsicHeight * h);
        imageWidth = (int) (intrinsicWidth * w);
    }

    private int getInitTransX(ImageInfo imageData, View view) {
        return imageData.imageViewX + (imageData.imageViewWidth - view.getWidth()) / 2;
    }

    private int getInitTransY(ImageInfo imageData, View view) {
        return imageData.imageViewY + (imageData.imageViewHeight - view.getHeight()) / 2;
    }

    private float getInitScaleX(ImageInfo imageData) {
        return imageData.imageViewWidth * 1.0f / imageWidth;
    }

    private float getInitScaleY(ImageInfo imageData) {
        return imageData.imageViewHeight * 1.0f / imageHeight;
    }

    /**
     * Integer 估值器
     */
    public Integer evaluateInt(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue;
        return (int) (startInt + fraction * (endValue - startInt));
    }

    /**
     * Float 估值器
     */
    public Float evaluateFloat(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * Argb 估值器
     */
    public int evaluateArgb(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24
                | (startR + (int) (fraction * (endR - startR))) << 16
                | (startG + (int) (fraction * (endG - startG))) << 8
                | (startB + (int) (fraction * (endB - startB)));
    }
}

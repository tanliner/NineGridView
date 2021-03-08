package com.lzy.ninegrid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NineGridView extends ViewGroup {

    /**
     * 填充模式，类似于微信
     */
    public static final int MODE_FILL = 0;
    /**
     * 网格模式，类似于微信/QQ，4张图会 2X2布局
     */
    public static final int MODE_GRID = 1;
    private static final int SPAN = 3;

    /**
     * 全局的图片加载器(必须设置,否者不显示图片)
     */
    private static ImageLoader mImageLoader;

    /**
     * 单张图片时的最大大小,单位dp
     */
    private int singleDefWidth = 180;
    /**
     * 多图片时的最大大小, 单位dp
     */
    private int gridMaxWidth = 250;
    /**
     * 单张图片的宽高比(宽/高), 但可以被 layoutparams 覆盖
     */
    private float singleImageRatio = 1.0F;
    /**
     * 最大显示的图片数
     */
    private int maxImageCount = SPAN * SPAN;
    private int gridSpacing = 3;                    // 宫格间距，单位dp
    private int mode = MODE_FILL;                   // 默认使用fill模式

    private int columnCount;    // 列数
    private int rowCount;       // 行数
    private int gridWidth;      // 宫格宽度
    private int gridHeight;     // 宫格高度

    private List<ImageView> imageViews;
    private List<ImageInfo> mImageInfos;
    private ArrayList<ImageInfo> fixedImageInfos;
    private NineGridViewAdapter mAdapter;

    public NineGridView(Context context) {
        this(context, null);
    }

    public NineGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        gridSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gridSpacing, dm);
        singleDefWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, singleDefWidth, dm);
        gridMaxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gridMaxWidth, dm);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NineGridView);
        gridSpacing = (int) a.getDimension(R.styleable.NineGridView_ngv_grid_spacing, gridSpacing);
        singleDefWidth = a.getDimensionPixelSize(R.styleable.NineGridView_ngv_single_def_width, singleDefWidth);
        gridMaxWidth = a.getDimensionPixelSize(R.styleable.NineGridView_ngv_grid_max_width, gridMaxWidth);
        singleImageRatio = a.getFloat(R.styleable.NineGridView_ngv_single_ratio, singleImageRatio);
        maxImageCount = a.getInt(R.styleable.NineGridView_ngv_max_count, maxImageCount);
        mode = a.getInt(R.styleable.NineGridView_ngv_mode, mode);
        a.recycle();

        imageViews = new ArrayList<>();
        mImageInfos = new ArrayList<>();
        fixedImageInfos = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdapter == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        List<ImageInfo> adapterInfos = mAdapter.getImageInfo();
        if (CollectionUtils.isBlank(adapterInfos)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        if (adapterInfos.size() > 1) {
            totalWidth = gridMaxWidth - getPaddingLeft() - getPaddingRight();
            // gridWidth = gridHeight = (totalWidth - gridSpacing * (columnCount - 1)) / columnCount;
            // 这里无论是几张图片，宽高都按总宽度的 1/3
            gridWidth = gridHeight = (totalWidth - gridSpacing * (SPAN - 1)) / SPAN;
        } else {
            gridWidth = Math.min(totalWidth, gridMaxWidth);
            gridHeight = (int) (gridWidth / singleImageRatio);
            // 矫正图片显示区域大小，不允许超过最大显示范围
            if (gridHeight > gridMaxWidth) {
                float ratio = gridMaxWidth * 1.0f / gridHeight;
                gridWidth = (int) (gridWidth * ratio);
                gridHeight = gridMaxWidth;
            }
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp != null) {
                gridWidth = lp.width;
                gridHeight = lp.height;
            }
        }
        width = gridWidth * columnCount + gridSpacing * (columnCount - 1) + getPaddingLeft() + getPaddingRight();
        height = gridHeight * rowCount + gridSpacing * (rowCount - 1) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed || CollectionUtils.isBlank(mImageInfos)) {
            return;
        }
        int count = mImageInfos.size();
        for (int i = 0; i < count; i++) {
            ImageView childrenView = (ImageView) getChildAt(i);

            int rowNum = i / columnCount;
            int columnNum = i % columnCount;
            int left = (gridWidth + gridSpacing) * columnNum + getPaddingLeft();
            int top = (gridHeight + gridSpacing) * rowNum + getPaddingTop();
            int right = left + gridWidth;
            int bottom = top + gridHeight;

            childrenView.layout(left, top, right, bottom);
        }
    }

    /**
     * 设置适配器
     */
    public void setAdapter(@NonNull NineGridViewAdapter adapter) {
        mAdapter = adapter;
        if (CollectionUtils.isBlank(adapter.getImageInfo())) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        // url's length is grater than maxImgCount
        fixImageCount(fixedImageInfos);
        // update image count
        int imageCount = fixedImageInfos.size();
        justifyItems(imageCount);
        resetLayout(imageCount);

        // update data set
        mImageInfos.clear();
        mImageInfos.addAll(fixedImageInfos);
        // loading
        loadImages(imageCount);

        // 修改最后一个条目，决定是否显示更多
        if (adapter.getImageInfo().size() > maxImageCount) {
            View child = getChildAt(maxImageCount - 1);
            if (child instanceof NineGridViewWrapper) {
                NineGridViewWrapper imageView = (NineGridViewWrapper) child;
                imageView.setMoreNum(adapter.getImageInfo().size() - maxImageCount);
            }
        }
    }

    private void fixImageCount(ArrayList<ImageInfo> fixedInfos) {
        fixedInfos.clear();
        List<ImageInfo> adapterInfos = mAdapter.getImageInfo();
        int imageCount = adapterInfos.size();
        if (maxImageCount > 0 && imageCount > maxImageCount) {
            fixedInfos.addAll(adapterInfos.subList(0, maxImageCount));
        } else {
            fixedInfos.addAll(adapterInfos);
        }
    }

    private void justifyItems(int imageCount) {
        // 默认是3列显示，行数根据图片的数量决定
        rowCount = imageCount / SPAN + (imageCount % SPAN == 0 ? 0 : 1);
        columnCount = Math.min(imageCount, SPAN);
        // grid模式下，显示4张使用 2x2 模式
        if (mode == MODE_GRID && imageCount == 4) {
            rowCount = 2;
            columnCount = 2;
        }
    }

    private void resetLayout(int newViewCount) {
        // 保证View的复用，避免重复创建
        if (CollectionUtils.isBlank(imageViews)) {
            for (int i = 0; i < newViewCount; i++) {
                ImageView iv = getImageView(i);
                if (iv == null) {
                    break;
                }
                addView(iv, generateDefaultLayoutParams());
            }
            return;
        }

        int oldViewCount = mImageInfos.size();
        if (oldViewCount > newViewCount) {
            removeViews(newViewCount, oldViewCount - newViewCount);
        } else if (oldViewCount < newViewCount) {
            for (int i = oldViewCount; i < newViewCount; i++) {
                ImageView iv = getImageView(i);
                if (iv == null) {
                    break;
                }
                addView(iv, generateDefaultLayoutParams());
            }
        }
    }

    private void loadImages(int imageCount) {
        for (int i = 0; i < imageCount; i++) {
            ImageView iv = getImageView(i);
            if (iv == null) {
                continue;
            }
            if (imageCount == 1) {
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            if (mImageLoader != null) {
                mImageLoader.onDisplayImage(getContext(), iv, mImageInfos.get(i).thumbnailUrl);
            }
        }
    }

    /**
     * 获得 ImageView 保证了 ImageView 的重用
     */
    private ImageView getImageView(final int position) {
        ImageView imageView;
        if (position < imageViews.size()) {
            imageView = imageViews.get(position);
        } else {
            imageView = mAdapter.generateImageView(getContext());
            // TODO, throttleFirst
            imageView.setOnClickListener(v -> {
                mAdapter.onImageItemClick(getContext(),
                        NineGridView.this,
                        position,
                        mAdapter.getImageInfo());
            });
            imageViews.add(imageView);
        }
        return imageView;
    }

    /**
     * 设置宫格间距
     */
    public void setGridSpacing(int spacing) {
        gridSpacing = spacing;
    }

    public int getGridSpacing() {
        return gridSpacing;
    }

    /**
     * 设置只有一张图片时的宽
     */
    public int getSingleDefWidth() {
        return singleDefWidth;
    }

    public int getGridMaxWidth() {
        return gridMaxWidth;
    }

    /**
     * 设置只有一张图片时的宽高比
     */
    public void setSingleImageRatio(float ratio) {
        singleImageRatio = ratio;
    }

    /**
     * 设置最大图片数
     */
    public void setMaxSize(int maxSize) {
        maxImageCount = maxSize;
    }

    public int getMaxSize() {
        return maxImageCount;
    }

    public static void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public static ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public interface ImageLoader {
        /**
         * 需要子类实现该方法，以确定如何加载和显示图片
         *
         * @param context 上下文
         * @param imageView 需要展示图片的ImageView
         * @param url 图片地址
         */
        void onDisplayImage(Context context, ImageView imageView, String url);

        /**
         * @param url 图片的地址
         * @return 当前框架的本地缓存图片
         */
        Bitmap getCacheImage(String url);
    }
}

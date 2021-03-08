package com.lzy.ninegrid;

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author jeasonlzy
 */
public class NineGridViewAdapter implements Serializable {

    protected Context context;
    private ArrayList<ImageInfo> imageInfos;

    public NineGridViewAdapter(Context context, ArrayList<ImageInfo> imageInfos) {
        this.context = context;
        this.imageInfos = imageInfos;
    }

    /**
     * 如果要实现图片点击的逻辑，重写此方法即可
     *
     * @param context      上下文
     * @param nineGridView 九宫格控件
     * @param index        当前点击图片的的索引
     * @param adapterInfos 图片地址的数据集合
     */
    protected void onImageItemClick(Context context, NineGridView nineGridView, int index, ArrayList<ImageInfo> adapterInfos) {
    }

    /**
     * 生成ImageView容器的方式，默认使用NineGridImageViewWrapper类，即点击图片后，图片会有蒙板效果
     * 如果需要自定义图片展示效果，重写此方法即可
     *
     * @param context 上下文
     * @return 生成的 ImageView
     */
    protected ImageView generateImageView(Context context) {
        NineGridViewWrapper imageView = new NineGridViewWrapper(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.ic_default_color);
        return imageView;
    }

    public ArrayList<ImageInfo> getImageInfo() {
        return imageInfos;
    }

    public void setImageInfoList(ArrayList<ImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
    }
}
package com.lzy.ninegrid.preview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.lzy.ninegrid.NineGridViewAdapter;
import com.tw.moments.common.StatusBarUtil;

import java.util.ArrayList;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/3/21
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class NineGridViewClickAdapter extends NineGridViewAdapter {

    private int statusHeight;

    public NineGridViewClickAdapter(Context context, ArrayList<ImageInfo> imageInfo) {
        super(context, imageInfo);
        statusHeight = StatusBarUtil.INSTANCE.getStatusBarHeight(context);
    }

    @Override
    protected void onImageItemClick(Context context, NineGridView nineGridView, int index, ArrayList<ImageInfo> adapterInfos) {
        for (int i = 0; i < adapterInfos.size(); i++) {
            ImageInfo info = adapterInfos.get(i);
            View imageView;
            if (i < nineGridView.getMaxSize()) {
                imageView = nineGridView.getChildAt(i);
            } else {
                // 如果图片的数量大于显示的数量，则超过部分的返回动画统一退回到最后一个图片的位置
                imageView = nineGridView.getChildAt(nineGridView.getMaxSize() - 1);
            }
            info.imageViewWidth = imageView.getWidth();
            info.imageViewHeight = imageView.getHeight();
            int[] points = new int[2];
            imageView.getLocationInWindow(points);
            info.imageViewX = points[0];
            info.imageViewY = points[1] - statusHeight;
        }
        startPreview(index, adapterInfos);
    }

    private void startPreview(int index, ArrayList<ImageInfo> sourceInfos) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ImagePreviewActivity.IMAGE_INFO, sourceInfos);
        bundle.putInt(ImagePreviewActivity.CURRENT_ITEM, index);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(0, 0);
    }
}

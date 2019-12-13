package com.digital.restaurant;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PreviewItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);

        if (position < 3) {
            outRect.top = 16;
        } else {
            outRect.top = 8;
        }
        if (position % 3 == 0) {
            outRect.left = 16;
        } else {
            outRect.left = 8;
        }
        if (position % 3 == 2) {
            outRect.right = 16;
        } else {
            outRect.right = 8;
        }
        outRect.bottom = 8;
    }
}

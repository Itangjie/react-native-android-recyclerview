package com.rn.recycleview;
import android.content.Context;
import android.view.ViewGroup;

public class RNRecycleviewItemview extends ViewGroup {

    private int mItemIndex;
    private boolean mItemIndexInitialized;

    public RNRecycleviewItemview(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    public void setItemIndex(int itemIndex) {
        if (mItemIndexInitialized  && this.mItemIndex != itemIndex){
            this.mItemIndex = itemIndex;
            if (getParent() != null) {
                ((RNRecycleview.RecyclableWrapperViewGroup) getParent()).getAdapter().notifyItemChanged(mItemIndex);
                ((RNRecycleview.RecyclableWrapperViewGroup) getParent()).getAdapter().notifyItemChanged(itemIndex);
            }
        } else {
            this.mItemIndex = itemIndex;
        }

        mItemIndexInitialized = true;
    }

    public int getItemIndex() {
        return mItemIndex;
    }
}

package com.rn.recycleview;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class RNRecycleviewItemviewManager extends ViewGroupManager<RNRecycleviewItemview> {

    private static final String REACT_CLASS = "RCTRecycleviewItemview";

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected RNRecycleviewItemview createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNRecycleviewItemview(reactContext);
    }

    @ReactProp(name = "itemIndex")
    public void setItemIndex(RNRecycleviewItemview view, int itemIndex) {
        view.setItemIndex(itemIndex);
    }


}

package com.rn.recycleview;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class RNRecycleviewModule extends ReactContextBaseJavaModule {

    public RNRecycleviewModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNRecycleview";
    }
}

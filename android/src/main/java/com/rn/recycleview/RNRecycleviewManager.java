package com.rn.recycleview;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;


import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.ContentSizeChangeEvent;
import com.facebook.react.views.scroll.ScrollEventType;

import java.util.Map;

import javax.annotation.Nullable;

public class RNRecycleviewManager extends ViewGroupManager<RNRecycleview> {

    private static final String REACT_CLASS = "RCTRecycleview";

    public static final String COMMAND_NOTIFY_ITEM_RANGE_INSERTED = "inserted";
    public static final String COMMAND_NOTIFY_ITEM_RANGE_REMOVED = "removed";
    public static final String COMMAND_NOTIFY_DATASET_CHANGED = "changed";
    public static final String COMMAND_SCROLL_TO_INDEX = "toIndex";
    public static final String COMMAND_NOTIFY_ITEM_MOVED = "moved";

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected RNRecycleview createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNRecycleview(reactContext);
    }

    @Override
    public void addView(RNRecycleview parent, View child, int index) {
        Assertions.assertCondition(child instanceof RNRecycleviewItemview, "Views attached to RNRecycleview must be RNRecycleviewItemview views.");
        RNRecycleviewItemview item = (RNRecycleviewItemview) child;
        parent.addViewToAdapter(item, index);
    }

    @Override
    public int getChildCount(RNRecycleview parent) {
        return parent.getChildCountFromAdapter();
    }

    @Override
    public View getChildAt(RNRecycleview parent, int index) {
        return parent.getChildAtFromAdapter(index);
    }

    @Override
    public void removeViewAt(RNRecycleview parent, int index) {
        parent.removeViewFromAdapter(index);
    }

    @ReactProp(name = "showsVerticalScrollIndicator")
    public void setShowsVerticalScrollIndicator(RNRecycleview parent, boolean value) {
        parent.setVerticalScrollBarEnabled(value);
    }

    @ReactProp(name = "itemCount")
    public void setItemCount(RNRecycleview parent, int itemCount) {
        parent.setItemCount(itemCount);
        parent.getAdapter().notifyDataSetChanged();
    }

    @ReactProp(name = "inverted", defaultBoolean = false)
    public void setInverted(RNRecycleview parent, boolean inverted) {
        parent.setInverted(inverted);
    }

    @ReactProp(name = "itemAnimatorEnabled", defaultBoolean = true)
    public void setItemAnimatorEnabled(RNRecycleview parent, boolean enabled) {
        parent.setItemAnimatorEnabled(enabled);
    }

    @ReactProp(name = "column",defaultInt = 1)
    public void setColumn(RNRecycleview parent, int column){
        parent.setColumn(column);
    }

    @Override
    public Map getCommandsMap() {
        return MapBuilder.of(
                "notifyItemRangeInserted", COMMAND_NOTIFY_ITEM_RANGE_INSERTED,
                "notifyItemRangeRemoved", COMMAND_NOTIFY_ITEM_RANGE_REMOVED,
                "notifyItemMoved", COMMAND_NOTIFY_ITEM_MOVED,
                "notifyDataSetChanged", COMMAND_NOTIFY_DATASET_CHANGED,
                "scrollToIndex", COMMAND_SCROLL_TO_INDEX
        );
    }

    @Override
    public void receiveCommand(final RNRecycleview parent, String commandType, @Nullable ReadableArray args) {

        Assertions.assertNotNull(parent);
        Assertions.assertNotNull(args);
        switch (commandType) {
            case COMMAND_NOTIFY_ITEM_RANGE_INSERTED: {
                final int position = args.getInt(0);
                final int count = args.getInt(1);
                //Log.d(TAG, String.format("notify item range inserted: position %d, count %d", position, count));

                RNRecycleview.ReactListAdapter adapter = (RNRecycleview.ReactListAdapter) parent.getAdapter();
                adapter.setItemCount(adapter.getItemCount() + count);
                adapter.notifyItemRangeInserted(position, count);
                return;
            }

            case COMMAND_NOTIFY_ITEM_RANGE_REMOVED: {
                final int position = args.getInt(0);
                final int count = args.getInt(1);
                //Log.d(TAG, String.format("notify item range removed: position %d, count %d", position, count));

                RNRecycleview.ReactListAdapter adapter = (RNRecycleview.ReactListAdapter) parent.getAdapter();
                adapter.setItemCount(adapter.getItemCount() - count);
                adapter.notifyItemRangeRemoved(position, count);
                return;
            }


            case COMMAND_NOTIFY_ITEM_MOVED: {
                final int currentPosition = args.getInt(0);
                final int nextPosition = args.getInt(1);
                RNRecycleview.ReactListAdapter adapter = (RNRecycleview.ReactListAdapter) parent.getAdapter();
                adapter.notifyItemMoved(currentPosition, nextPosition);
                return;
            }

            case COMMAND_NOTIFY_DATASET_CHANGED: {
                final int itemCount = args.getInt(0);
                RNRecycleview.ReactListAdapter adapter = (RNRecycleview.ReactListAdapter) parent.getAdapter();
                adapter.setItemCount(itemCount);
                parent.getAdapter().notifyDataSetChanged();
                return;
            }

            case COMMAND_SCROLL_TO_INDEX: {
                boolean animated = args.getBoolean(0);
                int index = args.getInt(1);
                RNRecycleview.ScrollOptions options = new RNRecycleview.ScrollOptions();
                options.millisecondsPerInch = args.isNull(2) ? null : (float) args.getDouble(2);
                options.viewPosition = args.isNull(3) ? null : (float) args.getDouble(3);
                options.viewOffset = args.isNull(4) ? null : (float) args.getDouble(4);

                if (animated) {
                    parent.smoothScrollToPosition(index, options);
                } else {
                    parent.scrollToPosition(index, options);
                }
                return;
            }

            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }

    @Override
    public
    @Nullable
    Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.builder()
                .put(ScrollEventType.getJSEventName(ScrollEventType.BEGIN_DRAG), MapBuilder.of("registrationName", "onScrollBeginDrag"))
                .put(ScrollEventType.getJSEventName(ScrollEventType.SCROLL), MapBuilder.of("registrationName", "onScroll"))
                .put(ScrollEventType.getJSEventName(ScrollEventType.END_DRAG), MapBuilder.of("registrationName", "onScrollEndDrag"))
                .put(ContentSizeChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", "onContentSizeChange"))
                .put(RNRecycleview.VisibleItemsChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", "onVisibleItemsChange"))
                .build();
    }
}

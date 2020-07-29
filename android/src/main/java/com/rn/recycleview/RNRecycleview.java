package com.rn.recycleview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.scroll.OnScrollDispatchHelper;
import com.facebook.react.views.scroll.ScrollEvent;
import com.facebook.react.views.scroll.ScrollEventType;

import java.util.ArrayList;
import java.util.List;

public class RNRecycleview extends RecyclerView {

    private final OnScrollDispatchHelper mOnScrollDispatchHelper = new OnScrollDispatchHelper();

    static class ScrollOptions {
        @Nullable Float millisecondsPerInch;
        @Nullable Float viewPosition;
        @Nullable Float viewOffset;
    }

    private static class ConcreteViewHolder extends ViewHolder {
        public ConcreteViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class RecyclableWrapperViewGroup extends ViewGroup {

        private ReactListAdapter mAdapter;
        private int mLastMeasuredWidth;
        private int mLastMeasuredHeight;

        public RecyclableWrapperViewGroup(Context context, ReactListAdapter adapter) {
            super(context);
            mAdapter = adapter;
            mLastMeasuredHeight = 10;
            mLastMeasuredWidth = 10;
        }

        private OnLayoutChangeListener mChildLayoutChangeListener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int oldHeight = (oldBottom - oldTop);
                int newHeight = (bottom - top);

                if (oldHeight != newHeight) {
                    if (getParent() != null) {
                        requestLayout();
                        getParent().requestLayout();
                    }
                }
            }
        };

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            // This view will only have one child that is managed by the `NativeViewHierarchyManager` and
            // its position and dimensions are set separately. We don't need to handle its layouting here
        }

        @Override
        public void onViewAdded(View child) {
            super.onViewAdded(child);
            child.addOnLayoutChangeListener(mChildLayoutChangeListener);
        }

        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            child.removeOnLayoutChangeListener(mChildLayoutChangeListener);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We override measure spec and use dimensions of the children. Children is a view added
            // from the adapter and always have a correct dimensions specified as they are calculated
            // and set with NativeViewHierarchyManager.
            // In case there is no view attached, we use the last measured dimensions.

            if (getChildCount() > 0) {
                View child = getChildAt(0);
                mLastMeasuredWidth = child.getMeasuredWidth();
                mLastMeasuredHeight = child.getMeasuredHeight();
                setMeasuredDimension(mLastMeasuredWidth, mLastMeasuredHeight);
            } else {
                setMeasuredDimension(mLastMeasuredWidth, mLastMeasuredHeight);
            }
        }

        public ReactListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Similarly to ReactViewGroup, we return true.
            // In this case it is necessary in order to force the RecyclerView to intercept the touch events,
            // in this way we can exactly know when the drag starts because "onInterceptTouchEvent"
            // of the RecyclerView will return true.
            return true;
        }
    }

    static class ReactListAdapter extends Adapter<ConcreteViewHolder> {

        private final List<RNRecycleviewItemview> mViews = new ArrayList<>();
        private final RNRecycleview mScrollView;
        private int mItemCount = 0;

        public ReactListAdapter(RNRecycleview scrollView) {
            mScrollView = scrollView;
        }

        public void addView(RNRecycleviewItemview child, int index) {
            mViews.add(index, child);

            final int itemIndex = child.getItemIndex();

            notifyItemChanged(itemIndex);
        }

        public void removeViewAt(int index) {
            RNRecycleviewItemview child = mViews.get(index);
            if (child != null) {
                mViews.remove(index);
            }
        }

        public int getViewCount() {
            return mViews.size();
        }

        @Override
        public ConcreteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ConcreteViewHolder(new RecyclableWrapperViewGroup(parent.getContext(), this));
        }

        @Override
        public void onBindViewHolder(ConcreteViewHolder holder, int position) {
            RecyclableWrapperViewGroup vg = (RecyclableWrapperViewGroup) holder.itemView;
            View row = getViewByItemIndex(position);
            if (row != null && row.getParent() != vg) {
                if (row.getParent() != null) {
                    ((ViewGroup) row.getParent()).removeView(row);
                }
                vg.addView(row, 0);
            }
        }

        @Override
        public void onViewRecycled(ConcreteViewHolder holder) {
            super.onViewRecycled(holder);
            ((RecyclableWrapperViewGroup) holder.itemView).removeAllViews();
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

        public void setItemCount(int itemCount) {
            this.mItemCount = itemCount;
        }

        public View getView(int index) {
            return mViews.get(index);
        }

        public RNRecycleviewItemview getViewByItemIndex(int position) {
            for (int i = 0; i < mViews.size(); i++) {
                if (mViews.get(i).getItemIndex() == position) {
                    return mViews.get(i);
                }
            }

            return null;
        }
    }

    private boolean mDragging;
    private int mFirstVisibleIndex, mLastVisibleIndex;

    private ReactContext getReactContext() {
        return (ReactContext) ((ContextThemeWrapper) getContext()).getBaseContext();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollDispatchHelper.onScrollChanged(l, t)) {
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.SCROLL,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            mOnScrollDispatchHelper.getXFlingVelocity(),
                            mOnScrollDispatchHelper.getYFlingVelocity(),
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
        }

        final int firstIndex = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        final int lastIndex = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (firstIndex != mFirstVisibleIndex || lastIndex != mLastVisibleIndex) {

            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(new VisibleItemsChangeEvent(
                            getId(),
                            SystemClock.nanoTime(),
                            firstIndex,
                            lastIndex));

            mFirstVisibleIndex = firstIndex;
            mLastVisibleIndex = lastIndex;
        }
    }



    public RNRecycleview(Context context) {
        super(new ContextThemeWrapper(context, R.style.ScrollbarRecyclerView));
        setHasFixedSize(true);
        ((DefaultItemAnimator)getItemAnimator()).setSupportsChangeAnimations(false);
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(new ReactListAdapter(this));
    }

    void addViewToAdapter(RNRecycleviewItemview child, int index) {
        ((ReactListAdapter) getAdapter()).addView(child, index);
    }

    void removeViewFromAdapter(int index) {
        ((ReactListAdapter) getAdapter()).removeViewAt(index);
    }

    View getChildAtFromAdapter(int index) {
        return ((ReactListAdapter) getAdapter()).getView(index);
    }

    int getChildCountFromAdapter() {
        return ((ReactListAdapter) getAdapter()).getViewCount();
    }

    void setItemCount(int itemCount) {
        ((ReactListAdapter) getAdapter()).setItemCount(itemCount);
    }

    int getItemCount() {

        return getAdapter().getItemCount();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (super.onInterceptTouchEvent(ev)) {
            NativeGestureUtil.notifyNativeGestureStarted(this, ev);
            mDragging = true;
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.BEGIN_DRAG,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            0, // xVelocity
                            0, // yVelocity
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_UP && mDragging) {
            mDragging = false;
//            mVelocityHelper.calculateVelocity(ev);
            getReactContext().getNativeModule(UIManagerModule.class).getEventDispatcher()
                    .dispatchEvent(ScrollEvent.obtain(
                            getId(),
                            ScrollEventType.END_DRAG,
                            0, /* offsetX = 0, horizontal scrolling only */
                            computeVerticalScrollOffset(),
                            ev.getX(),
                            ev.getY(),
                            getWidth(),
                            computeVerticalScrollRange(),
                            getWidth(),
                            getHeight()));
        }
        return super.onTouchEvent(ev);
    }

    private boolean mRequestedLayout = false;

    @Override
    public void requestLayout() {
        super.requestLayout();

        if (!mRequestedLayout) {
            mRequestedLayout = true;
            this.post(new Runnable() {
                @SuppressLint("WrongCall")
                @Override
                public void run() {
                    mRequestedLayout = false;
                    layout(getLeft(), getTop(), getRight(), getBottom());
                    onLayout(false, getLeft(), getTop(), getRight(), getBottom());
                }
            });
        }
    }

    @Override
    public void scrollToPosition(int position) {
        this.scrollToPosition(position, new ScrollOptions());
    }

    public void scrollToPosition(final int position, final ScrollOptions options) {
        if (options.viewPosition != null) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            final ReactListAdapter adapter = (ReactListAdapter) getAdapter();
            final View view = adapter.getViewByItemIndex(position);
            if (view != null) {
                final int viewHeight = view.getHeight();

                // In order to calculate the correct offset, we need the height of the target view.
                // If the height of the view is not available it means RN has not calculated it yet.
                // So let's listen to the layout change and we will retry scrolling.
                if (viewHeight == 0) {
                    view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            view.removeOnLayoutChangeListener(this);
                            scrollToPosition(position, options);
                        }
                    });
                    return;
                }

                final int boxStart = layoutManager.getPaddingTop();
                final int boxEnd = layoutManager.getHeight() - layoutManager.getPaddingBottom();
                final int boxHeight = boxEnd - boxStart;
                float viewOffset = options.viewOffset != null ? PixelUtil.toPixelFromDIP(options.viewOffset) : 0;
                int offset = (int) ((boxHeight - viewHeight) * options.viewPosition + viewOffset);
                layoutManager.scrollToPositionWithOffset(position, offset);
                return;
            }
        }

        super.scrollToPosition(position);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        this.smoothScrollToPosition(position, new ScrollOptions());
    }

    public void smoothScrollToPosition(int position, final ScrollOptions options) {
        final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this.getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ((LinearLayoutManager) this.getLayoutManager()).computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                if (options.millisecondsPerInch != null) {
                    return options.millisecondsPerInch / displayMetrics.densityDpi;
                } else {
                    return super.calculateSpeedPerPixel(displayMetrics);
                }
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                int calc = super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
                if (options.viewPosition != null) {
                    int viewHeight = viewEnd - viewStart;
                    int boxHeight = boxEnd - boxStart;
                    float viewOffset = options.viewOffset != null ? PixelUtil.toPixelFromDIP(options.viewOffset) : 0;
                    float target = boxStart + (boxHeight - viewHeight) * options.viewPosition + viewOffset;
                    return (int) (target - viewStart);
                } else {
                    return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
                }
            }
        };

        smoothScroller.setTargetPosition(position);
        this.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    public void setInverted(boolean inverted) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        assert layoutManager != null;
        layoutManager.setReverseLayout(inverted);
    }

    public void setItemAnimatorEnabled(boolean enabled) {
        if (enabled) {
            DefaultItemAnimator animator = new DefaultItemAnimator();
            animator.setSupportsChangeAnimations(false);
            setItemAnimator(animator);
        } else {
            setItemAnimator(null);
        }
    }

    public void setColumn(int column){
        if (column <= 1) return;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),column);
        setLayoutManager(gridLayoutManager);
    }


    public class VisibleItemsChangeEvent extends Event<VisibleItemsChangeEvent> {

        public static final String EVENT_NAME = "visibleItemsChange";

        private final int mFirstIndex;
        private final int mLastIndex;

        public VisibleItemsChangeEvent(int viewTag, long timestampMs, int firstIndex, int lastIndex) {
            super(viewTag);
            mFirstIndex = firstIndex;
            mLastIndex = lastIndex;
        }

        @Override
        public String getEventName() {
            return EVENT_NAME;
        }

        @Override
        public void dispatch(RCTEventEmitter rctEventEmitter) {
            WritableMap data = Arguments.createMap();
            data.putInt("firstIndex", mFirstIndex);
            data.putInt("lastIndex", mLastIndex);
            rctEventEmitter.receiveEvent(getViewTag(), EVENT_NAME, data);
        }
    }
}

package com.rearrangeexample.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.nineoldandroids.animation.*;

import java.util.*;

/**
 * View which animatedly rearranges child views
 * NOTE! Adapter should have stable Ids
 */
public class RearrangeListView extends ListView {

    private static final int POSITION_NONE = -1;

    private static final int DEFAULT_ANIMATION_DURATION = 500;

    private List<HoverCell> hoverCells = new ArrayList<HoverCell>();
    private List<Animator> animators = new ArrayList<Animator>();
    private List<Long> oldIds = new ArrayList<Long>();
    private boolean animating = false;

    private AdapterDatasetChangedObserver mObserver = new AdapterDatasetChangedObserver();

    private int animationDuration = DEFAULT_ANIMATION_DURATION;


    public RearrangeListView(Context context) {
        super(context);
    }

    public RearrangeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RearrangeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCells.size()>0) {
            for (HoverCell eachCell : hoverCells) {
                eachCell.drawable.draw(canvas);
            }
        }
    }

    /**
     * set the duration of animations
     * @param animationDuration new duration
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * Method which starts children rearrangement
     * @param movementMap map which connects old children positions with new
     */
    public void rearrangeViews(Map<Integer, Integer> movementMap) {
        Set<Integer> currentLocations = movementMap.keySet();
        for (Integer currentLocation: currentLocations) {
            moveView(currentLocation, movementMap.get(currentLocation));
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                ((RearrangeAdapterWrapper)getAdapter()).toggleUpdatingState();
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hoverCells.clear();
                animators.clear();
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setVisibility(VISIBLE);
                }
                animating = false;
                ((RearrangeAdapterWrapper)getAdapter()).toggleUpdatingState();
                ((RearrangeAdapterWrapper)getAdapter()).clearHiddenPositions();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    /**
     * prepare bitmaps from view and set up animator
     * @param currentPosition current view position
     * @param newPosition new position
     */
    private void moveView (int currentPosition, int newPosition) {
        if (positionsOutOfView(currentPosition, newPosition)) {
            return;
        }

        int childPosition = currentPosition > getFirstVisiblePosition() + getChildCount() - 1 || currentPosition < getFirstVisiblePosition() ?
                POSITION_NONE : currentPosition - getFirstVisiblePosition();

        View convertView = childPosition == POSITION_NONE ? null : getChildAt(childPosition);

        View currentView = getAdapter().getView(newPosition,
                convertView,
                null);
        ((RearrangeAdapterWrapper)getAdapter()).addHiddenPosition(newPosition);
        HoverCell cell = setUpHoverCell(currentView, currentPosition, newPosition);
        hoverCells.add(cell);
        prepareAnimation(cell);
    }

    /**
     * creates a cell to be drawn on the screen during animation
     * @param v current view
     * @param currentPos current view's position
     * @param newPos new view's position
     * @return cell ready to be drawn
     */
    private HoverCell setUpHoverCell(View v, int currentPos, int newPos) {
        Rect hoverCellOriginalBounds;

        //if view is outside of current visible region - measure it
        if (v.getMeasuredWidth() <= 0) {
            int specWidth = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
            v.measure(specWidth, specWidth);
        }

        //if current position is outside of current field of view start from the first invisible position
        if (currentPos < getFirstVisiblePosition()) {
            hoverCellOriginalBounds = new Rect(0, -v.getMeasuredHeight(), v.getMeasuredWidth(), 0);
        }
        else if (currentPos > getFirstVisiblePosition() + getChildCount() - 1) {
            hoverCellOriginalBounds = new Rect(0,
                    (getChildCount() - 1) * v.getMeasuredHeight(),
                    v.getMeasuredWidth(),
                    getChildCount() * v.getMeasuredHeight());
        }
        else {
            int w = v.getMeasuredWidth();
            int h = v.getMeasuredHeight();
            int top = v.getTop();
            int left = v.getLeft();
            hoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        }

        Bitmap b = getBitmapFromView(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        int newTop;
        int newBottom;

        //if new position is outside of current field of view end with the first invisible position
        if (newPos < getFirstVisiblePosition()) {
            newTop = -v.getHeight();
            newBottom = 0;
        }
        else if (newPos > getFirstVisiblePosition() + getChildCount() - 1) {
            newTop = getHeight();
            newBottom = getHeight() + v.getHeight();
        }
        else {
            newTop = newPos * (v.getMeasuredHeight() + getDividerHeight());
            newBottom = (newPos+1) * (v.getMeasuredHeight() + getDividerHeight());
        }

        Rect hoverCellNewBounds = new Rect(hoverCellOriginalBounds.left,
                newTop,
                hoverCellOriginalBounds.right,
                newBottom);

        drawable.setBounds(hoverCellNewBounds);

        return new HoverCell(drawable, hoverCellOriginalBounds, hoverCellNewBounds);
    }

    /**
     * Create a bitmap for a view
     * @param v view to draw on bitmap
     * @return bitmap representing view
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Check if current and new positions are out of ListViews field of sight
     * @param currentPosition current view position
     * @param newPosition new view position
     * @return true if view is outside of field of sight
     */
    private boolean positionsOutOfView(int currentPosition, int newPosition) {
        return currentPosition < getFirstVisiblePosition() && newPosition < getFirstVisiblePosition() ||
                currentPosition > getFirstVisiblePosition() + getChildCount()-1 && newPosition > getFirstVisiblePosition() + getChildCount()-1 ||
                currentPosition < getFirstVisiblePosition() && newPosition > getFirstVisiblePosition() + getChildCount()-1 ||
                currentPosition > getFirstVisiblePosition() + getChildCount()-1 && newPosition < getFirstVisiblePosition();
    }

    /**
     * Prepare animator for a HoverCell
     * @param cell cell to be animated
     */
    private void prepareAnimation(HoverCell cell) {
        ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(cell.drawable,
                "bounds",
                sBoundEvaluator,
                cell.initialBounds,
                cell.endBounds);
        hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
        hoverViewAnimator.setDuration(animationDuration);
        animators.add(hoverViewAnimator);
    }

    /**
     * Auxiliary class to store initial and final coordinates of hovercell and its drawable
     */
    private class HoverCell {
        private Drawable drawable;
        private Rect initialBounds;
        private Rect endBounds;

        private HoverCell(Drawable drawable, Rect initialBounds, Rect endBounds) {
            this.drawable = drawable;
            this.initialBounds = initialBounds;
            this.endBounds = endBounds;
        }
    }

    /**
     * Prepare a map connecting old and new positions
     * @return map
     */
    private Map<Integer, Integer> prepareMovementMap() {
        Map<Integer, Integer> movementMap = new HashMap<Integer, Integer>();
        for (int i=0; i<getAdapter().getCount(); i++) {
            int foundPos = -1;
            for (int j=0; j< oldIds.size(); j++) {
                if (getAdapter().getItemId(i) == oldIds.get(j)) {
                    foundPos = j;
                    break;
                }

            }
            if (i != foundPos) movementMap.put(foundPos, i);
        }
        return movementMap;
    }


    @Override
    protected void onAttachedToWindow() {
        if (getAdapter() != null && mObserver == null) {
            mObserver = new AdapterDatasetChangedObserver();
            getAdapter().registerDataSetObserver(mObserver);
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (getAdapter() != null && mObserver != null) {
            getAdapter().unregisterDataSetObserver(mObserver);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof RearrangeAdapterWrapper)) throw new IllegalStateException("Adapter class is not correct, please use RearrangeAdapterWrapper instead");
        if (!adapter.hasStableIds()) throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        adapter.registerDataSetObserver(mObserver);
        super.setAdapter(adapter);
        cacheIdOrder();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //disable touch response while views are animated
        return animating || super.dispatchTouchEvent(ev);
    }

    /**
     * Cache old order of items using only IDs
     */
    private void cacheIdOrder() {
        oldIds.clear();
        for (int i=0; i<getAdapter().getCount(); i++) {
            oldIds.add(getAdapter().getItemId(i));
        }
    }

    /**
     * Observer class to monitor data set changes
     */
    private class AdapterDatasetChangedObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            rearrangeViews(prepareMovementMap());
            cacheIdOrder();
        }
    }
}

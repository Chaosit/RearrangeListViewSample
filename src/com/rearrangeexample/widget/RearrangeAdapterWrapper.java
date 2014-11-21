package com.rearrangeexample.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter wrapper which delegates basic method calls to underlying adapter, hides views which are being moved,
 * and verifies that a new update of view's positions won't start until previous is finished
 */
public class RearrangeAdapterWrapper extends BaseAdapter {

    private BaseAdapter adapter;

    private List<Integer> hiddenPositions = new ArrayList<Integer>();
    private boolean movingViews = false;
    private boolean updatePending = false;

    public RearrangeAdapterWrapper(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return adapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = adapter.getView(position, convertView, parent);
        if (hiddenPositions.contains(position)) v.setVisibility(View.INVISIBLE);
        return v;
    }

    /**
     * prevent extra call of notifyDataSetChanged while views are moving
     */
    @Override
    public void notifyDataSetChanged() {
        if (movingViews) {
            updatePending = true;
        }
        else adapter.notifyDataSetChanged();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    /**
     * position number which is being moved
     * @param position
     */
    public void addHiddenPosition(int position) {
        hiddenPositions.add(position);
    }

    /**
     * clear hidden positions list, so the will be drawn next time
     */
    public void clearHiddenPositions() {
        hiddenPositions.clear();
    }

    public void toggleUpdatingState() {
        movingViews = !movingViews;
        if (!movingViews && updatePending) {
            updatePending = false;
            notifyDataSetChanged();
        }
    }
}

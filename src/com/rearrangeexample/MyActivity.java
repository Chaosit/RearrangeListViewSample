package com.rearrangeexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.rearrangeexample.widget.RearrangeAdapterWrapper;
import com.rearrangeexample.widget.RearrangeListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author V.Bolnykh
 * Demo Activity for working with RearrangeListView
 */
public class MyActivity extends Activity {

    private RearrangeListView listView;
    private List<TestItem> itemList;
    RearrangeAdapterWrapper adapter;

    private static final int SORT_ID_POSITION = 1;
    private static final int SORT_DATE_POSITION = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listView = (RearrangeListView) findViewById(R.id.list);
        prepareDataSet(30);
    }

    private void prepareDataSet(int length) {
        itemList = new ArrayList<TestItem>(length);
        for (int i=0; i<length; i++) {

            TestItem item = new TestItem();
            item.setId(i);
            long currentTime = new Date().getTime();
            item.setDate(new Date(currentTime - (long)(Math.random() * currentTime)));
            itemList.add(item);
        }
        adapter = new RearrangeAdapterWrapper(new TestAdapter());
        listView.setAdapter(adapter);
        listView.setAnimationDuration(500);
    }

    private class TestAdapter extends BaseAdapter {
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return itemList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_view_cell, null);
                holder = new ItemHolder();
                holder.bindViews(convertView);
                convertView.setTag(holder);
            }
            holder = (ItemHolder) convertView.getTag();
            holder.initViews(itemList.get(position));
            return convertView;
        }

        private class ItemHolder {

            private TextView name;
            private TextView date;

            private void bindViews(View root) {
                name = (TextView) root.findViewById(R.id.name);
                date = (TextView) root.findViewById(R.id.date);
            }

            private void initViews(TestItem item) {
                name.setText("Item " + Long.toString(item.getId()));
                date.setText(format.format(item.getDate()));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, SORT_ID_POSITION, Menu.NONE,getString(R.string.sor_by_id));
        menu.add(1, SORT_DATE_POSITION, Menu.NONE,getString(R.string.sort_by_date));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SORT_ID_POSITION:{
                Collections.sort(itemList, new Comparator<TestItem>() {
                    @Override
                    public int compare(TestItem lhs, TestItem rhs) {
                        return lhs.getId() - rhs.getId();
                    }
                });
                break;
            }
            case SORT_DATE_POSITION:{
                Collections.sort(itemList, new Comparator<TestItem>() {
                    @Override
                    public int compare(TestItem lhs, TestItem rhs) {
                        return rhs.getDate().compareTo(lhs.getDate());
                    }
                });
                break;
            }

        }
        adapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    private class TestItem {
        private int id;
        private Date date;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}

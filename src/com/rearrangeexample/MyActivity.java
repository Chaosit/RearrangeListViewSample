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

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private RearrangeListView listView;
    private List<TestItem> itemList;
    RearrangeAdapterWrapper adapter;

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
        menu.add(1, 1, Menu.NONE,"Sort by id");
        menu.add(1, 2, Menu.NONE,"Sort by date");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        List<TestItem> testList = new ArrayList<TestItem>(itemList);
        switch (item.getItemId()) {
            case 1:{
                Collections.sort(itemList, new Comparator<TestItem>() {
                    @Override
                    public int compare(TestItem lhs, TestItem rhs) {
                        return lhs.getId() - rhs.getId();
                    }
                });
                break;
            }
            case 2:{
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

package com.test.projectnewsudacity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {
    public NewsAdapter(Context context, List<News> news) {
        super(context, 0, news);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date);
        String date = String.valueOf(currentNews.getDate());

        dateTextView.setText(date);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        titleTextView.setText(currentNews.getTitle());

        TextView sectionNameTextView = (TextView) listItemView.findViewById(R.id.section_name);
        sectionNameTextView.setText(currentNews.getSectionName());

        TextView bylineTextView = (TextView) listItemView.findViewById(R.id.byline);
        bylineTextView.setText(currentNews.getByline());

        return listItemView;
    }
}

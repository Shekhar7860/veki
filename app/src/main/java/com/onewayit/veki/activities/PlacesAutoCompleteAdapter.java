package com.onewayit.veki.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.onewayit.veki.R;
import com.onewayit.veki.utilities.PlaceAPI;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    public ArrayList<String> resultList;

    Context mContext;
    int mResource;

    PlaceAPI mPlaceAPI = new PlaceAPI();

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    resultList = mPlaceAPI.autocomplete(constraint.toString());
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        //if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position != (resultList.size() - 1))
            view = inflater.inflate(R.layout.auto_complete_listitem, null);
        else
            view = inflater.inflate(R.layout.auto_complete_listitem, null);
        //}
        //else {
        //    view = convertView;
        //}
        TextView setonmap = (TextView) view.findViewById(R.id.setonmap);
        TextView autocompleteTextView = (TextView) view.findViewById(R.id.autocompleteText);
            if(position==0){
                setonmap.setVisibility(View.VISIBLE);
                autocompleteTextView.setVisibility(View.GONE);
            }
            else {
                setonmap.setVisibility(View.GONE);
                autocompleteTextView.setVisibility(View.VISIBLE);
                if(resultList.get(position)!=null) {
                    autocompleteTextView.setText(resultList.get(position));
                }
            }
        return view;
    }}
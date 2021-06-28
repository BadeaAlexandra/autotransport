package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultAdapter extends BaseAdapter {

    Activity context;
    ArrayList<Result> resultArrayList;
    private static LayoutInflater inflater = null;
    static TextView priceTextView;

    public ResultAdapter (Activity context, ArrayList<Result> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (resultArrayList.size()<=0) {
            return 1;
        }
        return resultArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        itemView = (itemView == null) ? inflater.inflate(R.layout.list_item,null) : itemView;

        ImageView resultImageView = (ImageView) itemView.findViewById(R.id.resultImageView);
        TextView locationTextView = (TextView) itemView.findViewById(R.id.locationTextView);
        TextView hourTextView = (TextView) itemView.findViewById(R.id.hourTextView);
        TextView companyTextView = (TextView) itemView.findViewById(R.id.companyTextView);
        priceTextView = (TextView) itemView.findViewById(R.id.priceTextView);

        Result selectedResult = resultArrayList.get(position);
        resultImageView.setImageResource(selectedResult.getCompanyImageID());
        locationTextView.setText(selectedResult.getDeparture() + " - " + selectedResult.getArrival());
        hourTextView.setText(selectedResult.getDepartureHour() + " - " + selectedResult.getArrivalHour());
        companyTextView.setText(selectedResult.getCompany());

        if (selectedResult.getCurrency() == "lei") priceTextView.setText(String.format("%.2f", selectedResult.getPrice()) + " lei");
            else priceTextView.setText(String.format("%.2f", selectedResult.getPrice()) + " €");


        return itemView;
    }
}

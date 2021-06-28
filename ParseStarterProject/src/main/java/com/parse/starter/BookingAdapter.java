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

public class BookingAdapter extends BaseAdapter {

    Activity context;
    ArrayList<Booking> bookingArrayList;
    private static LayoutInflater inflater = null;

    static TextView priceTextViewB;

    public BookingAdapter(Activity context, ArrayList<Booking> bookingArrayList) {
        this.context = context;
        this.bookingArrayList = bookingArrayList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (bookingArrayList.size()<=0) {
            return 1;
        }
        return bookingArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookingArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        itemView = (itemView == null) ? inflater.inflate(R.layout.booking_list_item,null) : itemView;

        TextView dateTextViewB = (TextView) itemView.findViewById(R.id.dateTextViewB);
        ImageView resultImageViewB = (ImageView) itemView.findViewById(R.id.resultImageViewB);
        TextView locationTextViewB = (TextView) itemView.findViewById(R.id.locationTextViewB);
        TextView hourTextViewB = (TextView) itemView.findViewById(R.id.hourTextViewB);
        TextView companyTextViewB = (TextView) itemView.findViewById(R.id.companyTextViewB);
        priceTextViewB = (TextView) itemView.findViewById(R.id.priceTextViewB);
        TextView seriesTextViewB = (TextView) itemView.findViewById(R.id.seriesTextViewB);
        TextView issueDateTextViewB = (TextView) itemView.findViewById(R.id.issueDateTextViewB);

        Booking selectedBooking = bookingArrayList.get(position);
        dateTextViewB.setText("Data cursei: " + selectedBooking.getDepartureDate());
        resultImageViewB.setImageResource(selectedBooking.getCompanyImageID());
        locationTextViewB.setText(selectedBooking.getDeparture() + " - " + selectedBooking.getArrival());
        hourTextViewB.setText(selectedBooking.getDepartureHour() + " - " + selectedBooking.getArrivalHour());
        companyTextViewB.setText(selectedBooking.getCompany());

        if (selectedBooking.getCurrency() == "lei") priceTextViewB.setText(String.format("%.2f", selectedBooking.getPrice()) + " lei");
        else priceTextViewB.setText(String.format("%.2f", selectedBooking.getPrice()) + " €");

        seriesTextViewB.setText("Seria biletului: " + selectedBooking.getSeries());
        issueDateTextViewB.setText("Data emiterii: " + selectedBooking.getIssueDate());
        return itemView;
    }
}

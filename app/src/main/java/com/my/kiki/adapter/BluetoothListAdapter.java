package com.my.kiki.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.kiki.R;
import com.my.kiki.model.PairedDevicesModel;

import java.util.ArrayList;

public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.MyViewHolder> {
//    private String [] optionsNameArr;
//    private int [] imagesArr;
//    private List<OptionsData> optionsArr;
    Context context;
    BluetoothSelected bluetoothSelected;
    ArrayList<PairedDevicesModel> arrPairedDevices;

    public interface BluetoothSelected {
        void onBluetoothSelected(int pos);
    }

    public void setBluetoothSelected (BluetoothSelected bluetoothSelected) {
        this.bluetoothSelected = bluetoothSelected;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvBluetoothName,tvBluetoothAddress;
        public ImageView ivBluetoothImage;
        public CardView rootCardLayout;

        public MyViewHolder(View view) {
            super(view);
            tvBluetoothName =  view.findViewById(R.id.tvBluetoothName);
            tvBluetoothAddress =  view.findViewById(R.id.tvBluetoothAddress);
            ivBluetoothImage = view.findViewById(R.id.ivBluetoothImage);
            rootCardLayout = view.findViewById(R.id.rootCardLayout);
        }
    }


    public BluetoothListAdapter(/*List<OptionsData> optionsArr,*//*String [] optionsNameArr, int [] imagesArr,*/ArrayList<PairedDevicesModel> arrPairedDevices, Context context) {
//        this.optionsNameArr = optionsNameArr;
//        this.imagesArr = imagesArr;
        this.arrPairedDevices = arrPairedDevices;
//        this.optionsArr = optionsArr;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_adapter_bluetooth, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        holder.rootCardLayout.setCardBackgroundColor(Color.parseColor("#ffffff"));

        /*if (optionsNameArr[position] != null) {

            if (optionsNameArr[position] != null) {
                if (!optionsNameArr[position].equals("")) {
                    holder.tvBluetoothName.setText(optionsNameArr[position]);
                }
            }

            holder.ivBluetoothImage.setImageResource(imagesArr[position]);

            holder.rootCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothSelected.onBluetoothSelected(position);
                }
            });

        }*/

        if (arrPairedDevices.get(position) != null) {
            if (arrPairedDevices.get(position).getDeviceName() != null && !arrPairedDevices.get(position).getDeviceName().equals("")) {
                holder.tvBluetoothName.setText(arrPairedDevices.get(position).getDeviceName());
            }
            if (arrPairedDevices.get(position).getDeviceAddress() != null && !arrPairedDevices.get(position).getDeviceAddress().equals("")) {
                holder.tvBluetoothAddress.setText(arrPairedDevices.get(position).getDeviceAddress());
            }

            holder.rootCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothSelected.onBluetoothSelected(position);
                }
            });

        }


    }

    @Override
    public int getItemCount() {
        return arrPairedDevices.size();
    }



    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}

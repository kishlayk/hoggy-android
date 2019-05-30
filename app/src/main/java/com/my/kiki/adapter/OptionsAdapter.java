package com.my.kiki.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.my.kiki.R;
import com.my.kiki.utils.Utils;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.MyViewHolder> {
    private String[] optionsNameArr;
    private int [] imagesArr;
    private int [] layoutBgArr;
//    private List<OptionsData> optionsArr;
    Context context;
    OptionsSelected optionsSelected;

    public interface OptionsSelected {
        void onOptionsSelected(int pos);
    }

    public void setOptionsSelected (OptionsSelected optionsSelected) {
        this.optionsSelected = optionsSelected;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvOptionName;
        public ImageView ivOptionImage;
        public CardView rootCardLayout;


        public MyViewHolder(View view) {
            super(view);
            tvOptionName =  view.findViewById(R.id.tvOptionName);
            ivOptionImage = view.findViewById(R.id.ivOptionImage);
            rootCardLayout = view.findViewById(R.id.rootCardLayout);

        }
    }


    public OptionsAdapter(/*List<OptionsData> optionsArr,*/String[] optionsNameArr, int [] imagesArr, int [] layoutBgArr, Context context) {
        this.optionsNameArr = optionsNameArr;
        this.imagesArr = imagesArr;
        this.layoutBgArr = layoutBgArr;
//        this.optionsArr = optionsArr;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_adapter_options, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        holder.rootCardLayout.setCardBackgroundColor(Color.parseColor("#ffffff"));

        if (optionsNameArr[position] != null) {

            if (optionsNameArr[position] != null) {
                if (!optionsNameArr[position].equals("")) {
                    holder.tvOptionName.setText(optionsNameArr[position]);
                }
            }

            holder.ivOptionImage.setImageResource(imagesArr[position]);
            holder.rootCardLayout.setBackgroundResource(R.drawable.gradientdash);

           // holder.rootCardLayout.setBackgroundResource(layoutBgArr[position]);

            holder.rootCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionsSelected.onOptionsSelected(position);
                }
            });



            /*if (Utils.getInstance(context).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                if (Utils.getInstance(context).getString(Utils.PREF_CONNECTED_DEVICE_MAC) != null && !Utils.getInstance(context).getString(Utils.PREF_CONNECTED_DEVICE_MAC).equals("")
                        && Utils.getInstance(context).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(context).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
                    if (position == 0) {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                    }
                }
            }*/

        }


    }

    @Override
    public int getItemCount() {
        return optionsNameArr.length;
    }



    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}

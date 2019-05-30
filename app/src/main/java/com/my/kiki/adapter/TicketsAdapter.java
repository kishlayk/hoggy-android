package com.my.kiki.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.my.kiki.R;
import com.my.kiki.model.TicketsModel;

public class TicketsAdapter extends FirestoreAdapter<TicketsAdapter.ViewHolder> {

    private OnTicketSelectedListener mListener;
    Context context;

    public TicketsAdapter(Query query, OnTicketSelectedListener listener, Context context) {
        super(query);
        mListener = listener;
        this.context = context;
    }

    public interface OnTicketSelectedListener {

        void onTicketSelected(DocumentSnapshot restaurant);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_adapter_tickets, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {


        TextView tvTicketTitle;
        TextView tvTicketDesc;
        TextView tvTicketStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTicketTitle = itemView.findViewById(R.id.tvTicketTitle);
            tvTicketDesc = itemView.findViewById(R.id.tvTicketDesc);
            tvTicketStatus = itemView.findViewById(R.id.tvTicketStatus);

        }



        public void bind(final DocumentSnapshot snapshot,
                         final OnTicketSelectedListener listener) {

            TicketsModel ticketsModel = snapshot.toObject(TicketsModel.class);
            Resources resources = itemView.getResources();



            tvTicketTitle.setText(itemView.getContext().getString(R.string.lbl_title)+": "+ticketsModel.getTicketTitle());
            tvTicketDesc.setText(itemView.getContext().getString(R.string.lbl_desc)+": "+ticketsModel.getTicketDesc());
            if (ticketsModel.getTicketStatus().equals("O")) {
                tvTicketStatus.setText(itemView.getContext().getString(R.string.lbl_status)+": "+itemView.getContext().getString(R.string.lbl_opened));
            } else if (ticketsModel.getTicketStatus().equals("C")) {
                tvTicketStatus.setText(itemView.getContext().getString(R.string.lbl_status)+": "+itemView.getContext().getString(R.string.lbl_closed));
            }



            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onTicketSelected(snapshot);
                    }
                }
            });
        }

    }

}

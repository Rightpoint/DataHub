package com.raizlabs.datacontroller.sample.data.school;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raizlabs.datacontroller.sample.R;
import com.raizlabs.datacontroller.sample.data.RecyclerAdapter;

public class SchoolAdapter extends RecyclerAdapter<School, SchoolAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(ViewHolder.LAYOUT_RESOURCE, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemClicked(viewHolder);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        School school = list.get(position);
        holder.textViewName.setText(school.name);
        holder.textViewEmail.setText("TYPE: " + school.type);
        holder.textViewBody.setText(school.buildingName + "\n" + school.street + "\n" + school.city + ", " + school.state + " - " + school.zipcode);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static final int LAYOUT_RESOURCE = R.layout.list_item_three_fields;

        public TextView textViewName;

        public TextView textViewEmail;

        public TextView textViewBody;

        public ViewHolder(View v) {
            super(v);
            textViewName = (TextView) v.findViewById(R.id.text_title);
            textViewEmail = (TextView) v.findViewById(R.id.text_description);
            textViewBody = (TextView) v.findViewById(R.id.text_body);
        }
    }
}

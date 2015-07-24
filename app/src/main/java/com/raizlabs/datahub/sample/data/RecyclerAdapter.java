package com.raizlabs.datahub.sample.data;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class RecyclerAdapter<Data, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public interface OnItemClickedListener<Data, VH>{
        void onItemClicked(VH viewHolder, int position, Data data);
    }

    protected List<Data> list = new ArrayList<>();

    private HashSet<OnItemClickedListener<Data, VH>> listeners = new HashSet<>();

    public void loadData(List<Data> list){
        if(list == null){
            list = new ArrayList<>();
        }
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public boolean addOnItemClickedListener(OnItemClickedListener<Data, VH> listener){
        return listeners.add(listener);
    }

    public boolean removeOnItemClickedListener(OnItemClickedListener<Data, VH> listener){
        return listeners.remove(listener);
    }

    @SuppressWarnings("unchecked")
    protected void notifyItemClicked(VH viewHolder){
        int position = viewHolder.getAdapterPosition();
        for(OnItemClickedListener<Data, VH> listener : listeners) {
            listener.onItemClicked(viewHolder, position, list.get(position));
        }
    }
}

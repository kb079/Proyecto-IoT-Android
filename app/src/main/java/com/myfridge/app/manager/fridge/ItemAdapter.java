package com.myfridge.app.manager.fridge;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfridge.app.databinding.FridgesItemBinding;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

    private ArrayList<Item> items;

    public ItemAdapter(ArrayList<Item> itemList){
        this.items = itemList;
    }

    public void updateItemList(ArrayList<Item>items){
        this.items = items;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(FridgesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.binding.itemText.setText(items.get(position).getBarCode());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        FridgesItemBinding binding;

        public ItemHolder(FridgesItemBinding b){
            super(b.getRoot());
            binding = b;
        }

    }
}



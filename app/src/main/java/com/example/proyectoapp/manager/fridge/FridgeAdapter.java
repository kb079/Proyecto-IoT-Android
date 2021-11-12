package com.example.proyectoapp.manager.fridge;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoapp.R;

import java.util.ArrayList;

public class FridgeAdapter extends RecyclerView.Adapter<FridgeAdapter.ViewHolderFridges> {

    private ArrayList<Fridge> fridgesList;


    public FridgeAdapter(ArrayList<Fridge> fridgeList){
        this.fridgesList = fridgeList;

    }
    @NonNull
    @Override
    public FridgeAdapter.ViewHolderFridges onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridges_element, null, false);
        return new ViewHolderFridges(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FridgeAdapter.ViewHolderFridges holder, int position) {
        holder.name.setText(fridgesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return fridgesList.size();
    }

    public class ViewHolderFridges extends RecyclerView.ViewHolder {

        TextView name;

        public ViewHolderFridges(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nombre);
        }
    }
}


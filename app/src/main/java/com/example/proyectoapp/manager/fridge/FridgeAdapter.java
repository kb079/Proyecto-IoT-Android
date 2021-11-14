package com.example.proyectoapp.manager.fridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FridgeAdapter extends FirestoreRecyclerAdapter<Fridge, FridgeAdapter.FridgeHolder> {

    public FridgeAdapter(FirestoreRecyclerOptions<Fridge> fridgeList){
        super(fridgeList);
    }

    @Override
    protected void onBindViewHolder(@NonNull FridgeHolder fridgeHolder, int i, @NonNull Fridge fridge) {
        fridgeHolder.name.setText(fridge.getName());
    }

    @NonNull
    @Override
    public FridgeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridges_element, null, false);
        return new FridgeHolder(view);
    }

    public class FridgeHolder extends RecyclerView.ViewHolder {
        TextView name;

        public FridgeHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nombre);
        }
    }
}


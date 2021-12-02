package com.myfridge.app.manager.fridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfridge.app.MainActivity;
import com.myfridge.app.databinding.FridgesElementBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.myfridge.app.R;

public class FridgeAdapter extends FirestoreRecyclerAdapter<Fridge, FridgeAdapter.FridgeHolder> {

    public FridgeAdapter(FirestoreRecyclerOptions<Fridge> fridgeList){
        super(fridgeList);
    }

    @Override
    protected void onBindViewHolder(@NonNull FridgeHolder fridgeHolder, int i, @NonNull Fridge fridge) {
        fridgeHolder.binding.fridgeName.setText(fridge.getName());
        fridgeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("list", fridge.getItems());
                MainActivity.navController.navigate(R.id.nav_contentFridge, bundle);
                //activity.beginTransaction().replace(R.id.nav_host_fragment, a).addToBackStack(null).commit();
            }
        });
    }

    @NonNull
    @Override
    public FridgeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FridgeHolder(FridgesElementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    public class FridgeHolder extends RecyclerView.ViewHolder {
        FridgesElementBinding binding;

        public FridgeHolder(FridgesElementBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}


package com.myfridge.app.manager.fridge;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myfridge.app.MainActivity;
import com.myfridge.app.databinding.FridgesElementBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.myfridge.app.R;

public class FridgeAdapter extends FirestoreRecyclerAdapter<Fridge, FridgeAdapter.FridgeHolder> {

    private int tempID = -1;

    public FridgeAdapter(FirestoreRecyclerOptions<Fridge> fridgeList){
        super(fridgeList);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseFirestore.getInstance().collection("data").document(uidUsuario).collection("fridges").document("fridge" + tempID).delete();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
            }
        }
    };

    @Override
    protected void onBindViewHolder(@NonNull FridgeHolder fridgeHolder, @SuppressLint("RecyclerView") int i, @NonNull Fridge fridge) {
        fridgeHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tempID = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("¿Desea eliminar esta nevera?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();
                return false;
            }
        });

        fridgeHolder.binding.fridgeName.setText(fridge.getName());
        fridgeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("list", fridge.getItems());
                bundle.putInt("fridgeID", fridge.getId());
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
        private FridgesElementBinding binding;

        public FridgeHolder(FridgesElementBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
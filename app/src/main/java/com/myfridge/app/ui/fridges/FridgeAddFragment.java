package com.myfridge.app.ui.fridges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentAddfridgeBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.Location;

import java.util.ArrayList;

public class FridgeAddFragment extends Fragment {

    private FragmentAddfridgeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddfridgeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.botonAnyadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFridge();
            }
        });

        return root;
    }

    public void createFridge(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("data").
                document(uidUsuario).collection("fridges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int fridgeID = task.getResult().size();

                            Fridge fr = new Fridge(fridgeID, binding.fridgeName.getText().toString(), 0, false, new Location(0, 0));
                            fr.setItems(new ArrayList<Item>());

                            db.collection("data").
                                    document(uidUsuario).collection("fridges").
                                    document("fridge" + fridgeID).
                                    set(fr);

                            Toast.makeText(getActivity(), "Nevera creada", Toast.LENGTH_SHORT).show();
                            MainActivity.navController.navigate(R.id.nav_fridgesList);
                        }
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
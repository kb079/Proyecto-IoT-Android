package com.example.proyectoapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.proyectoapp.databinding.FragmentFridgelistBinding;
import com.example.proyectoapp.manager.fridge.Fridge;
import com.example.proyectoapp.manager.fridge.FridgeAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FridgesListFragment extends Fragment {

    private FragmentFridgelistBinding binding;
    private FridgeAdapter adaptador;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFridgelistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initList();

        // TODO: 14/11/2021
        binding.fabAddFridge.setOnClickListener(view ->
                Toast.makeText(view.getContext(), "FUNCIÓN PARA AÑADIR NUEVA NEVERA", Toast.LENGTH_LONG).show()
        );

        return root;
    }


    public void initList(){

        Query query = FirebaseFirestore.getInstance()
                .collection("data/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/fridges/");

        /*
                .orderBy("timestamp")
                .limit(50);
        */

        FirestoreRecyclerOptions<Fridge> options = new FirestoreRecyclerOptions.Builder<Fridge>()
                .setQuery(query, Fridge.class)
                .build();

        adaptador = new FridgeAdapter(options);


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adaptador);
        adaptador.startListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adaptador.stopListening();
    }
}
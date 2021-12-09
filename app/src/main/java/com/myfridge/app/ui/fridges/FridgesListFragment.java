package com.myfridge.app.ui.fridges;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentFridgelistBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.FridgeAdapter;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FridgesListFragment extends Fragment {

    private FragmentFridgelistBinding binding;
    private FridgeAdapter adaptador;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFridgelistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initList();

        return root;
    }

    public void initList(){

        Query query = FirebaseFirestore.getInstance()
                .collection("data/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/fridges/");

        /*
                .orderBy("timestamp")
                .limit(50);
        */

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int totalFridges = value.size();

                //SI SOLO HAY UNA NEVERA TE LLEVA DIRECTAMENTE A TUS ALIMENTOS
                if(totalFridges == 1){
                    MainActivity.navController.navigate(R.id.nav_contentFridge);

                }else if(totalFridges > 0){
                    binding.neveras.setVisibility(View.VISIBLE);
                    binding.sinNeveras.setVisibility(View.INVISIBLE);

                    FirestoreRecyclerOptions<Fridge> options = new FirestoreRecyclerOptions.Builder<Fridge>()
                            .setQuery(query, Fridge.class)
                            .build();
                    adaptador = new FridgeAdapter(options);

                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.recyclerView.setAdapter(adaptador);

                    adaptador.startListening();

                    binding.fabAddFridge.setOnClickListener(view ->
                            openAddFridgeFr()
                    );

                }else{
                    //NO HAY NEVERAS
                    binding.sinNeveras.setVisibility(View.VISIBLE);
                    binding.neveras.setVisibility(View.INVISIBLE);
                    binding.fabAddFridge.setOnClickListener(view ->
                            openAddFridgeFr()
                    );
                }
            }
        });
    }

    public void openAddFridgeFr(){
        MainActivity.navController.navigate(R.id.nav_addFridge);
    }

    @Override
    public void onDestroyView() {
        if(adaptador != null)  adaptador.stopListening();
        super.onDestroyView();
    }
}
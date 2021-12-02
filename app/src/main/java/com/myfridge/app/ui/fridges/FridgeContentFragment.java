package com.myfridge.app.ui.fridges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentContentfridgeBinding;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.ItemAdapter;

import java.util.ArrayList;

public class FridgeContentFragment extends Fragment {

    private FragmentContentfridgeBinding binding;
    private ArrayList<Item> items;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(getArguments() != null) items = (ArrayList<Item>) this.getArguments().getSerializable("list");

        binding = FragmentContentfridgeBinding.inflate(getLayoutInflater(), container, false);
        View root = binding.getRoot();

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                MainActivity.navController.popBackStack(R.id.nav_home, false);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        loadItems();
        return root;
    }

    public void loadItems(){
        ItemAdapter adaptador = new ItemAdapter(items);
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView2.setAdapter(adaptador);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
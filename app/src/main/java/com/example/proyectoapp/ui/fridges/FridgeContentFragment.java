package com.example.proyectoapp.ui.fridges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.proyectoapp.MainActivity;
import com.example.proyectoapp.R;
import com.example.proyectoapp.databinding.FragmentContentfridgeBinding;
import com.example.proyectoapp.ui.home.HomeFragment;

public class FridgeContentFragment extends Fragment {

    private FragmentContentfridgeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentContentfridgeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }





}
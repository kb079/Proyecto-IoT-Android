package com.myfridge.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.myfridge.app.ScannerActivity;
import com.myfridge.app.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String nombreUsuario = usuario.getDisplayName();
        String arr[] = nombreUsuario.split(" ", 2);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.Bienvenida.setText("BIENVENIDO " +arr[0].toUpperCase());

        binding.fab2.setOnClickListener(view ->
                startActivity(new Intent(view.getContext(), ScannerActivity.class))
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.proyectoapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.proyectoapp.MainActivity;
import com.example.proyectoapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String nombreUsuario = usuario.getDisplayName();
        String arr[] = nombreUsuario.split(" ", 2);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.Bienvenida.setText("BIENVENIDO "+arr[0].toUpperCase());
        MainActivity.currentFragment = getParentFragment();
        return root;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
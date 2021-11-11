package com.example.proyectoapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.proyectoapp.LoginActivity;
import com.example.proyectoapp.MainActivity;


import com.example.proyectoapp.databinding.FragmentProfileBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private MainActivity main;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        //TextView proveedores = (TextView) vista.findViewById(R.id.proveedores);
        TextView nombre = binding.nombre;
        TextView email = binding.email;
        //TextView phone = (TextView) vista.findViewById(R.id.phone);
        //TextView uid = (TextView) vista.findViewById(R.id.uid);
        //proveedores.setText(usuario.getProviderData().toString());
        nombre.setText(usuario.getDisplayName());
        email.setText(usuario.getEmail());

        binding.btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AuthUI.getInstance().signOut(main)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent i = new Intent(main, LoginActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                main.finish();
                            }
                        });
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
package com.myfridge.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.myfridge.app.LoginActivity;
import com.myfridge.app.MainActivity;


import com.myfridge.app.databinding.FragmentProfileBinding;
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

//-----------------------------------------------------------------------------------------------//
//--------------------------------------- User Info ---------------------------------------------//

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        TextView nombre = binding.nombre;
        TextView email = binding.email;
        TextView phone = binding.phone;

        nombre.setText(usuario.getDisplayName());
        email.setText(usuario.getEmail());
        String userPhone = usuario.getPhoneNumber();

        if(userPhone == ""){
            phone.setText("No hay ningun número registrado.");
        }
        else{
            phone.setText(userPhone);
        }

//-----------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------//

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
package com.myfridge.app.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myfridge.app.LoginActivity;
import com.myfridge.app.MainActivity;


import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentProfileBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private MainActivity main;


    //---------------------------//
    //-------- User Info --------//

    String USERNAME, USERMAIL, USERPHONE;

    //----------------------------//
    //----------- View -----------//

    TextView nameView, emailView, phoneView;
    EditText nameEdit, emailEdit, phoneEdit;
    FloatingActionButton editButton, acceptButton;



//-----------------------------------------------------------------------------------------------//
//-------------------------------------- OnCreate() ---------------------------------------------//

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        nameView = binding.nombre;
        emailView = binding.email;
        phoneView = binding.phone;
        nameEdit = binding.editNombre;
        emailEdit = binding.editEmail;
        phoneEdit = binding.editPhone;
        editButton = binding.editProfileButton;
        acceptButton = binding.acceptButton;


        //--------------------------------------------------------------------//
        //----------------------- Usuario Info -------------------------------//

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        USERNAME = usuario.getDisplayName();
        USERMAIL = usuario.getEmail();

        if(usuario.getPhoneNumber() == null){
            USERPHONE = "";
        }else{
            USERPHONE = usuario.getPhoneNumber();
        }

        mostrarInfoUsuario();

        //-------------------------------------------------------------------------//
        //--------------------------- Editar Info ---------------------------------//


        //activar Modo EditarPerfil
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Configuración de la Visibilidad de las vistas
                editButton.setVisibility(View.GONE); nameView.setVisibility(View.GONE);
                //emailView.setVisibility(View.GONE); phoneView.setVisibility(View.GONE);

                acceptButton.setVisibility(View.VISIBLE);nameEdit.setVisibility(View.VISIBLE);
                //emailEdit.setVisibility(View.VISIBLE); phoneEdit.setVisibility(View.VISIBLE);


                //Datos previos en los EditText
                nameEdit.setText(USERNAME, EditText.BufferType.EDITABLE);
                /*emailEdit.setText(USERMAIL, EditText.BufferType.EDITABLE);

                if(USERPHONE.length() == 0){
                    phoneEdit.setText("");
                }else{
                    phoneEdit.setText(USERPHONE);
                }*/
            }
        });

        //Editar el Perfil del Usuario
        acceptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Configuración de la Visibilidad de las vistas
                editButton.setVisibility(View.VISIBLE); nameView.setVisibility(View.VISIBLE);
                //emailView.setVisibility(View.VISIBLE); phoneView.setVisibility(View.VISIBLE);

                acceptButton.setVisibility(View.GONE);nameEdit.setVisibility(View.GONE);
                //emailEdit.setVisibility(View.GONE); phoneEdit.setVisibility(View.GONE);

                //Valores introducidos en el EditText
                String newName = nameEdit.getText().toString();
                //String newEmail = emailEdit.getText().toString();
                //String newPhone = phoneEdit.getText().toString();

                try {

                    //Cambio de nombre en Firebase
                    UserProfileChangeRequest perfil = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName).build();
                    usuario.updateProfile(perfil);

                    //Cambio de email en Firebase
                    /*
                    AuthCredential credential = EmailAuthProvider
                           .getCredential(USERMAIL, "AdR14t0R");

                    usuario.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    usuario.updateEmail(newEmail);
                                }
                            });
                    */
                    //Cambio de teléfono en Firebase

                    //Actialización de la vista
                    if(!USERNAME.equals(newName)){

                        //Actualización del nombre
                        USERNAME = newName;
                        nameView.setText(newName);

                        //Actualización del email

                        //Actualización del teléfono

                        //Aviso de actualización
                        Toast.makeText(getView().getContext(), "Se ha actualizado con éxito",
                                Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    Toast.makeText(getView().getContext(), "Introduce correctamente los datos",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        return root;
    }

//-----------------------------------------------------------------------------------------------//
//--------------------------------------- Draw Info ---------------------------------------------//

    private void mostrarInfoUsuario(){

        //Nombre
        nameView.setText(USERNAME);

        //Email
        emailView.setText(USERMAIL);

        //Telefono
        if(USERPHONE.length() == 0){
            phoneView.setText("Sin número de teléfono.");
        }
        else{
            phoneView.setText(USERPHONE);
        }
    }


//-----------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------//

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

//-----------------------------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------//


}
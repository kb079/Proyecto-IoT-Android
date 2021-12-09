package com.example.porteroautomatico;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storageRef = FirebaseStorage.getInstance().getReference();
        Button b = findViewById(R.id.button_upload);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, 1234);
            }
        });
        bajarFichero();

    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1234) {
                subirFichero(data.getData(), "imagenes/imagen");
            }
        }
    }

    private void subirFichero(Uri fichero, String referencia) {
        StorageReference ficheroRef = storageRef.child(referencia);
        ficheroRef.putFile(fichero)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("Almacenamiento", "Fichero subido");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Almacenamiento", "ERROR: subiendo fichero");
                    }
                });
    }

    private void bajarFichero() {
        File localFile = null;
        try {
            localFile = File.createTempFile("image", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String path = localFile.getAbsolutePath();
        Log.d("Almacenamiento", "creando fichero: " + path);
        StorageReference ficheroRef = storageRef.child("imagenes/imagen");
        ficheroRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>(){
                                                  @Override
                                                  public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot){
                                                      Log.d("Almacenamiento", "Fichero bajado");
                                                      ImageView imageView = findViewById(R.id.imageView);
                                                      imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                                                  }
                                              }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("Almacenamiento", "ERROR: bajando fichero");
            }
        });
    }

}
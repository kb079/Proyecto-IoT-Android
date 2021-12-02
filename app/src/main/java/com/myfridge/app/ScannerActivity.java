package com.myfridge.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.Result;

import java.util.Map;

import pl.coderion.model.Product;
import pl.coderion.model.ProductResponse;
import pl.coderion.service.OpenFoodFactsWrapper;
import pl.coderion.service.impl.OpenFoodFactsWrapperImpl;


public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private Activity actividad = this;
    FirebaseFirestore db;
    ListenerRegistration registration;
    Map<String, Object> nevera;
    Map<String, String> productos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_cod_barras);



        db = FirebaseFirestore.getInstance();
        registration = db.collection("data").document("fridge0").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                        @Nullable FirebaseFirestoreException e){
                    if (e != null) {
                        Log.e("Firestore", "Error al leer", e);
                    } else if (snapshot == null || !snapshot.exists()) {
                        Log.e("Firestore", "Error: documento no encontrado ");
                    } else {
                        Log.d("Firestore", "datos:" + snapshot.getData().get("productos"));
                        nevera = (Map) snapshot.getData();
                        productos = (Map) snapshot.getData().get("productos");
                    }
                }
            });
        //db.collection("fridges").document("fridge0").get("productos");


        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        OpenFoodFactsWrapper wrapper = new OpenFoodFactsWrapperImpl();
                        ProductResponse productResponse = wrapper.fetchProductByCode(result.getText());

                        if (!productResponse.isStatus()) {
                            Log.e("TAG", "Status: " + productResponse.getStatusVerbose());
                            return;
                        }

                        Product product = productResponse.getProduct();

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        productos.put(String.valueOf(productos.size()),result.getText());
                                        Toast.makeText(ScannerActivity.this, "El producto se añadirá a su nevera al salir del escáner", Toast.LENGTH_SHORT).show();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Toast.makeText(ScannerActivity.this, "Operación cancelada", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(actividad);
                        builder.setMessage("¿Desea añadir el siguiente producto a su nevera?\n\n"+product.getProductName()).setPositiveButton("Continuar", dialogClickListener)
                                .setNegativeButton("Cancelar", dialogClickListener).show();

                        /*Intent i = new Intent(actividad, AnyadirProductoActivity.class);
                        i.putExtra("nombreProducto", product.getProductName());
                        actividad.startActivity(i);*/

                        /*Log.e("TAG", "Name: " + product.getProductName());
                        Log.e("TAG", "Generic name: " + product.getGenericName());
                        Log.e("TAG", "Product code: " + product.getCode());*/
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        nevera.put("productos", productos);
        db.collection("data").document("fridge0").set(nevera);
        mCodeScanner.releaseResources();
        super.onPause();
    }
}

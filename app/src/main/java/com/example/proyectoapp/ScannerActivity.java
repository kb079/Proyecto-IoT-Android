package com.example.proyectoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import pl.coderion.model.Product;
import pl.coderion.model.ProductResponse;
import pl.coderion.service.OpenFoodFactsWrapper;
import pl.coderion.service.impl.OpenFoodFactsWrapperImpl;


public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private Activity actividad = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_cod_barras);
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
                                        Toast.makeText(ScannerActivity.this, "El producto ha sido añadido correctamente a su nevera", Toast.LENGTH_SHORT).show();
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
        mCodeScanner.releaseResources();
        super.onPause();
    }
}

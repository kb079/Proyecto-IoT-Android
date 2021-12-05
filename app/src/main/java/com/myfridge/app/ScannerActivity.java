package com.myfridge.app;

import static com.myfridge.app.MainActivity.binding;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.Result;
import com.myfridge.app.databinding.UiAnyadirProductoBinding;
import com.myfridge.app.manager.fridge.Item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pl.coderion.model.Product;
import pl.coderion.model.ProductResponse;
import pl.coderion.service.OpenFoodFactsWrapper;
import pl.coderion.service.impl.OpenFoodFactsWrapperImpl;





public class ScannerActivity extends AppCompatActivity {

    private UiAnyadirProductoBinding binding;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    View root;
    Dialog dialog;

    private CodeScanner mCodeScanner;
    private Activity actividad = this;
    FirebaseFirestore db;
    ListenerRegistration registration;
    Map<String, Object> nevera;
    ArrayList<Item> productos;
    FirebaseUser usuario;
    String uidUsuario;
    Long expDate = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_cod_barras);



        usuario = FirebaseAuth.getInstance().getCurrentUser();
        uidUsuario = usuario.getUid();
        Log.e("UID",uidUsuario);



        db = FirebaseFirestore.getInstance();
        registration = db.collection("data").document(uidUsuario).collection("fridges").document("koszTIbIHuyKYM9U8i0L").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e){
                if (e != null) {
                    Log.e("Firestore", "Error al leer", e);
                } else if (snapshot == null || !snapshot.exists()) {
                    Log.e("Firestore", "Error: documento no encontrado ");
                } else {
                    Log.d("Firestore", "datos:" + snapshot.getData().get("productos"));
                    nevera = snapshot.getData();
                    productos = (ArrayList<Item>) snapshot.getData().get("items");
                }
            }
        });

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

                        binding = UiAnyadirProductoBinding.inflate(getLayoutInflater());
                        root = binding.getRoot();

                        new FetchImage(product.getImageFrontUrl()).start();
                        binding.nombreProductoTxt.setText(product.getProductName());
                        binding.acceptBtn.setOnClickListener(view ->
                                anyadirProducto(result)
                        );
                        binding.cancelBtn.setOnClickListener(view ->
                                cancelarOperacion()
                        );
                        binding.plusBtn.setOnClickListener(view -> {
                            Integer nuevaCantidad = Integer.parseInt(binding.itemCountTxt.getText().toString()) + 1;
                            binding.itemCountTxt.setText(nuevaCantidad.toString());
                        });
                        binding.minusBtn.setOnClickListener(view -> {
                            Integer nuevaCantidad = 0;
                            if (Integer.parseInt(binding.itemCountTxt.getText().toString()) > 1) {
                                nuevaCantidad = Integer.parseInt(binding.itemCountTxt.getText().toString()) - 1;
                            }
                            binding.itemCountTxt.setText(nuevaCantidad.toString());
                        });
                        binding.fechaCaducidadEditTxt.setInputType(InputType.TYPE_NULL);
                        binding.fechaCaducidadEditTxt.setOnClickListener(view -> {
                            final Calendar cldr = Calendar.getInstance();
                            int day = cldr.get(Calendar.DAY_OF_MONTH);
                            int month = cldr.get(Calendar.MONTH);
                            int year = cldr.get(Calendar.YEAR);
                            // date picker dialog
                            DatePickerDialog picker = new DatePickerDialog(view.getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                            binding.fechaCaducidadEditTxt.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                            try {
                                                expDate= format.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year).getTime();
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    }, year, month, day);
                            picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            picker.show();
                            /*DatePicker simpleDatePicker = (DatePicker)findViewById(R.id.datePicker1); // initiate a date picker

                            simpleDatePicker.setSpinnersShown(false); // set false value for the spinner shown function*/
                        });



                        showCustomDialog();
                        Log.e("IMAGENPRODUCTO", wrapper.fetchProductByCode(result.getText()).getProduct().getImageFrontUrl());
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
        nevera.put("items", productos);
        db.collection("data").document(uidUsuario).collection("fridges").document("koszTIbIHuyKYM9U8i0L").set(nevera);
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void anyadirProducto(Result result){
        /*for( int i= 0;i<productos.size();i++){
            if(productos.get(i).getBarCode().equals(result.getText()) && productos.get(i).getExpDate()==expDate){
                productos.set(i, new Item(result.getText(),productos.get(i).getQty() + Integer.parseInt(binding.itemCountTxt.getText().toString()), expDate));
            }
            else{
                productos.add(new Item(result.getText(), Integer.parseInt(binding.itemCountTxt.getText().toString()), expDate));
            }
        }*/

        productos.add(new Item(result.getText(), Integer.parseInt(binding.itemCountTxt.getText().toString()), expDate));
        Toast.makeText(ScannerActivity.this, "El producto se añadirá a su nevera al salir del escáner", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        mCodeScanner.startPreview();
    }

    private void cancelarOperacion(){
        Toast.makeText(ScannerActivity.this, "Operación cancelada", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        mCodeScanner.startPreview();
    }

    void showCustomDialog(){
        dialog = new Dialog(actividad);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(root);
        dialog.show();
    }

    class FetchImage extends Thread{

        String URL;
        Bitmap bitmap;

        FetchImage(String URL){

            this.URL = URL;

        }

        @Override
        public void run() {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(actividad);
                    progressDialog.setMessage("Cargando información sobre el producto...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            InputStream inputStream = null;
            try {
                inputStream = new URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    binding.imagenProductoImg.setImageBitmap(bitmap);

                }
            });




        }
    }
}
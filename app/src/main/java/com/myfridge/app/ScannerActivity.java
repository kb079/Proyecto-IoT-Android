package com.myfridge.app;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.myfridge.app.databinding.UiAnyadirProductoBinding;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.ui.fridges.FridgeContentFragment;
import com.myfridge.app.utils.ProductModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScannerActivity extends AppCompatActivity {

    private UiAnyadirProductoBinding binding;

    private View root;
    private Dialog dialog;

    private CodeScanner mCodeScanner;
    private Long expDate = 0L;

    private ArrayList<Item> items;
    private int fridgeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escaner_cod_barras);

        items = (ArrayList<Item>) getIntent().getSerializableExtra("fridgeItems");
        fridgeID = getIntent().getIntExtra("fridgeID", -1);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProductModel product = new ProductModel(result.getText());

                        binding = UiAnyadirProductoBinding.inflate(getLayoutInflater());
                        root = binding.getRoot();

                        if(product.getProduct() == null){
                            Toast.makeText(getApplicationContext(), "Error al leer el producto, inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        product.registerProduct();

                        Glide.with(getApplicationContext())
                                .load(product.getProductPhotoURL())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(binding.imagenProductoImg);

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
                            Integer nuevaCantidad = 1;
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
                                                expDate = format.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year).getTime();
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, year, month, day);
                            picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            picker.show();
                        });

                        showCustomDialog();
                        //Log.e("IMAGENPRODUCTO", wrapper.fetchProductByCode(result.getText()).getProduct().getImageFrontUrl());
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
    @Override
    protected void onDestroy() {
        mCodeScanner.releaseResources();
        super.onDestroy();
    }

    private void anyadirProducto(Result result){
        String barcode = result.getText();
        boolean found = false;
        int qty = Integer.parseInt(binding.itemCountTxt.getText().toString());

        for (Item item: items) {
            if(item.getBarCode().equals(barcode)){
                item.setQty(item.getQty()+qty);
                found = true;
            }
        }

        if(!found){
            items.add(new Item(barcode, qty, expDate));
        }

        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("data").document(uidUsuario).collection("fridges").document("fridge" + fridgeID).update("items", items);

        Toast.makeText(getApplicationContext(), "¡El producto ha sido añado a tu nevera!", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        mCodeScanner.startPreview();
    }

    private void cancelarOperacion(){
        Toast.makeText(ScannerActivity.this, "Operación cancelada", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        mCodeScanner.startPreview();
    }

    void showCustomDialog(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(root);
        dialog.show();
    }
}
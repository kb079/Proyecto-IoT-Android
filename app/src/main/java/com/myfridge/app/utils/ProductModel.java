package com.myfridge.app.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.coderion.model.Product;
import pl.coderion.model.ProductResponse;
import pl.coderion.service.OpenFoodFactsWrapper;
import pl.coderion.service.impl.OpenFoodFactsWrapperImpl;

public class ProductModel {

    private String barCode;
    private Product product;
    private boolean taskDone = false;

    private OpenFoodFactsWrapper wrapper = new OpenFoodFactsWrapperImpl();

    public ProductModel(String barcode){
        this.barCode = barcode.trim();

        ProductResponse productResponse;
        try{
            productResponse = wrapper.fetchProductByCode(barCode);

            if (!productResponse.isStatus()) {
                Log.e("TAG", "Status: " + productResponse.getStatusVerbose());
                return;
            }
            product = productResponse.getProduct();

        }catch (Exception e){
            System.out.println(e);
            product = null;
        }finally {
            taskDone = true;
        }
    }

    public Product getProduct(){
        return product;
    }

    public String getProductPhotoURL(){
       return product.getImageUrl();
    }

    public String getProductName(){
        return product.getProductName();
    }

    public String getBrandName(){
        return product.getBrands();
    }

    public String getNutriscore(){
        if(product.getNutritionGrades() == null) return "";
        return product.getNutritionGrades();
    }

    public boolean isDone(){
        return this.taskDone;
    }

    public void registerProduct(){
        if(product == null) return;

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("food").document(barCode);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        docRef.set(new SavedItem(getProductName(), getBrandName(), getProductPhotoURL(), getNutriscore()));
                    }
                }
            }
        });
    }
}

package com.myfridge.app.ui.fridges;

import static com.myfridge.app.utils.Utils.parseData;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentAddfridgeBinding;
import com.myfridge.app.databinding.FragmentSingleItemInfoBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.ItemAdapter;
import com.myfridge.app.manager.fridge.Location;
import com.myfridge.app.utils.SavedItem;

import java.util.ArrayList;

public class SingleItemFragment extends Fragment {

    private FragmentSingleItemInfoBinding binding;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SavedItem itemInfo;
        Item item;


        if (getArguments() != null) {
            itemInfo = (SavedItem) this.getArguments().getSerializable("itemInfo");
            item = (Item) this.getArguments().getSerializable("item");
        }else{
            itemInfo = new SavedItem();
            item = new Item();
        }

        binding = FragmentSingleItemInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Glide.with(getContext()).load(itemInfo.getPhotoURL())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.imagenProducto);

        binding.productName.setText(itemInfo.getName());
        binding.productBrand.setText(itemInfo.getBrand());
        parseNutriscore(itemInfo.getNutriscore());
        binding.quantity.setText("" + item.getQty());


        if(item.getExpDate() == 0){
            binding.date.setText("Sin fecha de caducidad");
        } else{
            binding.date.setText(parseData(item.getExpDate()));
        }



        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void parseNutriscore(String score){

        if(score == null) return;

        int color = R.color.gray;

        switch(score){
            case "a":
                color = R.color.scoreA;
                break;
            case "b":
                color = R.color.scoreB;
                break;
            case "c":
                color = R.color.scoreC;
                break;
            case "d":
                color = R.color.scoreD;
                break;
            case "e":
                color = R.color.scoreE;
                break;
        }
        binding.nutriscore2.setBackgroundTintList(getContext().getResources().getColorStateList(color));


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}

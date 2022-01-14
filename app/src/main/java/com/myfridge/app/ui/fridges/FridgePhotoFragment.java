package com.myfridge.app.ui.fridges;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FragmentPhotofridgeBinding;
import com.myfridge.app.manager.fridge.FridgePhoto;
import com.myfridge.app.utils.Utils;


public class FridgePhotoFragment extends Fragment {

    private FragmentPhotofridgeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPhotofridgeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("foto")
                .orderBy("tiempo", Query.Direction.DESCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()) {
                    FridgePhoto photo = task.getResult().getDocuments().get(0).toObject(FridgePhoto.class);
                    Log.i("FridgePhoto",photo.getUrl());

                    Glide.with(getContext()).load(photo.getUrl())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.imageView);

                    binding.fridgeName.setText(Utils.parseData(photo.getTiempo()));
                }
            }
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
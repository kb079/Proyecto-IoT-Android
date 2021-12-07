package com.myfridge.app.manager.fridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myfridge.app.R;
import com.myfridge.app.databinding.FridgesItemBinding;
import com.myfridge.app.utils.SavedItem;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

    private ArrayList<Item> items;
    private Context context;
    private ViewGroup parent;

    public ItemAdapter(ArrayList<Item> itemList, Context c) {
        this.items = itemList;
        if(itemList == null){
            this.items = new ArrayList<Item>();
        }
        this.context = c;
    }

    public void updateItemList(ArrayList<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        return new ItemHolder(FridgesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item = items.get(position);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("food").document(item.getBarCode().trim());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        SavedItem itemInfo = document.toObject(SavedItem.class);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                Glide.with((context)).load(itemInfo.getPhotoURL())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(holder.binding.itemPhoto);

                                holder.binding.itemName.setText(itemInfo.getName());
                                holder.binding.itemBrand.setText(itemInfo.getBrand());
                                holder.binding.itemName4.setText(itemInfo.getNutriscore());
                                if(item.getExpDate() == 0){
                                    holder.binding.itemExpDate.setText("???");
                                }else{
                                    holder.binding.itemExpDate.setText(parseData(item.getExpDate()));
                                }

                                holder.binding.itemQty.setText("" + item.getQty());
                            }
                        });

                    }else {
                        parent.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                    }
                }else {
                    parent.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String parseData(long time){
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date currenTimeZone = new Date(time * 1000);

        return sdf.format(currenTimeZone);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        FridgesItemBinding binding;

        public ItemHolder(FridgesItemBinding b) {
            super(b.getRoot());
            binding = b;
        }

    }
}



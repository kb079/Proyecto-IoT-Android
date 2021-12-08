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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import java.util.Collection;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> implements Filterable {

    private ArrayList<Item> itemsAll;
    private ArrayList<Item> items;

    private ArrayList<SavedItem> products;
    private Context context;
    private ViewGroup parent;

    private boolean filtering;

    public ItemAdapter(ArrayList<Item> itemList, Context c) {
        this.items = itemList;
        if(itemList == null){
            this.items = new ArrayList<Item>();
        }

        itemsAll = new ArrayList<Item>();
        itemsAll.addAll(items);
        filtering = false;

        this.context = c;
        this.products = new ArrayList<SavedItem>();
    }

    public void updateItemList(ArrayList<Item> items) {
        this.items.clear();
        this.items.addAll(items);
        this.itemsAll.clear();
        this.itemsAll.addAll(items);
        filtering = false;
        notifyUpdate();
    }

    private void notifyUpdate(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
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

        if(filtering){
            for (SavedItem product: products) {
                if(product.getBarCode() == item.getBarCode()){
                    drawInfo(holder, product, item);
                    return;
                }
            }
            return;
        }

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("food").document(item.getBarCode().trim());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        SavedItem itemInfo = document.toObject(SavedItem.class);
                        itemInfo.setBarCode(item.getBarCode());
                        products.add(itemInfo);

                        drawInfo(holder, itemInfo, item);


                }else {
                    parent.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                }
        }}});
    }

    private void drawInfo(ItemHolder holder, SavedItem itemInfo, Item item){
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
                holder.binding.group.setVisibility(View.VISIBLE);
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

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(!filtering) filtering = true;

            ArrayList<Item> filteredList = new ArrayList<>();
            final String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();

            if (filterPattern.isEmpty() || filterPattern.length() == 0) {
                filteredList.addAll(itemsAll);
            } else {
                for (SavedItem product : products) {
                    if (product.getName().toLowerCase().contains(filterPattern)) {
                        for(Item item : itemsAll){
                            if(item.getBarCode() == product.getBarCode()){
                                filteredList.add(item);
                                break;
                            }
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            items.addAll((Collection<? extends Item>) results.values);
            notifyUpdate();
        }
    };

    public class ItemHolder extends RecyclerView.ViewHolder {
        private FridgesItemBinding binding;

        public ItemHolder(FridgesItemBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}



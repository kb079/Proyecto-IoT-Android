package com.myfridge.app.manager.fridge;

import static com.myfridge.app.utils.Utils.parseData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.myfridge.app.MainActivity;
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

    private int fridgeID = 0;

    private boolean filtering;

    //Selecionar y eliminar ITEMS
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<Item> selectList = new ArrayList<>();

    ItemViewModel itemViewModel;


    public ItemAdapter(ArrayList<Item> itemList, Context c, int fridgeID) {
        this.fridgeID = fridgeID;
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


    public void updateItemList(ArrayList<Item> items2) {
        this.items.clear();
        this.items.addAll(items2);
        this.itemsAll.clear();
        this.itemsAll.addAll(items2);
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

        //Selecionar y eliminar ITEMS
        itemViewModel = ViewModelProviders.of((FragmentActivity) context).get(ItemViewModel.class);

        return new ItemHolder(FridgesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item = items.get(position);



        //---------------------------------------------------------------------------------------//
        //--------------------------------- Selecionar ITEMS ------------------------------------//

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                if(!isEnable){
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                            MenuInflater menuInflater = actionMode.getMenuInflater();
                            menuInflater.inflate(R.menu.delete_view, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                            isEnable = true;
                            ClickItem(holder);
                            itemViewModel.getText().observe((LifecycleOwner) context
                                    , new Observer<String>() {
                                        @Override
                                        public void onChanged(String s) {
                                            actionMode.setTitle(String.format("%s Seleccionados", s));
                                        }
                                    });
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                            int id = menuItem.getItemId();

                            switch (id){

                                //Al pulsar eliminar
                                case R.id.app_bar_delete:
                                    for(Item item : selectList ){
                                        items.remove(item);
                                    }
                                    actionMode.finish();

                                    //REMOVE ITEMS FROM DB
                                    String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    FirebaseFirestore.getInstance().collection("data").document(uidUsuario).collection("fridges").document("fridge" + fridgeID).update("items", items);
                                    updateItemList(items); //UPDATE FOR FILTERING
                                    //

                                    Toast.makeText(context, "Se han eliminado los alimentos",
                                            Toast.LENGTH_SHORT).show();
                                    break;

                                //Al pulsar selecionar todos
                                case R.id.app_bar_select_all:
                                    if (selectList.size() == items.size()){
                                        isSelectAll = false;
                                        selectList.clear();
                                    }else{
                                        isSelectAll = true;
                                        selectList.clear();
                                        selectList.addAll(items);
                                    }
                                    itemViewModel.setText(String.valueOf(selectList.size()));
                                    notifyDataSetChanged();
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode actionMode) {
                            isEnable = false;
                            isSelectAll = false;
                            selectList.clear();
                            notifyDataSetChanged();
                        }
                    };
                    ((AppCompatActivity) view.getContext()).startSupportActionMode( callback );
                }else{
                    ClickItem(holder);
                }
                return true;
            }
        });

        holder.itemView.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(isEnable){
                    ClickItem(holder);
                }
            }
        });

        if (isSelectAll){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.WHITE);
        }


        //---------------------------------------------------------------------------------------//
        //---------------------------------------------------------------------------------------//


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

    //-------------------------------------------------------------------------------------------//
    //----------------------------- Selecionar y eliminar ITEMS ---------------------------------//


    private void ClickItem(ItemHolder holder) {
        Item item = items.get(holder.getAdapterPosition());


        if(holder.checkBox.getVisibility() == View.GONE){
            //Item no seleccionado
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            selectList.add(item);

        }else{
            //Item seleccionado
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.WHITE);
            selectList.remove(item);
        }

        itemViewModel.setText(String.valueOf(selectList.size()));
    }


    /*public void eliminarProducto(Result result){

        //Codigo de barra
        String barcode = result.getText();
        //ID del usuario
        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //ID de la nevera
        int fridgeID = 0;

        FirebaseFirestore.getInstance().collection("data").document(uidUsuario).collection("fridges").document("fridge" + fridgeID);

                //.document("items", barcode);

        Toast.makeText(context, "Eliminado de tu nevera", Toast.LENGTH_SHORT).show();
    }*/



    //---------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------//

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

                parseNutriscore(holder, itemInfo.getNutriscore());
                if(item.getExpDate() == 0){
                    holder.binding.itemExpDate.setText("Sin fecha de caducidad");
                }else{
                    holder.binding.itemExpDate.setText(parseData(item.getExpDate(), false));
                }

                holder.binding.itemQty.setText("" + item.getQty());
                //holder.binding.group.setVisibility(View.VISIBLE);
            }
        });
        //---------------------------------------------------------------------------------------//
        //--------------------------------- Visualizar ITEMS ------------------------------------//

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("itemInfo", itemInfo);
                bundle.putSerializable("item", item);
                MainActivity.navController.navigate(R.id.nav_singleItem, bundle);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void parseNutriscore(ItemHolder holder, String score){

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
        holder.binding.nutriscore.setBackgroundTintList(context.getResources().getColorStateList(color));


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
        ImageView checkBox;

        public ItemHolder(FridgesItemBinding b) {
            super(b.getRoot());
            binding = b;
            checkBox = b.getRoot().findViewById(R.id.check_box);
        }
    }
}


package com.myfridge.app.ui.fridges;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.ScannerActivity;
import com.myfridge.app.databinding.FragmentContentfridgeBinding;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.ItemAdapter;

import java.util.ArrayList;

public class FridgeContentFragment extends Fragment {

    private FragmentContentfridgeBinding binding;
    private static ArrayList<Item> items;
    private static ItemAdapter adaptador;

    private SearchView searchBar;

    private int fridgeID;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openScanner();
                } else {
                    Toast.makeText(getActivity(), "¡Debes dar permisos para poder escanear un producto!", Toast.LENGTH_SHORT).show();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null){
            items = (ArrayList<Item>) this.getArguments().getSerializable("list");
            fridgeID = this.getArguments().getInt("fridgeID");
        }

        binding = FragmentContentfridgeBinding.inflate(getLayoutInflater(), container, false);
        View root = binding.getRoot();

        binding.fabScannerAddItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }else{
                    openScanner();
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MainActivity.navController.popBackStack(R.id.nav_home, false);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        loadItems();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view, menu);

        MenuItem searchViewItem
                = menu.findItem(R.id.app_bar_search);
        searchBar = (SearchView) searchViewItem.getActionView();
        searchBar.setQueryHint("Buscar alimentos...");
        searchBar.setIconified(true);
        searchBar.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query)
                    {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        adaptador.getFilter().filter(newText);

                        return false;
                    }
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public static void updateRecyclerItems(ArrayList<Item> items2){
        items = items2;
        adaptador.updateItemList(items);
    }

    public void loadItems(){
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptador = new ItemAdapter(items, getContext());

        binding.recyclerView2.setAdapter(adaptador);
    }

    private void openScanner(){
        Intent intent = new Intent(getContext(), ScannerActivity.class);
        intent.putExtra("fridgeItems", items);
        intent.putExtra("fridgeID", fridgeID);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
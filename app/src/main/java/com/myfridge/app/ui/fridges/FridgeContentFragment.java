package com.myfridge.app.ui.fridges;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.myfridge.app.MainActivity;
import com.myfridge.app.R;
import com.myfridge.app.ScannerActivity;
import com.myfridge.app.databinding.FragmentContentfridgeBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.ItemAdapter;

import java.util.ArrayList;

public class FridgeContentFragment extends Fragment {

    private FragmentContentfridgeBinding binding;
    private ArrayList<Item> items;
    private ItemAdapter adaptador;

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

        if (getArguments() != null) {
            items = (ArrayList<Item>) this.getArguments().getSerializable("list");
            fridgeID = this.getArguments().getInt("fridgeID");
        }

        binding = FragmentContentfridgeBinding.inflate(getLayoutInflater(), container, false);
        View root = binding.getRoot();

        binding.fabScannerAddItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                } else {
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
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adaptador.getFilter().filter(newText);
                        return false;
                    }
                });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void updateRecyclerItems(ArrayList<Item> items2) {
        items.clear();
        items.addAll(items2);
        adaptador.updateItemList(new ArrayList<>(items));
    }

    public void loadItems() {
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptador = new ItemAdapter(items, getContext());

        binding.recyclerView2.setAdapter(adaptador);

        //UPDATE RECYCLERVIEW IF FIELD WAS UPTADED IN DB
        final DocumentReference docRef = FirebaseFirestore.getInstance().collection("data").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("fridges").document("fridge" + fridgeID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    items.clear();
                    items.addAll(snapshot.toObject(Fridge.class).getItems());
                    updateRecyclerItems(new ArrayList<>(items));
                }
            }
        });
    }

    private void openScanner() {
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
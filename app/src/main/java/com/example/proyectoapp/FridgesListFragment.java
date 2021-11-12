package com.example.proyectoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectoapp.databinding.ActivityMainBinding;
import com.example.proyectoapp.databinding.FragmentFridgelistBinding;
import com.example.proyectoapp.manager.fridge.Fridge;
import com.example.proyectoapp.manager.fridge.FridgeAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FridgesListFragment extends AppCompatActivity {

    private RecyclerView rw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_fridgelist);
        rw = findViewById(R.id.recyclerView);
        rw.setLayoutManager(new LinearLayoutManager(this));

        initList();



    }


    /*
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFridgelistBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        initList();

        return root;
    }
*/

    public void initList(){

        ArrayList<Fridge> list = new ArrayList<>();

        /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                List<DownloadInfo> downloadInfoList = task.getResult().toObjects(DownloadInfo.class);
                                for (DownloadInfo downloadInfo : downloadInfoList) {
                                    doSomething(downloadInfo.file_name, downloadInfo.id, downloadInfo.size);
                                }
                            }
                        }
                    } else {
                        Log.w("TAG", "Error getting documents.", task.getException());
                    }
                }
    });*/


        //get data
        list.add(new Fridge("test", 20, false, new Location(50.41, 57.20)));
        list.add(new Fridge("test222", 20, false, new Location(50.41, 57.20)));

        FridgeAdapter adaptador = new FridgeAdapter(list);

        rw.setAdapter(adaptador);

    }



}
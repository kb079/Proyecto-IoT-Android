package com.myfridge.app;

import android.os.Bundle;
import android.view.View;

import com.myfridge.app.databinding.ActivityMainBinding;
import com.myfridge.app.manager.fridge.Fridge;
import com.myfridge.app.manager.fridge.Item;
import com.myfridge.app.manager.fridge.Location;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static ActivityMainBinding binding;
    public static NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.appBarMain2.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_about, R.id.nav_log_out)
                .setOpenableLayout(drawer)
                .build();

        // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController = findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

       // test("fridge0");
        //test("fridge1");

    }

    public void test(String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("data/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/fridges/").document(id);

        Fridge a = new Fridge(1, "test", 5, true, new Location(5.5, 5.6));
        ArrayList<Item> b = new ArrayList<Item>();
        /*
        b.add(new Item("555", 5, new Date().getTime()));
        b.add(new Item("test 2", 4, new Date().getTime()));
        b.add(new Item("test 3", 2, new Date().getTime()));
         */
        a.setItems(b);

        docRef.set(a);
    }

    public NavController findNavController(@NonNull Fragment fragment) {
        Fragment findFragment = fragment;
        while (findFragment != null) {
            if (findFragment instanceof NavHostFragment) {
                return ((NavHostFragment) findFragment).getNavController();
            }
            Fragment primaryNavFragment = findFragment.requireFragmentManager()
                    .getPrimaryNavigationFragment();
            if (primaryNavFragment instanceof NavHostFragment) {
                return ((NavHostFragment) primaryNavFragment).getNavController();
            }
            findFragment = findFragment.getParentFragment();
        }

        // Try looking for one associated with the view instead, if applicable
        View view = fragment.getView();
        if (view != null) {
            return Navigation.findNavController(view);
        }
        throw new IllegalStateException("Fragment " + fragment
                + " does not have a NavController set");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void viewFridges(View v) {
        navController.navigate(R.id.nav_fridgesList);
    }

}
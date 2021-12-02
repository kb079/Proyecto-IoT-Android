package com.myfridge.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PruebaNotificaciones extends AppCompatActivity {

    // Nombres de las pestañas
    private String[] nombres = new String[]{"Puerta Abierta","Temperatura Anómala"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prueba_notificaciones);

        //Pestañas
        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MiPagerAdapter(this));
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position){
                        tab.setText(nombres[position]);
                    }

                }

        ).attach();

    }

    public class MiPagerAdapter extends FragmentStateAdapter {
        public MiPagerAdapter(FragmentActivity activity){
            super(activity);
        }
        @Override
        public int getItemCount() {
            return 2;
        }
        @Override @NonNull
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new NotificacionPuertaAbierta();
                case 1: return new NotificacionTempAnomala();

            }
            return null;
        }
    }



}


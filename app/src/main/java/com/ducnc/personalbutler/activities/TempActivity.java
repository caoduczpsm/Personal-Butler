package com.ducnc.personalbutler.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.adapters.ViewPagerAdapter;
import com.ducnc.personalbutler.fragment.BarChartFragment;
import com.ducnc.personalbutler.fragment.PieChartFragment;
import com.ducnc.personalbutler.fragment.RadarChartFragment;
import com.google.android.material.tabs.TabLayout;

public class TempActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);


        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PieChartFragment(), "Tròn");
        adapter.addFragment(new BarChartFragment(), "Cột");
        adapter.addFragment(new RadarChartFragment(), "Mạng");

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

    }


}
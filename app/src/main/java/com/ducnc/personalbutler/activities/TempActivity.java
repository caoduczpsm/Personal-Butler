package com.ducnc.personalbutler.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import com.ducnc.personalbutler.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class TempActivity extends AppCompatActivity {

    PieChart pieChart;
    int [] colorClassArray = new int[]{Color.LTGRAY, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.MAGENTA, Color.RED};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        pieChart = findViewById(R.id.pieChart);
        PieDataSet pieDataSet = new PieDataSet(dataValues(), "");
        pieDataSet.setColors(colorClassArray);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("Data");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextRadiusPercent(50);
        pieChart.setHoleRadius(30);
        pieChart.setTransparentCircleRadius(40);
        pieChart.setTransparentCircleColor(Color.RED);
        pieChart.setTransparentCircleAlpha(50);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }

    private ArrayList<PieEntry> dataValues(){
        ArrayList<PieEntry> dataVal = new ArrayList<>();

        dataVal.add(new PieEntry(15, "sun"));
        dataVal.add(new PieEntry(34, "mon"));
        dataVal.add(new PieEntry(23, "tue"));
        dataVal.add(new PieEntry(86, "web"));
        dataVal.add(new PieEntry(17, "thu"));
        dataVal.add(new PieEntry(26, "fri"));
        dataVal.add(new PieEntry(63, "sat"));
        return dataVal;
    }
}
package com.ducnc.personalbutler.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.adapters.ChartAdapter;
import com.ducnc.personalbutler.models.Expenses;
import com.ducnc.personalbutler.ultilities.Constants;
import com.ducnc.personalbutler.ultilities.PreferenceManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class BarChartFragment extends Fragment {

    PreferenceManager preferenceManager;
    FirebaseFirestore database;

    Calendar currentDate;

    ConstraintLayout layoutDay, layoutMonth;
    TextView textDay, textMonth, textDayTotal, textMonthTotal, textTitleDay, textTitleMonth, textDateSelected;
    RecyclerView expensesDayRecyclerView, expensesMonthRecyclerView;
    View viewSupporterDay, viewSupportMonth;

    ChartAdapter chartMonthAdapter, chartDayAdapter;
    List<Expenses> expensesDayList, expensesMonthList;

    BarChart barChartDay, barChartMonth;
    ArrayList<BarEntry> barEntriesDay, barEntriesMonth;
    BarDataSet barDataSetDay, barDataSetMonth;
    BarData barDataDay, barDataMonth;
    ArrayList<String> idDay;
    ArrayList<String> idMonth = new ArrayList<>();

    private int lastSelectedYear;
    private int lastSelectedMonth;
    private int lastSelectedDayOfMonth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);

        init();

        barChartDay = view.findViewById(R.id.barChartDay);
        barChartMonth = view.findViewById(R.id.barChartMonth);

        textDay = view.findViewById(R.id.textDay);
        textMonth = view.findViewById(R.id.textMonth);
        textDayTotal = view.findViewById(R.id.textDayTotal);
        textMonthTotal = view.findViewById(R.id.textMonthTotal);
        textTitleDay = view.findViewById(R.id.textTitleDay);
        textTitleMonth = view.findViewById(R.id.textTitleMonth);
        textDateSelected = view.findViewById(R.id.textDateSelected);

        viewSupporterDay = view.findViewById(R.id.viewSupporterDay);
        viewSupportMonth = view.findViewById(R.id.viewSupporterMonth);

        layoutDay = view.findViewById(R.id.layoutChartDay);
        layoutMonth = view.findViewById(R.id.layoutChartMonth);

        expensesMonthRecyclerView = view.findViewById(R.id.expensesMonthRecyclerView);
        expensesDayRecyclerView = view.findViewById(R.id.expensesDayRecyclerView);

        lastSelectedYear = currentDate.get(Calendar.YEAR);
        lastSelectedMonth = currentDate.get(Calendar.MONTH);
        lastSelectedDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        textDateSelected.setText(lastSelectedDayOfMonth + "/" + (lastSelectedMonth + 1) + "/" + lastSelectedYear);

        textDateSelected.setOnClickListener(view1 -> selectDate());

        createBarChartMonth();
        createBarChartDay();
        getDataOfYear();

        setListener();

        return view;
    }


    private void init() {
        preferenceManager = new PreferenceManager(requireContext());
        database = FirebaseFirestore.getInstance();
        currentDate = Calendar.getInstance();
        expensesDayList = new ArrayList<>();
        expensesMonthList = new ArrayList<>();
    }


    private void setListener() {
        textDay.setOnClickListener(view -> {
            viewSupportMonth.setVisibility(View.GONE);
            viewSupporterDay.setVisibility(View.VISIBLE);
            layoutDay.setVisibility(View.VISIBLE);
            layoutMonth.setVisibility(View.GONE);
        });
        textMonth.setOnClickListener(view -> {
            viewSupportMonth.setVisibility(View.VISIBLE);
            viewSupporterDay.setVisibility(View.GONE);
            layoutDay.setVisibility(View.GONE);
            layoutMonth.setVisibility(View.VISIBLE);
        });

        barChartMonth.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int x = barChartMonth.getBarData().getDataSetForEntry(e).getEntryIndex((BarEntry) e);
                getDataOfMonth(idMonth.get(x));
                expensesMonthList.clear();
            }

            @Override
            public void onNothingSelected() {
                getDataOfYear();
                expensesMonthList.clear();
            }
        });

        barChartDay.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int x = barChartDay.getBarData().getDataSetForEntry(e).getEntryIndex((BarEntry) e);
                getDataOfDay(idDay.get(x));
                expensesDayList.clear();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void selectDate() {
        // Date Select Listener.
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {

            textDateSelected.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
            lastSelectedYear = year;
            lastSelectedMonth = monthOfYear;
            lastSelectedDayOfMonth = dayOfMonth;
            getDataOfDaySelected(lastSelectedDayOfMonth, lastSelectedMonth + 1);
        };

        DatePickerDialog datePickerDialog;


        datePickerDialog = new DatePickerDialog(getActivity(),
                dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);

        // Show
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
    }

    private void getDataOfDaySelected(int day, int month) {
        barEntriesDay = new ArrayList<>();
        idDay = new ArrayList<>();
        database.collection(Constants.KEY_DAY)
                .document(month + "")
                .collection(Constants.KEY_DAY)
                .document(day + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task1 -> {
                    int i = 1;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                        barEntriesDay.add(new BarEntry(i,
                                Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)))));
                        idDay.add(queryDocumentSnapshot.getId());
                        i++;
                    }
                    barDataSetDay = new BarDataSet(barEntriesDay, "");
                    barDataSetDay.setColors(ColorTemplate.MATERIAL_COLORS);
                    barDataSetDay.setValueTextColor(Color.BLACK);
                    barDataSetDay.setValueTextSize(12f);
                    barDataDay = new BarData(barDataSetDay);
                    barChartDay.setFitBars(true);
                    barChartDay.setData(barDataDay);
                    barChartDay.getDescription().setEnabled(false);
                    barChartDay.animateY(2000);

                });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void getDataOfYear() {
        chartMonthAdapter = new ChartAdapter(expensesMonthList);
        expensesMonthRecyclerView.setAdapter(chartMonthAdapter);
        expensesMonthRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.YEAR) + "")
                .collection(Constants.KEY_MONTH)
                .get()
                .addOnCompleteListener(task -> {
                    final int[] detail = {0};
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        database.collection(Constants.KEY_MONTH)
                                .document(currentDate.get(Calendar.YEAR) + "")
                                .collection(Constants.KEY_MONTH)
                                .document(queryDocumentSnapshot.getId())
                                .collection(Constants.KEY_EXPENSES)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    int total = 0;
                                    for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()) {
                                        Expenses expenses = new Expenses();

                                        expenses.setName(queryDocumentSnapshot1.getString(Constants.KEY_EXPENSES) + "(" + queryDocumentSnapshot.getId() + ")");
                                        expenses.setAmount(queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_MONEY));
                                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                                        expensesMonthList.add(expenses);


                                        chartMonthAdapter.notifyDataSetChanged();
                                    }
                                    textTitleMonth.setText("Các khoản chi trong năm " + currentDate.get(Calendar.YEAR));
                                    detail[0] = detail[0] + total;
                                    textMonthTotal.setText("Tổng cộng: " + detail[0] + " VNĐ");
                                });
                    }
                });

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void getDataOfMonth(String id) {
        chartMonthAdapter = new ChartAdapter(expensesMonthList);
        expensesMonthRecyclerView.setAdapter(chartMonthAdapter);
        expensesMonthRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.YEAR) + "")
                .collection(Constants.KEY_MONTH)
                .document(id)
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task -> {
                    int total = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Expenses expenses = new Expenses();
                        expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                        expenses.setAmount(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                        expensesMonthList.add(expenses);
                        chartMonthAdapter.notifyDataSetChanged();
                    }
                    textTitleMonth.setText("Các khoản chi trong tháng " + id);
                    textMonthTotal.setText("Tổng cộng: " + total + " VNĐ");
                });
    }

    @SuppressLint("SetTextI18n")
    private void createBarChartMonth() {
        barEntriesMonth = new ArrayList<>();
        database.collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.YEAR) + "")
                .collection(Constants.KEY_MONTH)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                        database.collection(Constants.KEY_MONTH)
                                .document(currentDate.get(Calendar.YEAR) + "")
                                .collection(Constants.KEY_MONTH)
                                .document(queryDocumentSnapshot.getId())
                                .collection(Constants.KEY_EXPENSES)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    int total = 0;
                                    for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()) {
                                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                                    }

                                    idMonth.add(queryDocumentSnapshot.getId());
                                    barEntriesMonth.add(new BarEntry(Integer.parseInt(queryDocumentSnapshot.getId()), total));
                                    barDataSetMonth = new BarDataSet(barEntriesMonth, "");
                                    barDataSetMonth.setColors(ColorTemplate.MATERIAL_COLORS);
                                    barDataSetMonth.setValueTextColor(Color.BLACK);
                                    barDataSetMonth.setValueTextSize(12f);
                                    barDataMonth = new BarData(barDataSetMonth);
                                    barChartMonth.setFitBars(true);
                                    barChartMonth.setData(barDataMonth);
                                    barChartMonth.getDescription().setEnabled(false);
                                    barChartMonth.animateY(2000);
                                });
                    }

                });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void getDataOfDay(String id) {
        chartDayAdapter = new ChartAdapter(expensesDayList);
        expensesDayRecyclerView.setAdapter(chartDayAdapter);
        expensesDayRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_DAY)
                .document((lastSelectedMonth + 1) + "")
                .collection(Constants.KEY_DAY)
                .document(lastSelectedDayOfMonth + "")
                .collection(Constants.KEY_EXPENSES)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Expenses expenses = new Expenses();
                    expenses.setName(documentSnapshot.getString(Constants.KEY_EXPENSES));
                    expenses.setAmount(documentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                    Log.d("HHH", documentSnapshot.getString(Constants.KEY_EXPENSES) + "null");
                    expensesDayList.add(expenses);
                    chartDayAdapter.notifyDataSetChanged();
                    textDayTotal.setText("Tổng cộng: " + expenses.getAmount() + " VNĐ");
                });
    }

    private void createBarChartDay() {
        barEntriesDay = new ArrayList<>();
        idDay = new ArrayList<>();
        database.collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.MONTH) + 1 + "")
                .collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task1 -> {
                    int i = 1;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                        barEntriesDay.add(new BarEntry(i,
                                Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)))));
                        idDay.add(queryDocumentSnapshot.getId());
                        i++;
                    }
                    barDataSetDay = new BarDataSet(barEntriesDay, "");
                    barDataSetDay.setColors(ColorTemplate.MATERIAL_COLORS);
                    barDataSetDay.setValueTextColor(Color.BLACK);
                    barDataSetDay.setValueTextSize(12f);
                    barDataDay = new BarData(barDataSetDay);
                    barChartDay.setFitBars(true);
                    barChartDay.setData(barDataDay);
                    barChartDay.getDescription().setEnabled(false);
                    barChartDay.animateY(2000);

                });

    }
}
package com.ducnc.personalbutler.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.adapters.ChartAdapter;
import com.ducnc.personalbutler.models.Expenses;
import com.ducnc.personalbutler.ultilities.Constants;
import com.ducnc.personalbutler.ultilities.PreferenceManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class PieChartFragment extends Fragment {

    PreferenceManager preferenceManager;
    FirebaseFirestore database;

    List<Expenses> expensesDayList, expensesMonthList;
    PieChart pieChartDay, pieChartMonth;
    PieDataSet pieDataSetDay, pieDataSetMonth;
    ChartAdapter pieChartDayAdapter, pieChartMonthAdapter;

    Calendar currentDate;

    ConstraintLayout layoutDay, layoutMonth;
    TextView textDay, textMonth, textDayTotal, textMonthTotal, textDateSelected;
    RecyclerView expensesDayRecyclerView, expensesMonthRecyclerView;
    View viewSupporterDay, viewSupportMonth;

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
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        init();

        pieChartDay = view.findViewById(R.id.pieChartDay);
        pieChartMonth = view.findViewById(R.id.pieChartMonth);

        textDay = view.findViewById(R.id.textDay);
        textMonth = view.findViewById(R.id.textMonth);
        textDayTotal = view.findViewById(R.id.textDayTotal);
        textMonthTotal = view.findViewById(R.id.textMonthTotal);
        textDateSelected = view.findViewById(R.id.textDateSelected);

        viewSupporterDay = view.findViewById(R.id.viewSupporterDay);
        viewSupportMonth = view.findViewById(R.id.viewSupporterMonth);

        layoutDay = view.findViewById(R.id.layoutChartDay);
        layoutMonth = view.findViewById(R.id.layoutChartMonth);

        expensesDayRecyclerView = view.findViewById(R.id.expensesDayRecyclerView);
        expensesMonthRecyclerView = view.findViewById(R.id.expensesMonthRecyclerView);

        lastSelectedYear = currentDate.get(Calendar.YEAR);
        lastSelectedMonth = currentDate.get(Calendar.MONTH);
        lastSelectedDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        textDateSelected.setText(lastSelectedDayOfMonth + "/" + (lastSelectedMonth + 1) + "/" + lastSelectedYear);

        textDateSelected.setOnClickListener(view1 -> selectDate());

        setListener();
        createPieChartDay();
        createPieChartMonth();
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

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void getDataOfDaySelected(int day, int month){
        expensesDayList.clear();
        pieChartDayAdapter = new ChartAdapter(expensesDayList);
        expensesDayRecyclerView.setAdapter(pieChartDayAdapter);
        expensesDayRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_DAY)
                .document(month + "")
                .collection(Constants.KEY_DAY)
                .document(day + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<PieEntry> dataVal = new ArrayList<>();
                    int total = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        dataVal.add(new PieEntry(Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY))),
                                queryDocumentSnapshot.getString(Constants.KEY_EXPENSES)));
                        Expenses expenses = new Expenses();
                        expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                        expenses.setAmount(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                        expensesDayList.add(expenses);
                        pieChartDayAdapter.notifyDataSetChanged();
                    }

                    textDayTotal.setText("Tổng cộng: " + total + " VNĐ");

                    pieDataSetDay = new PieDataSet(dataVal, "");
                    pieDataSetDay.setColors(ColorTemplate.COLORFUL_COLORS);
                    pieDataSetDay.setValueTextSize(18f);

                    PieData pieData = new PieData(pieDataSetDay);

                    pieChartDay.setDrawEntryLabels(false);
                    pieChartDay.getDescription().setText("Các khoản chi tiêu ");
                    pieChartDay.getDescription().setTextSize(16f);
                    pieChartDay.setUsePercentValues(true);
                    pieChartDay.setEntryLabelTextSize(18f);
                    pieChartDay.setCenterTextRadiusPercent(50);
                    pieChartDay.setHoleRadius(30);
                    pieChartDay.setTransparentCircleRadius(40);
                    pieChartDay.setTransparentCircleColor(Color.RED);
                    pieChartDay.setTransparentCircleAlpha(50);
                    pieChartDay.setData(pieData);
                    pieChartDay.invalidate();
                    pieChartDay.getDescription().setEnabled(false);
                    pieChartDay.animateX(2000);
                });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void createPieChartDay() {
        pieChartDayAdapter = new ChartAdapter(expensesDayList);
        expensesDayRecyclerView.setAdapter(pieChartDayAdapter);
        expensesDayRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.MONTH) + 1 + "")
                .collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<PieEntry> dataVal = new ArrayList<>();
                    int total = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        dataVal.add(new PieEntry(Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY))),
                                queryDocumentSnapshot.getString(Constants.KEY_EXPENSES)));
                        Expenses expenses = new Expenses();
                        expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                        expenses.setAmount(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                        expensesDayList.add(expenses);
                        pieChartDayAdapter.notifyDataSetChanged();
                    }

                    textDayTotal.setText("Tổng cộng: " + total + " VNĐ");

                    pieDataSetDay = new PieDataSet(dataVal, "");
                    pieDataSetDay.setColors(ColorTemplate.COLORFUL_COLORS);
                    pieDataSetDay.setValueTextSize(18f);

                    PieData pieData = new PieData(pieDataSetDay);

                    pieChartDay.setDrawEntryLabels(false);
                    pieChartDay.getDescription().setText("Các khoản chi tiêu ");
                    pieChartDay.getDescription().setTextSize(16f);
                    pieChartDay.setUsePercentValues(true);
                    pieChartDay.setEntryLabelTextSize(18f);
                    pieChartDay.setCenterTextRadiusPercent(50);
                    pieChartDay.setHoleRadius(30);
                    pieChartDay.setTransparentCircleRadius(40);
                    pieChartDay.setTransparentCircleColor(Color.RED);
                    pieChartDay.setTransparentCircleAlpha(50);
                    pieChartDay.setData(pieData);
                    pieChartDay.invalidate();
                    pieChartDay.getDescription().setEnabled(false);
                    pieChartDay.animateX(2000);
                });

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void createPieChartMonth() {
        pieChartMonthAdapter = new ChartAdapter(expensesMonthList);
        expensesMonthRecyclerView.setAdapter(pieChartMonthAdapter);
        expensesMonthRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.YEAR) + "")
                .collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.MONTH) + 1 + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<PieEntry> dataVal = new ArrayList<>();
                    int total = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        dataVal.add(new PieEntry(Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY))),
                                queryDocumentSnapshot.getString(Constants.KEY_EXPENSES)));
                        Expenses expenses = new Expenses();
                        expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                        expenses.setAmount(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                        expensesMonthList.add(expenses);
                        pieChartMonthAdapter.notifyDataSetChanged();
                    }
                    textMonthTotal.setText("Tổng cộng: " + total + " VNĐ");

                    pieDataSetMonth = new PieDataSet(dataVal, "");
                    pieDataSetMonth.setColors(ColorTemplate.COLORFUL_COLORS);
                    pieDataSetMonth.setValueTextSize(18f);

                    PieData pieData = new PieData(pieDataSetMonth);

                    pieChartMonth.setDrawEntryLabels(false);
                    pieChartMonth.getDescription().setText("Các khoản chi tiêu ");
                    pieChartMonth.getDescription().setTextSize(16f);
                    pieChartMonth.setUsePercentValues(true);
                    pieChartMonth.setEntryLabelTextSize(18f);
                    pieChartMonth.setCenterTextRadiusPercent(50);
                    pieChartMonth.setHoleRadius(30);
                    pieChartMonth.setTransparentCircleRadius(40);
                    pieChartMonth.setTransparentCircleColor(Color.RED);
                    pieChartMonth.setTransparentCircleAlpha(50);
                    pieChartMonth.setData(pieData);
                    pieChartMonth.invalidate();
                    pieChartMonth.getDescription().setEnabled(false);
                    pieChartMonth.animateX(2000);
                });
    }

}
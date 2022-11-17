package com.ducnc.personalbutler.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.adapters.ExpensesAdapter;
import com.ducnc.personalbutler.adapters.ExpensesMainAdapter;
import com.ducnc.personalbutler.adapters.MoneyAdapter;
import com.ducnc.personalbutler.listeners.ExpensesListener;
import com.ducnc.personalbutler.models.Expenses;
import com.ducnc.personalbutler.ultilities.Constants;
import com.ducnc.personalbutler.ultilities.PreferenceManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ExpensesListener {

    TextView textName, txtMoney, textTotal, textRefresh;
    ImageView imgLogout, imgEdit, imgRefresh;
    PieChart pieChart;
    PieDataSet pieDataSet;
    ConstraintLayout layoutExpenses, layoutManager, layoutChart;
    List<Expenses> listExpenses;
    List<Expenses> moneyList;
    List<Expenses> expensesMainList;
    ExpensesAdapter expensesAdapter;
    MoneyAdapter moneyAdapter;
    ExpensesMainAdapter expensesMainAdapter;
    RecyclerView expensesRecyclerView;

    FirebaseFirestore database;
    PreferenceManager preferenceManager;
    Calendar currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        createPieChart();
        setListener();

        database.collection(Constants.KEY_COLLECTION_USER)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    textName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                });

        updateDataToFirebase();

    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void init() {

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        currentDate = Calendar.getInstance();

        textName = findViewById(R.id.textName);
        txtMoney = findViewById(R.id.txtMoney);
        textTotal = findViewById(R.id.textTotal);
        textRefresh = findViewById(R.id.textRefresh);

        imgLogout = findViewById(R.id.imgLogout);
        imgEdit = findViewById(R.id.imgEdit);
        imgRefresh = findViewById(R.id.imgRefresh);

        pieChart = findViewById(R.id.pieChart);

        expensesRecyclerView = findViewById(R.id.expensesRecyclerView);

        database.collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.YEAR) + "")
                .collection(Constants.KEY_MONTH)
                .document(currentDate.get(Calendar.MONTH) + 1 + "")
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (Objects.equals(documentSnapshot.get(Constants.KEY_REMAINING_MONEY), documentSnapshot.getString(Constants.KEY_MONEY_OF_MONTH))) {
                        txtMoney.setText(documentSnapshot.getString(Constants.KEY_MONEY_OF_MONTH) + " VNĐ");
                    } else {
                        txtMoney.setText(documentSnapshot.getString(Constants.KEY_REMAINING_MONEY) + " VNĐ");
                    }
                });

        layoutExpenses = findViewById(R.id.layoutExpenses);
        layoutManager = findViewById(R.id.layoutManager);
        layoutChart = findViewById(R.id.layoutChart);
        listExpenses = new ArrayList<>();
        moneyList = new ArrayList<>();
        expensesMainList = new ArrayList<>();


    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setListener() {

        imgLogout.setOnClickListener(view -> logOut());

        imgEdit.setOnClickListener(view -> editMoney());

        imgRefresh.setOnClickListener(view -> {
            textRefresh.setVisibility(View.GONE);
            imgRefresh.setVisibility(View.GONE);
            createPieChart();
        });

        textRefresh.setOnClickListener(view -> {
            textRefresh.setVisibility(View.GONE);
            imgRefresh.setVisibility(View.GONE);
            createPieChart();
        });

        txtMoney.setOnClickListener(view -> editMoney());

        textName.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TempActivity.class);
            startActivity(intent);
        });

        layoutManager.setOnClickListener(view -> {
            final Dialog dialog = openDialog(R.layout.layout_dialog_manager_money_expenses);

            assert dialog != null;
            RecyclerView expensesRecyclerView = dialog.findViewById(R.id.recyclerViewExpenses);
            Button btnClose = dialog.findViewById(R.id.btnClose);

            moneyAdapter = new MoneyAdapter(moneyList, this);
            expensesRecyclerView.setAdapter(moneyAdapter);
            expensesRecyclerView.setVisibility(View.VISIBLE);

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .collection(Constants.KEY_EXPENSES)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Expenses expenses = new Expenses();
                                expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                                moneyList.add(expenses);
                            }
                            if (moneyList.size() > 0) {
                                moneyAdapter.notifyDataSetChanged();
                                expensesRecyclerView.scrollToPosition(moneyList.size() - 1);
                            }

                        }
                    });

            if (!dialog.isShowing())
                moneyList.clear();
            btnClose.setOnClickListener(view1 -> dialog.dismiss());
            dialog.show();

        });

        layoutExpenses.setOnClickListener(view -> {
            final Dialog dialog = openDialog(R.layout.layout_dialog_expenses);

            assert dialog != null;
            RecyclerView expensesRecyclerView = dialog.findViewById(R.id.recyclerViewExpenses);
            Button btnAdd = dialog.findViewById(R.id.btnAdd);
            Button btnClose = dialog.findViewById(R.id.btnClose);
            EditText inputText = dialog.findViewById(R.id.inputText);


            btnAdd.setOnClickListener(view1 -> {
                if (!inputText.getText().toString().equals("")) {

                    Expenses expenses = new Expenses();
                    expenses.setName(inputText.getText().toString());
                    listExpenses.add(expenses);

                    HashMap<String, Object> money = new HashMap<>();
                    money.put(Constants.KEY_EXPENSES, expenses.getName());
                    money.put(Constants.KEY_AMOUNT_OF_MONEY, "0");

                    database.collection(Constants.KEY_MONTH)
                            .document(currentDate.get(Calendar.YEAR) + "")
                            .collection(Constants.KEY_MONTH)
                            .document(currentDate.get(Calendar.MONTH) + 1 + "")
                            .collection(Constants.KEY_EXPENSES)
                            .add(money)
                            .addOnSuccessListener(documentReference -> showToast("Cập nhật khoản chi thành công!"));

                    inputText.setText("");
                } else {
                    showToast("Hãy nhập khoản chi cần thêm");
                }

                if (listExpenses.size() > 0) {
                    expensesAdapter = new ExpensesAdapter(listExpenses, this);
                    expensesRecyclerView.setAdapter(expensesAdapter);
                    expensesAdapter.notifyDataSetChanged();
                    expensesAdapter.notifyItemInserted(listExpenses.size() - 1);
                    expensesRecyclerView.scrollToPosition(listExpenses.size() - 1);
                }

            });
            expensesAdapter = new ExpensesAdapter(listExpenses, this);
            expensesRecyclerView.setAdapter(expensesAdapter);

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .collection(Constants.KEY_EXPENSES)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Expenses expenses = new Expenses();
                                expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                                listExpenses.add(expenses);
                            }
                            if (listExpenses.size() > 0) {
                                expensesAdapter.notifyDataSetChanged();
                                expensesRecyclerView.scrollToPosition(listExpenses.size() - 1);
                            }

                        }
                    });

            listExpenses.clear();

            btnClose.setOnClickListener(view1 -> dialog.dismiss());

            dialog.show();
        });

        layoutChart.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
            startActivity(intent);
        });

    }

    @SuppressLint("SetTextI18n")
    private void editMoney() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_manager_money);
        assert dialog != null;

        EditText inputMoney = dialog.findViewById(R.id.inputMoney);
        EditText inputSubMoney = dialog.findViewById(R.id.inputSubMoney);
        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        btnAdd.setOnClickListener(view12 -> {

            if (!inputMoney.getText().toString().equals("")) {
                HashMap<String, Object> money = new HashMap<>();
                database.collection(Constants.KEY_MONTH)
                        .document(currentDate.get(Calendar.YEAR) + "")
                        .collection(Constants.KEY_MONTH)
                        .document(currentDate.get(Calendar.MONTH) + 1 + "")
                        .get()
                        .addOnCompleteListener(task -> {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.getString(Constants.KEY_MONEY_OF_MONTH) != null ||
                                    !Objects.equals(documentSnapshot.getString(Constants.KEY_MONEY_OF_MONTH), "")
                                    || !Objects.equals(documentSnapshot.getString(Constants.KEY_MONEY_OF_MONTH), "0")) {
                                int recentMoney = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_REMAINING_MONEY)));
                                if (inputSubMoney.getText().toString().equals("")) {
                                    int updateMoney = Integer.parseInt(inputMoney.getText().toString()) + recentMoney;
                                    money.put(Constants.KEY_REMAINING_MONEY, String.valueOf(updateMoney));
                                    money.put(Constants.KEY_MONEY_OF_MONTH, String.valueOf(updateMoney));
                                } else {
                                    int updateMoney = Integer.parseInt(inputMoney.getText().toString()) + Integer.parseInt(inputSubMoney.getText().toString()) + recentMoney;
                                    money.put(Constants.KEY_REMAINING_MONEY, String.valueOf(updateMoney));
                                    money.put(Constants.KEY_MONEY_OF_MONTH, String.valueOf(updateMoney));
                                }
                                database.collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.YEAR) + "")
                                        .collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                        .update(money)
                                        .addOnSuccessListener(documentReference -> {
                                            showToast("Đã cập nhật số tiền!");
                                            if (inputSubMoney.getText().toString().equals("")) {
                                                txtMoney.setText(Integer.parseInt(inputMoney.getText().toString()) + recentMoney + " VNĐ");
                                            } else {
                                                txtMoney.setText(Integer.parseInt(inputMoney.getText().toString()) + recentMoney + Integer.parseInt(inputSubMoney.getText().toString()) + " VNĐ");
                                            }
                                            dialog.dismiss();
                                        });
                            } else {
                                if (inputSubMoney.getText().toString().equals("")) {
                                    money.put(Constants.KEY_MONTH, currentDate.get(Calendar.MONTH) + 1 + "");
                                    money.put(Constants.KEY_REMAINING_MONEY, String.valueOf(Integer.parseInt(inputMoney.getText().toString())));
                                    money.put(Constants.KEY_MONEY_OF_MONTH, String.valueOf(Integer.parseInt(inputMoney.getText().toString())));
                                } else {
                                    money.put(Constants.KEY_MONTH, currentDate.get(Calendar.MONTH) + 1 + "");
                                    money.put(Constants.KEY_REMAINING_MONEY, String.valueOf(Integer.parseInt(inputMoney.getText().toString()) + Integer.parseInt(inputSubMoney.getText().toString())));
                                    money.put(Constants.KEY_MONEY_OF_MONTH, String.valueOf(Integer.parseInt(inputMoney.getText().toString()) + Integer.parseInt(inputSubMoney.getText().toString())));
                                }
                                database.collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.YEAR) + "")
                                        .collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                        .set(money)
                                        .addOnSuccessListener(documentReference -> {
                                            showToast("Đã cập nhật số tiền!");
                                            if (inputSubMoney.getText().toString().equals("")) {
                                                txtMoney.setText(Integer.parseInt(inputMoney.getText().toString()) + " VNĐ");
                                            } else {
                                                txtMoney.setText(Integer.parseInt(inputMoney.getText().toString()) + Integer.parseInt(inputSubMoney.getText().toString()) + " VNĐ");
                                            }
                                            dialog.dismiss();
                                        });
                            }
                        });
            } else {
                showToast("Vui lòng nhập nguồn tiền cố định!");
            }
        });

        btnClose.setOnClickListener(view13 -> dialog.dismiss());

        dialog.show();
    }

    private void updateDataToFirebase() {

        HashMap<String, Object> year = new HashMap<>();
        year.put(Constants.KEY_YEAR, currentDate.get(Calendar.YEAR) + "");

        HashMap<String, Object> month = new HashMap<>();
        month.put(Constants.KEY_MONTH, currentDate.get(Calendar.MONTH) + 1 + "");
        month.put(Constants.KEY_MONEY_OF_MONTH, "0");
        month.put(Constants.KEY_REMAINING_MONEY, "0");

        HashMap<String, Object> day = new HashMap<>();
        day.put(Constants.KEY_DAY, currentDate.get(Calendar.DAY_OF_MONTH) + "");
        day.put("123", "asddasdas");
        day.put("456", "asdasdas");
        day.put("678", "asdasdas");


        if (preferenceManager.getString(Constants.KEY_FIRST_LOGIN) == null) {

            database.collection(Constants.KEY_YEAR)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .set(year);

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .set(month);

            database.collection(Constants.KEY_DAY)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .set(day);

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .set(month);

            HashMap<String, Object> remaining = new HashMap<>();
            remaining.put(Constants.KEY_REMAINING_MONEY, "0");

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .set(remaining);

            preferenceManager.putString(Constants.KEY_FIRST_LOGIN, "done");
        } else {
            database.collection(Constants.KEY_YEAR)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .set(year);

            database.collection(Constants.KEY_MONTH)
                    .document(currentDate.get(Calendar.YEAR) + "")
                    .set(month);

            database.collection(Constants.KEY_DAY)
                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                    .set(day);
        }


    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void createPieChart() {
        expensesMainList.clear();
        expensesMainAdapter = new ExpensesMainAdapter(expensesMainList, this);
        expensesRecyclerView.setAdapter(expensesMainAdapter);
        expensesRecyclerView.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.MONTH) + 1 + "")
                .collection(Constants.KEY_DAY)
                .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                .collection(Constants.KEY_EXPENSES)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<PieEntry> dataVal = new ArrayList<>();
                    int total = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        dataVal.add(new PieEntry(Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY))),
                                queryDocumentSnapshot.getString(Constants.KEY_EXPENSES)));
                        Expenses expenses = new Expenses();
                        expenses.setName(queryDocumentSnapshot.getString(Constants.KEY_EXPENSES));
                        expenses.setAmount(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY));
                        total = total + Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY)));
                        expensesMainList.add(expenses);
                        expensesMainAdapter.notifyDataSetChanged();
                    }
                    textTotal.setText("Tổng cộng: " + total + " VNĐ");
                    pieDataSet = new PieDataSet(dataVal, "");
                    pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                    pieDataSet.setValueTextSize(18f);
                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setDrawEntryLabels(false);
                    pieChart.getDescription().setTextSize(16f);
                    pieChart.setUsePercentValues(true);
                    pieChart.setEntryLabelTextSize(18f);
                    pieChart.setCenterTextRadiusPercent(50);
                    pieChart.setHoleRadius(30);
                    pieChart.setTransparentCircleRadius(40);
                    pieChart.setTransparentCircleColor(Color.RED);
                    pieChart.setTransparentCircleAlpha(50);
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                    pieChart.getDescription().setEnabled(false);
                    pieChart.animateX(2000);
                });
    }

    private void logOut() {
        showToast("Signing out...");

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }


    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelection(List<Expenses> expensesList, int position) {

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onEditItem(List<Expenses> expensesList, int position) {
        final Dialog dialog = openDialog(R.layout.layout_dialog_add_money);

        assert dialog != null;

        Button btnClose = dialog.findViewById(R.id.btnClose);
        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        TextView textExpenses = dialog.findViewById(R.id.textExpenses);
        EditText inputMoney = dialog.findViewById(R.id.inputMoney);

        textExpenses.setText(expensesList.get(position).getName());
        String nameExpenses = expensesList.get(position).getName();

        btnAdd.setOnClickListener(view -> {

            if (!inputMoney.getText().toString().equals("")) {
                HashMap<String, Object> money = new HashMap<>();
                money.put(Constants.KEY_EXPENSES, Integer.parseInt(inputMoney.getText().toString()));

                database.collection(Constants.KEY_MONTH)
                        .document(currentDate.get(Calendar.YEAR) + "")
                        .collection(Constants.KEY_MONTH)
                        .document(currentDate.get(Calendar.MONTH) + 1 + "")
                        .get()
                        .addOnCompleteListener(task -> {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            int recentMoney = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_REMAINING_MONEY)));
                            int moneyUsed = Integer.parseInt(inputMoney.getText().toString());
                            if (recentMoney > moneyUsed) {
                                recentMoney = recentMoney - moneyUsed;
                                txtMoney.setText(recentMoney + " VNĐ");
                                money.put(Constants.KEY_REMAINING_MONEY, recentMoney + "");
                                database.collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.YEAR) + "")
                                        .collection(Constants.KEY_MONTH)
                                        .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                        .update(money)
                                        .addOnSuccessListener(unused -> {
                                            showToast("Đã cập nhật số tiền thành công!");
                                            dialog.dismiss();
                                            textRefresh.setVisibility(View.VISIBLE);
                                            imgRefresh.setVisibility(View.VISIBLE);
                                            HashMap<String, Object> expenses = new HashMap<>();
                                            expenses.put(Constants.KEY_EXPENSES, nameExpenses);
                                            expenses.put(Constants.KEY_AMOUNT_OF_MONEY, moneyUsed + "");

                                            database.collection(Constants.KEY_DAY)
                                                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                                    .collection(Constants.KEY_DAY)
                                                    .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                                                    .collection(Constants.KEY_EXPENSES)
                                                    .document(nameExpenses)
                                                    .get()
                                                    .addOnCompleteListener(task12 -> {

                                                        int recentRemainingMoney;
                                                        DocumentSnapshot documentSnapshot1 = task12.getResult();
                                                        if (documentSnapshot1.getString(Constants.KEY_AMOUNT_OF_MONEY) != null) {
                                                            recentRemainingMoney = Integer.parseInt(Objects.requireNonNull(documentSnapshot1.getString(Constants.KEY_AMOUNT_OF_MONEY)))
                                                                    + moneyUsed;
                                                            HashMap<String, Object> recentRemaining = new HashMap<>();
                                                            recentRemaining.put(Constants.KEY_AMOUNT_OF_MONEY, recentRemainingMoney + "");
                                                            database.collection(Constants.KEY_DAY)
                                                                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                                                    .collection(Constants.KEY_DAY)
                                                                    .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                                                                    .collection(Constants.KEY_EXPENSES)
                                                                    .document(nameExpenses)
                                                                    .update(recentRemaining);
                                                        } else {
                                                            database.collection(Constants.KEY_DAY)
                                                                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                                                    .collection(Constants.KEY_DAY)
                                                                    .document(currentDate.get(Calendar.DAY_OF_MONTH) + "")
                                                                    .collection(Constants.KEY_EXPENSES)
                                                                    .document(nameExpenses)
                                                                    .set(expenses);
                                                        }


                                                    });


                                            database.collection(Constants.KEY_MONTH)
                                                    .document(currentDate.get(Calendar.YEAR) + "")
                                                    .collection(Constants.KEY_MONTH)
                                                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                                    .collection(Constants.KEY_EXPENSES)
                                                    .whereEqualTo(Constants.KEY_EXPENSES, nameExpenses)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                                            HashMap<String, Object> updateMoney = new HashMap<>();
                                                            int used = Integer.parseInt(inputMoney.getText().toString());
                                                            int recentAmountMoney = Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_AMOUNT_OF_MONEY))) + used;
                                                            updateMoney.put(Constants.KEY_AMOUNT_OF_MONEY, String.valueOf(recentAmountMoney));
                                                            database.collection(Constants.KEY_MONTH)
                                                                    .document(currentDate.get(Calendar.YEAR) + "")
                                                                    .collection(Constants.KEY_MONTH)
                                                                    .document(currentDate.get(Calendar.MONTH) + 1 + "")
                                                                    .collection(Constants.KEY_EXPENSES)
                                                                    .document(queryDocumentSnapshot.getId())
                                                                    .update(updateMoney);

                                                        }
                                                    });


                                        });
                            } else {
                                showToast("Đã vượt quá số tiền hiện có!");
                            }

                        });
            } else {
                showToast("Vui lòng nhập số tiền đã chi!");
            }


        });


        btnClose.setOnClickListener(view1 -> {
            createPieChart();
            dialog.dismiss();
        });

        moneyList.clear();
        dialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        updateDataToFirebase();
    }

    @Override
    public void finishActivity(int requestCode) {
        super.finishActivity(requestCode);
        updateDataToFirebase();
    }
}
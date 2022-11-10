package com.ducnc.personalbutler.listeners;

import com.ducnc.personalbutler.models.Expenses;

import java.util.List;

public interface ExpensesListener {
    void onItemSelection(List<Expenses> expensesList, int position);
    void onEditItem(List<Expenses> expensesList, int position);
}

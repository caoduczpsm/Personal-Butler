package com.ducnc.personalbutler.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ducnc.personalbutler.R;
import com.ducnc.personalbutler.listeners.ExpensesListener;
import com.ducnc.personalbutler.models.Expenses;
import java.util.List;

public class MoneyAdapter extends RecyclerView.Adapter<MoneyAdapter.ExpensesViewHolder> {

    private final List<Expenses> expenses;
    private final ExpensesListener listener;

    public MoneyAdapter(List<Expenses> expenses, ExpensesListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ExpensesViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_expenses,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ExpensesViewHolder holder, int position) {
        holder.setData(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpensesViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        Button btnDelete, btnModify;

        ExpensesViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnModify = itemView.findViewById(R.id.btnModify);

            itemView.setOnClickListener(view -> {

                if (btnDelete.getVisibility() == View.GONE || btnModify.getVisibility() == View.GONE){
                    btnDelete.setVisibility(View.VISIBLE);
                    btnModify.setVisibility(View.VISIBLE);
                } else {
                    btnDelete.setVisibility(View.GONE);
                    btnModify.setVisibility(View.GONE);
                }

                listener.onItemSelection(expenses, getAdapterPosition());
            });

            btnModify.setOnClickListener(view -> {
                btnDelete.setVisibility(View.GONE);
                btnModify.setVisibility(View.GONE);
                listener.onEditItem(expenses, getAdapterPosition());
            });

        }

        void setData(Expenses expenses) {
            textName.setText(expenses.getName());
        }

    }

}

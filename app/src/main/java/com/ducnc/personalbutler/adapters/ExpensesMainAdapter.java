package com.ducnc.personalbutler.adapters;

import android.annotation.SuppressLint;
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

public class ExpensesMainAdapter extends RecyclerView.Adapter<ExpensesMainAdapter.ExpensesMainViewHolder> {

    private final List<Expenses> expenses;
    private final ExpensesListener listener;

    public ExpensesMainAdapter(List<Expenses> expenses, ExpensesListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpensesMainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ExpensesMainViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_main_expenses,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ExpensesMainViewHolder holder, int position) {
        holder.setData(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpensesMainViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textAmount;
        Button btnDelete, btnModify;

        ExpensesMainViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAmount = itemView.findViewById(R.id.textAmount);
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

        }

        @SuppressLint("SetTextI18n")
        void setData(Expenses expenses) {

            textName.setText(expenses.getName());
            textAmount.setText(expenses.getAmount() + " VNĐ");

        }

    }

}

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
import com.ducnc.personalbutler.models.Expenses;
import java.util.List;

public class PieChartAdapter extends RecyclerView.Adapter<PieChartAdapter.PieChartViewHolder> {

    private final List<Expenses> expenses;

    public PieChartAdapter(List<Expenses> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public PieChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new PieChartViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_main_expenses,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PieChartViewHolder holder, int position) {
        holder.setData(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class PieChartViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textAmount;
        Button btnDelete, btnModify;

        PieChartViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAmount = itemView.findViewById(R.id.textAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnModify = itemView.findViewById(R.id.btnModify);

            btnDelete.setVisibility(View.GONE);
            btnModify.setVisibility(View.GONE);

        }

        @SuppressLint("SetTextI18n")
        void setData(Expenses expenses) {

            textName.setText(expenses.getName());
            textAmount.setText(expenses.getAmount() + " VNĐ");

        }

    }

}

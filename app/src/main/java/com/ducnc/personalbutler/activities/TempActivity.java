package com.ducnc.personalbutler.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ducnc.personalbutler.R;

import java.util.Calendar;

public class TempActivity extends AppCompatActivity {

    private EditText editTextDate;
    private CheckBox checkBoxIsSpinnerMode;

    private int lastSelectedYear;
    private int lastSelectedMonth;
    private int lastSelectedDayOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        this.editTextDate = (EditText) this.findViewById(R.id.editText_date);
        Button buttonDate = findViewById(R.id.button_date);
        this.checkBoxIsSpinnerMode = this.findViewById(R.id.checkBox_isSpinnerMode);

        buttonDate.setOnClickListener(view -> buttonSelectDate());

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        this.lastSelectedYear = c.get(Calendar.YEAR);
        this.lastSelectedMonth = c.get(Calendar.MONTH);
        this.lastSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
    }

    // User click on 'Select Date' button.
    private void buttonSelectDate() {
        final boolean isSpinnerMode = this.checkBoxIsSpinnerMode.isChecked();

        // Date Select Listener.
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {

            editTextDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

            lastSelectedYear = year;
            lastSelectedMonth = monthOfYear;
            lastSelectedDayOfMonth = dayOfMonth;
        };

        DatePickerDialog datePickerDialog;

        if(isSpinnerMode)  {
            // Create DatePickerDialog:
            datePickerDialog = new DatePickerDialog(this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        }
        // Calendar Mode (Default):
        else {
            datePickerDialog = new DatePickerDialog(this,
                    dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        }

        // Show
        datePickerDialog.show();
    }


}
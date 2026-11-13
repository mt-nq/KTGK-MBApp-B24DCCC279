package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.model.Task;

import java.util.Calendar;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private EditText etDueDate;
    private Spinner spPriority;
    private Button btnSave;
    private Button btnCancel;
    private Toolbar toolbar;

    private DatabaseHelper databaseHelper;
    private Task existingTask = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        databaseHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        spPriority = findViewById(R.id.spPriority);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Priority spinner
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Thấp", "Trung bình", "Cao"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);

        // Check if editing existing task
        if (getIntent().hasExtra("task")) {
            existingTask = getIntent().getParcelableExtra("task");
            isEditMode = true;
        }

        if (isEditMode && existingTask != null) {
            toolbar.setTitle("Chỉnh sửa công việc");
            etTitle.setText(existingTask.getTitle());
            etDescription.setText(existingTask.getDescription());
            etDueDate.setText(existingTask.getDueDate());
            spPriority.setSelection(existingTask.getPriority());
        } else {
            toolbar.setTitle("Thêm công việc");
        }

        // DatePickerDialog for due date
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
        etDueDate.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveTask());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Pre-fill from existing value if available
        String currentDate = etDueDate.getText().toString().trim();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0]));
                }
            } catch (Exception ignored) {}
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String formatted = String.format("%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    etDueDate.setText(formatted);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề công việc", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        int priority = spPriority.getSelectedItemPosition(); // 0=Thấp, 1=Trung bình, 2=Cao

        if (isEditMode && existingTask != null) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setDueDate(dueDate);
            existingTask.setPriority(priority);
            databaseHelper.updateTask(existingTask);
            Toast.makeText(this, "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setDescription(description);
            newTask.setDueDate(dueDate);
            newTask.setPriority(priority);
            newTask.setStatus(0);
            databaseHelper.insertTask(newTask);
            Toast.makeText(this, "Đã thêm công việc mới", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

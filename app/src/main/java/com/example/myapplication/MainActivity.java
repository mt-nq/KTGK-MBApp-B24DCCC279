package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.TaskAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private Spinner spStatusFilter;
    private Spinner spPriorityFilter;
    private TextView tvStats;
    private TextView tvEmpty;

    private List<Task> allTasks = new ArrayList<>();

    // Filters
    private int selectedStatus = -1;   // -1 = all
    private int selectedPriority = -1; // -1 = all
    private String searchQuery = "";

    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        etSearch = findViewById(R.id.etSearch);
        spStatusFilter = findViewById(R.id.spStatusFilter);
        spPriorityFilter = findViewById(R.id.spPriorityFilter);
        tvStats = findViewById(R.id.tvStats);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Setup RecyclerView
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Setup ActivityResultLauncher
        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        loadTasks();
                    }
                });

        // Status Filter Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Chưa thực hiện", "Đã hoàn thành"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(statusAdapter);
        spStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedStatus = -1; break;
                    case 1: selectedStatus = 0;  break;
                    case 2: selectedStatus = 1;  break;
                }
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Priority Filter Spinner
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Thấp", "Trung bình", "Cao"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriorityFilter.setAdapter(priorityAdapter);
        spPriorityFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedPriority = -1; break;
                    case 1: selectedPriority = 0;  break;
                    case 2: selectedPriority = 1;  break;
                    case 3: selectedPriority = 2;  break;
                }
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            addEditLauncher.launch(intent);
        });

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        allTasks = databaseHelper.getAllTasks();
        applyFilters();
        updateStats();
    }

    private void applyFilters() {
        List<Task> filtered = new ArrayList<>();
        for (Task task : allTasks) {
            boolean matchStatus = (selectedStatus == -1) || (task.getStatus() == selectedStatus);
            boolean matchPriority = (selectedPriority == -1) || (task.getPriority() == selectedPriority);
            boolean matchSearch = searchQuery.isEmpty()
                    || task.getTitle().toLowerCase().contains(searchQuery.toLowerCase());
            if (matchStatus && matchPriority && matchSearch) {
                filtered.add(task);
            }
        }
        taskAdapter.updateList(filtered);
        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void updateStats() {
        int[] stats = databaseHelper.getStats(); // {total, done, notDone}
        int total = stats[0];
        int done = stats[1];
        int notDone = stats[2];
        tvStats.setText("Tổng: " + total + " | Hoàn thành: " + done + " | Chưa xong: " + notDone);
    }

    // ---- TaskAdapter.OnTaskActionListener ----

    @Override
    public void onTaskEdit(Task task) {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra("task", task);
        addEditLauncher.launch(intent);
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa công việc \"" + task.getTitle() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());
                    loadTasks();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onStatusChanged(Task task, boolean isDone) {
        task.setStatus(isDone ? 1 : 0);
        databaseHelper.updateTask(task);
        for (Task t : allTasks) {
            if (t.getId() == task.getId()) {
                t.setStatus(task.getStatus());
                break;
            }
        }
        applyFilters();
        updateStats();
    }
}

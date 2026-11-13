package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // ---- Listener interface ----
    public interface OnTaskActionListener {
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onStatusChanged(Task task, boolean isDone);
    }

    // ---- Priority colours ----
    private static final int COLOR_LOW    = Color.parseColor("#4CAF50"); // green
    private static final int COLOR_MEDIUM = Color.parseColor("#FF9800"); // orange
    private static final int COLOR_HIGH   = Color.parseColor("#F44336"); // red

    private final Context context;
    private OnTaskActionListener listener;

    /** Master copy – never filtered out of this list. */
    private List<Task> allTasks = new ArrayList<>();
    /** Currently displayed (possibly filtered) list. */
    private List<Task> displayedTasks = new ArrayList<>();

    public TaskAdapter(Context context, OnTaskActionListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public TaskAdapter(Context context, List<Task> tasks, OnTaskActionListener listener) {
        this.context  = context;
        this.listener = listener;
        if (tasks != null) {
            this.allTasks = new ArrayList<>(tasks);
            this.displayedTasks = new ArrayList<>(tasks);
        }
    }

    // ---- RecyclerView.Adapter ----

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = displayedTasks.get(position);

        // Basic fields
        holder.tvTaskId.setText(String.valueOf(task.getId()));
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription() != null ? task.getDescription() : "");
        holder.tvDueDate.setText(task.getDueDate() != null ? task.getDueDate() : "");

        // Priority badge
        switch (task.getPriority()) {
            case 1:
                holder.tvPriority.setText("Trung bình");
                holder.tvPriority.setBackgroundColor(COLOR_MEDIUM);
                break;
            case 2:
                holder.tvPriority.setText("Cao");
                holder.tvPriority.setBackgroundColor(COLOR_HIGH);
                break;
            default:
                holder.tvPriority.setText("Thấp");
                holder.tvPriority.setBackgroundColor(COLOR_LOW);
                break;
        }
        holder.tvPriority.setTextColor(Color.WHITE);

        // Status checkbox — suppress listener while programmatically setting it
        holder.cbStatus.setOnCheckedChangeListener(null);
        holder.cbStatus.setChecked(task.getStatus() == 1);
        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? 1 : 0);
            if (listener != null) listener.onStatusChanged(task, isChecked);
        });

        // Long press -> Edit / Delete dialog
        holder.itemView.setOnLongClickListener(v -> {
            showActionDialog(task);
            return true;
        });

        // Short click on the whole row (optional convenience)
        holder.itemView.setOnClickListener(v -> {
            // toggle checkbox on row click as well
            holder.cbStatus.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return displayedTasks.size();
    }

    // ---- Public API ----

    /** Replace the full data set and show all tasks. */
    public void updateList(List<Task> tasks) {
        allTasks = new ArrayList<>(tasks);
        displayedTasks = new ArrayList<>(tasks);
        notifyDataSetChanged();
    }

    /** Show only tasks with the given status. Pass -1 to show all. */
    public void filterByStatus(int status) {
        if (status == -1) {
            displayedTasks = new ArrayList<>(allTasks);
        } else {
            displayedTasks = new ArrayList<>();
            for (Task t : allTasks) {
                if (t.getStatus() == status) displayedTasks.add(t);
            }
        }
        notifyDataSetChanged();
    }

    /** Show only tasks with the given priority. Pass -1 to show all. */
    public void filterByPriority(int priority) {
        if (priority == -1) {
            displayedTasks = new ArrayList<>(allTasks);
        } else {
            displayedTasks = new ArrayList<>();
            for (Task t : allTasks) {
                if (t.getPriority() == priority) displayedTasks.add(t);
            }
        }
        notifyDataSetChanged();
    }

    /** Filter by title keyword (case-insensitive). Empty/null query shows all. */
    public void searchByTitle(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayedTasks = new ArrayList<>(allTasks);
        } else {
            String lower = query.toLowerCase().trim();
            displayedTasks = new ArrayList<>();
            for (Task t : allTasks) {
                if (t.getTitle() != null && t.getTitle().toLowerCase().contains(lower)) {
                    displayedTasks.add(t);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    // ---- Internal helpers ----

    private void showActionDialog(Task task) {
        new AlertDialog.Builder(context)
                .setTitle(task.getTitle())
                .setItems(new CharSequence[]{"Sửa", "Xóa"}, (dialog, which) -> {
                    if (listener == null) return;
                    if (which == 0) listener.onTaskEdit(task);
                    else            listener.onTaskDelete(task);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ---- ViewHolder ----

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTaskId;
        public TextView tvTitle;
        public TextView tvDescription;
        public TextView tvDueDate;
        public TextView tvPriority;
        public CheckBox cbStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskId      = itemView.findViewById(R.id.tvTaskId);
            tvTitle       = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDueDate     = itemView.findViewById(R.id.tvDueDate);
            tvPriority    = itemView.findViewById(R.id.tvPriority);
            cbStatus      = itemView.findViewById(R.id.cbStatus);
        }
    }
}

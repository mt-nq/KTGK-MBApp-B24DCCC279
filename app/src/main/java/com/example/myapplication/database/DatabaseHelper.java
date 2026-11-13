package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.model.Task;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 2;

    // Table & columns
    public static final String TABLE_TASKS      = "tasks";
    public static final String COL_ID           = "id";
    public static final String COL_TITLE        = "title";
    public static final String COL_DESCRIPTION  = "description";
    public static final String COL_DUE_DATE     = "due_date";
    public static final String COL_PRIORITY     = "priority";
    public static final String COL_STATUS       = "status";

    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
            COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE       + " TEXT NOT NULL, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_DUE_DATE    + " TEXT, " +
            COL_PRIORITY    + " INTEGER DEFAULT 0, " +
            COL_STATUS      + " INTEGER DEFAULT 0" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASKS);
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        String insertQuery = "INSERT INTO " + TABLE_TASKS + " (" + COL_TITLE + ", " + COL_DESCRIPTION + ", " + COL_DUE_DATE + ", " + COL_PRIORITY + ", " + COL_STATUS + ") VALUES ";
        db.execSQL(insertQuery + "('Hoàn thành bài tập lập trình', 'Viết unit test cho các function chính của ứng dụng', '31/12/2024', 2, 0)");
        db.execSQL(insertQuery + "('Mua sắm thực phẩm', 'Mua rau, thịt, cá và trái cây cho cả tuần', '25/11/2023', 0, 1)");
        db.execSQL(insertQuery + "('Dọn dẹp nhà cửa', 'Lau nhà, giặt đồ và dọn dẹp phòng ngủ', '26/11/2023', 1, 0)");
        db.execSQL(insertQuery + "('Học tiếng Anh', 'Học 50 từ vựng mới và làm bài tập ngữ pháp', '28/11/2023', 2, 0)");
        db.execSQL(insertQuery + "('Gặp gỡ đối tác', 'Thảo luận về dự án mới và ký kết hợp đồng', '30/11/2023', 2, 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // ---- CRUD ----

    /**
     * Insert a new task.
     * @return the row id of the newly inserted row, or -1 on error.
     */
    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildContentValues(task);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    /**
     * Update an existing task.
     * @return number of rows affected.
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildContentValues(task);
        int rows = db.update(TABLE_TASKS, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    /**
     * Delete a task by id.
     * @return number of rows deleted.
     */
    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // ---- Queries ----

    /** Return all tasks ordered by priority DESC, then id ASC. */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, null, null, null, null,
                COL_PRIORITY + " DESC, " + COL_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    /** Return tasks filtered by status (0=Not done, 1=Done). */
    public List<Task> getTasksByStatus(int status) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_STATUS + " = ?",
                new String[]{String.valueOf(status)},
                null, null,
                COL_PRIORITY + " DESC, " + COL_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    /** Return tasks filtered by priority (0=Low, 1=Medium, 2=High). */
    public List<Task> getTasksByPriority(int priority) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_PRIORITY + " = ?",
                new String[]{String.valueOf(priority)},
                null, null,
                COL_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    /** Search tasks whose title contains the keyword (case-insensitive). */
    public List<Task> searchTasksByTitle(String keyword) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TITLE + " LIKE ?",
                new String[]{"%" + keyword + "%"},
                null, null,
                COL_PRIORITY + " DESC, " + COL_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    /**
     * Return basic statistics.
     * @return int[] { total, done, notDone }
     */
    public int[] getStats() {
        int total = 0, done = 0, notDone = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_STATUS + ", COUNT(*) FROM " + TABLE_TASKS +
                " GROUP BY " + COL_STATUS, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int s = cursor.getInt(0);
                int count = cursor.getInt(1);
                total += count;
                if (s == 1) done += count;
                else notDone += count;
            }
            cursor.close();
        }
        db.close();
        return new int[]{total, done, notDone};
    }

    // ---- Helpers ----

    private ContentValues buildContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE,       task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE,    task.getDueDate());
        values.put(COL_PRIORITY,    task.getPriority());
        values.put(COL_STATUS,      task.getStatus());
        return values;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(       cursor.getInt(   cursor.getColumnIndexOrThrow(COL_ID)));
        task.setTitle(    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        task.setDueDate(  cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE)));
        task.setPriority( cursor.getInt(   cursor.getColumnIndexOrThrow(COL_PRIORITY)));
        task.setStatus(   cursor.getInt(   cursor.getColumnIndexOrThrow(COL_STATUS)));
        return task;
    }
}

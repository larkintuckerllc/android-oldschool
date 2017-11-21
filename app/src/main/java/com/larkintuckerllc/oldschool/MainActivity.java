package com.larkintuckerllc.oldschool;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.larkintuckerllc.oldschool.db.TodoContract;
import com.larkintuckerllc.oldschool.db.TodoDbHelper;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TodosAdapter mTodosAdapter;
    private TodoDbHelper mTodoDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lvTodos = findViewById(R.id.lvTodos);
        mTodoDbHelper = new TodoDbHelper(this);
        ArrayList<Todo> todos = new ArrayList<Todo>();
        // PERSIST
        SQLiteDatabase db = mTodoDbHelper.getReadableDatabase();
        Cursor cursor = db.query(TodoContract.TodoEntry.TABLE,
                new String[]{
                    TodoContract.TodoEntry._ID,
                    TodoContract.TodoEntry.COL_TODO_NAME,
                    TodoContract.TodoEntry.COL_TODO_DATE
                },
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idxId = cursor.getColumnIndex(TodoContract.TodoEntry._ID);
            int idxName = cursor.getColumnIndex(TodoContract.TodoEntry.COL_TODO_NAME);
            int idxDate = cursor.getColumnIndex(TodoContract.TodoEntry.COL_TODO_DATE);
            todos.add(new Todo(cursor.getLong(idxId), cursor.getString(idxName), cursor.getLong(idxDate)));
        }
        cursor.close();
        db.close();
        // VIEW
        mTodosAdapter =
            new TodosAdapter(this, todos);
        lvTodos.setAdapter(mTodosAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_todo:
                final EditText nameEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new todo")
                        .setMessage("What do you want to do next?")
                        .setView(nameEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name= String.valueOf(nameEditText.getText());
                                long date = (new Date()).getTime();
                                // PERSIST
                                SQLiteDatabase db = mTodoDbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TodoContract.TodoEntry.COL_TODO_NAME, name);
                                values.put(TodoContract.TodoEntry.COL_TODO_DATE, date);
                                long id = db.insertWithOnConflict(TodoContract.TodoEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                // VIEW
                                mTodosAdapter.add(new Todo(id, name, date));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void deleteTodo(View view) {
        View parent = (View) view.getParent();
        int position = (int) parent.getTag(R.id.POS);
        Todo todo = mTodosAdapter.getItem(position);
        // PERSIST
        SQLiteDatabase db = mTodoDbHelper.getWritableDatabase();
        db.delete(
            TodoContract.TodoEntry.TABLE,
            TodoContract.TodoEntry._ID + " = ?",
             new String[]{Long.toString(todo.getId())}
        );
        db.close();
        // VIEW
        mTodosAdapter.remove(todo);
    }

    public class TodosAdapter extends ArrayAdapter<Todo> {

       private class ViewHolder {
           TextView tvName;
           TextView tvDate;
       }

        public TodosAdapter(Context context, ArrayList<Todo> todos) {
            super(context, R.layout.item_todo , todos);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, ViewGroup parent) {
            Todo todo = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, parent, false);
                viewHolder.tvName = convertView.findViewById(R.id.tvName);
                viewHolder.tvDate = convertView.findViewById(R.id.tvDate);
                convertView.setTag(R.id.VH, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.id.VH);
            }
            viewHolder.tvName.setText(todo.getName());
            viewHolder.tvDate.setText((new Date(todo.getDate()).toString()));
            convertView.setTag(R.id.POS, position);
            return convertView;
        }

    }

}

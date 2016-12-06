package com.todo.vidyanandmishra.todoapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.MailTo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.todo.vidyanandmishra.todoapplication.BR;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    ArrayList<ToDoModel> listData;
    private ToDoDatabaseHelper toDoDatabaseHelper;
    private ToDoCursorAdapter toDoCursorAdapter;
    ListView lvToDos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listData = getHelper().getToDos();

        if(listData.isEmpty()) {
          listData = new ArrayList<>();
        }

        toDoCursorAdapter = new ToDoCursorAdapter(this, R.layout.item_todo, listData);

        lvToDos = (ListView) findViewById(R.id.lv_todo);
        lvToDos.setAdapter(toDoCursorAdapter);

        Button btnAddToDo = (Button) findViewById(R.id.btn_add_new);

        btnAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUpdateDialog("Add TODO", false, null, -1);
            }
        });

        lvToDos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ToDoModel toDoModel = (ToDoModel) toDoCursorAdapter.getItem(i);

                addUpdateDialog("Update TODO", true, toDoModel, i);
            }
        });

        lvToDos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete")
                        .setMessage("Are you sure, you want to delete?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ToDoModel toDoModel = (ToDoModel) toDoCursorAdapter.getItem(pos);

                                boolean result = getHelper().deleteToDo(toDoModel.id);

                                if(result) {
                                    Log.i(TAG, "Successfully deleted: "+toDoModel.id);
                                    listData.remove(pos);
                                    toDoCursorAdapter.notifyDataSetChanged();
                                } else {
                                    Log.e(TAG, "Error Deleting "+toDoModel.id);
                                }
                            }
                        }).create();

                alert.show();

                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        if (toDoDatabaseHelper != null) {
            toDoDatabaseHelper = null;
        }
        super.onDestroy();
    }


    private void addUpdateDialog(String title, final boolean isUpdate, final ToDoModel toDoModel, final int position) {
        View view1 = getLayoutInflater().inflate(R.layout.layout_input_dialog, null);

        final EditText etTitle = (EditText) view1.findViewById(R.id.et_title);
        final EditText etDescription = (EditText) view1.findViewById(R.id.et_description);

        if(isUpdate && toDoModel != null) {
            etTitle.setText(toDoModel.title);
            etDescription.setText(toDoModel.description);
        }

        AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setView(view1)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String title = etTitle.getText().toString();
                        String description = etDescription.getText().toString();

                        if(TextUtils.isEmpty(title)) {
                            etTitle.setError("Title Required");
                        } else if(TextUtils.isEmpty(description)){
                            etDescription.setError("Description Required");
                        } else {
                            saveToDo(etTitle.getText().toString(), etDescription.getText().toString(), isUpdate, toDoModel, position);
                        }
                    }
                }).create();

        alert.show();
    }

    private void saveToDo(String title, String description, boolean isUpdate, ToDoModel toDoModel, int position) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ToDoDatabaseHelper.COL_TITLE, title);
        contentValues.put(ToDoDatabaseHelper.COL_DESCRIPTION, description);

        boolean isChanged = false;
        long id = 0;

        if(isUpdate) {
            isChanged = getHelper().updateToDoDescription(toDoModel.id, contentValues);
        } else {
            id = getHelper().insertToDos(contentValues);
            isChanged = id > 0;
        }

        if(isChanged) {
            if(isUpdate) {
                Toast.makeText(MainActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                listData.get(position).title = title;
                listData.get(position).description = description;
            } else {
                Toast.makeText(MainActivity.this, "Inserted successfully!", Toast.LENGTH_SHORT).show();
                ToDoModel toDoModel1 = new ToDoModel();
                toDoModel1.id = String.valueOf(id);
                toDoModel1.title = title;
                toDoModel1.description = description;
                listData.add(toDoModel1);
            }
            toDoCursorAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(MainActivity.this, "Error while inserting!", Toast.LENGTH_SHORT).show();
        }
    }

    private ToDoDatabaseHelper getHelper() {
        if (toDoDatabaseHelper == null) {
            toDoDatabaseHelper = new ToDoDatabaseHelper(MainActivity.this);
        }
        return toDoDatabaseHelper;
    }

    public static class ToDoCursorAdapter extends ArrayAdapter<ToDoModel> {

        int layoutResourceId;
        Context context;
        ArrayList<ToDoModel> list;

        public ToDoCursorAdapter(Context context, int layoutResourceId, ArrayList<ToDoModel> list) {
            super(context, layoutResourceId, list);
            this.list = list;
            this.context = context;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder viewHolder;

            if(view == null) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                view = inflater.inflate(layoutResourceId, viewGroup, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final ToDoModel toDoModel = list.get(i);

            viewHolder.binding.setVariable(BR.toDoModel, toDoModel);

            return view;
        }

        private class ViewHolder {
            ViewDataBinding binding;

            public ViewHolder(View view) {
                binding = DataBindingUtil.bind(view);
            }
        }
    }
}

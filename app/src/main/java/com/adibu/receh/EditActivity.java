package com.adibu.receh;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;

import java.util.Calendar;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_RECEH_LOADER = 0;

    private EditText mTitleEditText;
    private EditText mValuesEditText;
    private RadioGroup mTransactionRadioGroup;
    private RadioButton mTransactionIncomeRadio;
    private RadioButton mTransactionOutcomeRadio;
    private EditText mDescriptionEditText;
    private EditText mDateEditText;
    private EditText mTimeEditText;

    private Uri mCurrentDataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mCurrentDataUri = intent.getData();

        if (mCurrentDataUri == null) {
            setTitle(R.string.edit_activity_add_data);
        }
        else {
            setTitle(R.string.edit_activity_edit_data);
            getSupportLoaderManager().initLoader(EXISTING_RECEH_LOADER, null, this);
        }


        mTitleEditText = (EditText)findViewById(R.id.activity_edit_title);
        mValuesEditText = (EditText)findViewById(R.id.activity_edit_values);
        mTransactionRadioGroup = (RadioGroup)findViewById(R.id.activity_edit_transaction);
        mTransactionIncomeRadio = (RadioButton)findViewById(R.id.activity_edit_income);
        mTransactionOutcomeRadio = (RadioButton)findViewById(R.id.activity_edit_outcome);
        mDescriptionEditText = (EditText)findViewById(R.id.activity_edit_description);
        mDateEditText = (EditText)findViewById(R.id.activity_edit_date);
        mTimeEditText = (EditText)findViewById(R.id.activity_edit_time);

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        String monthString = String.valueOf(month);
                        if (monthString.length()==1) {
                            monthString = "0" + monthString;
                        }
                        String dayString = String.valueOf(dayOfMonth);
                        if (dayString.length()==1) {
                            dayString = "0" + dayString;
                        }
                        mDateEditText.setText(year + "-" + monthString + "-" + dayString);
                    }
                };

                DatePickerDialog dateDialog = new DatePickerDialog(EditActivity.this, 0, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dateDialog.show();
            }
        });
        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String hourString = String.valueOf(hourOfDay);
                        if (hourString.length()==1) {
                            hourString = "0" + hourString;
                        }
                        String minuteString = String.valueOf(minutes);
                        if (minuteString.length()==1) {
                            minuteString = "0" + minuteString;
                        }
                        mTimeEditText.setText(hourString + ":" + minuteString);
                    }
                };

                TimePickerDialog timeDialog = new TimePickerDialog(EditActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timeDialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                saveItemData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItemData() {

        String title = mTitleEditText.getText().toString().trim();
        String values = mValuesEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString().trim();
        String date = mDateEditText.getText().toString().trim();
        String time = mTimeEditText.getText().toString().trim();

        Integer transaction = null;
        int radioSelectedId = mTransactionRadioGroup.getCheckedRadioButtonId();
        if (radioSelectedId == R.id.activity_edit_income) {
            transaction = RecehContract.RecehEntry.TRANSACTION_INCOME;
        } else if (radioSelectedId == R.id.activity_edit_outcome){
            transaction = RecehContract.RecehEntry.TRANSACTION_OUTCOME;
        }

        if(title.isEmpty()) {
            mTitleEditText.setError(getString(R.string.edit_activity_empty_title));
        }
        if (values.isEmpty()) {
            mValuesEditText.setError(getString(R.string.edit_activity_empty_values));
        }
        if (date.isEmpty()) {
            mDateEditText.setError(getString(R.string.edit_activity_empty_date));
        }
        if (time.isEmpty()) {
            mTimeEditText.setError(getString(R.string.edit_activity_empty_time));
        }
        
        if (!title.isEmpty() && !values.isEmpty() && !date.isEmpty() && !time.isEmpty()) {

            Integer intValues = Integer.parseInt(values);
            // Create a new map of values, where column names are the keys
            ContentValues contentValues = new ContentValues();
            contentValues.put(RecehContract.RecehEntry.COLUMN_TITLE, title);
            contentValues.put(RecehContract.RecehEntry.COLUMN_VALUES, intValues);
            contentValues.put(RecehContract.RecehEntry.COLUMN_TRANSACTION, transaction);
            contentValues.put(RecehContract.RecehEntry.COLUMN_DESCRIPTION, description);
            String datetime = date + " " + time;
            contentValues.put(RecehContract.RecehEntry.COLUMN_DATETIME, datetime);
            contentValues.put(RecehContract.RecehEntry.COLUMN_TABLE_CONTENT_ID, RecehContract.RecehEntry.SELECTED_TABLE_CONTENT_ID);

            if(mCurrentDataUri == null) {
                // Insert a new item into the provider, returning the content URI for the new item.
                Uri newUri = getContentResolver().insert(RecehContract.RecehEntry.CONTENT_URI, contentValues);
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.edit_activity_error_insert),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.edit_activity_successfull_insert),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int updateRow = getContentResolver().update(mCurrentDataUri, contentValues, null, null);
                if (updateRow == 0) {
                    Toast.makeText(this, getString(R.string.edit_activity_error_update),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.edit_activity_successfull_update),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                RecehContract.RecehEntry._ID,
                RecehContract.RecehEntry.COLUMN_TITLE,
                RecehContract.RecehEntry.COLUMN_VALUES,
                RecehContract.RecehEntry.COLUMN_TRANSACTION,
                RecehContract.RecehEntry.COLUMN_DATETIME,
                RecehContract.RecehEntry.COLUMN_DESCRIPTION
        };
        return new CursorLoader(this,
                mCurrentDataUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() <1) {
            return;
        }

        if (data.moveToFirst()) {
            String title = data.getString(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_TITLE));
            mTitleEditText.setText(title);

            Integer values = data.getInt(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_VALUES));
            mValuesEditText.setText(values.toString());

            int transaction = data.getInt(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_TRANSACTION));
            if (transaction == RecehContract.RecehEntry.TRANSACTION_INCOME) {
                mTransactionIncomeRadio.setChecked(true);
            }
            else if (transaction == RecehContract.RecehEntry.TRANSACTION_OUTCOME) {
                mTransactionOutcomeRadio.setChecked(true);
            }

            String datetime = data.getString(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_DATETIME));
            //datetime = YYYY-MM-DD HH:MM
            String date = datetime.substring(0,10);
            String time = datetime.substring(11);
            mDateEditText.setText(date);
            mTimeEditText.setText(time);

            String description = data.getString(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_DESCRIPTION));
            mDescriptionEditText.setText(description);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleEditText.setText("");
        mValuesEditText.setText("");
        mTransactionIncomeRadio.setChecked(true);
        mDescriptionEditText.setText("");
    }
}

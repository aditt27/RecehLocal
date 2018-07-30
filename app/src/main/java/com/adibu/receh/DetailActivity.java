package com.adibu.receh;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_RECEH_LOADER = 0;

    private TextView mTitleTV;
    private TextView mTransactionTV;
    private TextView mValuesTV;
    private TextView mDateTimeTV;
    private TextView mDescriptionTV;

    private Uri mCurrentDataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentDataUri = intent.getData();
        getSupportLoaderManager().initLoader(EXISTING_RECEH_LOADER, null, this);

        mTitleTV = (TextView)findViewById(R.id.activity_detail_title);
        mTransactionTV = (TextView)findViewById(R.id.activity_detail_transaction);
        mValuesTV = (TextView)findViewById(R.id.activity_detail_values);
        mDescriptionTV = (TextView)findViewById(R.id.activity_detail_description);
        mDateTimeTV = (TextView)findViewById(R.id.activity_detail_datetime);
        mDescriptionTV.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_detail_edit:
                Intent intent = new Intent(DetailActivity.this, EditActivity.class);
                intent.setData(mCurrentDataUri);
                startActivity(intent);
                return true;
            case R.id.menu_detail_delete:
                deleteItemWithConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItemWithConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_activity_delete_confirmation_title);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteData();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteData() {
        int deletedRow = getContentResolver().delete(mCurrentDataUri, null, null);
        if (deletedRow == 0) {
            Toast.makeText(this, getString(R.string.detail_activity_delete_error), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, getString(R.string.detail_activity_delete_success), Toast.LENGTH_SHORT).show();
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
            mTitleTV.setText(title);

            Integer values = data.getInt(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_VALUES));
            mValuesTV.setText(values.toString());

            int transaction = data.getInt(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_TRANSACTION));
            if (transaction == RecehContract.RecehEntry.TRANSACTION_INCOME) {
                mTransactionTV.setText(getString(R.string.detail_activity_income));
            }
            else if (transaction == RecehContract.RecehEntry.TRANSACTION_OUTCOME) {
                mTransactionTV.setText(getString(R.string.detail_activity_outcome));
            }

            String datetime = data.getString(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_DATETIME));
            mDateTimeTV.setText(datetime);


            String description = data.getString(data.getColumnIndex(RecehContract.RecehEntry.COLUMN_DESCRIPTION));
            mDescriptionTV.setText(description);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleTV.setText("No data");
        mValuesTV.setText("No data");
        mTransactionTV.setText("No data");
        mDescriptionTV.setText("No data");
    }
}

package com.adibu.receh.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adibu.receh.R;
import com.adibu.receh.data.RecehContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private TextView total;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        total = (TextView)rootView.findViewById(R.id.stats_fragment_total_number);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySumTransactionValues();
    }

    private void displaySumTransactionValues() {
        String[] projection = {RecehContract.RecehEntry.COLUMN_VALUES, RecehContract.RecehEntry.COLUMN_TRANSACTION};
        String selection = RecehContract.RecehEntry.COLUMN_TABLE_CONTENT_ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(RecehContract.RecehEntry.SELECTED_TABLE_CONTENT_ID)};
        Cursor cursor = getActivity().getContentResolver().query(
                RecehContract.RecehEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        try {
            int valuesColumnIndex = cursor.getColumnIndex(RecehContract.RecehEntry.COLUMN_VALUES);
            int transactionColumnIndex = cursor.getColumnIndex(RecehContract.RecehEntry.COLUMN_TRANSACTION);
            Integer sum = 0;
            while (cursor.moveToNext()) {
                int values = cursor.getInt(valuesColumnIndex);
                int transaction = cursor.getInt(transactionColumnIndex);

                if(transaction == RecehContract.RecehEntry.TRANSACTION_OUTCOME)
                    values = -values;

                sum += values;
            }
            total.setText(sum.toString());
        }
        finally {
            cursor.close();
        }
    }

}

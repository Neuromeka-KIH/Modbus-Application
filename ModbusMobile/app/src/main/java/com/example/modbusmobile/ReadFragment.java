package com.example.modbusmobile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class ReadFragment extends Fragment {

    private EditText modbusAddressEditText1;
    private EditText modbusAddressEditText2;
    private EditText modbusAddressEditText3;
    private EditText modbusAddressEditText4;
    private TextView dataTextView1;
    private TextView dataTextView2;
    private TextView dataTextView3;
    private TextView dataTextView4;
    private Button buttonRead;
    private Context mContext;

    public static ReadFragment newInstance() {
        return new ReadFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        mContext = getContext();

        modbusAddressEditText1 = view.findViewById(R.id.readTextModbusAddress1);
        modbusAddressEditText2 = view.findViewById(R.id.readTextModbusAddress2);
        modbusAddressEditText3 = view.findViewById(R.id.readTextModbusAddress3);
        modbusAddressEditText4 = view.findViewById(R.id.readTextModbusAddress4);
        buttonRead = view.findViewById(R.id.buttonRead);
        dataTextView1 = view.findViewById(R.id.TextValueToRead1);
        dataTextView2 = view.findViewById(R.id.TextValueToRead2);
        dataTextView3 = view.findViewById(R.id.TextValueToRead3);
        dataTextView4 = view.findViewById(R.id.TextValueToRead4);

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ReadRegistersTask().execute(
                        new EditTextTextViewPair(modbusAddressEditText1, dataTextView1),
                        new EditTextTextViewPair(modbusAddressEditText2, dataTextView2),
                        new EditTextTextViewPair(modbusAddressEditText3, dataTextView3),
                        new EditTextTextViewPair(modbusAddressEditText4, dataTextView4)
                );
            }
        });

        return view;
    }

    private static class ReadRegistersTask extends AsyncTask<EditTextTextViewPair, Void, Integer> {
        private Context mContext;
        private EditTextTextViewPair[] mPairs;

        @Override
        protected Integer doInBackground(EditTextTextViewPair... pairs) {
            mPairs = pairs;
            int registerValue = 0;

            for (EditTextTextViewPair pair : pairs) {
                EditText editText = pair.editText;

                try {
                    ModbusClient modbusClient = new ModbusClient();
                    int modbusAddress = Integer.parseInt(editText.getText().toString());
                    modbusClient.Connect();
                    int[] registerValues = modbusClient.ReadHoldingRegisters(modbusAddress, 1);
                    modbusClient.Disconnect();
                    if (registerValues.length > 0) {
                        registerValue = registerValues[0];
                    }
                } catch (IOException | ModbusException e) {
                    // Log the error or handle it as needed
                    e.printStackTrace();
                }
            }

            return registerValue;
        }

        @Override
        protected void onPostExecute(Integer registerValue) {
            super.onPostExecute(registerValue);

            // UI 업데이트를 메인(UI) 스레드에서 처리
            if (mContext != null) {
                String message;
                if (registerValue != null) {
                    message = "읽었습니다";
                } else {
                    message = "읽는 중입니다";
                }
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }

            // UI 업데이트
            for (EditTextTextViewPair pair : mPairs) {
                final TextView textViewToUpdate = pair.textView;
                textViewToUpdate.setText(String.valueOf(registerValue));
            }
        }
    }

    private static class EditTextTextViewPair {
        EditText editText;
        TextView textView;

        EditTextTextViewPair(EditText editText, TextView textView) {
            this.editText = editText;
            this.textView = textView;
        }
    }
}

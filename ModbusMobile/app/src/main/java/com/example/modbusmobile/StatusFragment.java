package com.example.modbusmobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class StatusFragment extends Fragment {

    private TextView[] angleTextViews = new TextView[6];
    private TextView[] torqueTextViews = new TextView[6];
    private TextView[] coordTextViews = new TextView[6];

    private boolean isToastShown = false;

    private Handler handler;
    private Runnable updateRunnable;

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        angleTextViews[0] = view.findViewById(R.id.angle1);
        angleTextViews[1] = view.findViewById(R.id.angle2);
        angleTextViews[2] = view.findViewById(R.id.angle3);
        angleTextViews[3] = view.findViewById(R.id.angle4);
        angleTextViews[4] = view.findViewById(R.id.angle5);
        angleTextViews[5] = view.findViewById(R.id.angle6);

        torqueTextViews[0] = view.findViewById(R.id.torque1);
        torqueTextViews[1] = view.findViewById(R.id.torque2);
        torqueTextViews[2] = view.findViewById(R.id.torque3);
        torqueTextViews[3] = view.findViewById(R.id.torque4);
        torqueTextViews[4] = view.findViewById(R.id.torque5);
        torqueTextViews[5] = view.findViewById(R.id.torque6);

        coordTextViews[0] = view.findViewById(R.id.coordX);
        coordTextViews[1] = view.findViewById(R.id.coordY);
        coordTextViews[2] = view.findViewById(R.id.coordZ);
        coordTextViews[3] = view.findViewById(R.id.coordU);
        coordTextViews[4] = view.findViewById(R.id.coordV);
        coordTextViews[5] = view.findViewById(R.id.coordW);

        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                new ReadValuesTask().execute();
                handler.postDelayed(this, 500); // 500ms 후에 다시 실행
            }
        };

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.post(updateRunnable); // 처음 실행

        checkServerConnectionAndShowToast();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }
    private void checkServerConnectionAndShowToast() {
        ModbusClient modbusClient = ((MainActivity) requireActivity()).getModbusClient();
        if (modbusClient == null || !modbusClient.isConnected()) {
            if (!isToastShown) {
                showToast("모드버스 서버와 연결되지 않았습니다.");
                isToastShown = true;
            }
        }
    }

    private class ReadValuesTask extends AsyncTask<Void, Void, Values> {
        @Override
        protected Values doInBackground(Void... voids) {
            Values values = new Values();

            try {
                ModbusClient modbusClient = ((MainActivity) requireActivity()).getModbusClient();
                if (modbusClient != null && modbusClient.isConnected()) {
                    int[] angleValues = modbusClient.ReadHoldingRegisters(1300, 6);
                    int[] torqueValues = modbusClient.ReadHoldingRegisters(1380, 6);
                    int[] coordValues = modbusClient.ReadHoldingRegisters(1600, 6);

                    for (int i = 0; i < 6; i++) {
                        values.angles[i] = angleValues[i];
                        values.torques[i] = torqueValues[i];
                        values.coords[i] = coordValues[i];
                    }
                }
            } catch (IOException | ModbusException e) {
                e.printStackTrace();
            }

            return values;
        }

        @Override
        protected void onPostExecute(Values values) {
            super.onPostExecute(values);

            // TextView에 값 설정
            for (int i = 0; i < 6; i++) {
                angleTextViews[i].setText(String.valueOf(values.angles[i]));
                torqueTextViews[i].setText(String.valueOf(values.torques[i]));
                coordTextViews[i].setText(String.valueOf(values.coords[i]));
            }
        }
    }

    private static class Values {
        int[] angles = new int[6];
        int[] torques = new int[6];
        int[] coords = new int[6];
    }
    private void showToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}
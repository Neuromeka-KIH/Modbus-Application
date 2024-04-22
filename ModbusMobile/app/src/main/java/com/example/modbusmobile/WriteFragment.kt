package com.example.modbusmobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.re.easymodbus.modbusclient.ModbusClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WriteFragment : Fragment() {
    private lateinit var modbusAddressEditText1: EditText
    private lateinit var modbusAddressEditText2: EditText
    private lateinit var modbusAddressEditText3: EditText
    private lateinit var modbusAddressEditText4: EditText
    private lateinit var valueToSendEditText1: EditText
    private lateinit var valueToSendEditText2: EditText
    private lateinit var valueToSendEditText3: EditText
    private lateinit var valueToSendEditText4: EditText
    private lateinit var sendButton: Button
    private var modbusClient: ModbusClient? = null // MainActivity에서 가져올 모드버스 클라이언트

    companion object {
        fun newInstance(): WriteFragment {
            return WriteFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_write, container, false)

        modbusAddressEditText1 = view.findViewById(R.id.editTextModbusAddress1)
        modbusAddressEditText2 = view.findViewById(R.id.editTextModbusAddress2)
        modbusAddressEditText3 = view.findViewById(R.id.editTextModbusAddress3)
        modbusAddressEditText4 = view.findViewById(R.id.editTextModbusAddress4)
        valueToSendEditText1 = view.findViewById(R.id.editTextValueToSend1)
        valueToSendEditText2 = view.findViewById(R.id.editTextValueToSend2)
        valueToSendEditText3 = view.findViewById(R.id.editTextValueToSend3)
        valueToSendEditText4 = view.findViewById(R.id.editTextValueToSend4)
        sendButton = view.findViewById(R.id.buttonSend)

        modbusClient = (requireActivity() as MainActivity).getModbusClient()

        sendButton.setOnClickListener {
            sendValuesToModbus()
        }

        return view
    }

    private fun sendValuesToModbus() {
        val modbusAddresses = listOf(
            modbusAddressEditText1.text.toString().toIntOrNull(),
            modbusAddressEditText2.text.toString().toIntOrNull(),
            modbusAddressEditText3.text.toString().toIntOrNull(),
            modbusAddressEditText4.text.toString().toIntOrNull()
        )

        val valuesToSend = listOf(
            valueToSendEditText1.text.toString().toIntOrNull(),
            valueToSendEditText2.text.toString().toIntOrNull(),
            valueToSendEditText3.text.toString().toIntOrNull(),
            valueToSendEditText4.text.toString().toIntOrNull()
        )

        // 입력된 주소와 값의 쌍들을 모은 리스트
        val addressValuePairs = modbusAddresses.zip(valuesToSend)

        modbusClient?.let { client ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    client.Connect()
                    val notifications = StringBuilder()
                    addressValuePairs.forEach { (address, value) ->
                        if (address != null && value != null) {
                            client.WriteSingleRegister(address, value)
                            notifications.append("주소: $address, 값: $value\n")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (notifications.isNotEmpty()) {
                            showToast("Modbus 데이터 전송 완료\n$notifications")
                        } else {
                            showToast("주소와 값을 입력해주세요.")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("전송 중 오류 발생: ${e.message}")
                    }
                } finally {
                    client.Disconnect()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.modbusmobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import de.re.easymodbus.modbusclient.ModbusClient
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var ipEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var connectButton: Button
    private lateinit var tabLayout: TabLayout

    private var modbusClient: ModbusClient? = null
    private var readFragment: ReadFragment? = null
    private var writeFragment: WriteFragment? = null
    private var statusFragment: StatusFragment? = null

    fun getModbusClient(): ModbusClient? {
        return modbusClient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ipEditText = findViewById(R.id.ipnumber)
        portEditText = findViewById(R.id.portnumber)
        connectButton = findViewById(R.id.connectbutton)
        tabLayout = findViewById(R.id.tabLayout)

        connectButton.setOnClickListener { connectToModbusServer() }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "Modbus 읽기" -> showReadFragment()
                    "Modbus 쓰기" -> showWriteFragment()
                    "Robot Status" -> showStatusFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showReadFragment()
    }
    private fun connectToModbusServer() {
        val ipAddress = ipEditText.text.toString()
        val portNumberStr = portEditText.text.toString()

        // IP 주소가 비어 있는지 확인
        if (ipAddress.isEmpty()) {
            showToast("IP 주소를 입력하세요.")
            return
        }

        // 포트 번호가 비어 있는지 확인
        if (portNumberStr.isEmpty()) {
            showToast("포트 번호를 입력하세요.")
            return
        }

        val portNumber = try {
            portNumberStr.toInt()
        } catch (e: NumberFormatException) {
            showToast("포트 번호는 숫자로 입력해야 합니다.")
            return
        }

        // 백그라운드 스레드에서 Modbus 연결을 시도
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Modbus 클라이언트 초기화
                modbusClient = ModbusClient(ipAddress, portNumber)
                modbusClient?.Connect()

                if (modbusClient?.isConnected() == true) {
                    showToast("Modbus 서버에 연결되었습니다.")
                } else {
                    showToast("Modbus 서버에 연결할 수 없습니다.")
                }
            } catch (e: ConnectException) {
                showToast("Modbus 연결 실패: 연결이 거부되었습니다.")
            } catch (e: UnknownHostException) {
                showToast("Modbus 연결 실패: 호스트를 찾을 수 없습니다.")
            } catch (e: SocketTimeoutException) {
                showToast("Modbus 연결 실패: 연결 시간이 초과되었습니다.")
            } catch (e: Exception) {
                showToast("Modbus 연결 실패: ${e.message}")
            } finally {
                // 연결 종료
                modbusClient?.Disconnect()
            }
        }
    }

    private fun showReadFragment() {
        val fragment = readFragment ?: ReadFragment.newInstance()
        readFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showWriteFragment() {
        val fragment = writeFragment ?: WriteFragment.newInstance()
        writeFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showStatusFragment() {
        val fragment = statusFragment ?: StatusFragment.newInstance()
        statusFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }



    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

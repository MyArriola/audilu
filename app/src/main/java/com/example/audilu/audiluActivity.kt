package com.example.audilu
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.util.*


class audiluActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audilu)

        if (bluetoothAdapter == null) {
            // El dispositivo no admite Bluetooth
            Log.e("Bluetooth", "Bluetooth no es soportado en este dispositivo")
            return
        }
        val deviceAddress = "98:D3:36:81:02:77"
        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)

        coroutineScope.launch {
            try {
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@launch
                }
                bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream

                readData()
            } catch (e: IOException) {
                Log.e("Bluetooth", "Error estableciendo conexión", e)
            }
        }
    }

    private fun readData() {
        while (true) {
            try {
                val buffer = ByteArray(1024)
                val bytesRead = inputStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    val receivedData = String(buffer, 0, bytesRead)
                    Log.d("Bluetooth", "Received data: $receivedData")
                    // Aquí puedes procesar los datos recibidos según sea necesario
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Error leyendo desde Bluetooth", e)
                break
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancelar el trabajo en segundo plano al destruir la actividad
        try {
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error closing Bluetooth connection", e)
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
//variables para el bluetooth
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

}
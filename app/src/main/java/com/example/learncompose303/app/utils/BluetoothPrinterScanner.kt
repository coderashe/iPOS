package com.example.learncompose303.app.utils

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.util.UUID

class BluetoothPrinterScanner(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var bluetoothSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    fun connectToHardcodedPrinter(printData: String) {
        val deviceMacAddress = "02:20:85:53:BB:E6" // Hardcoded MAC address
        val device = bluetoothAdapter?.getRemoteDevice(deviceMacAddress)

        if (device == null) {
            Log.e("Bluetooth", "Device not found with MAC address: $deviceMacAddress")
            return
        }

        connectToPrinter(device, printData)
    }

    @SuppressLint("MissingPermission")
    private fun connectToPrinter(device: BluetoothDevice, printData: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                bluetoothSocket =
                    device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                bluetoothSocket?.connect()

                Log.i("Bluetooth", "Connected to printer: ${device.name}")

                val outputStream = bluetoothSocket?.outputStream
                if (outputStream == null) {
                    Log.e("Bluetooth", "Output stream is null!")
                    return@launch
                }

                // Initialize Printer
                outputStream.write(byteArrayOf(0x1B, 0x40)) // ESC @ (Reset)
                outputStream.write(byteArrayOf(0x1B, 0x33, 0x10)) // ESC 3 16 (Set line spacing)

                // Print user-defined text
                outputStream.write(printData.encodeToByteArray())
                outputStream.write("\r\n".encodeToByteArray()) // Ensure new line

                // Add spacing before cutting
                outputStream.write("\n\n\n\n\n".encodeToByteArray())

                // Force print & Paper Cut
                outputStream.write(byteArrayOf(0x0C)) // Form feed (forces print)
                outputStream.write(byteArrayOf(0x1D, 0x56, 0x00)) // ESC i (partial cut)

                outputStream.flush()

                // Add a short delay before closing
                delay(900)

                outputStream.close()
                bluetoothSocket?.close()
                Log.i("Bluetooth", "Printing completed.")

            } catch (e: IOException) {
                Log.e("Bluetooth", "Connection failed: ${e.message}")
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun connectToPrinterTest(device: BluetoothDevice) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                bluetoothSocket =
                    device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                bluetoothSocket?.connect()

                Log.i("Bluetooth", "Connected to printer: ${device.name}")

                val outputStream = bluetoothSocket?.outputStream
                if (outputStream == null) {
                    Log.e("Bluetooth", "Output stream is null!")
                    return@launch
                }

                // Initialize Printer
                outputStream.write(byteArrayOf(0x1B, 0x40)) // ESC @ (Reset)
                outputStream.write(byteArrayOf(0x1B, 0x33, 0x10)) // ESC 3 16 (Set line spacing)

                // Print text
                outputStream.write("Simba Start is a great developer\r\n".encodeToByteArray())
                outputStream.write("Happy coding\r\n".encodeToByteArray())
                outputStream.write("Kellaz is a scam, ndapedza!!\r\n".encodeToByteArray())

                // Add spacing before cutting
                outputStream.write("\n\n\n".encodeToByteArray())

                // Force print & Paper Cut
                outputStream.write(byteArrayOf(0x0C)) // Form feed (forces print)
                outputStream.write(byteArrayOf(0x1D, 0x56, 0x00)) // ESC i (partial cut)

                outputStream.flush()

                // Add a short delay before closing
                delay(900)

                outputStream.close()
                bluetoothSocket?.close()
                Log.i("Bluetooth", "Printing completed.")

            } catch (e: IOException) {
                Log.e("Bluetooth", "Connection failed: ${e.message}")
            }
        }
    }

}

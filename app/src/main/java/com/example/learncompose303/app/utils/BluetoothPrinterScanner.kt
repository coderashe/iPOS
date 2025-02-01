package com.example.learncompose303.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import kotlinx.coroutines.*
import java.util.UUID

//class BluetoothPrinterScanner(private val context: Context) {
//    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//    private val coroutineScope = CoroutineScope(Dispatchers.IO)
//    private var bluetoothSocket: BluetoothSocket? = null
//
//    @SuppressLint("MissingPermission")
//    fun connectToHardcodedPrinter() {
//        val deviceMacAddress = "02:20:85:53:BB:E6" // Hardcoded MAC address
//        val device = bluetoothAdapter?.getRemoteDevice(deviceMacAddress)
//
//        if (device == null) {
//            Log.e("Bluetooth", "Device not found with MAC address: $deviceMacAddress")
//            return
//        }
//
//        connectToPrinter(device)
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun connectToPrinter(device: BluetoothDevice) {
//        coroutineScope.launch(Dispatchers.IO) {
//            try {
//                val bluetoothConnection = BluetoothConnection(device)
//                if (!bluetoothConnection.isConnected) {
//                    bluetoothConnection.connect()
//                }
//
//                Log.i("Bluetooth", "Connected to printer: ${device.name}")
//
//                val printer = EscPosPrinter(
//                    bluetoothConnection,  // Use BluetoothConnection
//                    203,  // DPI
//                    80f,  // Width in mm
//                    32    // Characters per line
//                )
//
//                printer.printFormattedText("[C]Simba Start is a great developer\n")
//                printer.printFormattedText("[C]Happy coding\n")
//                printer.printFormattedText("[C]Kellaz is a scam, ndapedza!!\n")
//
//                // Simulate paper cut (ESC/POS command)
//                printer.printFormattedText("\n\u001DVA0")
//
//                bluetoothConnection.disconnect()
//                Log.i("Bluetooth", "Printing completed.")
//            } catch (e: Exception) {
//                Log.e("Bluetooth", "Connection failed: ${e.message}")
//            }
//        }
//    }
//}

class BluetoothPrinterScanner(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var bluetoothSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    fun connectToHardcodedPrinter() {
        val deviceMacAddress = "02:20:85:53:BB:E6" // Hardcoded MAC address
        val device = bluetoothAdapter?.getRemoteDevice(deviceMacAddress)

        if (device == null) {
            Log.e("Bluetooth", "Device not found with MAC address: $deviceMacAddress")
            return
        }

        connectToPrinter(device)
    }

    @SuppressLint("MissingPermission")
    private fun connectToPrinter(device: BluetoothDevice) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val bluetoothConnection = BluetoothConnection(device)
                if (!bluetoothConnection.isConnected) {
                    bluetoothConnection.connect()
                }

                Log.i("Bluetooth", "Connected to printer: ${device.name}")

                val printer = EscPosPrinter(
                    bluetoothConnection,  // Use BluetoothConnection
                    203,  // DPI
                    58f,  // Width in mm
                    32    // Characters per line
                )

                // Print each line without extra spacing
                printer.printFormattedText("[C]Simba Start: Android Developer")
                printer.printFormattedText("[C]Happy coding with ISTART_K")
                printer.printFormattedText("[C]Kellaz watch the space, ndapedza!!!")

                // Simulate paper cut (ESC/POS command) with minimal spacing
                printer.printFormattedText("\u001DVA0")

                bluetoothConnection.disconnect()
                Log.i("Bluetooth", "Printing completed.")
            } catch (e: Exception) {
                Log.e("Bluetooth", "Connection failed: ${e.message}")
            }
        }
    }
}


//class BluetoothPrinterScanner(private val context: Context) {
//    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//    private val coroutineScope = CoroutineScope(Dispatchers.IO)
//    private var bluetoothSocket: BluetoothSocket? = null
//
//    @SuppressLint("MissingPermission")
//    fun startScanning() {
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            Log.e("Bluetooth", "Bluetooth is not available or not enabled.")
//            return
//        }
//
//        // Check and request permissions for Android 12+ at runtime
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(
//                    context, Manifest.permission.BLUETOOTH_SCAN
//                ) != PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(
//                    context, Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.e("Bluetooth", "Bluetooth permissions are missing. Requesting now.")
//
//                // Request permissions dynamically (assuming this is called in an Activity)
//                if (context is android.app.Activity) {
//                    ActivityCompat.requestPermissions(
//                        context,
//                        arrayOf(
//                            Manifest.permission.BLUETOOTH_SCAN,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ),
//                        101 // Request code
//                    )
//                }
//                return
//            }
//        }
//
//        // Register BroadcastReceiver for device discovery
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        context.registerReceiver(bluetoothReceiver, filter)
//
//        // Start discovery
//        if (bluetoothAdapter.isDiscovering) {
//            bluetoothAdapter.cancelDiscovery()
//        }
//        bluetoothAdapter.startDiscovery()
//        Log.i("Bluetooth", "Scanning for Bluetooth devices...")
//    }
//
//    @SuppressLint("MissingPermission")
//    private val bluetoothReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action: String? = intent.action
//            if (BluetoothDevice.ACTION_FOUND == action) {
//                val device: BluetoothDevice? =
//                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                val deviceName = device?.name ?: "Unknown Device"
//                val deviceAddress = device?.address ?: "Unknown Address"
//
//                Log.i("Bluetooth", "Discovered: $deviceName - $deviceAddress")
//
//                if (device != null) {
//                    if (deviceName.contains("POS", ignoreCase = true) ||
//                        device.bluetoothClass.deviceClass == BluetoothClass.Device.Major.IMAGING // Printer class
//                    ) {
//                        Log.i("Bluetooth", "Bluetooth Printer Found: $deviceName")
//                        bluetoothAdapter?.cancelDiscovery()
//                        connectToPrinter(device)
//                        context.unregisterReceiver(this) // Stop scanning after finding a printer
//                    }
//                }
//            }
//        }
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    @SuppressLint("MissingPermission")
//    private fun connectToPrinter(device: BluetoothDevice) {
//        coroutineScope.launch(Dispatchers.IO) {
//            try {
//                val bluetoothConnection = BluetoothConnection(device)
//                if (!bluetoothConnection.isConnected) {
//                    bluetoothConnection.connect()
//                }
//
//                Log.i("Bluetooth", "Connected to printer: ${device.name}")
//
//                val printer = EscPosPrinter(
//                    bluetoothConnection,  // Use BluetoothConnection
//                    203,  // DPI
//                    80f,  // Width in mm
//                    32    // Characters per line
//                )
//
//                printer.printFormattedText("[C]Sample Text for Testing\n")
//                printer.printFormattedText("[C]Thank you for using our service!\n")
//                printer.printFormattedText("[C]Have a great day!\n")
//
//                // Simulate paper cut (ESC/POS command)
//                printer.printFormattedText("\n\n\n\n\n\u001DVA0")
//
//                bluetoothConnection.disconnect()
//                Log.i("Bluetooth", "Printing completed.")
//            } catch (e: Exception) {
//                Log.e("Bluetooth", "Connection failed: ${e.message}")
//            }
//        }
//    }
//
//    fun ensureBluetoothEnabled() {
//        if (bluetoothAdapter == null) {
//            Log.e("Bluetooth", "Bluetooth not supported on this device.")
//            return
//        }
//
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            if (context is android.app.Activity) {
//                if (ActivityCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return
//                }
//                (context as android.app.Activity).startActivityForResult(enableBtIntent, 102)
//            }
//        }
//    }
//
//
//    fun stopScanning() {
//        try {
//            context.unregisterReceiver(bluetoothReceiver)
//        } catch (e: IllegalArgumentException) {
//            Log.w("Bluetooth", "Receiver was not registered or already unregistered.")
//        }
//    }
//}

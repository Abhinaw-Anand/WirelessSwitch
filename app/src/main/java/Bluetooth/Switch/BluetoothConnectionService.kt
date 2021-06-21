package Bluetooth.Switch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class BluetoothConnectionService {
    var connectionstatus = false
    private val mBluetoothAdapter: BluetoothAdapter
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mmDevice: BluetoothDevice? = null
    private var deviceUUID: UUID? = null
    private var mConnectedThread: ConnectedThread? = null
    private  val TAG = "TAG"
    private  val appName = "BluetoothSwitch"
    private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    init {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        start()
    }

    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket?
        override fun run() {
            Log.d(TAG, " AcceptThread Running.")
            var socket: BluetoothSocket? = null
            try {
                Log.d(TAG, "RFCOM server socket start.")
                socket = mmServerSocket!!.accept()
                Log.d(TAG, "RFCOM server socket accepted connection.")
            } catch (e: IOException) {
                Log.e(TAG, "IOException: " + e.message)
            }
            socket?.let { connected(it) }
            Log.i(TAG, "END mAcceptThread ")
        }

        fun cancel() {
            Log.d(TAG, "Canceling AcceptThread.")
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, " Close of AcceptThread ServerSocket failed. " + e.message)
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE)
                Log.d(TAG, "Setting up Server using: " + MY_UUID_INSECURE)
            } catch (e: IOException) {
                Log.e(TAG, "IOException: " + e.message)
            }
            mmServerSocket = tmp
        }
    }

    private inner class ConnectThread(device: BluetoothDevice?, uuid: UUID?) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        override fun run() {
            var tmp: BluetoothSocket? = null
            Log.i(TAG, "RUN mConnectThread ")
            try {
                Log.d(TAG, "Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE)
                tmp = mmDevice!!.createRfcommSocketToServiceRecord(deviceUUID)
            } catch (e: IOException) {
                Log.e(TAG, "Could not create InsecureRfcommSocket " + e.message)
            }
            mmSocket = tmp
            mBluetoothAdapter.cancelDiscovery()
            try {
                mmSocket!!.connect()
                Log.d(TAG, "ConnectThread connected.")
            } catch (e: IOException) {
                try {
                    mmSocket!!.close()
                    Log.d(TAG, "Closed Socket.")
                } catch (e1: IOException) {
                    Log.e(TAG, " Unable to close connection  " + e1.message)
                }
                Log.d(TAG, "Could not connect to UUID: " + MY_UUID_INSECURE)
            }
            connected(mmSocket)
        }

        fun cancel() {
            try {
                Log.d(TAG, "Closing Client Socket.")
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close of mmSocket in Connectthread failed. " + e.message)
            }
        }

        init {
            Log.d(TAG, "ConnectThread: started.")
            mmDevice = device
            deviceUUID = uuid
        }
    }

    @Synchronized
    fun start() {
        Log.d(TAG, "start")
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread()
            mInsecureAcceptThread!!.start()
        }
    }

    fun startClient(device: BluetoothDevice?, uuid: UUID?) {
        Log.d(TAG, "startClient Started.")
        mConnectThread = ConnectThread(device, uuid)
        mConnectThread!!.start()
    }

    private inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    connectionstatus = true
                    bytes = mmInStream!!.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    Log.d(TAG, "InputStream: $incomingMessage")
                } catch (e: IOException) {
                    connectionstatus = false
                    Log.e(TAG, " Error reading Input Stream. " + e.message)
                    break
                }
            }
        }

        fun write(bytes: ByteArray?) {
            val text = String(bytes!!, Charset.defaultCharset())
            Log.d(TAG, "Writing to outputstream: $text")
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, " Error writing to output stream. " + e.message)
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }

        init {
            Log.d(TAG, "ConnectedThread Start.")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            try {
                tmpIn = mmSocket!!.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    private fun connected(mmSocket: BluetoothSocket?) {
        Log.d(TAG, "connected Starting.")
        mConnectedThread = ConnectedThread(mmSocket)
        mConnectedThread!!.start()
    }

    fun write(out: ByteArray?) {
        var r: ConnectedThread
        Log.d(TAG, " Write Called.")
        mConnectedThread!!.write(out)
    }




}
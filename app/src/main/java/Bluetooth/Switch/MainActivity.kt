package Bluetooth.Switch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    var bt: BluetoothDevice? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    var mBluetoothConnection: BluetoothConnectionService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide();
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

connect.setTextColor(Color.parseColor("#000000"))
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, 1)
        }


        mBluetoothConnection = BluetoothConnectionService()
           connect.text="Connect"
        connect.setBackgroundColor(Color.parseColor("#FF0000"))
        connect.text="DISCONNECTED"
        connect.setOnClickListener {

            try {
                bt = bluetoothAdapter.getRemoteDevice(mac.text.toString())//mac of my device=B8:27:EB:65:E6:5A

                connect.setBackgroundColor(Color.parseColor("#ffff00"))
                connect.text="CONNECTING"
                mBluetoothConnection!!.startClient(bt,uuid)
            }
            catch (e:Exception)
            {
                 Toast.makeText(applicationContext, "No such device found.Cheack MAC ", Toast.LENGTH_LONG).show()



            }




        }
send1.setOnClickListener {

    GlobalScope.launch(Dispatchers.IO) {

        val send="1S"+"0"+s1.text.toString() +","+"0"+t1.text.toString()

        mBluetoothConnection!!.write(send.toByteArray())
    }

}

        send2.setOnClickListener {

            GlobalScope.launch(Dispatchers.IO) {

                val send="3"+"0"+r2.text.toString() +","+"0"+t2.text.toString()+"|"+"0"+s2.text.toString()

                mBluetoothConnection!!.write(send.toByteArray())
            }

        }
       GlobalScope.launch(Dispatchers.IO) {



             while (true) {

           Thread.sleep(3800)

                       if (mBluetoothConnection!!.connectionstatus) {
                           connect.text = "Connected".toUpperCase()
                           connect.setBackgroundColor(Color.parseColor("#2EFF2E"))
                           connect.setTextColor(Color.parseColor("#FF0000"))
                       }
                       else
                       {
                           connect.text = "DisConnected".toUpperCase()
                           connect.setBackgroundColor(Color.parseColor("#FF0000"))
                           connect.setTextColor(Color.parseColor("#2EFF2E"))
                       }






           }

        }






    }





}
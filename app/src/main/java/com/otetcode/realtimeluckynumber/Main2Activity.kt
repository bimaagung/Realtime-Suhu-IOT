package com.otetcode.realtimeluckynumber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main2.*
import java.lang.StringBuilder

class Main2Activity : AppCompatActivity() {

    internal var isDisconnect = false
    lateinit var socket: Socket


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        //Koneksi
        try{
            //memasang url lewat socket io
            socket = IO.socket("http://192.168.43.43:3000/data")
            socket.on(Socket.EVENT_CONNECT){
                runOnUiThread{
                    Toast.makeText(this,"Connected", Toast.LENGTH_SHORT).show() //ketika koneksi berhasil
                }
            }

            socket.connect()

        }catch (e:Exception)
        {
            Toast.makeText(this," "+e.message, Toast.LENGTH_SHORT).show() //ketika koneksi gagal
        }

        //perintah diskoneksi
        tb_koneksi.setOnClickListener {
            socket.emit("connection", "data")

            if(!isDisconnect)
            {

                //koneksi nyambung
                socket.disconnect()
                tb_koneksi.text = "CONNECT"

            }else{

                //koneksi tidak nyambung
                socket.connect()
                tb_koneksi.text ="DISCONNECT"

            }

            //perintah kebalikasi misal konek jadi diskonek dan sebaliknya
            isDisconnect = !isDisconnect
        }

        socket.on("sensor_lampu"){
                args ->
            runOnUiThread{
              tx_hasil.text = args[0].toString()
            }
        }

    }
}

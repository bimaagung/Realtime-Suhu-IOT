package com.otetcode.realtimeluckynumber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.otetcode.realtimeluckynumber.Model.Common
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    internal var isDisconnect=false
    internal var isBet = false
    internal var canPlay = true
    internal var resultNumber = -1

    lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{
            //memasang url lewat socket io
            socket = IO.socket("http://192.168.43.43:3000")
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
        tb_disconnect.setOnClickListener {

            if(!isDisconnect)
            {

                //koneksi nyambung
                socket.disconnect()
                tb_disconnect.text = "CONNECT"

            }else{

                //koneksi tidak nyambung
                socket.connect()
                tb_disconnect.text ="DISCONNECT"

            }

            //perintah kebalikasi misal konek jadi diskonek dan sebaliknya
            isDisconnect = !isDisconnect
        }

        //ketika klik submit
        tb_submit.setOnClickListener{
            try {

                //giliran bermain
                if (canPlay){

                    //memasukkan angka beat
                    if(!isBet){

                        //mengubah sting ke int
                        val bet_money_value = Integer.parseInt(edt_money_value.text.toString())

                        //misal input score melebihi score
                        if (Common.score >= bet_money_value){

                            //memanggil JSONObject
                            val json = JSONObject()

                            //mengirim nilai beat dan uang ke bentuk json
                            json.put("money",bet_money_value)
                            json.put("betValue", Integer.parseInt(edt_bet_value.text.toString()))

                            socket.emit("client_send_money", json)

                            //uang berkurang sesuai diambil
                            Common.score -= bet_money_value
                            tx_score.text = Common.score.toString()

                            isBet = true

                        }else{
                            Toast.makeText(this,"You don't enough money",Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        Toast.makeText(this,"Masukkan angka beat",Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this,"Menunggu giliran bermain",Toast.LENGTH_SHORT).show()
                }

            }catch (e:Exception)
            {
                Toast.makeText(this," "+e.message,Toast.LENGTH_SHORT).show()
            }
        }


        //callback emit restart dari node js
        socket.on("restart"){
            canPlay = true
            //berjalan di ui secara realtime
            runOnUiThread{
                tx_result.visibility = View.GONE
            }
        }

        socket.on("broadcast"){
            args ->
            runOnUiThread{
                //mengambil json count array[0] dan membuat ke string
                tx_count.text = StringBuilder("Timer").append(args[0].toString())
                tx_status.text = ""
                tx_result.text = ""
            }
        }

        socket.on("result"){
            //mengambil result dari json ke int
            args -> resultNumber = Integer.parseInt(args[0].toString())
            runOnUiThread {
                tx_result.visibility = View.VISIBLE
                //mappend menggabungkan nilai
                tx_result.text = StringBuilder("Result: ").append(args[0].toString())
            }
        }

        socket.on("wait_for_restart"){
            args -> canPlay = false
            runOnUiThread{
                tx_status.text = "Please wait for "+args[0]+" second"
                tx_count.text = "Wait...."
                isBet = false
            }
        }

        socket.on("money_send"){
            args -> runOnUiThread{
            tx_money.text = args[0].toString()
         }
        }

        socket.on("reward")
        {
            args ->
            runOnUiThread {
                tx_result.text = "Result: "+resultNumber+" | Congratulations! You win"+args[0]+"USD"
                tx_result.setBackgroundResource(R.drawable.win_text_view)
                Common.score+=Integer.parseInt(args[0].toString())
                tx_score.text = Common.score.toString()
            }
        }

        socket.on("lose"){
            args -> val money = Integer.parseInt(args[0].toString())
            runOnUiThread{
                tx_result.setBackgroundResource(R.drawable.lose_text_view)
                tx_result.text = "Result: $resultNumber | You lose L $money usd"
            }
        }

        socket.on(Socket.EVENT_DISCONNECT){
            runOnUiThread{
                tx_result.text = "DISCONNECT"
                tx_count.text = "DISCONNECT"
                tx_money.text = "DISCONNECT"
            }
        }


    }
}

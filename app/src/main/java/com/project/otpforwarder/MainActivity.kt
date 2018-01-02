package com.project.otpforwarder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.widget.TextView
import android.content.Intent.getIntent
import android.support.v4.app.ActivityCompat
import android.telephony.SmsMessage
import android.util.Log
import android.support.v4.app.NotificationCompat.getExtras
import android.telephony.SmsManager


class MainActivity : AppCompatActivity() {

    val myBroadcastReceiver : MyBroadCastReceiver by lazy {
        println("Broadcast Receiver Created")
        MyBroadCastReceiver()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Otp Forwarder"

        requestSmsPermission()
        number_etv.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim()?.length == 10) {
                    submit_bv.isEnabled = true
                    submit_bv.setBackgroundColor(ContextCompat.getColor(this@MainActivity,  R.color.colorAccent))
                } else {
                    submit_bv.isEnabled = false
                    submit_bv.setBackgroundColor(ContextCompat.getColor(this@MainActivity,  android.R.color.darker_gray))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        submit_bv.setOnClickListener({
            sendOtpToNumberWhenReceived()
        })
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private val MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10

    private fun requestSmsPermission() {

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            Log.i("TAG", "MY_PERMISSIONS_REQUEST_SMS_RECEIVE --> YES");
        }
    }
    private fun sendOtpToNumberWhenReceived() {
        val intentFilter  = IntentFilter()
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        myBroadcastReceiver.recepientNumber = number_etv.text.toString()
        myBroadcastReceiver.serviceProviderName = sprovider_et.text.toString()
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    class MyBroadCastReceiver : BroadcastReceiver() {

        var recepientNumber : String? = null
        var serviceProviderName : String? = null

        override fun onReceive(context: Context?, intent: Intent?) {

            val data = intent?.getExtras()

            val pdus = data?.get("pdus") as Array<Any>

            for (i in pdus.indices) {
                val smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)


                val messageBody = smsMessage.messageBody
                if (messageBody.contains(serviceProviderName as CharSequence, true)) {
                    Log.d("Message: ", "It contains sName")

                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(context?.getString(R.string.india_country_code).plus(recepientNumber), null, messageBody, null, null)
                    Log.d("Message: ", messageBody)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

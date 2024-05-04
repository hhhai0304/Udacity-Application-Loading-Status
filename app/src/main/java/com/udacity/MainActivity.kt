package com.udacity

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID = 0L
    private var fileName = ""

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initNotification()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        binding.mainLayout.apply {
            btnLoading.setOnClickListener {
                val selectedId = rgChoose.checkedRadioButtonId
                if (selectedId == -1) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.empty_select_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    fileName = (findViewById<RadioButton>(selectedId)).text.toString()
                    btnLoading.setState(ButtonState.Loading)
                    download()
                }
            }
        }
    }

    private fun initNotification() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Default", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.denied_notification),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // allow
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.denied_notification),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != null) {
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                if (cursor.moveToFirst()) {
                    cursor.apply {
                        val index = getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (index != -1) {
                            val statusCode = getInt(index)
                            var isSuccess: Boolean? = null
                            if (statusCode == DownloadManager.STATUS_SUCCESSFUL) {
                                isSuccess = true
                            } else if (statusCode == DownloadManager.STATUS_FAILED) {
                                isSuccess = false
                            }
                            isSuccess?.let {
                                binding.mainLayout.btnLoading.setState(ButtonState.Completed)
                                pushNotification(isSuccess)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun download() {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        if (downloadID != 0L) {
            downloadManager.remove(downloadID)
            downloadID = 0L
        }

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadID = downloadManager.enqueue(request)
    }

    private fun pushNotification(isSuccess: Boolean) {
        Toast.makeText(
            this@MainActivity,
            getString(R.string.notification_description),
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.FILE_NAME_KEY, fileName)
        intent.putExtra(DetailActivity.IS_SUCCESS_KEY, isSuccess)

        val pendingIntent =
            PendingIntent.getActivity(
                this, 1, intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val checkStatusAction = NotificationCompat.Action.Builder(
            null,
            getString(R.string.check_status),
            pendingIntent
        ).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setAutoCancel(true)
            .addAction(checkStatusAction)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1, builder.build())
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        const val CHANNEL_ID = "channelId"
    }
}
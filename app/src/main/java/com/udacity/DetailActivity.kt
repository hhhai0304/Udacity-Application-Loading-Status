package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    companion object {
        const val FILE_NAME_KEY = "fileName"
        const val IS_SUCCESS_KEY = "isSuccess"
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val fileName = intent?.extras?.getString(FILE_NAME_KEY, "") ?: ""
        val isSuccess = intent?.extras?.getBoolean(IS_SUCCESS_KEY)

        binding.detailLayout.apply {
            txtFileNameDescription.text = fileName

            txtStatusDescription.apply {
                when (isSuccess) {
                    true -> {
                        text = getString(R.string.success)
                        setTextColor(getColor(R.color.colorPrimaryDark))
                    }

                    false -> {
                        text = getString(R.string.fail)
                        setTextColor(getColor(android.R.color.holo_red_dark))
                    }

                    else -> {
                        text = ""
                        setTextColor(getColor(R.color.colorPrimaryDark))
                    }
                }
            }

            btnOk.setOnClickListener { finish() }
        }
    }
}

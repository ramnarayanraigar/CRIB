package com.ramnarayan.crib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ramnarayan.crib.ui.main.ReadSmsFragment

class ReadSmsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read_sms_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ReadSmsFragment.newInstance())
                .commitNow()
        }
    }
}
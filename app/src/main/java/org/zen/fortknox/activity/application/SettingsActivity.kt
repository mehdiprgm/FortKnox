package org.zen.fortknox.activity.application

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zen.fortknox.R
import org.zen.fortknox.databinding.ActivitySettingsBinding
import org.zen.fortknox.tools.disableScreenPadding

class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        b.btnClose.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.btnClose -> {
                finish()
            }
        }
    }
}
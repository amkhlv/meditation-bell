package com.andreimikhailov.sino

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.andreimikhailov.sino.databinding.ActivityColorSelectorBinding
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.SVBar
import com.larswerkman.holocolorpicker.SaturationBar
import com.larswerkman.holocolorpicker.ValueBar

class ColorSelectorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_selector)
        val picker: ColorPicker = findViewById(R.id.picker)
        val satBar: SaturationBar = findViewById(R.id.saturationbar)
        val valBar: ValueBar = findViewById(R.id.valuebar)
        picker.addSaturationBar(satBar)
        picker.addValueBar(valBar)
        val btn: Button = findViewById(R.id.btn_setcolor)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val extras = getIntent().getExtras();

        extras?.getString("what")?.let {
            val old = prefs.getInt(
                it,
                ContextCompat.getColor(
                    applicationContext,
                    extras?.getInt("colorid") ?: Color.GREEN
                )
            )

            picker.color = old
            val hsv = FloatArray(3)
            Color.RGBToHSV(old.red, old.green, old.blue, hsv)
            println("------ SATURATION=${hsv[1]}")
            println("------ VALUE=${hsv[2]}")
            satBar.setSaturation(hsv[1])
            valBar.setValue(hsv[2])
        }



        btn.setOnClickListener {
            if (extras != null) {
                val c = picker.color
                val w = extras.getString("what")
                println("---picked COLOR $c for $w")
                val editor = prefs.edit()
                editor.putInt(w, picker.color)
                editor.apply()
            }
        }


    }

}
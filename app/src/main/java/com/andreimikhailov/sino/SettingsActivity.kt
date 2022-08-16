package com.andreimikhailov.sino


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.io.File


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //val editor: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val uri = intent?.data
                    context?.resources?.getString(R.string.mp3_filename)?.let {
                        val file = File(this.context?.filesDir, it)
                        if (uri != null) file.outputStream().use {
                            requireContext().contentResolver.openInputStream(uri)?.copyTo(it)
                        }
                    }
                } else {
                    println("--- ERROR         ==============" + result.resultCode)
                }
            }
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val chooser: Preference? = context?.resources?.getString(R.string.mp3)
                ?.let { findPreference(it) }
            chooser?.setOnPreferenceClickListener {
                val fileChooser = Intent(Intent.ACTION_GET_CONTENT)
                fileChooser.addCategory(Intent.CATEGORY_OPENABLE)
                fileChooser.type = "*/*"
                val intent = Intent.createChooser(fileChooser, "Choose an MP3 file")
                startForResult.launch(intent)
                true
            }
            fun setColor(key: Int, colorid: Int) {
                val k = context?.resources?.getString(key)
                val ticking_fg: Preference? = findPreference(k!!)
                ticking_fg?.setOnPreferenceClickListener {
                    val i: Intent =  Intent(context, ColorSelectorActivity::class.java)
                    i.putExtra("what", k!!)
                    i.putExtra("colorid", colorid)
                    startActivity(i)
                    true
                }
            }
            setColor(R.string.mainwin_bg, R.color.mainwin_bg)
            setColor(R.string.mainframe_bg, R.color.mainframe_bg)
            setColor(R.string.ticking_fg, R.color.ticking_fg)
            setColor(R.string.ticking_bg, R.color.ticking_bg)
            setColor(R.string.stopped_fg, R.color.stopped_fg)
            setColor(R.string.stopped_bg, R.color.stopped_bg)
            setColor(R.string.overtime_fg, R.color.overtime_fg)
            setColor(R.string.overtime_bg, R.color.overtime_bg)
            setColor(R.string.buttons_fg, R.color.buttons_fg)
            setColor(R.string.buttons_bg, R.color.buttons_bg)

        }
    }
}

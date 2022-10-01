package com.andreimikhailov.sino

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.io.path.exists

class State(var isTicking: Boolean)

class Update(
    private val mins: EditText,
    private val space: View,
    private val started: LocalDateTime,
    private val howlong: Int,
    private val alreadySounded: Boolean,
    private val context: Context,
    private val state: State,
    private val prefs: SharedPreferences
    ) : Runnable {
    override fun run() {
        if (state.isTicking) {
            val now: LocalDateTime = LocalDateTime.now()
            val passed: Long = started.until(now, java.time.temporal.ChronoUnit.MINUTES)
            mins.setText(String.format("%d", howlong - passed), TextView.BufferType.NORMAL)
            var sounded = alreadySounded
            if (passed >= howlong && !alreadySounded) {
                context?.resources?.getString(R.string.mp3_filename)?.let { mp3_filename ->
                    val mp = MediaPlayer()
                    val file = File(
                        context.filesDir,
                        context.resources.getString(R.string.final_bell_filename)
                    )
                    mp.setDataSource(context, Uri.fromFile(file))
                    mp.prepareAsync()
                    mp.setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.start()
                    }
                    mp.setOnErrorListener { player, w, e ->
                        println("----- MP ERROR ----")
                        println(w.toString())
                        println(e.toString())
                        println("--------")
                        true
                    }
                }
                sounded = true
            }
            if (passed > howlong) {
                mins.setBackgroundColor(
                    prefs.getInt(context.resources.getString(R.string.overtime_bg), ContextCompat.getColor(context, R.color.overtime_bg))
                )
                mins.setTextColor(
                    prefs.getInt(context.resources.getString(R.string.overtime_fg), ContextCompat.getColor(context, R.color.overtime_fg))
                )
                space.foreground = ColorDrawable(prefs.getInt(context.resources.getString(R.string.overtime_bg), ContextCompat.getColor(context, R.color.overtime_bg)))
            }
            mins.postDelayed(
                Update(mins, space, started, howlong, sounded, context, state, prefs),
                5000
            )
        }
    }
}
fun reconfButton(b: Button, m: EditText, s: View, state: State, prefs: SharedPreferences, context: Context) {
    if (state.isTicking) {
        b.text = context.resources.getString(R.string.stop_button_title)
        m.isEnabled = false
        m.setTextColor(prefs.getInt(context.resources.getString(R.string.ticking_fg), ContextCompat.getColor(context, R.color.ticking_fg)))
        m.setBackgroundColor(prefs.getInt(context.resources.getString(R.string.ticking_bg), ContextCompat.getColor(context, R.color.ticking_bg)))
        s.foreground = ColorDrawable(prefs.getInt(context.resources.getString(R.string.ticking_bg), ContextCompat.getColor(context, R.color.ticking_bg)))
    } else {
        b.text = context.resources.getString(R.string.start_button_title)
        m.isEnabled = true
        m.setText(
            prefs.getString(
                context.resources.getString(R.string.interval),
                context.resources.getString(R.string.default_interval)
            ),
            TextView.BufferType.EDITABLE
        )
        m.setTextColor(prefs.getInt(context.resources.getString(R.string.stopped_fg), ContextCompat.getColor(context, R.color.stopped_fg)))
        m.setBackgroundColor(prefs.getInt(context.resources.getString(R.string.stopped_bg), ContextCompat.getColor(context, R.color.stopped_bg)))
        s.foreground = ColorDrawable(prefs.getInt(context.resources.getString(R.string.stopped_bg), ContextCompat.getColor(context, R.color.stopped_bg)))
    }
}

class MainActivity : AppCompatActivity() {

    private val state = State(isTicking =  false)

    private fun setup(context: Context) {

        val toPath: Path = Paths.get(context.filesDir.toString(), context.resources.getString(R.string.mp3_filename))
        if (!toPath.exists()) {
            val inStream = resources.openRawResource(R.raw.sino)
            inStream.copyTo(FileOutputStream(toPath.toFile()))
        }
    }
    override fun onResume(){
        super.onResume()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        window.setBackgroundDrawable(
            ColorDrawable(prefs.getInt(applicationContext.getString(R.string.mainwin_bg), ContextCompat.getColor(applicationContext, R.color.mainwin_bg)))
        )
        val mainframe: LinearLayout = findViewById(R.id.mainframe)
        mainframe.setBackgroundColor(prefs.getInt(applicationContext.getString(R.string.mainframe_bg), ContextCompat.getColor(applicationContext, R.color.mainframe_bg)))
        val btn: Button = findViewById(R.id.button)
        btn.setBackgroundColor(prefs.getInt(applicationContext.getString(R.string.buttons_bg), ContextCompat.getColor(applicationContext, R.color.buttons_bg)))
        btn.setTextColor(prefs.getInt(applicationContext.getString(R.string.buttons_fg), ContextCompat.getColor(applicationContext, R.color.buttons_fg)))
        val btnSettings: Button  = findViewById(R.id.btnSettings)
        btnSettings.setBackgroundColor(prefs.getInt(applicationContext.getString(R.string.buttons_bg), ContextCompat.getColor(applicationContext, R.color.buttons_bg)))
        btnSettings.setTextColor(prefs.getInt(applicationContext.getString(R.string.buttons_fg), ContextCompat.getColor(applicationContext, R.color.buttons_fg)))
        val editMins: EditText = findViewById(R.id.editMins)
        val space: View = findViewById(R.id.space)
        editMins.setTextColor(
            prefs.getInt(
                applicationContext.resources.getString(if (state.isTicking) { R.string.ticking_fg } else { R.string.stopped_fg }),
                ContextCompat.getColor(applicationContext, if (state.isTicking) { R.color.ticking_fg } else { R.color.stopped_fg })
            )
        )
        editMins.setBackgroundColor(
            prefs.getInt(
                applicationContext.resources.getString(if (state.isTicking) { R.string.ticking_bg } else { R.string.stopped_bg }),
                ContextCompat.getColor(applicationContext, if (state.isTicking) { R.color.ticking_bg } else { R.color.stopped_bg })
            )
        )
        space.foreground = ColorDrawable(
            prefs.getInt(
                applicationContext.resources.getString(if (state.isTicking) { R.string.ticking_bg } else { R.string.stopped_bg }),
                ContextCompat.getColor(applicationContext, if (state.isTicking) { R.color.ticking_bg } else { R.color.stopped_bg })
            )
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setup(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setBackgroundDrawable(
            ColorDrawable(prefs.getInt(applicationContext.getString(R.string.mainwin_bg), ContextCompat.getColor(applicationContext, R.color.mainwin_bg)))
        )
        val btn: Button = findViewById(R.id.button)
        btn.setBackgroundColor(prefs.getInt(applicationContext.getString(R.string.buttons_bg), ContextCompat.getColor(applicationContext, R.color.buttons_bg)))
        btn.setTextColor(prefs.getInt(applicationContext.getString(R.string.buttons_fg), ContextCompat.getColor(applicationContext, R.color.buttons_fg)))
        val btnSettings: Button  = findViewById(R.id.btnSettings)
        btnSettings.setBackgroundColor(prefs.getInt(applicationContext.getString(R.string.buttons_bg), ContextCompat.getColor(applicationContext, R.color.buttons_bg)))
        btnSettings.setTextColor(prefs.getInt(applicationContext.getString(R.string.buttons_fg), ContextCompat.getColor(applicationContext, R.color.buttons_fg)))
        val editMins: EditText = findViewById(R.id.editMins)
        val space: View = findViewById(R.id.space)
        val interval = prefs.getString(
            applicationContext.resources.getString(R.string.interval),
            applicationContext.resources.getString(R.string.default_interval)
        )
        println("------ INTERVAL=$interval")
        if (!state.isTicking) {
            editMins.setText(interval,TextView.BufferType.EDITABLE)
        }
        reconfButton(btn, editMins, space, state, prefs, applicationContext)
        btn.setOnClickListener {
            state.isTicking = ! state.isTicking
            reconfButton(btn, editMins, space, state, prefs, applicationContext)
            if (state.isTicking) {
                val hl: Int? = editMins.text.toString().toIntOrNull()
                if (hl != null) {
                    editMins.postDelayed(
                        Update(editMins, space, LocalDateTime.now(), hl, false, applicationContext, state, prefs),
                        500
                    )
                }
            }
        }
        btnSettings.setOnClickListener {
            val i: Intent =  Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }
    }
}
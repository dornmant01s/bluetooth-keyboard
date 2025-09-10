
package com.example.channelremote

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private val adapter = ChannelListAdapter()

    private var pickingChannel: Int? = null

    private val appPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val pkg = result.data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE) ?: return@registerForActivityResult
                val ch = pickingChannel ?: return@registerForActivityResult
                ChannelPrefs(this).setPackageForChannel(ch, pkg)
                pickingChannel = null
                reload()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById(R.id.rvChannels)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<View>(R.id.btnOpenA11y).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        findViewById<View>(R.id.btnAdd).setOnClickListener {
            showAddChannelDialog()
        }

        reload()
    }

    private fun reload() {
        val prefs = ChannelPrefs(this)
        val list = prefs.allAssignedChannels().sorted()
        val pm = packageManager
        val items = list.map { ch ->
            val pkg = prefs.getPackageForChannel(ch)
            val (label, icon) = try {
                if (pkg != null) {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    Pair(pm.getApplicationLabel(ai).toString(), pm.getApplicationIcon(pkg))
                } else Pair("(미지정)", null)
            } catch (e: Exception) {
                Pair("$pkg (제거됨?)", null)
            }
            ChannelItem(ch, pkg, label, icon)
        }
        adapter.submit(items)
    }

    private fun showAddChannelDialog() {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(9))
            hint = getString(R.string.input_channel_number)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_channel))
            .setView(et)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val txt = et.text.toString().trim()
                if (txt.isNotEmpty() && txt.all { it.isDigit() }) {
                    val ch = txt.toInt()
                    pickingChannel = ch
                    val i = Intent(this, AppPickerActivity::class.java)
                    appPicker.launch(i)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    inner class ChannelListAdapter : RecyclerView.Adapter<ChannelVH>() {
        private val data = mutableListOf<ChannelItem>()
        fun submit(items: List<ChannelItem>) {
            data.clear()
            data.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelVH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
            return ChannelVH(v)
        }
        override fun getItemCount(): Int = data.size
        override fun onBindViewHolder(holder: ChannelVH, position: Int) {
            holder.bind(data[position])
        }
    }

    inner class ChannelVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvChannel = v.findViewById<TextView>(R.id.tvChannel)
        private val tvApp = v.findViewById<TextView>(R.id.tvApp)
        private val img = v.findViewById<ImageView>(R.id.imgIcon)
        private val btnAssign = v.findViewById<Button>(R.id.btnAssign)
        private val btnClear = v.findViewById<Button>(R.id.btnClear)

        fun bind(item: ChannelItem) {
            tvChannel.text = item.channel.toString()
            tvApp.text = "${item.label}\n${item.pkg ?: ""}"
            img.setImageDrawable(item.icon)

            btnAssign.setOnClickListener {
                pickingChannel = item.channel
                val i = Intent(this@MainActivity, AppPickerActivity::class.java)
                appPicker.launch(i)
            }
            btnClear.setOnClickListener {
                ChannelPrefs(this@MainActivity).clearChannel(item.channel)
                reload()
            }
        }
    }

    data class ChannelItem(
        val channel: Int,
        val pkg: String?,
        val label: String,
        val icon: android.graphics.drawable.Drawable?
    )
}

package com.example.channelkeylauncher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var btnAdd: MaterialButton
    private lateinit var btnSetInc: MaterialButton
        private lateinit var btnRandom: MaterialButton
        private lateinit var btnClear: MaterialButton
    private lateinit var btnSetDec: MaterialButton
    private lateinit var txtKeys: TextView

    private var map = mutableMapOf<Int, String>()

    private val pickApp = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            val channel = res.data?.getIntExtra("channel", -1) ?: -1
            val pkg = res.data?.getStringExtra("package") ?: return@registerForActivityResult
            if (channel >= 0) {
                map[channel] = pkg
                Prefs.saveMap(this, map)
                refresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list = findViewById(R.id.list)
        btnAdd = findViewById(R.id.btnAdd)
        btnSetInc = findViewById(R.id.btnSetInc)
            btnRandom = findViewById(R.id.btnRandom)
            btnClear = findViewById(R.id.btnClear)
        btnSetDec = findViewById(R.id.btnSetDec)
        txtKeys = findViewById(R.id.txtKeys)

        map = Prefs.loadMap(this)

        val adapter = ChannelAdapter { ch ->
            map.remove(ch)
            Prefs.saveMap(this, map)
            refresh()
        }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        refresh()

        btnAdd.setOnClickListener {
            val input = EditText(this).apply { hint = "채널 번호(정수)" }
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("채널 추가")
                .setView(input)
                .setPositiveButton("앱 선택") { _, _ ->
                    val ch = input.text.toString().toIntOrNull()
                    if (ch == null) { Toast.makeText(this, "숫자 입력", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                    val i = Intent(this, AppPickerActivity::class.java)
                    i.putExtra("channel", ch)
                    pickApp.launch(i)
                }.setNegativeButton("취소", null).show()
        }

        btnSetInc.setOnClickListener { captureKey { code ->
            Prefs.setIncKey(this, code); showKeys()

            if (map.isEmpty()) { randomizeMapping() }

btnRandom.setOnClickListener {
    randomizeMapping()
    Toast.makeText(this, getString(R.string.toast_random_done), Toast.LENGTH_SHORT).show()
}
btnClear.setOnClickListener {
    map.clear()
    Prefs.saveMap(this, map)
    refresh()
    Toast.makeText(this, "채널 매핑 초기화", Toast.LENGTH_SHORT).show()
}

            Toast.makeText(this, "+1 키 설정 완료", Toast.LENGTH_SHORT).show()
        } }

        btnSetDec.setOnClickListener { captureKey { code ->
            Prefs.setDecKey(this, code); showKeys()

            if (map.isEmpty()) { randomizeMapping() }

btnRandom.setOnClickListener {
    randomizeMapping()
    Toast.makeText(this, getString(R.string.toast_random_done), Toast.LENGTH_SHORT).show()
}
btnClear.setOnClickListener {
    map.clear()
    Prefs.saveMap(this, map)
    refresh()
    Toast.makeText(this, "채널 매핑 초기화", Toast.LENGTH_SHORT).show()
}

            Toast.makeText(this, "−1 키 설정 완료", Toast.LENGTH_SHORT).show()
        } }

        showKeys()

            if (map.isEmpty()) { randomizeMapping() }

btnRandom.setOnClickListener {
    randomizeMapping()
    Toast.makeText(this, getString(R.string.toast_random_done), Toast.LENGTH_SHORT).show()
}
btnClear.setOnClickListener {
    map.clear()
    Prefs.saveMap(this, map)
    refresh()
    Toast.makeText(this, "채널 매핑 초기화", Toast.LENGTH_SHORT).show()
}

    }

    private fun showKeys()

            if (map.isEmpty()) { randomizeMapping() }

btnRandom.setOnClickListener {
    randomizeMapping()
    Toast.makeText(this, getString(R.string.toast_random_done), Toast.LENGTH_SHORT).show()
}
btnClear.setOnClickListener {
    map.clear()
    Prefs.saveMap(this, map)
    refresh()
    Toast.makeText(this, "채널 매핑 초기화", Toast.LENGTH_SHORT).show()
}
 {
        txtKeys.text = "현재: +1=" + Prefs.getIncKey(this) + " / −1=" + Prefs.getDecKey(this)
    }

    private fun refresh() {
        (list.adapter as ChannelAdapter).submitList(map.keys.sorted())
    }

    

private fun randomizeMapping() {
    val pm = packageManager
    val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val apps = pm.queryIntentActivities(main, 0).toMutableList()
    apps.shuffle()
    map.clear()
    var ch = 1
    for (ri in apps) {
        map[ch++] = ri.activityInfo.packageName
    }
    Prefs.saveMap(this, map)
    refresh()
}
private fun captureKey(onPick: (Int) -> Unit) {
        Toast.makeText(this, "키보드에서 원하는 키를 누르세요", Toast.LENGTH_SHORT).show()
        val originalCallback = window.callback
        window.callback = object : android.view.Window.Callback by originalCallback {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    onPick(event.keyCode)
                    // 복구
                    window.callback = originalCallback
                    return true
                }
                return originalCallback.dispatchKeyEvent(event)
            }
        }
    }

    private class ChannelAdapter(val onDelete: (Int) -> Unit) :
        ListAdapter<Int, VH>(object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(v as ViewGroup, onDelete)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class VH(val root: ViewGroup, val onDelete: (Int)->Unit) : RecyclerView.ViewHolder(root) {
        fun bind(ch: Int) {
            root.findViewById<TextView>(android.R.id.text1).text = "채널 $ch"
            root.findViewById<TextView>(android.R.id.text2).text = "길게 눌러 삭제"
            root.setOnLongClickListener { onDelete(ch); true }
        }
    }
}

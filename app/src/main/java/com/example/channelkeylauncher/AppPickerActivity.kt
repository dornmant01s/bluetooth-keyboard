package com.example.channelkeylauncher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val ch = intent.getIntExtra("channel", -1)
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(main, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val list = findViewById<RecyclerView>(R.id.appList)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
                return VH(v as ViewGroup)
            }
            override fun getItemCount() = apps.size
            override fun onBindViewHolder(holder: VH, position: Int) {
                val ri = apps[position]
                holder.t1.text = ri.loadLabel(pm)
                holder.t2.text = ri.activityInfo.packageName
                holder.root.setOnClickListener {
                    setResult(RESULT_OK, Intent().putExtra("channel", ch).putExtra("package", ri.activityInfo.packageName))
                    finish()
                }
            }
        }
    }

    class VH(val root: ViewGroup): RecyclerView.ViewHolder(root) {
        val t1: TextView = root.findViewById(android.R.id.text1)
        val t2: TextView = root.findViewById(android.R.id.text2)
    }
}

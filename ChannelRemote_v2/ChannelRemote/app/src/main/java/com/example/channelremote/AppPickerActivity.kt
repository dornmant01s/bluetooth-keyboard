
package com.example.channelremote

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val rv = findViewById<RecyclerView>(R.id.rvApps)
        rv.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            .sortedBy { it.loadLabel(pm).toString() }

        rv.adapter = object : RecyclerView.Adapter<AppVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppVH {
                val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                return AppVH(v)
            }

            override fun getItemCount(): Int = apps.size

            override fun onBindViewHolder(holder: AppVH, position: Int) {
                val ri = apps[position]
                val label = ri.loadLabel(pm).toString()
                val pkg = ri.activityInfo.packageName
                (holder.itemView as TextView).text = "$label\n$pkg"
                holder.itemView.setOnClickListener {
                    val data = Intent()
                    data.putExtra(EXTRA_PACKAGE, pkg)
                    setResult(RESULT_OK, data)
                    finish()
                }
            }
        }
    }

    class AppVH(v: View) : RecyclerView.ViewHolder(v)
}

package com.regain.focusshield

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.Activity

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    var checked: Boolean
)

class AppPickerActivity : Activity() {
    private lateinit var items: MutableList<AppItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val pm = packageManager
        val current = Prefs.allowed(this).toMutableSet()

        items = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName }
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                AppItem(
                    pm.getApplicationLabel(it).toString(),
                    it.packageName,
                    pm.getApplicationIcon(it),
                    current.contains(it.packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toMutableList()

        val list = findViewById<ListView>(R.id.appList)
        list.adapter = AppAdapter()
        list.setOnItemClickListener { _, _, position, _ ->
            items[position].checked = !items[position].checked
            list.adapter = AppAdapter()
        }

        findViewById<Button>(R.id.saveApps).setOnClickListener {
            Prefs.setAllowed(this, items.filter { it.checked }.map { it.packageName }.toSet())
            finish()
        }
    }

    private inner class AppAdapter : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: LayoutInflater.from(this@AppPickerActivity)
                .inflate(R.layout.row_app, parent, false)
            val item = items[position]
            v.findViewById<ImageView>(R.id.icon).setImageDrawable(item.icon)
            v.findViewById<TextView>(R.id.name).text = item.label
            v.findViewById<CheckBox>(R.id.check).isChecked = item.checked
            v.findViewById<CheckBox>(R.id.check).setOnClickListener {
                item.checked = (it as CheckBox).isChecked
            }
            return v
        }
    }
}
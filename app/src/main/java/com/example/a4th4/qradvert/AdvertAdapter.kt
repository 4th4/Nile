package com.example.a4th4.qradvert

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.ArrayList

class AdvertAdapter(private val context: Context, private val list: ArrayList<Advert>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var advert: Advert = getItem(position) as Advert
        var inflateView: View

        if (convertView == null) {
            inflateView = LayoutInflater.from(context).
                    inflate(R.layout.list_item, parent, false);

            val titleView: TextView = inflateView.findViewById(R.id.title)
            titleView.text = advert.title
            val desView: TextView = inflateView.findViewById(R.id.description)
            desView.text = advert.description

        }else{
            inflateView = convertView
        }
        return inflateView
    }

    override fun getItem(position: Int): Any {
       return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
       return list.size
    }

}
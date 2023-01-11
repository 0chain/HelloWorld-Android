package org.zus.bolt.helloworld.ui.selectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.zus.bolt.helloworld.databinding.RowDetailsBinding

class DetailsListAdapter(
    private val detailsList: List<Pair<String, String>>
) : BaseAdapter() {
    override fun getCount(): Int = detailsList.size

    override fun getItem(position: Int): Any {
        return detailsList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowDetailsBinding = RowDetailsBinding.inflate(LayoutInflater.from(parent?.context))
        rowDetailsBinding.tvDetailTitle.text = detailsList[position].first
        rowDetailsBinding.tvDetailValue.text = detailsList[position].second
        return rowDetailsBinding.root
    }
}

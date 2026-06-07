package com.industrial.barcodescanner.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.industrial.barcodescanner.R
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity

class HistoryAdapter(
    private val onPriceRowInteraction: (BarcodeEntity) -> Unit,
    private val onDeleteInteraction: (BarcodeEntity) -> Unit
) : ListAdapter<BarcodeEntity, HistoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtBarcode: TextView = view.findViewById(R.id.txtBarcodeValue)
        val txtPrice: TextView = view.findViewById(R.id.txtPriceValue)
        val txtTag: TextView = view.findViewById(R.id.txtTagValue)
        val txtUnit: TextView = view.findViewById(R.id.txtUnitValue)
        val txtCopies: TextView = view.findViewById(R.id.txtCopiesValue)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.txtBarcode.text = item.barcode
        holder.txtPrice.text = "Price: ${item.price ?: "N/A"}"
        holder.txtTag.text = item.tagType
        holder.txtUnit.text = item.unitType
        holder.txtCopies.text = "Copies: ${item.copies}"

        holder.itemView.setOnClickListener { onPriceRowInteraction(item) }
        holder.btnDelete.setOnClickListener { onDeleteInteraction(item) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BarcodeEntity>() {
        override fun areItemsTheSame(oldItem: BarcodeEntity, newItem: BarcodeEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BarcodeEntity, newItem: BarcodeEntity): Boolean =
            oldItem == newItem
    }
}

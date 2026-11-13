package com.example.contacts2

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts2.databinding.ItemContactBinding

class ContactAdapter(private val onClick: (Contact) -> Unit) :
    RecyclerView.Adapter<ContactAdapter.VH>() {

    private var items: List<Contact> = emptyList()

    fun submit(list: List<Contact>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemContactBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.b.tvNomPrenom.text = "${c.nom} ${c.prenom}"
        holder.b.tvTel.text = c.telephone
        if (c.photoUri != null) holder.b.ivPhoto.setImageURI(Uri.parse(c.photoUri))
        else holder.b.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
        holder.b.root.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size
}
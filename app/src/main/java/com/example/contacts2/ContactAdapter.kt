package com.example.contacts2

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ContactAdapter(
    private val context: Context,
    private val contacts: MutableList<Contact>
) : BaseAdapter() {

    override fun getCount(): Int {
        return contacts.size
    }

    override fun getItem(position: Int): Contact {
        return contacts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_contact, parent, false)

        val image = view.findViewById<ImageView>(R.id.imgContact)
        val nomPrenom = view.findViewById<TextView>(R.id.txtNomPrenom)
        val telephone = view.findViewById<TextView>(R.id.txtTelephone)

        val contact = contacts[position]

        nomPrenom.text = "${contact.nom} ${contact.prenom}"
        telephone.text = contact.telephone

        if (contact.photo.isNotEmpty()) {
            try {
                image.setImageURI(Uri.parse(contact.photo))
            } catch (e: Exception) {
                image.setImageResource(R.drawable.ic_contact)
            }
        } else {
            image.setImageResource(R.drawable.ic_contact)
        }

        return view
    }
}
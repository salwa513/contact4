package com.example.contacts2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts2.databinding.ItemContactBinding
import java.io.File

class ContactAdapter(
    private val onClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.VH>() {

    private var items: List<Contact> = emptyList()
    var taillePoliceSp: Float = 16f
    var couleurTexte: Int = 0xFF1C1B1F.toInt()
    var afficherBoutonAppel: Boolean = false

    // Palette d'avatars cyclique
    private val avatarColors = listOf(
        R.color.avatar_1, R.color.avatar_2, R.color.avatar_3,
        R.color.avatar_4, R.color.avatar_5, R.color.avatar_6
    )

    fun submit(list: List<Contact>) {
        items = list
        notifyDataSetChanged()
    }
//save vues//
    inner class VH(val b: ItemContactBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        val context = holder.itemView.context

        holder.b.tvNomPrenom.text = "${c.nom} ${c.prenom}"
        holder.b.tvTel.text = c.telephone

        holder.b.tvNomPrenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, taillePoliceSp)
        holder.b.tvTel.setTextSize(TypedValue.COMPLEX_UNIT_SP, (taillePoliceSp - 2).coerceAtLeast(12f))

        // Photo ou avatar avec initiales
        val p = c.photoUri
        if (p != null && File(p).exists()) {
            holder.b.ivPhoto.visibility = View.VISIBLE
            holder.b.tvInitiales.visibility = View.GONE
            holder.b.ivPhoto.setImageURI(Uri.fromFile(File(p)))

            // Avatar circulaire pour la photo
            val photoDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
            }
            holder.b.ivPhoto.background = photoDrawable
            holder.b.ivPhoto.clipToOutline = true
        } else {
            holder.b.ivPhoto.visibility = View.GONE
            holder.b.tvInitiales.visibility = View.VISIBLE

            // Initiales
            val initiales = buildString {
                if (c.nom.isNotEmpty()) append(c.nom.first().uppercaseChar())
                if (c.prenom.isNotEmpty()) append(c.prenom.first().uppercaseChar())
                if (isEmpty()) append("?")
            }
            holder.b.tvInitiales.text = initiales

            // Couleur cyclique + fond circulaire
            val colorRes = avatarColors[position % avatarColors.size]
            val color = ContextCompat.getColor(context, colorRes)
            val circle = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            holder.b.tvInitiales.background = circle
        }

        holder.b.root.setOnClickListener { onClick(c) }

        if (afficherBoutonAppel) {
            holder.b.btnAppelerItem.visibility = View.VISIBLE
            holder.b.btnAppelerItem.setOnClickListener {
                val tel = c.telephone.trim()
                if (tel.isEmpty()) {
                    Toast.makeText(context, "Numéro vide", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(tel)}"))
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Aucune application téléphone", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            holder.b.btnAppelerItem.visibility = View.GONE
            holder.b.btnAppelerItem.setOnClickListener(null)
        }
    }

    override fun getItemCount() = items.size
}

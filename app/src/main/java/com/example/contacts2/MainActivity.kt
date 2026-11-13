package com.example.contacts2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ContactAdapter

    private val contacts = mutableListOf(
        Contact("Ben Ali", "Ahmed", "22111222", "ahmed@gmail.com"),
        Contact("Trabelsi", "Sarra", "22333444", "sarra@gmail.com"),
        Contact("Mansouri", "Mohamed", "55444555", "mohamed@gmail.com"),
        Contact("Hamdi", "Amira", "99111222", "amira@gmail.com")
    )

    private val detailLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data

                if (data != null) {

                    val position = data.getIntExtra("position", -1)

                    val contact = Contact(
                        data.getStringExtra("nom") ?: "",
                        data.getStringExtra("prenom") ?: "",
                        data.getStringExtra("telephone") ?: "",
                        data.getStringExtra("email") ?: "",
                        data.getStringExtra("photo") ?: ""
                    )

                    if (position == -1) {
                        // Nouveau contact
                        contacts.add(contact)
                    } else if (position >= 0 && position < contacts.size) {
                        // Modification d'un contact existant
                        contacts[position] = contact
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listViewContacts)
        adapter = ContactAdapter(this, contacts)
        listView.adapter = adapter

        // Clic sur un contact : ouvrir DetailActivity
        listView.setOnItemClickListener { _, _, position, _ ->

            val contact = contacts[position]

            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("nom", contact.nom)
            intent.putExtra("prenom", contact.prenom)
            intent.putExtra("telephone", contact.telephone)
            intent.putExtra("email", contact.email)
            intent.putExtra("photo", contact.photo)

            detailLauncher.launch(intent)
        }

        // Bouton Ajouter : DetailActivity vide (position = -1)
        findViewById<Button>(R.id.btnAjouter).setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            detailLauncher.launch(intent)
        }
    }
}
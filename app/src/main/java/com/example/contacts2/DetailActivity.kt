package com.example.contacts2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var imgDetail: ImageView
    private lateinit var edtNom: EditText
    private lateinit var edtPrenom: EditText
    private lateinit var edtTelephone: EditText
    private lateinit var edtEmail: EditText

    private var position: Int = -1
    private var photoUri: Uri? = null
    private var cameraUri: Uri? = null

    // Galerie (OpenDocument : permission persistante)
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                photoUri = uri
                imgDetail.setImageURI(uri)
            }
            // uri == null : l'utilisateur a annulé, on ne fait rien
        }

    // Caméra
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraUri != null) {
                photoUri = cameraUri
                imgDetail.setImageURI(cameraUri)
            } else {
                cameraUri?.let {
                    try {
                        contentResolver.delete(it, null, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                cameraUri = null
                Toast.makeText(this, "Photo non prise", Toast.LENGTH_SHORT).show()
            }
        }

    // Permission caméra
    private val permissionCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordee ->
            if (accordee) {
                ouvrirCamera()
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imgDetail = findViewById(R.id.imgDetail)
        edtNom = findViewById(R.id.edtNom)
        edtPrenom = findViewById(R.id.edtPrenom)
        edtTelephone = findViewById(R.id.edtTelephone)
        edtEmail = findViewById(R.id.edtEmail)

        val btnGalerie = findViewById<Button>(R.id.btnGalerie)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnAppeler = findViewById<Button>(R.id.btnAppeler)
        val btnEmail = findViewById<Button>(R.id.btnEmail)
        val btnEnregistrer = findViewById<Button>(R.id.btnEnregistrer)
        val btnAnnuler = findViewById<Button>(R.id.btnAnnuler)

        // Récupération des données
        position = intent.getIntExtra("position", -1)
        edtNom.setText(intent.getStringExtra("nom"))
        edtPrenom.setText(intent.getStringExtra("prenom"))
        edtTelephone.setText(intent.getStringExtra("telephone"))
        edtEmail.setText(intent.getStringExtra("email"))

        val anciennePhoto = intent.getStringExtra("photo")
        if (!anciennePhoto.isNullOrEmpty()) {
            photoUri = Uri.parse(anciennePhoto)
            try {
                imgDetail.setImageURI(photoUri)
            } catch (e: Exception) {
                imgDetail.setImageResource(R.drawable.ic_contact)
            }
        }

        btnGalerie.setOnClickListener {
            galleryLauncher.launch(arrayOf("image/*"))
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                ouvrirCamera()
            } else {
                permissionCamera.launch(Manifest.permission.CAMERA)
            }
        }

        btnAppeler.setOnClickListener {
            val telephone = edtTelephone.text.toString().trim()
            if (telephone.isEmpty()) {
                edtTelephone.error = "Numéro obligatoire"
            } else {
                val intentAppel = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telephone"))
                try {
                    startActivity(intentAppel)
                } catch (e: Exception) {
                    Toast.makeText(
                        this, "Aucune application téléphonique disponible", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnEmail.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            if (email.isEmpty()) {
                edtEmail.error = "E-mail obligatoire"
            } else {
                val intentEmail = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                }
                try {
                    startActivity(intentEmail)
                } catch (e: Exception) {
                    Toast.makeText(
                        this, "Aucune application de messagerie disponible", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnEnregistrer.setOnClickListener { enregistrerContact() }

        btnAnnuler.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun ouvrirCamera() {
        try {
            val fichier = File.createTempFile("contact_photo_", ".jpg", cacheDir)
            cameraUri = FileProvider.getUriForFile(
                this, "$packageName.fileprovider", fichier
            )
            cameraLauncher.launch(cameraUri!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Caméra indisponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enregistrerContact() {
        val nom = edtNom.text.toString().trim()
        val prenom = edtPrenom.text.toString().trim()
        val telephone = edtTelephone.text.toString().trim()
        val email = edtEmail.text.toString().trim()

        if (nom.isEmpty()) {
            edtNom.error = "Nom obligatoire"
            edtNom.requestFocus()
            return
        }
        if (prenom.isEmpty()) {
            edtPrenom.error = "Prénom obligatoire"
            edtPrenom.requestFocus()
            return
        }
        if (telephone.isEmpty()) {
            edtTelephone.error = "Téléphone obligatoire"
            edtTelephone.requestFocus()
            return
        }
        if (email.isEmpty()) {
            edtEmail.error = "E-mail obligatoire"
            edtEmail.requestFocus()
            return
        }

        val resultat = Intent().apply {
            putExtra("position", position)
            putExtra("nom", nom)
            putExtra("prenom", prenom)
            putExtra("telephone", telephone)
            putExtra("email", email)
            putExtra("photo", photoUri?.toString() ?: "")
        }

        setResult(Activity.RESULT_OK, resultat)
        Toast.makeText(this, "Contact enregistré", Toast.LENGTH_SHORT).show()
        finish()
    }
}
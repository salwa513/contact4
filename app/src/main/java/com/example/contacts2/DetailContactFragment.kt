package com.example.contacts2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.contacts2.databinding.FragmentDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class DetailContactFragment : Fragment(R.layout.fragment_detail) {

    private val vm: ContactsViewModel by activityViewModels()
    private var _b: FragmentDetailBinding? = null
    private val b get() = _b!!

    private var contactId = 0L
    private var photoPath: String? = null
    private var tempCameraUri: Uri? = null
    private var tempCameraPath: String? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val path = withContext(Dispatchers.IO) { copierPhoto(it) }
                if (path != null) {
                    photoPath = path
                    afficherPhoto()
                } else {
                    toast("Impossible de lire l'image")
                }
            }
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            photoPath = tempCameraPath
            afficherPhoto()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentDetailBinding.bind(view)

        contactId = arguments?.getLong(ARG_CONTACT_ID, 0L) ?: 0L
        photoPath = savedInstanceState?.getString("photo")
        b.btnSupprimer.visibility = if (contactId == 0L) View.GONE else View.VISIBLE

        appliquerPreferences()

        if (savedInstanceState == null && contactId != 0L) {
            charger()
        } else {
            afficherPhoto()
        }

        b.btnGalerie.setOnClickListener { pickImage.launch("image/*") }
        b.btnCamera.setOnClickListener { capturerPhoto() }
        b.btnCarte.setOnClickListener { ouvrirCarte() }
        b.btnAppeler.setOnClickListener { appeler() }
        b.btnEmail.setOnClickListener { envoyerMail() }
        b.btnEnregistrer.setOnClickListener { enregistrer() }
        b.btnAnnuler.setOnClickListener { parentFragmentManager.popBackStack() }
        b.btnSupprimer.setOnClickListener { confirmerSuppression() }
    }

    private fun appliquerPreferences() {
        val context = context ?: return
        val taille = PreferencesManager.getTaillePolice(context)
        val couleur = PreferencesManager.getCouleurPreferee(context)

        b.etNom.setTextSize(TypedValue.COMPLEX_UNIT_SP, taille)
        b.etPrenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, taille)
        b.etTel.setTextSize(TypedValue.COMPLEX_UNIT_SP, (taille - 2).coerceAtLeast(12f))

        b.etNom.setTextColor(couleur)
        b.etPrenom.setTextColor(couleur)
    }

    private fun charger() {
        viewLifecycleOwner.lifecycleScope.launch {
            val c = vm.getById(contactId)
            if (c == null) {
                toast("Contact introuvable")
                parentFragmentManager.popBackStack()
                return@launch
            }
            b.etNom.setText(c.nom)
            b.etPrenom.setText(c.prenom)
            b.etTel.setText(c.telephone)
            b.etEmail.setText(c.email ?: "")
            b.etLat.setText(c.latitude?.toString() ?: "")
            b.etLng.setText(c.longitude?.toString() ?: "")
            photoPath = c.photoUri
            afficherPhoto()
        }
    }

    private val avatarColors = listOf(
        R.color.avatar_1, R.color.avatar_2, R.color.avatar_3,
        R.color.avatar_4, R.color.avatar_5, R.color.avatar_6
    )

    private fun afficherPhoto() {
        val p = photoPath
        if (p != null && File(p).exists()) {
            b.ivPhoto.visibility = View.VISIBLE
            b.tvInitialesDetail.visibility = View.GONE
            b.ivPhoto.setImageURI(Uri.fromFile(File(p)))
            b.ivPhoto.clipToOutline = true
            val circle = GradientDrawable().apply { shape = GradientDrawable.OVAL }
            b.ivPhoto.background = circle
        } else {
            b.ivPhoto.visibility = View.GONE
            b.tvInitialesDetail.visibility = View.VISIBLE
            val nom = b.etNom.text.toString()
            val prenom = b.etPrenom.text.toString()
            val initiales = buildString {
                if (nom.isNotEmpty()) append(nom.first().uppercaseChar())
                if (prenom.isNotEmpty()) append(prenom.first().uppercaseChar())
                if (isEmpty()) append("?")
            }
            b.tvInitialesDetail.text = initiales
            val colorIndex = (nom + prenom).hashCode().and(0x7FFFFFFF) % avatarColors.size
            val color = ContextCompat.getColor(requireContext(), avatarColors[colorIndex])
            val circle = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            b.tvInitialesDetail.background = circle
        }
    }

    private fun copierPhoto(uri: Uri): String? = try {
        val f = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            f.outputStream().use { output -> input.copyTo(output) }
        }
        f.absolutePath
    } catch (e: IOException) {
        null
    }

    private fun capturerPhoto() {
        try {
            val photoFile = File(requireContext().filesDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            tempCameraPath = photoFile.absolutePath
            takePicture.launch(uri)
        } catch (e: Exception) {
            toast("Caméra indisponible")
        }
    }

    private fun ouvrirCarte() {
        val lat = b.etLat.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val lng = b.etLng.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val nomComplet = "${b.etNom.text.toString().trim()} ${b.etPrenom.text.toString().trim()}"

        if (lat == null || lng == null) {
            toast("Aucune coordonnée GPS renseignée pour ce contact")
            return
        }
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
            toast("Coordonnées GPS invalides")
            return
        }

        // Tenter d'ouvrir l'application Google Maps
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(nomComplet)})")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast("Aucune application de cartographie disponible")
        }
    }

    private fun enregistrer() {
        val nom = b.etNom.text.toString().trim()
        val prenom = b.etPrenom.text.toString().trim()
        val tel = b.etTel.text.toString().trim()
        if (nom.isEmpty() || prenom.isEmpty() || tel.isEmpty()) {
            toast("Nom, prénom et téléphone sont obligatoires")
            return
        }

        val latTxt = b.etLat.text.toString().trim().replace(',', '.')
        val lngTxt = b.etLng.text.toString().trim().replace(',', '.')
        var lat: Double? = null
        var lng: Double? = null
        if (latTxt.isNotEmpty() || lngTxt.isNotEmpty()) {
            lat = latTxt.toDoubleOrNull()
            lng = lngTxt.toDoubleOrNull()
            if (lat == null || lng == null) {
                toast("Latitude et longitude doivent être numériques (ou toutes deux vides)")
                return
            }
            if (lat !in -90.0..90.0) {
                toast("Latitude entre -90 et 90")
                return
            }
            if (lng !in -180.0..180.0) {
                toast("Longitude entre -180 et 180")
                return
            }
        }

        val contact = Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = tel,
            email = b.etEmail.text.toString().trim().ifEmpty { null },
            photoUri = photoPath,
            latitude = lat,
            longitude = lng
        )

        vm.save(contact) { ok ->
            if (!isAdded) return@save
            if (ok) {
                toast("Contact enregistré")
                parentFragmentManager.popBackStack()
            } else {
                toast("Échec de l'enregistrement")
            }
        }
    }

    private fun confirmerSuppression() {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer")
            .setMessage("Voulez-vous vraiment supprimer ce contact ?")
            .setPositiveButton("Supprimer") { _, _ ->
                vm.delete(contactId) { ok ->
                    if (!isAdded) return@delete
                    if (ok) {
                        toast("Contact supprimé")
                        parentFragmentManager.popBackStack()
                    } else {
                        toast("Suppression impossible")
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun appeler() {
        val tel = b.etTel.text.toString().trim()
        if (tel.isEmpty()) {
            toast("Numéro vide")
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(tel)}")))
        } catch (e: ActivityNotFoundException) {
            toast("Aucune application téléphone")
        }
    }

    private fun envoyerMail() {
        val mail = b.etEmail.text.toString().trim()
        if (mail.isEmpty()) {
            toast("E-mail vide")
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mail")))
        } catch (e: ActivityNotFoundException) {
            toast("Aucune application e-mail")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo", photoPath)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"

        fun newInstance(contactId: Long = 0L) = DetailContactFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_CONTACT_ID, contactId)
            }
        }
    }
}

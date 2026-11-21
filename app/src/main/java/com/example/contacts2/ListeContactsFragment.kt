package com.example.contacts2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contacts2.databinding.FragmentListeBinding

class ListeContactsFragment : Fragment(R.layout.fragment_liste) {

    private val vm: ContactsViewModel by activityViewModels()
    private var _b: FragmentListeBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentListeBinding.bind(view)

        adapter = ContactAdapter { contact -> ouvrirDetail(contact.id) }
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        fun appliquer() {
            val q = b.searchBar.text.toString().trim().lowercase()
            val tous = vm.contacts.value.orEmpty()
            val res = if (q.isEmpty()) tous else tous.filter {
                it.nom.lowercase().contains(q) ||
                        it.prenom.lowercase().contains(q) ||
                        it.telephone.contains(q)
            }
            adapter.submit(res)
            b.tvVide.visibility = if (res.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.contacts.observe(viewLifecycleOwner) {
            appliquer()
        }

        b.searchBar.doAfterTextChanged { appliquer() }

        // Bouton flottant + pour ajouter un contact (mode création avec id = 0L)
        b.fabAdd.setOnClickListener {
            ouvrirDetail(0L)
        }
    }

    override fun onResume() {
        super.onResume()
        // Appliquer immédiatement les préférences utilisateur (taille, couleur, bouton appel)
        val context = requireContext()
        adapter.taillePoliceSp = PreferencesManager.getTaillePolice(context)
        adapter.couleurTexte = PreferencesManager.getCouleurPreferee(context)
        adapter.afficherBoutonAppel = PreferencesManager.isAppelDepuisListe(context)
        adapter.notifyDataSetChanged()
    }

    private fun ouvrirDetail(id: Long) {
        val fragment = DetailContactFragment.newInstance(id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

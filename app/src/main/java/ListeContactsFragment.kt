package com.example.contacts2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contacts2.databinding.FragmentListeBinding

class ListeContactsFragment : Fragment(R.layout.fragment_liste) {

    private val vm: ContactsViewModel by activityViewModels()
    private var _b: FragmentListeBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentListeBinding.bind(view)

        val adapter = ContactAdapter { contact -> ouvrirDetail(contact.id) }
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        fun appliquer() {
            val q = b.etRecherche.text.toString().trim().lowercase()
            val tous = vm.contacts.value.orEmpty()
            val res = if (q.isEmpty()) tous else tous.filter {
                it.nom.lowercase().contains(q) ||
                        it.prenom.lowercase().contains(q) ||
                        it.telephone.contains(q)
            }
            adapter.submit(res)
            b.tvVide.visibility = if (res.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.contacts.observe(viewLifecycleOwner) { appliquer() }
        b.etRecherche.doAfterTextChanged { appliquer() }
    }

    private fun ouvrirDetail(id: Int) {
        Toast.makeText(requireContext(), "Contact $id", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
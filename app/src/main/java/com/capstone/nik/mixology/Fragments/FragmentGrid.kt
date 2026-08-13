package com.capstone.nik.mixology.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nik.mixology.Adapters.DrinkAdapter
import com.capstone.nik.mixology.Activities.ActivityMain
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FragmentGrid : Fragment() {

    private val viewModel: FragmentGridViewModel by viewModels()
    private lateinit var adapter: DrinkAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private val filter: DrinkFilter
        get() = DrinkFilter.valueOf(requireArguments().getString(ARG_FILTER)!!)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_main)
        emptyView = rootView.findViewById(R.id.empty_view)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = DrinkAdapter(
            onItemSelected = { cocktail ->
                (activity as? ActivityMain)?.onItemSelected(cocktail)
            },
            onToggleSaved = { item ->
                val message = if (item.saved) R.string.drink_deleted else R.string.drink_added
                Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
                viewModel.toggleSaved(item)
            },
        )
        recyclerView.adapter = adapter
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.errorShown()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.drinks.collect { items ->
                    adapter.submitList(items)
                    val empty = filter.showEmptySaved && items.isEmpty()
                    recyclerView.visibility = if (empty) View.INVISIBLE else View.VISIBLE
                    emptyView.visibility = if (empty) View.VISIBLE else View.GONE
                    if (empty) {
                        emptyView.setText(R.string.empty_string_add_a_drink)
                    }
                }
            }
        }
        viewModel.bind(filter)
    }

    companion object {
        private const val ARG_FILTER = "filter"

        @JvmStatic
        fun newInstance(filter: DrinkFilter): FragmentGrid {
            return FragmentGrid().apply {
                arguments = bundleOf(ARG_FILTER to filter.name)
            }
        }
    }
}

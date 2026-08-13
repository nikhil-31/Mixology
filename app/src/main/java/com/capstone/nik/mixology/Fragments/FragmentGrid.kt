package com.capstone.nik.mixology.Fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter

class FragmentGrid : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private val viewModel: FragmentGridViewModel by viewModels()
    private lateinit var adapter: DrinkCursorAdapter
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
        adapter = DrinkCursorAdapter(null, requireActivity())
        recyclerView.adapter = adapter
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this)
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.errorShown()
            }
        }
        viewModel.refresh(filter)
    }

    override fun onResume() {
        super.onResume()
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(requireContext(), filter.contentUri, null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        adapter.swapCursor(data)
        val empty = filter.showEmptySaved && (data == null || data.count == 0)
        recyclerView.visibility = if (empty) View.INVISIBLE else View.VISIBLE
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        if (empty) {
            emptyView.setText(R.string.empty_string_add_a_drink)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.swapCursor(null)
    }

    companion object {
        private const val ARG_FILTER = "filter"
        private const val LOADER_ID = 0

        @JvmStatic
        fun newInstance(filter: DrinkFilter): FragmentGrid {
            return FragmentGrid().apply {
                arguments = bundleOf(ARG_FILTER to filter.name)
            }
        }
    }
}

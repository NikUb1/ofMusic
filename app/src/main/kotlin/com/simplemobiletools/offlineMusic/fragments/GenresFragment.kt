package com.simplemobiletools.offlineMusic.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.google.gson.Gson
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.offlineMusic.R
import com.simplemobiletools.offlineMusic.activities.SimpleActivity
import com.simplemobiletools.offlineMusic.activities.TracksActivity
import com.simplemobiletools.offlineMusic.adapters.GenresAdapter
import com.simplemobiletools.offlineMusic.databinding.FragmentGenresBinding
import com.simplemobiletools.offlineMusic.dialogs.ChangeSortingDialog
import com.simplemobiletools.offlineMusic.extensions.audioHelper
import com.simplemobiletools.offlineMusic.extensions.config
import com.simplemobiletools.offlineMusic.extensions.mediaScanner
import com.simplemobiletools.offlineMusic.extensions.viewBinding
import com.simplemobiletools.offlineMusic.helpers.GENRE
import com.simplemobiletools.offlineMusic.helpers.TAB_GENRES
import com.simplemobiletools.offlineMusic.models.Genre
import com.simplemobiletools.offlineMusic.models.sortSafely

class GenresFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var genres = ArrayList<Genre>()
    private val binding by viewBinding(FragmentGenresBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            val cachedGenres = activity.audioHelper.getAllGenres()
            activity.runOnUiThread {
                gotGenres(activity, cachedGenres)
            }
        }
    }

    private fun gotGenres(activity: BaseSimpleActivity, cachedGenres: ArrayList<Genre>) {
        genres = cachedGenres
        activity.runOnUiThread {
            val scanning = activity.mediaScanner.isScanning()
            binding.genresPlaceholder.text = if (scanning) {
                context.getString(R.string.loading_files)
            } else {
                context.getString(com.simplemobiletools.commons.R.string.no_items_found)
            }

            binding.genresPlaceholder.beVisibleIf(genres.isEmpty())

            val adapter = binding.genresList.adapter
            if (adapter == null) {
                GenresAdapter(activity, genres, binding.genresList) {
                    activity.hideKeyboard()
                    Intent(activity, TracksActivity::class.java).apply {
                        putExtra(GENRE, Gson().toJson(it as Genre))
                        activity.startActivity(this)
                    }
                }.apply {
                    binding.genresList.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    binding.genresList.scheduleLayoutAnimation()
                }
            } else {
                val oldItems = (adapter as GenresAdapter).items
                if (oldItems.sortedBy { it.id }.hashCode() != genres.sortedBy { it.id }.hashCode()) {
                    adapter.updateItems(genres)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = genres.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Genre>
        getAdapter()?.updateItems(filtered, text)
        binding.genresPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(genres)
        binding.genresPlaceholder.beGoneIf(genres.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_GENRES) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            genres.sortSafely(activity.config.genreSorting)
            adapter.updateItems(genres, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.genresPlaceholder.setTextColor(textColor)
        binding.genresFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.genresList.adapter as? GenresAdapter
}

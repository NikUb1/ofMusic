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
import com.simplemobiletools.offlineMusic.activities.AlbumsActivity
import com.simplemobiletools.offlineMusic.activities.SimpleActivity
import com.simplemobiletools.offlineMusic.adapters.ArtistsAdapter
import com.simplemobiletools.offlineMusic.databinding.FragmentArtistsBinding
import com.simplemobiletools.offlineMusic.dialogs.ChangeSortingDialog
import com.simplemobiletools.offlineMusic.extensions.audioHelper
import com.simplemobiletools.offlineMusic.extensions.config
import com.simplemobiletools.offlineMusic.extensions.mediaScanner
import com.simplemobiletools.offlineMusic.extensions.viewBinding
import com.simplemobiletools.offlineMusic.helpers.ARTIST
import com.simplemobiletools.offlineMusic.helpers.TAB_ARTISTS
import com.simplemobiletools.offlineMusic.models.Artist
import com.simplemobiletools.offlineMusic.models.sortSafely

// Artists -> Albums -> Tracks
class ArtistsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var artists = ArrayList<Artist>()
    private val binding by viewBinding(FragmentArtistsBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            val cachedArtists = activity.audioHelper.getAllArtists()
            activity.runOnUiThread {
                gotArtists(activity, cachedArtists)
            }
        }
    }

    private fun gotArtists(activity: BaseSimpleActivity, cachedArtists: ArrayList<Artist>) {
        artists = cachedArtists
        activity.runOnUiThread {
            val scanning = activity.mediaScanner.isScanning()
            binding.artistsPlaceholder.text = if (scanning) {
                context.getString(R.string.loading_files)
            } else {
                context.getString(com.simplemobiletools.commons.R.string.no_items_found)
            }
            binding.artistsPlaceholder.beVisibleIf(artists.isEmpty())

            val adapter = binding.artistsList.adapter
            if (adapter == null) {
                ArtistsAdapter(activity, artists, binding.artistsList) {
                    activity.hideKeyboard()
                    Intent(activity, AlbumsActivity::class.java).apply {
                        putExtra(ARTIST, Gson().toJson(it as Artist))
                        activity.startActivity(this)
                    }
                }.apply {
                    binding.artistsList.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    binding.artistsList.scheduleLayoutAnimation()
                }
            } else {
                val oldItems = (adapter as ArtistsAdapter).items
                if (oldItems.sortedBy { it.id }.hashCode() != artists.sortedBy { it.id }.hashCode()) {
                    adapter.updateItems(artists)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = artists.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Artist>
        getAdapter()?.updateItems(filtered, text)
        binding.artistsPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(artists)
        binding.artistsPlaceholder.beGoneIf(artists.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_ARTISTS) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            artists.sortSafely(activity.config.artistSorting)
            adapter.updateItems(artists, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.artistsPlaceholder.setTextColor(textColor)
        binding.artistsFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.artistsList.adapter as? ArtistsAdapter
}

package com.simplemobiletools.offlineMusic.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.offlineMusic.R
import com.simplemobiletools.offlineMusic.activities.ExcludedFoldersActivity
import com.simplemobiletools.offlineMusic.activities.SimpleActivity
import com.simplemobiletools.offlineMusic.activities.TracksActivity
import com.simplemobiletools.offlineMusic.adapters.FoldersAdapter
import com.simplemobiletools.offlineMusic.databinding.FragmentFoldersBinding
import com.simplemobiletools.offlineMusic.dialogs.ChangeSortingDialog
import com.simplemobiletools.offlineMusic.extensions.audioHelper
import com.simplemobiletools.offlineMusic.extensions.config
import com.simplemobiletools.offlineMusic.extensions.mediaScanner
import com.simplemobiletools.offlineMusic.extensions.viewBinding
import com.simplemobiletools.offlineMusic.helpers.FOLDER
import com.simplemobiletools.offlineMusic.helpers.TAB_FOLDERS
import com.simplemobiletools.offlineMusic.models.Folder
import com.simplemobiletools.offlineMusic.models.sortSafely

class FoldersFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var folders = ArrayList<Folder>()
    private val binding by viewBinding(FragmentFoldersBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            val folders = context.audioHelper.getAllFolders()

            activity.runOnUiThread {
                val scanning = activity.mediaScanner.isScanning()
                binding.foldersPlaceholder.text = if (scanning) {
                    context.getString(R.string.loading_files)
                } else {
                    context.getString(com.simplemobiletools.commons.R.string.no_items_found)
                }
                binding.foldersPlaceholder.beVisibleIf(folders.isEmpty())
                binding.foldersFastscroller.beGoneIf(binding.foldersPlaceholder.isVisible())
                binding.foldersPlaceholder2.beVisibleIf(folders.isEmpty() && context.config.excludedFolders.isNotEmpty() && !scanning)
                binding.foldersPlaceholder2.underlineText()

                binding.foldersPlaceholder2.setOnClickListener {
                    activity.startActivity(Intent(activity, ExcludedFoldersActivity::class.java))
                }

                this.folders = folders

                val adapter = binding.foldersList.adapter
                if (adapter == null) {
                    FoldersAdapter(activity, folders, binding.foldersList) {
                        activity.hideKeyboard()
                        Intent(activity, TracksActivity::class.java).apply {
                            putExtra(FOLDER, (it as Folder).title)
                            activity.startActivity(this)
                        }
                    }.apply {
                        binding.foldersList.adapter = this
                    }

                    if (context.areSystemAnimationsEnabled) {
                        binding.foldersList.scheduleLayoutAnimation()
                    }
                } else {
                    (adapter as FoldersAdapter).updateItems(folders)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = folders.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Folder>
        getAdapter()?.updateItems(filtered, text)
        binding.foldersPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(folders)
        binding.foldersPlaceholder.beGoneIf(folders.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_FOLDERS) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            folders.sortSafely(activity.config.folderSorting)
            adapter.updateItems(folders, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.foldersPlaceholder.setTextColor(textColor)
        binding.foldersFastscroller.updateColors(adjustedPrimaryColor)
        binding.foldersPlaceholder2.setTextColor(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.foldersList.adapter as? FoldersAdapter
}

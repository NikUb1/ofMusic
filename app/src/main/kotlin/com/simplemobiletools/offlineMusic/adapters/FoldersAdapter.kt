package com.simplemobiletools.offlineMusic.adapters

import android.view.View
import android.view.ViewGroup
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.highlightTextPart
import com.simplemobiletools.commons.extensions.setupViewBackground
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.offlineMusic.R
import com.simplemobiletools.offlineMusic.databinding.ItemFolderBinding
import com.simplemobiletools.offlineMusic.extensions.audioHelper
import com.simplemobiletools.offlineMusic.extensions.config
import com.simplemobiletools.offlineMusic.models.Events
import com.simplemobiletools.offlineMusic.models.Folder
import com.simplemobiletools.offlineMusic.models.Track
import org.greenrobot.eventbus.EventBus

class FoldersAdapter(
    activity: BaseSimpleActivity, items: ArrayList<Folder>, recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : BaseMusicAdapter<Folder>(items, activity, recyclerView, itemClick), RecyclerViewFastScroller.OnPopupTextUpdate {

    override fun getActionMenuId() = R.menu.cab_folders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderBinding.inflate(layoutInflater, parent, false)
        return createViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items.getOrNull(position) ?: return
        holder.bindView(folder, allowSingleClick = true, allowLongClick = true) { itemView, _ ->
            setupView(itemView, folder)
        }
        bindViewHolder(holder)
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_exclude_folders -> excludeFolders()
            R.id.cab_share -> shareFiles()
        }
    }

    private fun excludeFolders() {
        getSelectedItems().forEach {
            context.config.addExcludedFolder(it.path)
        }

        finishActMode()
        EventBus.getDefault().post(Events.RefreshFragments())
    }

    override fun getSelectedTracks(): List<Track> {
        val tracks = arrayListOf<Track>()
        getSelectedItems().forEach {
            tracks += context.audioHelper.getFolderTracks(it.title)
        }

        return tracks
    }

    private fun setupView(view: View, folder: Folder) {
        ItemFolderBinding.bind(view).apply {
            root.setupViewBackground(context)
            folderFrame.isSelected = selectedKeys.contains(folder.hashCode())
            folderTitle.text = if (textToHighlight.isEmpty()) folder.title else folder.title.highlightTextPart(textToHighlight, properPrimaryColor)
            folderTitle.setTextColor(textColor)

            val tracks = resources.getQuantityString(R.plurals.tracks_plural, folder.trackCount, folder.trackCount)
            folderTracks.text = tracks
            folderTracks.setTextColor(textColor)
        }
    }

    override fun onChange(position: Int) = items.getOrNull(position)?.getBubbleText(context.config.folderSorting) ?: ""
}

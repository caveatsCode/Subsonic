package github.daneren2005.dsub.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import github.daneren2005.dsub.R;
import github.daneren2005.dsub.domain.Artist;
import github.daneren2005.dsub.domain.Indexes;
import github.daneren2005.dsub.domain.MusicDirectory;
import github.daneren2005.dsub.domain.MusicFolder;
import github.daneren2005.dsub.service.MusicService;
import github.daneren2005.dsub.service.MusicServiceFactory;
import github.daneren2005.dsub.util.BackgroundTask;
import github.daneren2005.dsub.util.Constants;
import github.daneren2005.dsub.util.ProgressListener;
import github.daneren2005.dsub.util.TabBackgroundTask;
import github.daneren2005.dsub.util.Util;
import github.daneren2005.dsub.view.ArtistAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectArtistFragment extends SelectListFragment<Artist> {
	private static final String TAG = SelectArtistFragment.class.getSimpleName();
	private static final int MENU_GROUP_MUSIC_FOLDER = 10;

	private View folderButtonParent;
	private View folderButton;
	private TextView folderName;
	private List<MusicFolder> musicFolders = null;
	private List<MusicDirectory.Entry> entries;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		if(bundle != null) {
			musicFolders = (List<MusicFolder>) bundle.getSerializable(Constants.FRAGMENT_LIST2);
		}
		artist = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(Constants.FRAGMENT_LIST2, (Serializable) musicFolders);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		folderButton = null;
		super.onCreateView(inflater, container, bundle);

		if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			listView.setFastScrollAlwaysVisible(true);
		}

		if(objects != null) {
			if (Util.isOffline(context) || Util.isTagBrowsing(context)) {
				folderButton.setVisibility(View.GONE);
			}
			setMusicFolders();
		}

		return rootView;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Object entry = listView.getItemAtPosition(info.position);

		if (entry instanceof Artist) {
			onCreateContextMenu(menu, view, menuInfo, entry);
		} else if (info.position == 0) {
			String musicFolderId = Util.getSelectedMusicFolderId(context);
			MenuItem menuItem = menu.add(MENU_GROUP_MUSIC_FOLDER, -1, 0, R.string.select_artist_all_folders);
			if (musicFolderId == null) {
				menuItem.setChecked(true);
			}
			if (musicFolders != null) {
				for (int i = 0; i < musicFolders.size(); i++) {
					MusicFolder musicFolder = musicFolders.get(i);
					menuItem = menu.add(MENU_GROUP_MUSIC_FOLDER, i, i + 1, musicFolder.getName());
					if (musicFolder.getId().equals(musicFolderId)) {
						menuItem.setChecked(true);
					}
				}
			}
			menu.setGroupCheckable(MENU_GROUP_MUSIC_FOLDER, true, true);
		}

		recreateContextMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		if(menuItem.getGroupId() != getSupportTag()) {
			return false;
		}

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
		Artist artist = (Artist) listView.getItemAtPosition(info.position);

		if (artist != null) {
			return onContextItemSelected(menuItem, artist);
		} else if (info.position == 0) {
			MusicFolder selectedFolder = menuItem.getItemId() == -1 ? null : musicFolders.get(menuItem.getItemId());
			String musicFolderId = selectedFolder == null ? null : selectedFolder.getId();
			String musicFolderName = selectedFolder == null ? context.getString(R.string.select_artist_all_folders)
															: selectedFolder.getName();
			Util.setSelectedMusicFolderId(context, musicFolderId);
			folderName.setText(musicFolderName);
			context.invalidate();
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (view == folderButtonParent) {
			selectFolder();
		} else {
			Artist artist = (Artist) parent.getItemAtPosition(position);
			SubsonicFragment fragment = new SelectDirectoryFragment();
			Bundle args = new Bundle();
			args.putString(Constants.INTENT_EXTRA_NAME_ID, artist.getId());
			args.putString(Constants.INTENT_EXTRA_NAME_NAME, artist.getName());
			if("root".equals(artist.getId())) {
				args.putSerializable(Constants.FRAGMENT_LIST, (Serializable) entries);
			}
			args.putBoolean(Constants.INTENT_EXTRA_NAME_ARTIST, true);
			fragment.setArguments(args);

			replaceFragment(fragment);
		}
	}

	@Override
	protected void refresh(final boolean refresh) {
		listView.setVisibility(View.INVISIBLE);

		BackgroundTask<Indexes> task = new TabBackgroundTask<Indexes>(this) {
			@Override
			protected Indexes doInBackground() throws Throwable {
				MusicService musicService = MusicServiceFactory.getMusicService(context);
				if (!Util.isOffline(context) && !Util.isTagBrowsing(context)) {
					musicFolders = musicService.getMusicFolders(refresh, context, this);
				}
				String musicFolderId = Util.getSelectedMusicFolderId(context);
				return musicService.getIndexes(musicFolderId, refresh, context, this);
			}

			@Override
			protected void done(Indexes result) {
				objects = new ArrayList<Artist>(result.getShortcuts().size() + result.getArtists().size());
				objects.addAll(result.getShortcuts());
				objects.addAll(result.getArtists());
				listView.setFastScrollEnabled(false);
				listView.setAdapter(adapter = getAdapter(objects));
				listView.setFastScrollEnabled(true);
				entries = result.getEntries();

				setMusicFolders();
				listView.setVisibility(View.VISIBLE);
				refreshLayout.setRefreshing(false);
			}
		};
		task.execute();
	}

	@Override
	public int getOptionsMenu() {
		return R.menu.select_artist;
	}

	@Override
	public ArrayAdapter getAdapter(List<Artist> objects) {
		createMusicFolderButton();
		return new ArtistAdapter(context, objects);
	}

	@Override
	public List<Artist> getObjects(MusicService musicService, boolean refresh, ProgressListener listener) throws Exception {
		return null;
	}

	@Override
	public int getTitleResource() {
		return R.string.button_bar_browse;
	}

	private void createMusicFolderButton() {
		if(folderButton == null) {
			folderButtonParent = getLayoutInflater(null).inflate(R.layout.select_artist_header, listView, false);
			folderName = (TextView) folderButtonParent.findViewById(R.id.select_artist_folder_2);
			listView.addHeaderView(folderButtonParent);
			folderButton = folderButtonParent.findViewById(R.id.select_artist_folder);
		}

		if (Util.isOffline(context) || Util.isTagBrowsing(context)) {
			folderButton.setVisibility(View.GONE);
		} else {
			folderButton.setVisibility(View.VISIBLE);
		}
	}

	private void setMusicFolders() {
		// Display selected music folder
		if (musicFolders != null) {
			String musicFolderId = Util.getSelectedMusicFolderId(context);
			if (musicFolderId == null) {
				folderName.setText(R.string.select_artist_all_folders);
			} else {
				for (MusicFolder musicFolder : musicFolders) {
					if (musicFolder.getId().equals(musicFolderId)) {
						folderName.setText(musicFolder.getName());
						break;
					}
				}
			}
		}
	}

	private void selectFolder() {
		folderButton.showContextMenu();
	}
}

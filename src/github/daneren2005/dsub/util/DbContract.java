/*
  This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2014 (C) Scott Jackson
*/

package github.daneren2005.dsub.util;

import android.provider.BaseColumns;

public final class DbContract {
	public DbContract() {}

	public static abstract class EntryContract implements BaseColumns {
		public static final String table = "entries";

		public static final String server = "server";
		public static final String id = "id";
		public static final String parent = "parent";
		public static final String grandParent = "grandParent";
		public static final String albumId = "albumId";
		public static final String artistId = "artistId";
		public static final String directory = "directory";
		public static final String title = "title";
		public static final String album = "album";
		public static final String artist = "artist";
		public static final String track = "track";
		public static final String year = "year";
		public static final String genre = "genre";
		public static final String contentType = "contentType";
		public static final String suffix = "suffix";
		public static final String transcodedContentType = "transcodedContentType";
		public static final String transcodedSuffix = "transcodedSuffix";
		public static final String coverArt = "coverArt";
		public static final String size = "size";
		public static final String duration = "duration";
		public static final String bitRate = "bitRate";
		public static final String path = "path";
		public static final String video = "video";
		public static final String discNumber = "discNumber";
		public static final String starred = "starred";
		public static final String bookmark = "bookmark";
	}

	public static abstract class EntryListContract implements BaseColumns {
		public static final String table = "entryList";

		public static final String server = "server";
		public static final String id = "listId";
		public static final String name = "name";
		public static final String type = "type";
		public static final String parent = "parentEntry";
	}

	public static int PLAYLIST = 1;
	public static int FOLDER = 2;
	public static int INDEX = 3;
	public static int ARTIST = 4;
	public static int ALBUM = 5;
	public static int NOW_PLAYING = 6;

	public static abstract class EntryListEntriesContract implements BaseColumns {
		public static final String table = "entryListEntries";

		public static final String server = "server";
		public static final String listId = "listIds";
		public static final String entryId = "entryIds";
	}
}

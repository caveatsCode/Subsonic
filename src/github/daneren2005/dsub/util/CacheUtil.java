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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.List;

import github.daneren2005.dsub.domain.MusicDirectory;

import static github.daneren2005.dsub.domain.MusicDirectory.Entry;
import static github.daneren2005.dsub.util.DbContract.EntryContract;
import static github.daneren2005.dsub.util.DbContract.EntryListContract;
import static github.daneren2005.dsub.util.DbContract.EntryListEntriesContract;

public final class CacheUtil {
	private static final String TAG = CacheUtil.class.getSimpleName();

	public static MusicDirectory getEntries(SQLiteDatabase db, int server, String id, int type) {
		String[] projection = {
			EntryListContract.id,
			EntryListContract.name,
			EntryContract.id,
			EntryContract.parent,
			EntryContract.grandParent,
			EntryContract.albumId,
			EntryContract.artistId,
			EntryContract.directory,
			EntryContract.title,
			EntryContract.album,
			EntryContract.artist,
			EntryContract.track,
			EntryContract.year,
			EntryContract.genre,
			EntryContract.contentType,
			EntryContract.suffix,
			EntryContract.transcodedContentType,
			EntryContract.transcodedSuffix,
			EntryContract.coverArt,
			EntryContract.size,
			EntryContract.duration,
			EntryContract.bitRate,
			EntryContract.path,
			EntryContract.video,
			EntryContract.discNumber,
			EntryContract.starred,
			EntryContract.bookmark
		};

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EntryListContract.table +
			" LEFT OUTER JOIN " + EntryListEntriesContract.table + " ON " + EntryListContract.table + "." + EntryListContract.id + " = " + EntryListEntriesContract.table + "." + EntryListEntriesContract.listId +
			" LEFT OUTER JOIN " + EntryContract.table + " ON " + EntryListEntriesContract.entryId + " = " + EntryContract.id);

		String selection = EntryListContract.table + "." + EntryListContract.id + " LIKE ? AND " + EntryListContract.table + "." + EntryListContract.server + " LIKE ?";
		String[] selectionArgs = {id, Integer.toString(server)};

		Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, null);
		if(!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		MusicDirectory dir = new MusicDirectory();
		dir.setId(cursor.getString(cursor.getColumnIndexOrThrow(EntryListContract.id)));
		dir.setName(cursor.getString(cursor.getColumnIndexOrThrow(EntryListContract.name)));
		do {
			Entry entry = new Entry();
			entry.setId(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.id)));
			entry.setParent(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.parent)));
			entry.setGrandParent(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.grandParent)));
			entry.setAlbumId(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.albumId)));
			entry.setArtistId(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.artistId)));
			entry.setDirectory(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.directory)) > 0);
			entry.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.title)));
			entry.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.album)));
			entry.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.artist)));
			entry.setTrack(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.track)));
			entry.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.year)));
			entry.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.genre)));
			entry.setContentType(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.contentType)));
			entry.setSuffix(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.suffix)));
			entry.setTranscodedContentType(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.transcodedContentType)));
			entry.setTranscodedSuffix(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.transcodedSuffix)));
			entry.setCoverArt(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.coverArt)));
			entry.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(EntryContract.size)));
			entry.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.duration)));
			entry.setBitRate(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.bitRate)));
			entry.setPath(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.path)));
			entry.setVideo(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.video)) > 0);
			entry.setDiscNumber(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.discNumber)));
			entry.setStarred(cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.starred)) > 0);
			// entry.setBookmark(cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.bookmark)));

			dir.addChild(entry);
		} while(cursor.moveToNext());
		cursor.close();

		return dir;
	}

	public static void updateEntries(SQLiteDatabase db, int server, MusicDirectory dir, int type) {
		// Update parent listing
		ContentValues values = new ContentValues();
		values.put(EntryListContract.server, server);
		values.put(EntryListContract.id, dir.getId());
		values.put(EntryListContract.name, dir.getName());
		values.put(EntryListContract.type, type);

		String selection = EntryListContract.id + " LIKE ? AND " + EntryListContract.server + " LIKE ?";
		String[] selectionArgs = {dir.getId(), Integer.toString(server)};
		int updated = db.update(EntryListContract.table, values, selection, selectionArgs);

		if(updated == 0) {
			db.insertOrThrow(EntryListContract.table, null, values);
		}

		// Update all entry data
		updateEntries(db, server, dir.getChildren());

		// Update parent/entry linkage
		updateEntryList(db, server, dir.getId(), dir.getChildren());
	}
	public static void updateEntries(SQLiteDatabase db, int server, List<Entry> entries) {
		for(Entry entry: entries) {
			ContentValues values = new ContentValues();
			values.put(EntryContract.server, server);
			values.put(EntryContract.id, entry.getId());
			values.put(EntryContract.parent, entry.getParent());
			values.put(EntryContract.grandParent, entry.getGrandParent());
			values.put(EntryContract.albumId, entry.getAlbumId());
			values.put(EntryContract.artistId, entry.getArtistId());
			values.put(EntryContract.directory, entry.isDirectory());
			values.put(EntryContract.title, entry.getTitle());
			values.put(EntryContract.album, entry.getAlbum());
			values.put(EntryContract.artist, entry.getArtist());
			values.put(EntryContract.track, entry.getTrack());
			values.put(EntryContract.year, entry.getYear());
			values.put(EntryContract.genre, entry.getGenre());
			values.put(EntryContract.contentType, entry.getContentType());
			values.put(EntryContract.suffix, entry.getSuffix());
			values.put(EntryContract.transcodedContentType, entry.getTranscodedContentType());
			values.put(EntryContract.transcodedSuffix, entry.getTranscodedSuffix());
			values.put(EntryContract.coverArt, entry.getCoverArt());
			values.put(EntryContract.size, entry.getSize());
			values.put(EntryContract.duration, entry.getDuration());
			values.put(EntryContract.bitRate, entry.getBitRate());
			values.put(EntryContract.path, entry.getPath());
			values.put(EntryContract.video, entry.isVideo());
			values.put(EntryContract.discNumber, entry.getDiscNumber());
			values.put(EntryContract.starred, entry.isStarred());
			values.put(EntryContract.bookmark, (String) null);

			String selection = EntryContract.id + " LIKE ? AND " + EntryContract.server + " LIKE ?";
			String[] selectionArgs = {entry.getId(), Integer.toString(server)};
			int updated = db.update(EntryContract.table, values, selection, selectionArgs);

			if(updated == 0) {
				db.insertOrThrow(EntryContract.table, null, values);
			}
		}
	}
	private static void updateEntryList(SQLiteDatabase db, int server, String listId, List<Entry> entries) {
		// Remove old linkages
		String removeSelection = EntryListEntriesContract.server + " LIKE ? AND " + EntryListEntriesContract.listId + " LIKE ?";
		String[] removeArgs = { String.valueOf(server), listId };
		db.delete(EntryListEntriesContract.table, removeSelection, removeArgs);

		for(Entry entry: entries) {
			ContentValues values = new ContentValues();
			values.put(EntryListEntriesContract.server, server);
			values.put(EntryListEntriesContract.listId, listId);
			values.put(EntryListEntriesContract.entryId, entry.getId());
			db.insertOrThrow(EntryListEntriesContract.table, null, values);
		}
	}
}

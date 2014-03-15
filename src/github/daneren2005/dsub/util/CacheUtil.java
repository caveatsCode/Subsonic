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
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import github.daneren2005.dsub.domain.MusicDirectory;

import static github.daneren2005.dsub.util.DbContract.EntryListContract;

public final class CacheUtil {
	public static void updateEntries(SQLiteDatabase db, int server, MusicDirectory dir, int type) {
		// Update parent listing
		ContentValues values = new ContentValues();
		values.put(EntryListContract.server, server);
		values.put(EntryListContract.id, dir.getId());
		values.put(EntryListContract.name, dir.getName());
		values.put(EntryListContract.type, type);
		db.insert(EntryListContract.table, null, values);

		// Update all entry data
		updateEntries(db, dir.getChildren());
	}
	public static void updateEntries(SQLiteDatabase db, List<MusicDirectory.Entry> entries) {

	}
}

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static github.daneren2005.dsub.util.DbContract.EntryContract;
import static github.daneren2005.dsub.util.DbContract.EntryListContract;
import static github.daneren2005.dsub.util.DbContract.EntryListEntriesContract;

public class DbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "entries.db";

	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + EntryContract.table +
		EntryContract._ID + "BIGINT PRIMARY KEY, " + EntryContract.id + " TEXT, " + EntryContract.parent + " TEXT, " +
		EntryContract.grandParent + " TEXT, " + EntryContract.albumId + " TEXT, " + EntryContract.artistId + " TEXT, " +
		EntryContract.directory + " BOOLEAN, " + EntryContract.title + " TEXT, " + EntryContract.album + " TEXT, " +
		EntryContract.artist +" TEXT, " + EntryContract.track + " INTEGER, " + EntryContract.year + " INTEGER, " +
		EntryContract.genre + " TEXT, " + EntryContract.contentType + " TEXT, " + EntryContract.suffix +" TEXT, " +
		EntryContract.transcodedContentType + " TEXT, " + EntryContract.transcodedSuffix + " TEXT, " +
		EntryContract.coverArt + " TEXT, " + EntryContract.size + " BIGINT, " + EntryContract.duration + " INTEGER, " +
		EntryContract.bitRate + " INTEGER, " + EntryContract.path + " TEXT, " + EntryContract.video + " BOOLEAN, " +
		EntryContract.discNumber + " INTEGER, " + EntryContract.starred + " BOOLEAN, " + EntryContract.bookmark + " INTEGER";
	public static final String SQL_CREATE_ENTRY_LIST = "CREATE TABLE " + EntryListContract.table +
		EntryListContract._ID + "BIGINT PRIMARY KEY, " + EntryListContract.id + " TEXT, " + EntryListContract.name + " TEXT, " +
		EntryListContract.type + " SMALLINT, " + EntryListContract.parent + " TEXT, " + EntryListContract.server + " INTEGER";
	public static final String SQL_CREATE_ENTRY_LIST_ENTRIES = "CREATE TABLE " + EntryListEntriesContract.table +
		EntryListEntriesContract._ID + "BIGINT PRIMARY KEY, " + EntryListEntriesContract.id + " TEXT, "+
		EntryListEntriesContract.entryId + " TEXT";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + EntryContract.table;
	private static final String SQL_DELETE_ENTRY_LIST = "DROP TABLE IF EXISTS " + EntryListContract.table;
	private static final String SQL_DELETE_ENTRY_LIST_ENTRIES = "DROP TABLE IF EXISTS " + EntryListEntriesContract.table;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
		db.execSQL(SQL_CREATE_ENTRY_LIST);
		db.execSQL(SQL_CREATE_ENTRY_LIST_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		db.execSQL(SQL_DELETE_ENTRY_LIST);
		db.execSQL(SQL_DELETE_ENTRY_LIST_ENTRIES);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}

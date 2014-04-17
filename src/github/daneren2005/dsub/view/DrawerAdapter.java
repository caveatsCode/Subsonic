/*
	This file is part of Subsonic.

	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

	Copyright 2009 (C) Sindre Mehus
*/
package github.daneren2005.dsub.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import github.daneren2005.dsub.R;

/**
 * Created by Scott on 11/8/13.
 */
public class DrawerAdapter extends ArrayAdapter<String> {
	private static String TAG = DrawerAdapter.class.getSimpleName();
	private Context context;
	private List<String> items;
	private List<Integer> icons;
	private List<Boolean> visible;

	public DrawerAdapter(Context context, List<String> items, List<Integer> icons, List<Boolean> visible) {
		super(context, R.layout.drawer_list_item, items);

		this.context = context;
		this.items = items;
		this.icons = icons;
		this.visible = visible;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		position = getActualPosition(position);
		String item = items.get(position);
		Integer icon = icons.get(position);

		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.drawer_list_item, null);
		}

		TextView textView = (TextView) convertView.findViewById(R.id.drawer_name);
		textView.setText(item);
		ImageView iconView = (ImageView) convertView.findViewById(R.id.drawer_icon);
		iconView.setImageResource(icon);

		return convertView;
	}

	@Override
	public int getCount() {
		int count = 0;
		for(int i = 0; i < visible.size(); i++) {
			if(visible.get(i)) {
				count++;
			}
		}

		return count;
	}

	public int getActualPosition(int position) {
		for(int i = 0; i <= position; i++) {
			if(!visible.get(i)) {
				position++;
			}
		}

		return position;
	}
	public int getAdapterPosition(int position) {
		for(int i = position; i >= 0; i--) {
			if(!visible.get(i)) {
				position--;
			}
		}

		return position;
	}

	public void setItemVisible(int position, boolean visible) {
		if(this.visible.get(position) != visible) {
			this.visible.set(position, visible);
			notifyDataSetInvalidated();
		}
	}
	public void setDownloadVisible(boolean visible) {
		setItemVisible(items.size() - 2, visible);
	}
}

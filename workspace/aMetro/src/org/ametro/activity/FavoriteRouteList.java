/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.ametro.activity;

import static org.ametro.Constants.STATION_FROM_ID;
import static org.ametro.Constants.STATION_TO_ID;

import java.util.ArrayList;

import org.ametro.model.SubwayMap;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FavoriteRouteList extends ListActivity {

	private Point[] mRoutes;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRoutes = BrowseVectorMap.Instance.getFavoriteRoutes();
		
		final SubwayMap map = BrowseVectorMap.Instance.getSubwayMap();
		final ArrayList<String> routeNames = new ArrayList<String>();
		for(Point p : mRoutes){
			String name = map.stations[p.x].name + " - " + map.stations[p.y].name;
			routeNames.add(name);
		}
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, routeNames);
		setListAdapter(adapter);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent data = new Intent();
		Point r = mRoutes[position];
		data.putExtra(STATION_FROM_ID, r.x);
		data.putExtra(STATION_TO_ID, r.y);
		setResult(RESULT_OK, data);
		finish();
		super.onListItemClick(l, v, position, id);
	}
}
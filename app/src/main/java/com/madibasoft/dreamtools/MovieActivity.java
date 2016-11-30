package com.madibasoft.dreamtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.MovieObject;
import com.webhiker.enigma2.api.ServiceObject;


public class MovieActivity extends AbstractServiceObjectActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	List<ServiceObject> getGroups() throws IOException, JSONException {
//		Enigma2API api = DreamToolsActivity.getAPI(MovieActivity.this);
		List<ServiceObject> res = new ArrayList<ServiceObject>();
		res.add(new ServiceObject(new JSONObject("{\"e2servicename\":\""+getString(R.string.movies)+"\"}")));
		return res;
	}

	@Override
	void childClicked(ServiceObject service) throws IOException {
		Enigma2API api = DreamToolsActivity.getAPI(MovieActivity.this);
		api.zapTo(service);
	}

	@Override
	List<MovieObject> getChildren(ServiceObject group) throws JSONException, IOException {
		Enigma2API api = DreamToolsActivity.getAPI(MovieActivity.this);
		return api.getMovies();
	}

}

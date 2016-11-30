package com.madibasoft.dreamtools;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.os.Bundle;

import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceObject;


public class ZapActivity extends AbstractServiceObjectActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	List<ServiceObject> getGroups() throws IOException, JSONException {
		Enigma2API api = DreamToolsActivity.getAPI(ZapActivity.this);
		return api.getBouquets();
	}

	@Override
	void childClicked(ServiceObject service) throws IOException {
		Enigma2API api = DreamToolsActivity.getAPI(ZapActivity.this);
		api.zapTo(service);
	}

	@Override
	List<ServiceObject> getChildren(ServiceObject bouquet) throws JSONException, IOException {
		Enigma2API api = DreamToolsActivity.getAPI(ZapActivity.this);
		return api.getBouquetServices(bouquet);
	}

}

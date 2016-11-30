package com.madibasoft.dreamtools;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.webhiker.enigma2.api.Enigma2API;
import com.webhiker.enigma2.api.ServiceObject;


public class ServiceSelectorActivity extends AbstractServiceObjectActivity {
	public static final String SERVICE_RESULT = "serviceresult";
	public static final int SERVICE_ACTIVITY_RESULT = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,false);
	}

	@Override
	List<? extends ServiceObject> getGroups() throws IOException, JSONException {
		Enigma2API api = DreamToolsActivity.getAPI(ServiceSelectorActivity.this);
		return api.getBouquets();
	}

	@Override
	void childClicked(ServiceObject service) throws IOException {
		// store zap channel
		Log.i("serviceactivity","storing channel "+service.getName());
		Intent resultIntent = new Intent();
		resultIntent.putExtra(SERVICE_RESULT, service.toJSONString());
		setResult(Activity.RESULT_OK, resultIntent);
		Toast.makeText(getBaseContext(), service.getName(), Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	List<? extends ServiceObject> getChildren(ServiceObject bouquet)
			throws JSONException, IOException {
		Enigma2API api = DreamToolsActivity.getAPI(ServiceSelectorActivity.this);
		return api.getBouquetServices(bouquet);
	}

}
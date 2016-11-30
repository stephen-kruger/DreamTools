package com.madibasoft.dreamtools;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.webhiker.enigma2.api.Enigma2API;

public class VideoActivity extends Activity {

	private VideoView mVideoView;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_video);
		setTitle(getString(R.string.stream));
		mVideoView = (VideoView) findViewById(R.id.videoView);

		/*
		 * Alternatively,for streaming media you can use
		 * mVideoView.setVideoURI(Uri.parse(URLstring));
		 */
		Enigma2API api = DreamToolsActivity.getAPI(this);
		try {
//			ServiceInformationObject sio = api.getCurrent();
//			mVideoView.setVideoURI(Uri.parse(api.getCurrentStream(sio.getService())));
			mVideoView.setVideoURI(Uri.parse(api.getCurrentStream()));
			mVideoView.setMediaController(new MediaController(this));
			mVideoView.requestFocus();
		}
		catch (Throwable t) {
			Utils.infoDialog(this, getString(R.string.error), t.getMessage()+"("+api.getCurrentStream()+")", true);
		}
	}
}
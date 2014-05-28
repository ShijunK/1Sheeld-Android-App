package com.integreight.onesheeld.shields.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.Foursquare;
import com.integreight.onesheeld.shields.controller.utils.Foursquare.DialogListener;
import com.integreight.onesheeld.shields.controller.utils.FoursquareDialogError;
import com.integreight.onesheeld.shields.controller.utils.FoursquareError;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.ControllerParent;

public class FoursquareShield extends ControllerParent<FoursquareShield> {

	private FoursquareEventHandler eventHandler;
	// private static final byte FOURSQUARE_COMMAND = (byte) 0x1B;
	private static final byte CHECKIN_METHOD_ID = (byte) 0x01;
	Foursquare foursquare;
	String clientID = "SXCMMTAXT05AVQYQS5S3ZCOLEKCN5ZCDJKQYBAFJJUDJIEC0";
	String clientSecret = "RQB3QEA2HLA0TQEM3XPOGJEJ4IHHK14LUMEOPI1BOIYFEHXA";
	String redirectUrl = "http://www.1sheeld.com";
	String placeID = "";
	String message = "";

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	public FoursquareShield() {
		super();
	}

	@Override
	public ControllerParent<FoursquareShield> setTag(String tag) {
		// getShareprefrences
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);

		return super.setTag(tag);
	}

	public FoursquareShield(Activity activity, String tag) {
		super(activity, tag);
		// getShareprefrences
	}

	public boolean isFoursquareLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences
				.getBoolean("PREF_KEY_FOURSQUARE_LOGIN", false);
	}

	public void setFoursquareEventHandler(FoursquareEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface FoursquareEventHandler {
		void onPlaceCheckin(String placeName);

		void setLastPlaceCheckin(String placeName);

		void onForsquareLoggedIn(String userName);

		void onForsquareLogout();

		void onForsquareError();

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == UIShield.FOURSQUARE_SHIELD.getId()) {
			if (isFoursquareLoggedInAlready())
				if (frame.getFunctionId() == CHECKIN_METHOD_ID) {
					placeID = frame.getArgumentAsString(0);
					message = frame.getArgumentAsString(1);
					if (ConnectionDetector
							.isConnectingToInternet(getApplication()
									.getApplicationContext())) {
						ConnectFour connectFour = new ConnectFour();
						connectFour.execute("");
					} else
						Toast.makeText(
								getApplication().getApplicationContext(),
								"Please check your Internet connection and try again.",
								Toast.LENGTH_SHORT).show();
				}
		}
	}

	private class ConnectFour extends AsyncTask<String, String, String> {

		String response = "";

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				String foursquare_token = mSharedPreferences.getString(
						"PREF_FourSquare_OAUTH_TOKEN", null);
				String placeId = placeID;
				String messageId = URLEncoder.encode(message, "UTF-8");
				String checkinURLRequest = "https://api.foursquare.com/v2/checkins/add?venueId="
						+ placeId
						+ "&"
						+ "shout="
						+ messageId
						+ "&broadcast=public&oauth_token="
						+ foursquare_token
						+ "&v=20140201";
				Log.d("checkinURLRequest", checkinURLRequest);
				HttpPost post = new HttpPost(checkinURLRequest);

				HttpClient hc = new DefaultHttpClient();
				HttpResponse rp = hc.execute(post);
				// Log.d("response from server ",EntityUtils.toString(rp.getEntity())+"");
				// EntityUtils.toString(rp.getEntity());
				HttpEntity mEntity = rp.getEntity();
				InputStream resp = mEntity.getContent();
				try {
					response = getStringFromInputStream(resp);
				} catch (Exception e) {
					response = getStringFromInputStream(resp);
				}

				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Log.d("Response From Server ::", rp.toString());
				}
			} catch (Exception e) {
				Log.d("HTTP ERROR ::", e.toString());
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// parse checkin response !
			try {
				JSONObject json = new JSONObject(result);
				JSONObject response = json.getJSONObject("response");
				JSONObject checkins = response.getJSONObject("checkin");
				JSONObject venue = checkins.getJSONObject("venue");
				String placeName = venue.getString("name");
				if (eventHandler != null)
					eventHandler.onPlaceCheckin(placeName);
				// save in share prefrences
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				editor.putString("PREF_FourSquare_LastPlace", placeName);
				// Commit the edits!
				editor.commit();

			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Exception of Parsing checkin response :: ", e.toString());
			}

		}

	}

	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	private class ParseUserFoursquareData extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String aa = null;

			try {

				aa = foursquare.request("users/self");
				// show and save prefrences user name , last place
				// checkin
				// jsonParser(aa);
				Log.d("Foursquare-Main", aa);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return aa;
		}

		@Override
		protected void onPostExecute(String result) {

			try {
				JSONObject json = new JSONObject(result);
				JSONObject response = json.getJSONObject("response");
				JSONObject user = response.getJSONObject("user");
				String userName = user.getString("firstName");
				// Set user name UI
				if (eventHandler != null)
					eventHandler.onForsquareLoggedIn(userName);
				JSONObject venue = user.getJSONObject("checkins")
						.getJSONArray("items").getJSONObject(0)
						.getJSONObject("venue");
				String placeName = venue.getString("name");
				if (eventHandler != null)
					eventHandler.setLastPlaceCheckin(placeName);

				// save in share prefrences
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				editor.putString("PREF_FourSquare_UserName", userName);
				editor.putString("PREF_FourSquare_LastPlace", placeName);

				// Commit the edits!
				editor.commit();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("Exception of Parsing User login response :: ",
						e.toString());

			}

		}

	}

	private class FoursquareAuthenDialogListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			new ParseUserFoursquareData().execute("");
		}

		@Override
		public void onFoursquareError(FoursquareError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(FoursquareDialogError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

	}

	public void jsonParser(String result) {
		try {
			JSONObject json = new JSONObject(result);
			JSONObject response = json.getJSONObject("response");
			JSONObject user = response.getJSONObject("user");
			String userName = user.getString("firstName");
			// Set user name UI
			if (eventHandler != null)
				eventHandler.onForsquareLoggedIn(userName);
			JSONObject venue = user.getJSONObject("checkins")
					.getJSONArray("items").getJSONObject(0)
					.getJSONObject("venue");
			String placeName = venue.getString("name");
			if (eventHandler != null)
				eventHandler.onPlaceCheckin(placeName);

			// save in share prefrences
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString("PREF_FourSquare_UserName", userName);
			editor.putString("PREF_FourSquare_LastPlace", placeName);

			// Commit the edits!
			editor.commit();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void loginToFoursquare() {
		foursquare = new Foursquare(clientID, clientSecret, redirectUrl);

		foursquare.authorize(getActivity(),
				new FoursquareAuthenDialogListener());
	}
	

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}

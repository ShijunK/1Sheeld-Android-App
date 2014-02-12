package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.LedShield.LedEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class LedFragment extends ShieldFragmentParent<LedFragment> {

	ImageView ledImage;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.led_shield_fragment_layout,
				container, false);
		ledImage = (ImageView) v.findViewById(R.id.led_shield_led_imageview);
		return v;

	}

	@Override
	public void onStart() {
		if (((LedShield) getApplication().getRunningShields().get(
				getControllerTag())) != null)
			toggleLed(((LedShield) getApplication().getRunningShields().get(
					getControllerTag())).refreshLed());
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();
	}

	@Override
	public void onStop() {
		if (getApplication().getRunningShields().get(getControllerTag()) != null)
			getApplication().getRunningShields().get(getControllerTag())
					.setHasForgroundView(false);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.led_fragment_connect_button);

		final CharSequence[] items = { "0", "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4",
				"A5" };

		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				AlertDialog.Builder builder3 = new AlertDialog.Builder(
						getActivity());
				builder3.setTitle("Connect With").setItems(items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// TODO Auto-generated method stub
								((LedShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.setLedEventHandler(ledEventHandler);
								((LedShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.setConnected(new ArduinoConnectedPin(
												which, ArduinoFirmata.INPUT));
								toggleLed(((LedShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.refreshLed());

							}

						});

				builder3.show();

			}

		});
	}

	private LedEventHandler ledEventHandler = new LedEventHandler() {

		@Override
		public void onLedChange(boolean isLedOn) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				toggleLed(isLedOn);

		}
	};

	private void toggleLed(boolean isOn) {
		if (isOn) {
			ledImage.setImageResource(R.drawable.led_shield_led_on);
		} else {
			ledImage.setImageResource(R.drawable.led_shield_led_off);
		}
	}

	// private void intializeFirmata(ArduinoFirmata firmata, int connectedPin){
	//
	// led=new Led(firmata,connectedPin);
	// led.setLedEventHandler(ledEventHandler);
	// toggleLed(led.isLedOn());
	// }

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new LedShield(getActivity(), getControllerTag()));
		}

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();

	}

}

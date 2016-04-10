package com.ankurmittal.torch;

import java.io.IOException;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements
		SurfaceHolder.Callback {

	private ImageView toggleView;
	private Thread thread;
	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	Parameters params;
	SurfaceHolder mHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toggleView = (ImageView) findViewById(R.id.toggleView);

		SurfaceView preview = (SurfaceView) findViewById(R.id.preview);
		mHolder = preview.getHolder();
		mHolder.addCallback(this);

		hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

		if (!hasFlash) {
			// device doesn't support flash
			// Show alert message and close the application
			AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
					.create();
			alert.setTitle(getString(R.string.alert_error));
			alert.setMessage(getString(R.string.alert_flash_not_avail_message));
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.setCanceledOnTouchOutside(false);
			alert.show();

			return;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		// on starting the app get the camera params
		getCamera();
		if (camera.getParameters().getFlashMode()
				.equals(Camera.Parameters.FLASH_MODE_TORCH)
				|| camera.getParameters().getFlashMode()
						.equals(Camera.Parameters.FLASH_MODE_ON)) {
			isFlashOn = true;
			toggleView.setImageDrawable(getResources().getDrawable(
					R.drawable.torch_on));
		} else {
			isFlashOn = false;
			toggleView.setImageDrawable(getResources().getDrawable(
					R.drawable.torch_off));
		}
		runOneMinuteThread();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void runOneMinuteThread() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(60 * 1000);
					showAlertDialog();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		thread.setName("One miunte thread");
		thread.start();

	}

	private void showAlertDialog() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
						.create();
				alert.setTitle(getString(R.string.alert_reminder_title));
				alert.setMessage(getString(R.string.alert_reminder_message));
				alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onStart();
							}
						});

				alert.setCanceledOnTouchOutside(false);
				alert.show();

			}
		});
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		mHolder = null;
	}

	@Override
	protected void onStop() {
		super.onStop();
		// on stop release the camera
		if (camera != null) {
			camera.release();
			camera = null;
		}
		if (thread.isAlive()) {
			thread.interrupt();
		}
	}

	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				params = camera.getParameters();
			} catch (RuntimeException e) {
				//Handle Exception
			}
		}
	}

	private void turnOnFlash() {
		if (!isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			camera.startPreview();
			isFlashOn = true;

		}

	}

	private void turnOffFlash() {
		if (isFlashOn) {
			if (camera == null || params == null) {
				return;
			}

			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(params);
			camera.stopPreview();
			isFlashOn = false;

		}
	}

	public void click(View v) {
		if (isFlashOn) {
			toggleView.setImageDrawable(getResources().getDrawable(
					R.drawable.torch_off));
			turnOffFlash();
			isFlashOn = false;

		} else {
			toggleView.setImageDrawable(getResources().getDrawable(
					R.drawable.torch_on));
			turnOnFlash();
			isFlashOn = true;

		}
	}

}

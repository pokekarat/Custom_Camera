package com.example.parsaniahardik.custom_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.arthenica.mobileffmpeg.FFmpeg;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	public static Camera camera = null;
	final Context context = this;
	int count = 1;
	Observable exportDisposable;
	private SurfaceView SurView;
	private SurfaceHolder camHolder;
	private boolean previewRunning;
	private RelativeLayout CamView;
	private Bitmap inputBMP = null, bmp, bmp1;
	private ImageView camera_image;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CamView = (RelativeLayout) findViewById(R.id.camview);//RELATIVELAYOUT OR
		//ANY LAYOUT OF YOUR XML

		SurView = (SurfaceView) findViewById(R.id.sview);//SURFACEVIEW FOR THE PREVIEW
		//OF THE CAMERA FEED
		camHolder = SurView.getHolder();                           //NEEDED FOR THE PREVIEW
		camHolder.addCallback(this);                               //NEEDED FOR THE PREVIEW
		camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//NEEDED FOR THE PREVIEW
		camera_image = findViewById(R.id.camera_image);//NEEDED FOR THE PREVIEW

		Button btn = findViewById(R.id.button1); //THE BUTTON FOR TAKING PICTURE

		btn.setOnClickListener(new View.OnClickListener() {    //THE BUTTON CODE
			public void onClick(View v) {
				//camera.takePicture(null, null, mPicture);//TAKING THE PICTURE
				new Thread(new Runnable() {
					@Override
					public void run() {

						long tStart = System.nanoTime();
						for(int i=1; i<=125; ++i) {
							takePhoto();
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						long tEnd = System.nanoTime();
						long tDelta = tEnd - tStart;
						double elapsedSeconds = tDelta / 1000000000.0;
						Log.d("Custom_Camera", "Use time = " + String.valueOf(elapsedSeconds));

					}
				}).start();

			}
		});


		Button toMP4 = findViewById(R.id.button2);
		toMP4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");
						dir_image.mkdirs();
						File exportedFile = new File(dir_image, "test2.mp4");
						//Found candidate encoder OMX.google.vp8.encoder
						FFmpeg.execute("-framerate 24 -i /storage/emulated/0/Ultimate/pic%03d.jpeg -b:v 2M " +exportedFile.getPath());
						//FFmpeg.execute("-r 24 -f image2 -s 1280x720 -i /storage/emulated/0/Ultimate/pic%03d.jpeg " +exportedFile.getPath());
					}
				}).start();


			}
		});
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,//NEEDED FOR THE PREVIEW
					   int height) {
		if (previewRunning) {
			camera.stopPreview();
		}

		Camera.Parameters camParams = camera.getParameters();
		List<Camera.Size> sizes = camParams.getSupportedPictureSizes();
		Camera.Size size = sizes.get(0);
		for(int i=0;i<sizes.size();i++)
		{
			if(sizes.get(i).width > size.width)
				size = sizes.get(i);
		}


		camParams.setPictureSize(size.width, size.height);
		Camera.Size size2 = getOptimalPreviewSize(sizes,1280,720); //camParams.getSupportedPreviewSizes().get(0);
		camParams.setPreviewSize(size2.width, size2.height);
		//camParams.setPictureFormat(ImageFormat.JPEG);
		//camParams.set("jpeg-quality", 100);
		camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		camera.setParameters(camParams);
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			previewRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {                  //NEEDED FOR THE PREVIEW
		try {
			camera = Camera.open(0);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {             //NEEDED FOR THE PREVIEW
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public Bitmap takePhoto() {

		// Create a bitmap the size of the scene view.
		//final Bitmap bitmap = Bitmap.createBitmap(SurView.getWidth(), SurView.getHeight(), Bitmap.Config.ARGB_8888);
		final Bitmap bitmap = Bitmap.createBitmap(1280,720, Bitmap.Config.ARGB_8888); //work = 640x360
		// Create a handler thread to offload the processing of the image.
		final HandlerThread handlerThread = new HandlerThread("PixelCopier");
		handlerThread.start();

		// Make the request to copy.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PixelCopy.request(SurView, bitmap, (int copyResult) -> {
				//bitmapToVideoEncoder.queueFrame(bitmap);
				if (copyResult == PixelCopy.SUCCESS) {

					Log.e("test", bitmap.toString() + " count = "+count);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					byte[] bitmapdata = bos.toByteArray();
					ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

					String picId = String.valueOf(count);
					String myfile = "";// "pic" + picId + ".jpeg";
					String type = ".jpeg";
					if(count < 10){
						myfile = "pic00"+picId+type;
					}else if(count >=10 && count <=99){
						myfile = "pic0"+picId+type;
					}else if(count >= 100){
						myfile = "pic"+picId+type;
					}
					++count;

					File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");
					dir_image.mkdirs();

					try {
						File tmpFile = new File(dir_image, myfile);
						FileOutputStream fos = new FileOutputStream(tmpFile);

						byte[] buf = new byte[1024];
						int len;
						while ((len = fis.read(buf)) > 0) {
							fos.write(buf, 0, len);
						}
						fis.close();
						fos.close();

						bmp1 = null;
						camera.startPreview();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}


				} else {

				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					handlerThread.quitSafely();
				}
			}, new Handler(handlerThread.getLooper()));
		}
		return bitmap;
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;
		if (sizes == null) {
			return null;
		}
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;
		for (Camera.Size size : sizes) {
			double ratio = (double) size.height / size.width;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
}



package com.example.parsaniahardik.custom_camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	public static Camera camera = null;
	final Context context = this;
	private SurfaceView SurView;
	private SurfaceHolder camHolder;
	private boolean previewRunning;
	private RelativeLayout CamView;
	private Bitmap inputBMP = null, bmp, bmp1;
	private ImageView camera_image;
	private Camera.PictureCallback mPicture = new Camera.PictureCallback() {   //THIS METHOD AND THE METHOD BELOW
		//CONVERT THE CAPTURED IMAGE IN A JPG FILE AND SAVE IT

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File dir_image2 = new File(Environment.getExternalStorageDirectory() +
				  File.separator + "Ultimate Entity Detector");
			dir_image2.mkdirs();  //AGAIN CHOOSING FOLDER FOR THE PICTURE(WHICH IS LIKE A SURFACEVIEW
			//SCREENSHOT)

			File tmpFile = new File(dir_image2, "TempGhost.jpg"); //MAKING A FILE IN THE PATH
			//dir_image2(SEE RIGHT ABOVE) AND NAMING IT "TempGhost.jpg" OR ANYTHING ELSE
			try { //SAVING
				FileOutputStream fos = new FileOutputStream(tmpFile);
				fos.write(data);
				fos.close();
				//grabImage();
			} catch (FileNotFoundException e) {
				Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			}

			String path = (Environment.getExternalStorageDirectory() +
				  File.separator + "Ultimate EntityDetector" +
				  File.separator + "TempGhost.jpg");//<---

			BitmapFactory.Options options = new BitmapFactory.Options();//<---
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;//<---
			bmp1 = BitmapFactory.decodeFile(path, options);//<---     *********(SEE BELOW)
			//THE LINES ABOVE READ THE FILE WE SAVED BEFORE AND CONVERT IT INTO A BitMap
			camera_image.setImageBitmap(bmp1); //SETTING THE BitMap AS IMAGE IN AN IMAGEVIEW(SOMETHING
			//LIKE A BACKGROUNG FOR THE LAYOUT)

			tmpFile.delete();
			takePhoto();
			//TakeScreenshot();//CALLING THIS METHOD TO TAKE A SCREENSHOT
			//********* THAT LINE MIGHT CAUSE A CRASH ON SOME PHONES (LIKE XPERIA T)<----(SEE HERE)
			//IF THAT HAPPENDS USE THE LINE "bmp1 =decodeFile(tmpFile);" WITH THE METHOD BELOW

		}
	};

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
		camera_image = (ImageView) findViewById(R.id.camera_image);//NEEDED FOR THE PREVIEW

		Button btn = (Button) findViewById(R.id.button1); //THE BUTTON FOR TAKING PICTURE

		btn.setOnClickListener(new View.OnClickListener() {    //THE BUTTON CODE
			public void onClick(View v) {
				camera.takePicture(null, null, mPicture);//TAKING THE PICTURE
				//THE mPicture IS CALLED
				//WHICH IS THE LAST METHOD(SEE BELOW)
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
		Camera.Size size = camParams.getSupportedPreviewSizes().get(0);
		camParams.setPreviewSize(size.width, size.height);
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

	public void TakeScreenshot() {    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
		Random num = new Random();
		int nu = num.nextInt(1000); //PRODUCING A RANDOM NUMBER FOR FILE NAME
		CamView.setDrawingCacheEnabled(true); //CamView OR THE NAME OF YOUR LAYOUR
		CamView.buildDrawingCache(true);
		Bitmap bmp = Bitmap.createBitmap(CamView.getDrawingCache());
		CamView.setDrawingCacheEnabled(false); // clear drawing cache
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

		String picId = String.valueOf(nu);
		String myfile = "Ghost" + picId + ".jpeg";

		File dir_image = new File(Environment.getExternalStorageDirectory() +//<---
			  File.separator + "Ultimate");          //<---
		dir_image.mkdirs();                                                  //<---
		//^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
		//THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"

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
			Toast.makeText(getApplicationContext(),
				  "The file is saved at :SD/Ultimate", Toast.LENGTH_LONG).show();
			bmp1 = null;
			camera_image.setImageBitmap(bmp1); //RESETING THE PREVIEW
			camera.startPreview();             //RESETING THE PREVIEW
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Bitmap decodeFile(File f) {  //FUNCTION BY Arshad Parwez
		Bitmap b = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();
			int IMAGE_MAX_SIZE = 1000;
			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(
					  2,
					  (int) Math.round(Math.log(IMAGE_MAX_SIZE
						    / (double) Math.max(o.outHeight, o.outWidth))
						    / Math.log(0.5)));
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}

	private void takePhoto() {

		// Create a bitmap the size of the scene view.
		final Bitmap bitmap = Bitmap.createBitmap(SurView.getWidth(), SurView.getHeight(),
			  Bitmap.Config.ARGB_8888);

		// Create a handler thread to offload the processing of the image.
		final HandlerThread handlerThread = new HandlerThread("PixelCopier");
		handlerThread.start();

		// Make the request to copy.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PixelCopy.request(SurView, bitmap, (copyResult) -> {
				if (copyResult == PixelCopy.SUCCESS) {
					Log.e("test",bitmap.toString());
					//String name = String.valueOf(System.currentTimeMillis() + ".jpg");

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					byte[] bitmapdata = bos.toByteArray();
					ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

					String picId = "1"; // String.valueOf(nu);
					String myfile = "Ghost" + picId + ".jpeg";

					File dir_image = new File(Environment.getExternalStorageDirectory() +//<---
						  File.separator + "Ultimate");          //<---
					dir_image.mkdirs();                                                  //<---
					//^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
					//THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"

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
						Toast.makeText(getApplicationContext(),
							  "The file is saved at :SD/Ultimate", Toast.LENGTH_LONG).show();
						bmp1 = null;
						camera_image.setImageBitmap(bmp1); //RESETING THE PREVIEW
						camera.startPreview();             //RESETING THE PREVIEW
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
	}
}



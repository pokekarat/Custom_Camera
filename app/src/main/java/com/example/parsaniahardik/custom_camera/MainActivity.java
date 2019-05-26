package com.example.parsaniahardik.custom_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
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

import com.ctech.bitmp4.Encoder;
import com.ctech.bitmp4.MP4Encoder;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

import static android.media.MediaCodec.MetricsConstants.HEIGHT;
import static android.media.MediaCodec.MetricsConstants.WIDTH;
import static android.media.MediaExtractor.MetricsConstants.MIME_TYPE;
import static android.media.MediaPlayer.MetricsConstants.MIME_TYPE_AUDIO;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.jcodec.codecs.s302.S302MDecoder.SAMPLE_RATE;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	public static Camera camera = null;
	final Context context = this;
	int count = 0;
	Observable exportDisposable;
	private SurfaceView SurView;
	private SurfaceHolder camHolder;
	private boolean previewRunning;
	private RelativeLayout CamView;
	private Bitmap inputBMP = null, bmp, bmp1;
	private ImageView camera_image;
	SequenceEncoder encoder;
	private Camera.PictureCallback mPicture = new Camera.PictureCallback() {   //THIS METHOD AND THE METHOD BELOW
		//CONVERT THE CAPTURED IMAGE IN A JPG FILE AND SAVE IT

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			//long tStart = System.currentTimeMillis();
			takePhoto();
			//long tEnd = System.currentTimeMillis();
			//long tDelta = tEnd - tStart;
			//double elapsedSeconds = tDelta / 1000.0;
			//Log.d("Custom_Camera","Use time = " +String.valueOf(elapsedSeconds));

			/*File dir_image2 = new File(Environment.getExternalStorageDirectory() +
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

			tmpFile.delete();*/

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
				//camera.takePicture(null, null, mPicture);//TAKING THE PICTURE
				new Thread(new Runnable() {
					@Override
					public void run() {
						File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");
						dir_image.mkdirs();
						File exportedFile = new File(dir_image, "test.mp4");
						//bitmapToVideoEncoder.startEncoding(SurView.getWidth(), SurView.getHeight(), exportedFile);
						//bitmapToVideoEncoder.startEncoding(800, 600, exportedFile);

						try {
							encoder = SequenceEncoder.create25Fps(exportedFile);

						} catch (IOException e) {
							e.printStackTrace();
						}

						for(int i=0; i<25; ++i) {
							takePhoto();
							try {
								Thread.sleep(40);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						try {
							encoder.finish();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();

			}
		});




		Button toMP4 = findViewById(R.id.button2);
		toMP4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Encoder encoder = new MP4Encoder();
				encoder.setFrameDelay(50);
				File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");
				File exportedFile = new File(dir_image, "export.mp4");
				if (exportedFile.exists()) {
					exportedFile.delete();
				}
				encoder.setOutputFilePath(exportedFile.getPath());
				encoder.setOutputSize(400, 300);
				encoder.startEncode();
				for(int i=0; i<=4; i++) {
					String imageInSD = Environment.getExternalStorageDirectory() +
						  File.separator + "Ultimate/" +
						  "pic_" + i + ".jpeg";
					Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
					int bc = bitmap.getByteCount();
					encoder.addFrame(bitmap);
				}
				encoder.stopEncode();*/


				/*BitmapToVideoEncoder bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
					@Override
					public void onEncodingComplete(File outputFile) {
						//Toast.makeText(this,  "Encoding complete!", Toast.LENGTH_LONG);
						Toast.makeText(getApplicationContext(),"complete",Toast.LENGTH_LONG).show();
					}
				});

				File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");
				File exportedFile = new File(dir_image, "export.mp4");
				if (exportedFile.exists()) {
					exportedFile.delete();
				}
				bitmapToVideoEncoder.startEncoding(400,300, exportedFile);
				for(int i=0; i<=4; i++) {
					String imageInSD = Environment.getExternalStorageDirectory() +
						  File.separator + "Ultimate/" +
						  "pic_" + i + ".jpeg";
					Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
					int bc = bitmap.getByteCount();
					bitmapToVideoEncoder.queueFrame(bitmap);
				}
				bitmapToVideoEncoder.stopEncoding();*/


			}
		});


	}

	BitmapToVideoEncoder bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
		@Override
		public void onEncodingComplete(File outputFile) {
			//Toast.makeText(this,  "Encoding complete!", Toast.LENGTH_LONG);
			//Toast.makeText(getApplicationContext(),"complete",Toast.LENGTH_LONG).show();
		}
	});

	private Bitmap createBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		v.draw(c);
		return bitmap;
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
		camParams.setPictureFormat(ImageFormat.JPEG);
		camParams.set("jpeg-quality", 100);
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

	//This function return black screen
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

	public Bitmap takePhoto() {


		// Create a bitmap the size of the scene view.
		//final Bitmap bitmap = Bitmap.createBitmap(SurView.getWidth(), SurView.getHeight(), Bitmap.Config.ARGB_8888);
		final Bitmap bitmap = Bitmap.createBitmap(128,1024, Bitmap.Config.ARGB_8888);
		// Create a handler thread to offload the processing of the image.
		final HandlerThread handlerThread = new HandlerThread("PixelCopier");
		handlerThread.start();

		// Make the request to copy.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PixelCopy.request(SurView, bitmap, (int copyResult) -> {
				//bitmapToVideoEncoder.queueFrame(bitmap);
				if (copyResult == PixelCopy.SUCCESS) {
					Log.e("test", bitmap.toString());
					/*int w = bitmap.getWidth();
					int h = bitmap.getHeight();
					byte[][] pixels = new byte[w][h];
					for(int i=0; i<w; i++){
						for(int j=0; j<h; j++){
							pixels[i][j] = (byte)bitmap.getPixel(i,j);
						}
					}
					Picture picture = Picture.createPicture(bitmap.getWidth(),bitmap.getHeight(),pixels,ColorSpace.RGB);
					try {
						encoder.encodeNativeFrame(picture);
					} catch (IOException e) {
						e.printStackTrace();
					}*/



					//String name = String.valueOf(System.currentTimeMillis() + ".jpg");

					long tStart = System.nanoTime();

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					byte[] bitmapdata = bos.toByteArray();
					ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

					String picId = String.valueOf(count);
					++count;
					String myfile = "pic_" + picId + ".jpeg";
					/*if(count == 125){
						//bitmapToVideoEncoder.stopEncoding();
						try {
							encoder.finish();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}*/

					File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ultimate");          //<---
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
					long tEnd = System.nanoTime();
					long tDelta = tEnd - tStart;
					double elapsedSeconds = tDelta / 1000000000.0;
					Log.d("Custom_Camera", "Use time = " + String.valueOf(elapsedSeconds));

				} else {

				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					handlerThread.quitSafely();
				}
			}, new Handler(handlerThread.getLooper()));
		}
		return bitmap;
	}


}



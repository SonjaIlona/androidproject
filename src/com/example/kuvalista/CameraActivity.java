package com.example.kuvalista;


/** Tässä luokassa otetaan kuva, siis kutsutaan varsinaista kameraa ja tallennetaan.
 *
 * kuva sd-kortille. Myöskin luodaan jokaisen kuvan otto tilanteessa uusi GPS-tiedosto, johon 
 * tallenetaan lat ja lon. Nämä voidaan poistaa, kuten myös kuvat Tyhjää välimuisti-painikkeella.
 * Lisäksi on erilliseen tiedostoon aina viimeinen lat ja lon tieto, jota voidaan verrata
 * palvelulta saatuun tietoon ja peruuttaa kuvan otto, jos lat ja lon eivät ole päivittyneet.
 * 
 * Tiedosotn käsittely olisi hyvä olla omassa luokassaan, koska tämä luokka alkaa näyttämään jo liian isolta.
 * 
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.kuvalista.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity {

	// Activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;	
	private static final String IMAGE_DIRECTORY_NAME = "Kamera";
	private static final String LATITJALONIT_DIRECTORY_NAME = "LL";		
	private Uri fileUri; // file url to store image/video	
	private Button btnCapturePicture;
	private GPSTracker gps;	
	private String latitjalonit;
	private File chDir;	
	private View btntyhjays;
	private File mediaStorageDir;
	private File mediaStorageDir2;
	private File ll;
	private ImageView imgPreview;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		chDir = getBaseContext().getCacheDir();			
		
		
		imgPreview = (ImageView) findViewById(R.id.imgPreview);		
		setContentView(R.layout.activity_camera);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		Button btnReturn1 = (Button) findViewById(R.id.btnback);
		btntyhjays = (Button) findViewById(R.id.btntyhjays);
		btnReturn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				paluu();
			}
		});

		/**
		 * Painikkeen toiminto kuvan ottamiseen.
		 */
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// otetaan kuva
				captureImage();
			}
		});

		 /**
		  *		  
		  * Välimuistin tyhjäys kuvineen ja paikkatietoineen. 
		  * 
		  */		
		btntyhjays.setOnClickListener(new View.OnClickListener() {
			
			List<Boolean> bool = new ArrayList<Boolean>();

			@Override
			public void onClick(View arg0) {
				
				 mediaStorageDir = new File(
			                Environment
			                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			                IMAGE_DIRECTORY_NAME);
				 
				 mediaStorageDir2 = new File(
			                Environment
			                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			                        LATITJALONIT_DIRECTORY_NAME);
				 
				File[] temp;
				List<File> fileList = new ArrayList<File>();
				List<File> listakuvat = new ArrayList<File>();
				List<File> listagps = new ArrayList<File>();
				
				//Hakee myös kansiot, joten kikkaillaan ne pois

				temp = chDir.listFiles();
				
				for(int i=0; i<temp.length;i++)
					if(! temp[i].isDirectory())
						fileList.add(temp[i]);
				
				//poistetaan filet sijoitetaan tulos (true tai false)

				if (fileList.size()>0){
					for (File file: fileList) {						
						bool.add(file.delete());
					}
				}	
				
				
				if(mediaStorageDir2.exists())
				temp = mediaStorageDir2.listFiles();
				if(temp!=null){
				for (int i = 0; i < temp.length; i++)
					listagps.add(temp[i]);
				
					for (File file: listagps) {
						bool.add(file.delete());
					}
					
				}
				
				
				
				
				//Listataan tiedostoon, jos viite polkuun on olemassa ja poistetaan filet sijoitetaan tulos (true tai false)
				//Listataan tiedostoon, jos viite polkuun on olemassa ja poistetaan filet sijoitetaan tulos (true tai false)
				//mediaStorageDir = new File(chDir.getPath()+"//Kamera");
				if(mediaStorageDir.exists())
				temp = mediaStorageDir.listFiles();
				if(temp!=null){
				for (int i = 0; i < temp.length; i++)
					listakuvat.add(temp[i]);
				
					for (File file: listakuvat) {
						bool.add(file.delete());
					}
					
				}
				if(bool.contains(false))
					Toast.makeText(getBaseContext(),
							"Tiedoston poistossa ongelmia",
							Toast.LENGTH_LONG).show();
				
				else
					Toast.makeText(getBaseContext(),
							"Tiedostot poistettu onnistuneesti",
							Toast.LENGTH_LONG).show();
			
				paluu();

			}
		});

		// Tarkistetaan kameran saatavuus.
		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(), "Sorry! Ei kameratukea",
					Toast.LENGTH_LONG).show();
			// Sulkee ohjelman, jos ei ole kameraa laitteessa
			finish();
		}
	}

	/**
         *	
         * Tarkistetaan kameran saatavuus.
	 * 
        */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// Kamera on
			return true;
		} else {
			// Ei ole kameraa
			return false;
		}
	}

	/**
	 * Käynnistetään kuvan otto.  
	 */
	private void captureImage() {
		gps = new GPSTracker(CameraActivity.this);
		// Tarkistetaan onko GPS saatavilla
		if (gps.canGetLocation()) {
			//Haetaan palvelulta latit ja lonit
			String strLine = null;
			double latitude = 0;
			double longitude =0;
			StringBuffer bufferi = new StringBuffer();
			
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();			
			bufferi.append(Double.toString(longitude));
			bufferi.append("-");
			bufferi.append(Double.toString(latitude));
			latitjalonit=(bufferi.toString());
			bufferi = null;
			
			// Tämä tiedosto tehdään sitä varten, jotta voidaan verrata GPS-tietoa ja ei oteta uutta kuvaa ennen kuin uusi GPS-tieto
			// päivittynyt.
			
			
			 // Muistikortille
	         mediaStorageDir2 = new File(
	                Environment
	                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
	                        LATITJALONIT_DIRECTORY_NAME);

			// Luodaan latitjalonit-kansio(GPS kansio), jos ei vielä olemassa
			if (!mediaStorageDir2.exists()) {			
				if (mediaStorageDir2.mkdir()) {
					Log.d(LATITJALONIT_DIRECTORY_NAME, "LUONTI EI ONNISUNUT "
							+ LATITJALONIT_DIRECTORY_NAME + " directory");
				}				
				
			}
			
			
			//Ei ole kuin 1 tiedosto, niin voidaan käyttää 0 indeksiä
			if(mediaStorageDir2.listFiles().length <1) {			
				
				ll = new File(mediaStorageDir2.getPath() + File.separator
						+"ll.txt");
				ll.deleteOnExit();		
				
			}	
			            
			//Alustetaan lukijat ja luetaan viimeisin GPS-tieto vertailua varten	
			//*********************************************************************************************************//
			try {
				//Tarkistus ei ole välttämätön, mutta on tehty tähän
				if(ll==null)
				 ll= new File(mediaStorageDir2.getPath() + File.separator+"ll.txt");
				
				FileReader fReader = new FileReader(ll);
				BufferedReader bReader = new BufferedReader(fReader);
				StringBuilder stringBuilder =  new StringBuilder();
				String receiveString;

				//** Luetaan rivi *//*
				while ( (receiveString = bReader.readLine()) != null ) {	            		
					stringBuilder.append(receiveString);
            	}
				strLine = stringBuilder.toString();
				bReader.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(strLine != null){
			   StringBuilder builder = new StringBuilder();
			   // Jostain syystä joka toinen tyhjää ja siksi on tehty tämä	
			   for(int i=1;i<strLine.length();i++){
				if(i % 2!=0)
					builder.append(strLine.charAt(i));				
			   }			
			   strLine = builder.toString();	
			   
			}
		 //*********************************************************************************************//
			
			//Estetään samoilla GPS-tiedoilla ottamasta toista kuvaa
			if(strLine != null){
				if (strLine.equals(latitjalonit)) {
					Toast.makeText(getBaseContext(),
						"Ei oteta kuvaa, koska GPS-tiedot samat",
						Toast.LENGTH_LONG).show();
				}
			}
			//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::://
			 //Jos ei ole samat yritetään kirjoittaa viimeisiä GPS-tietoja vertailuun seuraava kuvaa otettaessa 			
			else{					
				try {					
					
					DataOutputStream out = new DataOutputStream(
							new FileOutputStream(ll.toString()));
					out.writeChars(latitjalonit);
					out.close();
					} catch (IOException e) {
						Log.i("Data output", "I/O Error");	
					}			
				
				// Luodaan uusi GPS-text-file
				 String timeStamp = new SimpleDateFormat("dd.MM.yy",
						Locale.getDefault()).format(new Date());
				 File temp;
				
					temp = new File(chDir.getPath() + File.separator
							 + timeStamp + ".txt");
					temp.deleteOnExit();				 
				// Tässä kameraa varten try-lohko
				try {	
					DataOutputStream out = new DataOutputStream(
							new FileOutputStream(temp.toString()));
					out.writeChars(latitjalonit);	
					out.close();
				} catch (IOException e) {
					Log.i("Data output", "I/O Error");
				  }
				Toast.makeText(CameraActivity.this,
						"GPS-TIEDOT ladattu temppiin " + temp.getPath(),
						Toast.LENGTH_LONG).show();
				
	
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				// Käynnistetään kuvanotto ja odotetaan vastausta
				startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);			
				
			}
	
		}
		//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::://
				
		if (! gps.canGetLocation()) {
			// Ei saada paikkatietoja
			// GPS tai verkko ei saatavilla
			
			Toast.makeText(CameraActivity.this,
					"GPS-tietoja ei saatu. tarkista Gps-asetukset"
							+ latitjalonit, Toast.LENGTH_LONG).show();
			gps.showSettingsAlert();
		}
	//	paluu();
	}

	/**	
         * 
	 * Tässä laitetaan talteen urli, koska se nollautuu aina, kun palataan
	 * kamera-ohelmasta. Tätä ei tarvita nyt, kun esikatselu ei ole käytössä
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);		
		outState.putParcelable("file_uri", fileUri);
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Tässä se saadaan takaisin
		fileUri = savedInstanceState.getParcelable("file_uri");
		
	}

	/**
	 * Kutsutaan kameraan sulkeutumisen jälkeen
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Jos alla olevat ok
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// kuvanotto onnistui
				// näytetään esikatselussa
				
			} else if (resultCode == RESULT_CANCELED) {
				// käyttäjä perunut kuvan oton
				Toast.makeText(getApplicationContext(), "Kuvanotto peruutettu",
						Toast.LENGTH_SHORT).show();
			} else {
				// Kuvanotto ei onnistunut
				Toast.makeText(getApplicationContext(),
						"Sorry! Kuvan otto ei onnistunut", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	/**
          *	
          * Haetaan kuva esikatseluun ja säädetään koko pienemmäksi.
	  *
          */
	
	private void previewCapturedImage() {
        try {
                        
            imgPreview.setVisibility(View.VISIBLE);
 
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
 
            //Kuvakoon muuttaminen
            options.inSampleSize = 8;
 
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
 
            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

	/**
	 * ------------ Apumetodeja kuvan tiedosotn käsittelyyn ----------------------
	 * */

	/**
          *	 
          * Luodaan uri kuvan tallentamista varten.
          *
          */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/**
	 * returning image
	 */
	private File getOutputMediaFile(int type) {
		
		 // Muistikortille
         mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

		// Luodaan kuvat-kansio, jos ei olemassa
		if (!mediaStorageDir.exists()) {
			if (mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "LUONTI EI ONNISUNUT "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		
		// Luodaan kuvatiedoston nimi varustettuna aikaleimalla
		String timeStamp = new SimpleDateFormat("dd.MM.yy",
				Locale.getDefault()).format(new Date());
		File mediaFile;

		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "KUVA" + "_" + timeStamp + ".jpg");

		} else {
			return null;
		}

		return mediaFile;
	}
	/**
	 * Laitettu metodin sisään.
	 */
	private void paluu() {

		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);

	}

}

package com.example.kuvalista;
import com.example.kuvalista.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
 
public class SplashScreen extends Activity {
 
    // Asetettu aika, jolloin alustuskuva näkyy
    private static int SPLASH_TIME_OUT = 3000;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        new Handler().postDelayed(new Runnable() {
 
           
 
            @Override
            public void run() {
                // Tämä suoritetaan, kun asetettu aika ylitetty ja pääluokka käynnistetään
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
 
                // sulkee tämän aktiviteetin
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// N‰ytön kierron tunnistus ei toimi
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}
    
    
    
    
 
}
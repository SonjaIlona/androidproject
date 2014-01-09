package com.example.kuvalista;
import com.example.kuvalista.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
 
public class SplashScreen extends Activity {
 
    // Asetettu aika, jolloin alustuskuva n�kyy
    private static int SPLASH_TIME_OUT = 3000;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        new Handler().postDelayed(new Runnable() {
 
           
 
            @Override
            public void run() {
                // T�m� suoritetaan, kun asetettu aika ylitetty ja p��luokka k�ynnistet��n
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
 
                // sulkee t�m�n aktiviteetin
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// N�yt�n kierron tunnistus ei toimi
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}
    
    
    
    
 
}
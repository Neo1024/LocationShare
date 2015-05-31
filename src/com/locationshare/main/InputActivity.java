package com.locationshare.main;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.locationshare.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InputActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);
		
		Button buttonConfirm = (Button)findViewById(R.id.confirmButton);
		
		buttonConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText editOwnNumber = (EditText)findViewById(R.id.editOwnPhoneNumber);
				EditText editOtherNumber = (EditText)findViewById(R.id.editOtherPhoneNumber);
				
				Intent intent = new Intent(InputActivity.this, MainActivity.class);
				intent.putExtra("ownPhoneNumber", editOwnNumber.getText().toString());
				intent.putExtra("otherPhoneNumber", editOtherNumber.getText().toString());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input, menu);
		return true;
	}

}

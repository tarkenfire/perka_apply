package com.hinodesoftworks.perkaapp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class MainActivity extends Activity {
	
	protected ArrayList<String> items;
	protected ArrayAdapter<String> adapter;
	
	//ui handles
	ListView projectList;
	Button addButton;
	TextView pathDisplay;
	
	EditText firstName;
	EditText lastName;
	EditText email;
	EditText position;
	EditText explain;
	EditText source;
	
	protected enum InputMode{
		MODE_RESUME, MODE_PROJECT
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ApplicationFragment()).commit();
			
			items = new ArrayList<String>();
		} 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu;
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_send) {
			
			//due to internal use nature of the app, it is left to user to
			//ensure correct data is entered and required fields are submitted.
			
			JSONObject objectToSend = new JSONObject();
			
			
			try {
				objectToSend.put("first_name", getStripedText(firstName));
				objectToSend.put("last_name", getStripedText(lastName));
				objectToSend.put("email", getStripedText(email));
				objectToSend.put("position_id", getStripedText(position));
				objectToSend.put("explanation", getStripedText(explain));
				
				JSONArray arrayToPlace = new JSONArray(items);
				objectToSend.put("projects", arrayToPlace);
				
				objectToSend.put("source", getStripedText(source));
				
				//convert file to byte array then to base 64 string
				File file = new File(pathDisplay.getText().toString());
				byte[] fileBytes = FileUtils.readFileToByteArray(file);
				String encodedFile = Base64.encodeToString(fileBytes, Base64.DEFAULT);
				
				objectToSend.put("resume", encodedFile);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(this, "JSON error in data.", Toast.LENGTH_SHORT).show();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(this, "File Path error", Toast.LENGTH_SHORT).show();
				return false;
			}
			
			sendJSONObjectPOST(objectToSend);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String getStripedText(EditText et){
		return et.getText().toString().trim();
	}
	
	public void onClick(View v){
		switch (v.getId())
		{
		case R.id.add_new_project_button:
			createInputDialog("Add Project", "Enter string detail for project to add to list.", InputMode.MODE_PROJECT);
			break;
		case R.id.resume_selector:
			createInputDialog("Add Resume File", "Enter path of resume pdf file to upload", InputMode.MODE_RESUME);
			break;
		}
	}
	
	
	public void createInputDialog(String title, String message, final InputMode currentMode){
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);
		alert.setMessage(message);

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString().trim();
		  
			  switch (currentMode)
			  {
				case MODE_PROJECT:
					onInputResult(value);
					break;
				case MODE_RESUME:
					onFileURLSelected(value);
					break;
				  
			  }
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Cancelled.
		  }
		});

		alert.show();
		
	}
	
	public void onInputResult(String result) {
		items.add(result);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		projectList.setAdapter(adapter);
	}
	
	public void onFileURLSelected(String fileLocation){
		pathDisplay.setText(fileLocation);	
	}
	
	public void onFragmentLoaded() {
		projectList = (ListView) findViewById(R.id.projects_list);
		addButton = (Button) findViewById(R.id.add_new_project_button);
		pathDisplay= (TextView) findViewById(R.id.resume_display);
		
		
		firstName = (EditText) findViewById(R.id.first_name_field);
		lastName = (EditText) findViewById(R.id.last_name_field);
		email = (EditText) findViewById(R.id.email_field);
		position = (EditText) findViewById(R.id.position_id_field);
		explain = (EditText) findViewById(R.id.explain_field);
		source = (EditText) findViewById(R.id.source_field);
		
		addButton.setEnabled(true);
	}
	
	public void sendJSONObjectPOST(JSONObject jsonObj){
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 100000);
		HttpConnectionParams.setSoTimeout(httpParams, 100000);
		
		final HttpClient client = new DefaultHttpClient(httpParams);
		final HttpPost request = new HttpPost("http://requestb.in/1hfou8m1");
		request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        longInfo(jsonObj.toString());
		
		try {
			request.setEntity(new StringEntity(jsonObj.toString()));
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						HttpResponse response = client.execute(request);
						
						String responseBody = EntityUtils.toString(response.getEntity());
						longInfo(responseBody);
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}	
			}).start();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Fragment containing an application form for Perka.
	 */
	public static class ApplicationFragment extends Fragment {

		public ApplicationFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			
			MainActivity parent = (MainActivity) this.getActivity();
			parent.onFragmentLoaded();
		}
	}
	
	public static void longInfo(String str) 
	{
	    if(str.length() > 4000)
	    {
	        Log.i("logged", str.substring(0, 4000));
	        longInfo(str.substring(4000));
	    }
	    else
	    {
	    	Log.i("logged", str);
	    }
	}

}

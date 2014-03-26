package com.hinodesoftworks.perkaapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class MainActivity extends Activity {
	
	protected ArrayList<String> items;
	protected ArrayAdapter<String> adapter;
	
	//ui handles
	ListView projectList;
	Button addButton;
	

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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.add_new_project_button:
			createInputDialog("Add Project", "Enter string detail for project to add to list.");
			break;
		case R.id.resume_selector:
			break;
		}
	}
	
	
	public void createInputDialog(String title, String message)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);
		alert.setMessage(message);

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString().trim();
		  onInputResult(value);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		
	}
	
	public void onInputResult(String result)
	{
		items.add(result);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		
		Log.i("ADAPTER NULL CHECK", adapter == null ? "Is null" : "Is Not Null");
		Log.i("LIST NULL CHECK", projectList == null ? "Is null" : "Is Not Null");
		projectList.setAdapter(adapter);
	}
	
	public void onFragmentLoaded()
	{
		projectList = (ListView) findViewById(R.id.projects_list);
		addButton = (Button) findViewById(R.id.add_new_project_button);
		addButton.setEnabled(true);
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

}

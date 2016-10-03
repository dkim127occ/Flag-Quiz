package edu.orangecoastcollege.cs273.dkim127.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class QuizActivity extends AppCompatActivity {

	// keys for reading data from SharedPreferences
	public static final String CHOICES = "pref_numberOfChoices";
	public static final String REGIONS = "pref_regionsToInclude";
	
	private boolean phoneDevice = true;
	private boolean preferencesChanged = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the app's SharedPreferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// register listener for SharedPreferences changes
		PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferencesChangedListener);
		
		// determines screen size
		int screenSize = getResources().getConfiguration().screenLayout &
			Configuration.SCREENLAYOUT_SIZE_MASK;
		
		// if device is a tablet, set phoneDevice to false
		if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
			screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
		{
			phoneDevice = false;
		}
		
		// if running on phone-sized device, allow only portrait orientation
		if (phoneDevice)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
    }
	
	/**
		* onStart is called after onCreate completes execution.
		* This method will update the number of guess rows to display
		* and the regions to choose flags from, then resets the quiz
		* with the new preferences.
		*/
	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (preferencesChanged)
		{
			// now that the default preferences have been set,
			// initialize QuizActivityFragment and start the quiz
			QuizActivityFragment quizFragment = (QuizActivityFragment) 
				getSupportFragmentManager().findFragmentById(R.id.quizFragment);
			quizFragment.updateGuessRows(
				PreferenceManager.getDefaultSharedPreferences(this));
			quizFragment.updateRegions(
				PreferenceManager.getDefaultSharedPreferences(this));
			quizFragment.resetQuiz();
			preferencesChanged = false;
		}
	}

	/**
		* Shows the settings menu if the app is running on a phone or a portrait-oriented
		* tablet only. (Large screen sizes include the settings fragment in the layout)
		* @param menu The Settings menu
		* @return True if the settings menu was inflated, false otherwise 
		*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the device's current orientation
		int orientation = getResources().getConfiguration().orientation;
		
		// display the app's menu only in portrait orientation 
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			getMenuInflater().inflate(R.menu.menu_quiz, menu);
			return true;
		}
		else
		{
			return false;
		}
    }

	/**
		* Displays the SettingsActivity when running on a phone or portrait-oriented
		* tablet. Starts the activity by use of an Intent (no data passed because the
		* shared preferences, preferences.xml, has all data necessary)
		* @param item The menu item
		* @return True if an option item was selected
		*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
		startActivity(preferencesIntent);
		return super.onOptionsItemSelected(item);
    }

	/**
	 * Listener to handle changes in the app's shared preferences (preferences.xml)
	 * If either the guess options or regions are changed, the quiz will restart with the
	 * new settings.
	 */
	private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangedListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;

                    QuizActivityFragment quizFragment = (QuizActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if (key.equals(CHOICES)) // # of choices to display changed
                    {
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.resetQuiz();
                    }
                    else if (key.equals(REGIONS)) // regions to include changed
                    {
                        Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null && regions.size() > 0)
                        {
                            quizFragment.updateRegions(sharedPreferences);
                            quizFragment.resetQuiz();
                        }
                        else
                        {
                            // must select one region -- set North America as default
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(QuizActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(QuizActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
                }
            };
}

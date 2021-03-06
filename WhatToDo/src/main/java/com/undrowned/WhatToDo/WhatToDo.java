package com.undrowned.WhatToDo;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.TasksRequestInitializer;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Sole activity for WhatToDo app.
 *
 * @author Alex Davis
 */
public final class WhatToDo extends Activity {

    // Google Tasks API access
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    GoogleAccountCredential credential;
    com.google.api.services.tasks.Tasks service;

    // UI and resources
    private TextView taskTextView;
    private Spinner tasklistSpinner;
    private Spinner moodSpinner;
    private Resources res;

    // Tasks and related data
    Task task;
    TaskList tasklist;
    List<String> tasklistnames = new ArrayList<String>();
    ArrayAdapter<String> listsAdapter;
    String tasklistSpinnerSelectedText;
    Random rand = new Random();
    String[] moods;
    int[] moodColors = new int[] {
            android.R.color.holo_blue_light, // ordinary
            android.R.color.holo_green_light, // ambitious
            android.R.color.holo_purple, // guilty
            android.R.color.holo_orange_light, // sluggish
            android.R.color.holo_red_light // angry
    };

    // Logging and accounts
    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    static final String TAG = "WhatToDo";
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    // Bookkeeping
    int numAsyncTasks;
    Boolean init = Boolean.TRUE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        // view and menu
        setContentView(R.layout.main);
        taskTextView = (TextView) findViewById(R.id.text_task);

        // Set up spinners
        tasklistSpinner = (Spinner) findViewById(R.id.spinner_lists);
        // TODO layout probably isn't working b/c there is no such constructor signature
        listsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, tasklistnames);
        listsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        tasklistSpinner.setAdapter(listsAdapter);
        tasklistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                tasklistSpinnerSelectedText = listsAdapter.getItem(position);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        // TODO use saved value
        tasklistSpinnerSelectedText = "Todo";

        moodSpinner = (Spinner) findViewById(R.id.spinner_moods);
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String mood = getResources().getStringArray(R.array.spinner_moods)[position];
                if (!init) {
                    if (position != 5) {
                        setMood(position);
                    } else {
                        setMood(rand.nextInt(moodColors.length));
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // Google Accounts
        credential =
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        // Tasks client
        service =
                new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential)
                        .setApplicationName("WhatToDo/1.1")
                        .setTasksRequestInitializer(new TasksRequestInitializer(getString(R.string.google_api_key)))
                        .build();

        res = getResources();
        moods = res.getStringArray(R.array.spinner_moods);
    }

    /**
     * Changes end color for background gradient of given view.
     *
     * @param viewID
     * @param moodColor new gradient end color
     */
    private void setMoodGradient(int viewID, int moodColor) {
        ((GradientDrawable)findViewById(viewID).getBackground()).setColors(
                new int[]{res.getColor(R.color.gradient_light), moodColor});
    }

    /**
     * Changes solid background color of given view.
     *
     * @param viewID
     * @param moodColor
     */
    private void setMoodBackground(int viewID, int moodColor) {
        findViewById(viewID).setBackgroundColor(moodColor);
    }

    /**
     * Sets text of given view.
     *
     * @param viewID
     * @param text
     */
    private void setMoodText(int viewID, String text) {
        ((TextView) findViewById(viewID)).setText(text);
    }

    /**
     * Performs all changes to implement a given mood, as indexed on WhatToDo.moodColors and string_array resource spinner_moods
     *
     * @param moodIndex
     */
    private void setMood(int moodIndex) {
        // TODO rather than these hardcoded series, subclass the relevant views (and add custom fields where necessary)

        // set colors
        int moodColor = res.getColor(moodColors[moodIndex]);
        setMoodGradient(R.id.text_dividerV1, moodColor);
        setMoodGradient(R.id.text_dividerV2, moodColor);
        setMoodGradient(R.id.image_dividerH2, moodColor);
        setMoodGradient(R.id.text_dividerH3, moodColor);
        setMoodBackground(R.id.text_colon1, moodColor);
        setMoodBackground(R.id.text_colon2, moodColor);
        setMoodBackground(R.id.text_colon3, moodColor);
        setMoodBackground(R.id.text_colon4, moodColor);
        setMoodBackground(R.id.text_colon5, moodColor);
//        setMoodBackground(R.id.text_colon6, moodColor);
        setMoodBackground(R.id.title_bar, moodColor);

        // set texts
        int moodStringsArrayID = res.getIdentifier("mood_" + moods[moodIndex], "array", getPackageName());
        String[] moodStrings = res.getStringArray(moodStringsArrayID);
        setMoodText(R.id.text_intro, moodStrings[0]);
        setMoodText(R.id.text_and, moodStrings[1]);
        setMoodText(R.id.button_did, moodStrings[2]);
        // TODO get button to shrink to new text when text wraps. (Apparently the white trailing space on the first line is counted as content.)
        //findViewById(R.id.button_did).getLayoutParams().width = GridLayout.LayoutParams.WRAP_CONTENT;

        setMoodText(R.id.text_didnt, moodStrings[3]);
        setMoodText(R.id.text_else, moodStrings[4]);
        setMoodText(R.id.button_pick, moodStrings[5]);
        setMoodText(R.id.text_newlist, moodStrings[6]);
        setMoodText(R.id.button_now, moodStrings[7]);
        setMoodText(R.id.text_notnow, moodStrings[8]);
        setMoodText(R.id.button_bye, moodStrings[9]);
    }

    /**
     * On button click, mark current task as done and select another from the current task list.
     *
     * @param view
     */
    public void onButtonClickDid(View view) {
        // TODO: rewrite in style of AsyncLoadTasks
        new AsyncTask<Void, Void, Task>() {

            @Override
            protected com.google.api.services.tasks.model.Task doInBackground(Void... params) {
                try {
                    if (task != null) {
                        DateTime date = new DateTime(System.currentTimeMillis(), 0);

                        task.setStatus("completed");
                        return service.tasks().update(tasklist.getId(), task.getId(), task).execute();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(com.google.api.services.tasks.model.Task result) {
                // TODO: handle task execute result
            };

        }.execute();
        AsyncLoadTasks.run(this);
    }

    /**
     * On button click, choose a new current task.
     *
     * @param view
     */
    public void onButtonClickElse(View view)
    {
        AsyncLoadTasks.run(this);
    }

    /**
     * On button click, "exit" the app.
     * (It's not good practice in Android to actually exit; run Home activity instead.)
     *
     * @param view
     */
    public void onButtonClickBye(View view)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

// re-used open source code from here
/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.

    MODIFIED for com.undrowned.WhatToDo 2013 Alex Davis
 */

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog =
                        GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, WhatToDo.this,
                                REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    void refreshView() {
        String taskTitle;
        if (task != null) {
            taskTitle = task.getTitle();
        } else {
            taskTitle = "Create a task list titled 'Todo'.";
        }
        taskTextView.setText(taskTitle);

        listsAdapter.notifyDataSetChanged();
        tasklistSpinner.setSelection(tasklistnames.indexOf(tasklistSpinnerSelectedText));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGooglePlayServicesAvailable()) {
            haveGooglePlayServices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    AsyncLoadTasks.run(this);
                } else {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        AsyncLoadTasks.run(this);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                AsyncLoadTasks.run(this);
                break;
            case R.id.menu_accounts:
                chooseAccount();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            AsyncLoadTasks.run(this);
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }
}

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
 */
package com.undrowned.WhatToDo;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample activity for Google Tasks API v1. It demonstrates how to use authorization to list tasks
 * with the user's permission.
 *
 * @author Yaniv Inbar
 */
public final class WhatToDo extends Activity {

    /**
     * Logging level for HTTP requests/responses.
     *
     * <p>
     * To turn on, set to {@link Level#CONFIG} or {@link Level#ALL} and run this from command line:
     * </p>
     *
     * <pre>
     adb shell setprop log.tag.HttpTransport DEBUG
     * </pre>
     */
    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    static final String TAG = "WhatToDo";
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    GoogleAccountCredential credential;

    com.google.api.services.tasks.Tasks service;

    private TextView taskTextView;
    private Spinner tasklistSpinner;
    private Spinner moodSpinner;
    Button button_pick;

    Task task;
    TaskList tasklist;
    List<String> tasklistnames = new ArrayList<String>();
    ArrayAdapter<String> listsAdapter;
    String tasklistSpinnerSelectedText;
    String mood;

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
        tasklistSpinnerSelectedText = "Todo";

        String[] moods = getResources().getStringArray(R.array.spinner_moods);
        moodSpinner = (Spinner) findViewById(R.id.spinner_moods);
        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String mood = getResources().getStringArray(R.array.spinner_moods)[position];
                // TODO something about changed mood
                if (!init) {
                    // TODO use color resources
                    // TODO iterate through all dividers and action bar
                    ((GradientDrawable)findViewById(R.id.image_dividerH2).getBackground()).setColors(new int[]{ Color.parseColor("#F4F4F4"), getResources().getColor(android.R.color.holo_orange_light) });
//                    colorBar.setBackgroundResource(R.drawable.gradient_red);
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
                        .setTasksRequestInitializer(new TasksRequestInitializer("AIzaSyCmE_OqAeCCws5bErfP2c4WeW5B5KtuET0"))
                        .build();

        button_pick = (Button) findViewById(R.id.button_pick);

//    try {
//           tasks = service.tasks().list("@default").execute().getItems();
//       } catch (IOException e) {
////           Log.e(ERROR_TAG, "fail to load tasks", e);
//       }
    }

    public void onButtonClickDid(View view) throws IOException {
        // TODO: integrate with AsyncLoadTasks
        new AsyncTask<Void, Void, Task>() {

            @Override
            protected com.google.api.services.tasks.model.Task doInBackground(Void... params) {
                try {
                    if (task != null) {
                        DateTime date = new DateTime(System.currentTimeMillis(), 0);

                        task.setStatus("completed");
//                        task.setDue(date);
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

    public void onButtonClickElse(View view)
    {
        AsyncLoadTasks.run(this);
    }

    public void onButtonClickBye(View view)
    {
        // Not good Android practice to actually exit; run Home activity instead
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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

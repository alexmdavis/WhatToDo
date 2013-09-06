/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Asynchronously load the tasks.
 *
 * @author Yaniv Inbar
 */
class AsyncLoadTasks extends CommonAsyncTask {

    AsyncLoadTasks(WhatToDo whatToDo) {
        super(whatToDo);
    }

    @Override
    protected void doInBackground() throws IOException {
        Task result;
        List<Task> tasks = new ArrayList<Task>();
        List<TaskList> tasklists =
                client.tasklists().list().execute().getItems();
        if (tasklists != null) {
            activity.tasklistnames.clear();
            for (TaskList tasklist : tasklists) {
                String tasklisttitle = tasklist.getTitle();

                // assemble tasklist titles
                activity.tasklistnames.add(tasklisttitle);

                // get tasks
                if (tasklisttitle.equals(activity.tasklistSpinnerSelectedText)) {
                    activity.tasklist = tasklist;
                    // TODO perhaps limit task fields retrieved
//                    tasks = client.tasks().list(tasklist.getId()).setFields("items").execute().getItems();
                    // Remove completed tasks. Would prefer to filter by lambda, but no Java 8 yet
                    for (Task t : client.tasks().list(tasklist.getId()).setFields("items").execute().getItems())
                        if (!t.getStatus().equals("completed")) tasks.add(t);
//                    tasks = client.tasks().list(tasklist.getId()).setFields("items/title").execute().getItems();
                }
            }

        }
//    tasks = client.tasks().list("@default").setFields("items/title").execute().getItems();
        if (tasks != null) {
            result = tasks.get(new Random().nextInt(tasks.size()));
        } else {
            result = null;
        }
        activity.task = result;
    }

    static void run(com.undrowned.WhatToDo.WhatToDo whatToDo) {
        new AsyncLoadTasks(whatToDo).execute();
    }
}

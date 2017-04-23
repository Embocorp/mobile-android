package com.b2ktechnology.multicorder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class ProjectActivity extends AppCompatActivity {
    public int currentId = 0;
    private String DELIM = "|";
    private String FILENAME = "experiments.txt";
    private String MyPREFERENCES = "MyPrefs";
    private FileManipulator f;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        f = new FileManipulator(ProjectActivity.this, FILENAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        File experiment_file = getBaseContext().getFileStreamPath("experiments.txt");

        if (experiment_file.exists()) {
            //Log.d("File","Exists");
            ViewGroup parent = (ViewGroup) findViewById(R.id.project_space);
            populateProjects(getProjects(), parent);
        } else {
            f.overwriteFile("");
        }

        setTitle("My Experiments");

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fab_new_project);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
                builder.setView(ProjectActivity.this.getLayoutInflater().inflate(R.layout.new_project_dialog, null))
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Dialog d = (Dialog) dialog;
                                EditText e = (EditText) d.findViewById(R.id.new_name);
                                dialog.dismiss();
                                createNewExperiment(e.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_project_display, menu);

        if (sharedPreferences.getBoolean("archive", false)) {
            menu.findItem(R.id.action_show_archive).setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_show_archive:
                LinearLayout parent = (LinearLayout) findViewById(R.id.project_space);
                if (item.isChecked()) {
                    item.setChecked(false);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("archive", false);
                    editor.commit();

                    ViewGroup parential = (ViewGroup) findViewById(R.id.project_space);
                    parential.removeAllViews();

                    populateProjects(getProjects(), parential);
                }
                else {
                    item.setChecked(true);
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        item.setChecked(true);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("archive", true);
                        editor.commit();

                        ViewGroup parential = (ViewGroup) findViewById(R.id.project_space);
                        parential.removeAllViews();

                        populateProjects(getProjects(), parential);
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public boolean createNewExperiment (String name) {
        if (name == "") {
            name = "My Project";
        }

        String string = (currentId + 1)+DELIM+name+DELIM+"False\n";

        f.appendFile(string);

        ViewGroup parent = (ViewGroup) findViewById(R.id.project_space);
        parent.removeAllViews();

        populateProjects(getProjects(), parent);

        Intent intent = new Intent(ProjectActivity.this, SensorDisplay.class);
        String experiment = String.valueOf(currentId);

        createProjectFile(currentId);

        intent.putExtra("title", name);
        intent.putExtra("id", experiment);
        startActivity(intent);

        return true;
    }

    public List<String[]> getProjects() {
        String[] lines = f.getLines();
        List<String[]> output = new ArrayList<String[]>();

        for (int i = 0; i < lines.length; i++) {
            output.add(f.getDelim(DELIM, lines[i]));
        }

        return output;
    }

    public void archiveProject (String projectId) {
        List<String[]> oldFile = getProjects();
        oldFile.get(Integer.parseInt(projectId) - 1)[2] = "Archived";

        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < oldFile.size(); i++) {
            temp.add(FileManipulator.concatArray(oldFile.get(i), DELIM));
        }

        String[] temp_string = new String[temp.size()];
        temp.toArray(temp_string);
        String newFile = FileManipulator.concatArray(temp_string, "\n");

        f.overwriteFile(newFile);
        ViewGroup parent = (ViewGroup) findViewById(R.id.project_space);
        parent.removeAllViews();
        populateProjects(getProjects(), parent);
    }

    public void changeProjectName (final String projectId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
        builder.setView(ProjectActivity.this.getLayoutInflater().inflate(R.layout.change_name_dialog, null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText e = (EditText) d.findViewById(R.id.new_name);
                        dialog.dismiss();

                        List<String[]> oldFile = getProjects();
                        oldFile.get(Integer.parseInt(projectId) - 1)[1] = e.getText().toString();

                        List<String> temp = new ArrayList<String>();
                        for (int i = 0; i < oldFile.size(); i++) {
                            temp.add(FileManipulator.concatArray(oldFile.get(i), DELIM));
                        }

                        String[] temp_string = new String[temp.size()];
                        temp.toArray(temp_string);
                        String newFile = FileManipulator.concatArray(temp_string, "\n");

                        f.overwriteFile(newFile);

                        ViewGroup parent = (ViewGroup) findViewById(R.id.project_space);
                        parent.removeAllViews();
                        populateProjects(getProjects(), parent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public void createProjectFile(int id) {
        String template = "Problem\n{\n}\nHypothesis\n{\n}\nMaterials\n{\n" +
                "}\nExperiment\n{\n}\nAnalysis\n{\n}\nConclusion\n{\n}";
        FileManipulator f2 = new FileManipulator(ProjectActivity.this, "experiment-"+Integer.toString(id)+".txt");
        f2.createFile();
        f2.overwriteFile(template);
    }

    private void onClick (View v, final String title) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SensorDisplay.class);
                String experiment = v.getTag().toString();

                intent.putExtra("title", title);
                intent.putExtra("id", experiment);
                startActivity(intent);
            }
        });
    }

    public boolean populateProjects (final List<String[]> projects, ViewGroup parent) {
        View view;
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int there = 0;
        boolean archival = sharedPreferences.getBoolean("archive", false);
        //Log.d("Preference", Boolean.toString(archival));

        for (int i = 0; i < projects.size(); i++) {
            view = vi.inflate(R.layout.content_project, parent, false);
            TextView name = (TextView) view.findViewById(R.id.experiment_name);
            name.setText(projects.get(i)[1]);
            view.findViewById(R.id.base).setTag(projects.get(i)[0]);
            onClick(view, projects.get(i)[1]);
            view.findViewById(R.id.experiment_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popup = new PopupMenu(ProjectActivity.this, v);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_archive:
                                    archiveProject(((LinearLayout) ((RelativeLayout) v.getParent()).getParent()).getTag().toString());
                                    return true;
                                case R.id.action_name:
                                    changeProjectName(((LinearLayout) ((RelativeLayout) v.getParent()).getParent()).getTag().toString());
                                    return true;
                                default:
                                    return false;
                            }

                        }
                    });
                    popup.inflate(R.menu.menu_project_options);
                    popup.show();
                }
            });
            //Log.d("Check",Boolean.toString(projects.get(i)[2].equals("False")));
            if (projects.get(i)[2].equals("False")) {
                ImageView im = (ImageView) view.findViewById(R.id.proj_progress_icon);
                im.setImageResource(R.drawable.ic_donut_large_black_24dp);
                im.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.progress));

                TextView tv = (TextView) view.findViewById(R.id.proj_progress);
                tv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.progress));
                tv.setText("In Progress");
                there++;
            } else if (projects.get(i)[2].equals("Archived")) {
                ImageView im = (ImageView) view.findViewById(R.id.proj_progress_icon);
                im.setImageResource(R.drawable.ic_archive_black_24dp);
                im.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.greyed));

                TextView tv = (TextView) view.findViewById(R.id.proj_progress);
                tv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.greyed));
                tv.setText("Archived");

                if (archival == false) {
                    view.setVisibility(View.GONE);
                } else {
                    there++;
                }
            } else {
                there++;
            }
            if (there > 0) {
                //Log.d("Heh","Heh");
                findViewById(R.id.no_prompt).setVisibility(View.GONE);
            } else {
                //Log.d("Heh","Heh");
                findViewById(R.id.no_prompt).setVisibility(View.VISIBLE);
            }
            parent.addView(view);
            currentId = i+1;
        }
        return true;
    }
}

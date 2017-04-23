package com.b2ktechnology.multicorder;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Iggy on 11/21/2016.
 */

public class FileManipulator {
    private String file_name;
    private Activity activity;

    FileManipulator(Activity active, String file) {
        activity = active;
        file_name = file;
    }

    public String[] getLines() {
        List<String> output = new ArrayList<String>();

        FileInputStream fis = retrieveInput();
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] real = new String[output.size()];

        return output.toArray(real);
    }

    public String getString() {
        StringBuilder output = new StringBuilder();
        List<String> temp = new ArrayList<String>();

        FileInputStream fis = retrieveInput();
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                temp.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convertListToString(temp);
    }

    public static String convertListToString(List<String> temp) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < temp.size(); i++) {
            output.append(temp.get(i));
            if (i + 1 < temp.size()) {
                output.append("\n");
            }
        }

        return output.toString();
    }

    public File getFileFromSub(String sub, String name) {
        File mydir = activity.getBaseContext().getDir(sub, Context.MODE_PRIVATE); //Creating an internal dir;
        File fileWithinMyDir = new File(mydir, name);
        return fileWithinMyDir; //Getting a file within the dir.
    }

    public boolean createFile () {
        String place = "";

        try {
            FileOutputStream fos = retrieveOutput(Context.MODE_PRIVATE);

            fos.write(place.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private FileOutputStream retrieveOutput(int mode) {
        try {
            if (file_name.contains("/")) {
                return new FileOutputStream(getFileFromSub(file_name.split(Pattern.quote("/"))[0], file_name.split(Pattern.quote("/"))[1]));
            } else {
                return activity.openFileOutput(file_name, mode);
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private FileInputStream retrieveInput() {
        try {
            if (file_name.contains("/")) {
                return new FileInputStream(getFileFromSub(file_name.split(Pattern.quote("/"))[0], file_name.split(Pattern.quote("/"))[1]));
            } else {
                return activity.openFileInput(file_name);
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String concatArray(String[] haystack, String needle) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < haystack.length; i++) {
            output.append(haystack[i]);
            if (i + 1 < haystack.length) {
                output.append(needle);
            }
        }
        return output.toString();
    }

    public String[] getDelim(String needle, String haystack) {
        return haystack.split(Pattern.quote(needle));
    }

    public boolean appendFile (String appendage) {
    try {
            FileOutputStream fos = retrieveOutput(Context.MODE_APPEND);
            fos.write(appendage.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<String> searchFile(String start, String end) {
        List<String> output = new ArrayList<String>();
        String[] temp = getLines();
        boolean flag = false;

        for (String i : temp) {
            if (flag == false) {
                flag = i.equals(start);
            } else {
                if (i.equals(end)) {
                    flag = false;
                    break;
                } else {
                    output.add(i);
                }
            }
        }
        return output;
    }

    public int getIndex (String needle) {
        int output = -1;

        String[] haystack = getLines();

        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i].equals(needle)) {
                output = i;
                break;
            }
        }

        return output;
    }

    public boolean overwriteFile (String contents) {
        //Commented out for development.  Uncomment for production
        String string = contents;

        //Uncommented for development.  Comment for production
        //String string = "1|Test Experiment\n2|Second Experiment";

        try {
            FileOutputStream fos = retrieveOutput(Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();

            //ViewGroup parent = (ViewGroup) findViewById(R.id.project_space);
            //populateProjects(getProjects(), parent);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String[] getRawResources(int id, Activity act) {
        List<String> s = new ArrayList<String>();
        InputStream file = act.getResources().openRawResource(id);
        String line = "";
        try {
            BufferedReader read = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            while ((line = read.readLine()) != null) {
                s.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] t = new String[s.size()];
        s.toArray(t);
        return t;
    }

    public static String[] getMeasures(Activity active) {
        List<String> output = new ArrayList<String>();
        String line = "";
        String[] feed = new String[3];
        String[] temp = FileManipulator.getRawResources(R.raw.sensors, active);

        for (int i = 0; i < temp.length; i++) {
            feed = temp[i].split(":");
            output.add(feed[0]);
        }

        String[] the = new String[ output.size() ];
        output.toArray( the );

        return the;
    }

    public static List<List<String[]>> getUnits(Activity active) {
        List<List<String[]>> output = new ArrayList<List<String[]>>();
        List<String[]> small = new ArrayList<String[]>();
        String line = "";
        String[] feed = new String[2];
        int index = 0;

        InputStream file = active.getResources().openRawResource(R.raw.sensors);
        BufferedReader read = new BufferedReader(new InputStreamReader(file));
        try {
            while ((line = read.readLine()) != null) {
                small.clear();
                for (int i = 0; i < line.split(":")[2].split(";").length; i++) {
                    feed = line.split(":")[2].split(";")[i].split(",");

                    small.add(feed);
                }

                output.add( new ArrayList<String[]>() );
                for (int i = 0; i < small.size(); i++) {
                    output.get(index).add(small.get(i));
                }

                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("Hello",output.toString());
        return output;
    }

    public static String[] getSensors(Activity active) {
        List<String> output = new ArrayList<String>();
        String line = "";
        String[] feed = new String[3];
        String[] temp = FileManipulator.getRawResources(R.raw.sensors, active);

        for (int i = 0; i < temp.length; i++) {
            feed = temp[i].split(":");
            output.add(feed[1]);
        }

        String[] the = new String[ output.size() ];
        output.toArray( the );

        return the;
    }

    public static List<String> searchRaw(int id, Activity act, String needle, String start, String end) {
        List<String> output = new ArrayList<String>();
        String[] temp = getRawResources(id, act);
        boolean flag = false;

        for (String i : temp) {
            if (flag == false) {
                flag = i.equals(needle);
            } else {
                if (i.equals(end)) {
                    flag = false;
                    break;
                } else if (i.equals(start)) {
                    continue;
                } else {
                    output.add(i);
                }
            }
        }
        return output;
    }
}

package com.b2ktechnology.multicorder;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Iggy on 11/23/2016.
 */

public class TabFragment {
    protected Activity activity;
    protected View view;
    protected LayoutInflater inflater;
    protected ViewGroup parent;
    protected int experimentId = 0;
    protected int subTab = 0;
    protected String[] dropdown = {
            "symbol"
    };
    public String version = "";

    public TabFragment (Activity a, int id, LayoutInflater i, ViewGroup p) {
        activity = a;
        inflater = i;
        parent = p;
        view = inflater.inflate(id, parent, false);
    }

    public View getView() {
        return view;
    }

    public void setInfoShow (final ViewGroup card, final String[] info) {
        for (View i : SensorDisplay.getViewsByTag(card, "info")) {
            i.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(Html.fromHtml(info[1])).setTitle(info[0]);
                    builder.show();
                }
            });
        }
    }

    public String[][] getHintInfo (String section) {
        String[] temp = FileManipulator.getRawResources(R.raw.comp_info, activity);
        String[][] info = new String[temp.length][2];
        boolean flag = false;
        int count = 0;

        for (int i = 0; i < temp.length; i++) {
            if (flag == false) {
                flag = (section.equals(temp[i]));
            } else {
                if (temp[i].equals("{")) {
                    continue;
                } else if (temp[i].equals("}")) {
                    flag = false;
                    count = 0;
                    break;
                } else {
                    info[count] = temp[i].split(Pattern.quote(","));
                    count++;
                }
            }
        }
        return info;
    }

    public ViewGroup addCard (int type, ViewGroup container, String id, @Nullable List<String[]> data) {
        View v = inflater.inflate(type, container, false);
        v.setTag(id);
        List<List<String[]>> units = FileManipulator.getUnits(activity);

        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                List<View> children = SensorDisplay.getViewsByTag((ViewGroup) v, data.get(i)[0]);
                if (children.size() > 0) {
                    for (View c : children) {
                        ((TextView) c).setText(Html.fromHtml(data.get(i)[1]));
                    }
                }
                for (String o : dropdown) {
                    if (o.equals(data.get(i)[0])) {
                        for (View c : children) {
                            for (int pop = 0; pop < units.size(); pop++) {
                                for (String[] q : units.get(pop)) {
                                    if (q[1].equals(data.get(i)[1])) {
                                        ((SensorDisplay) activity).chooseUnitBind(pop, c);
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        container.addView(v, container.getChildCount() - 1);
        container.invalidate();

        return (ViewGroup) v;
    }

    public int newRecordData (String name, String type, int id) {
        FileManipulator f = new FileManipulator(activity, "experiment-"+Integer.toString(experimentId)+".txt");
        String[] temp = f.getLines();
        List<String> output = new ArrayList<String>();
        int counter = 0;
        List<String> template = FileManipulator.searchRaw(R.raw.project_template, activity, type, "[", "]");
        boolean flag = false;
        StringBuilder b = new StringBuilder();

        for (String i : temp) {
            if (flag == false) {
                flag = i.equals(name);
            } else {
                if (i.equals("}")) {
                    output.add(counter, type);
                    counter++;
                    output.add(counter, version + Integer.toString(id + 1));
                    counter++;
                    output.add(counter, "[");
                    counter++;

                    for (int q = 0; q < template.size(); q++) {
                        output.add(counter, template.get(q));
                        counter++;
                    }
                    output.add("]");
                    counter++;
                    flag = false;
                } else if (i.equals("{")) {
                    //continue;
                } else {
                    //continue;
                }
            }
            output.add(counter, i);
            counter++;
        }

        counter = 0;
        for (String text : output) {
            b.append(text);
            counter++;
            if (counter < output.size()) {
                b.append("\n");
            }
        }
        f.overwriteFile(b.toString());
        return id + 1;
    }

    public void saveData (String type, String id, String[][] data){
        //Log.d("Call", "saveData");

        StringBuilder output = new StringBuilder();
        FileManipulator f = new FileManipulator(activity, "experiment-"+Integer.toString(experimentId)+".txt");
        List<String> segment = f.searchFile(id, "]");

        output.append(type+"\n");
        output.append(id+"\n");

        for (int i = 0; i < segment.size(); i++) {
            if (segment.get(i).contains(",")) {
                for (int g = 0; g < data.length; g++) {
                    if (segment.get(i).split(",")[0].equals(data[g][0])) {
                        output.append(data[g][0] + "," + data[g][1] + "\n");
                        break;
                    } else if (g + 1 == data.length) {
                        output.append(segment.get(i) + "\n");
                    }
                }
            } else {
                for (int g = 0; g < data.length; g++) {
                    if (segment.get(i).equals(data[g][0])) {
                        output.append(data[g][0] + "," + data[g][1] + "\n");
                        break;
                    } else if (g + 1 == data.length) {
                        output.append(segment.get(i) + "\n");
                    }
                }
            }
        }

        output.append("]");

        String original = f.getString();
        String search = type + "\n" + id + "\n" + FileManipulator.convertListToString(segment) + "\n]";

        f.overwriteFile(original.replace(search, output));
        //Log.d("Output", (original.replace(search, output.toString)));
        //Log.d("Output", Boolean.toString(original.contains(search)));
        //Log.d("Output", output.toString());
    }
}

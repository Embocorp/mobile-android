package com.b2ktechnology.multicorder;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
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
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TabExperiment extends TabFragment {
    private Map<Integer, String> typeMap = new HashMap<Integer, String>();
    private Integer[] memberTypes = {R.layout.fragment_experiment_trials, R.layout.fragment_experiment_procedure, R.layout.fragment_experiment_measurement};
    private String[] memberTypeDefinitions = {"Trial","Procedure","Measurement"};
    private ViewGroup directParent = ((ViewGroup) getView().findViewById(R.id.base_experiment));

    private int fullId = 0;

    public TabExperiment(Activity a, int id, LayoutInflater i, ViewGroup p, int pid) {
        super(a, id, i, p);
        experimentId = pid;
        for (int w = 0; w < memberTypes.length; w++) {
            typeMap.put(memberTypes[w], memberTypeDefinitions[w]);
        }
        version = "Experiment";
    }

    public void loadCards () {
        FileManipulator f = new FileManipulator(activity, "experiment-" + Integer.toString(experimentId) + ".txt");
        String[] temp = f.getLines();
        List<String> list = new ArrayList<String>();
        String tempId = "";
        boolean flag = false;

        for (int i = 0; i < temp.length; i++) {
            if (flag == false) {
                flag = temp[i].equals(version);
            } else {
                if (temp[i].equals("{")) {
                    continue;
                } else if (temp[i].equals("}")) {
                    break;
                } else {
                    for (int w = 0; w < memberTypes.length; w++) {
                        if (temp[i].equals(memberTypeDefinitions[w])) {
                            List<String[]> card = new ArrayList<>();
                            tempId = temp[i + 1];
                            for (int po = i + 2; po > 0; po++) {
                                if (temp[po].equals("[")) {
                                    continue;
                                } else if (temp[po].equals("]")) {
                                    i = po - 1;
                                    break;
                                } else {
                                    if (temp[po].contains(",")) {
                                        card.add(temp[po].split(Pattern.quote(",")));
                                    }
                                }
                            }

                            ViewGroup ted = addCard(memberTypes[w], directParent, tempId, card);
                            setInfoShow(ted, getHintInfo(version)[w]);
                            setKeyBoardListener(ted, w, "number");
                            fullId++;
                        }
                    }
                }
            }
        }
    }

    public void setMenu () {
        getView().findViewById(R.id.new_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popup = new PopupMenu(activity, v, Gravity.CENTER);

                for (int opt = 0; opt < memberTypeDefinitions.length; opt++) {
                    popup.getMenu().add(0, opt, opt, memberTypeDefinitions[opt]);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int wId = 0;
                        for (int sel = 0; sel < memberTypeDefinitions.length; sel++) {
                            if (item.getItemId() == sel) {
                                wId = memberTypes[sel];
                                fullId = newRecordData(version, typeMap.get(wId), fullId);
                                ViewGroup result = addCard(wId, directParent, version + Integer.toString(fullId), null);
                                setInfoShow(result, getHintInfo(version)[sel]);
                                setKeyBoardListener(result, sel, "number");
                                return true;
                            }
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    public void setKeyBoardListener (final ViewGroup card, final int mid, final String tag) {
        //Log.d("<Msg", Integer.toString(SensorDisplay.getViewsByTag(card, tag).size()));
        for (View qeu: SensorDisplay.getViewsByTag(card, tag)){
            ((EditText) qeu).setHorizontallyScrolling(false);
            ((EditText) qeu).setMaxLines(5);
            ((EditText) qeu).setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (!v.getText().toString().isEmpty()) {
                            String[][] data = {{tag, v.getText().toString()}};
                            saveData(memberTypeDefinitions[mid],card.getTag().toString() ,data);
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            handled = true;
                        } else {
                            handled = false;
                        }
                    }
                    return handled;
                }
            });
            ((EditText) qeu).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && !((TextView) v).getText().toString().isEmpty()) {
                        String[][] data = {{tag, ((TextView) v).getText().toString()}};
                        saveData(memberTypeDefinitions[mid],card.getTag().toString(), data);
                    }
                }
            });
        }
    }
}


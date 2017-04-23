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

public class TabHypothesis extends TabFragment {
    private Map<Integer, String> typeMap = new HashMap<Integer, String>();
    private Integer[] memberTypes = {R.layout.fragment_hypothesis_relationship, R.layout.fragment_hypothesis_quantitative, R.layout.fragment_hypothesis_qualitative};
    private String[] memberTypeDefinitions = {"Relationship", "Quantitative Relationship", "Qualitative Causality"};
    private ViewGroup directParent = ((ViewGroup) getView().findViewById(R.id.base_hypothesis));
    private String[] quantMeasures = {"Correlation", "Functional", "Statistics"};
    private String[][] subTypeQuantMeasures = {
            {"positive","negative","constant","zero"},
            {"linear","power","logarithmic","exponential","inverse","circular"},
            {"significant","not significant"}
    };
    private int fullId = 0;

    public TabHypothesis(Activity a, int id, LayoutInflater i, ViewGroup p, int pid) {
        super(a, id, i, p);
        experimentId = pid;
        version = "Hypothesis";
        for (int w = 0; w < memberTypes.length; w++) {
            typeMap.put(memberTypes[w], memberTypeDefinitions[w]);
        }
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
                            if (memberTypeDefinitions[w].equals("Quantitative Relationship")) {
                                for (String[] crud : card) {
                                    if (crud[0].equals("type")) {
                                        for (int bob = 0; bob < quantMeasures.length; bob++) {
                                            if (quantMeasures[bob].equals(crud[1])) {
                                                setTabQuant(ted, bob, 1);
                                                flag = true;
                                            }
                                        }
                                    } else {
                                        flag = false;
                                    }
                                }
                            }

                            if (flag == false) {
                                setTabQuant(ted, 1, 0);
                            }
                            setInfoShow(ted, getHintInfo(version)[w]);
                            setKeyBoardListener(ted, w, "description");
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
                                setKeyBoardListener(result, sel, "description");
                                if (memberTypeDefinitions[sel].equals("Quantitative Relationship")) {
                                    setTabQuant(result, 0, 1);
                                }
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
                            saveData(memberTypeDefinitions[mid],((View) v.getParent()).getTag().toString() ,data);
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
                        saveData(memberTypeDefinitions[mid],((View) v.getParent()).getTag().toString() ,data);
                    }
                }
            });
        }
    }

    public void setTabQuant (final ViewGroup v, int currentTab, int currentSubTab) {
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(new QuantFragmentPagerAdapter(((SensorDisplay) activity).getSupportFragmentManager(), (SensorDisplay) activity, currentTab));
        //viewPager.setCurrentItem(currentTab);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Log.d("Checl", "Check");
                String[][] data = {
                        {"type", quantMeasures[tab.getPosition()]}
                };
                subTab = tab.getPosition();
                saveData("Quantitative Relationship", v.getTag().toString(), data);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getTabAt(currentTab).select();
        List<View> spinners = SensorDisplay.getViewsByTag(v, "subtype-" + Integer.toString(currentTab));
        Log.d("Tag", "subtype-" + Integer.toString(currentTab));
        for (View s : spinners) {
            ((Spinner) s).setSelection(currentSubTab);
        }
    }

    public void saveSubTypeQuant (int t, int s, View v) {
        String type  = quantMeasures[t];
        String subtype = subTypeQuantMeasures[t][s];


        String[][] data = {
                {"subtype", subtype}
        };

        saveData("Quantitative Relationship", ((View) v.getParent().getParent().getParent().getParent()).getTag().toString(), data);
    }
}

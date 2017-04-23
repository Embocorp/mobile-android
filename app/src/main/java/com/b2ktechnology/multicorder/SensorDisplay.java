package com.b2ktechnology.multicorder;

import android.hardware.Sensor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.security.AccessController.getContext;

public class SensorDisplay extends AppCompatActivity {

    public int id = 0;
    public int mode = 0;
    public ViewPager viewPager;
    public TabFragment activeTab;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.activity_sensor_display);

        setTitle(getIntent().getStringExtra("title"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back_black_24dp, null);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(upArrow);

        id = Integer.parseInt(getIntent().getStringExtra("id"));

        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabFragmentPagerAdapter(getSupportFragmentManager(),
                SensorDisplay.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton start = (FloatingActionButton) findViewById(R.id.play);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Please finish setting up your experiment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final Drawable settings = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_settings_black_24dp, null);
        settings.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        final Drawable devices = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_devices_black_24dp, null);
        devices.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        getMenuInflater().inflate(R.menu.menu_sensor_display, menu);

        menu.getItem(0).setIcon(devices);
        menu.getItem(1).setIcon(settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(SensorDisplay.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_devices) {
            Intent intent = new Intent(SensorDisplay.this, DeviceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public void chooseMeasure(final View v) {
        final String[] measurements = FileManipulator.getMeasures(SensorDisplay.this);
        final String[] sensors = FileManipulator.getSensors(SensorDisplay.this);
        final List<List<String[]>> units = FileManipulator.getUnits(SensorDisplay.this);
        PopupMenu popup = new PopupMenu(SensorDisplay.this, v, Gravity.CENTER);

        for (int i = 0; i < measurements.length; i++) {
            popup.getMenu().add(0, i, i, measurements[i]);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                for (int i = 0; i < measurements.length; i++) {
                    if (i == item.getItemId()) {

                        FileManipulator f = new FileManipulator(SensorDisplay.this, "experiment-" + Integer.toString(id) + ".txt");
                        View parent = (View) v.getParent().getParent().getParent();
                        String ided = parent.getTag().toString();
                        int index = f.getIndex(ided) - 1;
                        String type = f.getLines()[index];
                        String[][] data = {
                                {"measure", measurements[i]},
                                {"unit", units.get(i).get(0)[0]},
                                {"symbol", units.get(i).get(0)[1]},
                                {"sensor", sensors[i]}
                        };

                        activeTab.saveData(type, ided, data);

                        ((TextView) v).setText(measurements[i]);
                        List<View> sensorview = getViewsByTag((ViewGroup) v.getParent(), "sensor");
                        for (View s : sensorview) {
                            ((TextView) s).setText(sensors[i]);
                        }
                        List<View> unitnameview = getViewsByTag((ViewGroup) v.getParent().getParent(), "unit");
                        for (View huh : unitnameview) {
                            ((TextView) huh).setText(units.get(i).get(0)[0]);
                        }
                        List<View> unitsimview = getViewsByTag((ViewGroup) v.getParent().getParent(), "symbol");
                        for (View u : unitsimview) {
                            ((TextView) u).setText(Html.fromHtml(units.get(i).get(0)[1]));

                            chooseUnitBind(i, u);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        popup.show();
    }

    public void chooseUnitBind(final int i, final View v) {
        ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand_more_black_24dp, null));
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (-5 * scale + 0.5f);
        ((TextView) v).setCompoundDrawablePadding(dpAsPixels);
        v.setPadding(0, 0, 0, dpAsPixels);

        final List<List<String[]>> units = FileManipulator.getUnits(SensorDisplay.this);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu menu = new PopupMenu(SensorDisplay.this, v, Gravity.CENTER);
                for (int t = 0; t < units.get(i).size(); t++) {
                    menu.getMenu().add(0, t, t, units.get(i).get(t)[0]);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            for (int g = 0; g < units.get(i).size(); g++) {
                                if (item.getItemId() == g) {
                                    ((TextView) view).setText(Html.fromHtml(units.get(i).get(g)[1]));
                                    List<View> nameview = getViewsByTag((ViewGroup) view.getParent(), "unit");
                                    for (View f : nameview) {
                                        ((TextView) f).setText(units.get(i).get(g)[0]);
                                    }
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                }
                menu.show();
            }
        });
    }

    public void removeCard(View v) {
        View card = ((View) (v.getParent().getParent().getParent()));
        ViewGroup parent = ((ViewGroup) v.getParent().getParent().getParent().getParent());
        parent.removeView(card);

        String viewId = card.getTag().toString();
        FileManipulator f = new FileManipulator(SensorDisplay.this, "experiment-" + Integer.toString(id) + ".txt");
        List<String> temp = f.searchFile(viewId, "]");
        String[] file = f.getLines();
        Log.d("Id", viewId + "csdf");
        String type = file[f.getIndex(viewId) - 1];
        StringBuilder b = new StringBuilder();
        StringBuilder c = new StringBuilder();

        b.append(type + "\n");
        b.append(viewId + "\n");
        for (int i = 1; i < temp.size(); i++) {
            b.append(temp.get(i) + "\n");
        }
        //b.append("]");

        boolean flag = false;
        for (int i = 0; i < file.length; i++) {
            if (flag == false) {
                if (file[i].equals(type) && file[i + 1].equals(viewId)) {
                    flag = true;
                    continue;
                } else {
                    c.append(file[i]);
                }
            } else {
                if (file[i].equals("]")) {
                    flag = false;
                }
                continue;
            }

            if (i < file.length - 1) {
                c.append("\n");
            }
        }

        f.overwriteFile(c.toString());
    }

    public void addProcedure(View v) {
        String[] steps = new String[] {
                "1. Go outside",
                "2. Set up Multicorder to record",
                "3. Start measurements",
                "4. Walk towards Christmas Tree slowly holding the Multicorder upright solidly"
        };


            ListView procedure = (ListView) ((ViewGroup) v.getParent().getParent().getParent()).findViewById(R.id.procedure);
            procedure.setAdapter(new ArrayAdapter<String>(SensorDisplay.this, android.R.layout.simple_list_item_1, android.R.id.text1, steps));

            ListAdapter listAdapter = procedure.getAdapter();
            if (listAdapter != null) {

                int numberOfItems = listAdapter.getCount();

                // Get total height of all items.
                int totalItemsHeight = 0;
                for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                    View item = listAdapter.getView(itemPos, null, procedure);
                    item.measure(0, 0);
                    totalItemsHeight += item.getMeasuredHeight();
                }

                // Get total height of all item dividers.
                int totalDividersHeight = procedure.getDividerHeight() *
                        (numberOfItems - 1);

                // Set list height.
                ViewGroup.LayoutParams params = procedure.getLayoutParams();
                params.height = totalItemsHeight + totalDividersHeight;
                procedure.setLayoutParams(params);
                procedure.requestLayout();
            }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SensorDisplay Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}

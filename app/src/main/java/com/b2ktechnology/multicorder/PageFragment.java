package com.b2ktechnology.multicorder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Iggy on 11/17/2016.
 */

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

        String[] sensors = FileManipulator.getSensors(getActivity());
        List<List<String[]>> units = FileManipulator.getUnits(getActivity());
        String[] measures = FileManipulator.getMeasures(getActivity());
        List<String[]> cards = new ArrayList<String[]>();

        if (mPage == 0) {

            TabProblem problem = new TabProblem(getActivity(), R.layout.content_problem_display, inflater, container, ((SensorDisplay) getActivity()).id);
            problem.setMenu();
            problem.loadCards();
            ((SensorDisplay) getActivity()).activeTab = problem;

            view = problem.getView();
        } else if (mPage == 1) {

            TabHypothesis hypothesis = new TabHypothesis(getActivity(), R.layout.content_hypothesis_display, inflater, container, ((SensorDisplay) getActivity()).id);
            hypothesis.setMenu();
            hypothesis.loadCards();
            ((SensorDisplay) getActivity()).activeTab = hypothesis;

            view = hypothesis.getView();
        } else if (mPage == 2) {
            TabMaterials materials = new TabMaterials(getActivity(), R.layout.content_materials, inflater, container, ((SensorDisplay) getActivity()).id);
            materials.setMenu();
            materials.loadCards();
            ((SensorDisplay) getActivity()).activeTab = materials;

            view = materials.getView();
        } else if (mPage == 3) {
            TabExperiment experiment = new TabExperiment(getActivity(), R.layout.content_sensor_display, inflater, container, ((SensorDisplay) getActivity()).id);
            experiment.setMenu();
            experiment.loadCards();
            ((SensorDisplay) getActivity()).activeTab = experiment;

            view = experiment.getView();

            String[] steps = new String[] {
                    "1. Go outside",
                    "2. Set up Multicorder to record",
                    "3. Start measurements"
            };

            /*
            ListView procedure = (ListView) view.findViewById(R.id.procedure);
            procedure.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, steps));

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

            inflater = getActivity().getLayoutInflater();
            View v;
            TextView sensor_name;
            TextView unit_name;
            TextView value;

            for (int i = 0; i < measures.length; i++) {
                if (i == 0 || i == 7) {
                    v = inflater.inflate(R.layout.sensor_card_layout, mLinearLayoutContainer, false);
                    mLinearLayoutContainer.addView(v, (mLinearLayoutContainer.getChildCount() - 1));
                    sensor_name = (TextView) v.findViewById(R.id.sensor_name);
                    sensor_name.setText(measures[i]);
                    unit_name = (TextView) v.findViewById(R.id.unit);
                    unit_name.setText(units.get(i).get(0)[1]);
                    //Log.d("Good",units.get(0).get(0)[0]);
                    value = (TextView) v.findViewById(R.id.measurment);
                    value.setText("0");
                }
            }
            */
        } else if (mPage == 4) {
            view = inflater.inflate(R.layout.content_data, container, false);
            LineChart chart = (LineChart) view.findViewById(R.id.chart);

            int[][] dataObjects = new int[][]{
                    {
                            0,
                            20
                    },
                    {
                            5,
                            8
                    },
                    {
                            10,
                            1
                    },
                    {
                            15,
                            0
                    },
                    {
                            20,
                            0
                    }
            };

            List<Entry> entries = new ArrayList<Entry>();

            for (int[] data : dataObjects) {

                // turn your data into Entry objects
                entries.add(new Entry(data[0], data[1]));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Trial 1"); // add entries to dataset
            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            dataSet.setCircleRadius(8);
            dataSet.setCircleHoleRadius(3);
            dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            dataSet.setLineWidth(3);
            dataSet.setHighLightColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            dataSet.setHighlightLineWidth(2);
            dataSet.setValueTextSize(0);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);

            XAxis xaxis = chart.getXAxis();
            xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xaxis.setTextSize(14f);
            xaxis.setDrawGridLines(false);
            xaxis.setLabelRotationAngle(30);
            xaxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setRoundingMode(RoundingMode.HALF_UP);
                    value = Float.valueOf(df.format(value));
                    return ((float) value)+" ft";
                }
            });


            chart.getAxisRight().setDrawLabels(false);
            chart.getAxisRight().setDrawAxisLine(false);

            YAxis yaxis = chart.getAxisLeft();
            yaxis.setTextSize(14f);
            yaxis.setDrawZeroLine(true);
            yaxis.setDrawAxisLine(false);
            yaxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setRoundingMode(RoundingMode.HALF_UP);
                    value = Float.valueOf(df.format(value));
                    return ((float) value)+" cd";
                }
            });

            Legend legend = chart.getLegend();
            legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            legend.setTextSize(14);

            chart.setDescription(null);
            chart.setTouchEnabled(true);
            chart.setPinchZoom(true);

            chart.invalidate(); // refresh
        } else if (mPage == 5) {
            TabHypothesis hypothesis = new TabHypothesis(getActivity(), R.layout.content_hypothesis_display, inflater, container, ((SensorDisplay) getActivity()).id);
            hypothesis.setMenu();
            hypothesis.loadCards();
            ((SensorDisplay) getActivity()).activeTab = hypothesis;

            view = hypothesis.getView();
        } else {
            view = inflater.inflate(R.layout.content_problem_display, container, false);
        }
        return view;
    }
}

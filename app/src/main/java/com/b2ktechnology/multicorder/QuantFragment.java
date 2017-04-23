package com.b2ktechnology.multicorder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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

public class QuantFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static QuantFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        QuantFragment fragment = new QuantFragment();
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

        List<String> correlations = new ArrayList<String>();
        correlations.add("Positive");
        correlations.add("Negative");
        correlations.add("Constant");
        correlations.add("None");

        List<String> regressions = new ArrayList<String>();
        regressions.add("Linear");
        regressions.add("Power");
        regressions.add("Logarithmic");
        regressions.add("Exponential");
        regressions.add("Inverse");
        regressions.add("Circular");

        List<String> stats = new ArrayList<String>();
        stats.add("Significant difference");
        stats.add("No significant difference");

        final float[][][][] dataset = {
                {
                        {{0, 0}, {1, 1}},
                        {{0, 1}, {1, 0}},
                        {{0, 0.5f}, {1, 0.5f}},
                        {{0, 1f}, {0.25f, 0.1f}, {0.75f, 0.8f}, {1f, 0.5f}}
                },
                {
                        {{0, 0}, {1, 1}},
                        {{0, 0}, {0.125f, 0.0156f}, {0.25f, 0.0625f}, {0.5f, 0.25f},{0.75f, 0.5625f},{0.9f, 0.81f},{1,1}},
                        {{0.1f, 0f}, {0.2f, 0.25f}, {0.3f, 0.45f}, {0.4f, 0.59f}, {0.5f, 0.7f}, {0.6f, 0.795f}, {0.7f, 0.87f}, {0.8f, 0.95f}, {1f, 1.03f}},
                        {{0, 0}, {0.25f, 0.19f}, {0.5f, 0.4f}, {1f, 1f}, {1.5f, 1.82f}, {2f, 3f}},
                        {{0.1f, 5}, {0.2f, 2.5f}, {0.4f, 1.25f}, {0.5f, 1}, {0.75f, 0.67f}, {1, 0.5f}, {1.5f, 0.33f}, {2, 0.25f}},
                        {{0, 1}, {0.3925f, 1.707f}, {0.58875f, 1.924f}, {0.785f, 2}, {0.98125f, 1.924f}, {1.1775f, 1.707f}, {1.57f, 1}, {1.9625f,0.293f}, {2.15857f, 0.07612f}, {2.36f, 0}, {2.55125f, 0.07612f}, {2.7475f,0.293f}, {3.14f, 1}}
                }
        };

        final int[][] bounds = {
                {1, 1},
                {1, 1},
                {1, 1},
                {2, 3},
                {2, 2},
                {4, 2}
        };

        final String[][] descriptions = {
                {
                        "The dependent variable increases with the independent variable.",
                        "The dependent variable decreases when the independent variable increases.",
                        "The dependent variable remains constant despite the independent variable.",
                        "There is no linear correlation between the two variables"
                },
                {
                        "The dependent variable increases at a constant rate in relation to the independent variable (y = mx).",
                        "The dependent increases at a constant exponential rate (y = x<sup><small>c</small></sup>) in relation to the independent variable.",
                        "The dependent variable increases at a logarithmic rate (y = log<sub><small>c</small></sub>x) in relation to the independent variable.",
                        "The dependent variable increases at an exponential rate (y = c<sup<small>x</small></sup>) in relation to the independent variable.",
                        "The dependent variable decreased as the independent variable increases at a given rate (y = c/x).",
                        "The dependent variable follows a cyclic, repeating pattern in relation to the independent variable (y = sinx)."
                },
                {
                        "There is not sufficient evident to claim that the changes experienced by the dependent variable is related to and caused by the change in the independent variable.  Therefore, results can be caused by factors other than chance.",
                        "The changes experienced by the dependent variable are not related to nor caused by the change in the independent variable.  The change is likely caused by chance."
                }
        };

        if (mPage == 0) {

            view = inflater.inflate(R.layout.fragment_hypothesis_quant_correlation, container, false);
            final LineChart chart = (LineChart) view.findViewById(R.id.chart);

            final Spinner spin = (Spinner) view.findViewById(R.id.spinner);
            final TextView descript = (TextView) view.findViewById(R.id.corr_descript);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, correlations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(adapter);

            setSpinner(view, spin, chart, descript, dataset, bounds, descriptions, mPage);
        } else if (mPage == 1) {
            view = inflater.inflate(R.layout.fragment_hypothesis_quant_regression, container, false);
            final LineChart chart = (LineChart) view.findViewById(R.id.chart);

            final Spinner spin = (Spinner) view.findViewById(R.id.spinner);
            final TextView descript = (TextView) view.findViewById(R.id.corr_descript);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, regressions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spin.setAdapter(adapter);

            setSpinner(view, spin, chart, descript, dataset, bounds, descriptions, mPage);
        } else {
            view = inflater.inflate(R.layout.fragment_hypothesis_quant_statistics, container, false);

            final Spinner spin = (Spinner) view.findViewById(R.id.spinner);
            final TextView descript = (TextView) view.findViewById(R.id.corr_descript);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stats);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(adapter);

            setSpinner(view, spin, null, descript, dataset, bounds, descriptions, mPage);

        }
        return view;
    }

    private void createModelChart (LineChart chart, float[][] model, int maxX, int maxY) {

        float[][] dataObjects = model;

        List<Entry> entries = new ArrayList<Entry>();

        for (float[] data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new Entry(data[0], data[1]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Trial 1"); // add entries to dataset
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(3);
        dataSet.setHighlightEnabled(false);
        dataSet.setValueTextSize(0);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xaxis = chart.getXAxis();
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setTextSize(0);
        xaxis.setDrawAxisLine(true);
        xaxis.setDrawGridLines(false);
        xaxis.setAxisMaximum(maxX);
        xaxis.setDrawLabels(false);
        xaxis.setAxisLineWidth(3);

        chart.getAxisRight().setEnabled(false);

        YAxis yaxis = chart.getAxisLeft();
        yaxis.setDrawLabels(false);
        yaxis.setAxisMinimum(0);
        yaxis.setAxisMaximum(maxY);
        yaxis.setDrawAxisLine(true);
        yaxis.setDrawGridLines(false);
        yaxis.setAxisLineWidth(3);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        chart.setDescription(null);
        chart.setTouchEnabled(false);

        chart.invalidate(); // refresh
    }

    private void setSpinner (final View parrot, final Spinner s, final View chart, final TextView descript, final float[][][][] dataset, final int[][] bounds, final String[][] descriptions, final int current) {
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (chart != null) {
                    createModelChart((LineChart) chart, dataset[current][position], bounds[position][0], bounds[position][1]);
                }

                descript.setText(Html.fromHtml(descriptions[current][position]));

                /*if (((SensorDisplay) getActivity()).activeTab.subTab == current) {
                    ((TabHypothesis) ((SensorDisplay) getActivity()).activeTab).saveSubTypeQuant(current, position, parent);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Test","Nothing");
            }
        });
    }
}

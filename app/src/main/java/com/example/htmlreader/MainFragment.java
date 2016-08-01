package com.example.htmlreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainFragment extends Fragment {

    // log
    private final String LOG_TAG = MainFragment.class.getSimpleName();

    // read html task
    ReadHtmlTask readHtmlTask;

    // control
    Button htmlButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // set up show html button
        htmlButton = (Button) rootView.findViewById(R.id.htmlButton);
        htmlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHtml(v);
            }
        });

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (readHtmlTask != null) {
            Log.v(LOG_TAG, "Read html task interrupted due to application being stopped");
            readHtmlTask.cancel(true);
            htmlButton.setEnabled(true);
        }
    }

    // start task to show html
    public void showHtml(View view) {

        // disable control
        htmlButton.setEnabled(false);

        // check if another similar task is running
        if (readHtmlTask != null) {
            readHtmlTask.cancel(true);
        }

        // new read html task
        readHtmlTask = new ReadHtmlTask();

        // getting URL from editText control and executing the task
        readHtmlTask.execute(((EditText) getActivity().findViewById(R.id.htmlEditText)).getText().toString());
    }

    /**
     * Asynchronously read html code given URL of a web page, then update UI
     */
    private class ReadHtmlTask extends AsyncTask<String, Void, String> {

        // log
        private final String LOG_TAG = ReadHtmlTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            // no URL
            if (params.length == 0)
                return null;

            // getting URL text
            String urlText = params[0];

            // connection data
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // will contain the html text
            String htmlText = null;

            try {

                // create url
                URL url = new URL(urlText);

                // making request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                htmlText = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return htmlText;
        }

        @Override
        protected void onPostExecute(String htmlText) {

            // set UI
            if (htmlText != null) {
                ((TextView) getActivity().findViewById(R.id.htmlTextView)).setText(htmlText);
            } else {
                // bad answer
                Toast.makeText(getContext(), R.string.toast_html_bad_answer, Toast.LENGTH_LONG).show();
            }

            // enable control
            htmlButton.setEnabled(true);
        }
    }

}

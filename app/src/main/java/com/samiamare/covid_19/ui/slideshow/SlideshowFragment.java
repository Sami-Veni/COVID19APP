package com.samiamare.covid_19.ui.slideshow;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.samiamare.covid_19.R;
import com.samiamare.covid_19.message.Message;
import com.samiamare.covid_19.message.MessageAdapter;
import com.samiamare.covid_19.ml.ClassifierModel;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    RecyclerView chatRecyclerView;
    EditText messageBox;
    Button sendButton;
    MessageAdapter messageAdapter;
    ArrayList<Message> messageList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("ChatBot");
        super.onCreate(savedInstanceState);
        chatRecyclerView = root.findViewById(R.id.chatRecyclerView);
        messageBox = root.findViewById(R.id.messageBox);
        sendButton = root.findViewById(R.id.sendButton);
        messageList = new ArrayList();
        messageAdapter = new MessageAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(messageAdapter);
        Message message = new Message("Hello and Welcome! I am here to answer " +
                "your questions about COVID-19. You can type in your questions and send them.", "answer");
        messageList.add(message);
        messageAdapter.notifyDataSetChanged();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if (message.equals("")){
                    Toast.makeText(getContext(), "Question Can't be Empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    Message messageSent = new Message(message, "question");
                    messageList.add(messageSent);
                    messageBox.setText("");
                    messageAdapter.notifyDataSetChanged();
                    //save_messages();
                    classifyText(message);
                    chatRecyclerView.scrollToPosition(messageList.size()-1);
                }

            }
        });

        return root;
    }

    public String [] formatKeys(JsonArray inp){
        String[] keys = new String[inp.size()];
        for (int i = 0; i < inp.size(); i++){
            String element = inp.get(i).toString();
            StringBuilder key = new StringBuilder(element);
            if (element.charAt(element.length()-1) == '"')
                key.deleteCharAt(element.length()-1);
            if (element.charAt(0) == '"')
                key.deleteCharAt(0);
            keys[i] = key.toString();
        }
        return keys;
    }

    public int [] formatValues(JsonArray inp){
        int[] values = new int[inp.size()];
        for (int i = 0; i < inp.size(); i++){
            JsonElement je = inp.get(i);
            String value = je.toString();
            values[i] = Integer.parseInt(value);
        }
        return values;
    }

    public int [] tokenizeSentence (String input) {

        String jsonString = getJson("keys.txt");
        String jsonValues = getJson("values.txt");
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        JsonElement jsonElement1 = JsonParser.parseString(jsonValues);

        JsonArray word_index = jsonElement.getAsJsonArray();
        JsonArray JValues = jsonElement1.getAsJsonArray();

        String[] keys = formatKeys(word_index);
        int[] values = formatValues(JValues);

        StringBuilder stringBuilder = new StringBuilder(input);
        int len = input.length();
        for (int i = 0; i < len; i++){
            if (Character.isDigit(input.charAt(i)) || Character.isAlphabetic(input.charAt(i))
                    || input.charAt(i) == ' ') {
            }
            else {
                stringBuilder.deleteCharAt(i);
                input = stringBuilder.toString();
                len = input.length();
                i--;
            }
        }
        String[] tokens = input.split(" ", 10000);
        int[] padded = new int[100];
        int ind_value;
        int i;
        for (i = 0;  i < tokens.length && i < 100; i++){
            ind_value = values[0];
            for (int j = 0; j < keys.length; j ++){
                if (tokens[i].toLowerCase(Locale.ROOT).equals(keys[j])) {
                    ind_value = values[j];
                    break;
                }
            }
            padded[i] = ind_value;
        }
        if (i < 100){
            for (; i < 100; i ++)
                padded[i] = 0;
        }
        return padded;
    }

    public void classifyText(String input) {
        try {
            // Reading the TFLite model
            ClassifierModel model = ClassifierModel.newInstance(getContext());

            // Creates inputs for model.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 100}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 100);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] sequence = tokenizeSentence(input);
            float[] sequenceFloat = convertToFloat(sequence);
            Log.d("SequenceFloat", Arrays.toString(sequenceFloat));

            for (int i = 0; i < 100; i ++){
                byteBuffer.putFloat(sequenceFloat[i]);
            }
            Log.d("ByteBuffer", Arrays.toString(byteBuffer.array()));

            inputFeature0.loadBuffer(byteBuffer);

            Log.d("Input Feature", Arrays.toString(inputFeature0.getFloatArray()));

            // Runs model inference and gets result.
            ClassifierModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            //Log.d("TensorBuffer", outputs.toString());

            float[] results = outputFeature0.getFloatArray();
            Log.d("Results", Arrays.toString(results));
            float max_result = results[0];
            int max_pos = 0;
            for (int i = 0; i < results.length; i++)
                if (results[i] > max_result) {
                    max_result = results[i];
                    max_pos = i;
                }
            String[] labels = {"common", "covid"};

            getAnswer(input, labels[max_pos]);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void getAnswer(String input, String label) {

        RequestQueue queue = Volley.newRequestQueue(getContext());

        String[] parameters = input.split(" ");

        String url = "https://covidchatbotapi.herokuapp.com/?ques=";
        StringBuilder urlBuilder = new StringBuilder(url);
        for (String parameter : parameters) {
            urlBuilder.append(parameter);
            urlBuilder.append("+");
        }
        urlBuilder.deleteCharAt(urlBuilder.length()-1);
        urlBuilder.append("&label=");
        urlBuilder.append(label);
        url = urlBuilder.toString();
        Log.d("URL", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(String response) {
                Message messageReceived = new Message(response, "answer");
                messageList.add(messageReceived);
                messageAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size()-1);
                //save_messages();
                Log.d("URL", "Inside on Response");
                Log.d("Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error Response", error.toString());
            }
        });
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(stringRequest);
    }

    private float[] convertToFloat(int[] sequence) {
        float[] converted = new float[sequence.length];
        for (int i = 0; i < sequence.length; i++)
            converted[i] = (float) sequence[i];
        return converted;
    }

    public String getJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = getContext().getAssets();
            BufferedReader bf = new BufferedReader(
                    new InputStreamReader(assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

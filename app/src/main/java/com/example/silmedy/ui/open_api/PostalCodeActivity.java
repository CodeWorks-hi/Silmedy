package com.example.silmedy.ui.open_api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class PostalCodeActivity extends AppCompatActivity {

    private ResultAdapter adapter;
    private ArrayList<String> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postal_code);

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        EditText inputAddress = findViewById(R.id.inputAddress);
        Button openPostcodeButton = findViewById(R.id.openPostcodeButton);
        RecyclerView resultList = findViewById(R.id.resultList);

        addressList = new ArrayList<>();
        adapter = new ResultAdapter(addressList);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        resultList.setAdapter(adapter);

        openPostcodeButton.setOnClickListener(v -> {
            String keyword = inputAddress.getText().toString();
            searchZipCode(keyword);
        });
    }

    public void searchZipCode(String keyword) {
        String apiKey = "devU01TX0FVVEgyMDI1MDQyMjE3MDExMTExNTY4MzQ=";
        String encodedKeyword = "";
        try {
            encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String apiUrl = "https://business.juso.go.kr/addrlink/addrLinkApi.do"
                + "?currentPage=1&countPerPage=100"
                + "&keyword=" + encodedKeyword
                + "&confmKey=" + apiKey
                + "&resultType=json";

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                is.close();

                JSONObject jsonObject = new JSONObject(result.toString());
                JSONArray jusoArray = jsonObject.getJSONObject("results").getJSONArray("juso");

                ArrayList<String> tempResults = new ArrayList<>();
                for (int i = 0; i < jusoArray.length(); i++) {
                    JSONObject obj = jusoArray.getJSONObject(i);
                    String zipNo = obj.getString("zipNo");
                    String roadAddr = obj.getString("roadAddr");
                    tempResults.add("[" + zipNo + "] " + roadAddr);
                }

                runOnUiThread(() -> {
                    addressList.clear();
                    addressList.addAll(tempResults);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private final ArrayList<String> items;

        public ResultAdapter(ArrayList<String> items) {
            this.items = items;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView zipTextView;
            TextView addrTextView;
            public ViewHolder(View view) {
                super(view);
                zipTextView = view.findViewById(R.id.zipTextView);
                addrTextView = view.findViewById(R.id.addrTextView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String[] split = items.get(position).split("]", 2);
            String zip = split[0].replace("[", "").trim();
            String addr = split.length > 1 ? split[1].trim() : "";
            holder.zipTextView.setText(zip);
            holder.addrTextView.setText(addr);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra("zipCode", zip);
                intent.putExtra("address", addr);
                ((AppCompatActivity) v.getContext()).setResult(RESULT_OK, intent);
                ((AppCompatActivity) v.getContext()).finish();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
package com.example.stockmarketapplicationproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private HashMap<String, String> stocks = new HashMap<String, String>();
    private HashMap<String, Integer> stockPersonalTracker = new HashMap<String, Integer>();
    private String[] STOCKS = new String[8882];
    private String[] STOCKS_SYMBOLS = new String[8882];
    private JSONObject metaData;
    private JSONObject refreshes;
    private double balance;
    private boolean isStockSelected = false;
    private RadioGroup buyorsellgroup;
    private RadioButton buyorsell;
    double price;
    String symbol;
    TextView balanceInd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        balance = getIntent().getDoubleExtra("startingValue", 0);
        balanceInd = findViewById(R.id.balanceView);
        balanceInd.setText("Balance : $ " + balance);

        Button apply = findViewById(R.id.btnApply);
        buyorsellgroup = findViewById(R.id.radioUpdate);
        RadioButton buy = findViewById(R.id.radioBuy);
        RadioButton sell = findViewById(R.id.radioSell);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText quantityOfShares = findViewById(R.id.textquantity);
                String test = quantityOfShares.getText().toString();
                if (test.matches("")) {
                    System.out.println("Invalid Entry");
                    return;
                }
                if (!isStockSelected) {
                    System.out.println("A Stock must be selected");
                    return;
                }
                int numShares = Integer.parseInt(quantityOfShares.getText().toString());
                if (buy.isChecked()) {
                    buy(numShares);
                } else if (sell.isChecked()) {
                    sell(numShares);
                } else {
                    System.out.println("Nothing selected");
                }
            }
        });
        Button refresher = findViewById(R.id.btnRefresh);
        refresher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText stockSearch = findViewById(R.id.searchStock);
                String stockToSearch = stockSearch.getText().toString();
                if (stockToSearch.equals("") || stockSearch == null) {
                    return;
                }
                if (doesStockExist(stockToSearch)) {
                    request(findStockinMap(stockToSearch));
                    requestSymbolSearch(stockToSearch);
                } else {
                    System.out.println("Stock does not exist");
                }
            }
        });

                Button enterSearch = findViewById(R.id.enterSearch);
        enterSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText stockSearch = findViewById(R.id.searchStock);
                String stockToSearch = stockSearch.getText().toString();
                if (doesStockExist(stockToSearch)) {
                    request(findStockinMap(stockToSearch));
                    requestSymbolSearch(stockToSearch);
                } else {
                    System.out.println("Stock does not exist");
                }
            }
        });

        Switch turnSymbolSearchOn = findViewById(R.id.switchSymbolSearch);
        //Loads stocks.txt into hashmap
        InputStream is = null;
        BufferedReader br = null;
        try {

            is = getAssets().open("stocks.txt");
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            int index = 0;
            while ((line = br.readLine()) != null) {
                String[] keyValSplit = line.trim().split("\\s++", 2);
                STOCKS[index] = keyValSplit[1];
                STOCKS_SYMBOLS[index] = keyValSplit[0];
                stocks.put(keyValSplit[1], keyValSplit[0]);
                index++;
            }
            for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
                stockPersonalTracker.put(stockEntry.getValue(), 0);
            }
            System.out.println("SS: " + stocks.size());
            System.out.println("SPTS: " + stockPersonalTracker.size());
        } catch (IOException ioe) {
            System.out.println("Exception while reading input " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }

        }
        AutoCompleteTextView editText = findViewById(R.id.searchStock);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STOCKS);
        editText.setAdapter(adapter);
        turnSymbolSearchOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (turnSymbolSearchOn.isChecked()) {
                    symbolOn();
                } else {
                    nameOn();
                }
            }
        });
    }

    //search by symbol
    public void symbolOn() {
        AutoCompleteTextView editText = findViewById(R.id.searchStock);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STOCKS_SYMBOLS);
        editText.setAdapter(adapter);
    }

    //search by full name
    public void nameOn() {
        AutoCompleteTextView editText = findViewById(R.id.searchStock);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STOCKS);
        editText.setAdapter(adapter);
    }

    /**
     * stock to check against hash map to see if it exists
     *
     * @param stockToSearch user input
     * @return true if the stock does exist in the hashmap, false otherwise.
     */
    public boolean doesStockExist(String stockToSearch) {
        for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
            if (stockToSearch.toLowerCase().equals(stockEntry.getKey().toLowerCase()) ||
                    stockToSearch.toLowerCase().equals(stockEntry.getValue().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks to see if the stock input is equivalent to a stock in the hashmap
     *
     * @param stockToSearch the users input
     * @return the stock symbol part of the url
     */
    public String findStockinMap(String stockToSearch) {
        String URLtoSend = "";
        for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
            if (stockToSearch.toLowerCase().equals(stockEntry.getKey().toLowerCase()) ||
                    stockToSearch.toLowerCase().equals(stockEntry.getValue().toLowerCase())) {
                URLtoSend = stockEntry.getValue();
            }
        }
        return URLtoSend;
    }

    /**
     * Prints stock symbol (value) and stock name (key)
     */
    public void printStocks() {
        for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
            System.out.println("Key: " + stockEntry.getKey());
            System.out.println("Value: " + stockEntry.getValue());
        }
    }

    public static void main(String[] args) throws Exception {

    }

    /**
     * volley request to alphavantage api to get time series of the stock
     *
     * @param stockSymbol symbol of stock we are attaching to the url
     */
    public void request(String stockSymbol) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.alphavantage.co/query?function=" +
                "TIME_SERIES_INTRADAY&symbol=" + stockSymbol + "&interval=1min&outputsize=compact&apikey=" +
                "V4DS7488LETWBK03";

// Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loadStockInformation(response);
                            isStockSelected = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonRequest);

    }

    public void requestSymbolSearch(String search) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + search +
                "&apikey=V4DS7488LETWBK03";

// Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray bestMatches = response.getJSONArray("bestMatches");
                            for (int i = 0; i < bestMatches.length(); i++) {
                                JSONObject match = bestMatches.getJSONObject(i);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonRequest);
    }

    @SuppressLint("SetTextI18n")
    public void loadStockInformation(JSONObject handleResponse) throws JSONException {
        TextView stockInfoText = findViewById(R.id.textStockInformation);
        System.out.println("RESPONSE: " + handleResponse);
        System.out.println("Meta Data: " + handleResponse.get("Meta Data"));
        metaData = handleResponse.getJSONObject("Meta Data");
        String lastRefresh = metaData.get("3. Last Refreshed").toString();
        symbol = metaData.get("2. Symbol").toString();
        refreshes = handleResponse.getJSONObject("Time Series (1min)");
        JSONObject currentRefresh = refreshes.getJSONObject(lastRefresh);
        System.out.println(currentRefresh);
        price = currentRefresh.getDouble("1. open");
        stockInfoText.setText("SYMBOL: " + symbol + "  " + "PRICE: " + price);
        updateOwnedAmount();
    }

    public void buy(int numShares) {
        String searchInPersonal = "";
        if (price * numShares <= balance) {
            balance -= (price * numShares);
            System.out.println(symbol);
            for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
                if (stockEntry.getValue().equals(symbol)) {
                    searchInPersonal = stockEntry.getValue();
                }
            }
            for (Map.Entry<String, Integer> stockEntry : stockPersonalTracker.entrySet()) {
                if (searchInPersonal.equals(stockEntry.getKey())) {
                    System.out.println("KEY :" + stockEntry.getKey());
                    if (stockEntry.getKey().equals(symbol)) {
                        stockEntry.setValue(stockEntry.getValue() + numShares);
                        System.out.println("STOCK: " + stockEntry.getKey() + " CURRENT SHARES OWNED: " + stockEntry.getValue());
                        balanceInd.setText("Balance : $ " + balance);
                        break;
                    }
                }
            }
        }
        updateOwnedAmount();
    }

    public void sell(int numShares) {
        String searchInPersonal = "";
        for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
            if (stockEntry.getValue().equals(symbol)) {
                searchInPersonal = stockEntry.getValue();
            }
        }
        for (Map.Entry<String, Integer> stockEntry : stockPersonalTracker.entrySet()) {
            if (searchInPersonal.equals(stockEntry.getKey())) {
                if (stockEntry.getValue() >= numShares) {
                    balance += (numShares * price);
                    stockEntry.setValue(stockEntry.getValue() - numShares);
                    System.out.println("STOCK: " + stockEntry.getKey() + " CURRENT SHARES OWNED: " + stockEntry.getValue());
                    balanceInd.setText("Balance : $ " + balance);
                    break;
                }
            }
        }
        updateOwnedAmount();
    }
    public void updateOwnedAmount() {
        TextView ownedStock = findViewById(R.id.txtamountOwned);
        String searchInPersonal = "";
        for (Map.Entry<String, String> stockEntry : stocks.entrySet()) {
            if (stockEntry.getValue().equals(symbol)) {
                searchInPersonal = stockEntry.getValue();
            }
        }
        for (Map.Entry<String, Integer> stockEntry : stockPersonalTracker.entrySet()) {
            if (searchInPersonal.equals(stockEntry.getKey())) {
                ownedStock.setText("Currently Own: " + stockEntry.getValue());
            }
        }
    }
}
package barber.startup.com.startup_barber;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import barber.startup.com.startup_barber.Utility.NetworkCheck;
import barber.startup.com.startup_barber.Utility.UserFavsAndCarts;

public class BarberActivity extends AppCompatActivity {

    private String[] b;
    private TextView tv;
    private ArrayList<ArrayList<ServiceDescriptionFormat>> completeBarberList = new ArrayList<>();

    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barber);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Cart");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        getServicesLeft();

        tv = (TextView) findViewById(R.id.textView);
        tv.setText("Barber Details \n");

        ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Data");
        parseQuery.fromPin("data");
        parseQuery.whereContainedIn("objectId", UserFavsAndCarts.listcart);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null) {
                    ArrayList<Map<Integer, ArrayList<Integer>>> barberSelectionList = new ArrayList<>();
                    for (int i = 0; i < objects.size(); i++) {

                        ArrayList<ServiceDescriptionFormat> arrayList = new ArrayList<>();
                        ArrayList<Integer> barberIds = new ArrayList<>();
                        JSONArray jsonArray = objects.get(i).getJSONArray("serviceDescription");
                        for (int j = 0; j < jsonArray.length(); j++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                ServiceDescriptionFormat serviceDescriptionFormat = new ServiceDescriptionFormat();
                                serviceDescriptionFormat.setBarberObjectId(jsonObject.getString("barberObjectId"));
                                serviceDescriptionFormat.setBarberName(jsonObject.getString("barberName"));
                                serviceDescriptionFormat.setBarberId(jsonObject.getInt("barberId"));
                                serviceDescriptionFormat.setServicePrice(jsonObject.getInt("servicePrice"));
                                serviceDescriptionFormat.setServiceTime(jsonObject.getInt("serviceTime"));
                                barberIds.add(jsonObject.getInt("barberId"));
                                arrayList.add(serviceDescriptionFormat);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }

                        Map<Integer, ArrayList<Integer>> map = new HashMap();
                        map.put(i, barberIds);
                        barberSelectionList.add(map);

                        completeBarberList.add(arrayList);
                    }

                    findBarbers(barberSelectionList);

                } else

                {
                    if (e.getCode() == 209) {
                        ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                            @Override
                            public void done(ParseException e) {
                                startActivity(new Intent(BarberActivity.this, Login.class));
                                finish();
                            }
                        });

                    } else Log.e("BarberActivity", e.getMessage());
                }

            }
        });

    }

    private void findBarbers(ArrayList<Map<Integer, ArrayList<Integer>>> barbersSelectionList) {
        ArrayList<Integer> barbersList = new ArrayList<>();
        for (int i = 0; i < barbersSelectionList.size(); i++) {

            Map<Integer, ArrayList<Integer>> map = barbersSelectionList.get(i);
            for (Integer key : map.keySet()) {
                if (i == 0) {
                    barbersList = map.get(key);
                } else
                    barbersList.retainAll(map.get(key));
            }


        }
        for (int i = 0; i < completeBarberList.size(); i++) {
            ArrayList<ServiceDescriptionFormat> serviceFormatList = completeBarberList.get(i);
            for (int j = 0; j < serviceFormatList.size(); j++) {
                ServiceDescriptionFormat serviceFormat = serviceFormatList.get(j);
                if (barbersList.contains(serviceFormat.getBarberId())) {
                    continue;
                } else {
                    serviceFormatList.remove(j);
                    j--;
                }
            }
        }

        updateTextView(completeBarberList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);

    }

    private void updateTextView(ArrayList<ArrayList<ServiceDescriptionFormat>> arrayList) {


        if (arrayList.size() == 0) {
            tv.append("No barber is providing cumulative services.");
        } else {

            List<ServiceDescriptionFormat> barberslist = new ArrayList<>();


            int j = 0;
            while (j < arrayList.get(0).size()) {

                ServiceDescriptionFormat data = new ServiceDescriptionFormat();
                int servicesPrice = 0;
                int servicesTime = 0;
                String barberName = null;
                int barberId = -1;
                for (int i = 0; i < arrayList.size(); i++) {
                    ArrayList<ServiceDescriptionFormat> newServiceList = arrayList.get(i);
                    ServiceDescriptionFormat serviceDescriptionFormat = newServiceList.get(j);
                    servicesPrice += serviceDescriptionFormat.getServicePrice();
                    servicesTime += serviceDescriptionFormat.getServiceTime();
                    barberName = serviceDescriptionFormat.getBarberName();
                    barberId = serviceDescriptionFormat.getBarberId();
                }

                data.setServicePrice(servicesPrice);
                data.setServiceTime(servicesTime);
                data.setBarberName(barberName);
                data.setBarberId(barberId);
                barberslist.add(data);

                // tv.append(barberName+" Price: "+servicesPrice+" Time: "+servicesTime+"\n");


                j++;
            }
            Adapter_Barber adapter_barber = new Adapter_Barber(BarberActivity.this, barberslist,recyclerView);
            recyclerView.setAdapter(adapter_barber);

        }

    }


    public void getServicesLeft() {

        if (NetworkCheck.checkConnection(BarberActivity.this)) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        Defaults.mNumberOfServicesLeft = object.getInt("rewardWallet");
                        Log.e("REWARD", String.valueOf(Defaults.mNumberOfServicesLeft));
                        if (Defaults.mNumberOfServicesLeft == 0) {
                            Snackbar.make(recyclerView, "You dont have any free service left", Snackbar.LENGTH_LONG).show();
                        } else if (Defaults.mNumberOfServicesLeft > 0) {
                            Snackbar.make(recyclerView, "You have " + Defaults.mNumberOfServicesLeft + " free service(s) left", Snackbar.LENGTH_LONG).show();
                        }
                    } else
                        Snackbar.make(recyclerView, e.getMessage(), Snackbar.LENGTH_SHORT).show();

                }
            });
        }
    }
}






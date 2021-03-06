package barber.startup.com.startup_barber;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import barber.startup.com.startup_barber.Utility.ToggleActionItemColor;
import barber.startup.com.startup_barber.Utility.UserFavsAndCarts;

public class Fragment_services_test extends android.support.v4.app.Fragment {

    List<Data> listparseobject = new ArrayList<>();
    ProgressDialog progressDialog = null;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private MainActivityAdapter adapter;
    private int category;
    private Menu menu;
    private ParseUser currentUser;
    private ProgressBar progressBar;
    private Context mContext;
    private boolean mUserDataChanged;
    private TextView checknet;
    private boolean dataChanged;


    public Fragment_services_test() {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        this.category = bundle.getInt("position");
        return inflater.inflate(R.layout.item_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_styles_fragment);
        progressBar = (ProgressBar) view.findViewById(R.id.data_loading_spinner);
        checknet = (TextView) view.findViewById(R.id.checkconnection);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager gaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);

        listparseobject.clear();

        currentUser = ParseUser.getCurrentUser();
        getFavCartIds();
        ;

    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void getFavCartIds() {
        Log.e("FRagment_services", "getFavCartIds called");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", currentUser.getObjectId());
        query.fromPin(ParseUser.getCurrentUser().getUsername());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    if (object != null) {
                        JSONArray arrayFav = object.getJSONArray("favLists");

                        if (arrayFav != null) {
                            if (arrayFav.length() > 0) {
                                if (mContext instanceof MainActivity) {
                                    menu = ((MainActivity) mContext).getMenu();
                                }
                                if (menu != null)
                                    new ToggleActionItemColor(menu, mContext).makeIconRed(R.id.action_fav);
                            } else if (arrayFav.length() == 0) {
                                if (mContext instanceof MainActivity) {
                                    menu = ((MainActivity) mContext).getMenu();
                                }
                                if (menu != null)
                                    new ToggleActionItemColor(menu, mContext).makeIconDefault(R.id.action_fav);
                            }
                            UserFavsAndCarts.listfav.clear();
                            for (int i = 0; i < arrayFav.length(); i++) {
                                try {
                                    UserFavsAndCarts.listfav.add(arrayFav.getString(i));
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        }

                        JSONArray arrayCart = object.getJSONArray("cartLists");
                        if (arrayCart != null) {
                            if (arrayCart.length() > 0) {
                                if (mContext instanceof MainActivity) {
                                    menu = ((MainActivity) mContext).getMenu();
                                }
                                if (menu != null)
                                    new ToggleActionItemColor(menu, mContext).makeIconRed(R.id.action_cart);
                            } else if (arrayCart.length() == 0) {
                                if (mContext instanceof MainActivity) {
                                    menu = ((MainActivity) mContext).getMenu();
                                }
                                if (menu != null)
                                    new ToggleActionItemColor(menu, mContext).makeIconDefault(R.id.action_cart);
                            }
                            UserFavsAndCarts.listcart.clear();

                            for (int i = 0; i < arrayCart.length(); i++) {
                                try {
                                    UserFavsAndCarts.listcart.add(arrayCart.getString(i));
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        }
                    }

                } else
                    Log.e("ERROR", e.getMessage() + " " + e.getCode());
                setUpRecyclerView();
            }
        });

    }

    public void setUpRecyclerView() {
        Log.e("This", "method is called");
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>(Defaults.INFO_CLASS);
        parseQuery.whereEqualTo("Category", category);
        parseQuery.fromPin("data");
        parseQuery.orderByDescending("updatedAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null && objects.size() > 0) {

                    for (int i = 0; i < objects.size(); i++) {

                        final ParseObject parseObject = objects.get(i);
                        final Data td = new Data();
                        td.title = parseObject.getString("title");
                        td.price = parseObject.getString("price");
                        td.id = parseObject.getObjectId();

                        ParseFile parseFile = parseObject.getParseFile("image");
                        if (parseFile != null)
                            td.url = parseFile.getUrl();


                        if (UserFavsAndCarts.listfav.contains(parseObject.getObjectId()))
                            td.fav = true;


                        if (UserFavsAndCarts.listcart.contains(parseObject.getObjectId()))
                            td.cart = true;
                        listparseobject.add(td);

                    }

                    adapter = new MainActivityAdapter(listparseobject, getActivity(), R.layout.card_item);
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);

                }


            }
        });

    }

}

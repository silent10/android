package com.evature.exampleapp;import android.content.Context;import android.content.Intent;import android.content.SharedPreferences;import android.graphics.Color;import android.os.Bundle;import android.os.Handler;import android.preference.PreferenceManager;import android.support.v4.app.ActivityCompat;import android.telecom.Call;import android.text.SpannableStringBuilder;import android.text.Spanned;import android.text.TextUtils;import android.text.method.LinkMovementMethod;import android.text.style.ForegroundColorSpan;import android.text.style.RelativeSizeSpan;import android.text.style.StyleSpan;import android.view.View;import android.support.design.widget.NavigationView;import android.support.v4.view.GravityCompat;import android.support.v4.widget.DrawerLayout;import android.support.v7.app.ActionBarDrawerToggle;import android.support.v7.app.AppCompatActivity;import android.support.v7.widget.Toolbar;import android.view.Menu;import android.view.MenuItem;import android.widget.ExpandableListView;import android.widget.TextView;import android.widget.Toast;import com.evature.evasdk.EvaChatTrigger;import com.evature.evasdk.appinterface.AppScope;import com.evature.evasdk.appinterface.AppSetup;import com.evature.evasdk.appinterface.CallbackResult;import com.evature.evasdk.appinterface.CarSearch;import com.evature.evasdk.appinterface.CruiseSearch;import com.evature.evasdk.appinterface.FlightNavigate;import com.evature.evasdk.appinterface.FlightSearch;import com.evature.evasdk.appinterface.HotelSearch;import com.evature.evasdk.appinterface.InitResult;import com.evature.evasdk.appinterface.PermissionsRequiredHandler;import com.evature.evasdk.evaapis.crossplatform.CruiseAttributes;import com.evature.evasdk.evaapis.crossplatform.EvaLocation;import com.evature.evasdk.evaapis.crossplatform.EvaTravelers;import com.evature.evasdk.evaapis.crossplatform.FlightAttributes;import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;import java.util.ArrayList;import java.util.Date;import java.util.HashMap;import java.util.List;import java.util.Random;import java.util.concurrent.Callable;import java.util.concurrent.FutureTask;public class MainActivity extends AppCompatActivity        implements NavigationView.OnNavigationItemSelectedListener,        InitResult, PermissionsRequiredHandler,        FlightNavigate, HotelSearch, CarSearch, FlightSearch, CruiseSearch {    ExpandableListAdapter listAdapter;    ExpandableListView expListView;    List<String> listDataHeader;    HashMap<String, List<CharSequence>> listDataChild;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_main);        TextView tv = (TextView) findViewById(R.id.mainTextView);        tv.setMovementMethod(LinkMovementMethod.getInstance());        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);        setSupportActionBar(toolbar);        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);        drawer.setDrawerListener(toggle);        toggle.syncState();        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);        navigationView.setNavigationItemSelectedListener(this);        expListView = (ExpandableListView) findViewById(R.id.lvExp);        listDataHeader = new ArrayList<String>();        listDataChild = new HashMap<String, List<CharSequence>>();        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);        expListView.setAdapter(listAdapter);        AppSetup.evaLogs(true);        // this will add a default float-action button        EvaChatTrigger.addDefaultButton(this);    }    @Override    public void onBackPressed() {        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);        if (drawer.isDrawerOpen(GravityCompat.START)) {            drawer.closeDrawer(GravityCompat.START);        } else {            super.onBackPressed();        }    }    @Override    public void onResume() {        super.onResume();        // normally your app credentials won't change and would be hard-coded once when the application loads        // eg.  something like  AppSetup.initEva('my-site-code', 'my-api-key', ...);        // this app allows changing the credentials...        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        TextView tv = (TextView) findViewById(R.id.mainTextView);        String siteCode = prefs.getString("site_code", null);        String apiKey = prefs.getString("api_key", null);        if (apiKey == null || siteCode == null) {            tv.setText(R.string.no_credentials);            return;        }        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);        ((TextView)nav.getHeaderView(0).findViewById(R.id.textViewNavigationSubHeader)).setText("Site Code: "+siteCode);        if (apiKey.equals(AppSetup.apiKey) == false  || siteCode.equals(AppSetup.siteCode) == false) {            tv.setText(R.string.checking_credentials);            AppSetup.initEva(apiKey, siteCode, this);  // the check results will be to the "initResult" callback        }        else {            tv.setVisibility(View.GONE);        }        // update scope menu from preferences        Menu menu = nav.getMenu();        menu.findItem(R.id.nav_car).setChecked(prefs.getBoolean("scope_car", false));        menu.findItem(R.id.nav_flight).setChecked(prefs.getBoolean("scope_flight", false));        menu.findItem(R.id.nav_cruise).setChecked(prefs.getBoolean("scope_cruise", false));        menu.findItem(R.id.nav_hotel).setChecked(prefs.getBoolean("scope_hotel", false));    }    @Override    public boolean onCreateOptionsMenu(Menu menu) {        // Inflate the menu; this adds items to the action bar if it is present.        getMenuInflater().inflate(R.menu.main, menu);        return true;    }    @Override    public boolean onOptionsItemSelected(MenuItem item) {        // Handle action bar item clicks here. The action bar will        // automatically handle clicks on the Home/Up button, so long        // as you specify a parent activity in AndroidManifest.xml.        int id = item.getItemId();        //noinspection SimplifiableIfStatement        if (id == R.id.action_settings) {            Intent intent = new Intent(this, SettingsActivity.class);            this.startActivity(intent);            return true;        }        if (id == R.id.voice_search) {            EvaChatTrigger.startSearchByVoice(MainActivity.this);            return true;        }        return super.onOptionsItemSelected(item);    }    private AppScope[] getScopeFromPreference() {        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        ArrayList<AppScope> scopeList = new ArrayList<>();        if (prefs.getBoolean("scope_car", false)) {            scopeList.add(AppScope.Car);        }        if (prefs.getBoolean("scope_cruise", false)) {            scopeList.add(AppScope.Cruise);        }        if (prefs.getBoolean("scope_flight", false)) {            scopeList.add(AppScope.Flight);        }        if (prefs.getBoolean("scope_hotel", false)) {            scopeList.add(AppScope.Hotel);        }        return scopeList.toArray(new AppScope[scopeList.size()]);    }    @SuppressWarnings("StatementWithEmptyBody")    @Override    public boolean onNavigationItemSelected(MenuItem item) {        // Handle navigation view item clicks here.        int id = item.getItemId();        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        SharedPreferences.Editor edit = prefs.edit();        // normally your app scope won't change and would be hard-coded once when the application loads,        // eg.  something like  AppSetup.setScope(AppScope.Flight, AppScope.Hotel);        // for example purposes we allow changing the app scope        if (id == R.id.nav_hotel || id == R.id.nav_flight || id == R.id.nav_car || id == R.id.nav_cruise) {            item.setChecked(!item.isChecked());            switch (id) {                case R.id.nav_car:                    edit.putBoolean("scope_car", item.isChecked());                    break;                case R.id.nav_cruise:                    edit.putBoolean("scope_cruise", item.isChecked());                    break;                case R.id.nav_flight:                    edit.putBoolean("scope_flight", item.isChecked());                    break;                case R.id.nav_hotel:                    edit.putBoolean("scope_hotel", item.isChecked());                    break;            }            edit.commit();            AppScope[] scopeArr = getScopeFromPreference();            AppSetup.setScope(scopeArr);        } else {            if (id == R.id.nav_send) {                // TODO: handle send feedback            }            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);            drawer.closeDrawer(GravityCompat.START);            return true;        }        return false;    }    // utility class to show some data on the screen in an ExpandableList    private class DataShower {        ArrayList<CharSequence> rows;        DataShower(String header) {            String headerIndexed  = (listDataHeader.size()+1) + ". "+header;            listDataHeader.add(headerIndexed);            rows = new ArrayList<>();            listDataChild.put(headerIndexed, rows);            listAdapter.notifyDataSetChanged();        }        <T> void addText(String title, T text) {            SpannableStringBuilder builder= new SpannableStringBuilder();            StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);            builder.append(title+": ", boldSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)                    .append(String.valueOf(text));            rows.add(builder);            listAdapter.notifyDataSetChanged();        }        void addList(String title, String delimiter, Object... data) {            addText(title, TextUtils.join(delimiter, data));        }    }    @Override    public void initResult(final String err, final Exception e) {        Handler mainHandler = new Handler(this.getMainLooper());        Runnable myRunnable = new Runnable() {            @Override            public void run() {                DataShower data = new DataShower("Init Result - " + (err == null ? "OK" : "Err"));                if (err != null) {                    data.addText("Err", err);                }                if (e != null) {                    data.addText("Excp", e.toString());                }                TextView tv = (TextView) findViewById(R.id.mainTextView);                if (err != null) {                    tv.setText("Error: "+err);                }                else {                    tv.setVisibility(View.GONE);                }            }        };        mainHandler.post(myRunnable);    }    @Override    public CallbackResult handleCruiseSearch(Context context, boolean isComplete, EvaLocation from, EvaLocation to, Date dateFrom, Date dateTo, Integer durationMin, Integer durationMax, CruiseAttributes attributes, RequestAttributes.SortEnum sortBy, RequestAttributes.SortOrderEnum sortOrder) {        DataShower data = new DataShower("Cruise Search Callback "+(isComplete ? " Done!" : " (ongoing)"));        data.addText("isComplete", isComplete);        if (from != null)            data.addText("from", from.Name);        if (to != null)            data.addText("to", to.Name);        if (dateFrom != null) {            data.addList("date", " - ", dateFrom, dateTo);        }        if (durationMin != null) {            data.addList("duration", " - ", durationMin, durationMax);        }        return CallbackResult.countResult( new FutureTask(new Callable() {            public Integer call() {                try {                    Thread.sleep(800); // fake getting data from slow server                } catch (InterruptedException e) {                    return null;                }                Random r = new Random();                int count = r.nextInt(200) - 100;                if (count < -50) count = 0;                if (count < 0) count = 1;                return count;            }        }));    }    @Override    public CallbackResult handleFlightSearch(Context context, boolean isComplete, EvaLocation origin, EvaLocation destination, Date departDateMin, Date departDateMax, Date returnDateMin, Date returnDateMax, EvaTravelers travelers, Boolean nonstop, FlightAttributes.SeatClass[] seatClass, String[] airlines, Boolean redeye, FlightAttributes.FoodType food, FlightAttributes.SeatType seatType, RequestAttributes.SortEnum sortBy, RequestAttributes.SortOrderEnum sortOrder) {        DataShower data = new DataShower("Flight Search Callback "+(isComplete ? " Done!" : " (ongoing)"));        data.addText("isComplete", isComplete);        if (origin != null)            data.addText("origin", origin.airportCode());        if (destination != null)            data.addText("destionation", destination.airportCode());        return null;    }    @Override    public CallbackResult handleHotelSearch(Context context, boolean isComplete, EvaLocation location, Date arriveDateMin, Date arriveDateMax, Integer durationMin, Integer durationMax, EvaTravelers travelers, HotelAttributes attributes,  RequestAttributes.SortEnum sortBy, RequestAttributes.SortOrderEnum sortOrder) {        DataShower data = new DataShower("Hotel Search Callback "+(isComplete ? " Done!" : " (ongoing)"));        data.addText("isComplete", isComplete);        if (location != null) {            data.addText("location", location.Name + " ("+location.Latitude + ":"+location.Longitude+")");        }        if (arriveDateMin != null) {            data.addList("arrive", " - ", arriveDateMin, arriveDateMax);        }        if (durationMin != null) {            data.addList("duration", " - ", durationMin, durationMax);        }        if (travelers != null) {            data.addList("travelers", ", ", travelers.getAllAdults(), travelers.getAllChildren());        }        if (attributes.chains != null && attributes.chains.size() != 0) {            data.addList("chains", ", ", attributes.chains);        }        CallbackResult result = CallbackResult.defaultHandling();        Random r = new Random();        int count = r.nextInt(200) - 100;        if (count < -50) count = 0;        if (count < 0) count = 1;        result.setCountResult(count);        return result;    }    @Override    public CallbackResult navigateTo(Context context, FlightPageType page) {        DataShower data = new DataShower("Flight Navigate Callback - "+page);        data.addText("Page", page.toString());        SpannableStringBuilder builder= new SpannableStringBuilder();        switch (page) {            case BoardingPass:                builder.append("Your boarding pass: \n")                        .append("Flight 318U,  Terminal B, Gate 9, 9:32am", new RelativeSizeSpan(1.5f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);                return CallbackResult.textResult(builder);            case ArrivalTime:                CallbackResult immediate = CallbackResult.textResult("Your arrival time is ");                FutureTask<CallbackResult> delayed = new FutureTask(new Callable() {                    public CallbackResult call() {                        try {                            Thread.sleep(2000); // fake getting data from slow server                        } catch (InterruptedException e) {                            return null;                        }                        SpannableStringBuilder builder2= new SpannableStringBuilder();                        builder2.append("12:51pm", new ForegroundColorSpan(Color.YELLOW), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);                        CallbackResult delayed = CallbackResult.textResult(builder2);                        delayed.setAppendToExistingText(true); // appending to previous sayit                        return delayed;                    }                });                return CallbackResult.delayedResult(immediate, delayed);            case DepartureTime:                builder.append("Your departure time is ")                        .append("9:32am", new ForegroundColorSpan(Color.YELLOW), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)                        .append(", Please hurry!");                return CallbackResult.textResult(builder);            case BoardingTime:                builder.append("Your boarding time is ")                        .append("9:22am", new ForegroundColorSpan(Color.YELLOW), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)                        .append(", Please hurry!");                return CallbackResult.textResult(builder);            case Gate:                builder.append("Gate ")                        .append("9", new ForegroundColorSpan(Color.BLUE), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);                return CallbackResult.textResult(builder);            case Itinerary:                builder.append("Flight ")                        .append("318U", new ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)                        .append("from ABC to DEF");                return CallbackResult.textResult(builder);        }        return null;    }    @Override    public void handleMissingPermissions(String[] missingPermissions) {        DataShower ds = new DataShower("Missing Permissions");        ds.addList("perms", ", ", missingPermissions);        // Should we show an explanation?        if (ActivityCompat.shouldShowRequestPermissionRationale(this,                missingPermissions[0])) {            Toast.makeText(this, "Permissions are needed to search by voice", Toast.LENGTH_LONG).show();        }        ActivityCompat.requestPermissions(this,                missingPermissions,                1234); // don't care to listen to the request response    }}
package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.ParseObject;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.RecyclerAdapter;

import java.util.LinkedList;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class MainScreen extends AppCompatActivity{

    // Analytics Tracker
    AnalyticsTrackers analyticsTrackers;

    // ToolBar
    private FloatingActionButton seeMap;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    // Status Bar Colour
    private ValueAnimator statusBarAnimator;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;
    private int scrolledDistance = 0;               // for showing and hiding the fam
    private static final int SHOW_THRESHOLD = 20;
    private static final int HIDE_THRESHOLD = 50;

    // Main content (RecyclerView)
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
//    private SwipeRefreshLayout swipeRefreshLayout;
    private WaveSwipeRefreshLayout swipeRefreshLayout;

    // Empty list
    private TextView textNoReminder;
    private Button borderlessNewReminder;

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE = 0x001;
    private static final int CREATE_NEW_NOR_REMINDER_REQUEST_CODE = 0x002;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE = 0x003;
    private static final int SETTINGS_REQUEST_CODE = 0x004;
    private static final int LOGIN_REQUEST_CODE = 0x005;

    // Importante
    // DataList
    private List<Reminder> reminderList;

    // For custom Nav Drawer
    private AccountHeader drawerHeader = null;
    private Drawer drawer = null;
    private IProfile userProfile;
    // Identifiers
    private static final int LOCAL_USER_IDENTIFIER =    101;
    private static final int ONLINE_USER_IDENTIFIER =   102;
    private static final int ALL_IDENTIFIER =           11;
    private static final int GEO_IDENTIFIER =           12;
    private static final int NOR_IDENTIFIER =           13;
    private static final int DRAFT_IDENTIFIER =         14;
    private static final int VIEW_MAP_IDENTIFIER =      15;
    private static final int ABOUT_IDENTIFIER =         21;
    private static final int SUPPORT_IDENTIFIER =       22;
    private static final int FEEDBACK_IDENTIFIER =      51;
    private static final int SETTINGS_IDENTIFIER =      52;

    // Record the last time "Back" key was pressed, to implement "double-click-exit"
    private long firstBackPress;

    private static boolean isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // TODO: delete when implementing actual back functions
//        sendParseTestObject();

        initData();             // load from sharedPreferences list of reminders

        initView(savedInstanceState);       // Bundle for creating drawer header

        initEvent();

        checkServices();

        loadPref();             //using SharedPreferences
    }

    private void initData() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // TODO: get data from shared preferences

        // Trackers
        analyticsTrackers = AnalyticsTrackers.getInstance();

        reminderList = new LinkedList<>();
        // TODO: remove these and actually get the reminders
        reminderList.add(new Reminder(this).setTitle("Reminder 1"));
        reminderList.add(new Reminder(this).setTitle("Reminder 2"));
        reminderList.add(new Reminder(this).setTitle("Reminder 3"));
        reminderList.add(new Reminder(this).setTitle("Reminder 4"));
        reminderList.add(new Reminder(this).setTitle("Reminder 5"));
        reminderList.add(new Reminder(this).setTitle("Reminder 6"));
        reminderList.add(new Reminder(this).setTitle("Reminder 7"));
        reminderList.add(new Reminder(this).setTitle("Reminder 8"));


        // Nav Drawer
        // create user profile
        //TODO: if user is registered and logged, skip this step and go ahead to load the profile as the user profile
        userProfile = new ProfileDrawerItem()
                .withName(getResources().getString(R.string.nav_head_appname))
                .withEmail(getResources().getString(R.string.nav_local_email))
                .withIcon(ContextCompat.getDrawable(MainScreen.this, R.mipmap.ic_default_avatar))
                .withIdentifier(LOCAL_USER_IDENTIFIER);
    }

    private void initView(Bundle savedInstanceState) {
        // initialise StatusBar color
        if(Build.VERSION.SDK_INT >= 21){
            getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
            //TODO: decide whether to change the navigation bar color or not
//            getWindow().setNavigationBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
        }

        // The main layout ------ RecyclerView
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coor_layout);

//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
//        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);        // color scheme
        swipeRefreshLayout.setMaxDropHeight(300);           // TODO: figure out why this doesn't work
        swipeRefreshLayout.setWaveColor(Color.parseColor("#8bc34a"));
        swipeRefreshLayout.setShadowRadius(7);
        swipeRefreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTransparent));

        recyclerView = (RecyclerView) findViewById(R.id.recycler_layout);


        // this buttons takes user to a page
        // the blue one with a map icon
        // and display all the reminders on a map
        seeMap = (FloatingActionButton) findViewById(R.id.fab_see_map);

        // The two mini add buttons (in floating action menu)
        addNorReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_norreminder);
        addGeoReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_georeminder);

        newReminder = (FloatingActionMenu) findViewById(R.id.fam_add_new);
        newReminder.hideMenuButton(false);
        new Handler().postDelayed(new Runnable() {                      // fam show and hide animation
            @Override
            public void run() {
                newReminder.showMenuButton(true);
                newReminder.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(MainScreen.this, R.anim.jump_from_down));
                newReminder.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(MainScreen.this, R.anim.jump_to_down));
            }
        }, 300);

        // Empty list
        textNoReminder = (TextView) findViewById(R.id.text_no_reminder);
        borderlessNewReminder = (Button) findViewById(R.id.borderless_btn_new_reminder);

        // Toolbar, preferably not make any changes to that
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        /* Below is initialisation of gadgets on the screen */

        // Changes the color of status bar, with animation (using ValueAnimator)
        // will only happen if higher than Lollipop
        if(Build.VERSION.SDK_INT >= 21){
            statusBarAnimator = ValueAnimator.ofArgb
                    (ContextCompat.getColor(MainScreen.this, R.color.colorPrimary),
                            ContextCompat.getColor(MainScreen.this, R.color.colorPrimaryDark));
            statusBarAnimator.setDuration(500);
            statusBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                // how it works is that every time it updates, it goes to change the color by a little bit
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        getWindow().setStatusBarColor((Integer) statusBarAnimator.getAnimatedValue());
                    }
                }
            });
        }

        // AppBar Layout, the top area
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {        // when collapsed, do not enbale
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                swipeRefreshLayout.setEnabled(verticalOffset == 0);
                // only version higher than 21 (Lollipop) will be getting this status bar animation
                if (Build.VERSION.SDK_INT >= 21) {
                    if (verticalOffset < -150) {                // negative indicates it has been moved up
                        if (!isDark) {
                            statusBarAnimator.start();
                            isDark = true;
                        }
                    } else {
                        if (isDark) {
                            statusBarAnimator.reverse();
                            isDark = false;
                        }
                    }
                }
            }
        });

        // initialise drawer
        // create account header
        buildHeader(false, savedInstanceState);         // drawerHeader is built in this method

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .withStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary))
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(ALL_IDENTIFIER).withName(getString(R.string.nav_opt_all)).withIcon(R.drawable.ic_nav_all),
                        new PrimaryDrawerItem().withIdentifier(GEO_IDENTIFIER).withName(getString(R.string.nav_opt_geo)).withIcon(R.drawable.ic_nav_geo),
                        new PrimaryDrawerItem().withIdentifier(NOR_IDENTIFIER).withName(getString(R.string.nav_opt_nor)).withIcon(R.drawable.ic_nav_nor),
                        new PrimaryDrawerItem().withIdentifier(DRAFT_IDENTIFIER).withName(getString(R.string.nav_opt_draft)).withIcon(R.drawable.ic_nav_draft),
                        new PrimaryDrawerItem().withIdentifier(VIEW_MAP_IDENTIFIER).withName(getString(R.string.nav_opt_view_in_map)).withIcon(R.drawable.ic_nav_view_map).withSelectable(false),

                        new SectionDrawerItem().withName(getString(R.string.nav_sec_other)).withTextColor(ContextCompat.getColor(MainScreen.this, R.color.colorAccent)),
                        new SecondaryDrawerItem().withIdentifier(ABOUT_IDENTIFIER).withName(getString(R.string.nav_opt_about)).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(SUPPORT_IDENTIFIER).withName(getString(R.string.nav_opt_support)).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case ALL_IDENTIFIER:

                                break;
                            case GEO_IDENTIFIER:

                                break;
                            case NOR_IDENTIFIER:

                                break;
                            case DRAFT_IDENTIFIER:

                                break;
                            case VIEW_MAP_IDENTIFIER:
                                toWholeMap(false);
                                break;
                            case ABOUT_IDENTIFIER:
                                Intent toMyWebsite = new Intent(Intent.ACTION_VIEW);
                                Uri homePageUri = Uri.parse("http://tpeterw.github.io");
                                toMyWebsite.setData(homePageUri);
                                startActivity(toMyWebsite);
                                break;
                            case SUPPORT_IDENTIFIER:
                                Toast thank_msg = Toast.makeText(MainScreen.this, getString(R.string.support_thank_msg), Toast.LENGTH_LONG);
                                thank_msg.setGravity(Gravity.CENTER, 0, 0);
                                thank_msg.show();
                                break;
                            case FEEDBACK_IDENTIFIER:
                                String uriText = "mailto:peterwangtao0@hotmail.com"
                                                + "?subject=" + Uri.encode(getString(R.string.feedback_subject))
                                                + "&body=" + Uri.encode(getString(R.string.feedback_content));
                                Uri emailUri = Uri.parse(uriText);
                                Intent sendFeedbackEmail = new Intent(Intent.ACTION_SENDTO);                // this will only pop up the apps that can send e-mails
                                sendFeedbackEmail.setData(emailUri);                                             // do not use setType, it messes things up
                                try {
                                    startActivity(Intent.createChooser(sendFeedbackEmail, getString(R.string.send_feedback)));
                                }
                                catch (ActivityNotFoundException e){
                                    Snackbar.make(newReminder, getString(R.string.activity_not_fonud), Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null)
                                            .show();
                                }
                                break;
                            case SETTINGS_IDENTIFIER:
                                Intent toSettingScreen = new Intent(MainScreen.this, SettingScreen.class);
                                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                                break;
                        }

                        drawer.closeDrawer();
                        return true;
                    }
                })
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.nav_feedback)).withIdentifier(FEEDBACK_IDENTIFIER).withIcon(R.drawable.ic_nav_feedback).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.nav_setting)).withIdentifier(SETTINGS_IDENTIFIER).withIcon(R.drawable.ic_nav_setting).withSelectable(false)
                )
                .withSavedInstance(savedInstanceState)
                .build();
    }

    private void initEvent() {
        firstBackPress = System.currentTimeMillis() - 2000;             // in case some idiot just presses back button when they enters the app

        // use linear layout manager to set Recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(MainScreen.this, reminderList);
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {              // click event for each reminder item
            @Override
            public void onItemClick(View view, int position) {
                if(newReminder.isOpened())
                    newReminder.close(true);
                else {
                    // TODO: do a check, which edit screen to go to
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if(newReminder.isOpened())
                    newReminder.close(true);

                // get the reminder
                String currentTitle = adapter.getItem(position).getTitle();

                // to alert the user about deleting by vibrating
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                // TODO: add code
                AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                builder.setTitle(currentTitle)
                        .setItems(new String[]{getString(R.string.dialog_edit_title), getString(R.string.dialog_share_title), getString(R.string.dialog_delete_title)},
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:     // edit button
                                        Toast.makeText(MainScreen.this, "Edit", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:     // share button
                                        showShareDialog(position);
                                        break;
                                    case 2:     // delete button
                                        showDeleteDialog(position);
                                        break;
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                // vibrate, TODO: check disable vibration
                vibrator.vibrate(20);
                dialog.show();

                //TODO: also after adding one, remember to hide these two views
            }
        });

        recyclerView.setAdapter(adapter);
        // set hide and show animation when user scrolls
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > HIDE_THRESHOLD && !newReminder.isMenuHidden()) {
                    newReminder.hideMenu(true);
                    scrolledDistance = 0;               // if menu is hidden, reset the scrolledDistance
                } else if (scrolledDistance < -SHOW_THRESHOLD && newReminder.isMenuHidden()) {
                    newReminder.showMenu(true);
                    scrolledDistance = 0;               // ditto here
                }

                if ((!newReminder.isMenuHidden() && dy > 0) || (newReminder.isMenuHidden() && dy < 0)) {
                    scrolledDistance += dy;
                }
            }
        });


        // add dividers
        // Currently not needed


        // Set up Swipe to Refresh
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {           // this is a very dirty workaround for the build tool support problem
                appBarLayout.setExpanded(false, true);
                recyclerView.setNestedScrollingEnabled(false);
                setTitle(getString(R.string.title_syncing));
                toolbar.setTitle(getString(R.string.title_syncing));
            }
        });


        // this buttons takes user to a page
        // the blue one with a map icon
        // and display all the reminders on a map
        seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toWholeMap(true);
            }
        });

        // The two mini add buttons (in floating action menu)
        addNorReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getString(R.string.bundle_with_map), false);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if(Build.VERSION.SDK_INT >= 21){
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                }
                else
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE);
            }
        });
        addGeoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getString(R.string.bundle_with_map), true);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });

        // Empty list
        borderlessNewReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newReminder = new Intent(MainScreen.this, EditorScreen.class);       // default is a new GeoReminder
                newReminder.putExtra(getString(R.string.bundle_with_map), true);
                //TODO: more specifications

                if(Build.VERSION.SDK_INT >= 21){
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                }
                else
                    startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
        if(reminderList.size() != 0) {
            textNoReminder.setAlpha(0);
            borderlessNewReminder.setAlpha(0);
            borderlessNewReminder.setClickable(false);
        }
    }

    private void loadPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //TODO: remember to check if the user wants to use amap instead of gms
    }

    private void checkServices() {
        // not sure which version of code is correct
//        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainScreen.this)){
//            case ConnectionResult.API_UNAVAILABLE:
//                break;
//        }
        //TODO: check if the user wants to use Amap first, before checking google play services

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())){
            case ConnectionResult.SUCCESS:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), true);
                break;

            case ConnectionResult.API_UNAVAILABLE:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                //TODO: only show this message once
                Toast.makeText(MainScreen.this, getString(R.string.svcs_unavail), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_DISABLED:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_disabled), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_MISSING:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_missing), Toast.LENGTH_SHORT).show();
                break;
            
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_req_update), Toast.LENGTH_SHORT).show();
                break;

            default:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_other), Toast.LENGTH_SHORT).show();
                break;
        }
        editor.apply();

        //TODO: check other availabilities such as Internet connection
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent toSettingScreen = new Intent(MainScreen.this, SettingScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            // TODO: check if list is till empty, otherwise hide the new reminder button and text
            case CREATE_NEW_GEO_REMINDER_REQUEST_CODE:
//                Bundle resultFromCreating = data.getExtras();
                return;
            case CREATE_NEW_NOR_REMINDER_REQUEST_CODE:

                return;

            case LOGIN_REQUEST_CODE:
                loadPref();
                //TODO: change avatar and sync all reminders
                return;

            case SETTINGS_REQUEST_CODE:
                loadPref();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(drawer.isDrawerOpen() || newReminder.isOpened()){
                    drawer.closeDrawer();
                    newReminder.close(true);
                    return true;
                }
                else {
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                        recyclerView.setNestedScrollingEnabled(true);
                        recyclerView.setScrollY(0);
                        appBarLayout.setExpanded(true, true);
                        setTitle(getString(R.string.app_name));     // change title back
                        toolbar.setTitle(getString(R.string.app_name));
                        return true;
                    }
                    else {
                        // if two presses differ from each other in time for more than 2 seconds
                        long currentBackPress = System.currentTimeMillis();         // then user has to press one more time
                        if((currentBackPress - firstBackPress) > 2000){
                            Snackbar snackbar = Snackbar.make(newReminder, getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null);
                            firstBackPress = currentBackPress;

                            snackbar.show();
                            return true;
                        }
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void buildHeader(boolean compact, Bundle savedInstanceState) {
        // Create the AccountHeader
        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimary)
                .withCompactStyle(compact)
                .addProfiles(
                        userProfile,
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
//                        new ProfileSettingDrawerItem().withName(getResources().getString(R.string.nav_acct_switch)).withDescription(getResources().getString(R.string.nav_desc_switch)).withIcon(R.drawable.ic_nav_add).withIdentifier(PROFILE_SETTING),
                        new ProfileSettingDrawerItem().withName(getString(R.string.nav_acct_manage)).withDescription(getString(R.string.nav_desc_manage))
                                .withIcon(R.drawable.ic_nav_manage).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
                                startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE);
                                return false;
                            }
                        })
                )
                .withSavedInstance(savedInstanceState)
                .withCloseDrawerOnProfileListClick(false)
                .build();
    }

    private void toWholeMap(Boolean animateExit) {
        Intent toViewWholeMap = new Intent(MainScreen.this, WholeMapScreen.class);
        //TODO: to check all the reminders and drafts

        if (Build.VERSION.SDK_INT >= 21) {
            if(animateExit){
                getWindow().setExitTransition(new Explode());
            }
            else {
                getWindow().setExitTransition(null);
            }
            getWindow().setReenterTransition(new Explode());
            startActivity(toViewWholeMap, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
        } else
            startActivity(toViewWholeMap);
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_delete_title))
                .setIcon(ContextCompat.getDrawable(MainScreen.this, R.drawable.ic_dialog_warning))
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.dialog_pos_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteReminder(position);
                        if (reminderList.size() == 0) {
                            textNoReminder.setAlpha(1);
                            borderlessNewReminder.setAlpha(1);
                            borderlessNewReminder.setClickable(true);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // let's do nothing
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showShareDialog(final int position){
        Intent shareWith = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "This is totally temporary").setType("text/plain");
        startActivity(Intent.createChooser(shareWith, getString(R.string.dialog_share_msg)));
    }

    // Below: code for testing and debugging




















    private void sendParseTestObject() {
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("Name", "Tao Peter Wang");
        testObject.put("Location", "NULL");
        Log.i("Cloud", "Sent Parse TestObject");
        testObject.saveInBackground();
    }
}

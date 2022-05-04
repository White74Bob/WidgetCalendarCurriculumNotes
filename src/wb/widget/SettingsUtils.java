package wb.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import wb.widget.WidgetMain.Show;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.Constellation;

public class SettingsUtils {
    private static final String PREF_NAME = "curriculum_calendar_settings";

    // Generic preferences.
    private static final String PREF_KEY_SHOW_VEHICLE_LIMIT = "show_vehicle_limit";
    private static final String PREF_KEY_SHOW_CONSTELLATION = "show_constellation";
    private static final String PREF_KEY_SHOW_CONSTELLATION_STONE = "show_constellation_stone";
    private static final String PREF_KEY_SHOW_CONSTELLATION_FLOWER = "show_constellation_flower";

    protected static final String PREF_KEY_VIEW_CURRENT = "current_show";
    protected static final String PREF_KEY_CALENDAR_VISIBLE = "calendar_visible";
    protected static final String PREF_KEY_CURRICULUM_VISIBLE = "curriculum_visible";
    protected static final String PREF_KEY_WEEK_NOTES_VISIBLE = "week_notes_visible";

    private static boolean sShowVehicleLimit;
    private static boolean sShowConstellation;
    private static boolean sShowConstellationStone;
    private static boolean sShowConstellationFlower;

    private static WidgetMain.Show sCurrentShow;

    public static SharedPreferencesHelper getPrefsHelper(final Context context) {
        return new SharedPreferencesHelper(context, PREF_NAME);
    }

    private static void saveShowVehicleLimitPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_VEHICLE_LIMIT, sShowVehicleLimit);
    }

    public static void saveShowVehicleLimitPreference(final Context context) {
        saveShowVehicleLimitPreference(getPrefsHelper(context));
    }

    private static void saveShowConstellationPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_CONSTELLATION, sShowConstellation);
    }

    public static void saveShowConstellationPreference(final Context context) {
        saveShowConstellationPreference(getPrefsHelper(context));
    }

    private static void saveShowConstellationStonePreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_CONSTELLATION_STONE, sShowConstellationStone);
    }

    private static void saveShowConstellationStonePreference(final Context context) {
        saveShowConstellationStonePreference(getPrefsHelper(context));
    }

    private static void saveViewShowPreference(final Context context, final Show show) {
        show.savePreference(getPrefsHelper(context));
    }

    private static void saveShowConstellationFlowerPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_CONSTELLATION_FLOWER, sShowConstellationFlower);
    }

    public static void saveShowConstellationFlowerPreference(final Context context) {
        saveShowConstellationFlowerPreference(getPrefsHelper(context));
    }

    public static void initShowVehicleLimit(final Context context, final Activity activity) {
        final CheckBox checkBoxShowVehicleLimit = activity
                .findViewById(R.id.checkbox_show_vehicle_limit);
        checkBoxShowVehicleLimit.setChecked(sShowVehicleLimit);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowVehicleLimit = isChecked;
                SettingsUtils.saveShowVehicleLimitPreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowVehicleLimit.setOnCheckedChangeListener(listener);
    }

    public static void initShowConstellation(final Context context, final Activity activity) {
        final View viewConstellation = activity.findViewById(R.id.view_constellation);
        viewConstellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConstellationInfo(context);
            }
        });

        final CheckBox checkBoxShowConstellation = activity
                .findViewById(R.id.checkbox_show_constellation);
        checkBoxShowConstellation.setChecked(sShowConstellation);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowConstellation = isChecked;
                SettingsUtils.saveShowConstellationPreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowConstellation.setOnCheckedChangeListener(listener);
    }

    public static void initShowConstellationStone(final Context context, final Activity activity) {
        final CheckBox checkBoxShowConstellationStone = activity.findViewById(
                R.id.checkbox_show_constellation_stone);
        checkBoxShowConstellationStone.setChecked(sShowConstellationStone);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowConstellationStone = isChecked;
                SettingsUtils.saveShowConstellationStonePreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowConstellationStone.setOnCheckedChangeListener(listener);
    }

    public static void initShowConstellationFlower(final Context context, final Activity activity) {
        final CheckBox checkBoxShowConstellationFlower = activity.findViewById(
                R.id.checkbox_show_constellation_flower);
        checkBoxShowConstellationFlower.setChecked(sShowConstellationFlower);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowConstellationFlower = isChecked;
                SettingsUtils.saveShowConstellationFlowerPreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowConstellationFlower.setOnCheckedChangeListener(listener);
    }

    public static void initViewVisibleCheckBoxes(final Context context, final Activity activity) {
        initViewVisible(context, activity, Show.CalendarMonth);
        initViewVisible(context, activity, Show.Curriculum);
        initViewVisible(context, activity, Show.WeekNotes);
    }

    private static void initViewVisible(final Context context, final Activity activity,
            final Show showRelated) {
        final CheckBox checkBoxViewVisible = activity.findViewById(showRelated.checkboxResId);
        checkBoxViewVisible.setChecked(showRelated.visible);
        checkBoxViewVisible.setEnabled(sCurrentShow != showRelated);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showRelated.visible = isChecked;
                saveViewShowPreference(context, showRelated);
                Constants.refreshWidget(context);
            }
        };
        checkBoxViewVisible.setOnCheckedChangeListener(listener);
    }

    private static void initAbout(final Context context, final Activity activity) {
        final View viewAbout = activity.findViewById(R.id.view_about);
        viewAbout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showSelectableInfo(context, R.string.about, R.string.about_info, true);
            }
        });
    }

    public static WidgetMain.Show loadShowCurrent(final SharedPreferencesHelper prefHelper) {
        if (sCurrentShow != null) return sCurrentShow;
        final String key = PREF_KEY_VIEW_CURRENT;
        final int showCurrent;
        if (prefHelper.contains(key)) {
            showCurrent = (Integer) prefHelper.get(key, WidgetMain.Show.CalendarMonth.ordinal());
        } else {
            showCurrent = 0;
            saveShowCurrentPreference(prefHelper, showCurrent);
        }
        sCurrentShow = WidgetMain.Show.get(showCurrent);
        return sCurrentShow;
    }

    public static void saveShowCurrentPreference(SharedPreferencesHelper prefHelper,
            final int showCurrent) {
        sCurrentShow = WidgetMain.Show.get(showCurrent);
        prefHelper.put(PREF_KEY_VIEW_CURRENT, showCurrent);
    }

    public static void saveShowCurrentPreference(final Context context, final int showCurrent) {
        saveShowCurrentPreference(getPrefsHelper(context), showCurrent);
    }

    public static void showConstellationInfo(final Context context) {
        final String title = context.getString(R.string.show_constellation);
        final String constellationInfo = Constellation.constellationInfo(context);
        Utility.showSelectableInfo(context, title, constellationInfo);
    }

    public static boolean loadShowVehicleLimit(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_VEHICLE_LIMIT;
        final boolean defaultValue = false;
        if (prefHelper.contains(key)) {
            sShowVehicleLimit = (Boolean) prefHelper.get(key, defaultValue);
        } else {
            sShowVehicleLimit = defaultValue;
            saveShowVehicleLimitPreference(prefHelper);
        }
        return sShowVehicleLimit;
    }

    public static boolean loadShowConstellation(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_CONSTELLATION;
        final boolean defaultValue = false;
        if (prefHelper.contains(key)) {
            sShowConstellation = (Boolean) prefHelper.get(key, defaultValue);
        } else {
            sShowConstellation = defaultValue;
            saveShowConstellationPreference(prefHelper);
        }
        return sShowConstellation;
    }

    public static boolean loadShowConstellationStone(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_CONSTELLATION_STONE;
        final boolean defaultValue = false;
        if (prefHelper.contains(key)) {
            sShowConstellationStone = (Boolean) prefHelper.get(key, defaultValue);
        } else {
            sShowConstellationStone = defaultValue;
            saveShowConstellationStonePreference(prefHelper);
        }
        return sShowConstellationStone;
    }

    public static boolean loadShowConstellationFlower(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_CONSTELLATION_FLOWER;
        final boolean defaultValue = false;
        if (prefHelper.contains(key)) {
            sShowConstellationFlower = (Boolean) prefHelper.get(key, defaultValue);
        } else {
            sShowConstellationFlower = defaultValue;
            saveShowConstellationFlowerPreference(prefHelper);
        }
        return sShowConstellationFlower;
    }

    public static void loadGeneric(final SharedPreferencesHelper prefHelper) {
        loadShowVehicleLimit(prefHelper);
        loadShowConstellation(prefHelper);
        loadShowConstellationStone(prefHelper);
        loadShowConstellationFlower(prefHelper);
        Show.loadPreferences(prefHelper);
    }

    public static void initGeneric(final Activity activity) {
        initAbout(activity, activity);
        initShowVehicleLimit(activity, activity);
        initShowConstellation(activity, activity);
        initShowConstellationStone(activity, activity);
        initShowConstellationFlower(activity, activity);
        initViewVisibleCheckBoxes(activity, activity);
    }
}

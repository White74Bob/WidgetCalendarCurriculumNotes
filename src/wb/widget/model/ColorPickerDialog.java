package wb.widget.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import wb.widget.R;
import wb.widget.utils.Reflector;
import wb.widget.utils.Reflector.StaticFieldInfo;

public class ColorPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

    public interface OnColorSetListener {
        public void onColorSet(int textColor, int backgroundColor);
    }

    private static enum TextStyle {
        Hex,
        Decimal,
        None;
    }

    private static class TextInfo {
        public TextStyle style;
        public final String text;
        public final int value;

        public TextInfo(String text) {
            this.text = text;
            value = -1;
            style = TextStyle.None;
        }

        public TextInfo(int value) {
            this.text = null;
            this.value = value;
            this.style = TextStyle.Decimal;
        }

        private String getText() {
            switch (style) {
                case Hex:
                    if (value >= 0) {
                        return Integer.toHexString(value).toUpperCase();
                    }
                    return text;
                case Decimal:
                    if (value >= 0) {
                        return Integer.toString(value);
                    }
                    return text;
                case None:
                default:
                    return null;
            }
        }

        public void setText(final TextView textView) {
            textView.setText(getText());
            if (style == TextStyle.Hex) {
                textView.setTextAppearance(R.style.color_text_hex);
            } else if (style == TextStyle.Decimal) {
                textView.setTextAppearance(R.style.color_text_decimal);
            }
        }

        public void switchStyle(final TextView textView) {
            if (TextUtils.isEmpty(text)) {
                if (style == TextStyle.Decimal) {
                    style = TextStyle.Hex;
                } else {
                    style = TextStyle.Decimal;
                }
            } else {
                if (style == TextStyle.Hex) {
                    style = TextStyle.None;
                } else {
                    style = TextStyle.Hex;
                }
            }
            setText(textView);
        }
    }

    private static class ColorInfo {
        private static final String FORMAT_SHORT_INFO = "%s %s";
        private static final String SYS_COLOR = "SYS";

        public final TextInfo red;
        public final TextInfo green;
        public final TextInfo blue;

        public final String name;
        public final String name_cn;
        public final TextInfo str_value;
        public final int int_color;

        public ColorInfo(String name, String name_cn, String str_value) {
            this.name = name;
            this.name_cn = name_cn;
            this.str_value = new TextInfo(str_value);

            int_color = Color.parseColor(str_value);
            red = new TextInfo(Color.red(int_color));
            green = new TextInfo(Color.green(int_color));
            blue = new TextInfo(Color.blue(int_color));
        }

        public ColorInfo(String name, int colorValue) {
            this.name = name;
            this.name_cn = SYS_COLOR;
            this.str_value = new TextInfo(Integer.toString(colorValue));

            int_color = colorValue;
            red = new TextInfo(Color.red(int_color));
            green = new TextInfo(Color.green(int_color));
            blue = new TextInfo(Color.blue(int_color));
        }

        public boolean isSysColor() {
            return name_cn == SYS_COLOR;
        }

        public boolean isSameColor(int color) {
            return Color.rgb(red.value, green.value, blue.value) == color;
        }

        public String shortInfo() {
            return String.format(FORMAT_SHORT_INFO, name, name_cn);
        }

        public int getColor() {
            // return Color.rgb(red.value, green.value, blue.value);
            return int_color;
        }

        private int getReverseColor() {
            if (red.value == green.value) {
                if (blue.value == red.value) {
                    return Color.rgb(Utils.randomInt(0, 255), Utils.randomInt(0, 255),
                            Utils.randomInt(0, 255));
                }
                return Color.rgb(Utils.randomInt(0, 255), Utils.randomInt(0, 255),
                        255 - blue.value);
            }
            if (red.value == blue.value) {
                return Color.rgb(Utils.randomInt(0, 255), 255 - green.value,
                        Utils.randomInt(0, 255));
            }
            if (blue.value == green.value) {
                return Color.rgb(255 - red.value, Utils.randomInt(0, 255),
                        Utils.randomInt(0, 255));
            }
            return Color.rgb(255 - red.value, 255 - green.value, 255 - blue.value);
        }

        public static ColorInfo parseColorInfo(final String input) {
            if (TextUtils.isEmpty(input)) return null;
            final String sep = ",";
            String[] elements = input.split(sep);
            String name = elements[0];
            String name_cn = elements[2];
            String strValue = elements[1];
            return new ColorInfo(name, name_cn, strValue);
        }
    }

    private enum ColorType {
        RED(Color.RED, 16), GREEN(Color.GREEN, 8), BLUE(Color.BLUE, 0);

        private final int mPureSingleColor;

        private final int mMovingBitCount;

        private ColorType(final int pureSingleColor, final int movingBitCount) {
            mPureSingleColor = pureSingleColor;
            mMovingBitCount = movingBitCount;
        }

        // 获得当前color的RGB分量颜色,
        // 保留alpha value, 其它颜色分量为0.
        public int getSingleColor(final int color) {
            return color & mPureSingleColor;
        }

        // 获得当前color的RGB分量value, 返回值不是颜色.
        // return value is between 0 and 255.
        public int getSingleColorValue(final int color) {
            if (mMovingBitCount > 0) {
                return (color & (0xFF << mMovingBitCount)) >> mMovingBitCount;
            }
            return (color & 0xFF);
        }

        // 根据single color value更新color
        // single color value should be in [0, 255].
        public int getUpdatedColorWithSingleColorValue(int color, final int singleColorValue) {
            if (singleColorValue > 0xFF || singleColorValue < 0) {
                throw new RuntimeException("value = " + Utils.getIntHex(singleColorValue)
                + ", NOT in [0, 255]!");
            }
            color &= ~(0xFF << mMovingBitCount); // 对应的颜色分量清零.
            if (mMovingBitCount > 0) {
                return (color | (singleColorValue << mMovingBitCount));
            }
            return (color | singleColorValue);
        }
    }

    private class ColorArea {
        private RelativeLayout mAreaView;
        private EditText mEditValue;
        private SeekBar mSeekBar;

        private final int mAreaViewResId;
        private final int mEditValueResId;
        private final int mSeekBarResId;

        private final ColorType mColorType;

        private final int mTextViewResId;
        private TextView mTextView;

        public ColorArea(ColorType colorType, int areaViewResId, int editValueResId,
                int seekBarResId, int textViewId) {
            mColorType = colorType;
            mAreaViewResId = areaViewResId;
            mEditValueResId = editValueResId;
            mSeekBarResId = seekBarResId;
            mTextViewResId = textViewId;
        }

        public void init(final View rootView) {
            mAreaView = (RelativeLayout) rootView.findViewById(mAreaViewResId);
            mEditValue = (EditText) rootView.findViewById(mEditValueResId);
            mEditValue.setKeyListener(mKeyListener);
            mEditValue.addTextChangedListener(new ColorTextWatcher(this));

            mSeekBar = (SeekBar) rootView.findViewById(mSeekBarResId);
            mSeekBar.setOnSeekBarChangeListener(new ColorSeekBarChangeListener(this));

            mTextView = (TextView) rootView.findViewById(mTextViewResId);
        }

        public void updateArea(final int color) {
            int singleColorValue = mColorType.getSingleColorValue(color);
            mEditValue.setText(Integer.toString(singleColorValue));
        }

        private void refreshColor(final int singleColorValue, final boolean fromSeekBarChange) {
            final int selectedColor = getSelectedColor();
            final int updatedColor = mColorType.getUpdatedColorWithSingleColorValue(selectedColor,
                    singleColorValue);
            int singleColor = mColorType.getSingleColor(updatedColor);
            mAreaView.setBackgroundColor(singleColor);
            mTextView.setText(Utils.getIntHex(singleColor));
            if (fromSeekBarChange) {
                mEditValue.setText(Integer.toString(singleColorValue));
            } else {
                mSeekBar.setProgress(singleColorValue);
            }
            updateSelectedColor(updatedColor);
        }
    }

    private class ColorTextWatcher implements TextWatcher {
        private final ColorArea mColorArea;

        public ColorTextWatcher(ColorArea colorArea) {
            mColorArea = colorArea;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int value = Utils.getIntFromCharSequence(s);
            if (value > 255) {
                return;
            }
            mColorArea.refreshColor(value, false);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

    private class ColorSeekBarChangeListener implements OnSeekBarChangeListener {
        private final ColorArea mColorArea;

        public ColorSeekBarChangeListener(ColorArea colorArea) {
            mColorArea = colorArea;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mColorArea.refreshColor(progress, true);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private final ColorArea[] mColorAreas = {
            new ColorArea(ColorType.RED,   R.id.red_area,   R.id.edit_red,   R.id.red_seek_bar,   R.id.text_red),
            new ColorArea(ColorType.GREEN, R.id.green_area, R.id.edit_green, R.id.green_seek_bar, R.id.text_green),
            new ColorArea(ColorType.BLUE,  R.id.blue_area,  R.id.edit_blue,  R.id.blue_seek_bar,  R.id.text_blue),
    };

    private final NumberKeyListener mKeyListener = (new NumberKeyListener() {
        private final char[] numChars = {
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
        };

        @Override
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER;
        }

        @Override
        protected char[] getAcceptedChars() {
            return numChars;
        }
    });

    private RadioGroup mRadioGroupColorArea;

    private final PreviewInfo mPreviewInfo = new PreviewInfo(R.id.demo_view);

    private final ColorSettings mColorSettings = new ColorSettings();

    private TextView mTextViewColor;
    private TextView mTextViewColorInfo;
    private TextView mTextViewSysColorInfo;

    private final ArrayList<ColorInfo> mColors = new ArrayList<ColorInfo>();

    private ListView mListViewColors;

    private OnColorSetListener mColorSetListener;

    public ColorPickerDialog(Context context, OnColorSetListener colorSetListener,
            String text, int textColor, int backgroundColor) {
        super(context);

        mColorSetListener = colorSetListener;

        final Context themeContext = getContext();
        final View view = View.inflate(themeContext, R.layout.color_settings, null);
        setView(view);

        setButton(BUTTON_POSITIVE, themeContext.getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, themeContext.getString(android.R.string.cancel), this);
        // setButtonPanelLayoutHint(1/*LAYOUT_HINT_SIDE*/);

        mPreviewInfo.initViews(view, text, textColor, backgroundColor);
        initColorSettings();
        initViews(view);

        loadColorsAsync();

        previewColorSettings();
    }

    private void initColorSettings() {
        ColorSettings previewColorSettings = mPreviewInfo.getColorSettings();
        mColorSettings.color_background = previewColorSettings.color_background;
        mColorSettings.color_text = previewColorSettings.color_text;
    }

    private void initColorList() {
        mListViewColors = (ListView) findViewById(R.id.list_colors);
        mListViewColors.setAdapter(new ColorInfoListAdapter(getContext()));

        OnItemClickListener colorClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                final ColorInfo colorInfo = mColors.get(position);
                final int color = colorInfo.getColor();
                updateAreasColor(color);
                updateColorInfo(color);
            }
        };
        mListViewColors.setOnItemClickListener(colorClickListener);
    }

    public void setOnDateSetListener(OnColorSetListener listener) {
        mColorSetListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mColorSetListener != null) {
                    mColorSetListener.onColorSet(mColorSettings.color_text,
                            mColorSettings.color_background);
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    private void loadColorsAsync() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mColors.clear();
                loadSysColors();
                loadFileFromAssets("android_colors.txt");
                return null;
            }

            private void loadSysColors() {
                StaticFieldInfo[] fieldsInfo = Reflector.getPublicStaticFields(Color.class);
                String fieldName;
                int color;
                for (StaticFieldInfo fieldInfo : fieldsInfo) {
                    color = (Integer) fieldInfo.value;
                    if (color == Color.TRANSPARENT) continue;
                    fieldName = SysColorInfo.getFieldName(fieldInfo.fieldName);
                    mColors.add(new ColorInfo(fieldName, color));
                }
            }

            private void loadFileFromAssets(final String filename) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(getContext().getAssets().open(filename)));

                    // Loop reading the asset file until end of file.
                    String line;
                    ColorInfo colorInfo;
                    while ((line = reader.readLine()) != null) {
                        colorInfo = ColorInfo.parseColorInfo(line);
                        if (colorInfo != null) {
                            mColors.add(colorInfo);
                        }
                    }
                } catch (IOException ioe) {
                    // log the exception
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ioe) {
                            // log the exception
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                initColorList();
                updateColorInfo(getSelectedColor());
                super.onPostExecute(result);
            }
        }).execute();
    }

    private void updateColorInfo(final int color) {
        String colorInfo = findColorInfo(color);
        mTextViewColorInfo.setText(colorInfo);

        String sysColorName = SysColorInfo.findSysColorInfo(color);
        if (TextUtils.isEmpty(sysColorName)) {
            mTextViewSysColorInfo.setVisibility(View.INVISIBLE);
        } else {
            mTextViewSysColorInfo.setText(sysColorName);
            mTextViewSysColorInfo.setVisibility(View.VISIBLE);
        }
    }

    private void initViews(final View rootView) {
        mTextViewColor = (TextView) rootView.findViewById(R.id.text_color);
        mTextViewColorInfo = (TextView) rootView.findViewById(R.id.text_color_info);
        mTextViewSysColorInfo = (TextView) rootView.findViewById(R.id.text_sys_color_info);
        initAreas(rootView);

        mRadioGroupColorArea = (RadioGroup) rootView.findViewById(R.id.radio_select_view);
        mRadioGroupColorArea.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                updateAreasColor();
            }
        });
        updateAreasColor();
    }

    private void initAreas(final View rootView) {
        for (ColorArea colorArea : mColorAreas) {
            colorArea.init(rootView);
        }
    }

    private void updateAreasColor() {
        updateAreasColor(getSelectedColor());
    }

    private void updateAreasColor(final int color) {
        for (ColorArea colorArea : mColorAreas) {
            colorArea.updateArea(color);
        }
    }

    private int getSelectedColor() {
        return getSelectedColor(mRadioGroupColorArea.getCheckedRadioButtonId());
    }

    private int getSelectedColor(final int checkedRadioId) {
        switch (checkedRadioId) {
            case R.id.radio_background_color:
                return mColorSettings.color_background;
            case R.id.radio_text_color:
            default:
                return mColorSettings.color_text;
        }
    }

    private void updateSelectedColor(final int color) {
        switch (mRadioGroupColorArea.getCheckedRadioButtonId()) {
            case R.id.radio_background_color:
                mColorSettings.color_background = color;
                break;
            case R.id.radio_text_color:
                mColorSettings.color_text = color;
                break;
            default:
                return;
        }
        previewColorSettings();
    }

    private static String getSelectedColorId(final int checkedRadioId) {
        switch (checkedRadioId) {
            case R.id.radio_background_color:
                return "radio_background_color";
            case R.id.radio_text_color:
                return "radio_text_color";
            default:
                return Integer.toString(checkedRadioId);
        }
    }

    private static final String FORMAT_COLOR = "%s, %s";

    private void previewColorSettings() {
        final int checkedRadioId = mRadioGroupColorArea.getCheckedRadioButtonId();
        final int color = getSelectedColor(checkedRadioId);

        final String checkedColorId = getSelectedColorId(checkedRadioId);
        String colorText = String.format(FORMAT_COLOR, checkedColorId, Utils.toARGBString(color));
        mTextViewColor.setText(colorText);
        updateColorInfo(color);

        boolean isTextColor = checkedRadioId == R.id.radio_text_color;
        mPreviewInfo.previewColorSettings(color, isTextColor);
    }

    private String findColorInfo(final int color) {
        StringBuilder sb = new StringBuilder();
        for (ColorInfo colorInfo : mColors) {
            if (colorInfo.isSysColor()) continue;
            if (colorInfo.isSameColor(color)) {
                if (sb.length() > 0) sb.append(';');
                sb.append(colorInfo.shortInfo());
            }
        }
        if (sb.length() > 0) return sb.toString();
        return null;
    }

    private class ColorInfoListAdapter extends BaseAdapter {
        private final Context mContext;

        public ColorInfoListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mColors.size();
        }

        @Override
        public Object getItem(int position) {
            return mColors.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ColorInfoViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.color_item, null);

                viewHolder = new ColorInfoViewHolder();
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.color_name);
                viewHolder.name_cnView = (TextView) convertView.findViewById(R.id.color_name_cn);
                viewHolder.colorView = (TextView) convertView.findViewById(R.id.color_demo);
                viewHolder.redView = (TextView) convertView.findViewById(R.id.color_red);
                viewHolder.greenView = (TextView) convertView.findViewById(R.id.color_green);
                viewHolder.blueView = (TextView) convertView.findViewById(R.id.color_blue);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ColorInfoViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final ColorInfoViewHolder viewHolder, final int position) {
            final ColorInfo item = (ColorInfo) getItem(position);
            viewHolder.nameView.setText(item.name);
            viewHolder.name_cnView.setText(item.name_cn);
            item.str_value.setText(viewHolder.colorView);
            viewHolder.colorView.setBackgroundColor(item.getColor());
            viewHolder.colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.str_value.switchStyle(viewHolder.colorView);
                    viewHolder.colorView.setTextColor(item.getReverseColor());
                }
            });
            item.red.setText(viewHolder.redView);
            viewHolder.redView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.red.switchStyle(viewHolder.redView);
                }
            });
            item.green.setText(viewHolder.greenView);
            viewHolder.greenView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.green.switchStyle(viewHolder.greenView);
                }
            });
            item.blue.setText(viewHolder.blueView);
            viewHolder.blueView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.blue.switchStyle(viewHolder.blueView);
                }
            });
        }
    }

    private static class ColorInfoViewHolder {
        TextView nameView;
        TextView name_cnView;
        TextView colorView;
        TextView redView;
        TextView greenView;
        TextView blueView;
    }
}

class ColorSettings {
    private static final int DEFAULT_COLOR_TEXT = Color.YELLOW;
    private static final int DEFAULT_COLOR_BACKGROUND = Color.DKGRAY;

    public int color_text = DEFAULT_COLOR_TEXT;
    public int color_background = DEFAULT_COLOR_BACKGROUND;
}

class PreviewInfo {
    private final int mTextViewResId;

    private TextView mTextView;

    private final ColorSettings mColorSettings = new ColorSettings();

    public PreviewInfo(int textViewResId) {
        mTextViewResId = textViewResId;
    }

    public void initViews(final View rootView, final String text, final int textColor,
            final int backgroundColor) {
        mColorSettings.color_text = textColor;
        mColorSettings.color_background = backgroundColor;
        mTextView = (TextView) rootView.findViewById(mTextViewResId);
        mTextView.setText(text);
        previewColorSettings();
    }

    public void previewColorSettings(final int color, final boolean isTextColor) {
        if (isTextColor) {
            mColorSettings.color_text = color;
        } else {
            mColorSettings.color_background = color;
        }
        previewColorSettings();
    }

    private void previewColorSettings() {
        mTextView.setTextColor(mColorSettings.color_text);
        mTextView.setBackgroundColor(mColorSettings.color_background);
    }

    public ColorSettings getColorSettings() {
        return mColorSettings;
    }
}

class SysColorInfo {
    private final String mFieldName;
    public final int color;

    public final String strAlpha;
    public final String strRed;
    public final String strGreen;
    public final String strBlue;

    public SysColorInfo(String fieldName, int color) {
        mFieldName = fieldName;
        this.color = color;

        strAlpha = getAlphaText(color);
        strRed   = getRedText(color);
        strGreen = getGreenText(color);
        strBlue  = getBlueText(color);
    }

    private static final String FORMAT_SINGLE_COLOR = "%3d,%s";
    public static String getRedText(final int color) {
        int red = Color.red(color);
        String hex = Utils.fillString(Integer.toHexString(red).toUpperCase(), 2, '0');
        return String.format(FORMAT_SINGLE_COLOR, red, hex);
    }
    public static String getGreenText(final int color) {
        int green = Color.green(color);
        String hex = Utils.fillString(Integer.toHexString(green).toUpperCase(), 2, '0');
        return String.format(FORMAT_SINGLE_COLOR, green, hex);
    }
    public static String getBlueText(final int color) {
        int blue = Color.blue(color);
        String hex = Utils.fillString(Integer.toHexString(blue).toUpperCase(), 2, '0');
        return String.format(FORMAT_SINGLE_COLOR, blue, hex);
    }

    public static String getAlphaText(final int color) {
        int alpha = Color.alpha(color);
        String hex = Utils.fillString(Integer.toHexString(alpha).toUpperCase(), 2, '0');
        return String.format(FORMAT_SINGLE_COLOR, alpha, hex);
    }

    private static final String FORMAT_FIELD = "Color.%s";

    public String getFieldName() {
        return String.format(FORMAT_FIELD, mFieldName);
    }

    public static String getFieldName(String fieldName) {
        return String.format(FORMAT_FIELD, fieldName);
    }

    private static final String FORMAT_TO_STRING = "Color.%s = %s";

    @Override
    public String toString() {
        return String.format(FORMAT_TO_STRING, mFieldName, Utils.toARGBString(color));
    }

    private static SysColorInfo[] sSysColors;

    public static int count() {
        if (sSysColors == null || sSysColors.length <= 0) {
            getSysColors();
        }
        return sSysColors == null ? 0 : sSysColors.length;
    }

    public static SysColorInfo get(final int index) {
        if (sSysColors == null || sSysColors.length <= 0) {
            getSysColors();
        }
        if (sSysColors == null) return null;
        if (index < 0 || index >= sSysColors.length) return null;
        return sSysColors[index];
    }

    public static void getSysColors() {
        StaticFieldInfo[] fieldsInfo = Reflector.getPublicStaticFields(Color.class);
        sSysColors = new SysColorInfo[fieldsInfo.length];
        String fieldName;
        int color;
        for (int i = 0; i < sSysColors.length; i++) {
            fieldName = fieldsInfo[i].fieldName;
            color = (Integer) fieldsInfo[i].value;
            sSysColors[i] = new SysColorInfo(fieldName, color);
        }
    }

    public static String findSysColorInfo(final int color) {
        if (sSysColors == null || sSysColors.length <= 0) {
            getSysColors();
        }
        StringBuilder sb = new StringBuilder();
        for (SysColorInfo colorInfo : sSysColors) {
            if (colorInfo.color == color) {
                if (sb.length() > 0) sb.append(';');
                sb.append(colorInfo.getFieldName());
            }
        }
        if (sb.length() > 0) return sb.toString();
        return null;
    }

    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color));
        y /= 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }
}

class Utils {
    private static final String FORMAT_HEX_STRING = "0x%s";

    // format: #AARRGGBB
    private static final String FORMAT_COLOR = "#%s%s%s%s";

    public static String getRedHex(final int color) {
        int red = Color.red(color);
        return Utils.fillString(Integer.toHexString(red).toUpperCase(), 2, '0');
    }

    public static String getGreenHex(final int color) {
        int green = Color.green(color);
        return Utils.fillString(Integer.toHexString(green).toUpperCase(), 2, '0');
    }

    public static String getBlueHex(final int color) {
        int blue = Color.blue(color);
        return Utils.fillString(Integer.toHexString(blue).toUpperCase(), 2, '0');
    }

    public static String getAlphaHex(final int color) {
        int alpha = Color.alpha(color);
        return Utils.fillString(Integer.toHexString(alpha).toUpperCase(), 2, '0');
    }

    public static String toARGBString(final int color) {
        String alpha = getAlphaHex(color);
        String red = getRedHex(color);
        String green = getGreenHex(color);
        String blue = getBlueHex(color);
        return String.format(FORMAT_COLOR, alpha, red, green, blue);
    }

    public static String getIntHex(final int anInteger) {
        String hexString = Integer.toHexString(anInteger).toUpperCase();
        return String.format(FORMAT_HEX_STRING, hexString);
    }

    public static int getIntFromCharSequence(CharSequence s) {
        if (TextUtils.isEmpty(s)) return 0;
        return Integer.parseInt(s.toString());
    }

    public static String fillString(final String input, final int length) {
        return fillString(input, length, ' ');
    }

    public static String fillString(final String input, final int length, final char fillChar) {
        if (input != null && input.length() >= length) return input;
        final StringBuilder sb;
        if (input == null) {
            sb = new StringBuilder();
        } else {
            sb = new StringBuilder(input);
        }
        while (sb.length() < length) {
            sb.insert(0, fillChar);
        }
        return sb.toString();
    }

    public static int randomInt(final int min, final int max) {
        final Random rand = new Random();
        if (min < 0 || min > max) {
            throw new IllegalArgumentException();
        }
        if (min == max) {
            return min;
        }
        int randNumber;
        do {
            randNumber = rand.nextInt() % (max + 1);
        } while (randNumber < min || randNumber > max);
        return randNumber;
    }
}

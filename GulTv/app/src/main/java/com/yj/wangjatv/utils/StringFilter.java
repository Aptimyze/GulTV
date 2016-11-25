package com.yj.wangjatv.utils;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.yj.wangjatv.R;

import java.util.regex.Pattern;

/**
 * @author hyogij@gmail.com
 * @description : Inputfilter class to constrain the EditText changes
 */
public class StringFilter {
    private static final String CLASS_NAME = StringFilter.class
            .getCanonicalName();

    public static final int ALLOW_ALPHANUMERIC = 0;
    public static final int ALLOW_ALPHA_HANGUL = 1;
    public static final int ALLOW_ALPHANUMERIC_HANGUL = 2;
    public static final int ALLOW_NUMERIC_HANGUL = 3;
    public static final int ALLOW_ALPHANUMERIC_HANGUL_SPECIAL = 4;
    public static final int ALLOW_ALPHANUMERIC_MONKEY = 5;
    public static final int ALLOW_NUMERIC_HANGUL_SPECIAL = 6;
    public static final int TOAST_LELNGTH = 400;

    private Context context = null;

    public StringFilter(Context context) {
        this.context = context;
    }

    // Allows only alphanumeric characters. Filters special and hangul
    // characters.
    public InputFilter allowAlphanumeric = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_ALPHANUMERIC);
        }
    };

    // Allows only alpha and hangul characters. Filters special
    // characters.
    public InputFilter allowAlphaHangul = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_ALPHA_HANGUL);
        }
    };

    // Allows only alphanumeric and hangul characters. Filters special
    // characters.
    public InputFilter allowAlphanumericHangul = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_ALPHANUMERIC_HANGUL);
        }
    };

    // Allows only alphanumeric and hangul characters. Filters special
    // characters.
    public InputFilter allowNumericHangul = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_NUMERIC_HANGUL);
        }
    };

    // Allows only alphanumeric and hangul characters. Filters special
    // characters.
    public InputFilter allowAlphanumericHangulSpecial = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_ALPHANUMERIC_HANGUL_SPECIAL);
        }
    };

    // Allows only alphanumeric and hangul characters. Filters special
    // characters.
    public InputFilter allowNumericHnagulSpecial = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_NUMERIC_HANGUL_SPECIAL);
        }
    };

    // Allows only alphanumeric and hangul characters. Filters special
    // characters.
    public InputFilter allowAlphanumericMonkey = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            return filteredString(source, start, end, ALLOW_ALPHANUMERIC_MONKEY);
        }
    };

    // Returns the string result which is filtered by the given mode
    private CharSequence filteredString(CharSequence source, int start,
                                        int end, int mode) {
        Pattern pattern = null;
        if (mode == ALLOW_ALPHANUMERIC) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_alphanumeric));
        } else if (mode == ALLOW_ALPHA_HANGUL) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_alpha_hangul));
        } else if (mode == ALLOW_ALPHANUMERIC_HANGUL) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_alphanumeric_hangul));
        } else if (mode == ALLOW_ALPHANUMERIC_HANGUL_SPECIAL) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_alphanumeric_hangul_special));
        }
        else if (mode == ALLOW_ALPHANUMERIC_MONKEY) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_alphanumeric_monkey));
        }
        else if(mode == ALLOW_NUMERIC_HANGUL_SPECIAL) {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_numeric_hangul_special));
        }
        else {
            pattern = Pattern.compile(context
                    .getString(R.string.pattern_numeric_hangul));
        }

        boolean keepOriginal = true;
        StringBuilder stringBuilder = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (pattern.matcher(Character.toString(c)).matches()) {
                stringBuilder.append(c);
            } else {
                if (mode == ALLOW_ALPHANUMERIC) {
                    showToast(context.getString(R.string.input_error_alphanum));
                } else if (mode == ALLOW_ALPHA_HANGUL) {
                    showToast(context.getString(R.string.input_error_alpha_hangul));
                } else if (mode == ALLOW_ALPHANUMERIC_HANGUL) {
                    showToast(context
                            .getString(R.string.input_error_alphanumeric_hangul));
                } else if (mode == ALLOW_ALPHANUMERIC_HANGUL_SPECIAL) {
                    showToast(context
                            .getString(R.string.input_error_alphanumeric_hangul_special));
                }
                else if (mode == ALLOW_ALPHANUMERIC_MONKEY) {
                    showToast(context
                            .getString(R.string.input_error_alphanum_monkey));
                }
                else if (mode == ALLOW_NUMERIC_HANGUL_SPECIAL) {
                    showToast(context
                            .getString(R.string.input_error_numeric_hangul_special));
                }
                else{
                    showToast(context
                            .getString(R.string.input_error_numeric_hangul));
                }

                keepOriginal = false;
            }
        }

        if (keepOriginal) {
            return null;
        } else {
            if (source instanceof Spanned) {
                SpannableString spannableString = new SpannableString(
                        stringBuilder);
                TextUtils.copySpansFrom((Spanned) source, start,
                        stringBuilder.length(), null, spannableString, 0);
                return spannableString;
            } else {
                return stringBuilder;
            }
        }
    }

    // Shows toast with specify delay that is shorter than Toast.LENGTH_SHORT
    private void showToast(String msg) {
        final Toast toast = Toast.makeText(context.getApplicationContext(),
                msg, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, TOAST_LELNGTH);
    }

    public static void setCharacterLimited(Context ctx, EditText editText, int type) {
        StringFilter stringFilter = new StringFilter(ctx);
        InputFilter fileter = stringFilter.allowAlphanumeric;

        switch (type) {
            case ALLOW_ALPHANUMERIC:
                fileter = stringFilter.allowAlphanumeric;
                break;
            case ALLOW_ALPHA_HANGUL:
                fileter = stringFilter.allowAlphaHangul;
                break;
            case ALLOW_ALPHANUMERIC_HANGUL:
                fileter = stringFilter.allowAlphanumericHangul;
                break;
            case ALLOW_ALPHANUMERIC_HANGUL_SPECIAL:
                fileter = stringFilter.allowAlphanumericHangulSpecial;
                break;
            case ALLOW_ALPHANUMERIC_MONKEY:
                fileter = stringFilter.allowAlphanumericMonkey;
                break;
            case ALLOW_NUMERIC_HANGUL:
                fileter = stringFilter.allowNumericHangul;
                break;
            case ALLOW_NUMERIC_HANGUL_SPECIAL:
                fileter = stringFilter.allowNumericHnagulSpecial;
                break;
        }

        InputFilter[] curFilters = editText.getFilters();
        InputFilter[] newFilters = new InputFilter[curFilters.length + 1];
        for (int i = 0; i < curFilters.length; i++)
            newFilters[i] = curFilters[i];
        newFilters[curFilters.length] = fileter;
        editText.setFilters(newFilters);
    }
}
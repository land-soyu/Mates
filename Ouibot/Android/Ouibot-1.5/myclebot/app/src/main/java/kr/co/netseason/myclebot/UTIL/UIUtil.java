package kr.co.netseason.myclebot.UTIL;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.R;


/**
 * @author Jang-Ho Hwang, <rath@xrath.com>
 * @version 1.0,  05/01/2012, 04:51
 */
public class UIUtil {
	
    public static void setLayout(View view, boolean fillWidth, boolean fillHeight) {
        setLayout(view, fillWidth, fillHeight, 0.0f);
    }

	public static void setLayout(View view, boolean fillWidth, boolean fillHeight, float weight) {
		view.setLayoutParams(new LinearLayout.LayoutParams(
                fillWidth ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                fillHeight ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                weight
        ));
	}

    public static void frame(View view, float x, float y, float width, float height) {
        frame(view, (int) x, (int) y, (int) width, (int) height);
    }
    public static void frame(View view, double x, double y, double width, double height) {
    	frame(view, (int) x, (int) y, (int) width, (int) height);
    }

    public static void frame(View view, int x, int y, int width, int height) {
        FrameLayout.LayoutParams params;
        if( view.getLayoutParams() instanceof FrameLayout.LayoutParams ) {
            params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.width = width;
            params.height = height;
        } else {
            params = new FrameLayout.LayoutParams(width, height);
        }
        params.leftMargin = x;
        params.topMargin = y;
        view.setLayoutParams(params);
        view.layout(x, y, x + width, y + height);
    }
    public static void frame(View view, int width, int height) {
        LinearLayout.LayoutParams params;
            params = new LinearLayout.LayoutParams(width, height);
        view.setLayoutParams(params);
    }
    public static void frame(View view, int width, int height, float weight) {
        LinearLayout.LayoutParams params;
            params = new LinearLayout.LayoutParams(width, height, weight);
        view.setLayoutParams(params);
    }
    public static void frame(View view, double width, double height) {
    	frame(view, (int) width, (int) height);
    }

    public static void frameFill(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
    }
    public static void frameFillMatch(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
    }
    public static void frameMatch(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
    }

    public static void setBounds(View view, int x, int y, int w, int h) {
		if( view instanceof TextView ) {
			TextView tv = (TextView) view;
			tv.setWidth(w);
			tv.setHeight(h);
		}
		view.setLayoutParams(new AbsoluteLayout.LayoutParams(w, h, x, y));
	}

	public static String toString(View view) {
		StringBuilder sb = new StringBuilder();
		sb.append(view.getClass().getName()).append(':');
		sb.append(view.getLeft()).append(',');
		sb.append(view.getTop()).append(',');
		sb.append(view.getRight()).append(',');
		sb.append(view.getBottom()).append(" ");
		sb.append(view.getVisibility());
		return sb.toString();
	}

	public static Animation createFadeOut() {
		AlphaAnimation ani = new AlphaAnimation(0.7f, 0.0f);
		ani.setDuration(300);
//		ani.setInterpolator(new AccelerateDecelerateInterpolator());
		return ani;
	}

	public static Animation createFadeIn() {
		AlphaAnimation ani = new AlphaAnimation(0.3f, 1.0f);
		ani.setDuration(300);
//		ani.setInterpolator(new AccelerateDecelerateInterpolator());
		return ani;
	}

	public static int getBrighterColor(int value, int delta) {
		int a = (value >> 24) & 0xff;
		int r = (value >> 16) & 0xff;
		int g = (value >>  8) & 0xff;
		int b = (value >>  0) & 0xff;

		r += delta;
		g += delta;
		b += delta;

		r = safeByte(r);
		g = safeByte(g);
		b = safeByte(b);

		int newValue = (a<<24) | (r<<16) | (g<<8) | (b<<0);
		return newValue;
	}

	private static int safeByte(int value) {
		if( value < 0 ) value = 0x00;
		if( value > 0xff ) value = 0xff;
		return value;
	}

    public static void updateLinearMargin(View view, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) view.getLayoutParams();
        param.setMargins(left, top, right, bottom);
    }
    public static void updateLinearMarginCenter(View view, int left, int top, int right, int bottom, boolean flag) {
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) view.getLayoutParams();
        param.setMargins(left, top, right, bottom);
        if (flag) {
            param.gravity = Gravity.CENTER_HORIZONTAL;
        } else {
            param.gravity = Gravity.CENTER_VERTICAL;
        }
    }
    public static void updateFrameMargin(View view, int left, int top, int right, int bottom) {
    	FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) view.getLayoutParams();
        param.setMargins(left, top, right, bottom);
    }
    public static void updateFrameMargin(View view, int left, int top, int right, int bottom, boolean flag) {
    	FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) view.getLayoutParams();
        param.setMargins(left, top, right, bottom);
        if (flag) {
            param.gravity = Gravity.TOP;
        } else {
            param.gravity = Gravity.BOTTOM;
        }
    }

    public static SpannableString makeBold(String text) {
        SpannableString str = new SpannableString(text);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
        return str;
    }

    public static LinearLayout createLoadingPanel(Context context, float density) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setBackgroundResource(R.color.progress_background);
        ProgressBar spinner = new ProgressBar(context);
        spinner.setIndeterminateDrawable(context.getResources().getDrawable(R.anim.progressbar));
        spinner.setIndeterminate(true);
        int size = (int)(60f * density + 0.5f);
        ll.addView(spinner, new LinearLayout.LayoutParams(size, size));
        ll.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        UIUtil.frameFill(ll);
        ( (FrameLayout.LayoutParams)ll.getLayoutParams() ).gravity = Gravity.CENTER;
        return ll;
    }
    public static LinearLayout createOuibotLoadingPanel(Context context, int size) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setBackgroundResource(R.color.progress_background);
        ProgressBar spinner = new ProgressBar(context);
        spinner.setIndeterminateDrawable(context.getResources().getDrawable(R.anim.progressbar));
        spinner.setIndeterminate(true);
        ll.addView(spinner, new LinearLayout.LayoutParams(size, size));
        ll.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        UIUtil.frameFill(ll);
        ( (FrameLayout.LayoutParams)ll.getLayoutParams() ).gravity = Gravity.CENTER;
        return ll;
    }

    public static Bitmap filterGrayscale(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();
        Bitmap ret = Bitmap.createBitmap(w ,h, Bitmap.Config.RGB_565);
        Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        p.setColorFilter(filter);

        Canvas c = new Canvas(ret);
        c.drawBitmap(b, 0, 0, p);
        b.recycle();
        return ret;
    }
    
    public static int charSize(Activity a) {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    	int deviceWidth = displayMetrics.widthPixels;

    	return (int) (deviceWidth * 0.04 / (int)displayMetrics.density);
    }
    public static int charSize(Activity a, double value) {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    	int deviceWidth = displayMetrics.widthPixels;

    	return (int) (deviceWidth * value / displayMetrics.density);
    }
    public static int textfielHeight(Activity a) {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    	int deviceHeight = displayMetrics.heightPixels;
    	
    	return (int) (deviceHeight * 0.07);
    }
    public static int deviceWidth(Activity a) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        return width;
    }
    public static int deviceHeight(Activity a) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        return height;
    }
    public static int deviceWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        return width;
    }
    public static int deviceHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        return height;
    }

    public static void viewSizeSetting(Activity a, View view, int w, int h, boolean layoutFlag) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)( (w*100)/1280 * displayMetrics.widthPixels /100 );
        int height = (int)( (h*100)/800 * displayMetrics.heightPixels /100 );


        if ( layoutFlag ) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            view.setLayoutParams(params);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
            view.setLayoutParams(params);
        }
    }
    public static void viewSizeSetting(Activity a, View view, int size, boolean layoutFlag) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)( (size*100)/1280 * displayMetrics.widthPixels /100 );
        int height = width;


        if ( layoutFlag ) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            view.setLayoutParams(params);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
            view.setLayoutParams(params);
        }
    }


    
    public static Spannable stripUnderlines(TextView textView) {
    	CharSequence str = textView.getText();
    	try {
        	Spannable s = (Spannable)str;
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span: spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }
            return s;
    	} catch (ClassCastException e) {
//    		Logger.e("Flitto", e.getMessage());
    	}
    	return new SpannableString(textView.getText().toString());
    }


    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }



    
    //	soyu add 07/18/2013
    public static void frameLeftButton(View view, int x, int y, int width, int height, boolean flag) {
        FrameLayout.LayoutParams params;
        if( view.getLayoutParams() instanceof FrameLayout.LayoutParams ) {
            params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.width = width;
            params.height = height;
        } else {
            params = new FrameLayout.LayoutParams(width, height);
        }
        params.bottomMargin = y;
        if ( flag ) {
            params.leftMargin = x;
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        } else {
            params.rightMargin = x;
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        }
        view.setLayoutParams(params);
        view.layout(x, y, x + width, y + height);
    }
    //	soyu add 07/18/2013
    public static TranslateAnimation getButtonAnimation(int point1, int point2, int duration) {
        TranslateAnimation move = new TranslateAnimation(0, 0, point1, point2);
        move.setDuration(duration);
    	return move;
    }
    
    //	soyu add 08/06/2013
    public static RotateAnimation getStartButtonAnination(int point1, int point2, int duration) {
    	RotateAnimation rotate = new RotateAnimation(point1, point2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    	rotate.setDuration(duration);
    	return rotate;
    }

    //  soyu add 20151110
    public static void setCAMSIZE(Context context, double v, View view) {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
        params.width = (int)(UIUtil.deviceWidth(context) * v);
        params.height = (int)(UIUtil.deviceWidth(context) * v);
        view.setLayoutParams(params);
    }

    //  soyu add 20151120
    public static void setIMAGESIZE(Context context, double v, View view, int layout_flag, int gravity_flag) {
        Logger.e("!!!", "UIUtil.deviceWidth(context) = " + UIUtil.deviceWidth(context));
        Logger.e("!!!", "UIUtil.deviceHeight(context) = " + UIUtil.deviceHeight(context));

        switch (layout_flag) {
            case 1:
                LinearLayout.LayoutParams lparams = (LinearLayout.LayoutParams) view.getLayoutParams();
                if ( v > 0 ) {
                    lparams.width = (int)(UIUtil.deviceWidth(context) * (v/755));
                    lparams.height = (int)(UIUtil.deviceWidth(context) * (v/755));
                } else {
                    lparams.width = (int)(v);
                    lparams.height = (int)(v);
                }
                if ( gravity_flag > 0 ) lparams.gravity = gravity_flag;
                view.setLayoutParams(lparams);
                break;
            case 2:
                FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
                if ( v > 0 ) {
                    fparams.width = (int)(UIUtil.deviceWidth(context) * (v/755));
                    fparams.height = (int)(UIUtil.deviceWidth(context) * (v/755));
                } else {
                    fparams.width = (int)(v);
                    fparams.height = (int)(v);
                }
                if ( gravity_flag > 0 ) fparams.gravity = gravity_flag;
                view.setLayoutParams(fparams);
                break;
        }
    }
    public static void setIMAGESIZE(Context context, double x, double y, View view, int layout_flag, int gravity_flag) {
        if ( context.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ) {
            switch (layout_flag) {
                case 1:
                    LinearLayout.LayoutParams lparams = (LinearLayout.LayoutParams) view.getLayoutParams();
                    if ( x > 0 ) {
                        lparams.width = (int)(UIUtil.deviceWidth(context) * (x/755));
                    } else {
                        lparams.width = (int)(x);
                    }
                    if ( y > 0 ) {
                        lparams.height = (int)(UIUtil.deviceHeight(context) * (y/455));
                    } else {
                        lparams.height = (int)(y);
                    }
                    if ( gravity_flag > 0 ) lparams.gravity = gravity_flag;
                    view.setLayoutParams(lparams);
                    break;
                case 2:
                    FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    if ( x > 0 ) {
                        fparams.width = (int)(UIUtil.deviceWidth(context) * (x/755));
                    } else {
                        fparams.width = (int)(x);
                    }
                    if ( y > 0 ) {
                        fparams.height = (int)(UIUtil.deviceHeight(context) * (y/455));
                    } else {
                        fparams.height = (int)(y);
                    }
                    if ( gravity_flag > 0 ) fparams.gravity = gravity_flag;
                    view.setLayoutParams(fparams);
                    break;
            }
        } else {
            switch (layout_flag) {
                case 1:
                    LinearLayout.LayoutParams lparams = (LinearLayout.LayoutParams) view.getLayoutParams();
                    if ( x > 0 ) {
                        lparams.width = (int)(UIUtil.deviceHeight(context) * (x/455));
                    } else {
                        lparams.width = (int)(x);
                    }
                    if ( y > 0 ) {
                        lparams.height = (int)(UIUtil.deviceWidth(context) * (y/755));
                    } else {
                        lparams.height = (int)(y);
                    }
                    if ( gravity_flag > 0 ) lparams.gravity = gravity_flag;
                    view.setLayoutParams(lparams);
                    break;
                case 2:
                    FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    if ( x > 0 ) {
                        fparams.width = (int)(UIUtil.deviceHeight(context) * (x/455));
                    } else {
                        fparams.width = (int)(x);
                    }
                    if ( y > 0 ) {
                        fparams.height = (int)(UIUtil.deviceWidth(context) * (y/755));
                    } else {
                        fparams.height = (int)(y);
                    }
                    if ( gravity_flag > 0 ) fparams.gravity = gravity_flag;
                    view.setLayoutParams(fparams);
                    break;
            }
        }

    }



    public static void setSelfView(Context context, TextureView selfView) {
        Logger.e("!!!", "setView setSelfView UIUtil.deviceWidth(context) = " + UIUtil.deviceWidth(context));
        Logger.e("!!!", "setView setSelfView UIUtil.deviceHeight(context) = " + UIUtil.deviceHeight(context));

        FrameLayout.LayoutParams lparams = (FrameLayout.LayoutParams) selfView.getLayoutParams();

        Logger.e("!!!", "lparams.width = " + lparams.width);
        lparams.width = (int)(UIUtil.deviceWidth(context)) - (int)(UIUtil.deviceHeight(context) * 4 / 3 );
        Logger.e("!!!", "lparams.width = " + lparams.width);
        Logger.e("!!!", "lparams.height = " + lparams.height);
        lparams.height = (int)( lparams.width * 3 / 4 );
        Logger.e("!!!", "lparams.height = " + lparams.height);
        selfView.setLayoutParams(lparams);
    }
    public static void setRemoteView(Context context, TextureView remoteView) {
        Logger.e("!!!", "setView setRemoteView UIUtil.deviceWidth(context) = " + UIUtil.deviceWidth(context));
        Logger.e("!!!", "setView setRemoteView UIUtil.deviceHeight(context) = " + UIUtil.deviceHeight(context));

        FrameLayout.LayoutParams lparams = (FrameLayout.LayoutParams) remoteView.getLayoutParams();
        Logger.e("!!!", "lparams.width = " + lparams.width);
        lparams.width = (int)(UIUtil.deviceHeight(context) * 4 / 3 );
        Logger.e("!!!", "lparams.width = " + lparams.width);
        Logger.e("!!!", "lparams.height = " + lparams.height);
        lparams.height = (int)(UIUtil.deviceHeight(context));
        Logger.e("!!!", "lparams.height = " + lparams.height);
        remoteView.setLayoutParams(lparams);
    }

}

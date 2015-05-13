package ch.unibe.scg.zeeguuu.Search_Fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import ch.unibe.scg.zeeguuu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 29/04/15.
 */
public class MainLinearLayout extends LinearLayout {

    public MainLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    //// little hack to be able to integrate a keyboard listener ////
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("Search Layout", "Handling Keyboard Window shown");

        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedheight) {
            //when keyboard is closed
            findViewById(R.id.relativeLayout_text_translated).setVisibility(GONE);
        } else if (actualHeight < proposedheight) {
            //when keyboard is opened
            findViewById(R.id.relativeLayout_text_translated).setVisibility(VISIBLE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

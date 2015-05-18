package ch.unibe.scg.zeeguuu.Search_Fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import ch.unibe.scg.zeeguuu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 29/04/15.
 */
public class MainFrameLayout extends FrameLayout {

    public MainFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    //// little hack to be able to integrate a keyboard listener ////
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("Search Layout", "Handling Keyboard Window shown");

        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int height = getHeight();

        if (height > proposedheight) {
            //when keyboard is opened
            findViewById(R.id.relativeLayout_text_translated).setVisibility(GONE);
        } else if (height < proposedheight) {
            //when keyboard is closed
            findViewById(R.id.relativeLayout_text_translated).setVisibility(VISIBLE);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

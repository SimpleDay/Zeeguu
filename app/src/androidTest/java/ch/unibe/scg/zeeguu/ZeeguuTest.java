package ch.unibe.scg.zeeguu;

import android.app.Activity;
import android.content.ClipboardManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import ch.unibe.scg.zeeguu.Core.ConnectionManager;
import ch.unibe.scg.zeeguu.Core.ZeeguuActivity;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ZeeguuTest extends ActivityInstrumentationTestCase2<ZeeguuActivity> {
    private ZeeguuActivity zeeguuActivity;
    private ConnectionManager connectionManager;
    private ClipboardManager clipboard;

    public ZeeguuTest() {
        super(ZeeguuActivity.class);
    }


    public void setUp() throws Exception {
        super.setUp();
        zeeguuActivity = getActivity();
        connectionManager = ConnectionManager.getConnectionManager(zeeguuActivity);
        clipboard = (ClipboardManager) zeeguuActivity.getSystemService(Activity.CLIPBOARD_SERVICE);

        setActivityInitialTouchMode(true);
    }

    public void testPreconditions() {
        assertNotNull("ZeeguuActivity is null", zeeguuActivity);
        assertNotNull("ConnectionManager is null", connectionManager);
    }

    public void testCopyPaste() {
        ImageView btn_paste = (ImageView) zeeguuActivity.findViewById(R.id.btn_paste);
        ImageView btn_copy = (ImageView) zeeguuActivity.findViewById(R.id.btn_copy);

        assertEquals(btn_paste.getAlpha(), clipboard.hasPrimaryClip()? 1f : .3f);
        assertEquals(btn_copy.getAlpha(), 0.3f);
    }

    public void testTranslation() {
        EditText edit_text_native = (EditText) zeeguuActivity.findViewById(R.id.text_native);
        EditText edit_text_translated = (EditText) zeeguuActivity.findViewById(R.id.text_translated);
        ImageButton btn_transl = (ImageButton) zeeguuActivity.findViewById(R.id.btn_translate);

        edit_text_native.setText("hallo");
        TouchUtils.clickView(this, btn_transl);

        assertEquals(edit_text_translated.getText(),"hello");

    }

    public void testWordlist() {
        //do nothing at the moment
    }

}
package ch.unibe.scg.zeeguuu;

import android.app.Activity;
import android.content.ClipboardManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import ch.unibe.scg.zeeguuu.Core.ZeeguuActivity;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ZeeguuTest extends ActivityInstrumentationTestCase2<ZeeguuActivity> {
    private ZeeguuActivity zeeguuActivity;
    private ZeeguuConnectionManager connectionManager;
    private ClipboardManager clipboard;

    public ZeeguuTest() {
        super(ZeeguuActivity.class);
    }


    public void setUp() throws Exception {
        super.setUp();
        zeeguuActivity = getActivity();
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
        EditText editTextLanguageFrom = (EditText) zeeguuActivity.findViewById(R.id.edit_text_language_from);
        EditText editTextLanguageTo = (EditText) zeeguuActivity.findViewById(R.id.edit_text_language_to);
        TextView btn_transl = (TextView) zeeguuActivity.findViewById(R.id.btn_translate);

        editTextLanguageFrom.setText("hallo");
        TouchUtils.clickView(this, btn_transl);

        assertEquals(editTextLanguageTo.getText(),"hello");

    }

    public void testMyWords() {
        //do nothing at the moment
    }

}
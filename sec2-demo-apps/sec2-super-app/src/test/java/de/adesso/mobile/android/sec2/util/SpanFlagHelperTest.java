/**
 * 
 */
package de.adesso.mobile.android.sec2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.Context;
import android.text.Editable;
import android.widget.EditText;
import de.adesso.mobile.android.sec2.model.Lock;

/**
 * @author hoppe
 *
 */
public class SpanFlagHelperTest {

    @Mock
    Context context;
    Editable editable;

    EditText editText;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        editText = mock(EditText.class);
        editable = mock(Editable.class);

        when(editText.getText()).thenReturn(editable);
        when(editable.toString()).thenReturn("Hier steht ein Text");
        when(editText.length()).thenReturn("Hier steht ein Text".length());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSpanFlagHelper() throws Exception {
        final SpanFlagHelper helper = new SpanFlagHelper(context, editText);
        assertNotNull("constructor failed", helper);
        assertNotNull(editText);
        assertNotEquals(String.valueOf(""), editText.getText().toString());
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#addSpanFlag(de.adesso.mobile.android.sec2.util.SpanFlag)}.
     */
    @Test
    public void testAddSpanFlag() {
        final SpanFlagHelper helper = new SpanFlagHelper(context, editText);
        helper.getCount();
        helper.addSpanFlag(new SpanFlag(0, editText.length()));
        assertTrue("new SpanFlag was not added correctly ", helper.getCount() > 0);
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#removeSpanFlag(de.adesso.mobile.android.sec2.util.SpanFlag)}.
     */
    @Test
    @Ignore
    public void testRemoveSpanFlag() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#clear()}.
     */
    @Test
    @Ignore
    public void testClear() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#initiateText()}.
     */
    @Test
    @Ignore
    public void testInitiateText() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#encrypt(int, int)}.
     */
    @Test
    @Ignore
    public void testEncrypt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#encryptAll()}.
     */
    @Test
    @Ignore
    public void testEncryptAll() {
        final SpanFlagHelper helper = new SpanFlagHelper(context, editText);
        helper.encryptAll();
        assertEquals(Lock.LOCKED, helper.determineLockStatus());
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#decrypt(int, int)}.
     */
    @Test
    @Ignore
    public void testDecrypt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#decryptAll()}.
     */
    @Test
    @Ignore
    public void testDecryptAll() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#splitSpanFlag(int, int)}.
     */
    @Test
    @Ignore
    public void testSplitSpanFlag() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#deleteSpanFlag(int, int)}.
     */
    @Test
    @Ignore
    public void testDeleteSpanFlag() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#mergeSpanFlag()}.
     */
    @Test
    @Ignore
    public void testMergeSpanFlag() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#determineLockStatus()}.
     */
    @Test
    @Ignore
    public void testDetermineLockStatus() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#getCount()}.
     */
    @Test
    public void testGetCount() {
        final SpanFlagHelper helper = new SpanFlagHelper(context, editText);
        assertEquals("getCount delivers wrong value", 0, helper.getCount());
    }

    /**
     * Test method for {@link de.adesso.mobile.android.sec2.util.SpanFlagHelper#getSpanFlag(int)}.
     */
    @Test
    @Ignore
    public void testGetSpanFlag() {
        fail("Not yet implemented");
    }

}

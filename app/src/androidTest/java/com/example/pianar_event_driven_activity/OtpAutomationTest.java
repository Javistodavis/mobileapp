package com.example.pianar_event_driven_activity;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * OTP AUTOMATION (with 2 second delay between steps)
 *  Scenario 1: invalid OTP -> red "Invalid code" message
 *  Scenario 2: valid OTP   -> green success message, then the Main Dashboard opens
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OtpAutomationTest {

    // Delay between steps (2000 = 2 seconds). Change this number to go faster/slower.
    private static final long DELAY_MS = 2000;

    // ------------------------------------------------------------------
    // SCENARIO 1: INVALID OTP
    // ------------------------------------------------------------------
    @Test
    public void scenario1_invalidOtp_showsInvalidCodeError() throws Exception {
        try (ActivityScenario<AuthenticationActivity> scenario =
                     ActivityScenario.launch(AuthenticationActivity.class)) {

            pause();   // Authentication screen is visible

            // Read the real code shown on screen, then make a DIFFERENT 6-digit code
            String realCode = getText(onView(withId(R.id.tvCode)));
            assertEquals(6, realCode.length());
            String wrongCode = makeWrongCode(realCode);
            assertNotEquals(realCode, wrongCode);

            // Type the wrong code
            onView(withId(R.id.etCode)).perform(replaceText(wrongCode));
            closeSoftKeyboard();
            pause();

            // Tap Verify
            onView(withId(R.id.btnVerify)).perform(click());
            pause();

            // Red "Invalid code" message is shown
            onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.tvStatus)).check(matches(withText(R.string.auth_wrong)));

            // Input was cleared, Verify is still available, success was NOT shown
            onView(withId(R.id.etCode)).check(matches(withText("")));
            onView(withId(R.id.btnVerify)).check(matches(isEnabled()));
            assertTextMissing(successText());

            // Still on the Authentication screen (did not go to the dashboard)
            onView(withText(R.string.auth_title)).check(matches(isDisplayed()));
            assertTextMissing("Main Dashboard");
            pause();
        }
    }

    // ------------------------------------------------------------------
    // SCENARIO 2: VALID OTP
    // ------------------------------------------------------------------
    @Test
    public void scenario2_validOtp_showsSuccess_thenDashboard() throws Exception {
        try (ActivityScenario<AuthenticationActivity> scenario =
                     ActivityScenario.launch(AuthenticationActivity.class)) {

            pause();   // Authentication screen is visible

            // Read the real code shown on screen
            String code = getText(onView(withId(R.id.tvCode)));
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"));

            // Type the correct code
            onView(withId(R.id.etCode)).perform(replaceText(code));
            closeSoftKeyboard();
            pause();

            // Tap Verify
            onView(withId(R.id.btnVerify)).perform(click());

            // Green success message is shown
            onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.tvStatus)).check(matches(withText(R.string.auth_success)));
            pause();

            // After ~1.5 seconds the Main Dashboard opens
            waitForText("Main Dashboard", 8_000);
            pause();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Waits so you can watch what the test is doing. */
    private static void pause() throws InterruptedException {
        Thread.sleep(DELAY_MS);
    }

    /** The success message text from strings.xml (works for any wording). */
    private static String successText() {
        return InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getString(R.string.auth_success);
    }

    /** Changes the last digit so the code is always different from the real one. */
    private static String makeWrongCode(String code) {
        int lastDigit = code.charAt(5) - '0';
        return code.substring(0, 5) + ((lastDigit + 1) % 10);
    }

    /** Reads the text of a TextView. */
    private static String getText(ViewInteraction interaction) {
        final String[] result = new String[1];
        interaction.perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "get text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                result[0] = ((TextView) view).getText().toString();
            }
        });
        return result[0];
    }

    /** Waits until a text appears on the screen. */
    private static void waitForText(String text, long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (true) {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError e) {
                if (System.currentTimeMillis() > end) throw e;
                Thread.sleep(250);
            }
        }
    }

    /** Asserts that a text is NOT on the current screen. */
    private static void assertTextMissing(String text) {
        try {
            onView(withText(text)).check(matches(isDisplayed()));
        } catch (NoMatchingViewException | AssertionError e) {
            return;   // not found / not displayed = what we want
        }
        throw new AssertionError("Text should not be on screen: " + text);
    }
}
package com.example.pianar_event_driven_activity;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Collection;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginAutomationTest {

    // Delay between steps (2000 = 2 seconds). Change this number to go faster/slower.
    private static final long DELAY_MS = 2000;

    private static final String VALID_EMAIL = "autotest@example.com";
    private static final String VALID_PASSWORD = "Test1234!";

    @Before
    public void createTestAccount() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocalUserStore store = new LocalUserStore(ctx);
        if (!store.exists(VALID_EMAIL)) {
            store.save("Auto", "Test", VALID_EMAIL, VALID_PASSWORD);
        }
    }


    @Test
    public void scenario1_invalidLogin_staysOnLoginScreen() throws Exception {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            pause();   // login screen is visible

            onView(withId(R.id.etUsername)).perform(replaceText(VALID_EMAIL));
            pause();

            onView(withId(R.id.etPassword)).perform(replaceText("WrongPassword!"));
            closeSoftKeyboard();
            pause();

            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
            onView(withId(R.id.btnLogin)).perform(click());

            // The app also asks the API (up to ~60s if the Render server is asleep).
            waitUntilLoginButtonEnabled(scenario, 70_000);
            pause();

            // Clear message if the app is not on the login screen anymore
            String screen = currentScreenName();
            if (!"MainActivity".equals(screen)) {
                throw new AssertionError("Expected to stay on the login screen (MainActivity) "
                        + "but the screen is: " + screen
                        + ". Do not touch the emulator while the test runs.");
            }

            // Still on the login screen
            onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
            onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));

            // Authentication screen was NOT opened
            assertViewMissing(R.id.tvCode);
            pause();
        }
    }

    // ------------------------------------------------------------------
    // SCENARIO 2: VALID LOGIN
    // ------------------------------------------------------------------
    @Test
    public void scenario2_validLogin_opensAuthenticationScreen() throws Exception {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            pause();   // login screen is visible

            onView(withId(R.id.etUsername)).perform(replaceText(VALID_EMAIL));
            pause();

            onView(withId(R.id.etPassword)).perform(replaceText(VALID_PASSWORD));
            closeSoftKeyboard();
            pause();

            onView(withId(R.id.btnLogin)).perform(click());
            pause();

            // Authentication screen is shown
            onView(withText(R.string.auth_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tvCode)).check(matches(isDisplayed()));
            onView(withId(R.id.etCode)).check(matches(isDisplayed()));
            onView(withId(R.id.btnVerify)).check(matches(isDisplayed()));
            onView(withId(R.id.btnResend)).check(matches(isDisplayed()));
            pause();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------


    private static void pause() throws InterruptedException {
        Thread.sleep(DELAY_MS);
    }


    private static String currentScreenName() {
        final String[] name = {"none"};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumed = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED);
            if (!resumed.isEmpty()) {
                name[0] = resumed.iterator().next().getClass().getSimpleName();
            }
        });
        return name[0];
    }


    private static void waitUntilLoginButtonEnabled(ActivityScenario<MainActivity> scenario,
                                                    long timeoutMs) throws InterruptedException {
        final boolean[] enabled = {false};
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            scenario.onActivity(activity ->
                    enabled[0] = activity.findViewById(R.id.btnLogin).isEnabled());
            if (enabled[0]) return;
            Thread.sleep(250);
        }
        throw new AssertionError("Login did not finish within " + timeoutMs + " ms");
    }


    private static void assertViewMissing(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
        } catch (NoMatchingViewException e) {
            return;   // view not found = what we want
        }
        throw new AssertionError("View should not be on screen, but it is");
    }
}
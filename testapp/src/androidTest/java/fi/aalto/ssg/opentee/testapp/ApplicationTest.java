package fi.aalto.ssg.opentee.testapp;

import android.app.Application;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

/**
 * Android Unit Test.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void init(){
    }

    @Test
    public void testSixKeysInARow(){
        onView(withId(R.id.button_create_root_key)).perform(click());
        onView(withId(R.id.button_initialize)).perform(click());
        onView(withId(R.id.button_create_directory_key)).perform(click());
        onView(withId(R.id.button_encrypt_file)).perform(click());
        onView(withId(R.id.button_decrypt_file)).perform(click());
        onView(withId(R.id.button_finalize)).perform(click());

        onView(withId(R.id.view_log)).check(matches(not(withText(containsString("fail")))));
    }
}
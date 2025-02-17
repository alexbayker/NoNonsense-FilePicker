package com.nononsenseapps.filepicker.sample;


import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.nononsenseapps.filepicker.sample.PermissionGranter.allowPermissionsIfNeeded;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FastScrollerNewFile {

    @Rule
    public ActivityTestRule<NoNonsenseFilePickerTest> mActivityTestRule =
            new ActivityTestRule<>(NoNonsenseFilePickerTest.class);

    @Before
    public void allowPermissions() {
        allowPermissionsIfNeeded(mActivityTestRule.getActivity());
    }

    @Test
    public void selectNewFile() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile), withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), 
                        isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        // sub dir
        recyclerView.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("testfile"));

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // Click ok
        appCompatImageButton.perform(click());

        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/000000_nonsense-tests/C-dir/testfile")));
    }

    @Test
    public void withSingleClick() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkSingleClick), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Navigate to file
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(2, click()));
        // Click file
        recyclerView.perform(actionOnItemAtPosition(4, click()));

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/000000_nonsense-tests/B-dir/file-3.txt")));
    }

    @Test
    public void clickTwiceShouldNotClearFilename() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), 
                        isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Navigate to file
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        recyclerView.perform(actionOnItemAtPosition(2, click()));

        // Click on file once
        recyclerView.perform(actionOnItemAtPosition(4, click()));

        // Filename should be entered in field
        ViewInteraction editText = onView(withId(R.id.nnf_text_filename));
        editText.check(matches(withText("file-3.txt")));

        // Click twice
        recyclerView.perform(actionOnItemAtPosition(4, click()));

        // Filename should not change
        editText.check(matches(withText("file-3.txt")));
    }

    @Test
    public void enterFileNameWithPathWhichExists() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile),
                        withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        // Enter path in filename
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("B-dir/file-3.txt"));

        // Click ok
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/000000_nonsense-tests/B-dir/file-3.txt")));
    }

    @Test
    public void enterFileNameWithPathWhichDoesNotExist() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile),
                        withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        // Enter path in filename
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("path/to/file"));

        // Click ok
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/000000_nonsense-tests/path/to/file")));
    }

    @Test
    public void enterFileNameWithDotDot() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile),
                        withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        // Enter path in filename
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("../file.txt"));

        // Click ok
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/file.txt")));
    }

    @Test
    public void enterFileNameWithDot() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile),
                        withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        // Enter path in filename
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("./file.txt"));

        // Click ok
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/storage/emulated/0/000000_nonsense-tests/file.txt")));
    }

    @Test
    public void enterFileNameWithRoot() throws IOException {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioNewFile), withText("Select new file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowExistingFile),
                        withText("Allow selection of existing (new) file"), isDisplayed()));
        checkBox.perform(click());

        onView(withId(R.id.button_fastscroll)).perform(ViewActions.scrollTo());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_fastscroll), withText("Pick With Fast Scroller"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        // Enter path in filename
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.nnf_text_filename),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        // new file name
        appCompatEditText.perform(replaceText("/file.txt"));

        // Click ok
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.nnf_button_ok_newfile),
                        withParent(allOf(withId(R.id.nnf_newfile_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Should have returned
        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(withText("/file.txt")));
    }
}

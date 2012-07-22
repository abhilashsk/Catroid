/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.uitest.web;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.common.Constants;
import at.tugraz.ist.catroid.common.CostumeData;
import at.tugraz.ist.catroid.common.SoundInfo;
import at.tugraz.ist.catroid.common.StandardProjectHandler;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.ui.MainMenuActivity;
import at.tugraz.ist.catroid.uitest.util.UiTestUtils;
import at.tugraz.ist.catroid.utils.UtilFile;
import at.tugraz.ist.catroid.web.ServerCalls;

import com.jayway.android.robotium.solo.Solo;

public class ProjectUpAndDownloadTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {
	private Solo solo;
	private String testProject = UiTestUtils.PROJECTNAME1;
	private String newTestProject = UiTestUtils.PROJECTNAME2;
	private String saveToken;
	private int serverProjectId;
	private static final String TEST_FILE_DOWNLOAD_URL = "http://catroidtest.ist.tugraz.at/catroid/download/";
	private final int RESOURCE_SOUND = at.tugraz.ist.catroid.uitest.R.raw.longsound;
	private final int RESOURCE_IMAGE = at.tugraz.ist.catroid.uitest.R.drawable.catroid_sunglasses;
	private Project defaultProject;
	private ProjectManager projectManager = ProjectManager.getInstance();

	public ProjectUpAndDownloadTest() {
		super("at.tugraz.ist.catroid", MainMenuActivity.class);
		UiTestUtils.clearAllUtilTestProjects();
	}

	@Override
	@UiThreadTest
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		saveToken = prefs.getString(Constants.TOKEN, "0");
	}

	@Override
	public void tearDown() throws Exception {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefs.edit().putString(Constants.TOKEN, saveToken).commit();
		try {
			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		getActivity().finish();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
	}

	private void setServerURLToTestUrl() throws Throwable {
		runTestOnUiThread(new Runnable() {
			public void run() {
				ServerCalls.useTestUrl = true;
			}
		});
	}

	public void testUploadProjectSuccess() throws Throwable {
		setServerURLToTestUrl();

		createTestProject(testProject);
		addABrickToProject();

		//intent to the main activity is sent since changing activity orientation is not working
		//after executing line "UiTestUtils.clickOnLinearLayout(solo, R.id.btn_action_home);" 
		Intent intent = new Intent(getActivity(), MainMenuActivity.class);
		getActivity().startActivity(intent);

		UiTestUtils.createValidUser(getActivity());

		uploadProject();

		UiTestUtils.clearAllUtilTestProjects();

		downloadProject();
	}

	public void createDefaultProject() {
		try {
			setServerURLToTestUrl();
			UiTestUtils.createValidUser(getActivity());

			defaultProject = StandardProjectHandler.createAndSaveStandardProject(
					getActivity().getString(R.string.default_project_name), getInstrumentation().getTargetContext());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void renameDefaultProjectAndClickUploadButton() {
		solo.scrollUp();
		solo.clearEditText(0);
		solo.enterText(0, testProject);
		solo.clickOnButton(getActivity().getString(R.string.upload_button));
	}

	public void testUploadingProjectWithDefaultName() {

		createDefaultProject();
		Activity activity = getActivity();

		solo.clickOnButton(activity.getString(R.string.upload_project));
		solo.clickOnButton(activity.getString(R.string.upload_button));
		solo.sleep(200);
		assertTrue("When uploading the project with the default name the error message should be shown, but it wasn't",
				solo.searchText(activity.getString(R.string.error_upload_project_with_default_name)));
		solo.clickOnButton(activity.getString(R.string.close));

		renameDefaultProjectAndClickUploadButton();
		solo.waitForDialogToClose(10000);
		assertTrue("Upload of the default project succeeded although it should not be possible",
				solo.searchText(activity.getString(R.string.error_upload_default_project)));

	}

	public void testUploadDefaultProject() {

		createDefaultProject();
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());

		Activity activity = getActivity();
		solo.clickOnText(activity.getString(R.string.upload_project));
		renameDefaultProjectAndClickUploadButton();
		solo.waitForDialogToClose(10000);
		assertTrue("Upload of the default project succeeded although it should not be possible",
				solo.searchText(activity.getString(R.string.error_upload_default_project)));
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());
		solo.clickOnButton(activity.getString(R.string.close));

		defaultProject.setDefaultProjectFlag(false);
		assertFalse("The isDefaultProjectFlag is true but should be false", defaultProject.isDefaultProject());
		solo.clickOnButton(activity.getString(R.string.upload_button));
		solo.waitForDialogToClose(10000);
		assertTrue("The upload wasn't successful but it should have been",
				solo.searchText(activity.getString(R.string.success_project_upload)));
	}

	public void testDefaultProjectUploadableAfterAddingCostume() {

		createDefaultProject();
		Activity activity = getActivity();

		solo.clickOnButton(activity.getString(R.string.current_project_button));
		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_name));
		solo.clickOnText(activity.getString(R.string.costumes));
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());

		solo.clickOnText(activity.getString(R.string.add));
		solo.goBack();
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());

		solo.clickOnText(activity.getString(R.string.add));

		ArrayList<CostumeData> costumeDataList = projectManager.getCurrentSprite().getCostumeDataList();
		int oldCostumeCount = costumeDataList.size();

		File imageFile = UiTestUtils.saveFileToProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, "catroid_sunglasses.png",
				RESOURCE_IMAGE, getActivity(), UiTestUtils.FileTypes.IMAGE);
		CostumeData costumeData = new CostumeData();
		costumeData.setCostumeFilename(imageFile.getName());
		costumeData.setCostumeName("costumeNametest");
		costumeDataList.add(costumeData);
		projectManager.fileChecksumContainer.addChecksum(costumeData.getChecksum(), costumeData.getAbsolutePath());

		int newCostumeCount = costumeDataList.size();

		assertEquals("The costume has not been added", oldCostumeCount + 1, newCostumeCount);

		solo.goBack();
		solo.sleep(500);
		assertFalse("The isDefaultProjectFlag is true but should be false", defaultProject.isDefaultProject());

	}

	public void testDefaultProjectUploadableAfterAddingSound() {

		createDefaultProject();
		Activity activity = getActivity();

		solo.clickOnButton(activity.getString(R.string.current_project_button));
		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_name));
		solo.clickOnText(activity.getString(R.string.sounds));
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());

		solo.clickOnText(activity.getString(R.string.add));
		solo.goBack();
		assertTrue("The isDefaultProjectFlag is false but should be true", defaultProject.isDefaultProject());

		solo.clickOnText(activity.getString(R.string.add));

		ArrayList<SoundInfo> soundInfoList = projectManager.getCurrentSprite().getSoundList();
		int oldSoundCount = soundInfoList.size();

		File soundFile = UiTestUtils.saveFileToProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME, "longsound.mp3",
				RESOURCE_SOUND, getInstrumentation().getContext(), UiTestUtils.FileTypes.SOUND);
		SoundInfo soundInfo = new SoundInfo();
		soundInfo.setSoundFileName(soundFile.getName());
		soundInfo.setTitle("testSound1");
		soundInfoList.add(soundInfo);
		projectManager.fileChecksumContainer.addChecksum(soundInfo.getChecksum(), soundInfo.getAbsolutePath());

		int newSoundCount = soundInfoList.size();

		assertEquals("The costume has not been added", oldSoundCount + 1, newSoundCount);

		solo.goBack();
		solo.sleep(500);
		assertFalse("The isDefaultProjectFlag is true but should be false", defaultProject.isDefaultProject());

	}

	public void testDefaultProjectUploadableAfterSwitchingCostumes() {

		createDefaultProject();
		Activity activity = getActivity();

		solo.clickOnButton(activity.getString(R.string.current_project_button));
		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_name));
		solo.sleep(500);

		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_normalcat));
		assertFalse("The isDefaultProject flag is false but should be true", defaultProject.isDefaultProject());

		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_banzaicat));
		solo.sleep(500);
		assertFalse("The isDefaultProject flag is true but should be false", defaultProject.isDefaultProject());

	}

	public void testDefaultProjectUploadableAfterAddingBrick() {
		createDefaultProject();
		Activity activity = getActivity();
		solo.clickOnButton(activity.getString(R.string.current_project_button));
		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_name));
		assertTrue("The isDefaultProject flag is false but should be true", defaultProject.isDefaultProject());

		//random clicking previously known to reset the flag although the project has not been changed
		solo.clickOnText(activity.getString(R.string.add));
		solo.goBack();
		solo.clickLongOnText(activity.getString(R.string.brick_wait));
		assertTrue("The isDefaultProject flag is false but should be true", defaultProject.isDefaultProject());

		UiTestUtils.addNewBrick(solo, R.string.brick_play_sound);
		solo.sleep(1000);
		assertFalse("The isDefaultProject flag is true but should be false", defaultProject.isDefaultProject());

	}

	public void testDefaultProjectUploadableAfterRemovingBrick() {

		createDefaultProject();
		Activity activity = getActivity();
		solo.clickOnButton(activity.getString(R.string.current_project_button));
		solo.clickOnText(activity.getString(R.string.default_project_sprites_catroid_name));
		assertTrue("The isDefaultProject flag is false but should be true", defaultProject.isDefaultProject());

		solo.clickLongOnText(activity.getString(R.string.brick_wait));
		assertTrue("The isDefaultProject flag is false but should be true", defaultProject.isDefaultProject());

		Script currentScript = projectManager.getCurrentScript();
		Brick brick = currentScript.getBrick(0);
		currentScript.removeBrick(brick);
		assertFalse("The isDefaultProject flag is true but should be false", defaultProject.isDefaultProject());

	}

	private void createTestProject(String projectToCreate) {
		File directory = new File(Constants.DEFAULT_ROOT + "/" + projectToCreate);
		if (directory.exists()) {
			UtilFile.deleteDirectory(directory);
		}
		assertFalse("testProject was not deleted!", directory.exists());

		solo.clickOnButton(getActivity().getString(R.string.new_project));
		solo.enterText(0, projectToCreate);
		solo.goBack();
		solo.clickOnButton(0);
		solo.sleep(2000);

		File file = new File(Constants.DEFAULT_ROOT + "/" + projectToCreate + "/" + Constants.PROJECTCODE_NAME);
		assertTrue(projectToCreate + " was not created!", file.exists());
	}

	private void addABrickToProject() {
		solo.clickInList(0);
		UiTestUtils.addNewBrick(solo, R.string.brick_wait);
		UiTestUtils.clickOnLinearLayout(solo, R.id.btn_action_home);
	}

	private void uploadProject() {
		solo.clickOnText(getActivity().getString(R.string.upload_project));
		solo.sleep(500);

		// enter a new title
		solo.clearEditText(0);
		solo.clickOnEditText(0);
		solo.enterText(0, newTestProject);

		// enter a description
		solo.clearEditText(1);
		solo.clickOnEditText(1);
		solo.enterText(1, "the project description");

		//		solo.setActivityOrientation(Solo.LANDSCAPE);

		solo.clickOnButton(getActivity().getString(R.string.upload_button));

		solo.sleep(500);

		try {
			solo.setActivityOrientation(Solo.LANDSCAPE);

			solo.waitForDialogToClose(10000);
			assertTrue("Upload failed. Internet connection?",
					solo.searchText(getActivity().getString(R.string.success_project_upload)));
			String resultString = (String) UiTestUtils.getPrivateField("resultString", ServerCalls.getInstance());
			JSONObject jsonObject;
			jsonObject = new JSONObject(resultString);
			serverProjectId = jsonObject.optInt("projectId");

			solo.clickOnButton(0);
		} catch (JSONException e) {
			fail("JSON exception orrured");
		}
	}

	private void downloadProject() {
		String downloadUrl = TEST_FILE_DOWNLOAD_URL + serverProjectId + Constants.CATROID_EXTENTION;
		downloadUrl += "?fname=" + newTestProject;

		Intent intent = new Intent(getActivity(), MainMenuActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(downloadUrl));
		launchActivityWithIntent("at.tugraz.ist.catroid", MainMenuActivity.class, intent);

		boolean waitResult = solo.waitForActivity("MainMenuActivity", 10000);
		assertTrue("Download takes too long.", waitResult);
		assertTrue("Testproject2 not loaded.", solo.searchText(newTestProject));
		assertTrue("OverwriteRenameDialog not showed.",
				solo.searchText(getActivity().getString(R.string.overwrite_text)));

		solo.clickOnText(getActivity().getString(R.string.overwrite_rename));
		assertTrue("No text field to enter new name.", solo.searchEditText(newTestProject));
		solo.clickOnButton(getActivity().getString(R.string.ok));
		assertTrue("No error showed because of duplicate names.",
				solo.searchText(getActivity().getString(R.string.error_project_exists)));
		solo.clickOnButton(getActivity().getString(R.string.close));
		solo.clearEditText(0);
		solo.enterText(0, testProject);
		solo.clickOnButton(getActivity().getString(R.string.ok));
		assertTrue("Download not successful.",
				solo.searchText(getActivity().getString(R.string.success_project_download)));

		String projectPath = Constants.DEFAULT_ROOT + "/" + testProject;
		File downloadedDirectory = new File(projectPath);
		File downloadedProjectFile = new File(projectPath + "/" + Constants.PROJECTCODE_NAME);
		assertTrue("Downloaded Directory does not exist.", downloadedDirectory.exists());
		assertTrue("Downloaded Project File does not exist.", downloadedProjectFile.exists());

		projectPath = Constants.DEFAULT_ROOT + "/" + newTestProject;
		downloadedDirectory = new File(projectPath);
		downloadedProjectFile = new File(projectPath + "/" + Constants.PROJECTCODE_NAME);
		assertTrue("Original Directory does not exist.", downloadedDirectory.exists());
		assertTrue("Original Project File does not exist.", downloadedProjectFile.exists());

	}

}

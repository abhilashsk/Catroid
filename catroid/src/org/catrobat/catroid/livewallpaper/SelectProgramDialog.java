/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.livewallpaper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.UtilFile;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SelectProgramDialog extends Dialog {

	private Context context;
	private String selectedProject;

	public SelectProgramDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_lwp_select_program);
		addRadioButtons();
		addOkButton();
		addCancelButton();
	}

	private void addRadioButtons() {
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.dialog_lwp_select_project_radiogroup);

		File rootDirectory = new File(Constants.DEFAULT_ROOT);
		int numOfProjects = UtilFile.getProjectNames(rootDirectory).size();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String currentProjectName = sharedPreferences.getString(Constants.PREF_PROJECTNAME_KEY, null);

		RadioButton[] radioButton = new RadioButton[numOfProjects];
		int i = 0;
		List<String> projectNames = UtilFile.getProjectNames(rootDirectory);
		java.util.Collections.sort(projectNames, new SortIgnoreCase());
		for (String projectName : projectNames) {
			radioButton[i] = new RadioButton(context);
			radioButton[i].setText(projectName);
			radioButton[i].setTextColor(Color.WHITE);
			radioGroup.addView(radioButton[i], i);
			if (projectName.equals(currentProjectName)) {
				radioGroup.check(radioButton[i].getId());
			}
			i++;
		}

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
				selectedProject = checkedRadioButton.getText().toString();
			}
		});
	}

	private void addOkButton() {
		Button okButton = (Button) findViewById(R.id.dialog_lwp_select_project_ok_button);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (selectedProject == null) {
					dismiss();
					return;
				}
				new LoadProject().execute();
			}
		});
	}

	private void addCancelButton() {
		Button cancelButton = (Button) findViewById(R.id.dialog_lwp_select_project_cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	private class SortIgnoreCase implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.toLowerCase(Locale.getDefault()).compareTo(s2.toLowerCase(Locale.getDefault()));
		}
	}

	private class LoadProject extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public LoadProject() {
			progress = new ProgressDialog(getContext());
			progress.setTitle(context.getString(R.string.please_wait));
			progress.setMessage(context.getString(R.string.loading));
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Project project = StorageHandler.getInstance().loadProject(selectedProject);
			if (project != null) {
				ProjectManager projectManager = ProjectManager.getInstance();
				if (projectManager.getCurrentProject() != null
						&& projectManager.getCurrentProject().getName().equals(selectedProject)) {
					dismiss();
					return null;
				}
				projectManager.setProject(project);
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				Editor editor = sharedPreferences.edit();
				editor.putString(Constants.PREF_PROJECTNAME_KEY, selectedProject);
				editor.commit();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			LiveWallpaper.liveWallpaperEngine.changeWallpaperProgram();
			dismiss();
			if (progress.isShowing()) {
				progress.dismiss();
			}
			super.onPostExecute(result);
		}
	}
}
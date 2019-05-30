package com.my.kiki.ui;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.my.kiki.R;
import com.my.kiki.utils.Utils;


/**
 * Created by bluegenie-15 on 29/1/18.
 */

public class ClassDialogActivity extends AppCompatActivity {
	private RadioGroup amoutGrp;
	Toolbar toolbar;
	TextView tvToolbarTitle;
	int position;
	int positionstr = 0;

	int[] attrs = {R.attr.dialog_list_item1,R.attr.dialog_list_item2,R.attr.dialog_radio_btn_text_color,R.attr.dialog_radio_btn_disabled_text_color};
	TypedArray ta;
	int dialogListItem1,dialogListItem2,dialogRadioBtn,dialogRadioBtnDisabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setTheme(getNoTitleTheme());

		setTheme(R.style.DialogThemeDark);
		ta = obtainStyledAttributes(R.style.DialogThemeDark, attrs);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_amountdialog);
		amoutGrp = (RadioGroup) findViewById(R.id.radio_group);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		tvToolbarTitle = (TextView) findViewById(R.id.tv_title);

		dialogListItem1 = ta.getColor(0, ContextCompat.getColor(this,R.color.colorPrimaryDarkLight));
		dialogListItem2 = ta.getColor(1, ContextCompat.getColor(this,R.color.colorPrimaryDarkLight));
		dialogRadioBtn = ta.getColor(2, ContextCompat.getColor(this,R.color.colorPrimaryDarkLight));
		dialogRadioBtnDisabled = ta.getColor(3, ContextCompat.getColor(this,R.color.colorPrimaryDarkLight));

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Drawable upArrow = ContextCompat.getDrawable(ClassDialogActivity.this, R.drawable.back_b);
		upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

		getSupportActionBar().setHomeAsUpIndicator(upArrow);
		setTitle("");
//		toolbar.setBackgroundResource(R.color.colorPrimary);
//		tvToolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
		tvToolbarTitle.setText(getString(R.string.lbl_class));
		String[] websitesArray = getResources().getStringArray(R.array.class_array);
//		final String[] websitesArrayvalue = getResources().getStringArray(R.array.default_sorting_value);

		positionstr = Utils.getInstance(ClassDialogActivity.this).getInt(Utils.CLASS_KEY_POSITION);
		Log.e("Sa limit RadioButton ", " " + position + " " + positionstr);
		for (int i = 0; i < websitesArray.length; i++) {
			RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

			RadioButton radioButton = new RadioButton(this);
			radioButton.setText(websitesArray[i]);
//			radioButton.setTextSize(getResources().getDimension(R.dimen.text_8sp));
			lp.setMargins(0, 0, 0, 0);
			radioButton.setLayoutParams(lp);
			radioButton.setId(i);

			if (positionstr==(i)) {
				Log.e("ForLoop limit  ", " " + position + " " + i + " " + positionstr);
				radioButton.setChecked(true);
//				radioButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
				radioButton.setTextColor(dialogRadioBtn);
			} else {
				radioButton.setTextColor(dialogRadioBtnDisabled);
			}
			if (i%2==0){
//				radioButton.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
				radioButton.setBackgroundColor(dialogListItem1);
			} else if (i%2!=0){
//				radioButton.setBackgroundColor(ContextCompat.getColor(this,R.color.mild_white));
				radioButton.setBackgroundColor(dialogListItem2);
			}radioButton.setBackgroundColor(dialogListItem1);
			amoutGrp.addView(radioButton);
		}
		amoutGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int checkedRadioButtonId = amoutGrp.getCheckedRadioButtonId();
				RadioButton radioBtn = (RadioButton) findViewById(checkedRadioButtonId);
				radioBtn.setChecked(true);
				Log.e("RadioButton ", " " + checkedRadioButtonId + " " + /*Utils.mCurrency*/radioBtn.isSelected());
				Utils.getInstance(ClassDialogActivity.this).setInt(Utils.CLASS_KEY, /*websitesArrayvalue*/ checkedRadioButtonId+1);
				Utils.getInstance(ClassDialogActivity.this).setInt(Utils.CLASS_KEY_POSITION, checkedRadioButtonId);
				setResult(102);
				finish();
			}
		});
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}

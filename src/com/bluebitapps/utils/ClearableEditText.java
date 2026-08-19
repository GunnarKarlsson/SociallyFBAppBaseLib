/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.bluebitapps.utils;

import com.bluebitapps.fbclientbase.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class ClearableEditText extends RelativeLayout {
	LayoutInflater mInflater = null;
	EditText mEditText;
	Button mClearButton;
	OnClearClickListener mListener;
	
	public interface OnClearClickListener{
		public void onClearButtonClicked();
	}
	
	public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews();
	}

	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews();

	}

	public void setOnClearClickListener(OnClearClickListener listener){
		mListener = listener;
	}
	
	public ClearableEditText(Context context) {
		super(context);
		initViews();
	}
	
	public

	void initViews() {
		mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.clearable_edit_text, this, true);
		mEditText = (EditText) findViewById(R.id.clearable_edit);
		mClearButton = (Button) findViewById(R.id.clearable_button_clear);
		mClearButton.setVisibility(RelativeLayout.INVISIBLE);
		clearText();
		showHideClearButton();
	}
	
	public void addTextChangedListener(TextWatcher watcher){
		mEditText.addTextChangedListener(watcher);
	}
	
	public void removeTextChangedListener(TextWatcher watcher){
		mEditText.removeTextChangedListener(watcher);
	}

	void clearText() {
		mClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.setText("");
				mListener.onClearButtonClicked();
			}
		});
	}

	void showHideClearButton() {
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0)
					mClearButton.setVisibility(RelativeLayout.VISIBLE);
				else
					mClearButton.setVisibility(RelativeLayout.INVISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
	}

	public Editable getText() {
		Editable text = mEditText.getText();
		return text;
	}
}
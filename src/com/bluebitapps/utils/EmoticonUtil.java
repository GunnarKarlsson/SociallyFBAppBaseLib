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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import com.bluebitapps.fbclientbase.R;

public class EmoticonUtil {
	
	private static final Factory spannableFactory = Spannable.Factory
	        .getInstance();

	  private static final HashMap<String, Integer> emoticons = new HashMap<String, Integer>();
	     static {
	         emoticons.put(":)", R.drawable.emoticon_smile);
	         emoticons.put(":(", R.drawable.emoticon_frown);
	         emoticons.put(":P", R.drawable.emoticon_tongue);
	         emoticons.put(":D", R.drawable.emoticon_grin);
	         emoticons.put(":O", R.drawable.emoticon_gasp);
	         emoticons.put(":o" , R.drawable.emoticon_gasp);
	         emoticons.put(";)", R.drawable.emoticon_wink);
	         emoticons.put("B)", R.drawable.emoticon_glasses);
	         emoticons.put("B|", R.drawable.emoticon_sunglasses);
	         emoticons.put(">:(", R.drawable.emoticon_grumpy);
	         emoticons.put(":/", R.drawable.emoticon_unsure);
	         emoticons.put(":'(", R.drawable.emoticon_cry);
	         emoticons.put("3:)", R.drawable.emoticon_devil);
	         emoticons.put("O:)",R.drawable.emoticon_angel);
	         emoticons.put("o:)", R.drawable.emoticon_angel);
	         emoticons.put(":*", R.drawable.emoticon_kiss);
	         emoticons.put("<3", R.drawable.emoticon_heart);
	         emoticons.put("^_^", R.drawable.emoticon_kiki);
	         emoticons.put("-_-", R.drawable.emoticon_squint);
	         emoticons.put("o.O", R.drawable.emoticon_confused);
	         emoticons.put(">:o", R.drawable.emoticon_upset);
	         emoticons.put(":v", R.drawable.emoticon_pacman);
	         emoticons.put(":3", R.drawable.emoticon_curlylips);
	         emoticons.put(":|]", R.drawable.emoticon_robot);
	         emoticons.put("(^^^)", R.drawable.emoticon_shark);
	         emoticons.put("<(\")", R.drawable.emoticon_penguin);
	         emoticons.put("(y)",R.drawable.emoticon_thumb);
	         emoticons.put("(Y)", R.drawable.emoticon_thumb);
	     }

	     public static Spannable addSmiledText(Context ch, Editable s) {
	    	 
	    	 if(s.toString().contains("https://")){
	    		 return s;
	    	 }
	    	 
	    	 if(s.toString().contains("http://")){
	    		 return s;
	    	 }

	            int index;
	            for (index = 0; index < s.length(); index++) {
	                for (Entry<String, Integer> entry : emoticons.entrySet()) {
	                    int length = entry.getKey().length();
	                    if (index + length > s.length())
	                        continue;
	                   
	                    if (s.subSequence(index, index + length).toString().equals(entry.getKey())) {
	                        s.setSpan(new ImageSpan(ch, entry.getValue()), index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                        index += length - 1;
	                        break;
	                    }
	                }
	            }
	            return s;
	        }
}
package com.yoho.pinyin.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MultitoneUtils {
  
  public static final String[] filterDictArray = {"还", "长", "卡", "传", "乐", "弹",
      "地", "率", "都", "屏", "强", "圈", "省", "盛", "行", "曾", "粘", "重", "扒", "朝",
      "调", "降", "觉", "折", "奇", "宿"};
  
  private static final HanyuPinyinOutputFormat format;
  public static final Set<String> filterDictSet = new HashSet<String>();
  
  static {
    Collections.addAll(filterDictSet, filterDictArray);
    // 初始化读音format
    format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    format.setVCharType(HanyuPinyinVCharType.WITH_V);
  }
  
  //不包含多音字的词获取拼音
  public static PinyinDict getPinyin(String token) {
    char[] charArray = token.toCharArray();
    StringBuffer pinyinBuffer = new StringBuffer();
    StringBuffer pinyinShortBuffer = new StringBuffer();
    for (int i = 0; i < charArray.length; i++) {
      if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
        try {
          String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(charArray[i], format);
          pinyinBuffer.append(pinyinArray[0]);
          pinyinShortBuffer.append(pinyinArray[0].charAt(0));
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinBuffer.append(Character.toString(charArray[i]));
        pinyinShortBuffer.append(Character.toString(charArray[i]));
      }
    }
    PinyinDict pinyinDict = new PinyinDict();
    pinyinDict.setDict(token);
    pinyinDict.setPinyin(pinyinBuffer.toString());
    pinyinDict.setShortPinyin(pinyinShortBuffer.toString());
    return pinyinDict;
  }
  
  /**
   * 判断token字符串中是否存在多音字
   * 
   * @param token
   *          字符串
   * @return true 或者 false
   */
  public static String hasMultitone(String token) {
    char[] charArray = token.toCharArray();
    for (char ch : charArray) {
      if (filterDictSet.contains(Character.toString(ch))) {
        return Character.toString(ch);
      }
    }
    return null;
  }
  
  public static void main(String[] args) {
//    PinyinDict pinyinDict = getPinyin("你好啊");
//    System.out.println(pinyinDict.getPinyin() + "===" + pinyinDict.getShortPinyin());
    System.out.println(hasMultitone("大爱重庆:重庆市红十字会抗震救灾纪实"));
  }
}

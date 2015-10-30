package com.yoho.pinyin.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.elasticsearch.index.config.PinyinElasticConfigurator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

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

public class PinyinUtil {
  private static final HanyuPinyinOutputFormat format;
  private static final int HANZI_SIZE = 50;
  static {
    // 初始化读音format
    format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    format.setVCharType(HanyuPinyinVCharType.WITH_V);
  }
  
  //将所有多音字组合
  private static ArrayList<String> getPinyins(String token) {
    ArrayList<String> pinyinList = new ArrayList<String>();
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    format.setVCharType(HanyuPinyinVCharType.WITH_V);
    
    char[] charArray = token.toCharArray();
    List<Set<String>> pinyinSetList = new ArrayList<Set<String>>();
    List<Set<String>> pinyinShortSetList = new ArrayList<Set<String>>();
    Set<String> pinyinSet = null;
    Set<String> pinyinShortSet = null;
    for (int i = 0; i < token.length(); i++) {
      pinyinSet = new HashSet<String>();
      pinyinShortSet = new HashSet<String>();
      if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
        try {
          String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(charArray[i], format);
          Collections.addAll(pinyinSet, pinyinArray);
          for (String str : pinyinArray) {
            pinyinShortSet.add(Character.toString(str.charAt(0)));
          }
          pinyinSetList.add(pinyinSet);
          pinyinShortSetList.add(pinyinShortSet);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinSet.add(Character.toString(charArray[i]));
        pinyinSetList.add(pinyinSet);
        
        pinyinShortSet.add(Character.toString(charArray[i]));
        pinyinShortSetList.add(pinyinShortSet);
      }
    }
    pinyinList = combinePinyin(pinyinSetList);
    pinyinList.addAll(combinePinyin(pinyinShortSetList));
    return pinyinList;
  }
  
  //获取拼音组合
  public static ArrayList<String> combinePinyin(List<Set<String>> pinyinSetList) {
    List<Set<String>> setList;
    setList = doCombinePinyin(pinyinSetList);
    ArrayList<String> pinyinList = new ArrayList<String>();
    if (setList.size() > 0) {
      pinyinList.addAll(setList.get(0));
    }
    return pinyinList;
  }
  
  public static List<Set<String>> doCombinePinyin(List<Set<String>> pinyinSetList) {
    int len = pinyinSetList.size();
    if (len >= 2) {
        Set<String> temp = new HashSet<String>();
        for (String pinyin : pinyinSetList.get(0)) {
          for (String pinyin2 : pinyinSetList.get(1)) {
            temp.add(pinyin + pinyin2);
          }
        }
        pinyinSetList.remove(0);
        pinyinSetList.remove(0);
        pinyinSetList.add(0, temp);
        return doCombinePinyin(pinyinSetList);
    } else {  
      return pinyinSetList;
    }
  }
  
  /**
   * 将汉字转化为拼音
   * @param token 汉字
   */
  public static ArrayList<String> transToPinyin(String token) {
    ArrayList<String> pinyinList = new ArrayList<String>();
    int length = Math.min(HANZI_SIZE, token.length());
    String subToken = token.substring(0, length);
    PinyinDict pinyins;
    //判断token字符串中是否存在多音字
    if (MultitoneUtils.hasMultitone(subToken) != null) {
      int fclength = Math.min(HANZI_SIZE + 1, token.length());
      String tempSubToken = token.substring(0, fclength);
      PinyinDict pinyinTemp = MatchDict.getPinyinDict(tempSubToken);
      //如果能够找到多音词
      pinyins = new PinyinDict();
      pinyins.setDict(token);
      String dict;
      if (pinyinTemp != null) {
        dict = pinyinTemp.getDict();
        int i = tempSubToken.indexOf(dict);
        pinyins = connectPinyin(pinyins, pinyinTemp, subToken, i, dict.length());
      } else {
        //查找到是哪个多音字，然后找找是否有默认读音
        dict = MultitoneUtils.hasMultitone(subToken);
        pinyinList = getPinyins(subToken);
      }
    } else {
      pinyins = transToPinyinNoMultitone(token, subToken);
    }
    if (pinyins.getPinyin() != null) {
      pinyinList.add(pinyins.getPinyin());
      pinyinList.add(pinyins.getShortPinyin());
    }
    return pinyinList;
  }
  
  //从词典中获取多音字的拼音，将其他拼音连接起来
  private static PinyinDict connectPinyin(PinyinDict pinyins, PinyinDict pinyinDict, String subToken, int i, int length) {
    PinyinDict tempFirst;
    PinyinDict tempEnd;
    if (i > 0 && (i + length) < subToken.length()) {
      tempFirst = transToPinyinNoMultitone(subToken, subToken.substring(0, i));
      tempEnd = transToPinyinNoMultitone(subToken, subToken.substring(i + length, subToken.length()));
      pinyins.setPinyin(tempFirst.getPinyin() + pinyinDict.getPinyin() + tempEnd.getPinyin());
      pinyins.setShortPinyin(tempFirst.getShortPinyin() + pinyinDict.getShortPinyin() + tempEnd.getShortPinyin());
    } else if (i == 0 && length < subToken.length()) {
      tempEnd = transToPinyinNoMultitone(subToken, subToken.substring(length, subToken.length()));
      pinyins.setPinyin(pinyinDict.getPinyin() + tempEnd.getPinyin());
      pinyins.setShortPinyin(pinyinDict.getShortPinyin() + tempEnd.getShortPinyin());
    } else if (i > 0 && (i + length) >= subToken.length()) {
      tempFirst = transToPinyinNoMultitone(subToken, subToken.substring(0, i));
      pinyins.setPinyin(tempFirst.getPinyin() + pinyinDict.getPinyin());
      pinyins.setShortPinyin(tempFirst.getShortPinyin() + pinyinDict.getShortPinyin());
    } else {
      pinyins.setPinyin(pinyinDict.getPinyin());
      pinyins.setShortPinyin(pinyinDict.getShortPinyin());
    }
    return pinyins;
  }

  /**
   * 不包含多音字的词获取拼音
   * token 完整词
   * subToken 词的前六个字
   */
  public static PinyinDict transToPinyinNoMultitone(String token, String subToken) {
    char[] charArray = subToken.toCharArray();
    StringBuffer pinyinBuffer = new StringBuffer();
    StringBuffer pinyinShortBuffer = new StringBuffer();
    int length = charArray.length;
    for (int i = 0; i < length; i++) {
      if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
        try {
          String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(charArray[i], format);
          if (pinyinArray != null) {
              pinyinBuffer.append(pinyinArray[0]);
              pinyinShortBuffer.append(pinyinArray[0].charAt(0));
          }
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
  
  public static void main(String[] args) {
//    Map<String, List<String>> pinyinHanziMaps = new HashMap<String,List<String>>();
//    List<String> list = new ArrayList<String>();
//    list.add("你好");
//    list.add("呵呵呵");
//    pinyinHanziMaps.put("nihao", list);
//    pinyinHanziMaps.put("nihaoaa", list);
//    JSONUtil.toJSON(pinyinHanziMaps);
//    System.out.println(JSONUtil.toJSON(pinyinHanziMaps));
//    
//    String str = JSONUtil.toJSON(pinyinHanziMaps);
//    try {
//      Map<String, List<String>> map = (Map<String, List<String>>) ObjectBuilder.fromJSON(str);
//    } catch (IOException e) {
//      throw new RuntimeException();
//    }
//    ArrayList<String> pinyinList = getPinyins("奇");
//    System.out.println("aaa");
//    try {
//      readMapFromFile(new FileInputStream(new File("E:/workspace/tysolr/booksearch/data/suggest/tst-map.dat")));
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException();
//    } catch (IOException e) {
//      throw new RuntimeException();
//    }
//    System.out.println(1);
	  
	  ArrayList<String> list = PinyinUtil.transToPinyin("长裤");
	  for (String str : list) {
		  System.out.println(str);
	  }
  }
}

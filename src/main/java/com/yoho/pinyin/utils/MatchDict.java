package com.yoho.pinyin.utils;

import java.io.*;
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

public class MatchDict {
  
  private static final Set<String> tokenSet = new HashSet<String>();
  private static final Set<PinyinDict> dictSet = new HashSet<PinyinDict>();
  
  //初始化词典
  public static void init() {
    String fileName = "pinyin/multitoneDict.txt";

    InputStream input;
    try {
      input = new FileInputStream(new File(fileName));
      BufferedReader bufferReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
      String line;
      String[] strs;
      PinyinDict pinyinDict;
      while ((line = bufferReader.readLine()) != null) {
        strs = line.split("\t");
        if (strs.length == 3) {
          tokenSet.add(strs[0].trim());
          pinyinDict = new PinyinDict();
          pinyinDict.setDict(strs[0].trim());
          pinyinDict.setPinyin(strs[1].trim());
          pinyinDict.setShortPinyin(strs[2].trim());
          dictSet.add(pinyinDict);
        }
      }
      bufferReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * 获取多音词条
   * @param token 原始词条
   */
  public static PinyinDict getPinyinDict(String token) {
    for (PinyinDict pinyinDict : dictSet) {
      if (token.contains(pinyinDict.getDict())) {
        return pinyinDict;
      }
    }
    return null;
  }
  
  public static void main(String[] args) {
    MatchDict matchDict = new MatchDict();
    matchDict.init();
    PinyinDict pinyinDict = matchDict.getPinyinDict("传感器");
    if (pinyinDict != null) {
      System.out.println(pinyinDict.getDict() + " " + pinyinDict.getPinyin() + " " + pinyinDict.getShortPinyin());
    } else {
      System.out.println("空");
    }
  }
}

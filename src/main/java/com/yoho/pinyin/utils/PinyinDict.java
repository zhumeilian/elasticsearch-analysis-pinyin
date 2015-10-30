package com.yoho.pinyin.utils;

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

//词典数据结构
public class PinyinDict {
  
  private String dict;
  private String pinyin;
  private String shortPinyin;
  
  public void setDict(String dict) {
    this.dict = dict;
  }
  public String getDict() {
    return dict;
  }
  public void setPinyin(String pinyin) {
    this.pinyin = pinyin;
  }
  public String getPinyin() {
    return pinyin;
  }
  public void setShortPinyin(String shortPinyin) {
    this.shortPinyin = shortPinyin;
  }
  public String getShortPinyin() {
    return shortPinyin;
  }
  
  public String toString() {
    return dict + "\t" + pinyin + "\t" + shortPinyin;
  }

}

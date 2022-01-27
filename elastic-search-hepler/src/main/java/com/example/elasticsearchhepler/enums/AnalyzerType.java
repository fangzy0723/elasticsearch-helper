package com.example.elasticsearchhepler.enums;

/**
 * 常用分词器类型
 **/
public enum AnalyzerType {
    standard,//支持中文采用的方法为单字切分。他会将词汇单元转换成小写形式，并去除停用词和标点符号
    simple,//首先会通过非字母字符来分割文本信息，然后将词汇单元统一为小写形式。该分析器会去掉数字类型的字符
    whitespace,//仅仅是去除空格，对字符没有lowcase化,不支持中文
    keyword,
    pattern,
    ik_max_word,//ik中文分词 最细粒度的拆分
    ik_smart,//ik智能分词 最粗粒度的拆分
    pinyin;
}

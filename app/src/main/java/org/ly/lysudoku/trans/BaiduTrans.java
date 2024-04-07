package org.ly.lysudoku.trans;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import kotlin.jvm.internal.PropertyReference0Impl;

public class BaiduTrans {
    //https://api.fanyi.baidu.com/product/113
    //APP ID：20240123001949039
    //密钥：AFnGcR_E9CKXTTfNTlrM
    /*
    通用翻译API HTTPS 地址：
    https://fanyi-api.baidu.com/api/trans/vip/translate
    生成签名sign：
    Step1. 拼接字符串1：
    拼接appid=2015063000000001+q=apple+salt=1435660288+密钥=12345678得到字符串1：“2015063000000001apple143566028812345678”
    Step2. 计算签名：（对字符串1做MD5加密）
    sign=MD5(2015063000000001apple143566028812345678)，得到sign=f89f9594663708c1605f3d736d01d2d4
     */
    /*
    拼接完整请求：
    http://api.fanyi.baidu.com/api/trans/vip/translate?q=apple&from=en&to=zh&appid=2015063000000001&salt=1435660288&sign=f89f9594663708c1605f3d736d01d2d4
     */
    /*
    输入参数
    请求方式： 可使用 GET 或 POST 方式，如使用 POST 方式，Content-Type 请指定为：application/x-www-form-urlencoded
    字符编码：统一采用 UTF-8 编码格式
    query 长度：为保证翻译质量，请将单次请求长度控制在 6000 bytes以内（汉字约为输入参数 2000 个）
     */
    private static final String BAIDUAPI="https://fanyi-api.baidu.com/api/trans/vip/translate";
    private static final String APPID = "20240123001949039";
    private static final String P="AFnGcR_E9CKXTTfNTlrM";
    private static Random random=new Random();
    public static void Trans(String info,String to,ResponseCallBack responseCallBack) throws UnsupportedEncodingException {
        int s=10000+random.nextInt(100000);
        String s1=APPID+info+s+P;
        String md5=Md5.md5(s1).toLowerCase();
        String url=String.format("%s?q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s",
                BAIDUAPI,
                URLEncoder.encode(info,"UTF-8"),
                "en",
                to,
                APPID,
                s,
                md5);

        OkHttpUtils.getAsync(url,responseCallBack);
    }
}

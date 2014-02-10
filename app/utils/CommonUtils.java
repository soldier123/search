package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.libs.F;
import play.templates.JavaExtensions;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 常用工具类
 * User: wenzhihong
 * Date: 12-9-13
 * Time: 下午12:03
 */
public abstract class CommonUtils {
    public static final String[] DATE_FORMAT_STR_ARR = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmmss"};

    /**
     * 财务报表日期(因为固定)只有月日
     */
    public static final String[] FINANCE_REPORT_DATE_ARR = {"03-31", "06-30", "09-30", "12-31"};

    public static Gson createGson(){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }
   /* public static Gson createIncludeNulls(){
        return new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    }*/
    public static Gson createIncludeNulls(){
        return new GsonBuilder().registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
            public Date read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                //2009-08-29T03:07:13.000Z 目前存的是这种格式的日期
                String value = in.nextString();
              /*  if(value.contains("T") && value.contains("Z")){
                    value =  value.replace("T"," ").replace("Z"," ");
                }*/
                try {
                    //return yyyyMMddHHmmss.parse(value);
                    return DateUtils.parseDate(value, new String[]{"yyyyMMddHHmmss"});
                } catch (ParseException e) {
                    return null;
                }

            }

            public void write(JsonWriter out, Date value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                //  LOCALFORMAT2.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateFormatAsString = DateFormatUtils.format(value, "yyyy-MM-dd HH:mm:ss");
                out.value(dateFormatAsString);
            }
        }).serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    }

    //返回一个gson, 指定日期的格式
    public static Gson createGson(String dateFormate){
        return new GsonBuilder().setDateFormat(dateFormate).create();
    }

    /**
     * 把字符串解析成Date. 支持的字符串格式(yyyy-MM-dd)跟(yyyy-MM-dd hh:mm:ss)
     */
    public static Date parseDate(String d) {
        try {
            return DateUtils.parseDate(d, DATE_FORMAT_STR_ARR);
        } catch (ParseException e) {
            throw new NestableRuntimeException(e);
        }
    }

    /**
     * 根据指定格式获取时间
     * @param     format 格式
     * @param     date 给定时间
     * @return     指定格式的时间
     */
    public static  String getFormatDate(String format,Date date){
        if(date==null){
            return "";

        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 剪切字符串 超过Stringlength长度的部分用'...'代替
     * @return inputStr  剪切后的字符串
     * @param: inputStr  输入的字符串
     * Stringlength     想剪切的长度
     */
    public static String cutString(String inputStr, int Stringlength) {
        if (inputStr == null) {
            return "";
        }
        if (inputStr.length() > Stringlength) {
            inputStr = inputStr.substring(0, Stringlength) + "...";
        }
        return inputStr;
    }

    public static Date calcReportDate2(Date d, int periodCount){
        return parseDate(calcReportDate(d, periodCount));
    }

    /**
     * 根据给定的时间及报表期数,计算所要的报告日期.
     * 如果是向以前推,则periodCount为 负数, 如果是向以后推(将来), 则为正数
     * @param d
     * @param periodCount
     * @return 返回YYYY-MM-dd格式的日期
     */
    public static String calcReportDate(Date d, int periodCount){
        String monthDay = DateFormatUtils.format(d, "MM-dd");
        int year = DateUtils.toCalendar(d).get(Calendar.YEAR);
        int index = 0;
        boolean iscur = false; //是否就是报告期
        for (int i = 0; i < FINANCE_REPORT_DATE_ARR.length; i++) {
            int compareResult = FINANCE_REPORT_DATE_ARR[i].compareTo(monthDay);
            if(compareResult == 0){ //相等
                index = i;
                iscur = true;
                break;
            }
            if(compareResult > 0){ //大于
                index = (i + FINANCE_REPORT_DATE_ARR.length) % FINANCE_REPORT_DATE_ARR.length;
                break;
            }
        }

        if(! iscur){
            periodCount = periodCount -1;
        }

        int addYear = periodCount /  FINANCE_REPORT_DATE_ARR.length;
        int addPeriod = periodCount % FINANCE_REPORT_DATE_ARR.length;

        int lastIndex = (index + addPeriod + FINANCE_REPORT_DATE_ARR.length) % FINANCE_REPORT_DATE_ARR.length;
        if(addPeriod < 0 && index < lastIndex ){
            addYear -= 1;
        }
        if(addPeriod > 0 && index > lastIndex ){
            addYear += 1;
        }
        return (year + addYear) + "-" + FINANCE_REPORT_DATE_ARR[lastIndex];
    }

    public static String calcReportDateByCurDate(int periodCount){
        Date d = new Date();
        return calcReportDate(d, periodCount);
    }

    /**
     * 读取json配制文件,去掉注释. 以 //, /*, #, var  开头的, 都认为是注释. 注意,这里的注释是行注释
     *
     * @param input
     * @return
     */
    public static String readJsonConfigFile2String(InputStream input) {
        StringWriter writer = new StringWriter();
        try {
            LineIterator it = IOUtils.lineIterator(input, "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();
                String linePack = line.trim();
                if (linePack.startsWith("//") || linePack.startsWith("/*") || linePack.startsWith("#")
                        || linePack.startsWith("var ")) { //认为是注释,跳过
                    continue;
                } else {
                    writer.write(line);
                    writer.write(IOUtils.LINE_SEPARATOR);
                }
            }
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(input);
        }

        return writer.toString();
    }




    /**
     * 画图坐标轴上的最大值与最小值. 也就是在最大值上 * 1.1 , 在最小值上 * 0.9. 分别向上向下扩展 10%
     * @return
     */
    public static F.T2<Double, Double> charMinMax(Number[] arr){
        List<Number> numList = new ArrayList<Number>(arr.length);
        for (Number nu : arr) {
            if(nu != null){
                numList.add(nu);
            }
        }

        if(numList.size() == 0){ //如果没有的话
            return F.T2(new Double(0), new Double(10));
        }

        double[] changeArr = new double[numList.size()];
        for (int i = 0; i < numList.size(); i++) {
            changeArr[i] = numList.get(i).doubleValue();
        }

        F.T2<Double, Double> t2 = minMax(changeArr);

        return F.T2(t2._1 * 0.9, t2._2 * 1.1);
    }

    /**
     * 返回int数组里的最小值, 最大值.
     * _1 最小值
     * _2 最大值
     */
    public static F.T2<Integer, Integer> minMax(int[] arr) {
        if (arr.length == 1) {
            new F.T2(arr[0], arr[0]);
        }

        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < minVal) {
                minVal = arr[i];
            }
            if (arr[i] > maxVal) {
                maxVal = arr[i];
            }
        }

        return new F.T2(minVal, maxVal);
    }

    /**
     * 返回long数组里的最小值, 最大值.
     * _1 最小值
     * _2 最大值
     */
    public static F.T2<Long, Long> minMax(long[] arr) {
        if (arr.length == 1) {
            new F.T2(arr[0], arr[0]);
        }

        long minVal = Long.MAX_VALUE;
        long maxVal = Long.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < minVal) {
                minVal = arr[i];
            }
            if (arr[i] > maxVal) {
                maxVal = arr[i];
            }
        }

        return new F.T2(minVal, maxVal);
    }

    /**
     * 返回float数组里的最小值, 最大值.
     * _1 最小值
     * _2 最大值
     */
    public static F.T2<Float, Float> minMax(float[] arr) {
        if (arr.length == 1) {
            new F.T2(arr[0], arr[0]);
        }

        float minVal = Float.MAX_VALUE;
        float maxVal = Float.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < minVal) {
                minVal = arr[i];
            }
            if (arr[i] > maxVal) {
                maxVal = arr[i];
            }
        }

        return new F.T2(minVal, maxVal);
    }

    /**
     * 返回float数组里的最小值, 最大值.
     * _1 最小值
     * _2 最大值
     */
    public static F.T2<Double, Double> minMax(double[] arr) {
        if (arr.length == 1) {
            new F.T2(arr[0], arr[0]);
        }

        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < minVal) {
                minVal = arr[i];
            }
            if (arr[i] > maxVal) {
                maxVal = arr[i];
            }
        }

        return new F.T2(minVal, maxVal);
    }



    /**
     * 把数据库查询出来的对象转成float值。 如果不是number类型，则抛出异常
     * @param obj
     * @return
     */
    public static float dbObject2Float(Object obj){
        if(obj == null){
            throw  new IllegalArgumentException("obj参数为null");
        }
        if(obj instanceof Number){
            return ((Number)obj).floatValue();
        }   else{
            throw  new IllegalArgumentException("obj参数不是number类型，不能转成float");
        }
    }

    //提取出来, 方便以后替换算法
    /**
     * 压缩
     *
     * @param data 待压缩数据
     * @return byte[] 压缩后的数据
     */
    public static byte[] compress(byte[] data) {
        return ZLibUtils.compress(data);
    }

    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        return ZLibUtils.decompress(data);
    }

    /**
     * 把double转成指定位数的double.
     * @param num
     * @param scale  倍数. 如果要扩大, 则为正整数. 2表示 * 100, 缩小则为负整数. -2 表示 除以 100(也就是 乘以 0.01)
     * @param format 两位小数, 则format的格式为 "#.00"
     * @return
     */
    public static double scaleNum(double num, int scale, String format){
        num = num * Math.pow(10, scale);
        String s = JavaExtensions.format(num, format);
        return Double.parseDouble(s);
    }

    public static double scaleNum(float num, int scale, String format){
            double tmp = ((double)num) * Math.pow(10, scale);
            String s = JavaExtensions.format(tmp, format);
            return Double.parseDouble(s);
    }

    public static double scaleNum(Number num, int scale, String format) {
        if (num != null) {
            double tmp = (num.doubleValue()) * Math.pow(10, scale);
            String s = JavaExtensions.format(tmp, format);
            return Double.parseDouble(s);
        }else{
            return 0;
        }
    }

    /**
     * 把字符数组构造成 sql里的where 字符串的连接
     * @return
     */
    public static String sqlStrJoin(String[] arr){
        return sqlStrJoin(Arrays.asList(arr));
    }

    public static String sqlStrJoin(Iterable<String> parts) {
        return sqlStrJoin(parts.iterator());
    }

    public static String sqlStrJoin(Iterator<String> parts) {
        StringBuilder sb = new StringBuilder();
        while (parts.hasNext()) {
            String a = parts.next();
            if (StringUtils.isNotBlank(a)) {
                sb.append("'").append(a).append("',");
            }
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        }
        return "";
    }
}

package org.zj.crow;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.zj.crow.bean.Brand;
import org.zj.crow.bean.Model;
import org.zj.crow.bean.SubVersion;
import org.zj.crow.dao.DBUtil;
import org.zj.crow.http.IResponce;
import org.zj.crow.http.OkHttpUtil;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: crow
 * @BelongsPackage: org.zj.crow
 * @Author: ZhangJun
 * @CreateTime: 2018/12/20
 * @Description: ${Description}
 */
public class Content {

    static DBUtil dbUtil = new DBUtil();
    static PhantomJSDriver driver;
    static final String baseUrl = "https://car.autohome.com.cn";
    static Map<String, String> headMap = new HashMap<String, String>();
    static final String brandUrl = "https://car.autohome.com.cn/price/";
    static final List<String> brandUrls = new ArrayList<String>();
    static final List<Model> modelList = new ArrayList<Model>();
    static List<Brand> brandList = new ArrayList<Brand>();
    static List<SubVersion> subVersionsList = new ArrayList<SubVersion>();

    public static void main(String[] args) throws IOException {

      //  new Content().createTable();
        new Content().doCrowAndInsert();

        //new Content().genBean();
       /* new Content().genBean();

        initHeadMap();
        new OkHttpUtil().baseRequest(headMap,null,"https://car.autohome.com.cn/config/spec/1004577.html#pvareaid=3454541",null, new IResponce() {
            public void handleResult(String responseStr) {
                System.out.println(responseStr);
            }

            public void fail(Exception e) {

            }
        });*/
    }

    /**
     * 开始爬
     */
    public void doCrow() {
        crowBrand();
        crowModel();
        crowSubVersion();

    }

    public void doCrowAndInsert() {
        doCrow();
        doInsert();
    }

    //插入到mysql
    private void doInsert() {
        insertBrand();
        insertModel();
        insertSubversion();
    }

    private void insertSubversion() {
        for (SubVersion subVersion : subVersionsList) {
            String qczj_subversion = genInsertSql("qczj_subversion", subVersion);
            dbUtil.runSql(qczj_subversion);
        }
    }

    private void insertModel() {
        for (Model model : modelList) {
            String qczj_subversion = genInsertSql("qczj_model", model);
            dbUtil.runSql(qczj_subversion);
        }
    }

    private void insertBrand() {
        for (Brand brand : brandList) {
            String qczj_subversion = genInsertSql("qczj_brand", brand);
            dbUtil.runSql(qczj_subversion);
        }
    }

    private void crowSubVersion() {
        for (Model model : modelList) {

            String url = "https://car.autohome.com.cn/config/spec/";
            String s = model.getLink().split(".html")[0].split("-")[1];
            System.out.println("从这个地址来获得数据:---->" + model.getLink());

            //子品牌需要执行js,用phantomjs来获得动态页面
            Element element = getElementBySe("https://car.autohome.com.cn/config/spec/" + s + ".html#pvareaid=3454541");
            inflateSubVersion(element,model.getBrandId(),model.getModelId());
        }
    }

    private void inflateSubVersion(Element element,String brandId,String modelId) {
        subVersionsList.addAll(genSubVersions(element,brandId,modelId));
    }

    /**
     * 爬车型组
     */
    private void crowModel() {
        for (Brand brand : brandList) {
            Element element = getElement(brand.getLink());
            inflateModel(element,brand.getBrandId());
        }
    }

    /**
     * 解析element 并且填充到model的list中
     *
     * @param element
     */
    private void inflateModel(Element element,String brandId) {

        if (element == null) {
            return;
        }

        List<Element> modelEles = element.select("#brandtab-1");
        if (modelEles.size() == 0) {
            return;
        }

        for (Element ele : modelEles.get(0).getElementsByClass("list-cont")) {
            Elements itemElement = ele.getElementsByClass("list-cont-bg");
            Model model = genModel(itemElement);

            if (model != null) {
                model.setBrandId(brandId);
                modelList.add(model);
                System.out.println("车型组  ->" + model);
            }
        }

    }

    /**
     * 生成model
     *
     * @param itemElement
     * @return
     */
    private Model genModel(Elements itemElement) {

        if (itemElement == null) {
            return null;
        }
        Model model = new Model();
        String href = itemElement.select("> div > div.list-cont-img > a").attr("href");
        model.setLink(baseUrl + href);
        String picSrc = itemElement.select("> div.list-cont-img > a > img").attr("src");
        model.setPicLink(baseUrl + picSrc);
        String name = itemElement.select("> .list-cont-main >div > a").text();
        model.setName(name);
        String link = baseUrl + itemElement.select("> div.list-cont-main > div > a").attr("href");
        model.setLink(link);
        String lavel = itemElement.select("> .list-cont-main > div.main-lever > div.main-lever-left > ul > li:nth-child(1) > span").text();
        model.setLavel(lavel);
        String 发动机排量 = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-left > ul > li:nth-child(3) > span > a").text();
        model.setDisplacement(发动机排量);
        String carStruct = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-left > ul > li:nth-child(2) > a").text();
        model.setCarStruct(carStruct);
        String transfer = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-left > ul > li:nth-child(4) > a").text();
        model.setTransfer(transfer);
        String price = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-right > div:nth-child(1) > span > span").text();
        model.setPrice(price);
        String star = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-right > div.score-cont > a > span").text();
        model.setStar(star);
        String id=link.split(".html")[0].split("-")[1];
        model.setModelId(id);

        System.out.println("图片链接" + baseUrl + picSrc);
        System.out.println("名字:" + name);
        System.out.println("链接:" + link);
        System.out.println("等级:" + lavel);
        System.out.println("发动机排量 :" + 发动机排量);
        System.out.println("车身结构:" + carStruct);
        System.out.println("变速箱:" + transfer);
        System.out.println("指导价格" + price);
        System.out.println("用户评分:" + star);
        //接下来是获得颜色
        List<Element> colorEles = itemElement.select("> div.list-cont-main > div.main-lever > div.main-lever-left > ul > li.lever-ul-color > div.carcolor.fn-left");
        StringBuilder sb = new StringBuilder();
        for (Element e : colorEles) {
            String text = e.select("> div > div").text();
            System.out.println("这是颜色  " + text);
            sb.append(text).append(" ");
        }
        model.setColors(sb.toString());
        return model;
    }


    /**
     * 爬汽车品牌
     */
    private void crowBrand() {
        initBrandUrls();
        for (String url : brandUrls) {
            Element element = getElement(url);
            inflateBrand(brandList, element);
        }

        for (Brand brand : brandList) {
            System.out.println("品牌-> " + brand);
        }
    }

    private void initBrandUrls() {
        brandUrls.add("https://car.autohome.com.cn/AsLeftMenu/As_LeftListNew.ashx?typeId=1");
        //brandUrls.add("https://car.autohome.com.cn/AsLeftMenu/As_LeftListNew.ashx?typeId=2");
        //brandUrls.add("https://car.autohome.com.cn/AsLeftMenu/As_LeftListNew.ashx?typeId=3");
    }

    /**
     * 填充到list
     *
     * @param brandList
     * @param element
     */
    private void inflateBrand(List<Brand> brandList, Element element) {
        //解析这个
        List<Element> cartree = element.select("body");

        Element ele = cartree.get(0);
        List<Element> eles = ele.getElementsByTag("ul");

        for (Element e : eles) {
            List<Element> lis = e.getElementsByTag("li");
            for (Element ee : lis) {
                List<Element> a = ee.select("> h3 > a");
                Brand brand = genBean(a.size() != 0 ? a.get(0) : null);
                if (brand != null) {
                    brandList.add(brand);
                }
            }
        }
    }

    /**
     * 把元素解析成bean
     *
     * @param element
     * @return
     */
    private Brand genBean(Element element) {
        if (element == null) {
            return null;
        }
        Brand brand = new Brand();
        String href = element.attr("href");
        String url = baseUrl + href;
        String text = element.text();

        System.out.println(href + "  " + text);

        brand.setBrandId(href.split("-")[1].split(".html")[0]);
        brand.setLink(url);
        brand.setBrandName(text.substring(0, text.indexOf("(")));
        brand.setCount(Integer.parseInt(text.substring(text.indexOf("(") + 1, text.length() - 1)));

        return brand;
    }

    /**
     * 通过url获得element
     *
     * @param url
     * @return
     */
    private Element getElement(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void parseResponseAndInflate() {
        new OkHttpUtil().baseRequest(headMap, null, brandUrl, null, new IResponce() {
            @Override
            public void handleResult(String responseStr) {
                try {
                    String gbk = new String(responseStr.getBytes("UTF-8"), "UTF-8");

                    System.out.println(gbk);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(Exception e) {

            }
        });
    }

    private static void initHeadMap() {

        if (headMap.size() != 0) {
            return;
        }

        headMap.put("Content-Type", "application/json; charset=utf-8");
        headMap.put("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        headMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }

    private void genBean() throws IOException {
        HanyuPinyinOutputFormat hanyuPinyinOutputFormat = new HanyuPinyinOutputFormat();

        List<String> finalList = new ArrayList<String>();

        List<String> list = new ArrayList<String>();
        Element element = getElementBySe("https://car.autohome.com.cn/config/spec/30642.html#pvareaid=3454541");

        List<Element> eles = element.select("#config_data").get(0).getElementsByClass("tbcs");
        int i = 0;
        for (Element ele : eles) {
            String text = ele.select("> tbody > tr > th > div").text();

            String[] strs = text.split(" ");
            for (String s : strs) {
                System.out.println(s);
                list.add(s);
                i++;
            }
            i++;
        }
        System.out.println(i);
        finalList.add("public class SubVersion{");
        System.out.println("--------------");
        //转成拼音
        for (String s : list) {
            finalList.add("private String " + getFirstSpell(s) + ";//" + s);
        }
        finalList.add("}");

        String path = "D:\\java\\IdeaProjects\\base\\crow\\src\\main\\java\\org\\zj\\crow\\bean\\SubVersion.java";
        File file = new File(path);
        file.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));

        for (String s : finalList) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    /**
     * 调用okhttp来获得eleemnt
     *
     * @param url
     * @return
     */
    private Element getElementByOkHttp(String url) {
        initHeadMap();
        final Element[] result = new Element[1];
        new OkHttpUtil().baseRequest(headMap, null, url, null, new IResponce() {
            @Override
            public void handleResult(String responseStr) {
                result[0] = Jsoup.parse(responseStr);
            }

            @Override
            public void fail(Exception e) {

            }
        });
        return result[0];
    }


    private Element getElementBySe(String url) {
        if (driver == null) {
            initDriver();
        }
        //打开页面
        driver.get(url);
        return Jsoup.parse(driver.getPageSource()).body();
    }

    private void initDriver() {

        //设置必要参数
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //ssl证书支持
        dcaps.setCapability("acceptSslCerts", true);
        //截屏支持
        dcaps.setCapability("takesScreenshot", true);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", true);
        //js支持
        dcaps.setJavascriptEnabled(true);
        //驱动支持（第二参数表明的是你的phantomjs引擎所在的路径）
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "C:\\Users\\Java\\Downloads\\phantomjs-2.1.1-windows\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        //创建无界面浏览器对象

        driver = new PhantomJSDriver(dcaps);

        //设置隐性等待（作用于全局）
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    /**
     * 用于获得汉字的首拼音
     *
     * @param chinese
     * @return
     */
    public static String getFirstSpell(String chinese) {
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 根据element 来生成一个subVersion
     *
     * @param
     */
    public void genSubVersion() {
        Element element = getElementBySe("https://car.autohome.com.cn/config/spec/9293.html#pvareaid=3454541");
        List<SubVersion> subVersions = genSubVersions(element,null,null);
        for (SubVersion subVersion : subVersions) {
            System.out.println(subVersion);
        }
    }


    /**
     * 根据element来解析成subversion
     *
     * @param element
     * @return
     */
    private List<SubVersion> genSubVersions(Element element,String brandId,String modelId) {
        List<SubVersion> result = new ArrayList<SubVersion>();
        System.out.println(subVersionsList.size() + " 这是小分类的大小");
        List<Element> elements = element.select("#config_nav > table > tbody > tr > td");

        //这里决定创建多少个bean
        int index=0;
        for (Element ele : elements) {
            SubVersion subVersion = new SubVersion();
            subVersion.setBrandId(brandId);
            subVersion.setModelId(modelId);
            subVersion.setSubversionId(modelId+index++);
            result.add(subVersion);
        }

        //然后遍历下面的所有table
        List<Element> eles = element.select("#config_data > .tbcs");
        for (Element ele : eles) {
            List<Element> eless = ele.select("> tbody  > tr");

            for (Element e : eless) {
                String text = e.select(" >th > div").text();
                //这就对应每一个字段名
                System.out.println(text);

                String fieldName = getFirstSpell(text);

                diTdValues(result, fieldName, e.getElementsByTag("td"));
            }
        }

        return result;
    }

    /**
     * 把这个
     *
     * @param result
     * @param fieldName
     * @param tds
     */
    private void diTdValues(List<SubVersion> result, String fieldName, List<Element> tds) {
        //把这些td里面的值通过反射注入到class里面

        if (result.size() - tds.size() < 0) {
            return;
        }

        int index = result.size() - tds.size();
        for (Element e : tds) {
            System.out.println("    设置呢" + result.get(index));
            String val = e.getElementsByTag("div").text();
            setVal(result.get(index++), fieldName, val);
        }
    }

    /**
     * 设置值进去
     *
     * @param subVersion
     * @param fieldName
     * @param val
     */
    private void setVal(SubVersion subVersion, String fieldName, String val) {
        try {
            Field declaredField = subVersion.getClass().getDeclaredField(fieldName);
            if (declaredField == null) {
                return;
            }
            declaredField.setAccessible(true);
            declaredField.set(subVersion, val);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void createTable() {

        //这里先读取这个java文件的所有行数据
        createBrandTable();
        createModelTable();
        createSubversionTable();

    }

    private void createSubversionTable() {
        List<Map.Entry<String, String>> brands = getAllFieldInfo(new File("D:\\java\\IdeaProjects\\base\\crow\\src\\main\\java\\org\\zj\\crow\\bean\\SubVersion.java"));
        runCreateTable(brands, "qczj_subversion");
    }

    private void runCreateTable(List<Map.Entry<String, String>> datas, String tableName) {
        StringBuilder sb = new StringBuilder("create table " + tableName + "(");
        for (Map.Entry<String, String> entry : datas) {
            sb.append(entry.getKey() + " varchar(50) comment '" + entry.getValue() + "',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        new DBUtil().runSql(sb.toString());
    }

    private void createModelTable() {
        List<Map.Entry<String, String>> brands = getAllFieldInfo(new File("D:\\java\\IdeaProjects\\base\\crow\\src\\main\\java\\org\\zj\\crow\\bean\\Model.java"));
        runCreateTable(brands, "qczj_model");
    }

    private void createBrandTable() {
        List<Map.Entry<String, String>> brands = getAllFieldInfo(new File("D:\\java\\IdeaProjects\\base\\crow\\src\\main\\java\\org\\zj\\crow\\bean\\Brand.java"));
        runCreateTable(brands, "qczj_brand");
    }

    /**
     * 获得这个类的所有字段和注释的键值对
     *
     * @param file
     * @return
     */
    private List<Map.Entry<String, String>> getAllFieldInfo(File file) {
        List<Map.Entry<String, String>> result = new ArrayList<Map.Entry<String, String>>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.contains("//")) {
                    continue;
                }
                final String finalLine = line;
                result.add(new Map.Entry<String, String>() {
                    @Override
                    public String getKey() {
                        String[] s = finalLine.split("//")[0].split(" ");
                        return s[s.length - 1].replaceAll(";", "");
                    }

                    @Override
                    public String getValue() {
                        return finalLine.split("//")[1];
                    }

                    @Override
                    public String setValue(String value) {
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 根据表名和对象生成插入到表要执行的sql
     *
     * @param tableName
     * @param obj
     * @return
     */
    private String genInsertSql(String tableName, Object obj) {
        StringBuilder beforeSb = new StringBuilder("insert into " + tableName + "(");
        StringBuilder afterSb = new StringBuilder("values(");
        for (Field f : obj.getClass().getDeclaredFields()) {
            beforeSb.append(f.getName()).append(",");
            try {
                f.setAccessible(true);
                afterSb.append("'" + f.get(obj)).append("',");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        beforeSb.deleteCharAt(beforeSb.length() - 1);
        beforeSb.append(")");
        afterSb.deleteCharAt(afterSb.length() - 1);
        afterSb.append(")");
        return beforeSb.toString() + afterSb.toString();
    }

    public void testGenInsertSql() {
        Brand brand = new Brand();
        brand.setCount(88);
        brand.setBrandName("三星");
        brand.setLink("www.baidu.com");
        dbUtil.runSql(genInsertSql("qczj_brand", brand));
    }
}

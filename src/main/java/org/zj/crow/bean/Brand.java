package org.zj.crow.bean;

/**
 * @BelongsProject: crow
 * @BelongsPackage: org.zj.crow.bean
 * @Author: ZhangJun
 * @CreateTime: 2018/12/20
 * @Description: ${Description}
 */
public class Brand {
    private String brandId;//品牌id
    private String brandName;//品牌名
    private Integer count;//车型组数量
    private String link;//链接

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", count=" + count +
                ", link='" + link + '\'' +
                '}';
    }
}

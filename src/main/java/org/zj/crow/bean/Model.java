package org.zj.crow.bean;

/**
 * @BelongsProject: crow
 * @BelongsPackage: org.zj.crow.bean
 * @Author: ZhangJun
 * @CreateTime: 2018/12/20
 * @Description: ${Description}
 */
public class Model {
    private String brandId;//品牌id
    private String modelId;//车型组id
    private String name;//名字
    private String lavel;//等級
    private String carStruct;//结构
    private String engine;//引擎
    private String transfer;//变速器
    private String price;//价格
    private String star;//评分
    private String colors;//颜色
    private String picLink;//图片链接
    private String link;//链接
    private String displacement;//发送机排量


    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getDisplacement() {
        return displacement;
    }

    public void setDisplacement(String displacement) {
        this.displacement = displacement;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLavel() {
        return lavel;
    }

    public void setLavel(String lavel) {
        this.lavel = lavel;
    }

    public String getCarStruct() {
        return carStruct;
    }

    public void setCarStruct(String carStruct) {
        this.carStruct = carStruct;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getPicLink() {
        return picLink;
    }

    public void setPicLink(String picLink) {
        this.picLink = picLink;
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", lavel='" + lavel + '\'' +
                ", carStruct='" + carStruct + '\'' +
                ", engine='" + engine + '\'' +
                ", transfer='" + transfer + '\'' +
                ", price='" + price + '\'' +
                ", star='" + star + '\'' +
                ", colors='" + colors + '\'' +
                ", picLink='" + picLink + '\'' +
                ", link='" + link + '\'' +
                ", displacement='" + displacement + '\'' +
                '}';
    }
}

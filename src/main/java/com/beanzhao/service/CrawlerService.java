package com.beanzhao.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerService {
    
    private static final String USER_CONTAINER_URL = "http://m.weibo.cn/u/%s";
    private static final String USER_WEIBO_URL = "http://m.weibo.cn/page/json";
    private static final String FORWARD_IMG_PREFIX = "http://ww2.sinaimg.cn/bmiddle/%s.jpg";
    private static final String USER_CONTAINERID_SUBFIX = "_-_WEIBO_SECOND_PROFILE_WEIBO";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);
    
    public List<String> getContainerIdList(List<String> uidList) {
        List<String> cIdList = new ArrayList<String>();
        for(String uid : uidList) {
            String url = String.format(USER_CONTAINER_URL, uid);
            try {
                Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/42.0.2311.135 Safari/537.36").timeout(10000).get();
                String text = StringEscapeUtils.unescapeHtml(doc.html());
                Pattern pattern = Pattern.compile("'stageId':'([\\s\\S]*?)'};");
                Matcher matcher = pattern.matcher(text);
                while(matcher.find()) {
                    String containerId = matcher.group(1);
                    cIdList.add(containerId);
                }
            } catch (Exception e) {
                LOGGER.error("convert uid to containerid occur exception, url:{}, reason:{}", url, e.getMessage());
            }
        }
        return cIdList;
    }
    
    public Set<String> getUsersHomePageImg(List<String> containerIds, int maxPageNo) {
        Set<String> imgs = new HashSet<String>();
        for(String containerId : containerIds) {
            for(int i=1; i<=maxPageNo; i++) {
                imgs.addAll(getUserHomePageImg(containerId, i));
            }
        }
        return imgs;
    }
    
    public List<String> getUserHomePageImg(String containerId, int pageNo) {
        List<String> imgList = new ArrayList<String>();
        try {
            Connection.Response res = Jsoup.connect(USER_WEIBO_URL).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31")
                    .ignoreContentType(true).data("containerid", containerId + USER_CONTAINERID_SUBFIX, "page", String.valueOf(pageNo))
                    .method(Method.POST)
                    .execute();
                Document doc = res.parse();
                String text = StringEscapeUtils.unescapeHtml(doc.html());
                System.out.println(text);
                //原发微博图
                Pattern pattern = Pattern.compile("\"thumbnail_pic\":\"([\\s\\S]*?)\",");
                Matcher matcher = pattern.matcher(text);
                while(matcher.find()) {
                    String img = matcher.group(1).replace("\\", "");
                    System.out.println("-----img-----:"+ img);
                    imgList.add(img);
                }
                //转发微博图
                Pattern fpattern = Pattern.compile("\"pic_ids\":\\[([\\s\\S]*?)\\]");
                Matcher fmatcher = fpattern.matcher(text);
                while(fmatcher.find()) {
                    String str = fmatcher.group(1);
                    if(StringUtils.isNotBlank(str)) {
                        String[] imgArray = StringUtils.split(str, ",");
                        for(String img : imgArray) {
                            String imgUrl = String.format(FORWARD_IMG_PREFIX, img.substring(1, img.length()-1));
                            imgList.add(imgUrl);
                            System.out.println("-----imgUrl-----:"+ imgUrl);
                        }
                    }
                }
        }catch (Exception e) {
            LOGGER.error("get user weibo image failed, reason:{}", e.getMessage());
        }
        return imgList;
    }
    
    public static void main(String[] args) throws IOException {
        List<String> uidList = new ArrayList<String>();
        uidList.add("5322225955");
//        getContainerIdList(uidList);
//        getUserHomePageImg("1005055322225955", 1);
    }
}

package com.beanzhao.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohucs.org.apache.commons.lang.StringUtils;

public class CrawlerManagerService {
    private ImageUtilsService imgUtilService;
    private CrawlerService crawlerService;
    
    private ExecutorService uploader = Executors.newFixedThreadPool(20);
    private Logger LOGGER = LoggerFactory.getLogger(CrawlerManagerService.class);
    
    public CrawlerManagerService() {
        super();
        this.imgUtilService = new ImageUtilsService();
        this.crawlerService = new CrawlerService();
    }
    
    public Set<String> searchWeiboImage(List<String> uidList, int maxPageNo) {
        List<String> containerIds = crawlerService.getContainerIdList(uidList);
        return crawlerService.getUsersHomePageImg(containerIds, maxPageNo);
    }
    
    public Set<String> searchImages(List<String> uidList, int maxPageNo) {
        Set<String> newImgUrls = new HashSet<String>();
        Set<String> resourceImgUrls = searchWeiboImage(uidList, maxPageNo);
        List<Future<String>> futureList = new ArrayList<Future<String>>();
        for(String url : resourceImgUrls) {
            Future<String> future = uploader.submit(new ImgUploaderRunnable(url));
            futureList.add(future);
        }
        for(Future<String> future : futureList) {
            String url = "";
            try {
                url = future.get();
                if(StringUtils.isNotBlank(url)) {
                    newImgUrls.add(url);
                }
            }catch (Exception e) {
                LOGGER.error("upload image:{} failed, message:{}", url, e.getMessage());
            }
        }
        return newImgUrls;
    }
    
    class ImgUploaderRunnable implements Callable<String> {
        private String url;
        public ImgUploaderRunnable(String url) {
            this.url = url;
        }
        public String call() throws Exception {
            return imgUtilService.getImgUrlbyRemoteUrl(url);
        }
    }
}

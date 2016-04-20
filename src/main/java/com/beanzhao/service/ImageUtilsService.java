package com.beanzhao.service;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beanzhao.util.AttachmentErrorCode;
import com.beanzhao.util.AttachmentException;
import com.beanzhao.util.MD5Util;
import com.sohucs.SohuCSClientException;
import com.sohucs.SohuCSServiceException;
import com.sohucs.auth.BasicSohuCSCredentials;
import com.sohucs.auth.SohuCSCredentials;
import com.sohucs.services.scs.SohuSCS;
import com.sohucs.services.scs.SohuSCSClient;

public class ImageUtilsService {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ImageUtilsService.class);
    
    private static final String BUCKET = "comment";
    private static final String ACCESS_KEY = "";
    private static final String SECRET_KEY = "";
    private static final String ENT_POINT_URL = "";
    private static final String BASE_IMG_URL = "";// 图片的CDN地址
    private SohuSCS scs;

    public ImageUtilsService() {
        super();
        init();
    }

    private void init() {
        SohuCSCredentials cyanCredentials = new BasicSohuCSCredentials(ACCESS_KEY, SECRET_KEY);
        scs = new SohuSCSClient(cyanCredentials);
        scs.setEndpoint(ENT_POINT_URL);
    }
    
    public String getImgUrlbyRemoteUrl(String imgUrl) {
        if (StringUtils.isEmpty(imgUrl)) {
            return null;
        }
        try {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);

            HttpGet get = new HttpGet(imgUrl);
            get.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
            get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            get.setHeader("Referer", "http://www.baidu.com");
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            InputStream in = entity.getContent();

            String fileNameString = MD5Util.md5(imgUrl);
            
            return uploadImg(ImageUtilsService.BUCKET, fileNameString, in);
        } catch (Exception e) {
            LOGGER.info("get the image filestram occur exception imgUrl:{}, reason:{}", imgUrl, e.getMessage());
        }
        return null;
    }
    
    public String uploadImg(String bucketName, String fileName, InputStream input) throws Exception {
        return BASE_IMG_URL + upload(bucketName, fileName, input);
    }
    
    public String upload(String bucketName, String fileName, InputStream input) throws Exception {
        try {
            scs.putObject(bucketName, fileName, input, null);
        } catch (SohuCSServiceException e) {
            LOGGER.error("upload to scs internal exception.", e);
            throw new AttachmentException(AttachmentErrorCode.ATTACHMENT_SCS_ERROR);
        } catch (SohuCSClientException e) {
            LOGGER.error("upload to scs client exception.", e);
            throw new AttachmentException(AttachmentErrorCode.ATTACHMENT_SCS_CLIENT_ERROR);
        } catch (Exception e) {
            // 重新连接云存储
            init();
            LOGGER.error("upload to scs unexpected exception.", e);
            throw new AttachmentException(AttachmentErrorCode.ATTACHMENT_IO_ERROR);
        }
        return fileName;
    }
}

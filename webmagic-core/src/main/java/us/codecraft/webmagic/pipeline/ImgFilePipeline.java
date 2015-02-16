package us.codecraft.webmagic.pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.utils.FilePersistentBase;

public class ImgFilePipeline extends FilePersistentBase implements Pipeline {
	private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * create a FilePipeline with default path"/data/webmagic/"
     */
    public ImgFilePipeline() {
        setPath("/data/webmagic/");
    }

    public ImgFilePipeline(String path) {
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        try {
        	byte[] bytes = resultItems.getBytes();
        	String tail = ".html";
        	if(resultItems.getContentType().contains("image")){
        		tail = ".jpg";
        	}
        	FileOutputStream fos = new FileOutputStream(getFile(path + DigestUtils.md5Hex(resultItems.getRequest().getUrl()) + tail));
        	fos.write(bytes);
        	fos.close();
        } catch (IOException e) {
            logger.warn("write file error", e);
        }
    }
}

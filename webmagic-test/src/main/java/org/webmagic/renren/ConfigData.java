package org.webmagic.renren;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 保存配置文件信息
 * @author Administrator
 *
 */
public class ConfigData {
	Logger logger = LogManager.getLogger(ConfigData.class);
	
	private String userName;
	
	private String passWord;
	
	private String rootDir;
	
	private String dbHost;
	
	private int dbPort;
	
	private static ConfigData configData;
	
	public static ConfigData getInstance(){
		if(configData==null){
			synchronized (ConfigData.class) {
				if (configData ==null){
					configData = new ConfigData();
				}
			}
		}
		return configData;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public String getRootDir() {
		return rootDir;
	}

	public String getDbHost() {
		return dbHost;
	}

	public int getDbPort() {
		return dbPort;
	}

	private ConfigData() {
		init();
	}
	
	private void init(){
		Properties properties = new Properties();
		InputStream in = null;
		try{
			in = new FileInputStream(new File("config/renren.properties"));
			properties.load(in);
			userName = properties.getProperty("userName");
			passWord = properties.getProperty("userPasswd");
			rootDir = properties.getProperty("rootDir");
			dbHost = properties.getProperty("dbHost");
			try{
				dbPort = Integer.parseInt(properties.getProperty("dbPort"));
			}catch(NumberFormatException e){
				dbPort = 6379;
			}
			
		}catch(Exception e){
			userName = "15601589581";
			passWord = "520520";
			rootDir = "c:/人人相册";
			dbHost = "localhost";
		}finally{
			if(in!=null){
				try{
					in.close();
				}catch(Exception e){
					
				}
			}
		}
	}
	
}

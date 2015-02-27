package org.webmagic.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(new File("C:/Users/Administrator/Desktop/车模展.html")));
		StringBuffer buffer = new StringBuffer();
		String line = reader.readLine();
		while(line!=null){
			buffer.append(line);
			line = reader.readLine();
		}
		System.out.println(buffer.toString());
		Pattern p = Pattern.compile("http:((?!http).)*jpg");
		Matcher m = p.matcher(buffer.toString());
		while (m.find()) {
			String url = m.group();
			System.out.println(url);
		}
		
//		test();
	}
	
	private static void test(){
		Pattern p = Pattern.compile("AB((?!hede).)*fa");
		Matcher m = p.matcher("ABheeafafa");
		if(m.find()){
			System.out.println(m.group());
		}
	}

}

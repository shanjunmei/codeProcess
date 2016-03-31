package com.lanhun.codeProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processer {

    public static void main(String[] args) {

	File current = new File(".");
	File[] files = current.listFiles();
	for (File file : files) {
	    if (file.isDirectory()) {
		dirProcess(file, null);
	    }
	}

    }

    private static void dirProcess(File file, String root) {
	if (root == null) {
	    root = file.getAbsolutePath();
	}

	File[] processFiles = file.listFiles();
	for (File file2 : processFiles) {
	    if (!file2.isDirectory()) {
		TextFile textFile = read(file2, root);
		process(textFile);
		write(textFile);
	    } else {
		dirProcess(file2, root);
	    }
	}
    }

    private static void write(TextFile textFile) {
	String fileName = textFile.getFileName();
	fileName = fileName.replace(textFile.getRoot(), textFile.getRoot() + ".cs");
	FileOutputStream fos = null;
	OutputStreamWriter os = null;
	try {
	    File file = new File(fileName);
	    if (!file.getParentFile().exists()) {
		file.getParentFile().mkdirs();
	    }
	    fos = new FileOutputStream(file);
	    os = new OutputStreamWriter(fos, textFile.getEncoding());
	    os.write(textFile.getContent());
	    os.flush();
	    os.close();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}

    }

    /**
     * 文件处理
     * 
     * @param textFile
     */
    private static void process(TextFile textFile) {
	String content = textFile.getContent();
	// set/get方法移除
	String patternExp = "public .*[gs]et.*\\(.*\\).*[\\s\\S]*?}";
	Pattern pattern = Pattern.compile(patternExp);
	Matcher matcher = pattern.matcher(content);
	while (matcher.find()) {
	    content = content.replace(matcher.group(), "");

	}

	// 引用导入转换，去重
	patternExp = "import com.hzins.channel.api.model.info.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	boolean notFirst = false;
	while (matcher.find()) {
	    if (!notFirst) {
		content = content.replace(matcher.group(), "using com.hzins.channel.api.model.info;");
	    } else {
		content = content.replace(matcher.group(), "");
	    }
	    notFirst = true;
	}

	patternExp = "import com.hzins.channel.api.model.common.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    if (!notFirst) {
		content = content.replace(matcher.group(), "using com.hzins.channel.api.model.common;");
	    } else {
		content = content.replace(matcher.group(), "");
	    }
	    notFirst = true;
	}
	
	
	patternExp = "import com.hzins.channel.api.model.req.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    if (!notFirst) {
		content = content.replace(matcher.group(), "using com.hzins.channel.api.model.req;");
	    } else {
		content = content.replace(matcher.group(), "");
	    }
	    notFirst = true;
	}
	
	
	patternExp = "import com.hzins.channel.api.model.resp.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    if (!notFirst) {
		content = content.replace(matcher.group(), "using com.hzins.channel.api.model.resp;");
	    } else {
		content = content.replace(matcher.group(), "");
	    }
	    notFirst = true;
	}
	
	
	patternExp = "import com.hzins.channel.api.model.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    if (!notFirst) {
		content = content.replace(matcher.group(), "using com.hzins.channel.api.model;");
	    } else {
		content = content.replace(matcher.group(), "");
	    }
	    notFirst = true;
	}
	
	content = content.replace("import java.util.List", "using System.Collections.Generic");
	content = content.replace("import java.util.Map", "using System.Collections.Generic");
	content = content.replace("<p>", "<summary>");
	content = content.replace("</p>", "</summary>");
	
	patternExp = "import \\b(com|java)\\b.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	   
	    content = content.replace(matcher.group(), "");

	    notFirst = true;
	}
	
	
	patternExp = "\\[JsonSerialize\\(using.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    System.out.print(matcher.group());
	    content = content.replace(matcher.group(), "[Newtonsoft.Json.JsonConverter(typeof(Hzins.OpenApi.Client.rpc.client.utils.DateTimeConverter))]");
	    notFirst = true;
	}
	
	
	patternExp = "\\[JsonDeserialize\\(using.*";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    System.out.print(matcher.group());
	    content = content.replace(matcher.group(), "");

	    notFirst = true;
	}
	
	

	
	
/*	patternExp = "^(\\s*)\r\n";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    System.out.print(matcher.group());
	    content = content.replace(matcher.group(), "\r\n");
	    notFirst = true;
	}
	
	patternExp = "^(\\s*)\n";
	pattern = Pattern.compile(patternExp);
	matcher = pattern.matcher(content);
	notFirst = false;
	while (matcher.find()) {
	    System.out.print(matcher.group());
	    content = content.replace(matcher.group(), "\n");
	    notFirst = true;
	}*/
	
	content=content.replaceAll("(?m)^\\s+$", "");

	// 私有属性转换为公有属性
	content = content.replace("private", "public");

	
	content = content.replace("Map", "Dictionary");
	

	textFile.setContent(content);
    }

    /**
     * 文件读取
     * 
     * @param fileName
     * @return
     */
    private static TextFile read(File file, String root) {
	TextFile textFile = new TextFile();
	textFile.setRoot(root);
	try {
	    FileReader reader = new FileReader(file);
	    String encoding = reader.getEncoding();
	    textFile.setFileName(file.getAbsolutePath());
	    textFile.setEncoding(encoding);
	    StringBuffer sb = new StringBuffer();
	    int length = 0;
	    char[] buf = new char[1024];
	    while ((length = reader.read(buf)) != -1) {
		sb.append(buf, 0, length);
	    }
	    textFile.setContent(sb.toString());
	    reader.close();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return textFile;
    }

}

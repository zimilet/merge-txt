package com.building;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * 合并多个文本文件的内容，到一个LibreOffice Writer文件中。
 *
 */
public class MergeTxtToOdt {

	private static final Log logger = LogFactory.getLog(MergeTxtToOdt.class);

	private static final int LINE_QUEUE_SIZE = 1024 * 20;

	public static void main(String[] args) {

		// [01]
		if (logger.isDebugEnabled()) {
			logger.debug("[01]遍历(递归)，某个目录下的所有文件。选择文本文件(.txt)。");
		}
		BlockingQueue<File> fileQueue = TraverseFolder.traverse("/root/Dropbox/Notes/", 0);

		if (fileQueue.size() > 0) {

			// [02]
			if (logger.isDebugEnabled()) {
				logger.debug("[02]创建并打开汇总文件(Opening an empty Writer document)");
			}
			XTextDocument xTextDocument = createGatherFile();
			if (null != xTextDocument) {

				// 按行---保存所有的文本内容
				BlockingQueue<String> lineQueue = new ArrayBlockingQueue<String>(LINE_QUEUE_SIZE);

				// [03]
				if (logger.isDebugEnabled()) {
					logger.debug("[03]依次读取一个文本文件的内容，将每一行，转换为String；保存到容器中。");
				}
				File sourcefile = fileQueue.poll();
				while (null != sourcefile) {
					appendToLineQueue(sourcefile, lineQueue);
					sourcefile = fileQueue.poll();
				}

				// [04]
				if (logger.isDebugEnabled()) {
					logger.debug("[04]将容器中的String,写入到LibreOffice Writer文件中。");
				}
				if (lineQueue.size() > 0) {
					insertStrings(xTextDocument, lineQueue);
				}

			}

		}

	}

	/**
	 * 创建汇总文件
	 * 
	 * @return
	 */
	private static XTextDocument createGatherFile() {

		XComponentContext xRemoteContext = null;

		XComponentLoader xComponentLoader = null;

		XComponent xComponent = null;
		XTextDocument xTextDocument = null;

		try {

			// [01]
			// ServiceManager

			// get the remote office component context
			xRemoteContext = Bootstrap.bootstrap();
			if (xRemoteContext == null) {
				System.err.println("ERROR: Could not bootstrap default Office.");
				throw new RuntimeException("ERROR: Could not bootstrap default Office.");
			}

			XMultiComponentFactory xRemoteServiceManager = xRemoteContext.getServiceManager();

			// [02]
			// Desktop Object

			// get the Desktop, we need its XComponentLoader interface to load a
			// new document
			Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop",
					xRemoteContext);
			// query the XComponentLoader interface from the desktop
			xComponentLoader = UnoRuntime.queryInterface(XComponentLoader.class, desktop);

			// [03]
			// Document objects

			// create empty array of PropertyValue structs, needed for
			// loadComponentFromURL
			PropertyValue[] loadProps = new PropertyValue[0];

			// load new writer file
			xComponent = xComponentLoader.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps);
			xTextDocument = UnoRuntime.queryInterface(XTextDocument.class, xComponent);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return xTextDocument;

	}

	/**
	 * 读取一个文本文件的内容，将每一行，转换为String；保存到容器中。
	 * 
	 * @param source
	 * @param lineQueue
	 */
	private static void appendToLineQueue(File source, BlockingQueue<String> lineQueue) {

		// TODO 多线程

		String temp = null;

		// source
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;

		try {

			fileInputStream = new FileInputStream(source);
			inputStreamReader = new InputStreamReader(fileInputStream);
			bufferedReader = new BufferedReader(inputStreamReader);

			// line
			String sLine = bufferedReader.readLine();
			while (null != sLine) {
				temp = sLine.toString();
				lineQueue.add(temp);

				sLine = bufferedReader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != inputStreamReader) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != fileInputStream) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 将容器中的String,写入到LibreOffice Writer文件中。
	 * 
	 * @param xTextDocument
	 * @param lineQueue
	 */
	private static void insertStrings(XTextDocument xTextDocument, BlockingQueue<String> lineQueue) {

		// get the Text-Object of the document and create the cursor.
		// Now it is possible to insert a text at the cursor-position via
		// insertString

		// getting the text object
		XText xText = xTextDocument.getText();

		// create a cursor object
		XTextCursor xTCursor = xText.createTextCursor();

		// inserting Text
		for (String string : lineQueue) {
			xText.insertString(xTCursor, string, false);
			xText.insertString(xTCursor, "\n", false);
		}

	}

}

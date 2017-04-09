package com.building.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.building.TraverseFolder;
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

	private static final int THREAD_SIZE = 10;

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

				// [03]
				if (logger.isDebugEnabled()) {
					logger.debug("[03]多线程。将所有的文本文件,分为多个段。每个段，使用一个线程处理。");
				}
				// (01)
				// 将所有的文本文件,分为多个段。
				List<List<File>> splitListArray = splitList(fileQueue, THREAD_SIZE);

				int splitListArrayLength = splitListArray.size();
				if (splitListArrayLength > 0) {

					// (02)
					// 每个段，使用一个线程处理。

					// <00>
					// 并发容器---多线程共用。<fileName,Lines>
					ConcurrentHashMap<String, List<String>> concurrentHashMap = new ConcurrentHashMap<String, List<String>>();
					// <00>
					CountDownLatch countDownLatch = new CountDownLatch(splitListArrayLength);

					for (List<File> splitList : splitListArray) {

						// <01>
						// 每个段，使用一个线程处理。
						Worker worker = new Worker(countDownLatch, splitList, concurrentHashMap);
						worker.start();

					}

					try {
						// <02>
						// 使当前线程在锁存器倒计数至零之前一直等待,除非线程被中断或超出了指定的等待时间。
						// 当前线程-主线程
						countDownLatch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// [04]
					if (logger.isDebugEnabled()) {
						logger.debug("[04]将容器中的String(lines),写入到LibreOffice Writer文件中。");
					}
					if (concurrentHashMap.size() > 0) {
						insertStrings(xTextDocument, concurrentHashMap);
					}

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
				logger.error("ERROR: Could not bootstrap default Office.");
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
	 * ArrayList分段
	 * 
	 * @param queue
	 * @param pageSize
	 * @return
	 */
	private static <T> List<List<T>> splitList(BlockingQueue<T> queue, int pageSize) {

		// (00)
		List<List<T>> listArray = new ArrayList<List<T>>();

		// (01)
		ArrayList<T> currentList = new ArrayList<T>();

		for (T t : queue) {
			currentList.add(t);

			if (pageSize == currentList.size()) {
				listArray.add(currentList);

				// 创建新的一页
				currentList = new ArrayList<T>();
			}

		}

		if (0 != currentList.size()) {
			listArray.add(currentList);
		}

		return listArray;

	}

	/**
	 * 将容器中的String,写入到LibreOffice Writer文件中。
	 * 
	 * @param xTextDocument
	 * @param concurrentHashMap
	 */
	private static void insertStrings(XTextDocument xTextDocument,
			ConcurrentHashMap<String, List<String>> concurrentHashMap) {

		// get the Text-Object of the document and create the cursor.
		// Now it is possible to insert a text at the cursor-position via
		// insertString

		// getting the text object
		XText xText = xTextDocument.getText();

		// create a cursor object
		XTextCursor xTCursor = xText.createTextCursor();

		// inserting Text
		// 遍历---<fileName,Lines>
		for (Map.Entry<String, List<String>> entry : concurrentHashMap.entrySet()) {

			// <01>
			String fileName = entry.getKey();
			if (logger.isDebugEnabled()) {
				logger.debug("fileName:" + fileName);
			}
			xText.insertString(xTCursor, "标题：" + fileName, false);
			xText.insertString(xTCursor, "\n", false);

			// <02>
			List<String> lines = entry.getValue();
			if (null != lines && lines.size() > 0) {
				for (String line : lines) {
					xText.insertString(xTCursor, line, false);
					xText.insertString(xTCursor, "\n", false);
				}
			}

		}

	}

	/**
	 * 线程:对文本文件进行处理
	 */
	static class Worker extends Thread {

		private CountDownLatch countDownLatch;

		private List<File> filesFragment;

		/**
		 * 文件名-文件内容---&lt;fileName,Lines>
		 */
		private ConcurrentHashMap<String, List<String>> concurrentHashMap;

		public Worker(CountDownLatch countDownLatch, List<File> filesFragment,
				ConcurrentHashMap<String, List<String>> concurrentHashMap) {
			super();
			this.countDownLatch = countDownLatch;
			this.filesFragment = filesFragment;
			this.concurrentHashMap = concurrentHashMap;
		}

		@Override
		public void run() {

			// (01)
			// 处理一组文本文件
			for (File file : filesFragment) {

				// <01>
				String fileName = file.getName();
				if (!concurrentHashMap.containsKey(fileName)) {
					// 未处理过

					// <02>
					// 读取文本文件内容到字符串容器中
					List<String> lines = readLines(file);

					// <03>
					concurrentHashMap.put(fileName, lines);
				}

			}

			// (02)
			// 递减锁存器的计数,如果计数到达零,则释放所有等待的线程
			countDownLatch.countDown();

		}

		/**
		 * 读取一个文本文件的内容，将每一行，转换为String；保存到容器中。
		 * 
		 * @param source
		 */
		private List<String> readLines(File source) {

			List<String> temp = new ArrayList<String>();

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
					temp.add(sLine.toString());

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

			return temp;
		}

	}

}

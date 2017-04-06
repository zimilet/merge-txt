package com.building;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <ul>
 * <li>遍历(递归)，某个目录下的所有文件。</li>
 * <li>选择文本文件(.txt)。</li>
 * <li></li>
 * </ul>
 */
public class TraverseFolder {

	private static final Log logger = LogFactory.getLog(TraverseFolder.class);

	// 阻塞队列大小
	private static final int FILE_QUEUE_SIZE = 100;

	// 文件类型(后缀)
	private static final String[] ALLOW_SUFFIXS = new String[] { "txt" };

	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (logger.isDebugEnabled()) {
			logger.debug("递归遍历，某个目录下的所有文件。");
		}

		BlockingQueue<File> fileQueue = traverse("/root/Dropbox/Notes/", 0);
		if (logger.isDebugEnabled()) {
			logger.debug(fileQueue.size());
		}

	}

	/**
	 * @param folderName
	 *            当前要遍历的目录
	 * @param level
	 *            当前要遍历的目录,相对于最顶级目录的层次
	 */
	public static BlockingQueue<File> traverse(String folderName, int level) {

		// [00]
		// 要使用的文件
		BlockingQueue<File> fileQueue = new ArrayBlockingQueue<File>(FILE_QUEUE_SIZE);

		// [01]
		File folder = new File(folderName);
		if (folder.exists() && folder.isDirectory()) {

			// (1)
			// 当前目录前的层次符号
			for (int i = 0; i < level; i++) {
				System.out.print("-");
			}
			// 当前目录的名字
			System.out.println(folder.getAbsolutePath());

			// (2)
			// 遍历当前目录中的所有文件,即处理下一级层次的所有文件
			// (2-0)层次+1
			level++;

			// 遍历当前文件夹,下的所有文件
			File[] files = folder.listFiles();
			for (File file : files) {
				// (2-0)
				// 为每一条文件记录(文件夹/文件),前添加一个"|"
				System.out.print("|");

				// (2-1)
				// 如果为文件夹,递归遍历
				if (file.isDirectory()) {
					traverse(file.getAbsolutePath(), level);
				} else {
					// (2-2)
					// 如果为文件

					// <01>
					// 显示文件信息
					printFileInfo(level, file);

					// <02>
					// 选择文本文件(.txt)
					if (chooseFile(file, ALLOW_SUFFIXS)) {
						fileQueue.add(file);
					}

				}
			}
		}

		return fileQueue;

	}

	/**
	 * 显示文件信息
	 * 
	 * @param level
	 * @param file
	 */
	private static void printFileInfo(int level, File file) {

		// (1)
		// 文件前的层次符号
		// (1-1)
		// [前level-1],符号为“ ”
		for (int i = 0; i < level - 1; i++) {
			System.out.print(" ");
		}
		// (1-2)
		// [第level],符号为“|”
		System.out.print("|");

		// (2)
		// 文件信息
		System.out.println("--" + file.getName());

	}

	/**
	 * 选择文本文件(.txt)
	 * 
	 * @param file
	 * @param allowSuffixs
	 * @return
	 */
	private static boolean chooseFile(File file, String[] allowSuffixs) {

		boolean choose = false;

		// (1)
		// 文件名
		String fileName = file.getName();

		// (2)
		// 得到后缀
		String[] fileNameArray = fileName.split("\\.");
		String fileNameSuffix = fileNameArray[fileNameArray.length - 1];
		if (logger.isDebugEnabled()) {
			logger.debug("Suffix:" + fileNameSuffix);
		}

		// (3)
		// 是否匹配
		for (String allowSuffix : allowSuffixs) {
			if (fileNameSuffix.indexOf(allowSuffix) > -1) {
				choose = true;
				break;
			}
		}

		return choose;
	}

}

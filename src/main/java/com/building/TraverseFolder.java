package com.building;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 遍历(递归)
 */
public class TraverseFolder {

	private static final Log logger = LogFactory.getLog(TraverseFolder.class);

	public static void main(String[] args) {

		if (logger.isDebugEnabled()) {
			logger.debug("递归遍历，某个目录下的所有文件。");
		}
		traverse("/root/Dropbox/Notes/", 0);
	}

	/**
	 * @param folderName
	 *            当前要遍历的目录
	 * @param level
	 *            当前要遍历的目录,相对于最顶级目录的层次
	 */
	public static void traverse(String folderName, int level) {

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
					// 如果为文件,显示文件信息

					// (2-2-1)
					// 文件前的层次符号
					// (2-2-1-1)
					// 前level-1,符号为“ ”
					for (int i = 0; i < level - 1; i++) {
						System.out.print(" ");
					}
					// (2-2-1-2)
					// 第level,符号为“|”
					System.out.print("|");

					// (2-2-2)
					// 文件信息
					System.out.println("--" + file.getName());
				}
			}
		}

	}

}

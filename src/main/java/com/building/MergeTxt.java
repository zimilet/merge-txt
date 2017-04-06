package com.building;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 合并多个文本文件的内容，到一个文本文件中。
 *
 */
public class MergeTxt {

	private static final Log logger = LogFactory.getLog(MergeTxt.class);

	// private static final int BUFFER_SIZE = 1024;

	public static void main(String[] args) {

		if (logger.isDebugEnabled()) {
			logger.debug("[01]遍历(递归)，某个目录下的所有文件。选择文本文件(.txt)。");
		}
		BlockingQueue<File> fileQueue = TraverseFolder.traverse("/root/Dropbox/Notes/", 0);

		if (fileQueue.size() > 1) {

			// [02]
			if (logger.isDebugEnabled()) {
				logger.debug("[02]创建汇总文件");
			}
			Path gatherFile = createGatherFile();
			if (null != gatherFile) {

				// [03]
				File sourcefile = fileQueue.poll();
				while (null != sourcefile) {

					appendToFile(sourcefile, gatherFile.toFile());
					sourcefile = fileQueue.poll();

				}

			}

		}

	}

	/**
	 * 创建汇总文件
	 * 
	 * @return
	 */
	private static Path createGatherFile() {

		Path gatherFile = null;

		try {

			// Path,使用该类来操作任何文件系统中的文件.类似于JDK6中的File类.
			Path path = Paths.get("/root/Dropbox/Notes/notes.txt" + System.currentTimeMillis());

			gatherFile = Files.createFile(path);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return gatherFile;

	}

	/**
	 * 追加一个文本文件的内容，到另一个文本文件中。
	 * 
	 * @param source
	 * @param target
	 */
	private static void appendToFile(File source, File target) {

		// source
		FileInputStream fileInputStream = null;
		FileChannel inChannel = null;

		// target
		FileOutputStream fileOutputStream = null;
		FileChannel outChannel = null;

		try {

			if (!target.exists()) {
				target.createNewFile();
			}

			fileInputStream = new FileInputStream(source);
			inChannel = fileInputStream.getChannel();

			// XXX append = true
			fileOutputStream = new FileOutputStream(target, true);
			outChannel = fileOutputStream.getChannel();

			inChannel.transferTo(0, inChannel.size(), outChannel);

			// ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			// while (inChannel.read(byteBuffer) != -1) {
			// byteBuffer.flip();
			// outChannel.write(byteBuffer);
			// byteBuffer.clear();
			// }

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (null != outChannel) {
				try {
					outChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != fileOutputStream) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (null != inChannel) {
				try {
					inChannel.close();
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

}

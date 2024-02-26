package com.dcy.rpc.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kyle
 * @date 2024/02/26
 *
 * Scan package and return all class names in the package
 */
public class ScanPackage {

    public static List<String> scanPackage(String packageName) {
        return getAllClassNames(packageName);
    }

    /**
     * Get the full class names of all classes in this package
     *
     * @param packageName
     * @return
     */
    private static List<String> getAllClassNames(String packageName) {
        // 1.Get the absolute path by passing in packageName
        // com.dcy.xxx.yyy -> D://xxx/xww/sss/com/dcy/xxx/yyyl
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);

        if (url == null) {
            throw new RuntimeException("Path does not exist during package scan");
        }

        String absolutePath = url.getPath();

        List<String> classNameList = new ArrayList<>();
        classNameList = recursionFile(absolutePath, classNameList, basePath);

        return classNameList;
    }

    /**
     * Process files recursively
     *
     * @param absolutePath
     * @param classNameList
     * @param basePath
     * @return
     */
    private static List<String> recursionFile(String absolutePath, List<String> classNameList, String basePath) {
        // 1.Get file
        File file = new File(absolutePath);
        // 2.Determine whether the file is a folder
        if (file.isDirectory()) {
            // Find all files in a folder
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNameList;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNameList, basePath);
                } else {
                    // 文件 --> 类的全类名称
                    String className = getCLassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNameList.add(className);
                }
            }

        } else {
            // file --> Full class name of the class
            String className = getCLassNameByAbsolutePath(absolutePath, basePath);
            classNameList.add(className);
        }
        return classNameList;
    }

    /**
     * Get the full class name of a class by absolute path
     *
     * @param absolutePath
     * @return
     */
    private static String getCLassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\"))).replaceAll("\\\\", ".");
        String substring = fileName.substring(0, fileName.indexOf(".class"));
        return substring;
    }

}

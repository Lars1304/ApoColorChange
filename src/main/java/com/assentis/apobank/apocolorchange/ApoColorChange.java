package com.assentis.apobank.apocolorchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.assentis.apobank.apocolorchange.staging.RepoElement;
import com.assentis.apobank.apocolorchange.staging.Staging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class ApoColorChange {

    private static Path tempDir = null;
    private static Properties properties = null;
    private static String oldColor = null;
    private static String newColor = null;

    private static final int BUFFER = 4 * 1024 * 1024;

    public static void main(String[] args) {
        getProperties(Paths.get(args[0]));
        oldColor = properties.getProperty("oldColor");
        newColor = properties.getProperty("newColor");
        try {
            System.out.println("Prom processing started at - " + new Date());
            processPromFile(Paths.get(properties.getProperty("inputProm")), Paths.get(properties.getProperty("outputProm")));
            System.out.println("Prom processing finished at - " + new Date());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void processPromFile(Path inputProm, Path outputProm) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HH.mm.ss").format(new Date());
        tempDir = inputProm.resolveSibling(timeStamp);

        try {
            System.out.println("Reading prom file ...");
            extractPromFile(inputProm, tempDir);
        } catch (IOException e) {
            System.out.println("Cannot extract prom file");
            e.printStackTrace();
        }

        Staging staging = null;
        try {
            staging = new Staging(file2Document(tempDir + "/staging.xml"));
        } catch (Exception e) {
            System.out.println("Cannot read staging file.");
            e.printStackTrace();
        }

        String[] elementTypes = properties.getProperty("elementTypes").split(",");
        for (int i=0;i<elementTypes.length;i++){
            for (RepoElement element : staging.getElementByType(elementTypes[i])){
                if(changeColor(element)) {
                    System.out.println(element.getElementPath() + "(" + element.getElementId() + ") has been changed.");
                }
            }

        }

        System.out.println("Writing prom file ...");

        createPromFile(outputProm,tempDir);

        if ("yes".equalsIgnoreCase(properties.getProperty("removeTemp"))) {
            try {
                removeTempDir();
            } catch (IOException e) {
                System.out.println("Cannot delete temporary folder " + tempDir.toString());
                e.printStackTrace();
            }
        }

    }

    private static boolean changeColor(RepoElement element){
        String filename = tempDir + "/" + element.getElementId() + "." + element.getElementType();

        String content = null;

        try {
            content = file2String(filename);
        } catch (IOException e) {
            System.out.println("Cannot read " + filename);
            e.printStackTrace();
        }

        String result = content.replaceAll(oldColor, newColor);

        boolean returnValue = !content.equals(result);
        if (returnValue) {
            try {
                string2File(result,filename);
            } catch (IOException e) {
                System.out.println("Cannot write " + filename);
                e.printStackTrace();
            }
        }
        return returnValue;

    }

    private static Document file2Document(String fileName) throws IOException, SAXException, ParserConfigurationException {
        return file2Document(new File(fileName));
    }

    private static Document file2Document(File file) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);

    }

    private static String file2String(String filename) throws IOException {
        Path path = Paths.get(filename);

        int fileLength = (int) Files.size(path);
        char[] temp = new char[(int) fileLength];
        BufferedReader reader = Files.newBufferedReader(path,StandardCharsets.UTF_8);
        reader.read(temp);
        reader.close();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <fileLength-1;i++){
            if (temp[i] != 0) {
                sb.append(temp[i]);
            }
        }
        return new String(sb);
        //return temp.toString();
    }

    private static void string2File(String content, String filename) throws IOException {
        Path path = Paths.get(filename);

        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private static void extractPromFile(Path inputPromFile, Path tempDir) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(inputPromFile.toFile()), BUFFER))) {
            Files.createDirectories(tempDir);

            byte[] data = new byte[BUFFER];
            int len;
            String filename;
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                filename = FilenameUtils.getName(zipEntry.getName());
                try (OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(tempDir.resolve(filename).toFile()), BUFFER)) {
                    while ((len = zis.read(data)) > 0) {
                        os.write(data, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void createPromFile(Path outputPromFile, Path tempdir){

        try {
            ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(outputPromFile));
            Files.walkFileTree(tempdir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = tempdir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getProperties(Path file) {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(file.toFile()));
        } catch (FileNotFoundException e) {
            System.out.println("Properties file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO-Error");
            e.printStackTrace();
        }

    }

    private static void removeTempDir() throws IOException {
        FileUtils.deleteDirectory(tempDir.toFile());
    }

}

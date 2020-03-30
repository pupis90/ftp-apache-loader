package vg.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import sun.net.ftp.FtpProtocolException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import static java.nio.file.Files.newOutputStream;

public class MainFtpLoader {

    public static String ftpServer = "ftp.zakupki.gov.ru";
    public static String user = "free";
    public static String passw = "free";
    public static String tabs = "";

    public static void main(String[] args) {

        String srcCatalog = "fcs_regions";
        String destinationCatalog = "C:/" + srcCatalog;
        String fileFilter = "contract"; // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
        String catalogFlter = "fcs_regions";
        String dateFilter = "2019";
        boolean info = true;
        boolean load = false;

        //Содержание /fcs_regions/Adygeja_Resp/contracts/

        FTPClient apachFtpClient = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        config.setLenientFutureDates(true); // change required options
        config.setServerLanguageCode("ru");
        apachFtpClient.configure(config);
        String workdir = "/";

        try {

            //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
            //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));
            apachFtpClient.connect(ftpServer, 21);

            int reply = apachFtpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                apachFtpClient.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }

            apachFtpClient.login(user, passw);

            if (info) {
                FtpStatistic ftpStatisticI = new FtpStatistic();
                printFtpCatalogInfo(ftpStatisticI, fileFilter, catalogFlter, apachFtpClient, workdir);
                System.err.println("Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpStatisticI.filesTotalCount);
                System.err.println("Объем всех отфильтрованых файлов " + ftpStatisticI.folderVolume);

                //	System.err.println( "*******************" + new Date() + " Stop");
                String mess = "Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpStatisticI.filesTotalCount + System.lineSeparator();
                mess += "Объем всех отфильтрованых файлов " + ftpStatisticI.folderVolume + System.lineSeparator();
                mess += " Фильр каталогов " + catalogFlter + " ;  Фильтра файлов: " + fileFilter;
                writeLog(mess);
            }

            if (load) {
                FtpStatistic ftpStatistic = new FtpStatistic();
                //loadFromFtp(ftpStatistic, destinationCatalog,  fileFilter,  catalogFlter, apachFtpClient, workdir);
            }


            //     sunFtpFirstClient.close();
            /**
             TelnetInputStream t = cl.get("HEADER.txt");
             byte[] b = new byte[200];
             //читаю по 200 байт из файла HEADER.txt
             int readed = t.read(b);
             while(t.read(b) > 0) {
             System.out.println(new String(b));
             }
             */

            apachFtpClient.disconnect();

        } catch (IOException | FtpProtocolException e) {
            e.printStackTrace();
        } finally {
            if (apachFtpClient.isConnected()) {
                try {
                    apachFtpClient.disconnect();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

    }

    /**
     * @param ftpStatistic
     * @param fileFilter
     * @param catalogFilter
     * @param ftpClient
     * @return
     * @throws IOException
     * @throws FtpProtocolException
     */
    private static FtpStatistic printFtpCatalogInfo(FtpStatistic ftpStatistic, String fileFilter, String catalogFilter,
                                                    org.apache.commons.net.ftp.FTPClient ftpClient, String dir)
            throws IOException, FtpProtocolException {

        String folder_name = " ";
        String mess = ""; //dir + " : " + new Date() + " Start"+System.lineSeparator();
        //			System.err.print(dir + " : " + new Date() + " Start"+System.lineSeparator());
        long currDirFilesStartCnt = ftpStatistic.filesTotalCount;
        long currDirFilesVolume = ftpStatistic.folderVolume;
        FTPFile[] files = ftpClient.listFiles(dir);


        //Идем по файлам каталога
        for (FTPFile fileOrDir : files) {
            folder_name = fileOrDir.getName();
            //-----------
            if (fileOrDir.isFile()) {
                if (folder_name.contains(fileFilter)) {
                    ftpStatistic.filesTotalCount++;//число файлов
                    ftpStatistic.folderVolume += fileOrDir.getSize();
                }
            }
        }

        for (FTPFile fileOrDir : files) {
            folder_name = fileOrDir.getName();
            String prefix = dir;
            if (dir.equals("/")) prefix = "";
            String path = prefix + "/" + folder_name;
            //----------
            if (fileOrDir.isDirectory()) {
                if (path.contains(catalogFilter)) {
                    String beaforTabs = tabs;
                    tabs += tabs;
                    printFtpCatalogInfo(ftpStatistic, fileFilter, catalogFilter, ftpClient, path);
                    tabs = beaforTabs;
                }
            }
        }


        //С учетом филтров

        long countrFilesInDir = ftpStatistic.filesTotalCount - currDirFilesStartCnt;
        long volumeDir = ftpStatistic.folderVolume - currDirFilesVolume;
        if (countrFilesInDir != 0 || volumeDir != 0) {
            mess += tabs + dir + " : " + countrFilesInDir + " шт. Объем: " + volumeDir + " b " + System.lineSeparator();
            System.err.print(mess);
            writeLog(mess);
        }

        return ftpStatistic;

    }

    private static void writeLog(String mess) throws IOException {

        Path path = null;
        try {
            path = Paths.get(new URI("file:///" + "D:" + "/" + "logftp.txt"/*folder_name*/));
        } catch (URISyntaxException e) {

        }
        OutputStream outputStream = newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        outputStream.write(mess.getBytes());
    }

    /**
     * @param ftpStatistic
     * @param destinationCatalog
     * @param fileFilter
     * @param catalogFlter
     * @param ftpClient
     * @throws FtpProtocolException
     * @throws IOException
     */
    private static void loadFromFtp(FtpStatistic ftpStatistic, String destinationCatalog, String fileFilter, String catalogFlter, org.apache.commons.net.ftp.FTPClient ftpClient, String dir)
            throws FtpProtocolException, IOException {
        long count_contracts = 0L;
        String folder_name = " ";
        FTPFile[] files = ftpClient.listFiles(dir);

        for (FTPFile fileOrDir : files) {
            folder_name = fileOrDir.getName();
            Path path = null;
            try {
                path = Paths.get(new URI("file:///" + destinationCatalog + "/" + folder_name));
                OutputStream outputStream = newOutputStream(path, StandardOpenOption.CREATE);
                System.err.println(folder_name + " : " + new Date() + " Start");
                ftpClient.retrieveFile(folder_name, outputStream);
                System.err.println(folder_name + " : " + new Date() + " Stop");
                count_contracts++;

            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
            System.err.println("Число загруженных файлов " + count_contracts);
        }

    }


}

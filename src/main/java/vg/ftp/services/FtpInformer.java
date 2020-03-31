package vg.ftp.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import sun.net.ftp.FtpProtocolException;
import vg.ftp.data.FtpServerInfo;

import java.io.IOException;


public class FtpInformer {

    //@ToDo Это все в свойства и в Фильтры распихать
    private String srcCatalog = "fcs_regions";
    private String destinationCatalog = "C:/" + srcCatalog;
    private String fileFilter = "contract"; // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
    private String catalogFilter = "fcs_regions";
    private String dateFilter = "2019";
    private String tabs = "";
    private FTPClientConfig config;
    private FtpServerInfo ftpServerInfo;
    //Содержание /fcs_regions/Adygeja_Resp/contracts/

    public FtpInformer(FTPClientConfig _config, FtpServerInfo _ftpServerInfo) {
        config = _config;
        ftpServerInfo = _ftpServerInfo;
    }

    public void loadInfo() {
        FTPClient apachFtpClient = new FTPClient();
        apachFtpClient.configure(config);
        String workdir = "/";

        try {
            //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
            //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));
            apachFtpClient.connect(ftpServerInfo.ftpServer, 21);

            int reply = apachFtpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                apachFtpClient.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }

            apachFtpClient.login(ftpServerInfo.user, ftpServerInfo.passw);
            ftpServerInfo = new FtpServerInfo();
            gerFtpCatalogInfo(ftpServerInfo, fileFilter, catalogFilter, apachFtpClient, workdir);
            System.err.println("Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpServerInfo.filesTotalCount);
            System.err.println("Объем всех отфильтрованых файлов " + ftpServerInfo.folderVolume);

            //	System.err.println( "*******************" + new Date() + " Stop");
            String mess = "Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpServerInfo.filesTotalCount + System.lineSeparator();
            mess += "Объем всех отфильтрованых файлов " + ftpServerInfo.folderVolume + System.lineSeparator();
            mess += " Фильр каталогов " + catalogFilter + " ;  Фильтра файлов: " + fileFilter;
            VLogger.writeLog(mess, destinationCatalog + ".txt");
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
     * @param ftpServerInfo
     * @param fileFilter
     * @param catalogFilter
     * @param ftpClient
     * @return
     * @throws IOException
     * @throws FtpProtocolException
     */
    private FtpServerInfo gerFtpCatalogInfo(FtpServerInfo ftpServerInfo, String fileFilter, String catalogFilter,
                                            org.apache.commons.net.ftp.FTPClient ftpClient, String dir)
            throws IOException, FtpProtocolException {

        String folder_name = " ";
        String mess = ""; //dir + " : " + new Date() + " Start"+System.lineSeparator();
        //			System.err.print(dir + " : " + new Date() + " Start"+System.lineSeparator());
        long currDirFilesStartCnt = ftpServerInfo.filesTotalCount;
        long currDirFilesVolume = ftpServerInfo.folderVolume;
        FTPFile[] files = ftpClient.listFiles(dir);


        //Идем по файлам каталога
        for (FTPFile fileOrDir : files) {
            folder_name = fileOrDir.getName();
            //-----------
            if (fileOrDir.isFile()) {
                if (folder_name.contains(fileFilter)) {
                    ftpServerInfo.filesTotalCount++;//число файлов
                    ftpServerInfo.folderVolume += fileOrDir.getSize();
                }
            }
        }

        for (FTPFile fileOrDir : files) {
            folder_name = fileOrDir.getName();
            String prefix = dir;
            if (dir.equals("/")) prefix = "";
            String spath = prefix + "/" + folder_name;
            //----------
            if (fileOrDir.isDirectory()) {
                if (spath.contains(catalogFilter)) {
                    String beaforTabs = tabs;
                    tabs += tabs;
                    gerFtpCatalogInfo(ftpServerInfo, fileFilter, catalogFilter, ftpClient, spath);
                    tabs = beaforTabs;
                }
            }
        }


        //С учетом филтров

        long countrFilesInDir = ftpServerInfo.filesTotalCount - currDirFilesStartCnt;
        long volumeDir = ftpServerInfo.folderVolume - currDirFilesVolume;
        if (countrFilesInDir != 0 || volumeDir != 0) {
            mess += tabs + dir + " : " + countrFilesInDir + " шт. Объем: " + volumeDir + " b " + System.lineSeparator();
            System.err.print(mess);
            VLogger.writeLog(mess, destinationCatalog + ".txt");
        }

        return ftpServerInfo;

    }


}

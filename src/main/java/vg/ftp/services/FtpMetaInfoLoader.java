package vg.ftp.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import sun.net.ftp.FtpProtocolException;
import vg.ftp.config.ApplicationConfiguration;
import vg.ftp.model.FtpServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FtpMetaInfoLoader {

    private static final Logger logger = LogManager.getLogger(FtpMetaInfoLoader.class);
    //@ToDo Это все в свойства и в Фильтры распихать
    private String logCatalog = "logftp";
    private String destinationCatalog = "D:/" + logCatalog;
    private String fileFilter = "contract"; // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
    private String catalogFilter = "fcs_regions";
    private String dateFilter = "2019";
    private String tabs = "";
    private FTPClientConfig config;
    private FtpServerInfo ftpServerInfo;
    private String workdir;
    private int treeDeep;
    private List<String> subWorkDirFolderNames;
    //Содержание /fcs_regions/Adygeja_Resp/contracts/
    @Autowired
    ApplicationConfiguration applicationConfiguration;

    public FtpMetaInfoLoader(FTPClientConfig _config, FtpServerInfo _ftpServerInfo) {
        config = _config;
        ftpServerInfo = _ftpServerInfo;
        subWorkDirFolderNames = new ArrayList<>();

    }

    public void requestMetaInfoIfNeedFull() {
        int currentWorkdirSubFolderLevel = 0;

        FTPClient apachFtpClient = new FTPClient(); //ToDo получать из контекста как прототип??
        apachFtpClient.configure(config);


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
            workdir = applicationConfiguration.getSourceWorkdir();
            gerFtpCatalogInfo(ftpServerInfo, fileFilter, catalogFilter, apachFtpClient, workdir, currentWorkdirSubFolderLevel);
            System.err.println("Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpServerInfo.filesTotalCount);
            System.err.println("Объем всех отфильтрованых файлов " + ftpServerInfo.folderVolume);

            //	System.err.println( "*******************" + new Date() + " Stop");
            String mess = "Число всех отфильтрованых файлов без подсчета внутри архивов " + ftpServerInfo.filesTotalCount + System.lineSeparator();
            mess += "Объем всех отфильтрованых файлов " + ftpServerInfo.folderVolume + System.lineSeparator();
            mess += " Фильр каталогов " + catalogFilter + " ;  Фильтра файлов: " + fileFilter;
            logger.info(mess);
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
     * @param _currentWorkdirSubFolderLevel
     * @return
     * @throws IOException
     * @throws FtpProtocolException
     */
    private FtpServerInfo gerFtpCatalogInfo(FtpServerInfo ftpServerInfo, String fileFilter, String catalogFilter,
                                            FTPClient ftpClient, String dir, int _currentWorkdirSubFolderLevel)
            throws IOException, FtpProtocolException {

        int currentWorkdirSubFolderLevel = _currentWorkdirSubFolderLevel;
        if (!applicationConfiguration.isNeedFullMetaInfo() && currentWorkdirSubFolderLevel > 0) return ftpServerInfo;
        currentWorkdirSubFolderLevel++;
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
                    if (currentWorkdirSubFolderLevel == 1) {
                        subWorkDirFolderNames.add(spath);
                    }
                    String beaforTabs = tabs;
                    tabs += tabs;
                    gerFtpCatalogInfo(ftpServerInfo, fileFilter, catalogFilter, ftpClient, spath, currentWorkdirSubFolderLevel);
                    tabs = beaforTabs;
                }
            }
        }


        //С учетом филтров

        long countrFilesInDir = ftpServerInfo.filesTotalCount - currDirFilesStartCnt;
        long volumeDir = ftpServerInfo.folderVolume - currDirFilesVolume;
        if (countrFilesInDir != 0 || volumeDir != 0) {
            mess += tabs + dir + " : " + countrFilesInDir + " шт. Объем: " + volumeDir + " b " + System.lineSeparator();
            logger.info(mess);
        }

        return ftpServerInfo;

    }

    public List<String> getSrcWorkDirSubFoldersAndFiles() {
        return subWorkDirFolderNames;
    }
}

package vg.ftp.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import vg.ftp.model.FtpServerInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import static java.nio.file.Files.newOutputStream;

public class FtpFileLoaderImpl implements FtpFileLoader, ApplicationContextAware {

    FTPClient apachFtpClient;
    Path destinationRootPath; //Например С:\
    String destinationUndoCatalog;

    String rootdir = "/";

    public void setDestinationRootPath(Path destinationRootPath) {
        this.destinationRootPath = destinationRootPath;
    }

    public void setDestinationSubCatalog(String destinationUndoCatalog) {
        this.destinationUndoCatalog = destinationUndoCatalog;
    }

    public void setRootdir(String rootdir) {
        this.rootdir = rootdir;
    }

    private ApplicationContext ctx;
    private FtpServerInfo ftpServerInfo;
    private FtpPathFilter ftpPathFilter;
    private FTPClientConfig config;
    private String currDir;

    public FtpFileLoaderImpl() {
    }

    public void establishFtpConnection() {
        apachFtpClient = new FTPClient();
        config = (FTPClientConfig) ctx.getBean("client-config");
        ftpServerInfo = (FtpServerInfo) ctx.getBean("server-info");
        apachFtpClient.configure(config);
        //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
        //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));
        try {
            apachFtpClient.connect(ftpServerInfo.ftpServer, 21);
            int reply = apachFtpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                apachFtpClient.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            apachFtpClient.login(ftpServerInfo.user, ftpServerInfo.passw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public int loadFile(String dir) {
        currDir = dir;
        try {
            long count_contracts = 0L;
            String srcFileName = " ";
            String srcFolderName = " ";
            FTPFile[] files = apachFtpClient.listFiles(dir);
            Path fileDestinationPath = null;

            /**-----------------------------*/
            //Идем по файлам каталога
            for (FTPFile fileOrDir : files) {
                if (fileOrDir.isFile()) {
                    srcFileName = fileOrDir.getName();
                    String parentDir = dir;
                    if (dir.equals("/")) parentDir = "";
                    String strSrcAbsoluteFileName = parentDir + "/" + srcFileName;
                    String mess = Thread.currentThread().getName() + " " + strSrcAbsoluteFileName + " ... " + System.lineSeparator();
                    //  System.err.print(mess);
                    //-----------/fcs_regions/addinfo_Adygeja_Resp_2015120100_2016010100_001.xml.zip
                /*    if(strSrcAbsoluteFileName.equals("/fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip"))
                        System.err.print(strSrcAbsoluteFileName);
                  */
                    if (ftpPathFilter.isFileNameMatched(strSrcAbsoluteFileName) && ftpPathFilter.isCatalogNameMatched(parentDir)) {
                        System.err.println(Thread.currentThread().getName() + " " + strSrcAbsoluteFileName + " : " + new Date() + " Start");
                        fileDestinationPath = Paths.get(new URI("file:///" + destinationRootPath.toString().replace("\\", "") + "/" + destinationUndoCatalog + strSrcAbsoluteFileName));
                        //System.err.println(Thread.currentThread().getName() + " " + " Сохраняем в " + fileDestinationPath);
                        Path parentDirPath = fileDestinationPath.getParent();
                        if (!Files.exists(parentDirPath)) Files.createDirectories(parentDirPath);
                        OutputStream outputStream = newOutputStream(fileDestinationPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        apachFtpClient.retrieveFile(strSrcAbsoluteFileName, outputStream);
                        System.err.println(Thread.currentThread().getName() + " " + strSrcAbsoluteFileName + " : " + new Date() + " Stop");
                        count_contracts++;
                        outputStream.flush();
                        outputStream.close();

                    }

                }
            }

            for (FTPFile fileOrDir : files) {
                if (fileOrDir.isDirectory()) {
                    srcFolderName = fileOrDir.getName();
                    String parentDir = dir;
                    if (dir.equals("/")) parentDir = "";
                    String strSrcAbsoluteFolderName = parentDir + "/" + srcFolderName;
                    //----------
                    if (strSrcAbsoluteFolderName.startsWith("/fcs_regions")/*||strSrcAbsoluteFolderName.startsWith("/")*/) {
                        String mess = Thread.currentThread().getName() + "  FOLDER: " + strSrcAbsoluteFolderName + System.lineSeparator();
                        System.err.print(mess);
                        loadFile(strSrcAbsoluteFolderName);
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            System.err.print(" current Dir " + currDir + System.lineSeparator());
            try {

                //    apachFtpClient.connect(ftpServerInfo.ftpServer, 21);
                int reply = apachFtpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    System.err.print(" По видимому ФТП не отвечает" + System.lineSeparator());
                    System.out.print(apachFtpClient.getReplyString() + System.lineSeparator());
                    apachFtpClient.disconnect();
                    System.err.print(" Переподключение ... " + System.lineSeparator());
                    establishFtpConnection();
                }
                //   apachFtpClient.login(ftpServerInfo.user, ftpServerInfo.passw);
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }


        return 0;
    }

    @Override
    public int loadDir(String dir) {
        return 0;
    }

    @Override
    public int setPathFilter(FtpPathFilter _ftpPathFilter) {
        ftpPathFilter = _ftpPathFilter;
        return 0;
    }

    @Override
    public int setTxtFileInMemoryContentFilter(InMemoryContentFilter inMemoryContentFilter) {
        return 0;
    }

    @Override
    public void run() {
        Date startLoadDate = new Date();
        String mess = startLoadDate + " " + Thread.currentThread().getName() + " Старт загрузки " + System.lineSeparator();
        System.err.print(mess);
        loadFile(rootdir);
        Date endLoadDate = new Date();
        mess += endLoadDate + Thread.currentThread().getName() + " Загрузка с сервера " + " завершена" + System.lineSeparator();
        System.err.print(mess);
        try {
            VLogger.writeLog(mess, destinationRootPath + "/" + destinationUndoCatalog + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

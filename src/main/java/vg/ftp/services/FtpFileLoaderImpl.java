package vg.ftp.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import vg.ftp.data.FtpServerInfo;

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

public class FtpFileLoaderImpl implements FtpFileLoader {

    FTPClient apachFtpClient = new FTPClient();
    final Path destinationRootPath; //Например С:\
    String destinationUndoCatalog = "ftp";
    String rootdir = "/";
    private FtpPathFilter ftpPathFilter;

    public FtpFileLoaderImpl(FTPClientConfig config, FtpServerInfo ftpServerInfo, Path deviceRootDirectoryPath) throws IOException {

        destinationRootPath = deviceRootDirectoryPath;
        apachFtpClient.configure(config);
        //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
        //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));
        apachFtpClient.connect(ftpServerInfo.ftpServer, 21);

        int reply = apachFtpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            apachFtpClient.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        apachFtpClient.login(ftpServerInfo.user, ftpServerInfo.passw);

    }


    @Override
    public int loadFile(String dir) {
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
                        System.err.println(Thread.currentThread().getName() + " " + " Сохраняем в " + fileDestinationPath);
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
                        String mess = Thread.currentThread().getName() + " " + strSrcAbsoluteFolderName + " ... " + System.lineSeparator();
                        System.err.print(mess);
                        loadFile(strSrcAbsoluteFolderName);
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
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
}

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import static java.nio.file.Files.newOutputStream;

public class FtpFileLoaderImpl implements FtpFileLoader {

    FTPClient apachFtpClient = new FTPClient();
    final Path destinationRootPath;
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
                    //-----------
                    if (ftpPathFilter.isFileNameMatched(strSrcAbsoluteFileName) && ftpPathFilter.isCatalogNameMatched(parentDir)) {
                        fileDestinationPath = Paths.get(new URI("file:///" + destinationRootPath + "/" + destinationUndoCatalog + "/" + strSrcAbsoluteFileName));
                        OutputStream outputStream = newOutputStream(fileDestinationPath, StandardOpenOption.CREATE);
                        System.err.println(strSrcAbsoluteFileName + " : " + new Date() + " Start");
                        apachFtpClient.retrieveFile(strSrcAbsoluteFileName, outputStream);
                        System.err.println(strSrcAbsoluteFileName + " : " + new Date() + " Stop");
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
                    loadFile(strSrcAbsoluteFolderName);
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
        String mess = startLoadDate + "Старт загрузки " + System.lineSeparator();
        Date endLoadDate = new Date();
        loadFile(rootdir);
        mess += endLoadDate + "Загрузка с сервера " + " завершена" + System.lineSeparator();
        System.err.print(mess);
        try {
            VLogger.writeLog(mess, destinationRootPath + "/" + destinationUndoCatalog + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package vg.ftp.services;

import org.apache.commons.net.ftp.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import vg.ftp.model.FtpServerInfo;

import java.io.IOException;
import java.io.OutputStream;

@Service
@Scope(value = "prototype")
public class SpecialFtpClientImpl implements ApplicationContextAware, SpecialFtpClient {

    private FTPClient apachFtpClient;

    private FtpServerInfo ftpServerInfo;

    private FtpPathFilter ftpPathFilter;

    private FTPClientConfig config;

    private ApplicationContext ctx;

    public SpecialFtpClientImpl() {
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

    public void attempRepeatFtpConnection() {
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

    @Override
    public FTPFile[] listFiles(String dir) throws IOException {
        return apachFtpClient.listFiles(dir);
    }

    @Override
    public FTPFile[] listFiles(String dir, FTPFileFilter ftpFileFilter) throws IOException {
        return apachFtpClient.listFiles(dir, ftpFileFilter);
    }

    @Override
    public FTPFile[] listDirectories(String dir) throws IOException {
        return apachFtpClient.listDirectories(dir);
    }

    @Override
    public void retrieveFile(String strSrcAbsoluteFileName, OutputStream outputStream) throws IOException {
        apachFtpClient.retrieveFile(strSrcAbsoluteFileName, outputStream);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

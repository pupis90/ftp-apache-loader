package vg.ftp.services;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.IOException;
import java.io.OutputStream;

public interface SpecialFtpClient {

    public void establishFtpConnection();

    public void attempRepeatFtpConnection();

    FTPFile[] listFiles(String dir) throws IOException;

    FTPFile[] listFiles(String dir, FTPFileFilter ftpFileFilter) throws IOException;

    FTPFile[] listDirectories(String dir) throws IOException;

    void retrieveFile(String strSrcAbsoluteFileName, OutputStream outputStream) throws IOException;
}

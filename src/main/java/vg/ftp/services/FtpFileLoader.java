package vg.ftp.services;

public interface FtpFileLoader extends Runnable {

    public int loadFile(String dir);

    public int loadDir(String dir);

    public int setPathFilter(FtpPathFilter ftpPathFilter);

    public int setTxtFileInMemoryContentFilter(InMemoryContentFilter inMemoryContentFilter);


}

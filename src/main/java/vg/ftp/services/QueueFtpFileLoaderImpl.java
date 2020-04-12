package vg.ftp.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import vg.ftp.config.ApplicationConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

public class QueueFtpFileLoaderImpl implements FtpFileLoader, ApplicationContextAware {
    private static final Logger logger = LogManager.getLogger(QueueFtpFileLoaderImpl.class);

    private BlockingQueue<String> srcFullFileNamesForLoadFromFtp;
    private BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave;
    private ApplicationContext ctx;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private FtpPathFilter ftpPathFilter;

    private String destinationSubCatalog;

    @Autowired
    SpecialFtpClientImpl specialFtpClient;

    public QueueFtpFileLoaderImpl() {
    }

    public void setSrcFullFileNamesForLoadFromFtp(BlockingQueue<String> srcFullFileNamesForLoadFromFtp) {
        this.srcFullFileNamesForLoadFromFtp = srcFullFileNamesForLoadFromFtp;
    }

    public void setByteArrayOutputStreamsForLocalSave(BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave) {
        this.byteArrayOutputStreamsForLocalSave = byteArrayOutputStreamsForLocalSave;
    }

    @Override
    public int loadFile(String dir) {
        return -1;
    }

    private String srcAbsoluteFileName;

    public void setNeedInterrupt(boolean needInterrupt) {
        this.needInterrupt = needInterrupt;
    }

    private boolean needInterrupt = false;

    private long failureLoadCount = 0L;

    @Override
    public void loadFiles() {

        String mess;
        while (!needInterrupt) {
            try {

                long count = 0L;
                srcAbsoluteFileName = srcFullFileNamesForLoadFromFtp.take();

                logger.info(Thread.currentThread().getName() + " " + srcAbsoluteFileName + " : " + "Start load to memory ");// + System.lineSeparator()

                OutputStream outputStream = new SpecialByteArrayOutputStream(25000);

                specialFtpClient.retrieveFile(srcAbsoluteFileName, outputStream);

                ((SpecialByteArrayOutputStream) outputStream).setSrcAbsoluteFileName(srcAbsoluteFileName);

                byteArrayOutputStreamsForLocalSave.put(outputStream);

                logger.info(Thread.currentThread().getName() + " " + srcAbsoluteFileName + " : " + " Loading in memory Wait saving...");//+ System.lineSeparator()
                count++;


            } catch (IOException | InterruptedException e) {
                failureLoadCount++;
                logger.error("ATTENTON! LOAD FAILED " + srcAbsoluteFileName);
                logger.error("ATTENTON!  " + srcAbsoluteFileName + " need load at postprocess ");
                logger.error("ATTENTON!  " + " number of failures while loading: " + failureLoadCount);
                specialFtpClient.attempRepeatFtpConnection();

            }

            try {
                sleep(50);
            } catch (InterruptedException e) {
                logger.error(e);
            }


        }
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
        String mess = startLoadDate + " " + Thread.currentThread().getName() + " Старт  " + System.lineSeparator();
        System.err.print(mess);
        specialFtpClient.establishFtpConnection();
        destinationSubCatalog = applicationConfiguration.getDestinationSubCatalog();
        loadFiles();
        Date endLoadDate = new Date();
        mess += endLoadDate + Thread.currentThread().getName() + " Загрузка с ftp сервера в буфер завершена" + System.lineSeparator();
        logger.info(mess);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

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

    String rootdir = "/";
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


    public void setRootdir(String rootdir) {
        this.rootdir = rootdir;
    }

    @Override
    public int loadFile(String dir) {
        return -1;
    }

    @Override
    public int loadFiles() {

        String mess;
        while (true) {
            try {

                long count = 0L;
                String srcAbsoluteFileName = srcFullFileNamesForLoadFromFtp.take();


                //  System.err.print(mess);
                //-----------/fcs_regions/addinfo_Adygeja_Resp_2015120100_2016010100_001.xml.zip
                //  if(strSrcAbsoluteFileName.equals("/fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip"))
                //           System.err.print(strSrcAbsoluteFileName);
                //
                logger.info(Thread.currentThread().getName() + " " + srcAbsoluteFileName + " : " + new Date() + "Start load to memory ");// + System.lineSeparator()

                OutputStream outputStream = new SpecialByteArrayOutputStream(25000);

                specialFtpClient.retrieveFile(srcAbsoluteFileName, outputStream);
                ((SpecialByteArrayOutputStream) outputStream).setSrcAbsoluteFileName(srcAbsoluteFileName);

                byteArrayOutputStreamsForLocalSave.put(outputStream);

                logger.info(Thread.currentThread().getName() + " " + srcAbsoluteFileName + " : " + new Date() + " Loading in memory Wait saving...");//+ System.lineSeparator()
                count++;


            } catch (IOException | InterruptedException e) {
                logger.error(e);
                logger.error(" strSrcAbsoluteFileName  load failed ");//+ System.lineSeparator()
                specialFtpClient.attempRepeatFtpConnection();

            }

            try {
                sleep(100);
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

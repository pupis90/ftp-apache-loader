package vg.ftp.services;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import vg.ftp.config.ApplicationConfiguration;
import vg.ftp.model.Device;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

@Service
public class FtpLoadBalancer implements ApplicationContextAware {

    private static final Logger logger = LogManager.getLogger(FtpLoadBalancer.class);

    ApplicationContext ctx;

    @Autowired
    ApplicationConfiguration applicationConfiguration;

    private BlockingQueue<String> srcFullFileNamesForLoadFromFtp;

    private BlockingQueue<OutputStream> bufferedOutputStreamsForLocalSave;

    private List<Device> devices;

    /**
     * Список загружающих c ftp сервера потоков.
     * Каждый поток по имени файла из блокирующей очереди имен создает выходной буфер - стрим,
     * загружает туда файл с ftp и после загрузки помещает в блокирующую очередь буферизированных стримом на запись.
     * Если произошел сбой - имя должно вернуться обратно в очередь.
     * Группа потоков - писателей выбирает из очереди буферизированный стримм и сохраняет в файл
     */
    List<Thread> fileFtpLoaderThreadList;

    /**
     * Группа потоков - писателей: выбирает из очереди буферизированный стримм и сохраняет в файл.
     * Если произошел сбой - буферизированный стримм должен вернуться обратно в очередь.
     */
    List<Thread> localFileWriterThreadList;

    private String ftpServer;

    private DriverManager driverManager;

    @Autowired
    SpecialFtpClientImpl specialFtpClient;

    @Value("${destination.subcatalog}")
    String destinationSubCatalog;


//@ToDo Это все в свойства и в Фильтры распихать
//        String srcCatalog = "fcs_regions";
//        String fileFilter = "contract";
//        // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
//        String catalogFilter = "fcs_regions";
//        String dateFilter = "2019";
    // /fcs_regions/Adygeja_Resp/contracts/contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip
    // /fcs_regions/Adygeja_Resp/contracts/currMonth/
    // /fcs_regions/Burjatija_Resp/notifications/notification_Burjatija_Resp_2019090100_2019100100_001.xml.zip
    // /fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip

    public FtpLoadBalancer() {
    }

    /******************************************************/
    public void filterSrcWorkDirSubFoldersAndFilesPoolReadAndDiskArrayBalanceSave(List<String> srcWorkDirSubFoldersAndFiles) {

        threadListsAndReadWriteQueueInit();
        specialFtpClient.establishFtpConnection();
        int deviceCounter = 0;
        int shift = 5;
        for (Thread thread : fileFtpLoaderThreadList) {
            thread.start();
        }
        for (Thread thread : localFileWriterThreadList) {
            thread.start();
        }
        putFileNamesToBlockingQueue(srcWorkDirSubFoldersAndFiles);
        boolean isfinish = false;


        /**ToDo Не забыть дождаться очищения очереди - обработки всех файлов !!! или потоки чтения записи сделать demon - они
         * доработают до конца и остановятся
         */
        while (!isfinish) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void putFileNamesToBlockingQueue(List<String> srcWorkDirSubFoldersAndFiles) {
        String regFile = "/fcs_regions/.*/contract_[a-zA-Z_0-9]*.xml.zip";
        String regCatalog = "/fcs_regions/[a-zA-Z_0-9]*/contracts/";
        //Заполнение очереди имен на обработку
        for (String dir : srcWorkDirSubFoldersAndFiles) {
            regCatalog = dir + ".*/contracts";
            regFile = ".*/contract_[a-zA-Z_0-9]*.xml.zip";
            List<String> matchCatalogNames = new ArrayList();
            List<String> matchFileNames = new ArrayList();
            matchCatalogNames.add(regCatalog);
            FtpPathFilterImpl ftpPathFilter = (FtpPathFilterImpl) ctx.getBean(FtpPathFilterImpl.class);
            ftpPathFilter.setPatternsCatalogNames(matchCatalogNames);
            matchFileNames = new ArrayList();
            matchFileNames.add(regFile);
            ftpPathFilter.setPatternFileNames(matchFileNames);
            findAndAddQueueNameLoadFileInSubCatalogRecurcive(dir, ftpPathFilter);
        }
    }

    /***********************************************************************************************************
     *
     */

    private void threadListsAndReadWriteQueueInit() {
        srcFullFileNamesForLoadFromFtp = new ArrayBlockingQueue<String>(applicationConfiguration.getSourceReadPoolSize());
        driverManager = (DriverManager) ctx.getBean("driver-manager");
        devices = driverManager.getDevices();
        bufferedOutputStreamsForLocalSave = new ArrayBlockingQueue<>(devices.size());
        fileFtpLoaderThreadList = new ArrayList<>(applicationConfiguration.getSourceReadPoolSize());
        for (int i = 0; i < applicationConfiguration.getSourceReadPoolSize(); i++) {
            QueueFtpFileLoaderImpl ftpFileLoader = (QueueFtpFileLoaderImpl) ctx.getBean("ftp-file-loader-id");
            ftpFileLoader.setSrcFullFileNamesForLoadFromFtp(srcFullFileNamesForLoadFromFtp);
            ftpFileLoader.setByteArrayOutputStreamsForLocalSave(bufferedOutputStreamsForLocalSave);
            Thread thread = new Thread(ftpFileLoader);
            thread.setName("FileFtpToBlockingQueueLoader № " + i);
            fileFtpLoaderThreadList.add(thread);
        }

        localFileWriterThreadList = new ArrayList<>(devices.size());
        for (int k = 0; k < devices.size(); k++) {
            QueueLocalFileSaverImpl queueLocalFileSaverImpl = ctx.getBean(QueueLocalFileSaverImpl.class);
            queueLocalFileSaverImpl.setByteArrayOutputStreamsForLocalSave(bufferedOutputStreamsForLocalSave);
            queueLocalFileSaverImpl.setDestinationRootPath(devices.get(k).getRootDirectory());
            Thread thread = new Thread(queueLocalFileSaverImpl);
            thread.setName("BlockingQueueLocalFileSaver  № " + k);
            localFileWriterThreadList.add(thread);

        }
    }

    /****************************
     *
     * @param dir
     * @param _ftpPathFilter
     */
    private void findAndAddQueueNameLoadFileInSubCatalogRecurcive(String dir, FtpPathFilterImpl _ftpPathFilter) {
        FtpPathFilterImpl ftpPathFilter = _ftpPathFilter;
        String currDir = dir;
        try {
            long count_contracts = 0L;
            String srcFileName = " ";
            String srcFolderName = " ";
            String mess;
            if (!dir.equals("/fcs_regions")) {
                FTPFile[] files = specialFtpClient.listFiles(dir);
                /**-----------------------------*/
                //Идем по файлам каталога
                for (FTPFile fileOrDir : files) {
                    if (fileOrDir.isFile()) {
                        srcFileName = fileOrDir.getName();
                        String parentDir = dir;
                        if (dir.equals("/")) parentDir = "";
                        String strSrcAbsoluteFileName = parentDir + "/" + srcFileName;
                        //-----------/fcs_regions/addinfo_Adygeja_Resp_2015120100_2016010100_001.xml.zip
                /*    if(strSrcAbsoluteFileName.equals("/fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip"))
                        System.err.print(strSrcAbsoluteFileName);
                  */
                        if (ftpPathFilter.isFileNameMatched(strSrcAbsoluteFileName) && ftpPathFilter.isCatalogNameMatched(parentDir)) {
                            mess = Thread.currentThread().getName() + " Ставлю в очередь на загрузку" + strSrcAbsoluteFileName + System.lineSeparator();
                            logger.info(mess);
                            try {
                                srcFullFileNamesForLoadFromFtp.put(strSrcAbsoluteFileName);
                            } catch (InterruptedException e) {
                                logger.error(e);
                            }
                        }

                    }
                }
            }
            FTPFile[] directories = specialFtpClient.listDirectories(dir);
            for (FTPFile fileDir : directories) {
                srcFolderName = fileDir.getName();
                String parentDir = dir;
                if (dir.equals("/")) parentDir = "";
                String strSrcAbsoluteFolderName = parentDir + "/" + srcFolderName;
                //----------
                if (strSrcAbsoluteFolderName.startsWith("/fcs_regions")/*||strSrcAbsoluteFolderName.startsWith("/")*/) {
                    mess = Thread.currentThread().getName() + "Inspect folder: " + strSrcAbsoluteFolderName + System.lineSeparator();
                    logger.info(mess);
                    findAndAddQueueNameLoadFileInSubCatalogRecurcive(strSrcAbsoluteFolderName, ftpPathFilter);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(" current Dir " + currDir + System.lineSeparator());
            specialFtpClient.attempRepeatFtpConnection();

        }

    }

    /**
     *
     */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

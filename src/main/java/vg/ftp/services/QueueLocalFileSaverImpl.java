package vg.ftp.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import vg.ftp.config.ApplicationConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;
import static java.nio.file.Files.newOutputStream;

@Service
@Scope("prototype")
public class QueueLocalFileSaverImpl implements Runnable {

    private static final Logger logger = LogManager.getLogger(QueueLocalFileSaverImpl.class);

    @Autowired
    SpecialFtpClientImpl specialFtpClient;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave;

    String destinationSubCatalog;

    private Path destinationRootPath;

    String logmessage;


    private long diskC_limit;

    public void setNeedInterrupt(boolean needInterrupt) {
        this.needInterrupt = needInterrupt;
    }

    private boolean needInterrupt = false;

    private long failureSavingCount = 0L;

    private long saveCount = 0L;

    private String srcAbsoluteFileName;

    public QueueLocalFileSaverImpl() {
    }

    private void saveFiles() {

        Path fileDestinationPath = null;
        diskC_limit = applicationConfiguration.getDiskC_limit();

        while (!needInterrupt) {
            try {
                OutputStream byteArrayOutputStream = byteArrayOutputStreamsForLocalSave.take();
                srcAbsoluteFileName = ((SpecialByteArrayOutputStream) byteArrayOutputStream).getSrcAbsoluteFileName();
                fileDestinationPath = Paths.get(new URI("file:///" + destinationRootPath.toString().replace("\\", "") + "/" + destinationSubCatalog + srcAbsoluteFileName));
                logmessage = Thread.currentThread().getName() + " Сохранение файла " + srcAbsoluteFileName + "  в " + fileDestinationPath + "... "; //+ System.lineSeparator()
                logger.info(logmessage);
                Path parentDirPath = fileDestinationPath.getParent();
                if (!Files.exists(parentDirPath)) Files.createDirectories(parentDirPath);
                OutputStream outputStream = newOutputStream(fileDestinationPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                ((SpecialByteArrayOutputStream) byteArrayOutputStream).writeTo(outputStream);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                logmessage = Thread.currentThread().getName() + " Сохранение  " + srcAbsoluteFileName + " завершено "; //+ System.lineSeparator()
                logger.info(logmessage);

            } catch (URISyntaxException | IOException | InterruptedException e) {
                /**@ToDo Eckb Access Denited то поток завершить: needInterrupt = true*/
                logger.error("ATTENTON! CRASH SAVE " + srcAbsoluteFileName + "  !!! ");
                logger.error("ATTENTON!  " + srcAbsoluteFileName + " need load and save at postprocess !!! ");
                logger.error("ATTENTON!  " + " number of failures while saving " + failureSavingCount);
                failureSavingCount++;
                specialFtpClient.attempRepeatFtpConnection();
            }

            saveCount++;

            if (destinationRootPath.toString().contains("C:") && (saveCount > diskC_limit)) needInterrupt = true;
            logmessage = Thread.currentThread().getName() + " Остановден - превышен заданный лимит файлов  ";


            try {
                sleep(50);
            } catch (InterruptedException e) {
                logger.error(e);
                failureSavingCount++;
            }
        }

    }


    @Override
    public void run() {
        destinationSubCatalog = applicationConfiguration.getDestinationSubCatalog();
        Date startLoadDate = new Date();
        String mess = startLoadDate + " " + Thread.currentThread().getName() + " Старт  "; //+ System.lineSeparator()
        logger.info(mess);
        saveFiles();
        Date endLoadDate = new Date();
        mess += endLoadDate + Thread.currentThread().getName() + " Загрузка с сервера " + " завершена"; //+ System.lineSeparator()
        logger.info(mess);

    }

    public void setDestinationRootPath(Path _destinationRootPath) {
        this.destinationRootPath = _destinationRootPath;
    }

    public void setByteArrayOutputStreamsForLocalSave(BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave) {
        this.byteArrayOutputStreamsForLocalSave = byteArrayOutputStreamsForLocalSave;
    }
}

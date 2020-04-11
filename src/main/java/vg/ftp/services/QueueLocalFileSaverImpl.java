package vg.ftp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import vg.ftp.config.ApplicationConfiguration;

import java.io.FileOutputStream;
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

    @Autowired
    SpecialFtpClientImpl specialFtpClient;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave;

    String destinationSubCatalog;
    private Path destinationRootPath;

    public QueueLocalFileSaverImpl() {
    }

    private void saveFiles() {
        long count = 0L;
        Path fileDestinationPath = null;
        String mess;

        while (true) {
            try {
                OutputStream byteArrayOutputStream = byteArrayOutputStreamsForLocalSave.take();
                String strSrcAbsoluteFileName = ((SpecialByteArrayOutputStream) byteArrayOutputStream).getSrcAbsoluteFileName();
                //-----------/fcs_regions/addinfo_Adygeja_Resp_2015120100_2016010100_001.xml.zip
                //  if(strSrcAbsoluteFileName.equals("/fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip"))

                fileDestinationPath = Paths.get(new URI("file:///" + destinationRootPath.toString().replace("\\", "") + "/" + destinationSubCatalog + strSrcAbsoluteFileName));
                mess = Thread.currentThread().getName() + new Date() + " Сохранение файла " + strSrcAbsoluteFileName + "  в " + fileDestinationPath + " Старт " + System.lineSeparator();
                System.err.print(mess);
                Path parentDirPath = fileDestinationPath.getParent();
                if (!Files.exists(parentDirPath)) Files.createDirectories(parentDirPath);
                OutputStream outputStream = newOutputStream(fileDestinationPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                ((SpecialByteArrayOutputStream) byteArrayOutputStream).writeTo(outputStream);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                mess = Thread.currentThread().getName() + new Date() + " Сохранение  " + strSrcAbsoluteFileName + " Финиш " + System.lineSeparator();
                System.err.print(mess);

            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
                System.err.print(" current Dir " + System.lineSeparator());
                specialFtpClient.attempRepeatFtpConnection();
            }

            count++;

            try {
                sleep(100);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }


    @Override
    public void run() {
        destinationSubCatalog = applicationConfiguration.getDestinationSubCatalog();
        Date startLoadDate = new Date();
        String mess = startLoadDate + " " + Thread.currentThread().getName() + " Старт  " + System.lineSeparator();
        System.err.print(mess);
        saveFiles();
        Date endLoadDate = new Date();
        mess += endLoadDate + Thread.currentThread().getName() + " Загрузка с сервера " + " завершена" + System.lineSeparator();
        System.err.print(mess);

    }

    public void setDestinationRootPath(Path _destinationRootPath) {
        this.destinationRootPath = _destinationRootPath;
    }

    public void setByteArrayOutputStreamsForLocalSave(BlockingQueue<OutputStream> byteArrayOutputStreamsForLocalSave) {
        this.byteArrayOutputStreamsForLocalSave = byteArrayOutputStreamsForLocalSave;
    }
}

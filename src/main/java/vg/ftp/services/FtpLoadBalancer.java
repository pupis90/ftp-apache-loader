package vg.ftp.services;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import vg.ftp.model.Device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FtpLoadBalancer implements ApplicationContextAware {

    ApplicationContext ctx;
    private String ftpServer;
    private DriverManager driverManager;
    @Value("${destination.subcatalog}")
    String destinationSubCatalog;
    //@ToDo Это все в свойства и в Фильтры распихать
//        String srcCatalog = "fcs_regions";
//
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

    public void load() throws IOException {
        driverManager = (DriverManager) ctx.getBean("driver-manager");
        List<String> firstFolderNameLetters = new ArrayList(100);
        for (char c = 'A'; c <= 'Z'; c++) {
            firstFolderNameLetters.add(Character.toString(c));
        }
        String startFolderLetterDiapazon = "A";
        String endFolderLetter = "Z";
        String regFile = "/fcs_regions/.*/contract_[a-zA-Z_0-9]*.xml.zip";
        ;
        String regCatalog = "/fcs_regions/[a-zA-Z_0-9]*/contracts/";
        Thread loader;
        FtpFileLoaderImpl ftpFileLoader;
        List<Device> devices = driverManager.getDevices();
        int deviceCounter = 0;
        int shift = 5;

        for (Device device : devices) {
            ftpFileLoader = (FtpFileLoaderImpl) ctx.getBean("ftp-file-loader-id");
            ftpFileLoader.setDestinationSubCatalog(destinationSubCatalog);
            ftpFileLoader.setDestinationRootPath(device.getRootDirectory());
            ftpFileLoader.establishFtpConnection();
            int startFolderLetterIdx = deviceCounter * firstFolderNameLetters.size() / devices.size();
            int endFolderLetterIdx = (deviceCounter + 1) * firstFolderNameLetters.size() / devices.size() - 1;
            if (device.getVolumeLabel().equals("C:\\")) endFolderLetterIdx -= shift;
            else startFolderLetterIdx -= shift + 1;

            startFolderLetterDiapazon = "[" + firstFolderNameLetters.get(startFolderLetterIdx) + "-" + firstFolderNameLetters.get(endFolderLetterIdx) + "]";
            regCatalog = "/fcs_regions/" + startFolderLetterDiapazon + "[a-zA-Z_0-9]*/contracts/";
            regFile = "/fcs_regions/.*/contract_[a-zA-Z_0-9]*.xml.zip";
            List<String> matchCatalogNames = new ArrayList();
            List<String> matchFileNames = new ArrayList();
            matchCatalogNames.add(regCatalog);
            FtpPathFilterImpl ftpPathFilter = (FtpPathFilterImpl) ctx.getBean(FtpPathFilterImpl.class);
            ftpPathFilter.setPatternsCatalogNames(matchCatalogNames);
            matchFileNames = new ArrayList();
            matchFileNames.add(regFile);
            ftpPathFilter.setPatternFileNames(matchFileNames);
            ftpFileLoader.setPathFilter(ftpPathFilter);
            loader = new Thread(ftpFileLoader);
            loader.setName(" Поток для устройства " + device.getVolumeLabel());
            loader.start();
            deviceCounter++;

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

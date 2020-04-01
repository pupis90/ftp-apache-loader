package vg.ftp.services;

import org.apache.commons.net.ftp.FTPClientConfig;
import vg.ftp.data.FtpServerInfo;
import vg.ftp.model.Device;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FtpLoadBalancer {

    private String ftpServer;
    DriverManager driverManager;
    FTPClientConfig config;
    FtpServerInfo ftpServerInfo;
    FtpPathFilterImpl ftpPathFilter;

    public FtpLoadBalancer(FTPClientConfig _config, FtpServerInfo _ftpServerInfo) {
        config = _config;
        ftpServerInfo = _ftpServerInfo;
        driverManager = new DriverManager();

    }

    public void load() throws IOException {
        Thread loader;
        FtpFileLoaderImpl ftpFileLoader;
        List<Device> devices = driverManager.getDevices();
        int deviceCounter = 0;
        for (Device device : devices) {

            ftpFileLoader = new FtpFileLoaderImpl(config, ftpServerInfo, device.getRootDirectory());
            ftpPathFilter = new FtpPathFilterImpl();
            // "/fcs_regions/Adygeja_Resp/contracts/contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip"
            // /fcs_regions/Adygeja_Resp/contracts/currMonth/
            //  /fcs_regions/Burjatija_Resp/notifications/notification_Burjatija_Resp_2019090100_2019100100_001.xml.zip
            //  /fcs_regions/Amurskaja_obl/contracts/contract_Amurskaja_obl_2017100100_2017110100_001.xml.zip
            String regCatalog = "/fcs_regions/A[a-zA-Z_0-9]*/contracts/";
            String regFile = "/fcs_regions/.*/contract_[a-zA-Z_0-9]*.xml.zip";
            if (deviceCounter == 0) {
                regCatalog = "/fcs_regions/A[a-zA-Z_0-9]*/contracts";
                //   regCatalog = "/fcs_regions/A.*/notifications/";
            }
            if (deviceCounter == 1) {
                regCatalog = "/fcs_regions/M[a-zA-Z_0-9]*/contracts";
            }
            List<String> matchCatalogNames = new ArrayList();
            List<String> matchFileNames = new ArrayList();
            matchCatalogNames.add(regCatalog);
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


}

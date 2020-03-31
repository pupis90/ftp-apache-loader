package vg.ftp;

import org.apache.commons.net.ftp.FTPClientConfig;
import vg.ftp.data.FtpServerInfo;
import vg.ftp.services.FtpInformer;
import vg.ftp.services.FtpLoadBalancer;


public class MainFtpLoader {


    public static void main(String[] args) {

        //@ToDo Это все в свойства и в Фильтры распихать
        String srcCatalog = "fcs_regions";
        String destinationCatalog = "C:/" + srcCatalog;
        String fileFilter = "contract"; // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
        String catalogFilter = "fcs_regions";
        String dateFilter = "2019";
        boolean needInfo = false;
        boolean needLoad = true;
        //Содержание /fcs_regions/Adygeja_Resp/contracts/
        FTPClientConfig config = new FTPClientConfig();
        config.setLenientFutureDates(true); // change required options
        config.setServerLanguageCode("ru");
        String workdir = "/";
        FtpServerInfo ftpServerInfo = new FtpServerInfo();

        try {
            //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
            //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));


            if (needInfo) {
                FtpInformer ftpInformer = new FtpInformer(config, ftpServerInfo);
                ftpInformer.loadInfo();
            }

            if (needLoad) {
                FtpLoadBalancer ftpLoadBalancer = new FtpLoadBalancer(config, ftpServerInfo);
                ftpLoadBalancer.load();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}

package vg.ftp;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import vg.ftp.services.FtpInformer;
import vg.ftp.services.FtpLoadBalancer;

@Configuration
public class MainFtpLoader {


    public static void main(String[] args) {

        //@ToDo Это все в свойства и в Фильтры распихать
//        String srcCatalog = "fcs_regions";
//        String destinationCatalog = "C:/" + srcCatalog;
//        String fileFilter = "contract";
//        // ftp://ftp.zakupki.gov.ru/fcs_regions/Adygeja_Resp/contracts/"contract_Adygeja_Resp_2014010100_2014020100_001.xml.zip";
//        String catalogFilter = "fcs_regions";
//        String dateFilter = "2019";
        boolean needInfo = false;
        boolean needLoad = true;
        String workdir = "/";
        //Содержание /fcs_regions/Adygeja_Resp/contracts/

        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
        FTPClientConfig config = (FTPClientConfig) ctx.getBean("client-config");
        config.setLenientFutureDates(true); // change required options
        config.setServerLanguageCode("ru");
        //FtpServerInfo ftpServerInfo = (FtpServerInfo) ctx.getBean("server-info");

        try {
            //SocketAddress ftpAddress = new InetSocketAddress(ftpServer, 21);
            //apachFtpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.err)));

            if (needInfo) {
                FtpInformer ftpInformer = (FtpInformer) ctx.getBean("informer");
                ftpInformer.loadInfo();
            }

            if (needLoad) {

                FtpLoadBalancer ftpLoadBalancer = (FtpLoadBalancer) ctx.getBean("ftp-file-loader-balancer");
                ftpLoadBalancer.setContext(ctx);
                ftpLoadBalancer.load();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //   ((ClassPathXmlApplicationContext) ctx).close();

    }





}

package vg.ftp;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import vg.ftp.config.ApplicationFiltersConfiguration;
import vg.ftp.services.FtpInformer;
import vg.ftp.services.FtpLoadBalancer;

@Configuration
@ImportResource("vg-ftp-spring-context.xml")
@Import(ApplicationFiltersConfiguration.class)
public class MainFtpLoader {

    static ApplicationContext ctx;
    @Value("${info.enable}")
    static boolean needInfo;
    @Value("${load.enable}")
    static boolean needLoad;

    public static void main(String[] args) {
        String workdir = "/";
        //Содержание /fcs_regions/Adygeja_Resp/contracts/

        ctx = new ClassPathXmlApplicationContext("/vg-ftp-spring-context.xml");
        FTPClientConfig config = (FTPClientConfig) ctx.getBean("client-config");
        config.setLenientFutureDates(true); // change required options
        config.setServerLanguageCode("ru");
        //FtpServerInfo ftpServerInfo = (FtpServerInfo) ctx.getBean("server-info");

        try {
            if (needInfo) {
                FtpInformer ftpInformer = (FtpInformer) ctx.getBean("informer");
                ftpInformer.loadInfo();
            }

            if (needLoad) {

                FtpLoadBalancer ftpLoadBalancer = (FtpLoadBalancer) ctx.getBean("ftp-file-loader-balancer");
                ftpLoadBalancer.load();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //   ((ClassPathXmlApplicationContext) ctx).close();

    }



}

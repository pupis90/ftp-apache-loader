package vg.ftp;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import vg.ftp.config.ApplicationConfiguration;
import vg.ftp.services.FtpMetaInfoLoader;
import vg.ftp.services.FtpLoadBalancer;

import java.util.List;

@Configuration
@ImportResource("vg-ftp-spring-context.xml")
@Import(ApplicationConfiguration.class)
public class MainFtpLoader {

    public static void main(String[] args) {

        //Содержание /fcs_regions/Adygeja_Resp/contracts/
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/vg-ftp-spring-context.xml");
        FTPClientConfig config = (FTPClientConfig) ctx.getBean("client-config");
        config.setLenientFutureDates(true); // change required options
        config.setServerLanguageCode("ru");
        Environment environment = ctx.getEnvironment();

        try {
            FtpMetaInfoLoader ftpMetaInfoLoader = (FtpMetaInfoLoader) ctx.getBean("informer");
            ftpMetaInfoLoader.requestMetaInfoIfNeedFull();
            List<String> srcWorkDirSubFoldersAndFiles = ftpMetaInfoLoader.getSrcWorkDirSubFoldersAndFiles();
            FtpLoadBalancer ftpLoadBalancer = (FtpLoadBalancer) ctx.getBean("ftp-file-loader-balancer");
            ftpLoadBalancer.filterSrcWorkDirSubFoldersAndFilesPoolReadAndDiskArrayBalanceSave(srcWorkDirSubFoldersAndFiles);

        } catch (
                Exception e) {
            e.printStackTrace();
        }

        //   ((ClassPathXmlApplicationContext) ctx).close();

    }
}


package vg.ftp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import vg.ftp.services.FtpPathFilter;
import vg.ftp.services.FtpPathFilterImpl;

@Configuration
@PropertySources({
        @PropertySource("classpath:database.properties"),
        @PropertySource("classpath:add.properties")
})
public class ApplicationConfiguration {


    public boolean isNeedFullMetaInfo() {
        return isNeedFullMetaInfo;
    }

    @Value("${source.fullinfo.enable}")
    boolean isNeedFullMetaInfo;


    @Value("${source.load.enable}")
    boolean needLoad;

    public String getSourceWorkdir() {
        return sourceWorkdir;
    }

    @Value("${source.workdir}")
    String sourceWorkdir;


    //   destination.subcatalog=ftp


    public int getSourceCatalogTreeDeep() {
        return sourceCatalogTreeDeep;
    }

    @Value("${source.catalog.tree.deep}")
    int sourceCatalogTreeDeep;

    public int getSourceReadPoolSize() {
        return sourceReadPoolSize;
    }

    @Value("${source.read.pool.size}")
    int sourceReadPoolSize;

    public String getDestinationSubCatalog() {
        return destinationSubCatalog;
    }

    @Value("${destination.subcatalog}")
    String destinationSubCatalog;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FtpPathFilter getFtpPathFilter() {
        return new FtpPathFilterImpl();
    }


}

package vg.ftp.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import vg.ftp.services.FtpPathFilter;
import vg.ftp.services.FtpPathFilterImpl;

@Configuration
@PropertySources({
        @PropertySource("classpath:database.properties"),
        @PropertySource("classpath:add.properties")
})
public class ApplicationFiltersConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FtpPathFilter getFtpPathFilter() {
        return new FtpPathFilterImpl();
    }

    ;

}

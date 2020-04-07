package vg.ftp.services;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class FtpPathFilterImpl implements FtpPathFilter, ApplicationContextAware {

    Pattern catalogPattern;
    Pattern filePattern;
    private ApplicationContext ctx;

    @Override
    public List<String> getPatternCatalogNames() {
        return null;
    }

    @Override
    public void setPatternsCatalogNames(List<String> catalogPatternes) {
        catalogPattern = Pattern.compile(catalogPatternes.get(0));
    }

    @Override
    public List<String> getPatternFileNames() {
        return null;
    }

    @Override
    public void setPatternFileNames(List<String> filePatternes) {
        filePattern = Pattern.compile(filePatternes.get(0));
    }

    @Override
    public boolean isCatalogNameMatched(String catalogName) {
        return catalogPattern.matcher(catalogName).find();
    }

    @Override
    public boolean isFileNameMatched(String fileName) {
        return catalogPattern.matcher(fileName).find();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}

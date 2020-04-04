package vg.ftp.services;

import java.util.List;
import java.util.regex.Pattern;

public class FtpPathFilterImpl implements FtpPathFilter {

    Pattern catalogPattern;
    Pattern filePattern;

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
}

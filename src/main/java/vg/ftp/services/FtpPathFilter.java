package vg.ftp.services;

import java.util.List;

public interface FtpPathFilter {

    public List<String> getPatternCatalogNames();

    public void setPatternsCatalogNames(List<String> catalogPatternes);

    public List<String> getPatternFileNames();

    public void setPatternFileNames(List<String> filePatternes);

    public boolean isCatalogNameMatched(String cat);

    public boolean isFileNameMatched(String file);


}

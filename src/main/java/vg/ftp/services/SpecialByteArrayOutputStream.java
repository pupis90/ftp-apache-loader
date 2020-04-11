package vg.ftp.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;


@Service
@Scope("prototype")
public class SpecialByteArrayOutputStream extends ByteArrayOutputStream {

    public String getSrcAbsoluteFileName() {
        return srcAbsoluteFileName;
    }

    public void setSrcAbsoluteFileName(String srcAbsoluteFileName) {
        this.srcAbsoluteFileName = srcAbsoluteFileName;
    }

    private String srcAbsoluteFileName;

    public SpecialByteArrayOutputStream(int size) {
        super(size);

    }


}

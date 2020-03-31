package vg.ftp.model;


import java.nio.file.Path;

public class DeviceImpl implements Device {

    public Path rootDirectory;

    public DeviceImpl(Path _rootDirectory) {
        rootDirectory = _rootDirectory;
    }

    @Override
    public String getHost() {
        return null;
    }

    public String getIP4() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDriveFormat() {
        return null;
    }

    @Override
    public String getDriveType() {
        return null;
    }

    @Override
    public long getAvailableFreeSpace() {
        return 0;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public Path getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public long getTotalFreeSpace() {
        return 0;
    }

    @Override
    public long getTotalSize() {
        return 0;
    }

    @Override
    public String getVolumeLabel() {
        return null;
    }
}

package vg.ftp.services;

import vg.ftp.model.Device;
import vg.ftp.model.DeviceImpl;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DriverManager {

    private FileSystem fileSystem;

    public List<Device> getDevices() {
        return devices;
    }

    private List<Device> devices;
    private List<FileStore> stores;

    public DriverManager() {
    }

    private void init() {
        devices = new ArrayList<>();
        stores = new ArrayList<>();
        fileSystem = FileSystems.getDefault();
        Iterable<Path> rootDirectoriesIt = fileSystem.getRootDirectories();
        Iterable<FileStore> storesIt = fileSystem.getFileStores();
        Consumer<Path> diskConsumer = (path) -> devices.add(new DeviceImpl(path));
        rootDirectoriesIt.forEach(diskConsumer);
        Consumer<FileStore> storeConsumer = (fileStore) -> stores.add(fileStore);
        storesIt.forEach(storeConsumer);

    }


}

package vg.ftp.model;

/**
 * Аналог DriveInfo С#
 */

import java.nio.file.Path;

/**
 * Получить массив экземпляров класса DriveInfo для данного ПК можно с помощью выражения DriveInfo.GetDrives().
 * Метод GetDrives()
 * возвращает информацию обо всех логических дисках на компьютере.
 */
public interface Device {

    public String getHost();


    public String getIP4();

    /**
     * имя диска
     */
    public String getName();

    /**
     * Файловая система
     */
    public String getDriveFormat();

    /**
     * Тип диска
     */
    public String getDriveType();

    /**
     * объем доступного свободного места (в байтах)
     */
    public long getAvailableFreeSpace();

    /**
     * готов ли диск
     */
    public boolean isReady();

    /**
     * – Корневой каталог диска
     */
    public Path getRootDirectory();

    /**
     * общий объем свободного места, доступного на диске (в байтах)
     */
    public long getTotalFreeSpace();

    /**
     * – размер диска (в байтах)
     */
    public long getTotalSize();

    /**
     * – метка тома диска
     */
    public String getVolumeLabel();


}

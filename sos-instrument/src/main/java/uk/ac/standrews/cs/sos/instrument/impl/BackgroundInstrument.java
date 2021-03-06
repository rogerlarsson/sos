/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module instrument.
 *
 * instrument is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * instrument is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with instrument. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.sos.instrument.impl;

import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.MemoryStats;
import com.jezhumble.javasysmon.OsProcess;
import com.jezhumble.javasysmon.ProcessInfo;
import com.sun.management.OperatingSystemMXBean;
import uk.ac.standrews.cs.sos.instrument.Metrics;
import uk.ac.standrews.cs.sos.instrument.StatsTYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class BackgroundInstrument implements Metrics {

    public static final String OS_FILE = "_os.tsv";

    private static final Object LOCK_MEASUREMENTS_QUEUE = new Object();
    private Queue<Metrics> measurementsQueue;

    private ScheduledExecutorService scheduler;
    private Future future;

    private String filename;

    // e.g. exp, non-exp, etc...
    public static METRIC_TYPE type = METRIC_TYPE.non_experiment;

    public enum METRIC_TYPE {
        experiment, non_experiment
    }

    // METRICS
    private long residentBytes;
    private long totalBytes;
    private long sysUptime;
    private long userUptime;

    private String osName;
    private int noCPUs;
    private long cpuHZ;

    private long memTotalBytes;
    private long memUsedBytes;

    private long phyMemTotalBytes;
    private long phyMemUsedBytes;

    private int processPID;
    private String processName;

    private double processLoad;
    private double systemLoadAverage;

    public BackgroundInstrument(String filename) {
        this.filename = filename;
        System.out.println("Background instrumentation output will be collected at the file: " + filename + OS_FILE);

        this.measurementsQueue = new LinkedList<>();
    }

    private BackgroundInstrument() {}

    public long getResidentBytes() {
        return residentBytes;
    }

    public void setResidentBytes(long residentBytes) {
        this.residentBytes = residentBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getSysUptime() {
        return sysUptime;
    }

    public void setSysUptime(long sysUptime) {
        this.sysUptime = sysUptime;
    }

    public long getUserUptime() {
        return userUptime;
    }

    public void setUserUptime(long userUptime) {
        this.userUptime = userUptime;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public int getNoCPUs() {
        return noCPUs;
    }

    public void setNoCPUs(int noCPUs) {
        this.noCPUs = noCPUs;
    }

    public long getCpuHZ() {
        return cpuHZ;
    }

    public void setCpuHZ(long cpuHZ) {
        this.cpuHZ = cpuHZ;
    }

    public long getMemTotalBytes() {
        return memTotalBytes;
    }

    public void setMemTotalBytes(long memTotalBytes) {
        this.memTotalBytes = memTotalBytes;
    }

    public long getMemUsedBytes() {
        return memUsedBytes;
    }

    public void setMemUsedBytes(long memUsedBytes) {
        this.memUsedBytes = memUsedBytes;
    }

    public int getProcessPID() {
        return processPID;
    }

    public void setProcessPID(int processPID) {
        this.processPID = processPID;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public double getProcessLoad() {
        return processLoad;
    }

    public void setProcessLoad(double processLoad) {
        this.processLoad = processLoad;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public long getPhyMemTotalBytes() {
        return phyMemTotalBytes;
    }

    public void setPhyMemTotalBytes(long phyMemTotalBytes) {
        this.phyMemTotalBytes = phyMemTotalBytes;
    }

    public long getPhyMemUsedBytes() {
        return phyMemUsedBytes;
    }

    public void setPhyMemUsedBytes(long phyMemUsedBytes) {
        this.phyMemUsedBytes = phyMemUsedBytes;
    }

    public String getType() {
        return type.toString();
    }

    public static BackgroundInstrument measure() {
        JavaSysMon monitor = new JavaSysMon();

        BackgroundInstrument osMetrics = new BackgroundInstrument();

        OsProcess process = monitor.processTree().find(monitor.currentPid());
        ProcessInfo processInfo = process.processInfo();
        osMetrics.setResidentBytes(processInfo.getResidentBytes());
        osMetrics.setTotalBytes(processInfo.getTotalBytes());
        osMetrics.setUserUptime(processInfo.getUserMillis());
        osMetrics.setSysUptime(processInfo.getSystemMillis());

        osMetrics.setOsName(monitor.osName());
        osMetrics.setNoCPUs(monitor.numCpus());
        osMetrics.setCpuHZ(monitor.cpuFrequencyInHz());

        MemoryStats memoryStats = monitor.swap();
        osMetrics.setMemTotalBytes(memoryStats.getTotalBytes());
        osMetrics.setMemUsedBytes(memoryStats.getTotalBytes() - memoryStats.getFreeBytes());

        MemoryStats phyMemoryStats = monitor.physical();
        osMetrics.setPhyMemTotalBytes(phyMemoryStats.getTotalBytes());
        osMetrics.setPhyMemUsedBytes(phyMemoryStats.getTotalBytes() - phyMemoryStats.getFreeBytes());

        osMetrics.setProcessPID(monitor.currentPid());
        osMetrics.setProcessName(processInfo.getName());

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);

        // What % CPU load this current JVM is taking, from 0.0-1.0
        osMetrics.setProcessLoad(osBean.getProcessCpuLoad());

        // What % load the overall system is at, from 0.0-1.0
        osMetrics.setSystemLoadAverage(osBean.getSystemLoadAverage());

        return osMetrics;
    }

    public void start() {

        if (future == null) {

            try (FileWriter fileWriter = new FileWriter(new File(filename + OS_FILE), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                bufferedWriter.write(new BackgroundInstrument().tsvHeader());
                bufferedWriter.newLine();

            } catch (IOException e) {
                System.out.println("Unable to write HEADER stats from BackgroundInstrument to file");
            }

            scheduler = Executors.newScheduledThreadPool(1);
            future = scheduler.scheduleAtFixedRate(() -> {

//                OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
//                if(os instanceof UnixOperatingSystemMXBean){
//                    System.out.println("Number of open fd: " + ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
//                }

                synchronized (LOCK_MEASUREMENTS_QUEUE) {
                    Metrics metrics = measure();
                    measurementsQueue.add(metrics);
                }

            }, 0, 5, TimeUnit.SECONDS);
        }
    }

    public void flush() {

        synchronized (LOCK_MEASUREMENTS_QUEUE) {

            try (FileWriter fileWriter = new FileWriter(new File(filename + OS_FILE), true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                for (Metrics metrics : measurementsQueue) {
                    bufferedWriter.write(metrics.tsv());
                    bufferedWriter.newLine();
                }

            } catch (IOException e) {
                System.out.println("Unable to write stats from BackgroundInstrument to file");
            }

            measurementsQueue.clear();

        }
    }

    public void stop() {

        if (future != null) {
            future.cancel(true);
            scheduler.shutdown();
            future = null;
        }
    }

    @Override
    public String tsvHeader() {
        return "Type"+TAB+"OS"+TAB+"No CPUs"+TAB+"CPU Hz"+TAB+"Mem Used Bytes"+TAB+"Mem Total Bytes"+TAB+"PID"+TAB+"Process Name"+TAB+
                "Resident Bytes"+TAB+"Total Bytes"+TAB+"User Uptime"+TAB+"System Uptime"+TAB+"CPU Process Load"+TAB+
                "System Load Average"+TAB+"Physical Mem Total Bytes"+TAB+"Physical Mem Used Bytes";
    }

    @Override
    public String tsv() {
        return getType() + TAB + getOsName() + TAB + getNoCPUs() + TAB + getCpuHZ() + TAB + getMemUsedBytes() + TAB + getMemTotalBytes() + TAB +
                getProcessPID() + TAB + getProcessName() + TAB + getResidentBytes() + TAB + getTotalBytes() + TAB +
                getUserUptime() + TAB + getSysUptime() + TAB + getProcessLoad() + TAB + getSystemLoadAverage() + TAB +
                getPhyMemTotalBytes() + TAB + getPhyMemUsedBytes();
    }

    @Override
    public void setStatsType(StatsTYPE statsType) {
        // NOTHING
    }

    @Override
    public void setSubType(StatsTYPE subtype) {
        // NOTHING
    }

}

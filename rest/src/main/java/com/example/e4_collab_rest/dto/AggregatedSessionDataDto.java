package com.example.e4_collab_rest.dto;

import com.example.e4_collab_rest.entity.SensorSession;
import com.example.e4_collab_rest.entity.Session;


public final class AggregatedSessionDataDto {
    private Long id;
    private String username;
    private String deviceName;
    private Long startTimestamp;
    private Long endTimestamp;
    private Long duration;
    private Integer accSampleRate;
    private String accData;
    private Integer bvpSampleRate;
    private String bvpData;
    private Integer edaSampleRate;
    private String edaData;
    private Integer hrSampleRate;
    private String hrData;
    private String ibiData;
    private String tagsData;
    private Integer tempSampleRate;
    private String tempData;

    public AggregatedSessionDataDto(
            Session session,
            SensorSession accSensorSession,
            SensorSession bvpSensorSession,
            SensorSession edaSensorSession,
            SensorSession hrSensorSession,
            SensorSession ibiSensorSession,
            SensorSession tagsBtnSensorSession,
            SensorSession tempSensorSession) {
                    this.id = session != null ? session.getId() : null;
                    this.username = session != null ? session.getUsername() : null;
                    this.deviceName = session != null ? session.getDeviceName() : null;
                    this.startTimestamp = session != null ? session.getStartTimestamp() : null;
                    this.endTimestamp = session != null ? session.getEndTimestamp() : null;
                    this.duration = session != null ? session.getDuration() : null;
                    this.accSampleRate = accSensorSession != null ? accSensorSession.getSampleRate() : null;
                    this.accData = accSensorSession != null ? accSensorSession.getData() : null;
                    this.bvpSampleRate = bvpSensorSession != null ? bvpSensorSession.getSampleRate() : null;
                    this.bvpData = bvpSensorSession != null ? bvpSensorSession.getData() : null;
                    this.edaSampleRate = edaSensorSession != null ? edaSensorSession.getSampleRate() : null;
                    this.edaData = edaSensorSession != null ? edaSensorSession.getData() : null;
                    this.hrSampleRate = hrSensorSession != null ? hrSensorSession.getSampleRate() : null;
                    this.hrData = hrSensorSession != null ? hrSensorSession.getData() : null;
                    this.ibiData = ibiSensorSession != null ? ibiSensorSession.getData() : null;
                    this.tagsData = tagsBtnSensorSession != null ? tagsBtnSensorSession.getData() : null;
                    this.tempSampleRate = tempSensorSession != null ? tempSensorSession.getSampleRate() : null;
                    this.tempData = tempSensorSession != null ? tempSensorSession.getData() : null;
    }

    public AggregatedSessionDataDto(
            Long id,
            String username,
            String deviceName,
            Long startTimestamp,
            Long endTimestamp,
            Long duration,
            Integer accSampleRate,
            String accData,
            Integer bvpSampleRate,
            String bvpData,
            Integer edaSampleRate,
            String edaData,
            Integer hrSampleRate,
            String hrData,
            String ibiData,
            String tagsData,
            Integer tempSampleRate,
            String tempData) {
        this.id = id;
        this.username = username;
        this.deviceName = deviceName;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.duration = duration;
        this.accSampleRate = accSampleRate;
        this.accData = accData;
        this.bvpSampleRate = bvpSampleRate;
        this.bvpData = bvpData;
        this.edaSampleRate = edaSampleRate;
        this.edaData = edaData;
        this.hrSampleRate = hrSampleRate;
        this.hrData = hrData;
        this.ibiData = ibiData;
        this.tagsData = tagsData;
        this.tempSampleRate = tempSampleRate;
        this.tempData = tempData;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getAccSampleRate() {
        return accSampleRate;
    }

    public void setAccSampleRate(Integer accSampleRate) {
        this.accSampleRate = accSampleRate;
    }

    public String getAccData() {
        return accData;
    }

    public void setAccData(String accData) {
        this.accData = accData;
    }

    public Integer getBvpSampleRate() {
        return bvpSampleRate;
    }

    public void setBvpSampleRate(Integer bvpSampleRate) {
        this.bvpSampleRate = bvpSampleRate;
    }

    public String getBvpData() {
        return bvpData;
    }

    public void setBvpData(String bvpData) {
        this.bvpData = bvpData;
    }

    public Integer getEdaSampleRate() {
        return edaSampleRate;
    }

    public void setEdaSampleRate(Integer edaSampleRate) {
        this.edaSampleRate = edaSampleRate;
    }

    public String getEdaData() {
        return edaData;
    }

    public void setEdaData(String edaData) {
        this.edaData = edaData;
    }

    public Integer getHrSampleRate() {
        return hrSampleRate;
    }

    public void setHrSampleRate(Integer hrSampleRate) {
        this.hrSampleRate = hrSampleRate;
    }

    public String getHrData() {
        return hrData;
    }

    public void setHrData(String hrData) {
        this.hrData = hrData;
    }

    public String getIbiData() {
        return ibiData;
    }

    public void setIbiData(String ibiData) {
        this.ibiData = ibiData;
    }

    public String getTagsData() {
        return tagsData;
    }

    public void setTagsData(String tagsData) {
        this.tagsData = tagsData;
    }

    public Integer getTempSampleRate() {
        return tempSampleRate;
    }

    public void setTempSampleRate(Integer tempSampleRate) {
        this.tempSampleRate = tempSampleRate;
    }

    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }

}

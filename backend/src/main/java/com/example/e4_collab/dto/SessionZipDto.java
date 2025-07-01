package com.example.e4_collab.dto;

public final class SessionZipDto {
    private Long id;
    private String username;
    private String deviceName;
    private Long startTimestamp;
    private Long endTimestamp;
    private byte[] content;

    public SessionZipDto(
            Long id,
            String username,
            String deviceName,
            Long startTimestamp,
            Long endTimestamp,
            byte[] content
    ) {
        this.id = id;
        this.username = username;
        this.deviceName = deviceName;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.content = content;
    }

    public String getFileName() {
        return this.startTimestamp != null && this.deviceName != null ? startTimestamp + "_" + deviceName + ".zip" : null;
    }

    public Long getDuration() {
        return this.startTimestamp != null && this.endTimestamp != null ? this.endTimestamp - this.startTimestamp : null;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
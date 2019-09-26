package com.x3noku.daily_maps_android;

import com.google.android.gms.maps.model.LatLng;

public class TaskInfo {
    private String nameOfTask;
    private int startTimeOfTask;
    private long durationOfTask;
    private byte priorityOfTask;
    private LatLng coordinatesOfTask;

    TaskInfo() {
        //ToDo: add dependence to settings values
        nameOfTask = "";
        startTimeOfTask = 12*60;
        durationOfTask = 15;
        priorityOfTask = 3;
    }

    public void setNameOfTask(String nameOfTask) {
        this.nameOfTask = nameOfTask;
    }
    public void setStartTimeOfTask(int startTimeOfTask) {
        this.startTimeOfTask = startTimeOfTask;
    }
    public void setDurationOfTask(long durationOfTask) {
        this.durationOfTask = durationOfTask;
    }
    public void setPriorityOfTask(byte priorityOfTask) {
        this.priorityOfTask = priorityOfTask;
    }
    public void setCoordinatesOfTask(LatLng coordinatesOfTask) {
        this.coordinatesOfTask = coordinatesOfTask;
    }

    public String getNameOfTask() {
        return nameOfTask;
    }
    public int getStartTimeOfTask() {
        return startTimeOfTask;
    }
    public long getDurationOfTask() {
        return durationOfTask;
    }
    public byte getPriorityOfTask() {
        return priorityOfTask;
    }
    public LatLng getCoordinatesOfTask() {
        return coordinatesOfTask;
    }

}
